import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import mybot.Map;
import mybot.MapTile;

import core.Aim;
import core.Ants;


public class Agent extends Target {

	public static Ants ants;
	public static ArrayList<Agent> mine = new ArrayList<Agent>();
	public ArrayList<Aim> possible_moves = new ArrayList<Aim>();
	public Food assigned_food = null;
	public MapTile assigned_hill = null;
	public static HashMap<Aim, ArrayList<int[]>> viewDiff = new HashMap<Aim, ArrayList<int[]>>();
	public static ArrayList<int[]> attack_deltas = new ArrayList<int[]>();
	public static final List<Aim> default_prefered = (Arrays
			.asList(Aim.WEST, Aim.NORTH, Aim.EAST, Aim.SOUTH));
	public ArrayList<Aim> prefered = new ArrayList<Aim>();

	public Agent(MapTile position) {
		super(position);
		mine.add(this);

		// generate random prefered directions
		Random r = new Random();
		int i = r.nextInt(4);
		int direction = r.nextBoolean() ? 1 : -1;
		for (int j = 0; j < 4; j++) {
			prefered.add(default_prefered.get(i));
			i += direction;
			if (i < 0)
				i = 3;
			else if (i > 3)
				i = 0;
		}
	}

	public static void generatedAttackDeltas() {
		int r2 = ants.getAttackRadius2();
		int r = (int) Math.sqrt(r2)+1;
		r2 = (2*r)*(2*r);

		for (int col = -r*2; col <= r*2; col++) {
			for (int row = -r*2; row <= r*2; row++) {
				if (row * row + col * col <= r2)
					attack_deltas.add(new int[] { row, col });
			}
		}
	}

	public static void generateViewDiffs() {
		int r2 = ants.getViewRadius2();
		int r = (int) Math.sqrt(r2);
		// north
		ArrayList<int[]> north = new ArrayList<int[]>();
		ArrayList<int[]> south = new ArrayList<int[]>();
		ArrayList<int[]> east = new ArrayList<int[]>();
		ArrayList<int[]> west = new ArrayList<int[]>();
		viewDiff.put(Aim.NORTH, north);
		viewDiff.put(Aim.SOUTH, south);
		viewDiff.put(Aim.EAST, east);
		viewDiff.put(Aim.WEST, west);

		for (int col = -r - 1; col <= r + 1; col++) {
			for (int row = -r - 1; row < r + 1; row++) {
				if (row * row + col * col > r2) {
					if ((row + 1) * (row + 1) + col * col <= r2)
						north.add(new int[] { row, col });
					if ((row - 1) * (row - 1) + col * col <= r2)
						south.add(new int[] { row, col });
					if ((row) * (row) + (col + 1) * (col + 1) <= r2)
						west.add(new int[] { row, col });
					if ((row) * (row) + (col - 1) * (col - 1) <= r2)
						east.add(new int[] { row, col });
				}
			}
		}

	}

	public int getViewDiff(Aim aim) {
		Map map = Map.instance;
		int value = 0;
		ArrayList<int[]> deltas = viewDiff.get(aim);
		for (int[] delta : deltas) {
			value += map.getMapTile(position.row + delta[0],
					position.col + delta[1]).getAge();
		}
		return value;
	}

	public static void updateAgent(int row, int col) {
		MapTile tile = Map.instance.getMapTile(row, col);
		if (!tile.contains.containsKey(Target.MY_ANT))
			new Agent(tile);
	}

	public static void removeAgent(int row, int col) {
		MapTile tile = Map.instance.getMapTile(row, col);
		Agent agent = (Agent) tile.contains.get(Target.MY_ANT);
		if (agent != null) {
			agent.destroy();
		}
	}

	public void scheduleMove() {
		possible_moves.clear();
		// MyBot.log.info("Assigned for "+this+" is "+(assigned_food!=null?assigned_food:"Nothing"));
		if (assigned_food != null) {
			if (!assigned_food.position.contains.containsKey(Target.FOOD)) {
				assigned_food = null;
				scheduleMove();
				return;
			}
			ArrayList<MapTile> ns = position.getNeighbours(false);
			int best = Integer.MAX_VALUE;
			for (MapTile n : ns) {
				if (MapTile.owner.getSafety(n) <= 0)
					continue;
				int d = n.getDistance(assigned_food.position);
				if (d < best && d != -1) {
					best = d;
					possible_moves.clear();
				}
				if (d == best)
					possible_moves.addAll(ants.getDirections(position, n));
			}
		} else if(assigned_hill != null) {
			if (!ants.getEnemyHills().contains(assigned_hill)){
				assigned_hill = null;
				scheduleMove();
				return;
			}
			ArrayList<MapTile> ns = position.getNeighbours(false);
			int best = Integer.MAX_VALUE;
			for (MapTile n : ns) {
				if (MapTile.owner.getSafety(n) <= 0)
					continue;
				int d = n.getDistance(assigned_hill);
				if (d < best && d != -1) {
					best = d;
					possible_moves.clear();
				}
				if (d == best)
					possible_moves.addAll(ants.getDirections(position, n));
			}
			
		} else {
			for (MapTile tile : position.getNeighbours(false)) {
				if (MapTile.owner.getSafety(tile) < 0)
					continue;
				possible_moves.addAll(ants.getDirections(position, tile));
			}
		}
	}

	@Override
	public void moveTo(Aim direction) {
		ants.issueOrder(position, direction);
		super.moveTo(direction);
	}

	public void performMove() {
		Aim selected = null;
		double best_score = -1;

		for (Aim aim : prefered) {
			if (possible_moves.contains(aim)) {
				if (position.getNeighbour(aim).isEmpty()) {
					double score = getMoveScore(aim);
					// MyBot.log.info(this+" -> "+aim+" = "+score);
					if (best_score < score) {
						selected = aim;
						best_score = score;
					}
				}
			}
		}
		if (selected != null)
			moveTo(selected);
	}

	public double getMoveScore(Aim aim) {
		return getViewDiff(aim);
	}

	public static ArrayList<MapTile> getPositions() {
		ArrayList<MapTile> positions = new ArrayList<MapTile>();
		for (Agent agent : mine) {
			positions.add(agent.position);
		}
		return positions;

	}

	@Override
	public char getType() {
		return Target.MY_ANT;
	}

	@Override
	public void destroy() {
		mine.remove(this);
		super.destroy();
	}
}
