import java.awt.Color;
import java.io.File;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

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
	int status;
	
	boolean facingRight;
	
	boolean[] walls;
	Move ability;
	Color color; //may be deprecated as soon as player models are available
	Texture stand;
	Texture[] walk;
	Texture air;
	Texture duck;
	
	public Player() {
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
		
		jumps=2;
		wallJumps=1;
		
		suspicion=0;
		facingRight=true;
		
		walls = new boolean [4];
		ability=null;
		color= Color.white;
		
		status=NORMAL;
		
		//texture loading
		walk = new Texture[8];
		
	}
}
