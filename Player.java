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
	int position;
	boolean facingRight;
	Move ability;
	Color color; //may be deprecated as soon as player models are available
	public Player() {
		x=0;
		y=0;
		vx=0;
		vy=0;
		ax=0;
		ay=0;
		m=4;
		jumps=0;
		position=STANDING;
		boolean facingRight=true;
		ability=new Move();
		color= Color.white;
	}
}
