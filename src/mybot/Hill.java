package mybot;

import java.util.HashSet;

public class Hill extends Target {

	public static HashSet<Hill> all = new HashSet<Hill>();
	
	
	public static void addHill(MapTile tile) {
		all.add(new Hill(tile));
	}

	MapTile tile;
	
	public Hill(MapTile tile) {
		this.tile = tile;
	}

	@Override
	public MapTile getMapTile() {
		return tile;
	}

}
