import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class Map {
	
	static Ants ants;
	static Map instance;
	public MapTile[][] tiles;
	public int cols, rows;
	private HashMap<Tile,Integer> influence = new HashMap<Tile, Integer>();
	
	
	public Map(Ants ants) {
		MapTile.owner = this;
		Map.instance = this;
		Map.ants = ants;
		cols = ants.getCols();
		rows = ants.getRows();
		tiles = new MapTile[rows][cols];

		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tiles[row][col] = new MapTile(row, col);
			}
		}
	}
	
	public MapTile getMapTile(Tile tile){
		return getMapTile(tile.getRow(),tile.getCol());
	}
	
	public MapTile getMapTile(int row, int col){
		return tiles[(row+rows)%rows][(col+cols)%cols];
	}
	
	@Override
	public String toString() {
		String s = "";
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[0].length; j++) {
				s+=String.format("|%2d", getSafety(tiles[i][j]));
			}
			s+="\n";
		}
		
		return s;
	}

	public void update(Ilk value, int row, int col) {
		getMapTile(row,col).value = value;
	}
	
	/**
	 * 
	 * @param tile
	 * @return 1 : safe, -2 : danger, 0 : swap
	 */
	public int getSafety(Tile tile){
		Integer value = influence.get(tile);
		if (value == null)
			return 1;
		return value;
			/* || value > 0)
			return 1;
		if (value == 0)
			return 0;
		return -1;*/
	}
	
	public void generateInfluence(){
		influence.clear();
		Set<Tile> enemies = ants.getEnemyAnts();
		Set<Tile> mine = ants.getMyAnts();
		ArrayList<int[]> deltas = Agent.attack_deltas;
		for (int[] delta : deltas){
			for (Tile enemy : enemies){
				MapTile tile = getMapTile(enemy.getRow()+delta[0], enemy.getCol()+delta[1]);
				Integer value = influence.get(tile);
				if (value == null)
					value = -1;
				else
					value -=1; 
				influence.put(tile, value);
			}
			for (Tile my : mine){
				MapTile tile = getMapTile(my.getRow()+delta[0], my.getCol()+delta[1]);
				Integer value = influence.get(tile);
				if (value == null)
					value = 1;
				else
					value +=1; 
				influence.put(tile, value);
			}
		}
		
		
	}
	
}
