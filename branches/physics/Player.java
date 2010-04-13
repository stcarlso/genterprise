import java.awt.Color;

public class Player implements Constants {
	double x;
	double y;
	double vx;
	double vy;
	double ax;
	double ay;
	double m;
	double left;
	double right;
	double top;
	double bottom;
	
	int jumps;
	int wallJumps;
	int suspicion;
	int position;
	int status;
	
	boolean facingRight;
	Move ability;
	Color color; //may be deprecated as soon as player models are available
	public Player() {
		x=1;
		y=0;
		vx=0;
		vy=0;
		ax=0;
		ay=0;
		m=4;
		
		//hitbox of the player
		left=0;
		right=1;
		top=2;
		bottom=0;
		
		jumps=2;
		wallJumps=1;
		position=STANDING;
		suspicion=0;
		facingRight=true;
		ability=null;
		color= Color.white;
		
		position=STANDING;
		status=NORMAL;
	}
}
