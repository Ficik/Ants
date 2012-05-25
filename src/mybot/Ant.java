package mybot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mybot.algo.DCOP;
import mybot.algo.DCOP_MST;
import mybot.algo.Goal;
import mybot.algo.PotentialFields;
import mybot.algo.SimpleCombat;
import core.Aim;
import core.Ilk;

public class Ant implements Goal {

	/* ====== Ant manager part ======== */

	private static ArrayList<Ant> ants = new ArrayList<Ant>();

	public static void addAnt(int row, int col) {
		if (!GameState.getMap().getTile(row, col).isOccupied())
			new Ant(row, col);
	}

	public static void removeAnt(int row, int col) {
		Ant ant = GameState.getMap().getTile(row, col).getAnt();
		if (ant != null) {
			ant.changeCredibility(-1);
			ants.remove(ant);
			ant.unsetPosition();
		}
	}

	public static void scheduleMoves() {
		for (Ant ant : ants) {
			ant.scheduleMove();
		}
	}

	public static void performMoves() {
		for (Ant ant : ants) {
			ant.performScheduledMove();
		}
	}

	public static List<Ant> getAnts() {
		return ants;
	}
	
	public static void removeDeadAnts(){
		List<Ant> deads = new ArrayList<Ant>();
		for (Ant ant : ants)
			if (GameState.getCore().getIlk(ant.maptile) == Ilk.DEAD)
				deads.add(ant);
		for (Ant ant : deads)
			removeAnt(ant.getMapTile().getRow(), ant.getMapTile().getCol());
				
	}

	/* === Single ant specific part === */

	private int lastMove = -1;
	
	private MapTile maptile = null;
	private List<MapTile> scheduledMoves = new ArrayList<MapTile>();
	private Target assignedTarget;

	private Ant(int row, int col) {
		setPosition(row, col);
		ants.add(this);
		changeCredibility(1);
	}

	
	public void changeCredibility(int amount){
		for (MapTile tile : DCOP.getCurrentlyVisibleMapTilesByAntAtMapTile(maptile))
			tile.changeCredibility(amount);
	}
	
	public void scheduleMove() {
		scheduledMoves.clear();
		if (hasAssignedTarget())
			scheduleMoveToTarget();
		scheduleMoveToFight();
		if (scheduledMoves.isEmpty())
			scheduleMoveToDiscover();
	}

	private void scheduleMoveToTarget() {
		scheduledMoves = maptile.getPassableNeighbours();
		Collections.sort(scheduledMoves, new Comparator<MapTile>() {
			@Override
			public int compare(MapTile o1, MapTile o2) {
				return ((Integer) ((int) (o1.getRealDistance(assignedTarget.getMapTile())))).compareTo((int) (o2
						.getRealDistance(assignedTarget.getMapTile())));
			}
		});
	}

	private void scheduleMoveToFight() {

	}

	private void scheduleMoveToDiscover() {
		List<MapTile> possibleMoves = maptile.getPassableNeighbours();
		//scheduledMoves = maptile.getPassableNeighbours();
		
		//PotentialFields.instance.reorderMoves(scheduledMoves);
		possibleMoves.add(0, maptile);
		//scheduledMoves.clear();
		scheduledMoves.add(DCOP_MST.positions.get(this));
		//scheduledMoves.add(DCOP.selectPosition(possibleMoves));
		//scheduledMoves = DCOP.getNeighbourMapTilesOrderedByExploringValue(maptile);
	}

	public void performScheduledMove() {
		if (lastMove == GameState.getRound())
			return;
		lastMove = GameState.getRound();
		changeCredibility(-1);
		for (MapTile tile : scheduledMoves) {
			if (tryMoveToMapTile(tile))
				break;
		}
		
		changeCredibility(1);
	}

	private void unsetPosition() {
		if (maptile == null)
			return;
		maptile.setAnt(null);
		maptile = null;
	}

	private void setPosition(int row, int col) {
		setPosition(GameState.getMap().getTile(row, col));
	}

	private void setPosition(MapTile maptile) {
		unsetPosition();
		this.maptile = maptile;
		maptile.setAnt(this);
	}

	public MapTile getMapTileInDirection(Aim aim) {
		return maptile.getPassableNeighbour(aim);
	}

	public boolean tryMoveToMapTile(MapTile maptile) {
		if (maptile == null || !SimpleCombat.getInstance().isSafe(maptile))
			return false;
		if (maptile.isOccupied())
			maptile.getAnt().performScheduledMove();
		if (maptile.isOccupied())
			return false;
		issueMoveOrder(maptile);
		setPosition(maptile);
		return true;
	}

	private void issueMoveOrder(MapTile destination) {
		Aim direction = GameState.getCore().getDirections(maptile, destination).get(0);
		GameState.getCore().issueOrder(maptile, direction);
	}

	@Override
	public MapTile getMapTile() {
		return maptile;
	}

	public boolean assignTargetIfBetter(Target target) {
		if (isNewTargetIsBetter(target)) {
			assignTarget(target);
			return true;
		}
		return false;
	}

	public boolean hasAssignedTarget() {
		return (assignedTarget != null && assignedTarget.targetExists());
	}

	private boolean isNewTargetIsBetter(Target target) {
		if (!hasAssignedTarget())
			return true;
		int newDistance = target.getRealDistance(maptile);
		int oldDistance = assignedTarget.getRealDistance(maptile);
		return (newDistance != MapTile.UNSET && newDistance < oldDistance);
	}

	private void assignTarget(Target target) {
		if (hasAssignedTarget())
			assignedTarget.unassign();
		assignedTarget = target;
		assignedTarget.assign();
	}

	@Override
	public String toString() {
		return "Ant at " + maptile + ": Target: " + assignedTarget + " ["
				+ (hasAssignedTarget() ? "exists" : "dont exists") + "]";
	}

}
