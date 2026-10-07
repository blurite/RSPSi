package com.rspsi.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Computes how much a mountain, hill or mountain range raises the terrain. Pure logic: it knows nothing about the
 * scene, it only returns the height change per height point (vertex), keyed by {@code x << 16 | y} like
 * {@link PathOverlayFitter}. Heights in the map are negative going up, so the returned values are zero or negative.
 * The result only depends on the input and {@link Params#seed}.
 */
public class MountainGenerator {

	public static final class Params {
		/** Height of the top, in height units (128 units is one tile). */
		public double height = 600;
		/** Radius of a hill, or total width of a range, in tiles. */
		public double size = 12;
		/** Number of noise octaves: 1 is smooth, higher adds more rocky detail. */
		public int octaves = 3;
		/** 0 = perfectly round outline, 1 = very irregular outline. */
		public double irregularity = 0.4;
		/** Extra tiles around the footprint over which the foot of the mountain fades out. */
		public double blend = 3;
		/** 1 = gentle bell, higher = steeper sides and a sharper top. */
		public double steepness = 1.5;
		/** Only for ranges: how much the top height varies along the crest (0 = even). */
		public double peakVariation = 0.5;
		/** Steep rock face along the edge, with a gentler slope above it. */
		public boolean cliff = false;
		/** Part of the height (0..1) that is reached by the cliff face itself. */
		public double cliffLevel = 0.6;
		/** Horizontal width of the cliff face in tiles: the smaller, the steeper. */
		public double cliffWidth = 2;
		public long seed = 1;
	}

	/** Noise wavelength of the outline, in tiles. */
	private static final double OUTLINE_SCALE = 14;
	/** Noise wavelength of the rocky detail, in tiles. */
	private static final double DETAIL_SCALE = 7;
	/** Noise wavelength of the height variation along a range, in tiles. */
	private static final double CREST_SCALE = 18;

	/** A single mountain around {@code (centerX, centerY)} (tile coordinates, vertex space). */
	public static Map<Integer, Integer> hill(double centerX, double centerY, Params p, int mapWidth, int mapLength) {
		double reach = p.size + p.blend;
		Map<Integer, Integer> out = new HashMap<>();
		if (p.size <= 0 || p.height == 0)
			return out;
		// the outline can bulge outwards, so look a little further than the nominal reach
		double search = reach * (1 + 0.6 * p.irregularity) + 1;
		for (int x = Math.max(0, (int) (centerX - search)); x <= Math.min(mapWidth, (int) (centerX + search) + 1); x++) {
			for (int y = Math.max(0, (int) (centerY - search)); y <= Math.min(mapLength, (int) (centerY + search) + 1); y++) {
				double distance = Math.hypot(x - centerX, y - centerY);
				double outline = fbm(x / OUTLINE_SCALE, y / OUTLINE_SCALE, 2, p.seed);
				double warped = distance / Math.max(0.3, 1 + 0.6 * p.irregularity * outline);
				put(out, x, y, p, warped / reach, reach, 1, mapWidth, mapLength);
			}
		}
		return out;
	}

	/** Cells per side of the hand drawn footprint of a hill. */
	public static final int SHAPE_GRID = 21;

	/**
	 * A suggestion for the footprint of a hill: an uneven blob around the middle of the grid. {@code irregularity}
	 * (0..1) decides how far the outline strays from a circle; the same seed gives the same blob.
	 * Indexed {@code [column][row]}, row 0 is the top (north) of the picture.
	 */
	public static boolean[][] suggestShape(long seed, double irregularity) {
		boolean[][] shape = new boolean[SHAPE_GRID][SHAPE_GRID];
		double half = SHAPE_GRID / 2.0;
		double amount = 0.15 + 0.6 * irregularity;
		for (int x = 0; x < SHAPE_GRID; x++) {
			for (int y = 0; y < SHAPE_GRID; y++) {
				double dx = (x + 0.5 - half) / half, dy = (y + 0.5 - half) / half;
				double distance = Math.hypot(dx, dy);
				// a few big lobes plus smaller bumps around the outline
				double edge = 0.62 + amount * (0.7 * fbm(x / 5.0 + 3.1, y / 5.0 + 7.7, 1, seed)
						+ 0.3 * fbm(x / 2.5, y / 2.5, 1, seed + 17));
				shape[x][y] = distance <= Math.min(0.95, edge);
			}
		}
		shape[SHAPE_GRID / 2][SHAPE_GRID / 2] = true;
		return shape;
	}

	/**
	 * The tiles covered by a hand drawn footprint that is stretched over a square of {@code 2 * radius} tiles around
	 * the centre. The edge of the drawing is smoothed so large radii do not get blocky outlines.
	 */
	public static Set<Integer> shapeTiles(boolean[][] shape, int centerX, int centerY, double radius, int mapWidth,
	                                      int mapLength) {
		Set<Integer> tiles = new java.util.HashSet<>();
		int grid = shape.length;
		int reach = (int) Math.ceil(radius);
		for (int tx = Math.max(0, centerX - reach); tx <= Math.min(mapWidth, centerX + reach); tx++) {
			for (int ty = Math.max(0, centerY - reach); ty <= Math.min(mapLength, centerY + reach); ty++) {
				double u = (tx + 0.5 - (centerX + 0.5 - radius)) / (2 * radius) * grid - 0.5;
				// north (higher y) is the top of the picture
				double v = grid - 1 - ((ty + 0.5 - (centerY + 0.5 - radius)) / (2 * radius) * grid - 0.5);
				if (sample(shape, u, v) >= 0.5)
					tiles.add(tx << 16 | ty);
			}
		}
		return tiles;
	}

	/** Bilinear value of the drawing at cell coordinates (cell centres lie on whole numbers). */
	private static double sample(boolean[][] shape, double u, double v) {
		int grid = shape.length;
		int x0 = (int) Math.floor(u), y0 = (int) Math.floor(v);
		double fx = u - x0, fy = v - y0;
		double top = cell(shape, x0, y0, grid) * (1 - fx) + cell(shape, x0 + 1, y0, grid) * fx;
		double bottom = cell(shape, x0, y0 + 1, grid) * (1 - fx) + cell(shape, x0 + 1, y0 + 1, grid) * fx;
		return top * (1 - fy) + bottom * fy;
	}

	private static double cell(boolean[][] shape, int x, int y, int grid) {
		return x >= 0 && y >= 0 && x < grid && y < grid && shape[x][y] ? 1 : 0;
	}

	/** A mountain range along the (already smoothed) {@code line}; {@link Params#size} is the width. */
	public static Map<Integer, Integer> ridge(List<double[]> line, Params p, int mapWidth, int mapLength) {
		Map<Integer, Integer> out = new HashMap<>();
		if (line.size() < 2 || p.size <= 0 || p.height == 0)
			return out;
		double reach = p.size / 2.0 + p.blend;
		double search = reach * (1 + 0.6 * p.irregularity) + 1;
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
		for (double[] point : line) {
			minX = Math.min(minX, point[0]);
			minY = Math.min(minY, point[1]);
			maxX = Math.max(maxX, point[0]);
			maxY = Math.max(maxY, point[1]);
		}
		double[] nearest = new double[2];
		for (int x = Math.max(0, (int) (minX - search)); x <= Math.min(mapWidth, (int) (maxX + search) + 1); x++) {
			for (int y = Math.max(0, (int) (minY - search)); y <= Math.min(mapLength, (int) (maxY + search) + 1); y++) {
				double distance = nearestOnLine(line, x, y, nearest);
				if (distance > search)
					continue;
				double outline = fbm(x / OUTLINE_SCALE, y / OUTLINE_SCALE, 2, p.seed);
				double warped = distance / Math.max(0.3, 1 + 0.6 * p.irregularity * outline);
				// tops and saddles along the crest, taken at the closest point of the line
				double crest = fbm(nearest[0] / CREST_SCALE, nearest[1] / CREST_SCALE, 2, p.seed + 7919);
				double peak = Math.max(0.15, 1 + p.peakVariation * crest);
				put(out, x, y, p, warped / reach, reach, peak, mapWidth, mapLength);
			}
		}
		return out;
	}

	/**
	 * A mountain that fills the selected tiles ({@code x << 16 | y} keys): highest in the middle of the area and
	 * fading out towards its edge. Every corner of a selected tile is raised.
	 */
	public static Map<Integer, Integer> area(Set<Integer> tiles, Params p, int mapWidth, int mapLength) {
		Map<Integer, Integer> out = new HashMap<>();
		if (tiles.isEmpty() || p.height == 0)
			return out;
		int pad = (int) Math.ceil(p.blend) + 2;
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = 0, maxY = 0;
		for (int key : tiles) {
			minX = Math.min(minX, PathOverlayFitter.tileX(key));
			minY = Math.min(minY, PathOverlayFitter.tileY(key));
			maxX = Math.max(maxX, PathOverlayFitter.tileX(key) + 1);
			maxY = Math.max(maxY, PathOverlayFitter.tileY(key) + 1);
		}
		int ox = minX - pad, oy = minY - pad;
		int w = maxX - minX + 1 + 2 * pad, h = maxY - minY + 1 + 2 * pad;
		boolean[][] inside = new boolean[w][h];
		for (int key : tiles) {
			int tx = PathOverlayFitter.tileX(key) - ox, ty = PathOverlayFitter.tileY(key) - oy;
			for (int dx = 0; dx <= 1; dx++)
				for (int dy = 0; dy <= 1; dy++)
					inside[tx + dx][ty + dy] = true;
		}
		// distance of every point to the other side of the edge: >0 inside, <0 outside
		double[][] toOutside = distanceTransform(inside, false);
		double[][] toInside = distanceTransform(inside, true);
		double deepest = 0;
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				deepest = Math.max(deepest, toOutside[x][y]);
		if (deepest <= 0)
			return out;
		double reach = deepest + p.blend;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int vx = x + ox, vy = y + oy;
				double signed = inside[x][y] ? toOutside[x][y] : -toInside[x][y];
				if (signed < -p.blend - 1)
					continue;
				double outline = fbm(vx / OUTLINE_SCALE, vy / OUTLINE_SCALE, 2, p.seed);
				signed += p.irregularity * outline * Math.min(deepest, 4);
				// 0 at the outer edge of the foot, 1 in the deepest part of the area
				put(out, vx, vy, p, 1 - (signed + p.blend) / reach, reach, 1, mapWidth, mapLength);
			}
		}
		return out;
	}

	/**
	 * @param t     0 at the top, 1 and above outside the footprint
	 * @param reach distance in tiles from the top to the outer edge of the foot
	 * @param scale extra factor for the top height (range crest variation)
	 */
	private static void put(Map<Integer, Integer> out, int x, int y, Params p, double t, double reach, double scale,
	                        int mapWidth, int mapLength) {
		if (x < 0 || y < 0 || x > mapWidth || y > mapLength || t >= 1)
			return;
		double s = 1 - Math.max(0, t);
		double profile = Math.pow(s * s * (3 - 2 * s), p.steepness);
		if (p.cliff) {
			// s runs from 0 at the foot to 1 at the top; the face takes the first part of it
			double face = Math.min(0.95, Math.max(0.02, p.cliffWidth / reach));
			if (s < face) {
				double u = s / face;
				profile = p.cliffLevel * Math.pow(u * u * (3 - 2 * u), 0.7);
			} else {
				double u = (s - face) / (1 - face);
				profile = p.cliffLevel + (1 - p.cliffLevel) * Math.pow(u * u * (3 - 2 * u), p.steepness);
			}
		}
		double detail = 1;
		if (p.octaves > 1) {
			// rocky detail, strongest on the slopes and fading to nothing at the foot and the top
			detail += 0.45 * fbm(x / DETAIL_SCALE, y / DETAIL_SCALE, p.octaves, p.seed + 31) * (p.octaves - 1) / 5.0
					* 4 * profile * (1 - profile);
		}
		double raise = p.height * scale * profile * Math.max(0, detail);
		int value = (int) Math.round(raise);
		if (value != 0)
			out.put(x << 16 | y, -value);
	}

	/** Distance of the closest point of the polyline to (px, py); that point is stored in {@code nearest}. */
	private static double nearestOnLine(List<double[]> line, double px, double py, double[] nearest) {
		double best = Double.MAX_VALUE;
		for (int i = 0; i < line.size() - 1; i++) {
			double[] a = line.get(i), b = line.get(i + 1);
			double dx = b[0] - a[0], dy = b[1] - a[1];
			double lenSq = dx * dx + dy * dy;
			double t = lenSq == 0 ? 0 : Math.max(0, Math.min(1, ((px - a[0]) * dx + (py - a[1]) * dy) / lenSq));
			double cx = a[0] + t * dx, cy = a[1] + t * dy;
			double d = Math.hypot(px - cx, py - cy);
			if (d < best) {
				best = d;
				nearest[0] = cx;
				nearest[1] = cy;
			}
		}
		return best;
	}

	/**
	 * Distance from every cell to the closest cell of the other kind. With {@code fromInside} false that is, for a
	 * cell inside the area, the distance to the nearest outside cell; with {@code fromInside} true it is, for a cell
	 * outside the area, the distance to the nearest inside cell. Cells of the wrong kind get 0 or a huge value.
	 */
	private static double[][] distanceTransform(boolean[][] inside, boolean fromInside) {
		int w = inside.length, h = inside[0].length;
		double inf = 1e9;
		double[][] d = new double[w][h];
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				d[x][y] = inside[x][y] == fromInside ? 0 : inf;
		double diag = Math.sqrt(2);
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (x > 0) d[x][y] = Math.min(d[x][y], d[x - 1][y] + 1);
				if (y > 0) d[x][y] = Math.min(d[x][y], d[x][y - 1] + 1);
				if (x > 0 && y > 0) d[x][y] = Math.min(d[x][y], d[x - 1][y - 1] + diag);
				if (x > 0 && y < h - 1) d[x][y] = Math.min(d[x][y], d[x - 1][y + 1] + diag);
			}
		}
		for (int x = w - 1; x >= 0; x--) {
			for (int y = h - 1; y >= 0; y--) {
				if (x < w - 1) d[x][y] = Math.min(d[x][y], d[x + 1][y] + 1);
				if (y < h - 1) d[x][y] = Math.min(d[x][y], d[x][y + 1] + 1);
				if (x < w - 1 && y < h - 1) d[x][y] = Math.min(d[x][y], d[x + 1][y + 1] + diag);
				if (x < w - 1 && y > 0) d[x][y] = Math.min(d[x][y], d[x + 1][y - 1] + diag);
			}
		}
		return d;
	}

	// ---- seeded value noise ----

	/** Fractal noise in roughly [-1, 1]. */
	static double fbm(double x, double y, int octaves, long seed) {
		double sum = 0, amplitude = 1, total = 0;
		for (int i = 0; i < octaves; i++) {
			sum += amplitude * noise(x, y, seed + i * 1013L);
			total += amplitude;
			amplitude *= 0.5;
			x *= 2;
			y *= 2;
		}
		return sum / total;
	}

	/** Smooth value noise in [-1, 1]. */
	private static double noise(double x, double y, long seed) {
		int x0 = (int) Math.floor(x), y0 = (int) Math.floor(y);
		double fx = x - x0, fy = y - y0;
		double sx = fx * fx * (3 - 2 * fx), sy = fy * fy * (3 - 2 * fy);
		double top = lerp(lattice(x0, y0, seed), lattice(x0 + 1, y0, seed), sx);
		double bottom = lerp(lattice(x0, y0 + 1, seed), lattice(x0 + 1, y0 + 1, seed), sx);
		return lerp(top, bottom, sy);
	}

	private static double lerp(double a, double b, double t) {
		return a + (b - a) * t;
	}

	private static double lattice(int x, int y, long seed) {
		long h = seed * 0x9E3779B97F4A7C15L + x * 0xC2B2AE3D27D4EB4FL + y * 0x165667B19E3779F9L;
		h ^= h >>> 33;
		h *= 0xFF51AFD7ED558CCDL;
		h ^= h >>> 33;
		h *= 0xC4CEB9FE1A85EC53L;
		h ^= h >>> 33;
		return ((h >>> 11) / (double) (1L << 53)) * 2 - 1;
	}
}
