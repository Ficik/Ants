package mybot;


import java.util.TreeMap;


public class Map {
	
	private TreeMap<Integer, MapTile> tiles = new TreeMap<Integer, MapTile>();
	public static int cols, rows;
	
	public Map() {
		Map.cols = GameState.getCore().getCols();
		Map.rows = GameState.getCore().getRows();
		GameState.setMap(this);
	}
	
	public MapTile getTile(int row, int col){
		return tiles.get(MapTile.CalculateHash(row, col));
	}
	
	public MapTile createTile(int row, int col){
		MapTile mapTile = new MapTile(row, col);
		return tiles.put(MapTile.CalculateHash(row, col),mapTile);
	}

	public void updateWholeMap() {
		for(MapTile tile : tiles.values()){
			tile.updateState();
		}
	}
	
	
}
