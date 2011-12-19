import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Starter bot implementation.
 */
public class MyBot extends Bot {

	Map map;
	static Logger log;
	static int round = 0;

	/**
	 * Main method executed by the game engine for starting the bot.
	 * 
	 * @param args
	 *            command line arguments
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static void main(String[] args) throws IOException {
		log = Logger.getLogger("ANTS");
		//Handler handler = new FileHandler("../Ants.log", false);
		//handler.setFormatter(new SimpleFormatter());
		//log.addHandler(handler);
		log.setLevel(Level.OFF);
		log.info("========== Starting bot ==========");

		MyBot b = new MyBot();
		b.readSystemInput();
		b.prepare();
	}

	public void prepare() {
		map = new Map(getAnts());
		Agent.ants = getAnts();
		Astar.ants = getAnts();
		Astar.map = map;
		Agent.generateViewDiffs();
		Agent.generatedAttackDeltas();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doTurn() {
		if (round == 0)
			prepare();

		
		log.info("========== Starting new round ("+round+") ==========");
		round += 1;
		Ants ants = getAnts();
		map.generateInfluence();
		refreshMap();
		Food.checkFood(ants.getFoodTiles());
		//log.info("\n"+map.toString());

		ArrayList<MapTile> agent_positions = Agent.getPositions();
		for (Tile hill : getAnts().getEnemyHills()){
			Astar astar = new Astar(map.getMapTile(hill),
					(ArrayList<MapTile>) agent_positions.clone());
			MapTile closest = astar.next();
			if (closest != null){
				Agent agent = (Agent) closest.contains.get(Target.MY_ANT);
				agent.assigned_hill = map.getMapTile(hill);
			}
			
		}
		
		for (Food food : Food.known) {
			Astar astar = new Astar(food.position,
					(ArrayList<MapTile>) agent_positions.clone());
			MapTile closest = astar.next();
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
		}

		for (Agent ant : Agent.mine) {
			ant.scheduleMove();
		}
		for (Agent ant : Agent.mine) {
			ant.performMove();
		}
	}

	private void refreshMap() {
		Ants ants = getAnts();
		for (int row = 0; row < ants.getRows(); row++) {
			for (int col = 0; col < ants.getCols(); col++) {
				MapTile tile = map.getMapTile(row, col);
				if (ants.isVisible(tile)) {
					tile.last_seen = round;
					if (tile.value == Ilk.UNKNOWN
							&& ants.getIlk(tile).isPassable())
						tile.value = Ilk.LAND;
				}
			}
		}
	}

	@Override
	public void addFood(int row, int col) {
		new Food(map.getMapTile(row, col));
		super.addFood(row, col);
	}

	public static int getRound() {
		return round;
	}
	
	@Override
	public void addAnt(int row, int col, int owner) {
		if (owner == 0) {
			Agent.updateAgent(row, col);
		}
		super.addAnt(row, col, owner);
	}

	@Override
	public void removeAnt(int row, int col, int owner) {
		if (owner == 0) {
			Agent.removeAgent(row, col);
		}
		super.removeAnt(row, col, owner);
	}

	@Override
	public void addWater(int row, int col) {
		map.update(Ilk.WATER, row, col);
	}
}
