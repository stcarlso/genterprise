import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;
import javax.swing.JPanel;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.texture.TextureIO;

public class GameWindow extends JPanel implements Constants {
	ResourceGetter res;
	Level level;
	Block block;
	List<GameObject> elements;
	
	boolean up;
	boolean upDone;
	boolean left;
	boolean right;
	boolean down;
	boolean act;
	boolean move;
	boolean paused;
	
	int width=800;
	int height=800;
	
	long time=0;
	
	char action='a';
	char movement='d';
	char pause='f';
	
	Object sync= new Object();
	Player player;	
	private PhysicsThread physics;
	Animator anim;
	GLU glu;
	GLCanvas canvas;
	
	public GameWindow() {
		super(new BorderLayout());	
		res = new FilesystemResources(null, new File("res/"));
		LevelReader lreader = new LevelReader(res, "../test-level.dat");
		level = lreader.getLevel();
		block = level.blockIterator().next();
		elements = new ArrayList<GameObject>(block.getElements().size());
		Iterator<GameObject> itr = block.getElements().iterator();
		while(itr.hasNext()) {
			GameObject element = itr.next();
			if (element.getSource().getVertexArray() == null)
				element.getSource().loadGeometry(res);
			if(element.getZ() == 0.)
				elements.add(element);
		}
		
		player= new Player();
		GLCapabilities glcaps = new GLCapabilities(); 
		canvas = new GLCanvas(glcaps);

		GLGameListener listener= new GLGameListener(player,sync);
		canvas.addKeyListener(listener);
		canvas.addGLEventListener(listener);   // add event listener
		add(canvas);
		
		physics= new PhysicsThread();
		physics.start();
		anim=new Animator(canvas);
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
				//collideTest();
				if(player.walls[DOWN] && player.vy<=0) { //this condition will be obsolete after level editor, tells whether player is grounded
					player.jumps=2;
					player.wallJumps=2;
					if(down && player.ability==null)
						player.status=DUCKING;
					else
						player.status=NORMAL;
					if(player.ability!=null && player.ability instanceof AirDodge) {
						player.status=NORMAL;
						player.ability=null;
					}
					if(player.status==HELPLESS)
						player.status=NORMAL;
				}
				
				//****************KEY RESPONSE*******************
				//refresh when touching ground, tell the player where he is
				
				if(player.ability==null && player.status!=HELPLESS){
					if(act) {
						if(! (player.walls[UP] || player.walls[DOWN] || player.walls[LEFT] || player.walls[RIGHT])) {
							player.ability=new AirDodge(player);
						} else if(player.walls[DOWN]){
							if(down) {
								player.ability=new Dodge(player);
							} else if(up) {
								player.ability=new Dive(player);
							} else if(left) {
								player.ability=new Roll(player,-1);
							} else if(right) {
								player.ability=new Roll(player,1);
							} else {
								player.ability=new Activate(player);									
							}
						}

						act=false;
					} else if(move) {
						if(up) {
							player.ability=new ThirdJump(player);
						}
						
						move=false;
					} else {
						
						//basic movement
						if(left) {
							player.facingRight=false;
							if(player.status==DUCKING)
								player.ax=-.06;
							else if(player.walls[DOWN])
								player.ax=-.1;
							else if(player.walls[RIGHT] && (player.wallJumps==-1 || player.wallJumps==2)) {
								player.wallJumps=1;
								player.vy=.8;
								player.ax=-.3;
							} else if(! (player.walls[UP] || player.walls[DOWN] || player.walls[LEFT] || player.walls[RIGHT]))
								player.ax=-.07;
						}
						if(right) {
							player.facingRight=true;
							if(player.status==DUCKING)
								player.ax=.06;
							else if(player.walls[DOWN])
								player.ax=.1;
							else if(player.walls[LEFT] && (player.wallJumps==1 || player.wallJumps==2)) {
								player.wallJumps=-1;
								player.vy=.8;
								player.ax=.3;
							} else if(! (player.walls[UP] || player.walls[DOWN] || player.walls[LEFT] || player.walls[RIGHT]))
								player.ax=.07;
						}
						if(up && !upDone && player.jumps>0) {
							player.vy=.8;
							player.jumps--;
						}
						upDone=up;
					}
				}
				
				//working with character moves
				if(player.ability!=null) {
					if(player.ability.started) {
						player.suspicion+=player.ability.duration*4;
						initiate=time;
						effectStart= time+player.ability.start;
						effectEnd= time+player.ability.end;
						stop= time+player.ability.duration;
						player.ability.started=false;
						player.ability.initiate();
					}
					
					//any move effect should be invoked here
					if(time==effectStart) {
						player.ability.startEffect();
					}
					
					//end move effect at its end frame
					if(time==effectEnd) {
						player.ability.endEffect();
					}
					
					if(time==stop) {
						player.ability.linger();
						player.ability=null;
					}
				}
				
				//acceleration calculations
				if(! (player.walls[UP] || player.walls[DOWN] || player.walls[LEFT] || player.walls[RIGHT])) {		//air friction
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
				collideTest();
								
				if(!(player.walls[UP] && player.vy>0) && !(player.walls[DOWN] && player.vy<=0)) {
					player.ay+=-gravity;
					player.vy+=player.ay*dt;
					player.y+=player.vy*dt;
				} else {
					player.vy=0;
					player.ay=0;
				}

						/*
				if(player.y<0) //obsolete after block collision, stops you from falling into ground
					player.y=0;*/
				
				player.vx+=player.ax*dt;
				//move with velocity (speed limit of 1)	
				if(!(player.walls[LEFT] && player.vx<0) && !(player.walls[RIGHT] && player.vx>0)) {
					player.x+=Math.signum(player.vx)*Math.min(1,Math.abs(player.vx))*dt;
				} else {					
					player.vx=0;
					player.ax=0;
				}
				player.ax=0;
				player.ay=0;
				
				//recover suspicion
				player.suspicion=Math.max(0,player.suspicion-1);
			
				try {				
					Thread.sleep(30L);
				} catch (InterruptedException e) {}
			}
		}		
		
