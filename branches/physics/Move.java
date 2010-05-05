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
	
	public abstract void initiate();
	public abstract void startEffect();
	public abstract void continuous();
	public abstract void endEffect();
	public abstract void linger();
}
