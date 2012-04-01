package mybot;

import java.util.ArrayList;
import java.util.List;

import mybot.algo.Goal;

public class Food extends Target implements Goal {

	/* ==== Food Manager ===== */
	
	private static List<Food> food = new ArrayList<Food>();
	
	public static void addFood(int row, int col){
		new Food(row, col);
	}
	
	public static void removeFood(int row, int col){
		GameState.getMap().getTile(row, col).getFood().destroy();
	}
	
	
	/* ===== Food Instance ====== */
	
	private MapTile maptile = null;
	
	
	private Food(int row, int col) {
		maptile = GameState.getMap().getTile(row, col);
		maptile.setFood(this);
		food.add(this);
	}
	
	private void destroy() {
		maptile.unsetFood();
		food.remove(this);
	}
	
	@Override
	public MapTile getMaptile() {
		return maptile;
	}

}
