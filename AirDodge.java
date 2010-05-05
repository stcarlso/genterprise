/**
 * A dodge that leaves you vulnerable but can be used in the air
 */
public class AirDodge extends Move {
	public AirDodge(Player player) {
		super(player);
		start=2;
		end=19;
		duration=20;
	}

	public void endEffect() {
		player.status=NORMAL;
	}
	public void initiate() {}
	public void linger() {}
	public void startEffect() {
		player.status=INVINCIBLE;
		player.vy+=.2;
	}
	public void continuous(GameWindow win) {}
}
