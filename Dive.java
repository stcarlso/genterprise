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
			player.vx=1.5;
		else
			player.vx=-1.5;
		player.vy=.9;
	}
}
