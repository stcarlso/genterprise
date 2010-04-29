import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import java.io.*;
import java.nio.*;
import java.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.swing.*;

import com.sun.opengl.util.*;

public class GameWindow extends JPanel implements Constants {
	private static final long serialVersionUID = 0L;

	ResourceGetter res;
	Level level;
	Block block;
	List<GameObject> elements;
	List<GameObject> lasers;
	List<GameObject> interactives;
	
	boolean up;
	boolean upDone;
	boolean left;
	boolean right;
	boolean down;
	boolean act;
	boolean move;
	boolean paused;
	
	int width=0;
	int height=0;
	
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
		setBackground(Color.BLACK);
	}
	public void start() {
		res = new FilesystemResources(null, new File("res/"));
		JLabel load = new JLabel(res.getIcon("genterprise.png"));
		load.setHorizontalAlignment(SwingConstants.CENTER);
		load.setVerticalAlignment(SwingConstants.CENTER);
		add(load, BorderLayout.CENTER);
		validate();
	
		LevelReader lreader = new LevelReader(res, "../branches/levels/test-revamp.dat");
		level = lreader.getLevel();
		block = level.blockIterator().next();
		elements = new ArrayList<GameObject>(block.getElements().size());
		lasers = new ArrayList<GameObject>(block.getElements().size());
		interactives = new ArrayList<GameObject>(block.getElements().size());
		Iterator<GameObject> itr = block.getElements().iterator();
		while(itr.hasNext()) {
			GameObject element = itr.next();
			if (element.getSource().getVertexArray() == null)
				element.getSource().loadGeometry(res);
			if(element.getZ() == 0.)
				elements.add(element);
			else if(element.getSource().getName().indexOf("laser") >= 0)
				lasers.add(element);
			else if(element.getSource().getName().indexOf("savepoint") >= 0 || element.getSource().getName().indexOf("door") >= 0 || element.getSource().getName().indexOf("ladder") >= 0)
				interactives.add(element);
		}

		player= new Player();
		GLCapabilities glcaps = new GLCapabilities(); 
		canvas = new GLCanvas(glcaps);

		GLGameListener listener= new GLGameListener(player,sync);
		canvas.addKeyListener(listener);
		canvas.addGLEventListener(listener);   // add event listener
		removeAll();
		add(canvas);
		validate();
		
		physics= new PhysicsThread();
		physics.start();
		anim=new Animator(canvas);
		anim.start();
	}
	//***************************PHYSICS THREAD***********************
	public class PhysicsThread extends Thread {
		private double kair=.5;
		private double muk=6;
		private double gravity=.03;
		private long dt;
		private long effectStart=0;
		private long effectEnd=0;
		private long stop=0;
		
		public PhysicsThread() {			
			dt=1;
		}
		public void run() {
			while(true) {
				while (paused) Utils.sleep(1L);
				time+=dt;
				
				//player state determination
				if(player.walls[DOWN] && player.vy<=0) { //this condition will be obsolete after level editor, tells whether player is grounded
					player.groundJump=true;
					player.airJump=true;
					player.wallJumps=2;
					if(down && player.ability==null) {
						player.status=DUCKING;
						player.top=.9;
						player.left=0;
						player.right=1.9;
					} else {
						player.status=NORMAL;
						player.left=.1;
						player.right=.9;
						player.top=1.75;
					}
					if(player.ability!=null && player.ability instanceof AirDodge) {
						player.status=NORMAL;
						player.ability=null;
					}
					if(player.status==HELPLESS)
						player.status=NORMAL;
				} else {
					player.groundJump=false;
					if(player.status==WALKING || player.status==DUCKING) {
						player.status=NORMAL;
						player.left=.1;
						player.right=.9;
						player.top=1.75;
					}
				}
				//****************KEY RESPONSE*******************
				//refresh when touching ground, tell the player where he is
				
				if(player.ability==null){
					if(act && player.status!=LADDER && player.status!=HELPLESS) {
						if(!player.walls[DOWN]) {
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
					} else if(move && player.status!=LADDER && player.status!=HELPLESS) {
						if(up) {
							player.ability=new ThirdJump(player);
						}
						move=false;
					} else {
						
						//basic movement
						if(left) {
							player.facingRight=false;
							if(player.status==DUCKING) {
								player.ax=-.04;
							} else if(player.status==LADDER) {
								player.ax=-.2;
								player.ay=.2;
								player.status=NORMAL;								
							} else if(player.walls[DOWN]) {
								player.ax=-.15;
								if(player.status==NORMAL)
									player.status=WALKING;
							} else if(player.status!=HELPLESS && player.walls[RIGHT] && (player.wallJumps==-1 || player.wallJumps==2)) {
								player.wallJumps=1;
								player.vy=.4;
								player.ax=-.1;
							} else if(!player.walls[DOWN])
								player.ax=-.02;
						} else if(right) {
							player.facingRight=true;
							if(player.status==DUCKING) {
								player.ax=.04;
							} else if(player.status==LADDER) {
								player.ax=.2;
								player.ay=.2;
								player.status=NORMAL;
							} else if(player.walls[DOWN]) {
								player.ax=.15;
								if(player.status==NORMAL)
									player.status=WALKING;
							} else if(player.status!=HELPLESS && player.walls[LEFT] && (player.wallJumps==1 || player.wallJumps==2)) {
								player.wallJumps=-1;
								player.vy=.4;
								player.ax=.1;
							} else if(!player.walls[DOWN])
								player.ax=.02;
						}
						
						if(up && player.status!=LADDER && !upDone) {
							if(player.walls[DOWN]) {
								if(player.groundJump) {
									player.vy=.4;
									player.groundJump=false;
								}
							} else {
								if(player.airJump) {
									player.vy=.4;
									player.airJump=false;
								}
							}
						}
						upDone=up;
						
						if(player.status==LADDER) {
							if(up)
								player.y+=.05;
							else if(down)
								player.y-=.05;
						} 
					}
				}
				
				//working with character moves
				if(player.ability!=null) {
					if(player.ability.started) {
						player.suspicion+=player.ability.duration;
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
				if(!player.walls[DOWN]) {		//air friction
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
								
				if(player.status!= LADDER && !(player.walls[UP] && player.vy>0) && !(player.walls[DOWN] && player.vy<=0)) {
					player.ay+=-gravity;
					player.vy+=player.ay*dt;
					player.y+=Math.signum(player.vy)*Math.min(.3,Math.abs(player.vy))*dt;
				} else {
					player.vy=0;
					player.ay=0;
				}
				
				player.vx+=player.ax*dt;
				//move with velocity (with speed limit)	
				if(player.status!= LADDER && !(player.walls[LEFT] && player.vx<0) && !(player.walls[RIGHT] && player.vx>0)) {
					player.x+=Math.signum(player.vx)*Math.min(.3,Math.abs(player.vx))*dt;
				} else {					
					player.vx=0;
					player.ax=0;
				}
				player.ax=0;
				player.ay=0;
				
				//recover suspicion
				if (time % 3 == 0)
					player.suspicion=Math.max(0,player.suspicion-1);
			
				try {				
					Thread.sleep(15L);
				} catch (InterruptedException e) {}
			}
		}		
		
		/**
		 * Determines on which sides the character has encountered a wall
		 */
		public void collideTest() {
			
			double left = Math.round(1e4 * (player.x + player.left))/10000.0;
			double right = Math.round(1e4 * (player.x + player.right))/10000.0;
			double top = Math.round(1e4 * (player.y + player.top))/10000.0;
			double bottom = Math.round(1e4 * (player.y + player.bottom))/10000.0;
			double leftFuture = Math.round(1e4 * (player.x + player.vx*dt + player.left))/10000.0;
			double rightFuture = Math.round(1e4 * (player.x + player.vx*dt + player.right))/10000.0;
			double bottomFuture = Math.round(1e4 * (player.y + player.vy*dt + player.bottom))/10000.0;
			double topFuture = Math.round(1e4 * (player.y + player.vy*dt + player.top))/10000.0;
			System.out.println(right + " " + bottom);
			player.vx = Math.round(1e4 * player.vx)/10000.0;
			player.vy = Math.round(1e4 * player.vy)/10000.0;
			Iterator<GameObject> itr = elements.iterator();
			//System.out.println(right + " " + player.vx);
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
				if(rightFuture > element.getX() && leftFuture < element.getX()+source.getWidth()
					&& right > element.getX() && left < element.getX()+source.getWidth()
					&& bottomFuture+.9 >= element.getY()+source.getHeight() && bottomFuture <= element.getY()+source.getHeight()) {
					player.walls[DOWN]=true;
					ytemp=element.getY()+source.getHeight()+player.bottom;
				}
				//ceiling detection
				if(rightFuture > element.getX() && leftFuture < element.getX()+source.getWidth()
					&& right > element.getX() && left < element.getX()+source.getWidth()
					&& topFuture-.9 <= element.getY() && topFuture >= element.getY()) {
					player.walls[UP]=true;
					ytemp=element.getY()-player.top;
				}
				//left wall detection
				if(topFuture > element.getY() && bottomFuture < element.getY()+source.getHeight()
					&& top > element.getY() && bottom < element.getY()+source.getHeight()
					&& rightFuture >= element.getX()+source.getWidth() && leftFuture <= element.getX()+source.getWidth()) {
					player.walls[LEFT]=true;
					xtemp=element.getX()+source.getWidth()-player.left;
				}
				//right wall detection				
				if(topFuture > element.getY() && bottomFuture < element.getY()+source.getHeight()
					&& top > element.getY() && bottom < element.getY()+source.getHeight()
					&& rightFuture >= element.getX() && leftFuture <= element.getX()) {
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
			if(player.walls[UP] && player.walls[DOWN]) {
				player.status=DUCKING;
				player.top=.9;
			}			
			
			//laser detection
			itr = lasers.iterator();
			while(itr.hasNext()) {
				GameObject element = itr.next();
				Element source = element.getSource();
				if(element.getSource().getName().indexOf("laserZ") >= 0) {
					//for lasers along the Z axis
					double centerx=(element.getX()+source.getWidth())/2.0;
					double centery=(element.getY()+source.getHeight())/2.0;
					if(centerx<=right && centerx>=left && centery>=bottom && centery<=top)
						player.suspicion+=Math.max(1,90*Math.hypot((top+bottom)/2.0,(left+right)/2.0));
				} else if(player.status!=INVINCIBLE) {
					//for lasers along the X or Y axis
					if(element.getRotation()%180==0) {
						if( //floor
							right > element.getX()+.43 && left < element.getX()+source.getWidth()-.43
							&& bottom+.9 >= element.getY()+source.getHeight() && bottom <= element.getY()+source.getHeight()
							||
							//ceiling
							right > element.getX()+.43 && left < element.getX()+source.getWidth()-.43
							&& top-.9 <= element.getY() && top >= element.getY()
							||
							//left
							top > element.getY() && bottom < element.getY()+source.getHeight()
							&& right >= element.getX()+source.getWidth()-.43 && left <= element.getX()+source.getWidth()-.43
							||
							//right
							top > element.getY() && bottom < element.getY()+source.getHeight()
							&& right >= element.getX()+.43 && left <= element.getX()+.43) {
							player.suspicion+=Math.max(1,Math.abs(90*player.vx));
						} 
					} else {
						if(//bottom
							right > element.getX() && left < element.getX()+source.getWidth()
							&& bottom+.9 >= element.getY()+source.getHeight()-.43 && bottom <= element.getY()+source.getHeight()-.43
							//ceiling
							||
							right > element.getX() && left < element.getX()+source.getWidth()
							&& top-.9 <= element.getY()+.43 && top >= element.getY()+.43
							//left
							||
							top > element.getY()+.43 && bottom < element.getY()+source.getHeight()-.43
							&& right >= element.getX()+source.getWidth() && left <= element.getX()+source.getWidth()
							||
							//right
							top > element.getY()+.43 && bottom < element.getY()+source.getHeight()-.43
							&& right >= element.getX() && left <= element.getX()) {
							player.suspicion+=Math.max(1,Math.abs(90*player.vy));
						} 
					}
				}
			} 
			
			//interactive element detection
			itr = interactives.iterator();
			boolean ladder=false;
			while(itr.hasNext()) {
				GameObject element = itr.next();
				Element source = element.getSource();
				if(left > element.getX()-.2 && right < element.getX()+source.getWidth()+.2
					&& top > element.getY() && bottom < element.getY()+source.getHeight()) {
					if(element.getSource().getName().indexOf("savepoint") >= 0 && player.ability instanceof Activate) // TODO: change this to use attributes
						System.out.println("You just tried to save");
					if(element.getSource().getName().indexOf("door") >= 0 && player.ability instanceof Activate) // TODO: change this to use attributes
						System.out.println("You just tried to win. But you cannot win. I LOST!");
					if(element.getSource().getName().indexOf("ladder") >= 0) { // TODO: change this to use attributes
						ladder=true;
						if(up||down) {
							System.out.println("You just tried to use a ladder");
							player.status = LADDER;
							player.x=element.getX();
							left=.1;
							right=.9;
							top=1.75;
							bottom=0;			
							player.groundJump=false;
							player.airJump=true;
						}
					}
				}										
			}
			if (!ladder && player.status==LADDER) {
				player.status = NORMAL;
			}
		}
	}
	
	public class GLGameListener implements GLEventListener, KeyListener {
		private FloatBuffer suspicion;

		private double player_x;
		private double player_y;
		
		public GLGameListener(Player you, Object synch) {
			player=you;
		}
		
		public void display(GLAutoDrawable drawable) {
			GL gl = drawable.getGL();   		
		 	gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		 	
		 	gl.glLoadIdentity();
		 	player_x = player.x;
		 	player_y = player.y;
		 	glu.gluLookAt(player_x, player_y, 10, player_x, player_y, -10, 0, 1, 0);

			renderScene(gl);
			drawCharacter(gl);
			drawSuspicion(gl,player.suspicion);
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
			Element.clearOptions(gl);
			gl.glDisable(GL.GL_DEPTH_TEST);
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
			if(player.status==WALKING) {
				player.walk[((int)(time*.1))%8].bind();
			} else if(!player.walls[DOWN]) {
				player.air.bind();
			} else if(player.status==DUCKING) {
				player.duck.bind();
			} else
				player.stand.bind();
			gl.glBegin(GL.GL_QUADS);
				if(player.status==DUCKING) {
					if(player.facingRight) {
						gl.glTexCoord2d(0,1);
						gl.glVertex3d(player_x,player_y,0);
						gl.glTexCoord2d(1,1);
						gl.glVertex3d(player_x+2,player_y,0);
						gl.glTexCoord2d(1,0);
						gl.glVertex3d(player_x+2,player_y+1,0);
						gl.glTexCoord2d(0,0);
						gl.glVertex3d(player_x,player_y+1,0);
					} else {
						gl.glTexCoord2d(1,1);
						gl.glVertex3d(player_x,player_y,0);
						gl.glTexCoord2d(0,1);
						gl.glVertex3d(player_x+2,player_y,0);
						gl.glTexCoord2d(0,0);
						gl.glVertex3d(player_x+2,player_y+1,0);
						gl.glTexCoord2d(1,0);
						gl.glVertex3d(player_x,player_y+1,0);
					}
				} else {
					if(player.facingRight) {
						gl.glTexCoord2d(0,1);
						gl.glVertex3d(player_x,player_y,0);
						gl.glTexCoord2d(1,1);
						gl.glVertex3d(player_x+1,player_y,0);
						gl.glTexCoord2d(1,0);
						gl.glVertex3d(player_x+1,player_y+2,0);
						gl.glTexCoord2d(0,0);
						gl.glVertex3d(player_x,player_y+2,0);
					} else {
						gl.glTexCoord2d(1,1);
						gl.glVertex3d(player_x,player_y,0);
						gl.glTexCoord2d(0,1);
						gl.glVertex3d(player_x+1,player_y,0);
						gl.glTexCoord2d(0,0);
						gl.glVertex3d(player_x+1,player_y+2,0);
						gl.glTexCoord2d(1,0);
						gl.glVertex3d(player_x,player_y+2,0);
					}
				}
			gl.glEnd();
			gl.glEnable(GL.GL_DEPTH_TEST);
			Element.setOptions(gl);
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
			gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 2 * ((2 * Math.min(360, suspicion) / 5) / 2) + 3);
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
			gl.glClearColor(0.2f,0.2f,0.2f,1.0f);
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
				player.stand = res.getTexture("gunther.png");
				player.walk[0]= res.getTexture("gunther.png");
				player.walk[1]= res.getTexture("guntherwalk1.png");
				player.walk[2]= res.getTexture("guntherwalk2.png");
				player.walk[3]= res.getTexture("guntherwalk3.png");
				player.walk[4]= res.getTexture("guntherwalk4.png");
				player.walk[5]= res.getTexture("guntherwalk5.png");
				player.walk[6]= res.getTexture("guntherwalk6.png");
				player.walk[7]= res.getTexture("guntherwalk7.png");
				player.air= res.getTexture("guntherair.png");
				player.duck = res.getTexture("guntherduck.png");
		 	} catch (Exception e) {
				System.out.println("no file");
			}
		}
		/**
		 * Creates a vertex array for the suspicion meter
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
			double ratio = (double)width / (double)height;
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glOrtho(-6*ratio,6*ratio,-6,6,-20,20);
			gl.glMatrixMode(GL.GL_MODELVIEW);
		}
		
		//**************KEY LISTENER********************
		public void keyPressed(KeyEvent e) {		
			if(e.getKeyCode()==KeyEvent.VK_LEFT) {
				left=true;
				right=false;
			} if(e.getKeyCode()==KeyEvent.VK_RIGHT) {
				right=true;
				left=false;
			} if(e.getKeyCode()==KeyEvent.VK_UP && player.status!=DUCKING) {
				up=true;
				down=false;
			} if(e.getKeyCode()==KeyEvent.VK_DOWN) {
				down=true;
				up=false;
			}
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
			if(e.getKeyChar()==movement)
				move=true;
			if(e.getKeyChar()==pause)
				paused^=true;
		}
		
	}
}