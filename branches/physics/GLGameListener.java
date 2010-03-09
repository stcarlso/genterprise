import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;


public class GLGameListener implements GLEventListener, KeyListener  {
	Object sync;
	Player player;
	boolean up;
	boolean left;
	boolean right;
	boolean down;
	boolean act;
	char action='a';
	public GLGameListener(Player you, Object synch) {
		player=you;
		sync=synch;
	}
	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();   		
		GLU glu = new GLU(); 	
	 	gl.glClearColor(0.0f,0.0f,0.0f,0.0f);
		gl.glMatrixMode(GL.GL_PROJECTION);
	 	gl.glLoadIdentity();		 			 	
	 	glu.gluPerspective(60,1.0,1.0,100);
	 	gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
	 	glu.gluLookAt(0,0,50,0,0,0,0,1,0);
	 	gl.glMatrixMode(GL.GL_MODELVIEW);
	 	gl.glBegin(GL.GL_LINES);
	 		gl.glColor3f(0f,0f,1f);
	 		gl.glVertex3d(0,-50,0);
	 		gl.glVertex3d(0,50,0);
	 		gl.glColor3f(1f,0f,0f);
			gl.glVertex3d(-50,0,0);
			gl.glVertex3d(50,0,0);	 			
			gl.glEnd();
			gl.glColor3f(0f,1f,0f);
			gl.glVertex3d(0,0,-9001);
			gl.glVertex3d(0,0,9001);	 			
		gl.glEnd();
		//drawing the player
		if(player.ability instanceof Dodge)
			gl.glColor3f(0f,1f,0f);
		else if(player.ability instanceof Roll)
			gl.glColor3f(.5f,1f,0f);
		else
			gl.glColor3f(1f,1f,1f);			
		gl.glBegin(GL.GL_QUADS);
			if(player.position.equals("ducking")) {
				gl.glVertex3d(player.x-1,player.y-1,0);
				gl.glVertex3d(player.x+1,player.y-1,0);
				gl.glVertex3d(player.x+1,player.y,0);
				gl.glVertex3d(player.x-1,player.y,0);
			} else {
				gl.glVertex3d(player.x-1,player.y-1,0);
				gl.glVertex3d(player.x+1,player.y-1,0);
				gl.glVertex3d(player.x+1,player.y+1,0);
				gl.glVertex3d(player.x-1,player.y+1,0);
			}
		gl.glEnd();
		//****************KEY RESPONSE*******************
		//refresh when touching ground, tell the player where he is
		if(player.y<=0) { //this condition will be obsolete after level editor
			player.position="standing";
			player.jumps=1;
			if(down)
				player.position="ducking";
			else
				player.position="standing";
		} else
			player.position="airborne";
		
		if(act) {
			if(down && !player.position.equals("airborne")) {
				player.ability=new Dodge();
				player.ability.started=true;
				player.vx=0;
			}
			if(left && !player.position.equals("airborne")) {
				player.ability=new Roll();
				player.ability.started=true;
				player.vx=-.8;
			}
			if(right && !player.position.equals("airborne")) {
				player.ability=new Roll();
				player.ability.started=true;
				player.vx=.8;
			}
			act=false;
		} else if(player.ability==null){
			
			//basic movement
			if(left) {
				player.facingRight=false;
				if(player.position.equals("ducking"))
					player.ax=-.06;
				else
					player.ax=-.1;
			}
			if(right) {
				player.facingRight=true;
				if(player.position.equals("ducking"))
					player.ax=.06;
				else
					player.ax=.1;
			}
			if(up && player.jumps>0) {
				player.vy=1;
				player.jumps--;
				up=false;
			}
		}
	}

	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {		
	}

	public void init(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();   		
		GLU glu = new GLU(); 	
		gl.glClearColor(0.0f,0.0f,0.0f,0.0f);
		gl.glMatrixMode(GL.GL_PROJECTION);
	 	gl.glLoadIdentity();		 			 	
	 	glu.gluPerspective(60,1.0,1.0,100);
	}

	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
		
	}
	
	//**************KEY LISTENER********************
	public void keyPressed(KeyEvent e) {		
		if(e.getKeyCode()==KeyEvent.VK_LEFT)
			left=true;
		if(e.getKeyCode()==KeyEvent.VK_RIGHT)
			right=true;
		if(e.getKeyCode()==KeyEvent.VK_UP)
			up=true;
		if(e.getKeyCode()==KeyEvent.VK_DOWN)
			down=true;
	}
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_LEFT)
			left=false;
		if(e.getKeyCode()==KeyEvent.VK_RIGHT)
			right=false;
		if(e.getKeyCode()==KeyEvent.VK_UP)
			up=false;
		if(e.getKeyCode()==KeyEvent.VK_DOWN)
			down=false;
	}
	public void keyTyped(KeyEvent e) {
		if(e.getKeyChar()==action)
			act=true;
	}
	
}
