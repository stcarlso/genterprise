/**
 * An extra jump that puts the player into a helples state afterwards
 */
public class ThirdJump extends Move {
	public ThirdJump(Player player) {
		super(player);
		start=2;
		duration=5;
	}
	
	public void initiate() {
		player.vy=1.3;
		if(player.facingRight)
			player.vx=.6;
		else
			player.vx=-.6;
	}
	public void linger() {
		player.status = HELPLESS;
	}


	public void endEffect() {}
	public void startEffect() {}
}
