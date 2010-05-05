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
		player.airJump=false;
		player.vy=.5;
		if(player.facingRight)
			player.vx=.35;
		else
			player.vx=-.35;
	}
	public void linger() {
		player.status = HELPLESS;
	}
	public void endEffect() {}
	public void startEffect() {}
	public void continuous(GameWindow win) {}
}
