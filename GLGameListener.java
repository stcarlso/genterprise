import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;


public class GLGameListener implements GLEventListener {

	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();   		
		GLU glu = new GLU(); 	
	 	gl.glClearColor(0.0f,0.0f,0.0f,0.0f);
		gl.glMatrixMode(GL.GL_PROJECTION);
	 	gl.glLoadIdentity();		 			 	
	 	glu.gluPerspective(60,1.0,1.0,100);
	 	gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
	 	glu.gluLookAt(0,0,5,0,0,0,0,1,0);
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
	}

	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}

	public void init(GLAutoDrawable drawable) {
		System.out.println("hi");
		GL gl = drawable.getGL();   		
		GLU glu = new GLU(); 	
		gl.glClearColor(0.0f,0.0f,0.0f,0.0f);
		gl.glMatrixMode(GL.GL_PROJECTION);
	 	gl.glLoadIdentity();		 			 	
	 	glu.gluPerspective(60,1.0,1.0,100);
	}

	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3,
			int arg4) {
		// TODO Auto-generated method stub
		
	}

}
