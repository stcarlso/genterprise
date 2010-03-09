import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.sun.opengl.util.Animator;

public class GameWindow extends JPanel {
	Object sync= new Object();
	Player player;
	private PhysicsThread physics;
	Animator anim;
	public GameWindow() {
		super(new BorderLayout());	
		//setSize(width,height);
		player= new Player();
		GLCapabilities glcaps = new GLCapabilities(); 
		GLCanvas glCanvas = new GLCanvas(glcaps);

		GLGameListener listener= new GLGameListener(player,sync);
		glCanvas.addKeyListener(listener);
		glCanvas.addGLEventListener(listener);   // add event listener
		add(glCanvas);
		physics= new PhysicsThread();
		physics.start();
		anim=new Animator(glCanvas);
		anim.start();
	}
	//***************************PHYSICS THREAD***********************
	public class PhysicsThread extends Thread {
		private double muk=.7;
		private double gravity=9.8/100;
		private long dt;
		private long time=0;
		private long initiate=0;
		public PhysicsThread() {			
			dt=1;
		}
		public void run() {
			while(true) {
				synchronized (sync) {
					time+=dt;
					//working with character moves
					if(player.ability!=null && player.ability.started) {
						initiate=time;
						player.ability.started=false;
					}
					if(player.ability!=null && time>=initiate+player.ability.duration) {
						player.ability=null;
					}
					//ground friction
					if(player.vx>0) {
						player.ax+= Math.max(-player.vx/dt,-muk*gravity);
					} else if(player.vx<0) {
						player.ax+= Math.min(-player.vx/dt,muk*gravity);
						//player.ax= player.ax+muk*gravity;
					}
					//gravity and ground detection
					if(player.y<=0 && player.vy<=0) {
						player.ay=0;
						player.vy=0;
					} else
						player.ay=-gravity;
					player.vy+=player.ay*dt;
					player.vx+=player.ax*dt;
					if(player.y+player.vy*dt<0)
						player.y=0;
					else 
						player.y+=player.vy*dt;
					//move with velocity (speed limit of 1)
					player.x+=Math.signum(player.vx)*Math.min(1,Math.abs(player.vx))*dt;
					player.ax=0;
					player.ay=0;
				}
				try {				
					Thread.sleep(33L);
				} catch (InterruptedException e) {}
			}			
		}
		
	}
}
