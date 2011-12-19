
public abstract class Target {

	public static final char FOOD = 'f',MY_ANT = 'a', ENEMY_ANT='e', MY_HIVE='A',ENEMY_HIVE='E';
	
	public MapTile position;
	
	public Target(MapTile position) {
		this.position = position;
		position.contains.put(getType(),this);
	}
	
	public void moveTo(Aim direction){
		MapTile new_position = Map.instance.getMapTile(position.row+direction.getRowDelta(), position.col+direction.getColDelta());
		position.contains.remove(getType());
		new_position.contains.put(getType(),this);
		position = new_position;
	}
	
	abstract public char getType();

	public void destroy(){
		position.contains.remove(getType());
	}
	
	@Override
	public String toString() {
		return this.getClass().getName()+"@("+position+")";
	}
	
}
