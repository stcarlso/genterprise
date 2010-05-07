/**
 * An aerial dodge that moves the player left or right
 */
public class MovingAirDodge extends Move {
	public MovingAirDodge(Player player, int dir) {
		super(player,dir);
		start=2;
		end=25;
		duration=30;
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
		player.vx=0;
	}
	public void initiate() {}
	public void linger() {
		player.status=HELPLESS;
	}
	public void continuous(GameWindow win) {
		if(dir==LEFT)
			player.vx=-0.2;
		else
			player.vx=0.2;
		player.vy=0;
	}
}
