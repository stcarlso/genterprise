import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.sun.opengl.util.Animator;

public class GameWindow extends JPanel implements KeyListener {
	private int width;
	private int height;
	Player player;
	private PhysicsThread physics;
	Animator anim;
	public GameWindow() {
		super(new BorderLayout());
		width= 400;
		height= 400;		
		//setSize(width,height);
		player= new Player();
		GLCapabilities glcaps = new GLCapabilities(); 
		GLCanvas glCanvas = new GLCanvas(glcaps);
		GLGameListener listener= new GLGameListener();
		glCanvas.addGLEventListener(listener);   // add event listener
		add(glCanvas);
		physics= new PhysicsThread();
		physics.start();
		anim=new Animator(glCanvas);
		anim.start();
	}
	public void keyPressed(KeyEvent e) {	
		if(e.getKeyChar()==KeyEvent.VK_LEFT)
			player.ax=-10;
		else if(e.getKeyChar()==KeyEvent.VK_RIGHT)
			player.ax=10;
	}
	public void keyReleased(KeyEvent e) {
	}
	public void keyTyped(KeyEvent e) {
	}
	public class PhysicsThread extends Thread {
		private double muk;
		private double gravity=9.8;
		private long dt;
		private long time;
		public PhysicsThread() {
			dt=1;
		}
		public void run() {

			if(player.y<0) {
				player.ay=0;
			}
			player.vy+=player.ay*dt;
			player.vx+=player.ax*dt;
			player.y+=player.vy;
			player.x+=player.vx;
			if(player.ax>0 && player.ax+muk*player.m*gravity>0)
				player.ax-=muk*player.m*gravity;
			else if(player.ax<0 && player.ax+muk*player.m*gravity<0)
				player.ax+=muk*player.m*gravity;
		}
		
	}
}
