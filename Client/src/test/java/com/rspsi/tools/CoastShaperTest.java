package com.rspsi.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CoastShaperTest {

	/** A line along x = 20 going north (+y); with seaLeft the sea is at smaller x. */
	private static List<double[]> line() {
		List<double[]> l = new ArrayList<>();
		l.add(new double[] { 20, 0 });
		l.add(new double[] { 20, 60 });
		return l;
	}

	private static int[][] flat(int height) {
		int[][] h = new int[60][60];
		for (int[] row : h)
			java.util.Arrays.fill(row, height);
		return h;
	}

	private static int after(int[][] heights, Map<Integer, Integer> changes, int x, int y) {
		return heights[x][y] + changes.getOrDefault(x << 16 | y, 0);
	}

	@Test
	void seaSideIsFlattenedToZero() {
		int[][] heights = flat(-100);
		CoastShaper.Params p = new CoastShaper.Params();
		p.beachWidth = 8;
		Map<Integer, Integer> c = CoastShaper.shape(line(), p, heights);
		assertEquals(0, after(heights, c, 15, 30));
		assertEquals(-100, after(heights, c, 40, 30), "far land is untouched");
		assertEquals(-100, after(heights, c, 5, 30), "far sea is untouched");
	}

	@Test
	void seaSideFlipsWithTheToggle() {
		int[][] heights = flat(-100);
		CoastShaper.Params p = new CoastShaper.Params();
		p.beachWidth = 8;
		p.seaLeft = false;
		Map<Integer, Integer> c = CoastShaper.shape(line(), p, heights);
		assertEquals(0, after(heights, c, 25, 30));
		assertEquals(-100, after(heights, c, 5, 30));
	}

	@Test
	void beachSlopesDownToZeroAtTheWaterline() {
		int[][] heights = flat(-100);
		CoastShaper.Params p = new CoastShaper.Params();
		Map<Integer, Integer> c = CoastShaper.shape(line(), p, heights);
		int previous = 0;
		for (int x = 20; x < 20 + (int) p.beachWidth; x++) {
			int h = after(heights, c, x, 30);
			assertTrue(h <= previous, "land only rises further from the sea: x=" + x);
			previous = h;
		}
		assertTrue(after(heights, c, 20, 30) > -5);
		assertEquals(-100, after(heights, c, 20 + (int) p.beachWidth + 1, 30));
	}

	@Test
	void cliffRisesSteeplyToThePlateau() {
		int[][] heights = flat(-100);
		CoastShaper.Params p = new CoastShaper.Params();
		p.cliff = true;
		Map<Integer, Integer> c = CoastShaper.shape(line(), p, heights);
		assertEquals(0, after(heights, c, 15, 30));
		assertEquals(-100 - (int) p.cliffHeight, after(heights, c, 20 + (int) p.cliffWidth + 2, 30));
		assertTrue(after(heights, c, 40, 30) == -100, "terrain far inland is back to the original");
	}

	@Test
	void meanderIsDeterministicKeepsEndsAndStaysWithinAmplitude() {
		List<double[]> a = CoastShaper.meander(line(), 3, 7);
		List<double[]> b = CoastShaper.meander(line(), 3, 7);
		assertEquals(a.size(), b.size());
		double maxOffset = 0;
		for (int i = 0; i < a.size(); i++) {
			assertArrayEquals(a.get(i), b.get(i), 1e-9);
			maxOffset = Math.max(maxOffset, Math.abs(a.get(i)[0] - 20));
		}
		assertEquals(20, a.get(0)[0], 1e-9);
		assertEquals(20, a.get(a.size() - 1)[0], 1e-9);
		assertTrue(maxOffset > 0.3 && maxOffset <= 3.0, "meanders, but within the amplitude: " + maxOffset);
		assertEquals(20, CoastShaper.meander(line(), 0, 7).get(10)[0], 1e-9);
	}
}
