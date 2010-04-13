/**
 * Instead of using strings to indicate a property of the player, each possible state is linked to a number.
 *
 */
public interface Constants {
	/* Position variable indicates where the player is:
	 * 0: standing
	 * 1: ducking
	 * 2: airborne
	 * 3: touches wall on left
	 * 4: touches wall on right
	 */
	public static final int STANDING=0;
	public static final int DUCKING=1;
	public static final int AIRBORNE=2;
	public static final int WALLONLEFT=3;
	public static final int WALLONRIGHT=4;
	
	/*
	 * Status variable indicates special state of the user:
	 * 0: normal
	 * 1: invincibility
	 * 2: helplessness  
	 */
	public static final int NORMAL=0;
	public static final int INVINCIBLE=1;
	public static final int HELPLESS=2;	
	
	//Used to store directions in an int
	public static final int RIGHT=0;
	public static final int UP=1;
	public static final int LEFT=2;
	public static final int DOWN=3;
	
}
