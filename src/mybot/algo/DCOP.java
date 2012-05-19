package mybot.algo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import core.Aim;
import core.Tile;
import mybot.Ant;
import mybot.GameState;
import mybot.MapTile;

public class DCOP {

	private static DCOP instance;

	private static int viewRadius, viewRadiusSquared;
	private static HashMap<Aim, List<int[]>> viewDiff;


	public static DCOP getInstance() {
		if (instance == null)
			instance = new DCOP();
		return instance;
	}

	/**
	 * MGM MST algorithm
	 */
	public static void MGM_MST(Ant ant) {
		/*
		 * value SelectedValue() while (true) DCOP.getNeighboringAnts(ant) 3. send
		 * cur pos to each Ai 2 cur neiself 4. collect positions of each Ai 2 cur
		 * neiself 5. LR BestPossibleLocalReduction() 6. Send LR to each Ai 2 cur
		 * neiself 7. Collect LRs from each Ai 2 cur neiself 8. if (LR > 0) 9. if
		 * (LR > LRs of each Ai 2 cur neiself (ties broken using indexes)) 10. cur
		 * pos the position that gives LR
		 */
	}

	public static LR BestPossibleLocalReduction(Ant ant) {
		List<MapTile> possiblePositions = ant.getMapTile().getPassableNeighbours();
		// possible_pos <- positions within MRself from cur_pos

		// Temp_Diff <- Cur_Diff \ self_coverage
		ant.changeCredibility(-1);
		MapTile newPosition = selectPosition(possiblePositions);
		// new_pos <- select_pos(possible_pos , Temp_Diff)
		ant.changeCredibility(1);

		Aim aim = ant.getMapTile().getAimTo(newPosition);

		float currentCoverage = 0;
		for (MapTile tile : getCurrentlyVisibleMapTilesByAntAtMapTile(ant.getMapTile()))
			currentCoverage = Math.max(currentCoverage, tile.getCurrentDifference());
		// 14. cur cov highest Temp Diff among points
		// within SRself from cur pos and not within SRself from new pos

		float newCoverage = 0;
		for (MapTile tile : populateViewDiffFromTileTo(ant.getMapTile(), aim))
			newCoverage = Math.max(newCoverage, tile.getCurrentDifference());
		// 15. new cov highest Temp Diff among points not within
		// SRself from cur pos and within SRself from new pos

		return new LR(newPosition, Math.min(currentCoverage - newCoverage, 1f)); // return
																																							// min(cur
																																							// cov
																																							// -
		// new cov;Credself )
	}

	public static MapTile selectPosition(List<MapTile> possiblePositons) {
		if (possiblePositons.size() == 1)
			return possiblePositons.get(0);

		MapTile selectedPosition = null;
		float best = -9999;

		for (MapTile possiblePosition : possiblePositons) {
			List<MapTile> visibleTiles = getCurrentlyVisibleMapTilesByAntAtMapTile(possiblePosition);
			float value = (1-0.25f*possiblePosition.getPotential())*sumList(applyCurrentDifferenceToMaptiles(visibleTiles))/visibleTiles.size();
			//float value = applyCurrentDifferenceToMaptiles(visibleTiles);
			if (value >= best) {
				best = value;
				selectedPosition = possiblePosition;
			}
		}
		if (selectedPosition == null)
			selectedPosition = possiblePositons.get(new Random().nextInt(possiblePositons.size()));

		// target set <- points within SRself from some pos in pos set with largest
		// func value (must be larger than zero)
		// if (target set is empty)
		// return some pos 2 pos set
		// if (no pos 2 pos set is within SRself from all the points in target set)
		// target set largest subset of target set within SRself from some pos 2 pos
		// set
		// possible pos all positions in pos set which are within SRself from all
		// points in target set
		// intersect area area within SRself from all pos 2 possible pos
		// new func func n func:intersect area
		// return select pos(possible pos , new func)
		return selectedPosition;
	}

	public static List<MapTile> getCurrentlyVisibleMapTilesByAntAtMapTile(MapTile maptile) {
		ArrayList<MapTile> tiles = new ArrayList<MapTile>();
		Queue<MapTile> openList = new LinkedList<MapTile>();
		openList.add(maptile);
		while (!openList.isEmpty()) {
			MapTile tile = openList.poll();
			if (sizeOfVectorSquared(maptile.getRow() - tile.getRow(), maptile.getCol() - tile.getCol()) <= GameState.getCore()
					.getViewRadius2() && !tiles.contains(tile)){
				tiles.add(tile);
				openList.addAll(tile.getPassableAndUnknownNeighbours());
			}
		}
		return tiles;
		/*
		 * ArrayList<MapTile> tiles = new ArrayList<MapTile>(); int radius = (int)
		 * Math.ceil(Math.sqrt(GameState.getCore().getViewRadius2())); for (int col
		 * = -radius; col <= radius; col++) for (int row = -radius; row <= radius;
		 * row++) if (col * col + row * row <= GameState.getCore().getViewRadius2())
		 * tiles.add(GameState.getMap().getTile(maptile.getRow() + row,
		 * maptile.getCol() + col)); return tiles;
		 */
	}

