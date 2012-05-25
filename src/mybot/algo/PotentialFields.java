package mybot.algo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import core.Ilk;
import mybot.GameState;
import mybot.Map;
import mybot.MapTile;

public class PotentialFields {

	public static PotentialFields instance = new PotentialFields();
	
	private static final float LEVEL_1 = 1f / 4;
	private static final float LEVEL_2 = 1f / 16;

	public static void generatePotentialFromTile(int row, int col) {
		GameState.getMap().getTile(row, col).setPotential(0);
		level1(row, col);
		level2(row, col);
	}

	/*
	 * x x0x x
	 */
	private static void level1(int row, int col) {
		decreasePotential(row + 1, col, LEVEL_1);
		decreasePotential(row - 1, col, LEVEL_1);
		decreasePotential(row, col + 1, LEVEL_1);
		decreasePotential(row, col - 1, LEVEL_1);
	}

	/*
	 * x x1x x101x x1x x
	 */
	private static void level2(int row, int col) {
		decreasePotential(row + 1, col + 1, 2 * LEVEL_2);
		decreasePotential(row - 1, col + 1, 2 * LEVEL_2);
		decreasePotential(row + 1, col - 1, 2 * LEVEL_2);
		decreasePotential(row - 1, col - 1, 2 * LEVEL_2);

		decreasePotential(row + 2, col, LEVEL_2);
		decreasePotential(row - 2, col, LEVEL_2);
		decreasePotential(row, col + 2, LEVEL_2);
		decreasePotential(row, col - 2, LEVEL_2);

	}

	private static void decreasePotential(int row, int col, float amount) {
		GameState.getMap().getTile(row, col).decreasePotential(amount);
	}

	
	public void spreadOut(List<MapTile> possibleMoves){
		ValueComparator comparator = new ValueComparator();
		for (MapTile position : possibleMoves){
			int count = 0;
			float value = 0;
			for (MapTile point : getAreaWithinSR(position))
				if (point.getAnt() != null){
					value+=Map.getManhattanDistance(point, position);
					count++;
				}
			if (count==0)
				value = Float.POSITIVE_INFINITY;
			else
				value /= count;
			comparator.setValue(position, value);
		}
		Collections.sort(possibleMoves, comparator);
	}
	
	public void reorderMoves(List<MapTile> possibleMoves){
		ValueComparator comparator = new ValueComparator();
		for(MapTile move : possibleMoves)
			comparator.setValue(move, getValue(move)*move.getPotential());
		Collections.sort(possibleMoves, comparator);
		if (getValue(possibleMoves.get(0)) == 0)
			spreadOut(possibleMoves);
	}
	
	
	Queue<MapTile> openList = new LinkedList<MapTile>();
	Set<MapTile> closedSet = new HashSet<MapTile>(300);
	public float getValue(MapTile tile){
		float sum = 0;
		for (MapTile t : getAreaWithinSR(tile))
			sum+=t.getEnvironmentalRequirement();
		return sum;
	}
	
	public List<MapTile> getAreaWithinSR(MapTile tile){
		if (tile.SR != null)
			return tile.SR;
		return generateAreaWithinSR(tile);
	}
	
	private List<MapTile> generateAreaWithinSR(MapTile tile){
		List<MapTile> area = new ArrayList<MapTile>();
		boolean clear = true;
		openList.clear();
		closedSet.clear();
		openList.add(tile);
		closedSet.add(tile);
		area.add(tile);
		while (!openList.isEmpty()){
			MapTile x = openList.poll();
			for (MapTile n : x.getPassableAndUnknownNeighbours()){
				if (GameState.getCore().getDistance(tile, x) <= GameState.getCore().getViewRadius2()){
					if (!closedSet.contains(n)){
						openList.add(n);
						closedSet.add(n);
						area.add(n);
						if (n.getValue() == Ilk.UNKNOWN)
							clear = false;
					}
				}
			}
		}
		if(clear)
			tile.SR = area;
		return area;
	}
	
	
	public float getRadius2(){
		return GameState.getCore().getViewRadius2();
	}
	
	private float r = 0;
	public float getRadius(){
		if (r == 0)
			r = (float)Math.sqrt(getRadius2());
		return r;
	}
	
	class ValueComparator implements Comparator<MapTile> {
		
		HashMap<MapTile, Float> values = new HashMap<MapTile, Float>();
		
		public void setValue(MapTile tile, float value){
			values.put(tile, value);
		}
		
		@Override
		public int compare(MapTile o1, MapTile o2) {
			return -values.get(o1).compareTo(values.get(o2));
		}
	}
	
}
