/**
 * A quick forward tumble
 */
public class Dive extends Move {
	public Dive(Player player) {
		super(player);
		start=4;
		end=22;
		duration=25;
	}
	
	public void initiate() {
		player.status=DUCKING;
		player.top=.9;
		player.left=0;
		player.right=1.7;
		player.vy=.3;
	}

	public void endEffect() {
		player.vx=0;
		player.status=NORMAL;
		player.left=.1;
		player.right=.9;
		player.top=1.75;
	}
	public void linger() {}
	public void startEffect() {}
	public void continuous(GameWindow win) {
		if(player.facingRight)
			player.vx=.6;
		else
			player.vx=-.6;
		player.status=DUCKING;
	}
}
