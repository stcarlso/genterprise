/**
 * Allows the player to look around the map without moving.
 */
public class Scout extends Move {
	private static final double SCOUT = .1;

	public Scout(Player player) {
		super(player);
		start=5;
		end=240;
		duration=260;
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
		if(win.left)
			player.scoutx-=SCOUT;
		if(win.right)
			player.scoutx+=SCOUT;
		if(win.down)
			player.scouty-=SCOUT;
		if(win.up)
			player.scouty+=SCOUT;
	}
	public boolean causesSuspicion() {
		return false;
	}
}
