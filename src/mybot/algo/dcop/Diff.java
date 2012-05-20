package mybot.algo.dcop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mybot.GameState;
import mybot.MapTile;


public class Diff {
	
	private Diff parent = null;
	private HashMap<MapTile,Double> changes = new HashMap<MapTile, Double>();
	
	public Diff(Diff parent){
		this.parent = parent;
	}
	
	public void changeValueBy(List<Point> area, double value){
		for (Point point: area){
			setValue(point.tile, getValue(point.tile)+value);
		}
	}
	
	public List<Point> getPointWithinSR(MapTile pos){
		return getPointWithinArea(pos, Math.sqrt(GameState.getCore().getViewRadius2()));
	}

	public List<Point> getPointWithinArea(MapTile pos, double radius){
		List<Point> points = new ArrayList<Point>();
		for (int row = (int)-radius;row <= radius;row++)
			for (int col = (int)-radius;col <= radius;col++)
				if (row*row + col*col <= radius*radius)
					points.add(getPoint(row+pos.getRow(),col+pos.getCol()));
		return points;
	}

	public void substract(List<Point> area){
		for (Point point: area){
			double value = getValue(point.tile);
			setValue(point.tile, -value); //value+Math.min(value,1)
		}
	}
	
	private Point getPoint(int row, int col){
		MapTile tile = GameState.getMap().getTile(row, col);
		return new Point(tile,this);
	}
	
	public double getValue(int row, int col){
		return getValue(GameState.getMap().getTile(row, col));
	}

	public double getValue(MapTile tile) {
		Double update = changes.get(tile);
		if (update == null)
			update = 0D;
		if (parent == null)
			return tile.getCurrentDifference() + update;
		return parent.getValue(tile) + update;
	}
	
	private void setValue(int row, int col, double value){
		setValue(GameState.getMap().getTile(row, col), value);
	}
	
	private void setValue(MapTile tile, double value){
		changes.put(tile, value);
	}

}
