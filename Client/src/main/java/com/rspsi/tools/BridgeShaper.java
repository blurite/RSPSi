package com.rspsi.tools;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Works out the heights of a bridge: a deck along the drawn line, with an optional arch, and a ramp around it that runs
 * down to the surrounding terrain. Pure calculation, so it can be tested without a loaded map.
 * <p>
 * The map is drawn from the heights of the tile <em>corners</em> (a tile uses its own corner and the three next to it),
 * so a deck tile is only flat when all four corners have the deck height. Corners and tiles are identified by
 * {@code x << 16 | y}. Heights follow the cache: negative is higher.
 */
public class BridgeShaper {

	/** Fixed deck height, or {@link #AUTO} to start and end at the height of the ground at the first and last point. */
	public static final int AUTO = Integer.MAX_VALUE;

	/** The cache stores a level above another as a byte of 8 units, so it can lie at most this far above it. */
	private static final int MAX_ABOVE_LEVEL = 255 * 8;

	public static class Params {
		/** Number of corners around the deck over which the terrain slopes down to the ground. 0 gives a hard edge. */
		public int rampWidth = 1;
		/** Fixed deck height (negative = higher), or {@link #AUTO}. */
		public int deckHeight = AUTO;
		/** How far the middle of the bridge lies above a straight line between its ends; 0 gives no arch. */
		public int arc = 0;
	}

	/** The new height of every corner that has to change; corners outside the map are never included. */
	public static class Result {
		public final Map<Integer, Integer> deck = new HashMap<>();
		public final Map<Integer, Integer> ramp = new HashMap<>();
	}

	public static int key(int x, int y) {
		return x << 16 | y;
	}

	/**
	 * The tiles of a deck drawn along {@code line} (tile centres): every tile whose centre is within half the width of
	 * the line. Tiles outside the map are left out.
	 */
	public static Set<Integer> deckTiles(List<double[]> line, double deckWidth, int mapWidth, int mapLength) {
		Set<Integer> tiles = new HashSet<>();
		if (line.isEmpty())
			return tiles;
		double reach = Math.max(deckWidth, 1) / 2.0;
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
		for (double[] p : line) {
			minX = Math.min(minX, p[0]);
			minY = Math.min(minY, p[1]);
			maxX = Math.max(maxX, p[0]);
			maxY = Math.max(maxY, p[1]);
		}
		for (int x = Math.max(0, (int) Math.floor(minX - reach)); x <= Math.min(mapWidth - 1, (int) Math.ceil(maxX + reach)); x++) {
			for (int y = Math.max(0, (int) Math.floor(minY - reach)); y <= Math.min(mapLength - 1, (int) Math.ceil(maxY + reach)); y++) {
				if (distanceToLine(line, x + 0.5, y + 0.5) <= reach + 1e-9)
					tiles.add(key(x, y));
			}
		}
		return tiles;
	}

	/**
	 * Distance to the line, with flat ends: the deck stops at the first and last point instead of reaching past them.
	 * Bends in between are rounded so the deck has no gaps there.
	 */
	private static double distanceToLine(List<double[]> line, double px, double py) {
		if (line.size() == 1)
			return Math.hypot(px - line.get(0)[0], py - line.get(0)[1]);
		double best = Double.MAX_VALUE;
		int last = line.size() - 2;
		for (int i = 0; i <= last; i++) {
			double ax = line.get(i)[0], ay = line.get(i)[1], bx = line.get(i + 1)[0], by = line.get(i + 1)[1];
			double dx = bx - ax, dy = by - ay;
			double len2 = dx * dx + dy * dy;
			double raw = len2 == 0 ? 0 : ((px - ax) * dx + (py - ay) * dy) / len2;
			if ((i == 0 && raw < 0) || (i == last && raw > 1))
				continue;
			double t = Math.max(0, Math.min(1, raw));
			best = Math.min(best, Math.hypot(px - (ax + t * dx), py - (ay + t * dy)));
		}
		return best;
	}

