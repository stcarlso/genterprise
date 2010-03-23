/**
 * A quick roll that moves you forward or backward
 */
public class Roll extends Move {
	public Roll(Player player) {
		super(player);
		start=2;
		end=12;
		duration=18;
	}
	
	public void startEffect() {
		player.status = INVINCIBLE;
	}
	public void endEffect() {
		player.status = NORMAL;
	}
}
