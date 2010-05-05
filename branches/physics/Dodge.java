/**
 * A quick dodge that doesn't move you
 */
public class Dodge extends Move {
	public Dodge(Player player) {
		super(player);
		start=2;
		end=13;
		duration=15;
	}
	
	public void initiate() {
		player.vx=0;
	}
	public void startEffect() {
		player.status = INVINCIBLE;
	}
	public void endEffect() {
		player.status = NORMAL;
	}	
	public void linger() {}
	public void continuous(GameWindow win) {}
}
