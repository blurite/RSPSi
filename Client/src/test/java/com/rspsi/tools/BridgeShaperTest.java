package com.rspsi.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class BridgeShaperTest {

	/** Corner heights, like the map: one more than the number of tiles. */
	private static int[][] flat(int height) {
		int[][] h = new int[21][21];
		for (int[] row : h)
			Arrays.fill(row, height);
		return h;
	}

	/** A bridge along y = 10.5 from the tile at x = 5 to the tile at x = 14. */
	private static List<double[]> line() {
		return Arrays.asList(new double[] { 5.5, 10.5 }, new double[] { 14.5, 10.5 });
	}

	private static Set<Integer> deck(List<double[]> line, double width) {
		return BridgeShaper.deckTiles(line, width, 20, 20);
	}

	private static int deckAt(BridgeShaper.Result r, int x, int y) {
		return r.deck.get(BridgeShaper.key(x, y));
	}

	// ---- deck tiles ----

	@Test
	void deckTilesFollowTheLineWithTheGivenWidth() {
		List<double[]> line = Arrays.asList(new double[] { 2.5, 10.5 }, new double[] { 12.5, 10.5 });
		Set<Integer> three = BridgeShaper.deckTiles(line, 3, 20, 20);
		assertTrue(three.contains(BridgeShaper.key(7, 9)) && three.contains(BridgeShaper.key(7, 10))
				&& three.contains(BridgeShaper.key(7, 11)));
		assertFalse(three.contains(BridgeShaper.key(7, 12)));
		assertFalse(three.contains(BridgeShaper.key(14, 10)), "stops at the end of the line");
		assertFalse(three.contains(BridgeShaper.key(1, 10)), "no cap before the first point");
		assertEquals(33, three.size());
		assertEquals(11, BridgeShaper.deckTiles(line, 1, 20, 20).size());
	}

	@Test
	void deckTilesStayInsideTheMap() {
		List<double[]> line = Arrays.asList(new double[] { 0.5, 0.5 }, new double[] { 19.5, 0.5 });
		for (int key : BridgeShaper.deckTiles(line, 5, 20, 20))
			assertTrue((key >> 16) >= 0 && (key >> 16) < 20 && (key & 0xFFFF) >= 0 && (key & 0xFFFF) < 20);
	}

	// ---- heights ----

	@Test
	void everyCornerOfADeckTileGetsADeckHeight() {
		BridgeShaper.Result r = BridgeShaper.shape(deck(line(), 3), line(), flat(-100), flat(-100), new BridgeShaper.Params());
		for (int tile : deck(line(), 3)) {
			int x = tile >> 16, y = tile & 0xFFFF;
			for (int dx = 0; dx <= 1; dx++)
				for (int dy = 0; dy <= 1; dy++)
					assertTrue(r.deck.containsKey(BridgeShaper.key(x + dx, y + dy)), "corner " + (x + dx) + "," + (y + dy));
		}
	}

	@Test
	void deckStartsAndEndsOnTheGroundOfTheBanks() {
		int[][] lower = flat(-100);
		for (int y = 0; y <= 20; y++)
			for (int x = 8; x <= 12; x++)
				lower[x][y] = -400; // a hill in the middle must not lift the whole deck
		BridgeShaper.Result r = BridgeShaper.shape(deck(line(), 3), line(), lower, flat(-340), new BridgeShaper.Params());
		assertEquals(-100, deckAt(r, 5, 10), 8, "start on the bank");
		assertEquals(-100, deckAt(r, 15, 10), 8, "end on the bank");
	}

	@Test
	void deckSlopesBetweenBanksOfDifferentHeight() {
		int[][] lower = flat(0);
		for (int y = 0; y <= 20; y++)
			for (int x = 10; x <= 20; x++)
				lower[x][y] = -200;
		BridgeShaper.Result r = BridgeShaper.shape(deck(line(), 1), line(), lower, flat(-400), new BridgeShaper.Params());
		assertTrue(deckAt(r, 5, 10) > deckAt(r, 10, 10) && deckAt(r, 10, 10) >= deckAt(r, 15, 10), "rises towards the high bank");
	}

	@Test
	void arcLiftsTheMiddleAndKeepsTheEnds() {
		BridgeShaper.Params p = new BridgeShaper.Params();
		p.arc = 200;
		BridgeShaper.Result r = BridgeShaper.shape(deck(line(), 3), line(), flat(0), flat(-240), p);
		assertEquals(0, deckAt(r, 5, 10), 8);
		assertEquals(0, deckAt(r, 15, 10), 8);
		assertEquals(-200, deckAt(r, 10, 10), 24, "the middle is the full arc higher");
		assertTrue(deckAt(r, 7, 10) > deckAt(r, 10, 10), "rises towards the middle");
		assertEquals(deckAt(r, 7, 10), deckAt(r, 13, 10), 8, "symmetric");
	}

	@Test
	void deckWithoutArcIsFlatBetweenEqualBanks() {
		BridgeShaper.Result r = BridgeShaper.shape(deck(line(), 3), line(), flat(-100), flat(-340), new BridgeShaper.Params());
		for (int h : r.deck.values())
			assertEquals(-100, h, 8);
	}

	@Test
	void fixedDeckHeightIsFlatAndOverridesTheBanks() {
		BridgeShaper.Params p = new BridgeShaper.Params();
		p.deckHeight = -200;
		BridgeShaper.Result r = BridgeShaper.shape(deck(line(), 3), line(), flat(-40), flat(-280), p);
		for (int h : r.deck.values())
			assertEquals(-200, h);
	}

	@Test
	void deckNeverLiesBelowTheLevelItCrossesOrTooFarAbove() {
		BridgeShaper.Params p = new BridgeShaper.Params();
		p.deckHeight = -5000;
		BridgeShaper.Result high = BridgeShaper.shape(deck(line(), 1), line(), flat(-100), flat(-340), p);
		for (int h : high.deck.values())
			assertEquals(-100 - 2040, h, "limited to what the cache can store");

		p.deckHeight = 300;
		BridgeShaper.Result low = BridgeShaper.shape(deck(line(), 1), line(), flat(-100), flat(-340), p);
		for (int h : low.deck.values())
			assertEquals(-100, h, "not below the ground");
	}

	// ---- ramp ----

	@Test
	void rampSlopesDownGraduallyFromTheDeck() {
		BridgeShaper.Params p = new BridgeShaper.Params();
		p.rampWidth = 3;
		p.deckHeight = -240;
		BridgeShaper.Result r = BridgeShaper.shape(deck(line(), 3), line(), flat(0), flat(0), p);
		int edge = r.deck.get(BridgeShaper.key(10, 12));
		int one = r.ramp.get(BridgeShaper.key(10, 13));
		int two = r.ramp.get(BridgeShaper.key(10, 14));
		int three = r.ramp.get(BridgeShaper.key(10, 15));
		assertTrue(edge < one && one < two && two < three && three < 0, edge + " " + one + " " + two + " " + three);
		assertFalse(r.ramp.containsKey(BridgeShaper.key(10, 16)), "nothing beyond the ramp width");
	}

	@Test
	void rampNeverIncludesDeckCorners() {
		BridgeShaper.Params p = new BridgeShaper.Params();
		p.rampWidth = 2;
		BridgeShaper.Result r = BridgeShaper.shape(deck(line(), 3), line(), flat(-100), flat(0), p);
		for (int key : r.deck.keySet())
			assertFalse(r.ramp.containsKey(key));
	}

	@Test
	void zeroRampGivesNoRamp() {
		BridgeShaper.Params p = new BridgeShaper.Params();
		p.rampWidth = 0;
		assertTrue(BridgeShaper.shape(deck(line(), 3), line(), flat(-100), flat(0), p).ramp.isEmpty());
	}

	@Test
	void rampAtTheEndsDoesNotRiseIntoAWall() {
		BridgeShaper.Params p = new BridgeShaper.Params();
		p.rampWidth = 2;
		BridgeShaper.Result r = BridgeShaper.shape(deck(line(), 3), line(), flat(-100), flat(-340), p);
		int end = deckAt(r, 5, 10);
		int beyond = r.ramp.get(BridgeShaper.key(4, 10));
		assertTrue(Math.abs(end - beyond) <= 240, "a gentle step, not a wall: " + end + " -> " + beyond);
	}

	// ---- edges ----

	@Test
	void mapEdgeDoesNotThrowAndStaysInsideTheCorners() {
		BridgeShaper.Params p = new BridgeShaper.Params();
		p.rampWidth = 3;
		List<double[]> left = Arrays.asList(new double[] { 0.5, 0.5 }, new double[] { 5.5, 0.5 });
		List<double[]> right = Arrays.asList(new double[] { 14.5, 19.5 }, new double[] { 19.5, 19.5 });
		for (List<double[]> l : Arrays.asList(left, right)) {
			BridgeShaper.Result r = BridgeShaper.shape(deck(l, 3), l, flat(-100), flat(0), p);
			assertFalse(r.ramp.isEmpty());
			for (int key : r.ramp.keySet())
				assertTrue((key >> 16) >= 0 && (key >> 16) <= 20 && (key & 0xFFFF) >= 0 && (key & 0xFFFF) <= 20);
		}
	}

	@Test
	void emptyDeckGivesEmptyResult() {
		BridgeShaper.Result r = BridgeShaper.shape(new HashSet<>(), line(), flat(0), flat(0), new BridgeShaper.Params());
		assertTrue(r.deck.isEmpty() && r.ramp.isEmpty());
	}
}
