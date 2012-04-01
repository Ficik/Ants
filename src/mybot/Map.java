package mybot;


import java.util.TreeMap;


public class Map {
	
	private TreeMap<Integer, MapTile> tiles = new TreeMap<Integer, MapTile>();
	public static int cols, rows;
	
	public Map() {
		Map.cols = GameState.getCore().getCols();
		Map.rows = GameState.getCore().getRows();
	}
	
	public MapTile getTile(int row, int col){
		col = (col >= Map.cols) ? 0 : (col < 0) ? Map.cols - 1 : col; // it's
		row = (row >= Map.rows) ? 0 : (row < 0) ? Map.rows - 1 : row;
		MapTile tile = tiles.get(MapTile.CalculateHash(row, col));
		if (tile == null)
			tile = createTile(row, col);
		return tile;
	}
	
	public MapTile createTile(int row, int col){
		MapTile mapTile = new MapTile(row, col);
		tiles.put(MapTile.CalculateHash(row, col),mapTile);
		return mapTile;
	}

	public void updateWholeMap() {
		for(MapTile tile : tiles.values()){
			tile.updateState();
		}
	}
	
	@Override
	public String toString() {
		String string = "";
		for (int row=0;row<rows;row++){
			for (int col=0;col<cols;col++)
				string+=getTile(row, col).getValueCode();
			string+="\n";
		}
		return string;
	}
	
	
}
