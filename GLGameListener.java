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
		gl.glBegin(GL.GL_QUADS);
			gl.glVertex3d(player.x-1,player.y-1,0);
			gl.glVertex3d(player.x+1,player.y-1,0);
			gl.glVertex3d(player.x+1,player.y+1,0);
			gl.glVertex3d(player.x-1,player.y+1,0);
		gl.glEnd();
		//****************KEY RESPONSE*******************
		if(left) 
			player.vx=-.3;
		if(right)
			player.vx=.3;
		if(up)
			player.vy=1;
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
		else if(e.getKeyCode()==KeyEvent.VK_RIGHT)
			right=true;
		else if(e.getKeyCode()==KeyEvent.VK_UP)
			up=true;
				
	}
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_LEFT)
			left=false;
		else if(e.getKeyCode()==KeyEvent.VK_RIGHT)
			right=false;
		else if(e.getKeyCode()==KeyEvent.VK_UP)
			up=false;
	}
	public void keyTyped(KeyEvent e) {
	}
	
}
