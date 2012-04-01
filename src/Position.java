
public class Position {

	public int x,y;
	private static Position highBound;
	
	public static void setHighBound(int x, int y){
		highBound = new Position(x, y);
	}
	
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public int hashCode() {
		return highBound.y*y+x;
	}
	
	@Override
	public String toString() {
		return "("+x+", "+y+")";
	}
	
}
