import java.util.ArrayList;
import java.util.Set;

import mybot.MapTile;

import core.Tile;



public class Food extends Target {

	public static ArrayList<Food> known = new ArrayList<Food>();
	public Agent assigned_agent;
	
	
	public Food(MapTile position) {
		super(position);
		known.add(this);
	}
	
	public static void checkFood(Set<Tile> tiles){
		for (int i = 0; i < known.size(); i++) {
			Food food = known.get(i);
			if (food.position.isVisible() && !tiles.contains(food.position)){
				food.destroy();
				i--;
			}
		}
	}

	@Override
	public char getType() {
		return Target.FOOD;
	}

	@Override
	public void destroy() {
		known.remove(this);
		if (assigned_agent != null)	assigned_agent.assigned_food = null;
		super.destroy();
	}
	
}
