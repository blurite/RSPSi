package com.rspsi.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shapes the terrain along a drawn coast line. The sea side is flattened to sea level (height 0); the land side
 * either slopes down to sea level (beach) or rises steeply to a plateau (cliff). Heights follow the editor
 * convention: negative is higher, 0 is sea level.
 */
public class CoastShaper {

	/** Tiles over which a cliff plateau fades back into the original terrain. */
	private static final double PLATEAU_FADE = 6;

	public static class Params {
		/** True when the sea lies on the left of the drawing direction. */
		public boolean seaLeft = true;
		public double seaWidth = 8;
		public boolean cliff = false;
		public double beachWidth = 24;
		public double cliffHeight = 300;
		public double cliffWidth = 2;
		public double plateauDepth = 10;
	}

	/**
	 * Makes a drawn coast line look natural: resamples it to one point per tile and pushes every point sideways by
	 * smooth, seeded noise, so the shore gets small bays and headlands. Same input always gives the same line.
	 */
	public static List<double[]> meander(List<double[]> line, double amplitude, long seed) {
		List<double[]> dense = resample(line, 1.0);
		if (amplitude <= 0 || dense.size() < 3)
			return dense;
		List<double[]> out = new ArrayList<>();
		double arc = 0;
		int n = dense.size();
		for (int i = 0; i < n; i++) {
			double[] a = dense.get(Math.max(i - 1, 0));
			double[] b = dense.get(Math.min(i + 1, n - 1));
			double dx = b[0] - a[0], dy = b[1] - a[1];
			double len = Math.hypot(dx, dy);
			double[] p = dense.get(i);
			if (i > 0)
				arc += Math.hypot(p[0] - dense.get(i - 1)[0], p[1] - dense.get(i - 1)[1]);
			// two octaves: broad bays (about 14 tiles) plus small irregularities (about 5 tiles)
			double offset = (noise(arc / 14.0, seed) * 0.7 + noise(arc / 5.0, seed + 101) * 0.3) * amplitude;
			// the ends stay where they were drawn
			double fade = Math.min(1, Math.min(i, n - 1 - i) / 4.0);
			offset *= fade;
			out.add(len == 0 ? p.clone() : new double[] { p[0] - dy / len * offset, p[1] + dx / len * offset });
		}
		return out;
	}

	/** Points spaced {@code step} apart along the line. */
	static List<double[]> resample(List<double[]> line, double step) {
		List<double[]> out = new ArrayList<>();
		if (line.isEmpty())
			return out;
		out.add(line.get(0).clone());
		double carry = 0;
		for (int i = 0; i < line.size() - 1; i++) {
			double[] a = line.get(i), b = line.get(i + 1);
			double seg = Math.hypot(b[0] - a[0], b[1] - a[1]);
			double pos = step - carry;
			while (pos <= seg) {
				double t = pos / seg;
				out.add(new double[] { a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t });
				pos += step;
			}
			carry = seg - (pos - step);
		}
		double[] last = line.get(line.size() - 1);
		double[] end = out.get(out.size() - 1);
		if (Math.hypot(last[0] - end[0], last[1] - end[1]) > 1e-6)
			out.add(last.clone());
		return out;
	}

	/** Smooth 1D value noise in [-1, 1]. */
	private static double noise(double x, long seed) {
		int i = (int) Math.floor(x);
		double f = x - i;
		return lerp(hash(i, seed), hash(i + 1, seed), f * f * (3 - 2 * f));
	}

	private static double hash(int i, long seed) {
		long h = i * 0x9E3779B97F4A7C15L + seed * 0xC2B2AE3D27D4EB4FL;
		h ^= h >>> 29;
		h *= 0xBF58476D1CE4E5B9L;
		h ^= h >>> 32;
		return ((h & 0xFFFFFF) / (double) 0xFFFFFF) * 2 - 1;
	}

	private static double lerp(double a, double b, double t) {
		return a + (b - a) * t;
	}

