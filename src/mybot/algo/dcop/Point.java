package mybot.algo.dcop;

import mybot.GameState;
import mybot.MapTile;

public class Point {

	public MapTile tile;
	public Diff func;
	
	public Point(MapTile tile, Diff func) {
		this.tile = tile;
		this.func = func;
	}
	

	public boolean within_range(MapTile center, double range){
		return GameState.getCore().getDistance(center, tile) <= range*range;
	}
	
	public double getValue() {
		return func.getValue(tile);
	}
	
	@Override
	public boolean equals(Object obj) {
		return tile.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return tile.hashCode();
	}
}