	/** How far along the line (0 = first point, 1 = last point) the point lies, by its nearest position on the line. */
	private static double progress(List<double[]> line, double px, double py) {
		double total = 0;
		for (int i = 0; i + 1 < line.size(); i++)
			total += Math.hypot(line.get(i + 1)[0] - line.get(i)[0], line.get(i + 1)[1] - line.get(i)[1]);
		if (total == 0)
			return 0.5;

		double best = Double.MAX_VALUE, bestAt = 0, walked = 0;
		for (int i = 0; i + 1 < line.size(); i++) {
			double ax = line.get(i)[0], ay = line.get(i)[1], bx = line.get(i + 1)[0], by = line.get(i + 1)[1];
			double dx = bx - ax, dy = by - ay;
			double len = Math.hypot(dx, dy);
			double t = len == 0 ? 0 : Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / (len * len)));
			double dist = Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
			if (dist < best) {
				best = dist;
				bestAt = walked + t * len;
			}
			walked += len;
		}
		return Math.max(0, Math.min(1, bestAt / total));
	}

	/** Height of the ground at the middle of a tile, from its four corners. */
	private static int groundAt(int[][] heights, double px, double py) {
		int x = Math.max(0, Math.min((int) Math.floor(px), heights.length - 2));
		int y = Math.max(0, Math.min((int) Math.floor(py), heights[0].length - 2));
		return (heights[x][y] + heights[x + 1][y] + heights[x][y + 1] + heights[x + 1][y + 1]) / 4;
	}

	private static int round8(double height) {
		return (int) (Math.round(height / 8.0) * 8);
	}

	/** Keeps the deck level above the one it crosses, and within the range the cache can store. */
	private static int withinLevel(int height, int lowerHeight) {
		return Math.max(Math.min(height, lowerHeight), lowerHeight - MAX_ABOVE_LEVEL);
	}

	/**
	 * @param deckTiles    the tiles that become the bridge deck
	 * @param line         the line the bridge was drawn along (tile centres); it sets the height profile
	 * @param lowerHeights corner heights of the level the bridge crosses
	 * @param upperHeights corner heights of the level the bridge is built on, the ramp slopes down to these
	 */
	public static Result shape(Set<Integer> deckTiles, List<double[]> line, int[][] lowerHeights, int[][] upperHeights,
			Params params) {
		Result result = new Result();
		if (deckTiles.isEmpty())
			return result;

		int cornersX = upperHeights.length, cornersY = upperHeights[0].length;
		Set<Integer> corners = new HashSet<>();
		for (int tile : deckTiles) {
			int x = tile >> 16, y = tile & 0xFFFF;
			for (int dx = 0; dx <= 1; dx++) {
				for (int dy = 0; dy <= 1; dy++) {
					if (x + dx < cornersX && y + dy < cornersY)
						corners.add(key(x + dx, y + dy));
				}
			}
		}

		if (line.isEmpty())
			return result;

		double startHeight, endHeight;
		if (params.deckHeight == AUTO) {
			double[] first = line.get(0), last = line.get(line.size() - 1);
			startHeight = groundAt(lowerHeights, first[0], first[1]);
			endHeight = groundAt(lowerHeights, last[0], last[1]);
		} else {
			startHeight = endHeight = params.deckHeight;
		}

		for (int corner : corners) {
			int x = corner >> 16, y = corner & 0xFFFF;
			double t = progress(line, x, y);
			double height = startHeight + (endHeight - startHeight) * t - params.arc * 4 * t * (1 - t);
			result.deck.put(corner, withinLevel(round8(height), lowerHeights[x][y]));
		}

		// the ramp: from every deck corner outwards, sloping from its height to the ground
		int reach = Math.max(params.rampWidth, 0);
		if (reach == 0)
			return result;
		Map<Integer, int[]> reached = new HashMap<>(); // corner -> {source height, distance}
		ArrayDeque<Integer> queue = new ArrayDeque<>();
		for (Map.Entry<Integer, Integer> entry : result.deck.entrySet()) {
			reached.put(entry.getKey(), new int[] { entry.getValue(), 0 });
			queue.add(entry.getKey());
		}
		while (!queue.isEmpty()) {
			int corner = queue.poll();
			int[] from = reached.get(corner);
			if (from[1] >= reach)
				continue;
			int x = corner >> 16, y = corner & 0xFFFF;
			for (int dx = -1; dx <= 1; dx++) {
				for (int dy = -1; dy <= 1; dy++) {
					int nx = x + dx, ny = y + dy;
					if (nx < 0 || ny < 0 || nx >= cornersX || ny >= cornersY || reached.containsKey(key(nx, ny)))
						continue;
					reached.put(key(nx, ny), new int[] { from[0], from[1] + 1 });
					queue.add(key(nx, ny));
				}
			}
		}
		for (Map.Entry<Integer, int[]> entry : reached.entrySet()) {
			if (entry.getValue()[1] == 0)
				continue;
			int x = entry.getKey() >> 16, y = entry.getKey() & 0xFFFF;
			double t = entry.getValue()[1] / (double) (reach + 1);
			double height = entry.getValue()[0] + (upperHeights[x][y] - entry.getValue()[0]) * t;
			result.ramp.put(entry.getKey(), withinLevel(round8(height), lowerHeights[x][y]));
		}
		return result;
	}
}
