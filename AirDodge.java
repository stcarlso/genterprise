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
}