	public static List<Float> applyCurrentDifferenceToMaptiles(List<MapTile> tiles) {
		List<Float> credibility = new ArrayList<Float>();
		for (MapTile tile : tiles)
			//if (tile.getCurrentDifference() > 0)
			credibility.add(tile.getCurrentDifference());
		return credibility;
	}

	public static float sumList(List<Float> list) {
		float sum = 0;
		for (Float value : list)
			sum += value.floatValue();
		return sum;
	}

	/**
	 * Finds ants that are within MR_i + MR_j + SR_i + SR_j distance
	 * 
	 * @param ant
	 *          whose neighbors we're looking for
	 * @return list of neighbors
	 */
	public static List<Ant> getNeighboringAnts(Ant ant) {
		List<Ant> neighbours = new ArrayList<Ant>();
		ant.getMapTile();
		for (Ant other : Ant.getAnts())
			if (GameState.getCore().getDistance(ant.getMapTile(), other.getMapTile()) < 2 * viewRadiusSquared + 2
					&& !other.equals(ant))
				neighbours.add(other);
		return neighbours;
	}

	public static List<MapTile> getNeighbourMapTilesOrderedByExploringValue(MapTile tile) {
		List<MapTile> results = tile.getPassableNeighbours();
		Collections.sort(results, getInstance().new ExploringValueComparator(tile));
		return results;
	}

	public static int getMoveExploringValue(Tile tile, Aim aim) {
		int value = 0;
		for (MapTile diffTile : populateViewDiffFromTileTo(tile, aim))
			value += diffTile.getUnseenDuration();
		return value;
	}

	public static List<MapTile> populateViewDiffFromTileTo(Tile tile, Aim aim) {
		List<MapTile> diffs = new ArrayList<MapTile>();
		for (int[] delta : viewDiff.get(aim))
			diffs.add(GameState.getMap().getTile(tile.getRow() + delta[0], tile.getCol() + delta[1]));
		return diffs;
	}

	public static void initGameSpecificVariables() {
		viewRadiusSquared = GameState.getCore().getViewRadius2();
		viewRadius = (int) Math.ceil(Math.sqrt(viewRadiusSquared));
		getInstance().generateViewDifferenceMap();
	}

	private void initialsViewDifferenceMap() {
		viewDiff = new HashMap<Aim, List<int[]>>();
		for (Aim aim : Aim.values())
			viewDiff.put(aim, new ArrayList<int[]>());
	}

	private void generateViewDifferenceMap() {
		initialsViewDifferenceMap();
		for (int col = -viewRadius - 1; col <= viewRadius + 1; col++)
			for (int row = -viewRadius - 1; row <= viewRadius + 1; row++)
				if (!isVectorInViewRadius(row, col))
					for (Aim aim : Aim.values())
						addPointToViewDifferenceMapIfInViewRadius(aim, row + aim.getRowDelta(), col + aim.getColDelta());
	}

	private void addPointToViewDifferenceMapIfInViewRadius(Aim aim, int row, int col) {
		if (isVectorInViewRadius(row, col))
			viewDiff.get(aim).add(new int[] { row, col });
	}

	private boolean isVectorInViewRadius(int row, int col) {
		return sizeOfVectorSquared(row, col) <= viewRadiusSquared;
	}

	public static int sizeOfVectorSquared(int row, int col) {
		return row * row + col * col;
	}

	public class ExploringValueComparator implements Comparator<MapTile> {

		MapTile origin;
		Integer values[] = new Integer[4];

		public ExploringValueComparator(MapTile origin) {
			this.origin = origin;
		}

		@Override
		public int compare(MapTile o1, MapTile o2) {
			return -getValue(o1).compareTo(getValue(o2));
		}

		private Integer getValue(MapTile tile) {
			Aim aim = origin.getAimTo(tile);
			if (values[aim.ordinal()] == null)
				values[aim.ordinal()] = (Integer) (int) getMoveExploringValue(origin, aim);
			return values[aim.ordinal()];
		}

	}

	public static class LR {
		public MapTile tile;
		public float value;

		public LR(MapTile tile, float value) {
			this.tile = tile;
			this.value = value;
		}

	}

	public class DCOPEvent {

	}
}
