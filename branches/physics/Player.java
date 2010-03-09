
public class Player {
	double x;
	double y;
	double vx;
	double vy;
	double ax;
	double ay;
	double m;
	int jumps;
	boolean facingRight;
	String position;
	Move ability;
	public Player() {
		x=0;
		y=0;
		vx=0;
		vy=0;
		ax=0;
		ay=0;
		m=40;
		jumps=0;
		position="standing";
		boolean facingRight=true;
		ability=new Move();
	}
}
