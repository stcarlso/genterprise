/**
 * A quick roll that moves you forward or backward
 */
public class Roll extends Move {
	public Roll(Player player, int dir) {
		super(player,dir);
		start=2;
		end=17;
		duration=18;
	}
	
	public void startEffect() {
		player.status = INVINCIBLE;
		if(player.facingRight && dir==RIGHT)
			player.facingRight=false;
		else if(!player.facingRight && dir==LEFT)
			player.facingRight=true;
	}
	public void endEffect() {
		player.status = NORMAL;
	}
	public void initiate() {
		if(dir==LEFT)
			player.vx=-0.8;
		else
			player.vx=0.8;
		player.vy=.1;
	}
	public void linger() {}
	public void continuous() {}
}
