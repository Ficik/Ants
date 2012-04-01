package mybot;

import java.util.ArrayList;
import java.util.List;
import mybot.algo.Goal;
import core.Aim;

public class Ant implements Goal {
	
	/* ====== Ant manager part ======== */
	
	private static ArrayList<Ant> ants = new ArrayList<Ant>();
	
	public static void addAnt(int row, int col){
		if (!GameState.getMap().getTile(row, col).isOccupied())
			new Ant(row, col);
	}
	
	public static void removeAnt(int row, int col) {
		Ant ant = GameState.getMap().getTile(row, col).getAnt();
		if (ant != null){
			ants.remove(ant);
			ant.unsetPosition();
		}
	}
	
	public static void scheduleMoves(){
		for (Ant ant : ants) {
			ant.scheduleMove();
		}
	}
	
	public static void performMoves(){
		for (Ant ant : ants) {
			ant.performScheduledMove();
		}
	}
	
	public static List<Ant> getAnts() {
		return ants;
	}
	
	/* === Single ant specific part === */
	
	private MapTile maptile = null;
	private ArrayList<MapTile> scheduledMoves = new ArrayList<MapTile>();
	private Target assignedTarget;
	
	
	private Ant(int row, int col) {
		setPosition(row, col);
		ants.add(this);
	}
	
	public void scheduleMove(){
		scheduledMoves.clear();
		scheduleMoveForFood();
		scheduleMoveToFight();
		scheduleMoveToDiscover();
	}

	private void scheduleMoveForFood(){
		
	}
	
	private void scheduleMoveToFight(){
		
	}
	
	private void scheduleMoveToDiscover(){
		
	}
	
	public void performScheduledMove(){
		for (MapTile tile : scheduledMoves) {
			if (tryMoveToMapTile(tile)) 
				break;
		}
	}
	
	
	private void unsetPosition(){
		if (maptile == null) return;
		maptile.setAnt(null);
		maptile = null;
	}
	
	private void setPosition(int row, int col){
		setPosition(GameState.getMap().getTile(row, col));
	}
	
	private void setPosition(MapTile maptile){
		unsetPosition(); 
		this.maptile = maptile;
		maptile.setAnt(this);
	}
	
	public MapTile getMapTileInDirection(Aim aim){
		return maptile.getPassableNeighbour(aim);
	}
	
	public boolean tryMoveToMapTile(MapTile maptile){
		if (maptile == null || maptile.isOccupied()) 
			return false;
		issueMoveOrder(maptile);
		setPosition(maptile);
		return true;
	}
	
	private void issueMoveOrder(MapTile destination){
		Aim direction = GameState.getCore().getDirections(maptile, destination).get(0);
		GameState.getCore().issueOrder(maptile, direction);
	}

	@Override
	public MapTile getMapTile() {
		return maptile;
	}

	public void assignTargetIfBetter(Target target) {
		if (isNewTargetIsBetter(target))
			assignTarget(target);
	}
	
	public boolean hasAssignedTarget(){
		return (assignedTarget != null);
	}
	
	private boolean isNewTargetIsBetter(Target target){
		if (!hasAssignedTarget()) return true;
		int newDistance = target.getRealDistance(maptile);
		int oldDistance = assignedTarget.getRealDistance(maptile);
		return (newDistance != MapTile.UNSET && newDistance < oldDistance);
	}
	
	private void assignTarget(Target target){
		if (hasAssignedTarget())
			assignedTarget.unassign();
		assignedTarget = target;
		assignedTarget.assign();
	}
	
}
