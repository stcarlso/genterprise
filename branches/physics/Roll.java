/**
 * A quick roll that moves you forward or backward
 */
public class Roll extends Move {
	public Roll(Player player, int dir) {
		super(player,dir);
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
	public void initiate() {
		player.vx=dir*.2;
	}
	public void linger() {}
}
