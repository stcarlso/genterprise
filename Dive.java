/**
 * A quick forward tumble
 */
public class Dive extends Move {
	public Dive(Player player) {
		super(player);
		start=4;
		end=25;
		duration=29;
	}
	
	public void initiate() {
		if(player.facingRight)
			player.vx=.9;
		else
			player.vx=-.9;
		player.vy=.3;
	}

	public void endEffect() {}
	public void linger() {}
	public void startEffect() {}
	public void continuous() {}
}
