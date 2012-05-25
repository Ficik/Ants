package mybot.algo.dcop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import mybot.GameState;
import mybot.MapTile;
import mybot.algo.PotentialFields;


public class Diff {
	
	private Diff parent = null;
	private HashMap<MapTile,Double> changes = new HashMap<MapTile, Double>();
	
	public static int counter = 0;
	
	public Diff(Diff parent){
		this.parent = parent;
	}
	
	public void changeValueBy(List<Point> area, double value){
		for (Point point: area){
			Double v = changes.get(point.tile);
			if (v == null)
				v = 0D;
			setValue(point.tile, v+value);
		}
	}
	
	public void changeValueTo(List<Point> area, double value){
		for (Point point: area){
			setValue(point.tile, -getValue(point.tile)+value);
		}
	}
	
	public List<Point> getPointWithinSR(MapTile pos){
		List<Point> points = new ArrayList<Point>();
		for(MapTile tile : PotentialFields.instance.getAreaWithinSR(pos))
			points.add(new Point(tile, this));
		return points;
		//return getPointWithinArea(pos, Math.sqrt(GameState.getCore().getViewRadius2()));
	}

/*	public List<Point> getPointWithinArea(MapTile pos, double radius){
		
		
		/*for (int row = (int)-radius;row <= radius;row++)
			for (int col = (int)-radius;col <= radius;col++)
				if (row*row + col*col <= radius*radius)
					points.add(getPoint(row+pos.getRow(),col+pos.getCol()));
		
	}*/

	public void substract(Collection<Point> area){
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
		counter++;
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
		counter++;	
		changes.put(tile, value);
	}

	
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		for (int col = 0; col < GameState.getMap().cols; col++)
			sb.append((col % 10) + "");
		sb.append("\n");
		for (int row = 0; row < GameState.getMap().rows; row++) {
			sb.append((row % 10) + " ");
			for (int col = 0; col < GameState.getMap().cols; col++){
				sb.append(String.format("%3.0f", getValue(row,col)));
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
}
