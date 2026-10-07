package com.rspsi.tools;

import com.jagex.map.tile.ShapedTile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts a drawn polyline with a width into a set of overlay tiles, choosing for every tile the overlay shape
 * and rotation whose covered area best matches the ideal path area, while keeping the edges of neighbouring tiles
 * consistent. Shapes are taken from {@link ShapedTile}, so any shape the overlay painter can produce is a candidate.
 */
public class PathOverlayFitter {

	/** Sub samples per tile axis used to compare the ideal path area with a candidate shape. */
	private static final int SAMPLES = 8;
	private static final int FULL_SHAPE = 1;
	private static final int MAX_SHAPE = 12;
	private static final int WEST = 0, EAST = 1, SOUTH = 2, NORTH = 3;
	private static final int REFINE_PASSES = 6;

	/**
	 * The chosen shape for a tile. {@code shape} is the overlay paint shape id (1 = full tile, 2+ = shaped tiles)
	 * as used by {@code Options.overlayPaintShapeId}; the value stored in the map is {@code shape - 1}.
	 */
	public static final class Fit {
		public final int shape;
		public final int rotation;

		Fit(int shape, int rotation) {
			this.shape = shape;
			this.rotation = rotation;
		}
	}

	private static final class Candidate {
		final int shape;
		final int rotation;
		final boolean[] covered;
		/** Coverage just inside each tile side, indexed [side][position along the side]. */
		final boolean[][] edges;

		Candidate(int shape, int rotation, boolean[] covered, boolean[][] edges) {
			this.shape = shape;
			this.rotation = rotation;
			this.covered = covered;
			this.edges = edges;
		}
	}

	private static final Candidate EMPTY = new Candidate(0, 0, new boolean[SAMPLES * SAMPLES], new boolean[4][SAMPLES]);
	private static final List<Candidate> CANDIDATES = buildCandidates();

