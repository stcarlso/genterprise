import java.awt.Color;

import com.sun.opengl.util.texture.Texture;

public class Player implements Constants {
	double x;
	double y;
	double vx;
	double vy;
	double ax;
	double ay;
	double scoutx;
	double scouty;
	double m;
	double left;
	double right;
	double top;
	double bottom;
	
	int wallJumps;
	int suspicion;
	int status;
	
	boolean groundJump;
	boolean airJump;
	boolean facingRight;
	
	boolean[] walls;
	Move ability;
	Color color; //may be deprecated as soon as player models are available
	Texture stand;
	Texture[] walk;
	Texture[] ladder;
	Texture air;
	Texture duck;
	Texture[] crawl;
	
	public Player() {
		//texture loading
		walk = new Texture[8];
		ladder = new Texture[2];
		crawl = new Texture[4];
		walls = new boolean[4];
		reset();
	}
	public void reset() {
		x=0;
		y=0;
		vx=0;
		vy=0;
		ax=0;
		ay=0;
		m=4;
		
		//hitbox of the player
		left=.1;
		right=.9;
		top=1.75;
		bottom=0;
		
		wallJumps=1;
		
		suspicion=0;
		facingRight=true;

		walls[DOWN] = walls[UP] = walls[LEFT] = walls[RIGHT] = false;
		ability=null;
		color= Color.white;
		
		status=NORMAL;
	}
}
