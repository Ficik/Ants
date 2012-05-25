package mybot.algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		List<Ant> changes = new ArrayList<Ant>();
		changes.addAll(Ant.getAnts());
		System.err.println("DCOP START");
		Diff.counter=0;
		while (!stable){
			stable = true;
			for (Ant ant : Ant.getAnts()){
				localReduction.put(ant, bestPossibleLocalReduction(ant));
			}
			changes.clear();
			for (Ant ant : Ant.getAnts()){
				System.err.println("REDUCTION "+localReduction.get(ant).value);
				System.err.println("BY "+positions.get(ant)+" --> "+localReduction.get(ant).position);
				
				if (localReduction.get(ant).value > 0){
					if(localReduction.get(ant).position.equals(positions.get(ant))){
						System.err.println("WTF?");
						continue;
					}
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
						//changes.addAll(neighbors.get(ant));
						stable = false;
					}
				}
				
			}
		}
		System.err.println("NUMBER of diff ops: "+Diff.counter);
	}
	
	private static LR bestPossibleLocalReduction(Ant ant) {
		List<MapTile> possiblePositions = ant.getMapTile().getPassableNeighbours();
		possiblePositions.add(ant.getMapTile());
		Diff tempDiff = new Diff(cur_diff);
		tempDiff.changeValueBy((tempDiff.getPointWithinSR(positions.get(ant))),1); // zrusim vlastni pokryti
		MapTile newPosition = selectPositionFast(possiblePositions, new Diff(tempDiff), ant);
		
		System.err.println("Moving"+positions.get(ant)+" -> "+newPosition);
		
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
		//System.err.println(curCov - newCov);
		return new LR(newPosition, + curCov - newCov);
	}
	
	
	private static MapTile selectPositionFast(List<MapTile> possiblePositions, Diff func, Ant ant){
		float best = -9999f;
		MapTile bestTile = positions.get(ant);
		for (MapTile pos: possiblePositions) {
			float sum = 0;
			for (Point point: func.getPointWithinSR(pos))
				sum+=point.getValue();
			if (sum > best){
				best = sum;
				bestTile = pos;
			}
		}
		return bestTile;
	}
	/*private static MapTile selectPosition(List<MapTile> possiblePositions, Diff func, Ant ant){
		
		for (MapTile pos: possiblePositions) {
			float sum = 0;
			for (Point point: func.getPointWithinSR(pos))
				if (point.getValue() > 0)
		}
		return bestTile;
	}*/

	private static MapTile selectPosition(List<MapTile> possiblePositions, Diff func, Ant ant) {
		if (possiblePositions.size() == 0){
			System.err.println("NONE");
			return ant.getMapTile();
		}
		if (possiblePositions.size() == 1)
			return possiblePositions.get(0);
		// select points with highest positive value 
		HashSet<Point> target_set =new HashSet<Point>();
		double max_value = 0;
		for (Point point : func.getPointWithinSR(positions.get(ant))){
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
		//System.err.println("TARGETSET"+target_set);
		
		if (target_set.isEmpty()){
			return possiblePositions.get(0);
		}
		
		// Positions that covers largest subset of target_set
		max_value = 0;
		List<MapTile> new_possible_positions = new ArrayList<MapTile>();
		
		// subsets for each position
		HashMap<MapTile, HashSet<Point>> subsets = new HashMap<MapTile, HashSet<Point>>();
		for (MapTile pos : possiblePositions){
			HashSet<Point> subset = new HashSet<Point>();
			int value = 0;
			for (Point point : func.getPointWithinSR(pos)){
				if (target_set.contains(point)){
					subset.add(point);
					value+=1;
					if (max_value < value)
						max_value = value;
				}
			}
			subsets.put(pos, subset);
		}
		//System.err.println("SUBSETS: "+subsets);
		
		Set<Point> intersection = null;
		for (MapTile pos : possiblePositions){
			HashSet<Point> subset = subsets.get(pos);
			if (subset.size() < max_value)
				continue;
			if (intersection == null){
				intersection = subset;
				new_possible_positions.add(pos);
			} else {
				boolean isSame=true;
				for (Point point : subset){
					if (!intersection.contains(point)){
						isSame = false;
						break;
					}
				}
				if (isSame)
					new_possible_positions.add(pos);
			}
		}
		
		//System.err.println(func);
		func.substract(intersection);
		return selectPosition(new_possible_positions, func, ant);
	}
	
}
