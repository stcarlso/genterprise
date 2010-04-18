/**
 * Instead of using strings to indicate a property of the player, each possible state is linked to a number.
 *
 */
public interface Constants {	
	/*
	 * Status variable indicates special state of the user:
	 * 0: normal
	 * 1: ducking
	 * 2: walking
	 * 3: invincibility
	 * 4: helplessness  
	 */
	public static final int NORMAL=0;
	public static final int WALKING=1;
	public static final int DUCKING=2;
	public static final int INVINCIBLE=3;
	public static final int HELPLESS=4;	
	
	//Used to store directions in an int
	public static final int RIGHT=0;
	public static final int UP=1;
	public static final int LEFT=2;
	public static final int DOWN=3;
	
}
