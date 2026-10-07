package com.rspsi.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class MountainGeneratorTest {

	private static int x(int key) {
		return key >>> 16;
	}

	private static int y(int key) {
		return key & 0xFFFF;
	}

	@Test
	void sameSeedGivesSameResultAndOtherSeedDiffers() {
		MountainGenerator.Params p = new MountainGenerator.Params();
		Map<Integer, Integer> first = MountainGenerator.hill(50, 50, p, 100, 100);
		assertEquals(first, MountainGenerator.hill(50, 50, p, 100, 100));
		p.seed = 2;
		assertNotEquals(first, MountainGenerator.hill(50, 50, p, 100, 100));
	}

	@Test
	void hillPeaksInTheMiddleAndStaysWithinLimits() {
		MountainGenerator.Params p = new MountainGenerator.Params();
		Map<Integer, Integer> hill = MountainGenerator.hill(50, 50, p, 100, 100);
		int peak = 0, peakKey = 0;
		double farthest = 0;
		for (Map.Entry<Integer, Integer> e : hill.entrySet()) {
			assertTrue(e.getValue() <= 0, "heights only go up (negative)");
			assertTrue(-e.getValue() <= p.height * 1.5, "no value far above the top height");
			if (e.getValue() < peak) {
				peak = e.getValue();
				peakKey = e.getKey();
			}
			farthest = Math.max(farthest, Math.hypot(x(e.getKey()) - 50, y(e.getKey()) - 50));
		}
		assertTrue(Math.abs(x(peakKey) - 50) <= 4 && Math.abs(y(peakKey) - 50) <= 4);
		assertTrue(farthest <= (p.size + p.blend) * 1.6 + 1);
	}

	@Test
	void hillFadesOutGradually() {
		MountainGenerator.Params p = new MountainGenerator.Params();
		Map<Integer, Integer> hill = MountainGenerator.hill(50, 50, p, 100, 100);
		for (Map.Entry<Integer, Integer> e : hill.entrySet()) {
			Integer next = hill.get(e.getKey() + (1 << 16));
			if (next != null)
				assertTrue(Math.abs(next - e.getValue()) < p.height * 0.35, "no steep step between neighbours");
		}
	}

	@Test
	void resultIsClippedToTheMap() {
		MountainGenerator.Params p = new MountainGenerator.Params();
		for (int key : MountainGenerator.hill(2, 2, p, 100, 100).keySet()) {
			assertTrue(x(key) >= 0 && y(key) >= 0);
		}
		for (int key : MountainGenerator.hill(98, 98, p, 100, 100).keySet()) {
			assertTrue(x(key) <= 100 && y(key) <= 100);
		}
	}

	@Test
	void rangeFollowsTheLine() {
		MountainGenerator.Params p = new MountainGenerator.Params();
		List<double[]> line = new ArrayList<>();
		for (int i = 0; i <= 40; i++)
			line.add(new double[] { 20 + i, 30 + 0.3 * i });
		Map<Integer, Integer> range = MountainGenerator.ridge(line, p, 100, 100);
		assertFalse(range.isEmpty());
		assertNotNull(range.get(40 << 16 | 36));
		assertNull(range.get(40 << 16 | 80));
	}

	@Test
	void areaHasATopInTheMiddleAndLowEdges() {
		MountainGenerator.Params p = new MountainGenerator.Params();
		Set<Integer> selection = new HashSet<>();
		for (int tx = 30; tx < 50; tx++)
			for (int ty = 30; ty < 45; ty++)
				selection.add(tx << 16 | ty);
		Map<Integer, Integer> area = MountainGenerator.area(selection, p, 100, 100);
		int peak = 0;
		for (int value : area.values())
			peak = Math.min(peak, value);
		assertTrue(peak < -p.height * 0.8);
		Integer corner = area.get(30 << 16 | 30);
		assertTrue(corner == null || corner > -p.height * 0.2);
	}

	@Test
	void emptyInputGivesNothing() {
		MountainGenerator.Params p = new MountainGenerator.Params();
		assertTrue(MountainGenerator.ridge(List.of(new double[] { 1, 1 }), p, 100, 100).isEmpty());
		assertTrue(MountainGenerator.area(new HashSet<>(), p, 100, 100).isEmpty());
	}

	@Test
	void suggestedShapeIsDeterministicAndNotACircle() {
		boolean[][] shape = MountainGenerator.suggestShape(5, 0.6);
		assertArrayEquals(shape, MountainGenerator.suggestShape(5, 0.6));
		assertTrue(shape[MountainGenerator.SHAPE_GRID / 2][MountainGenerator.SHAPE_GRID / 2]);
		int grid = MountainGenerator.SHAPE_GRID;
		// a circle is symmetric left/right and top/bottom; the suggestion must not be
		boolean symmetric = true;
		for (int cx = 0; cx < grid; cx++)
			for (int cy = 0; cy < grid; cy++)
				symmetric &= shape[cx][cy] == shape[grid - 1 - cx][cy] && shape[cx][cy] == shape[cx][grid - 1 - cy];
		assertFalse(symmetric);
	}

	@Test
	void drawnShapeDecidesTheTiles() {
		int grid = MountainGenerator.SHAPE_GRID;
		boolean[][] shape = new boolean[grid][grid];
		// only the top (north) half of the drawing is coloured
		for (int cx = 0; cx < grid; cx++)
			for (int cy = 0; cy < grid / 2; cy++)
				shape[cx][cy] = true;
		Set<Integer> tiles = MountainGenerator.shapeTiles(shape, 50, 50, 10, 100, 100);
		assertTrue(tiles.contains(50 << 16 | 58), "north of the centre is covered");
		assertFalse(tiles.contains(50 << 16 | 42), "south of the centre is not");
		assertFalse(tiles.contains(50 << 16 | 70), "nothing outside the radius");
		Map<Integer, Integer> hill = MountainGenerator.area(tiles, new MountainGenerator.Params(), 100, 100);
		assertFalse(hill.isEmpty());
	}

	@Test
	void cliffEdgeIsSteeperAtTheEdgeThanASmoothMountain() {
		MountainGenerator.Params smooth = new MountainGenerator.Params();
		MountainGenerator.Params cliff = new MountainGenerator.Params();
		cliff.cliff = true;
		cliff.cliffLevel = 0.6;
		cliff.cliffWidth = 2;
		assertTrue(maxStep(MountainGenerator.hill(50, 50, cliff, 100, 100))
				> maxStep(MountainGenerator.hill(50, 50, smooth, 100, 100)));
		for (int value : MountainGenerator.hill(50, 50, cliff, 100, 100).values())
			assertTrue(-value <= cliff.height * 1.5);
	}

	private static int maxStep(Map<Integer, Integer> heights) {
		int step = 0;
		for (Map.Entry<Integer, Integer> e : heights.entrySet()) {
			Integer next = heights.get(e.getKey() + (1 << 16));
			if (next != null)
				step = Math.max(step, Math.abs(next - e.getValue()));
		}
		return step;
	}
}