	/** Moves every point of the line sideways by {@code distance} towards the sea. */
	public static List<double[]> shiftToSea(List<double[]> line, double distance, boolean seaLeft) {
		List<double[]> out = new ArrayList<>();
		int n = line.size();
		for (int i = 0; i < n; i++) {
			double[] a = line.get(Math.max(i - 1, 0));
			double[] b = line.get(Math.min(i + 1, n - 1));
			double dx = b[0] - a[0], dy = b[1] - a[1];
			double len = Math.hypot(dx, dy);
			if (len == 0) {
				out.add(line.get(i).clone());
				continue;
			}
			double side = seaLeft ? 1 : -1;
			out.add(new double[] { line.get(i)[0] - dy / len * distance * side,
					line.get(i)[1] + dx / len * distance * side });
		}
		return out;
	}

	/** Distance to the line, positive on the sea side and negative on the land side. */
	public static double seaDistance(List<double[]> line, double px, double py, boolean seaLeft) {
		if (line.size() == 1) {
			double[] p = line.get(0);
			return Math.hypot(px - p[0], py - p[1]);
		}
		double best = Double.MAX_VALUE, sign = 1;
		for (int i = 0; i < line.size() - 1; i++) {
			double[] a = line.get(i), b = line.get(i + 1);
			double dx = b[0] - a[0], dy = b[1] - a[1];
			double lenSq = dx * dx + dy * dy;
			double t = lenSq == 0 ? 0 : Math.max(0, Math.min(1, ((px - a[0]) * dx + (py - a[1]) * dy) / lenSq));
			double dist = Math.hypot(px - (a[0] + t * dx), py - (a[1] + t * dy));
			if (dist < best) {
				best = dist;
				double cross = dx * (py - a[1]) - dy * (px - a[0]);
				sign = (cross >= 0) == seaLeft ? 1 : -1;
			}
		}
		return best * sign;
	}

	/**
	 * @param heights current heights [x][y] of the level that is edited
	 * @return the height change per vertex, keyed by {@code x << 16 | y}; to be added to the current heights
	 */
	public static Map<Integer, Integer> shape(List<double[]> line, Params p, int[][] heights) {
		Map<Integer, Integer> changes = new HashMap<>();
		if (line.isEmpty())
			return changes;
		double landReach = p.cliff ? p.cliffWidth + p.plateauDepth + PLATEAU_FADE : p.beachWidth;
		double reach = Math.max(p.seaWidth, landReach);
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
		for (double[] pt : line) {
			minX = Math.min(minX, pt[0]);
			minY = Math.min(minY, pt[1]);
			maxX = Math.max(maxX, pt[0]);
			maxY = Math.max(maxY, pt[1]);
		}
		int mapWidth = heights.length, mapLength = heights[0].length;
		for (int x = Math.max(0, (int) (minX - reach)); x <= Math.min(mapWidth - 1, (int) (maxX + reach) + 1); x++) {
			for (int y = Math.max(0, (int) (minY - reach)); y <= Math.min(mapLength - 1, (int) (maxY + reach) + 1); y++) {
				double sd = seaDistance(line, x, y, p.seaLeft);
				double d = Math.abs(sd);
				int current = heights[x][y];
				double target;
				if (sd >= 0) {
					if (d > p.seaWidth)
						continue;
					target = 0;
				} else if (p.cliff) {
					if (d > landReach)
						continue;
					double rise = Math.min(1, d / Math.max(p.cliffWidth, 0.01));
					double lift = d <= p.cliffWidth + p.plateauDepth ? 1
							: 1 - smoothstep((d - p.cliffWidth - p.plateauDepth) / PLATEAU_FADE);
					target = current * rise - p.cliffHeight * lift * rise;
				} else {
					if (d >= p.beachWidth)
						continue;
					target = current * smoothstep(d / p.beachWidth);
				}
				int change = (int) Math.round(target) - current;
				if (change != 0)
					changes.put(x << 16 | y, change);
			}
		}
		return changes;
	}

	private static double smoothstep(double t) {
		t = Math.max(0, Math.min(1, t));
		return t * t * (3 - 2 * t);
	}
}
