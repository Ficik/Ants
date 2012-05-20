package mybot.algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mybot.Ant;
import mybot.GameState;
import mybot.MapTile;
import mybot.algo.dcop.Diff;
import mybot.algo.dcop.LR;
import mybot.algo.dcop.Point;

public class DCOP_MST {

	public static HashMap<Ant,MapTile> positions = new HashMap<Ant, MapTile>();
	static HashMap<Ant,LR> localReduction = new HashMap<Ant, LR>();
	static HashMap<Ant,ArrayList<Ant>> neighbors = new HashMap<Ant, ArrayList<Ant>>();
	static Diff cur_diff;
	
	public static void init(){
		positions.clear();
		for (Ant ant : Ant.getAnts())
			positions.put(ant, ant.getMapTile());
		
		double ndistance = Math.sqrt(GameState.getCore().getViewRadius2())*2 + 2;
		ndistance*=ndistance;
		
		neighbors.clear();
		for (Ant ant : Ant.getAnts())
			for (Ant other : Ant.getAnts())
				if (GameState.getCore().getDistance(ant.getMapTile(), other.getMapTile()) < ndistance &&
						!ant.equals(other))
					setNeighbors(ant,other);
		
		cur_diff = new Diff(null);
	}
	
	private static void setNeighbors(Ant ant, Ant other) {
		setNeighbor(ant, other);
		setNeighbor(other, ant);
	}
	
	private static void setNeighbor(Ant ant, Ant other){
		ArrayList<Ant> list = neighbors.get(ant);
		if (list == null){
			list = new ArrayList<Ant>();
			neighbors.put(ant, list);
		}
		list.add(other);
	}

	public static void loop(){
		boolean stable = false;
		
		while (!stable){
			stable = true;
			for (Ant ant : Ant.getAnts()){
				localReduction.put(ant, bestPossibleLocalReduction(ant));
			}
			for (Ant ant : Ant.getAnts()){
				if (localReduction.get(ant).value > 0){
					LR lr = localReduction.get(ant);
					double max = 0;
					List<Ant> neighs = neighbors.get(ant);
					if (neighs != null){
						for (Ant neighbor : neighs)
							max = Math.max(localReduction.get(neighbor).value, max);
					}
					if (lr.value > max){
						cur_diff.changeValueBy(cur_diff.getPointWithinSR(positions.get(ant)), 1);
						positions.put(ant, lr.position);
						cur_diff.changeValueBy(cur_diff.getPointWithinSR(positions.get(ant)), -1);
						stable = false;
					}
				}
			}
		}
	}
	
	private static LR bestPossibleLocalReduction(Ant ant) {
		List<MapTile> possiblePositions = ant.getMapTile().getPassableNeighbours();
		possiblePositions.add(ant.getMapTile());
		Diff tempDiff = new Diff(cur_diff);
		tempDiff.changeValueBy((tempDiff.getPointWithinSR(positions.get(ant))),1);
		MapTile newPosition = selectPosition(possiblePositions, new Diff(tempDiff), ant);
		
		List<Point> curCoverage = tempDiff.getPointWithinSR(positions.get(ant));
		List<Point> newCoverage = tempDiff.getPointWithinSR(newPosition);
		double curCov=0;
		double newCov=0;
		for(Point point : curCoverage){
			if (!newCoverage.contains(point))
				newCov+=point.getValue();
		}
		for(Point point : newCoverage){
			if (!curCoverage.contains(point))
				curCov+=point.getValue();
		}
		return new LR(newPosition, curCov - newCov);
	}
	
	private static MapTile selectPosition(List<MapTile> possiblePositions, Diff func, Ant ant){
		float best = 0;
		MapTile bestTile = positions.get(ant);
		for (MapTile pos: possiblePositions) {
			float sum = 0;
			for (Point point: func.getPointWithinSR(pos))
				if (point.getValue() > 0)
					sum+=point.getValue();
			if (sum > best){
				best = sum;
				bestTile = pos;
			}
		}
		return bestTile;
	}

/*	private static MapTile selectPosition(List<MapTile> possiblePositions, Diff func, Ant ant) {
		System.err.println(possiblePositions);
		if (possiblePositions.size() == 0){
			System.err.println("NONE");
			return ant.getMapTile();
		}
		if (possiblePositions.size() == 1)
			return possiblePositions.get(0);
		HashSet<Point> target_set =new HashSet<Point>();
		double max_value = 0;
		for (Point point : func.getPointWithinArea(positions.get(ant), Math.sqrt(GameState.getCore().getViewRadius2())+1)){
			double value = point.getValue();
			if (value > 0){
				if (value > max_value){
					max_value = value;
					target_set.clear();
				}
				if (value == max_value)
					target_set.add(point);
			}
		}
		
		if (target_set.isEmpty()){
			return possiblePositions.get(0);
		}
		max_value = 0;
		List<MapTile> new_possible_positions = new ArrayList<MapTile>();
		List<Point> intersection = new ArrayList<Point>();
		for (MapTile pos : possiblePositions){
			List<Point> points = func.getPointWithinSR(pos);
			int count = points.size();
			if (count > max_value){
				max_value = count;
				new_possible_positions.clear();
				intersection = points;
			}
			if (count == max_value){
				int needed = (int)max_value;
				for(Point point : points)
					if (intersection.contains(point))
						needed--;
				if (needed == 0)
					new_possible_positions.add(pos);
				}
		}
		func.substract(intersection);
		return selectPosition(new_possible_positions, func, ant);
	}*/
	
}
