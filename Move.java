/**
 * All abilities fit under this superclass.
 */
public abstract class Move implements Constants{
	Player player;
	boolean started = false;
	long duration;
	long start;
	long end;
	int dir;
	public Move(Player player) {
		this.player = player;
		started=true;
	}
	public Move(Player player,int dir) {
		this.player = player;
		this.dir=dir;
		started=true;
	}
	
	/**
	 * Effects that activate as soon as the button is pressed
	 */
	public abstract void initiate();
	/**
	 * Effects that start when the move's start frames are reached
	 */
	public abstract void startEffect();
	/**
	 * Effects that happen while the move is in progress
	 */
	public abstract void continuous(GameWindow win);
	/**
	 * Effects that activate when the move's end frames are reached
	 */
	public abstract void endEffect();
	/**
	 * Effects that activate after the move ends and continue after the move is deleted
	 */
	public abstract void linger();
	/**
	 * Some moves do not cause suspicion
	 */
	public boolean causesSuspicion() {
		return true;
	}
}
