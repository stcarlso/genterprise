/**
 * Basic command that is used to open doors, scroll text, etc.
 */
public class Activate extends Move {
	public Activate(Player player) {
		super(player);
		start=0;
		end=0;
		duration=1;
	}
}
