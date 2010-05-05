/**
 * Move forward quickly
 */
public class Dash extends Move {
	public Dash(Player player, int dir) {
		super(player,dir);
		start=1;
		end=15;
		duration=15;
	}
	
	public void startEffect() {
		player.status=WALKING;
	}
	public void endEffect() {
		player.status = NORMAL;
	}
	public void initiate() {
	}
	public void linger() {}
	public void continuous(GameWindow win) {
		if(dir==LEFT)
			player.vx=-0.6;
		else
			player.vx=0.6;
		player.status=WALKING;
	}
}
