package mybot;

import java.util.ArrayList;
import java.util.List;

import mybot.algo.AStar;
import mybot.algo.Goal;

public class Food extends Target implements Goal {

	/* ==== Food Manager ===== */

	private static List<Food> food = new ArrayList<Food>();

	public static void addFood(int row, int col) {
		new Food(row, col);
	}

	public static void removeFood(int row, int col) {
		GameState.getMap().getTile(row, col).getFood().destroy();
	}

	public static void checkAndRemoveLostFood() {
		for (Food the_food : food)
			the_food.checkAndSolveExistance();
	}

	public static List<Food> getFood() {
		return food;
	}

	public static void tryAssignFood() {
		for (Food the_food : food)
			if (!the_food.isAssigned())
				the_food.tryToAssignToClosestAnt();
	}

	/* ===== Food Instance ====== */

	private MapTile maptile = null;

	private Food(int row, int col) {
		maptile = GameState.getMap().getTile(row, col);
		maptile.setFood(this);
		food.add(this);
	}

	@Override
	protected void destroy() {
		maptile.unsetFood();
		food.remove(this);
		super.destroy();
	}

	@Override
	public MapTile getMapTile() {
		return maptile;
	}

	public void checkAndSolveExistance() {
		if (maptile.isVisible()
				&& !GameState.getCore().getFoodTiles().contains(maptile))
			destroy();
	}

	public void tryToAssignToClosestAnt() {
		MapTile closest = AStar.antSearch(maptile);
		if (MapTile.isValidTileWithAnt(closest))
			closest.getAnt().assignTargetIfBetter(this);
	}

}
