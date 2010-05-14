/**
 * Heals the player's suspicion more quickly but the player cannot move
 */
public class Hide extends Move {
	public Hide(Player player) {
		super(player);
		start=30;
		end=200;
		duration=220;
		player.scoutx=player.x;
		player.scouty=player.y;
	}
	
	public void startEffect() {}
	public void endEffect() {
	}
	public void initiate() {
		player.status = HELPLESS;
	}
	public void linger() {}
	public void continuous(GameWindow win) {
		player.suspicion=Math.max(0,player.suspicion-2);
	}
	public boolean causesSuspicion() {
		return false;
	}
}
