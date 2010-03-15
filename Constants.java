/**
 * Instead of using strings to indicate a property of the player, each possible state is linked to a number.
 *
 */
public interface Constants {
	/* Position variable indicates the player's state:
	 * 0: standing
	 * 1: ducking
	 * 2: airborne
	 * 3: on a ladder
	 */
	public static final int STANDING=0;
	public static final int DUCKING=1;
	public static final int AIRBORNE=2;
	public static final int WALLONLEFT=3;
	public static final int WALLONRIGHT=4;
}
