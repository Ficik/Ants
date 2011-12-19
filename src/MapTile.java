import java.util.ArrayList;
import java.util.HashMap;


public class MapTile extends Tile {

	static Map owner = null;
	public Ilk value = Ilk.UNKNOWN;
	public int last_seen = 0;
	public int row, col;
	public HashMap<Character,Target> contains = new HashMap<Character,Target>();
	private HashMap<MapTile,Integer>distances = new HashMap<MapTile,Integer>(); 
	
	public MapTile(int row, int col) {
		super(row, col);
		this.row = row;
		this.col = col;
	}
	
	public int getAge(){
		if (value == Ilk.WATER) return 0;
		return MyBot.round-last_seen;
	}
	
	/**
	 * Checks if there's any food, ant, enemy, hill on this tile
	 * @return 
	 */
	public boolean isEmpty(){
		return contains.isEmpty();
	}
	/**
	 * @param dest
	 * @return real distance to dest if known, -1 otherwise.
	 */
	public int getDistance(MapTile dest) {
		if (dest.equals(this)) return 0;
		Integer d = distances.get(dest);
		if (d == null) d = dest.distances.get(this);
		if (d == null) d = -1;
		return d.intValue();
	}
	
	public void setDistance(MapTile dest, int dist){
		if (dist == 0)
			return;
		distances.put(dest, dist);
	}
	
	public MapTile getNeighbour(Aim aim){
		return owner.getMapTile(row+aim.getRowDelta(), col+aim.getColDelta());
	}
	
	public ArrayList<MapTile> getNeighbours(boolean all){
		ArrayList<MapTile> results = new ArrayList<MapTile>();
		MapTile tile = owner.getMapTile(row+1, col);
		if (all || tile.isPassable()) results.add(tile);
		tile = owner.getMapTile(row-1, col);
		if (all || tile.isPassable()) results.add(tile);
		tile = owner.getMapTile(row, col+1);
		if (all || tile.isPassable()) results.add(tile);
		tile = owner.getMapTile(row, col-1);
		if (all || tile.isPassable()) results.add(tile);
		return results;
	}
		
	public boolean isPassable(){
		return value == Ilk.LAND;
	}

	public boolean isVisible() {
		return Map.ants.isVisible(this);
	}
}
