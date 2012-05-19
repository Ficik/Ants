import java.io.IOException;
import java.util.Iterator;

import mybot.Ant;
import mybot.Food;
import mybot.GameState;
import mybot.Hill;
import mybot.Map;
import mybot.MapTile;
import mybot.algo.AStar;
import mybot.algo.DCOP;
import mybot.algo.PotentialFields;
import mybot.algo.SimpleCombat;
import core.Bot;
import core.Ilk;
import core.Tile;

/**
 * Starter bot implementation.
 */
public class MyBot extends Bot {

	public static final int MY_BOT = 0;

	public static void main(String[] args) throws IOException {
		MyBot bot = new MyBot();
		bot.readSystemInput();
	}

	public void prepare() {
		GameState.setCore(getAnts());
		GameState.setMap(new Map());
		DCOP.initGameSpecificVariables();
	}

	@Override
	public void doTurn() {
		if (GameState.getRound() == 0)
			prepare();

		GameState.getInstance().prepareNewRound();

		for (Hill hill : Hill.all) {
			Iterator<MapTile> it = AStar.antSearch(hill.getMapTile());
			while(it.hasNext()){
				MapTile closest = it.next();
				if (closest == null)
					continue;
				Ant ant = closest.getAnt();
				if (ant != null)
					ant.assignTargetIfBetter(hill);
			}
		}

		Food.checkAndRemoveLostFood();
		Food.tryAssignFood();
		Ant.scheduleMoves();
		Ant.performMoves();

		GameState.getLogger().logMap();
		SimpleCombat.instance.closeTurn();
	}

	/* *********** *
	 * LISTENERS * ***********
	 */

	@Override
	public void addFood(int row, int col) {
		Food.addFood(row, col);
		super.addFood(row, col);
	}

	@Override
	public void addAnt(int row, int col, int owner) {
		// System.err.println(owner+": "+row+" "+col);
		SimpleCombat.instance.addAnt(owner, row, col);
		if (owner == MY_BOT)
			Ant.addAnt(row, col);
		super.addAnt(row, col, owner);
	}

	@Override
	public void removeAnt(int row, int col, int owner) {
		if (owner == MY_BOT)
			Ant.removeAnt(row, col);
		super.removeAnt(row, col, owner);
	}

	@Override
	public void addWater(int row, int col) {
		GameState.getMap().getTile(row, col).setValue(Ilk.WATER);
		PotentialFields.generatePotentialFromTile(row, col);
	}
	
	@Override
	public void addHill(int row, int col, int owner) {
		if (owner != MY_BOT)
			Hill.addHill(GameState.getMap().getTile(row, col));
		super.addHill(row, col, owner);
	}
}
