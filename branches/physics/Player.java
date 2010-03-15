import java.awt.Color;

public class Player implements Constants {
	double x;
	double y;
	double vx;
	double vy;
	double ax;
	double ay;
	double m;
	int jumps;
	int wallJumps;
	int position;
	double suspicion;
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
		jumps=0;
		wallJumps=0;
		position=STANDING;
		suspicion=0;
		boolean facingRight=true;
		ability=new Move();
		color= Color.white;
	}
}