		/**
		 * Determines on which sides the character has encountered a wall
		 */
		public void collideTest() {
			
			player.x = Math.round(1e4 * player.x)/10000.0;
			player.y = Math.round(1e4 * player.y)/10000.0;
			player.vx = Math.round(1e4 * player.vx)/10000.0;
			player.vy = Math.round(1e4 * player.vy)/10000.0;
			Iterator<GameObject> itr = elements.iterator();
			player.walls[UP]=false;
			player.walls[DOWN]=false;
			player.walls[LEFT]=false;
			player.walls[RIGHT]=false;
			double xtemp = player.x;
			double ytemp = player.y;
			while(itr.hasNext()) {
				GameObject element = itr.next();
				Element source = element.getSource();
				//floor detection
				if(player.x+player.vx*dt+player.right > element.getX() && player.x+player.vx*dt+player.left < element.getX()+source.getWidth()
					&& player.x+player.vx*dt+player.right > element.getX() && player.x+player.vx*dt+player.left < element.getX()+source.getWidth()
					&& player.y+player.vy*dt+player.bottom+1 >= element.getY()+source.getHeight() && player.y+player.vy*dt+player.bottom <= element.getY()+source.getHeight()) {
					player.walls[DOWN]=true;
					ytemp=element.getY()+source.getHeight()+player.bottom;
				}
				//ceiling detection
				if(player.x+player.vx*dt+player.right > element.getX() && player.x+player.vx*dt+player.left < element.getX()+source.getWidth()
					&& player.x+player.vx*dt+player.right > element.getX() && player.x+player.vx*dt+player.left < element.getX()+source.getWidth()
					&& player.y+player.vy*dt+player.top-1 <= element.getY() && player.y+player.vy*dt+player.top >= element.getY()) {
					player.walls[UP]=true;
					ytemp=element.getY()-player.top;
				}
				//left wall detection
				if(player.y+player.vy*dt+player.top > element.getY() && player.y+player.vy*dt+player.bottom < element.getY()+source.getHeight()
					&& player.y+player.top > element.getY() && player.y+player.bottom < element.getY()+source.getHeight()
					&& player.x+player.vx*dt+player.right >= element.getX()+source.getWidth() && player.x+player.vx*dt+player.left <= element.getX()+source.getWidth()) {
					player.walls[LEFT]=true;
					xtemp=element.getX()+source.getWidth()-player.left;
				}
				//right wall detection				
				if(player.y+player.vy*dt+player.top > element.getY() && player.y+player.vy*dt+player.bottom < element.getY()+source.getHeight()
					&& player.y+player.top > element.getY() && player.y+player.bottom < element.getY()+source.getHeight()
					&& player.x+player.vx*dt+player.right >= element.getX() && player.x+player.vx*dt+player.left <= element.getX()) {
					player.walls[RIGHT]=true;
					xtemp=element.getX()+(1-player.right)-1;
				}
			}
			xtemp = Math.round(1e4 * xtemp)/10000.0;
			ytemp = Math.round(1e4 * ytemp)/10000.0;
			if (player.walls[LEFT] != player.walls[RIGHT])
				player.x=xtemp;
			if(player.walls[UP] != player.walls[DOWN])
				player.y=ytemp;
		}
	}
	
	public class GLGameListener implements GLEventListener, KeyListener {
		private FloatBuffer suspicion;
		
		public GLGameListener(Player you, Object synch) {
			player=you;
		}
		
		public void display(GLAutoDrawable drawable) {
			GL gl = drawable.getGL();   		
		 	gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		 	
		 	gl.glLoadIdentity();
		 	glu.gluLookAt(player.x, player.y, 10, player.x, player.y, -10, 0, 1, 0);

			drawCharacter(gl);
			renderScene(gl);
		}
		
		private void renderScene(GL gl) {
			int objectCount = 1;
			synchronized (block) {
				List<GameObject> list = block.getElements();
				Iterator<GameObject> it = list.iterator();
				Element element; GameObject o; String lastTexture = null, lastModel = "";
				while (it.hasNext()) {
					gl.glPushMatrix();
					o = it.next();
					element = o.getSource();
					gl.glTranslated(o.getX(), o.getY(), o.getZ());
					doRotate(gl, element, o.getRotFlip());
					if (!Utils.properCompare(lastTexture, element.getTextureLocation())) {
						element.setTexture(gl);
						lastTexture = element.getTextureLocation();
					}
					if (!lastModel.equals(element.getGeometryLocation())) {
						element.renderModel(gl);
						lastModel = element.getGeometryLocation();
					}
					element.draw(gl);
					gl.glPopMatrix();
					objectCount++;
				}
			}
		}
		/**
		 * Correctly rotates the element.
		 * 
		 * @param gl the OpenGL context
		 * @param toRotate the object to rotate
		 * @param amount the amount to rotate
		 */
		private void doRotate(GL gl, Element toRotate, Vector3 amount) {
			if (amount.getX() != 0. || amount.getY() != 0. || amount.getZ() != 0.) {
				float x = (float)toRotate.getWidth() / 2.f, y = (float)toRotate.getHeight() / 2.f;
				gl.glTranslatef(x, y, 0);
				gl.glRotatef((float)amount.getZ(), 0, 0, 1);
				gl.glRotatef((float)amount.getY(), 0, 1, 0);
				gl.glRotatef((float)amount.getX(), 1, 0, 0);
				gl.glTranslatef(-x, -y, 0);
			}
		}
		/**
		 * Draw the character onto the screen. This method needs to be completely changed for the real character model
		 * @param gl
		 */
		public void drawCharacter(GL gl) {
			//drawing the player
			//colors will be obsolete when rendering improves
			if(player.status==HELPLESS)
				gl.glColor3f(.5f,.5f,.5f);	
			else if(player.status==INVINCIBLE)
				gl.glColor3f(1f,1f,0f);
			else if(player.ability instanceof Dodge)
				gl.glColor3f(0f,1f,0f);
			else if(player.ability instanceof Roll)
				gl.glColor3f(.5f,1f,0f);
			else if(player.ability instanceof Dive)
				gl.glColor3f(0f,1f,.5f);
			else if(player.ability instanceof AirDodge)
				gl.glColor3f(0f,.5f,1f);
			else if(player.ability instanceof Activate)
				gl.glColor3f(0f,0f,1f);
			else if(player.ability instanceof ThirdJump)
				gl.glColor3f(1f,0f,0f);	
			else
				gl.glColor3f(1f,1f,1f);
			Element.clearOptions(gl);
			player.stand.bind();
			gl.glBegin(GL.GL_QUADS);
				if(player.status==DUCKING) {
					gl.glVertex3d(player.x+player.left,player.y+player.bottom,0);
					gl.glVertex3d(player.x+player.right,player.y+player.bottom,0);
					gl.glVertex3d(player.x+player.right,player.y+player.top/2,0);
					gl.glVertex3d(player.x+player.left,player.y+player.top/2,0);
				} else {
					if(player.facingRight) {
						gl.glTexCoord2d(0,1);
						gl.glVertex3d(player.x,player.y,0);
						gl.glTexCoord2d(1,1);
						gl.glVertex3d(player.x+1,player.y,0);
						gl.glTexCoord2d(1,0);
						gl.glVertex3d(player.x+1,player.y+2,0);
						gl.glTexCoord2d(0,0);
						gl.glVertex3d(player.x,player.y+2,0);
					} else {
						gl.glTexCoord2d(1,1);
						gl.glVertex3d(player.x,player.y,0);
						gl.glTexCoord2d(0,1);
						gl.glVertex3d(player.x+1,player.y,0);
						gl.glTexCoord2d(0,0);
						gl.glVertex3d(player.x+1,player.y+2,0);
						gl.glTexCoord2d(1,0);
						gl.glVertex3d(player.x,player.y+2,0);
					}
				}
			gl.glEnd();
			/*	gunther texture coming soon
			gl.glColor3f(0f,0f,0f);	
			gl.glBegin(GL.GL_LINES);
				if(player.facingRight) {
					gl.glVertex3d(player.x+.5,player.y+1.5,.1);
					gl.glVertex3d(player.x+player.right,player.y+1.5,.1);
				} else {
					gl.glVertex3d(player.x+player.left,player.y+1.5,.1);
					gl.glVertex3d(player.x+.5,player.y+1.5,.1);
				}
			gl.glEnd();*/
			gl.glBegin(GL.GL_LINES);
	 		gl.glColor3f(0f,0f,1f);
	 		gl.glVertex3d(0,-50,0);
	 		gl.glVertex3d(0,50,0);
	 		gl.glColor3f(1f,0f,0f);
			gl.glVertex3d(-50,0,0);
			gl.glVertex3d(50,0,0);	 			
			gl.glEnd();
			Element.setOptions(gl);
			drawSuspicion(gl,player.suspicion);
		}
		/**
		 * Displays a suspicion meter on the screen
		 * @param gl
		 * @param suspicion
		 */
		private void drawSuspicion(GL gl, int suspicion) {
			int w = width, h = height;
			gl.glViewport(w - 200, h - 200, 200, 200);
			gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
			gl.glPushMatrix();
			gl.glLoadIdentity();
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glPushMatrix();
			gl.glLoadIdentity();
			gl.glDisable(GL.GL_DEPTH_TEST);
			gl.glOrtho(-1, 1, -1, 1, -1, 1);
			gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
			gl.glDisableClientState(GL.GL_COLOR_ARRAY);
			gl.glColor4f(0.7f, 0f, 0f, 0.3f);
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, this.suspicion);
			gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 2 * ((2 * suspicion / 5) / 2) + 3);
			gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
			gl.glEnableClientState(GL.GL_COLOR_ARRAY);
			gl.glEnable(GL.GL_DEPTH_TEST);
			gl.glPopMatrix();
			gl.glMatrixMode(GL.GL_MODELVIEW);
			gl.glPopMatrix();
			gl.glViewport(0, 0, w, h);
		}

		public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {		
		}

		public void init(GLAutoDrawable drawable) {
			GL gl = drawable.getGL();
			glu = new GLU();
			gl.glClearColor(0.7f,0.7f,0.6f,1.0f);
			gl.glMatrixMode(GL.GL_PROJECTION);
		 	gl.glLoadIdentity();
		 	gl.glMatrixMode(GL.GL_MODELVIEW);
		 	gl.glLoadIdentity();
		 	//glu.gluPerspective(60,1.0,1.0,100);
		 	
		 	//enables
		 	gl.glEnable(GL.GL_DEPTH_TEST);
			gl.glEnable(GL.GL_BLEND);
			gl.glDepthFunc(GL.GL_LEQUAL);
			gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glEnable(GL.GL_TEXTURE_2D);
			gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
			gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
			gl.glDisable(GL.GL_LIGHTING);
			
			Element el;
			for (GameObject o : block.getElements()) {
				el = o.getSource();
				if (el.getVertexArray() == null)
					el.loadGeometry(res);
				if (!el.hasTexture())
					el.loadTexture(res);
			}
		 	suspicion();
		 	Element.setOptions(gl);
		 	
		 	//player texture setting
		 	try {
				player.stand=TextureIO.newTexture(new File("res/textures/gunther.png"),false);
			} catch (Exception e) {
				System.out.println("no file");
			}
		}
		/**
		 * Creates a vertex array for the supicion meter
		 */
		private void suspicion() {
			suspicion = BufferUtil.newFloatBuffer(147 * 3);
			float x, y, x2, y2, ct, st;
			suspicion.put(0.f);
			suspicion.put(0.6f);
			suspicion.put(0.f);
			for (int theta = 0; theta <= 360; theta += 5) {
				x = 0.6f * (ct = (float)Math.sin(Math.toRadians(theta)));
				y = 0.6f * (st = (float)Math.cos(Math.toRadians(theta)));
				x2 = 0.45f * ct;
				y2 = 0.45f * st;
				suspicion.put(x2);
				suspicion.put(y2);
				suspicion.put(1.f);
				suspicion.put(x);
				suspicion.put(y);
				suspicion.put(1.f);
			}
			suspicion.rewind();
		}

		public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
			GL gl = arg0.getGL();
			width=arg3;
			height=arg4;
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glOrtho(-6,6,-6,6,-20,20);
			gl.glMatrixMode(GL.GL_MODELVIEW);
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
			if(e.getKeyChar()==movement)
				move=true;
			if(e.getKeyChar()==pause)
				paused^=true;
		}
		
	}
}