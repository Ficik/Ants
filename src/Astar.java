import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;


public class Astar {
	
	static Ants ants;
	static Map map;
	
	private MapTile start;
	private List<MapTile> goals;
	
	private PriorityQueue<MapTile> openList = new PriorityQueue<MapTile>(80, new Comparator<MapTile>() {
		@Override
		public int compare(MapTile o1, MapTile o2) {
			return ((Integer)f(o1)).compareTo((Integer)f(o2));
		}
	});
	
	private HashSet<MapTile> closeList = new HashSet<MapTile>();
	
	
	public Astar(MapTile start, List<MapTile> goals) {
		this.start = start;
		this.goals = goals;
		openList.add(start);
		
	}
	
	/**
	 * @return next closest Maptile from goals
	 */
	public MapTile next(){
		if (goals.isEmpty())
			return null;
		
		while (!openList.isEmpty()){
			MapTile x = openList.poll();
			if (goals.contains(x)){
				goals.remove(x);
				return x;
			}
			closeList.add(x);
			ArrayList<MapTile> ns = x.getNeighbours(false);
			int cur_distance = x.getDistance(start);
			for (MapTile n : ns){
				if (closeList.contains(n) || openList.contains(n))
					continue;
				n.setDistance(start, cur_distance+1);
				openList.add(n);
			}
		}
		return null;
	}
	
	private HashMap<MapTile, Integer> fCache = new HashMap<MapTile, Integer>();
	
	
	/**
	 * Cached f_score
	 * @param pos
	 * @return
	 */
	public int f(MapTile pos){
		Integer value = fCache.get(pos);
		if (value != null) return value;
		value = g(pos)+minH(pos);
		fCache.put(pos, value);
		return value;
	}
	
	public void getG(MapTile pos, int value){
		 start.setDistance(pos, value);
	}
	
	/**
	 * Distance from start
	 * @param pos
	 * @return
	 */
	public int g(MapTile pos){
		return start.getDistance(pos);
	}
	
	/**
	 * Heuristic function (distance to goal)
	 * @param pos
	 * @return distance if known, guess of distance otherwise
	 */
	public int h(MapTile pos, MapTile goal){
		int dist = pos.getDistance(goal);
		if (dist != -1)
			return dist;
		return ants.getDistance(pos, goal);
	}
	
	public int minH(MapTile pos){
		int best = Integer.MAX_VALUE;
		for (MapTile goal : goals){
			int cur = h(pos, goal);
			if (cur < best)
				best = cur;
		}
		return best;
	}
	
	
	
	
}
