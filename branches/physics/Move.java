/**
 * All abilities fit under this superclass.
 */
public class Move implements Constants{
	Player player;
	boolean started = false;
	long duration;
	long start;
	long end;
	Move(Player player) {
		this.player = player;
	}
	
	public void initiate() {}
	public void startEffect() {}
	public void endEffect() {}
	public void linger() {}
}
