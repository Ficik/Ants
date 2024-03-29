package mybot.algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import core.Tile;

import mybot.GameState;
import mybot.Map;
import mybot.MapTile;

public class SimpleCombat {

	public static SimpleCombat instance;

	HashMap<Integer, HashMap<MapTile, Integer>> influence = new HashMap<Integer, HashMap<MapTile, Integer>>(10);
	HashMap<MapTile, Integer> total = new HashMap<MapTile, Integer>(15000);
	HashMap<Tile, Integer> owners = new HashMap<Tile, Integer>(200);
	float attackRadius;

	public SimpleCombat(float attackRadius) {
		this.attackRadius = attackRadius;
	}

	public static SimpleCombat getInstance() {
		if (instance == null)
			instance = new SimpleCombat((float) Math.sqrt(GameState.getCore().getAttackRadius2()) + 1);
		return instance;
	}

	public boolean isSafe(MapTile tile) {
		int attacking = getTotalInfluence(tile) - getInfluence(0, tile);
		if (attacking == 0)
			return true;
		int attackers = attacking;
		int best = Integer.MAX_VALUE;
		for (MapTile maptile : fieldsInRadius(tile.getRow(), tile.getCol(), attackRadius)) {
			Integer owner = owners.get(maptile);
			if (owner == null || owner == 0)
				continue;
			attackers--;
			int value = getTotalInfluence(maptile) - getInfluence(owner, maptile);
			if (value < best) {
				best = value;
				if (best < attacking)
					return false;
			}
			if (attackers == 0)
				break;
		}
		return true;
	}

	public void addAnt(Integer owner, int row, int col) {
		if (GameState.getMap() != null) {
			owners.put(GameState.getMap().getTile(row, col), owner);
			for (MapTile maptile : fieldsInRadius(row, col, attackRadius))
				increaseInflunce(owner, maptile);
		}
	}

	private List<MapTile> fieldsInRadius(int row, int col, double d) {
		MapTile tile = GameState.getMap().getTile(row, col);
		if (tile.AR == null) {
			List<MapTile> list = new ArrayList<MapTile>();
			for (int r = (int) -d; r <= d; r++)
				for (int c = (int) -d; c <= d; c++)
					if (c * c + r * r <= d * d)
						list.add(GameState.getMap().getTile(row + r, col + c));
			tile.AR = list;
		}
		return tile.AR;
	}

	private void increaseInflunce(int owner, MapTile tile) {
		HashMap<MapTile, Integer> ownersInfluence = influence.get((Integer) owner);
		if (ownersInfluence == null) {
			ownersInfluence = new HashMap<MapTile, Integer>(15000);
			influence.put(owner, ownersInfluence);
		}
		increaseValue(ownersInfluence, tile);
		increaseValue(total, tile);
	}

	private void increaseValue(HashMap<MapTile, Integer> map, MapTile tile) {
		Integer value = map.get(tile);
		if (value == null)
			value = new Integer(0);
		value += 1;
		map.put(tile, value);
	}

	public int getInfluence(Integer owner, MapTile tile) {
		HashMap<MapTile, Integer> ownersInfluence = influence.get(owner);
		if (ownersInfluence == null)
			return 0;
		return valueOr(ownersInfluence.get(tile), 0);
	}

	public int getTotalInfluence(MapTile tile) {
		return valueOr(total.get(tile), 0);
	}

	public void closeTurn() {
		influence.clear();
		total.clear();
		owners.clear();
	}

	private int valueOr(Integer prefered, int fallback) {
		if (prefered == null)
			return fallback;
		return prefered;
	}

}
