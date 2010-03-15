import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.JPanel;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.j2d.Overlay;

public class GameWindow extends JPanel implements Constants {
	boolean up;
	boolean upDone;
	boolean left;
	boolean right;
	boolean down;
	boolean act;
	int width=800;
	int height=800;
	long time=0;
	char action='a';
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
		private double kair=.5;
		private double muk=.9;
		private double gravity=9.8/100;
		private long dt;
		private long initiate=0;
		private long effectStart=0;
		private long effectEnd=0;
		private long stop=0;
		public PhysicsThread() {			
			dt=1;
		}
		public void run() {
			while(true) {
				time+=dt;
				
				//player state determination
				if(player.y<=0 && player.vy<=0) { //this condition will be obsolete after level editor
					player.jumps=2;
					player.wallJumps=1;
					if(down)
						player.position=DUCKING;
					else
						player.position=STANDING;
					if(player.ability!=null && player.ability instanceof AirDodge)
						player.ability=null;
				} else if(player.x<=0){
					player.position=WALLONLEFT;
				} else
					player.position=AIRBORNE;
				
				//****************KEY RESPONSE*******************
				//refresh when touching ground, tell the player where he is
				
				if(player.ability==null){
					if(act) {
						if(player.position==AIRBORNE) {
							player.ability=new AirDodge();
							player.ability.started=true;
						} else if(player.position==STANDING || player.position==DUCKING){
							if(down) {
								player.ability=new Dodge();
								player.vx=0;
							}
							else if(up) {
								player.ability=new Dive();
								if(player.facingRight)
									player.vx=1.5;
								else
									player.vx=-1.5;
								player.vy=.9;
							}
							else if(left) {
								player.ability=new Roll();
								player.vx=-.8;
							}
							else if(right) {
								player.ability=new Roll();
								player.vx=.8;
							} else {
								player.ability=new Activate();									
							}
							player.ability.started=true;
						}

						act=false;
					} else {
						
						//basic movement
						if(left) {
							player.facingRight=false;
							if(player.position==DUCKING)
								player.ax=-.06;
							else if(player.position==STANDING)
								player.ax=-.1;
							else if(player.position==WALLONRIGHT && player.wallJumps>0) {
								player.wallJumps=0;
								player.vy=1;
								player.ax=-.3;
							} else if(player.position==AIRBORNE)
								player.ax=-.07;
						}
						if(right) {
							player.facingRight=true;
							if(player.position==DUCKING)
								player.ax=.06;
							else if(player.position==STANDING)
								player.ax=.1;
							else if(player.position==WALLONLEFT && player.wallJumps>0) {
								player.wallJumps=0;
								player.vy=1;
								player.ax=.3;
							} else if(player.position==AIRBORNE)
								player.ax=.07;
						}
						if(up && !upDone && player.jumps>0) {
							player.vy=1;
							player.jumps--;
						}
						upDone=up;
					}
				}
				
				//working with character moves
				if(player.ability!=null && player.ability.started) {
					player.suspicion+=10;
					initiate=time;
					effectStart= time+player.ability.start;
					effectEnd= time+player.ability.end;
					stop= time+player.ability.duration;
					player.ability.started=false;
				}
				
				//any move effect should be invoked here
				if(player.ability!=null && time>=effectStart && time<=effectEnd) {
					player.color= Color.yellow;
				}
				
				if(player.ability!=null && time>=stop) {
					player.ability=null;
				}
				
				//acceleration calculations
				if(player.position==AIRBORNE) {		//air friction
					if(player.vx>0) {
						player.ax+= Math.max(-player.vx/dt,-kair*player.vx/player.m);
					} else if(player.vx<0) {
						player.ax+= Math.min(-player.vx/dt,-kair*player.vx/player.m);
					}
				} else {		//ground friction
					if(player.vx>0) {
						player.ax+= Math.max(-player.vx/dt,-muk*gravity);
					} else if(player.vx<0) {
						player.ax+= Math.min(-player.vx/dt,muk*gravity);
					}
				}
				//gravity and ground detection
				player.ay+=-gravity;
				player.vy+=player.ay*dt;
				player.vx+=player.ax*dt;				
				player.y+=player.vy*dt;
				if(player.y<0) //obsolete after block collision, stops you from falling into ground
					player.y=0;
				//move with velocity (speed limit of 1)
				player.x+=Math.signum(player.vx)*Math.min(1,Math.abs(player.vx))*dt;
				if(player.x<0)	//obsolete after block collision, stops you from falling into walls
					player.x=0;
				player.ax=0;
				player.ay=0;
				
				//recover suspicion
				player.suspicion=Math.max(0,player.suspicion-.1);
			
				try {				
					Thread.sleep(33L);
				} catch (InterruptedException e) {}
			}
		}		
	}
	
	public class GLGameListener implements GLEventListener, KeyListener {
		Overlay overlay;
		
		public GLGameListener(Player you, Object synch) {
			player=you;
		}
		public void display(GLAutoDrawable drawable) {
			GL gl = drawable.getGL();   		
			GLU glu = new GLU(); 	
		 	gl.glClearColor(0.0f,0.0f,0.0f,0.0f);
			gl.glMatrixMode(GL.GL_PROJECTION);
		 	gl.glLoadIdentity();		 			 	
		 	glu.gluPerspective(60,1.0,1.0,100);
		 	gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		 	glu.gluLookAt(0,10,30,0,10,0,0,1,0);
		 	gl.glMatrixMode(GL.GL_MODELVIEW);
		 	
		 	//Draw the axes, which will not be visible in the final game
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
			drawCharacter(gl);
			drawOverlay();
		}
		/**
		 * Draw the character onto the screen. This method needs to be completely changed for the real character model
		 * @param gl
		 */
		public void drawCharacter(GL gl) {
			//drawing the player
			if(player.ability instanceof Dodge)
				gl.glColor3f(0f,1f,0f);
			else if(player.ability instanceof Roll)
				gl.glColor3f(.5f,1f,0f);
			else if(player.ability instanceof Dive)
				gl.glColor3f(0f,1f,.5f);
			else if(player.ability instanceof AirDodge)
				gl.glColor3f(0f,.5f,1f);
			else if(player.ability instanceof Activate)
				gl.glColor3f(0f,0f,1f);
			else
				gl.glColor3f(1f,1f,1f);		
			gl.glBegin(GL.GL_QUADS);
				if(player.position==DUCKING) {
					gl.glVertex3d(player.x,player.y,0);
					gl.glVertex3d(player.x+1,player.y,0);
					gl.glVertex3d(player.x+1,player.y+1,0);
					gl.glVertex3d(player.x,player.y+1,0);
				} else {
					gl.glVertex3d(player.x,player.y,0);
					gl.glVertex3d(player.x+1,player.y,0);
					gl.glVertex3d(player.x+1,player.y+2,0);
					gl.glVertex3d(player.x,player.y+2,0);
				}
			gl.glEnd();
			gl.glColor3f(0f,0f,0f);	
			gl.glBegin(GL.GL_LINES);
				if(player.facingRight) {
					gl.glVertex3d(player.x+.5,player.y+1.5,0);
					gl.glVertex3d(player.x+1,player.y+1.5,0);
				} else {
					gl.glVertex3d(player.x,player.y+1.5,0);
					gl.glVertex3d(player.x+.5,player.y+1.5,0);
				}
			gl.glEnd();	
		}
		
		/**
		 * Draws 2D elements as an overlay on the screen, including the suspicion meter
		 */
		public void drawOverlay() {
			Graphics2D g2=overlay.createGraphics();
			g2.setColor(Color.white);
			g2.fillRect(width-100,100,40,40);
			g2.setColor(Color.green);
			g2.drawString(player.suspicion+"",100,100);
			System.out.println(360-(int)player.suspicion);
			g2.fillArc(width-100,100,40,40,90,360-(int)player.suspicion);
			overlay.drawAll();
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
		 	overlay = new Overlay(drawable);
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
			if(e.getKeyCode()==KeyEvent.VK_UP) {
				up=false;
			}
			if(e.getKeyCode()==KeyEvent.VK_DOWN)
				down=false;
		}
		public void keyTyped(KeyEvent e) {
			if(e.getKeyChar()==action)
				act=true;
		}
		
	}
}
