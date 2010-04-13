/**
 * Instead of using strings to indicate a property of the player, each possible state is linked to a number.
 *
 */
public interface Constants {	
	/*
	 * Status variable indicates special state of the user:
	 * 0: normal
	 * 1: ducking
	 * 2: invincibility
	 * 3: helplessness  
	 */
	public static final int NORMAL=0;
	public static final int DUCKING=1;
	public static final int INVINCIBLE=2;
	public static final int HELPLESS=3;	
	
	//Used to store directions in an int
	public static final int RIGHT=0;
	public static final int UP=1;
	public static final int LEFT=2;
	public static final int DOWN=3;
	
}
