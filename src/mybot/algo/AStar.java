package mybot.algo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import mybot.Ant;
import mybot.GameState;
import mybot.MapTile;

public class AStar {

	
	private static final int DISTANCE_CUTOFF = 15;
	private Set<MapTile> closedSet = new HashSet<MapTile>();
	private MapTile start;
	private List<? extends Goal> goals;

	private PriorityQueue<MapTile> openSet = new PriorityQueue<MapTile>(80,
			new Comparator<MapTile>() {
				@Override
				public int compare(MapTile o1, MapTile o2) {
					return ((Integer) f(o1)).compareTo((Integer) f(o2));
				}
			});

	
	public static MapTile antSearch(MapTile start){
		return (new AStar(start, Ant.getAnts())).search();
	}
	
	
	private AStar(MapTile start, List<? extends Goal> goals) {
		this.start = start;
		this.goals = goals;
		openSet.addAll(start.getPassableNeighbours());
	}
	
	
	

	private boolean checkIfGoal(MapTile tile) {
		for (Goal goal : goals) {
			if (goal.getMapTile().equals(tile))
				return true;
		}
		return false;
	}

	public MapTile search() {
		if (!goals.isEmpty())
			while (!openSet.isEmpty()) {
				MapTile processed = openSet.poll();
				if (checkIfGoal(processed))
					return processed;
				closedSet.add(processed);
				int curDistance = processed.getRealDistance(start);
				if (curDistance > DISTANCE_CUTOFF)
					break;
				for (MapTile tile : processed.getPassableNeighbours())
					tryAddToOpenList(tile, curDistance + 1);
			}
		return null;
	}

	private void tryAddToOpenList(MapTile tile, int distance) {
		if (closedSet.contains(tile) || openSet.contains(tile))
			return;
		tile.setRealDistance(start, distance);
		openSet.add(tile);
	}

	private HashMap<MapTile, Integer> fCache = new HashMap<MapTile, Integer>();
	
	private int f(MapTile maptile) {
		Integer value = fCache.get(maptile);
		if (value != null) {
			value = g(maptile) + minH(maptile);
			fCache.put(maptile, value);
		}
		return value;
	}
	
	private int g(MapTile maptile){
		return start.getRealDistance(maptile);
	}
	
	
	private int h(MapTile pos, MapTile goal){
		int dist = pos.getRealDistance(goal);
		if (dist != MapTile.UNSET)
			return dist;
		return GameState.getCore().getDistance(pos, goal);
	}
	
	private int minH(MapTile pos){
		int best = Integer.MAX_VALUE;
		Iterator<? extends Goal> iterator = goals.iterator();
		while(iterator.hasNext())
			best = Math.min(h(pos, iterator.next().getMapTile()), best);
		return best;
	}

}
