/**
 * Slows down fall speed temporarily
 */
public class Glide extends Move {
	public Glide(Player player) {
		super(player);
		start=2;
		end=120;
		duration=124;
	}
	
	public void initiate() {}
	public void startEffect() {}
	public void endEffect() {}	
	public void linger() {}
	public void continuous(GameWindow win) {
		player.vy=Math.max(player.vy,-.05);
	}
	public boolean causesSuspicion() {
		return false;
	}
}
