

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import mybot.Ant;
import mybot.Food;
import mybot.GameLogger;
import mybot.GameState;
import mybot.Map;
import mybot.MapTile;
import mybot.algo.AStar;
import mybot.algo.PotentialFields;

import core.Ants;
import core.Bot;
import core.Ilk;
import core.Tile;

/**
 * Starter bot implementation.
 */
public class MyBot extends Bot {

	public static final int MY_BOT = 0;
	
	private GameState state = new GameState();
	

	
	public static void main(String[] args) throws IOException {
		MyBot bot = new MyBot();
		bot.readSystemInput();
		bot.prepare();
	}
	

	public void prepare() {
		GameState.setCore(getAnts());
		new Map();
		
		

		Agent.generateViewDiffs();
		Agent.generatedAttackDeltas();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doTurn() {
		state.prepareNewRound();
		
		for (Tile hill : getAnts().getEnemyHills()){
			MapTile closest = AStar.antSearch(GameState.getMap().getTile(hill.getRow(), hill.getCol()));
			if (isValidTileWithAnt(closest))
				closest.getAnt().assignHill(hill);
		}
		
		Food.tryAssignFood();
		
		Ant.scheduleMoves();
		Ant.performMoves();
		
		
		
		
		



		
	/*	
	 * map.generateInfluence();
		ArrayList<MapTile> agent_positions = Agent.getPositions();
		for (Tile hill : getAnts().getEnemyHills()){
			Astar astar = new Astar(map.getMapTile(hill),
					(ArrayList<MapTile>) agent_positions.clone());
			MapTile closest = astar.next();
			if (closest != null){
				Agent agent = (Agent) closest.contains.get(Target.MY_ANT);
				agent.assigned_hill = map.getMapTile(hill);
			}
				
			if (closest != null) {
				Agent agent = (Agent) closest.contains.get(Target.MY_ANT);
				if (agent.assigned_food != null &&
				    agent.position.getDistance(agent.assigned_food.position) <= 
					agent.position.getDistance(food.position)) {
						// TODO: call next() again to find another close agent;
						continue; // workaround
				}
					// |aA-f| > |A-f| .. I'll go for it. I'm closer
				if (food.assigned_agent != null && 
					food.assigned_agent.position.getDistance(food.position) > 
					agent.position.getDistance(food.position)) {
						food.assigned_agent.assigned_food = null;
					
				}
				food.assigned_agent = agent;
				agent.assigned_food = food;
			}
		}*/
	}
	
	
	
	
	
	/* *********** *
	 *  LISTENERS  *
	 * *********** */

	@Override
	public void addFood(int row, int col) {
		Food.addFood(row, col);
		super.addFood(row, col);
	}
	
	@Override
	public void addAnt(int row, int col, int owner) {
		if (owner == MY_BOT)
			Ant.addAnt(row,col);
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
}