	/**
	 * @param points      waypoints in tile coordinates, a tile (x, y) spans [x, x + 1] x [y, y + 1]
	 * @param width       path width in tiles
	 * @param edgeWeight  how strongly neighbouring tiles are made to line up; 0 fits every tile on its own
	 * @return the fitted shape per tile, keyed by {@code x << 16 | y}; tiles without overlay are omitted
	 */
	public static Map<Integer, Fit> fit(List<double[]> points, double width, int mapWidth, int mapLength,
	                                    double edgeWeight) {
		Map<Integer, Fit> result = new HashMap<>();
		if (points.isEmpty())
			return result;

		double half = width / 2.0;
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
		for (double[] p : points) {
			minX = Math.min(minX, p[0]);
			minY = Math.min(minY, p[1]);
			maxX = Math.max(maxX, p[0]);
			maxY = Math.max(maxY, p[1]);
		}
		int startX = Math.max(0, (int) Math.floor(minX - half));
		int startY = Math.max(0, (int) Math.floor(minY - half));
		int endX = Math.min(mapWidth - 1, (int) Math.ceil(maxX + half));
		int endY = Math.min(mapLength - 1, (int) Math.ceil(maxY + half));

		// 1. ideal coverage and a per tile best guess
		Map<Integer, boolean[]> ideals = new HashMap<>();
		Map<Integer, Candidate> chosen = new HashMap<>();
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				if (distanceToPath(points, x + 0.5, y + 0.5) > half + 0.75) // tile cannot touch the path
					continue;
				boolean[] ideal = new boolean[SAMPLES * SAMPLES];
				int hits = 0;
				for (int sx = 0; sx < SAMPLES; sx++) {
					for (int sy = 0; sy < SAMPLES; sy++) {
						double px = x + (sx + 0.5) / SAMPLES;
						double py = y + (sy + 0.5) / SAMPLES;
						if (distanceToPath(points, px, py) <= half) {
							ideal[sx * SAMPLES + sy] = true;
							hits++;
						}
					}
				}
				if (hits == 0)
					continue;
				int key = x << 16 | y;
				ideals.put(key, ideal);
				chosen.put(key, best(ideal, null, 0, x, y, 0));
			}
		}

		// 2. let neighbouring tiles agree on their shared edges
		if (edgeWeight > 0) {
			for (int pass = 0; pass < REFINE_PASSES; pass++) {
				boolean changed = false;
				for (Map.Entry<Integer, boolean[]> entry : ideals.entrySet()) {
					int key = entry.getKey();
					Candidate better = best(entry.getValue(), chosen, edgeWeight, tileX(key), tileY(key), 1);
					if (better != chosen.get(key)) {
						chosen.put(key, better);
						changed = true;
					}
				}
				if (!changed)
					break;
			}
		}

		chosen.forEach((key, candidate) -> {
			if (candidate != EMPTY)
				result.put(key, new Fit(candidate.shape, candidate.rotation));
		});
		return result;
	}

	/** Picks the candidate with the best area match, minus a penalty for edges that do not line up with neighbours. */
	private static Candidate best(boolean[] ideal, Map<Integer, Candidate> chosen, double edgeWeight, int x, int y,
	                              int mode) {
		Candidate west = null, east = null, south = null, north = null;
		if (mode == 1) {
			west = neighbour(chosen, x - 1, y);
			east = neighbour(chosen, x + 1, y);
			south = neighbour(chosen, x, y - 1);
			north = neighbour(chosen, x, y + 1);
		}

		Candidate best = EMPTY;
		double bestScore = Double.NEGATIVE_INFINITY;
		for (int i = -1; i < CANDIDATES.size(); i++) {
			Candidate candidate = i < 0 ? EMPTY : CANDIDATES.get(i);
			int match = 0;
			for (int s = 0; s < ideal.length; s++) {
				if (ideal[s] == candidate.covered[s])
					match++;
			}
			double score = match;
			if (mode == 1) {
				score -= edgeWeight * (mismatch(candidate.edges[WEST], west.edges[EAST])
						+ mismatch(candidate.edges[EAST], east.edges[WEST])
						+ mismatch(candidate.edges[SOUTH], south.edges[NORTH])
						+ mismatch(candidate.edges[NORTH], north.edges[SOUTH]));
			}
			if (score > bestScore) {
				bestScore = score;
				best = candidate;
			}
		}
		return best;
	}

	private static Candidate neighbour(Map<Integer, Candidate> chosen, int x, int y) {
		if (x < 0 || y < 0)
			return EMPTY;
		Candidate candidate = chosen.get(x << 16 | y);
		return candidate == null ? EMPTY : candidate;
	}

	private static int mismatch(boolean[] a, boolean[] b) {
		int count = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i])
				count++;
		}
		return count;
	}

	/**
	 * Turns waypoints into a smooth curve that passes through every waypoint (Catmull-Rom spline), returned as a
	 * dense polyline suitable for {@link #fit}.
	 */
	public static List<double[]> smooth(List<double[]> points) {
		if (points.size() < 3)
			return new ArrayList<>(points);
		final int steps = 12;
		List<double[]> out = new ArrayList<>();
		int n = points.size();
		for (int i = 0; i < n - 1; i++) {
			double[] p0 = points.get(Math.max(i - 1, 0));
			double[] p1 = points.get(i);
			double[] p2 = points.get(i + 1);
			double[] p3 = points.get(Math.min(i + 2, n - 1));
			for (int s = 0; s < steps; s++) {
				double t = (double) s / steps;
				double t2 = t * t;
				double t3 = t2 * t;
				out.add(new double[] { catmullRom(p0[0], p1[0], p2[0], p3[0], t, t2, t3),
						catmullRom(p0[1], p1[1], p2[1], p3[1], t, t2, t3) });
			}
		}
		out.add(points.get(n - 1));
		return out;
	}

	private static double catmullRom(double p0, double p1, double p2, double p3, double t, double t2, double t3) {
		return 0.5 * (2 * p1 + (p2 - p0) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2 + (3 * p1 - p0 - 3 * p2 + p3) * t3);
	}

	public static int tileX(int key) {
		return key >>> 16;
	}

	public static int tileY(int key) {
		return key & 0xFFFF;
	}

	public static double distanceToPath(List<double[]> points, double px, double py) {
		if (points.size() == 1) {
			double[] p = points.get(0);
			return Math.hypot(px - p[0], py - p[1]);
		}
		double best = Double.MAX_VALUE;
		for (int i = 0; i < points.size() - 1; i++) {
			best = Math.min(best, distanceToSegment(points.get(i), points.get(i + 1), px, py));
		}
		return best;
	}

	private static double distanceToSegment(double[] a, double[] b, double px, double py) {
		double dx = b[0] - a[0];
		double dy = b[1] - a[1];
		double lenSq = dx * dx + dy * dy;
		double t = lenSq == 0 ? 0 : ((px - a[0]) * dx + (py - a[1]) * dy) / lenSq;
		t = Math.max(0, Math.min(1, t));
		return Math.hypot(px - (a[0] + t * dx), py - (a[1] + t * dy));
	}

	// ---- candidate shape geometry, mirrors the ShapedTile constructor ----

	private static List<Candidate> buildCandidates() {
		List<Candidate> list = new ArrayList<>();
		boolean[] full = new boolean[SAMPLES * SAMPLES];
		Arrays.fill(full, true);
		boolean[][] fullEdges = new boolean[4][SAMPLES];
		for (boolean[] edge : fullEdges)
			Arrays.fill(edge, true);
		list.add(new Candidate(FULL_SHAPE, 0, full, fullEdges));

		for (int shape = 2; shape <= MAX_SHAPE; shape++) {
			for (int rotation = 0; rotation < 4; rotation++) {
				int[][][] triangles = overlayTriangles(shape, rotation);
				boolean[] covered = new boolean[SAMPLES * SAMPLES];
				for (int sx = 0; sx < SAMPLES; sx++) {
					for (int sy = 0; sy < SAMPLES; sy++) {
						covered[sx * SAMPLES + sy] = inAny(triangles, (sx + 0.5) / SAMPLES * 128,
								(sy + 0.5) / SAMPLES * 128);
					}
				}
				boolean[][] edges = new boolean[4][SAMPLES];
				for (int k = 0; k < SAMPLES; k++) {
					double along = (k + 0.5) / SAMPLES * 128;
					edges[WEST][k] = inAny(triangles, 1, along);
					edges[EAST][k] = inAny(triangles, 127, along);
					edges[SOUTH][k] = inAny(triangles, along, 1);
					edges[NORTH][k] = inAny(triangles, along, 127);
				}
				list.add(new Candidate(shape, rotation, covered, edges));
			}
		}
		return list;
	}

	/** Unit square coordinates (0..128) of a ShapedTile vertex type. */
	private static int[] vertex(int type) {
		switch (type) {
			case 1: return new int[] { 0, 0 };
			case 2: return new int[] { 64, 0 };
			case 3: return new int[] { 128, 0 };
			case 4: return new int[] { 128, 64 };
			case 5: return new int[] { 128, 128 };
			case 6: return new int[] { 64, 128 };
			case 7: return new int[] { 0, 128 };
			case 8: return new int[] { 0, 64 };
			case 9: return new int[] { 64, 32 };
			case 10: return new int[] { 96, 64 };
			case 11: return new int[] { 64, 96 };
			case 12: return new int[] { 32, 64 };
			case 13: return new int[] { 32, 32 };
			case 14: return new int[] { 96, 32 };
			case 15: return new int[] { 96, 96 };
			default: return new int[] { 32, 96 };
		}
	}

	/** The triangles drawn with the overlay colour for a shape and rotation, in 0..128 tile coordinates. */
	private static int[][][] overlayTriangles(int shape, int orientation) {
		int[] tileShape = ShapedTile.tileShapePoints[shape];
		int[][] verts = new int[tileShape.length][];
		for (int idx = 0; idx < tileShape.length; idx++) {
			int vertexType = tileShape[idx];
			if ((vertexType & 1) == 0 && vertexType <= 8) {
				vertexType = (vertexType - orientation - orientation - 1 & 7) + 1;
			}
			if (vertexType > 8 && vertexType <= 12) {
				vertexType = (vertexType - 9 - orientation & 3) + 9;
			}
			if (vertexType > 12 && vertexType <= 16) {
				vertexType = (vertexType - 13 - orientation & 3) + 13;
			}
			verts[idx] = vertex(vertexType);
		}

		List<int[][]> triangles = new ArrayList<>();
		int[] data = ShapedTile.shapedTileElementData[shape];
		for (int i = 0; i + 3 < data.length; i += 4) {
			if (data[i] != 1) // flag 1 triangles are drawn with the overlay colour, flag 0 with the underlay
				continue;
			int[][] tri = new int[3][];
			for (int k = 0; k < 3; k++) {
				int v = data[i + 1 + k];
				if (v < 4)
					v = v - orientation & 3;
				tri[k] = verts[v];
			}
			triangles.add(tri);
		}
		return triangles.toArray(new int[0][][]);
	}

	private static boolean inAny(int[][][] triangles, double px, double py) {
		for (int[][] tri : triangles) {
			if (inTriangle(tri, px, py))
				return true;
		}
		return false;
	}

	private static boolean inTriangle(int[][] t, double px, double py) {
		double d1 = sign(px, py, t[0], t[1]);
		double d2 = sign(px, py, t[1], t[2]);
		double d3 = sign(px, py, t[2], t[0]);
		boolean neg = d1 < 0 || d2 < 0 || d3 < 0;
		boolean pos = d1 > 0 || d2 > 0 || d3 > 0;
		return !(neg && pos);
	}

	private static double sign(double px, double py, int[] a, int[] b) {
		return (px - b[0]) * (a[1] - b[1]) - (a[0] - b[0]) * (py - b[1]);
	}
}
