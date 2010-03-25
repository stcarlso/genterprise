/**
 * A dodge that leaves you vulnerable but can be used in the air
 */
public class AirDodge extends Move {
	public AirDodge(Player player) {
		super(player);
		start=3;
		end=11;
		duration=18;
	}

	public void endEffect() {
		player.status=NORMAL;
	}
	public void initiate() {}
	public void linger() {}
	public void startEffect() {
		player.status=INVINCIBLE;
	}
}
