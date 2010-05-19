import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import java.nio.*;
import java.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.swing.*;

import com.sun.opengl.util.*;

public class GameWindow extends JPanel implements Constants {
	private static final long serialVersionUID = 0L;

	ResourceGetter res;
	GLGameListener listener;
	Level level;
	Block block;
	List<GameObject> elements;
	List<GameObject> lasers;
	List<GameObject> cameras;
	List<GameObject> interactives;
	
	boolean up;
	boolean upDone;
	boolean left;
	boolean right;
	boolean down;
	boolean act;
	boolean move;
	boolean sneak;
	boolean paused;
	boolean key;
	boolean canHide;
	volatile boolean KILL;
	volatile boolean killed;
	volatile boolean respawn;
	volatile boolean done;
	
	int width=0;
	int height=0;
	
	private long time=0;
	private long effectStart=0;
	private long effectEnd=0;
	private long stop=0;
	int fade=0;
	
	int action=KeyEvent.VK_A;
	int stealth=KeyEvent.VK_S;
	int movement = KeyEvent.VK_D;
	int pause=KeyEvent.VK_ESCAPE;
	
	Object sync= new Object();
	Object mSyn= new Object();
	Player player;
	Player savedPlayer;
	PhysicsThread physics;
	Animator anim;
	GLU glu;
	GLCapabilities glcaps;
	GLCanvas canvas;
	MusicThread music;
	JLabel load, keys, info;
	
	public GameWindow() {
		super(new BorderLayout());
		setBackground(Color.BLACK);
		KILL = killed = false;
		setupUI();
	}
	public GameWindow(MusicThread t) {
		this();
		music = t;
	}
	public void setupUI() {
		res = JarResources.PARENT;
		load = new JLabel(res.getIcon("genterprise.png"));
		load.setHorizontalAlignment(SwingConstants.CENTER);
		load.setVerticalAlignment(SwingConstants.CENTER);
		load.setForeground(Color.WHITE);
		load.setFont(load.getFont().deriveFont(24.f));
		keys = new JLabel(res.getIcon("keys.png"));
		keys.setText("ESC: Pause");
		keys.setHorizontalAlignment(SwingConstants.CENTER);
		keys.setVerticalAlignment(SwingConstants.CENTER);
		keys.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 0));
		keys.setForeground(Color.WHITE);
		keys.setFont(load.getFont());
		keys.setVerticalTextPosition(SwingConstants.TOP);
		keys.setHorizontalTextPosition(SwingConstants.CENTER);
		info = new JLabel(res.getIcon("info.png"));
		info.setHorizontalAlignment(SwingConstants.CENTER);
		info.setVerticalAlignment(SwingConstants.CENTER);
		info.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 0));
		glcaps = new GLCapabilities();
	}
	public void start(String level) {
		key = paused = done = KILL = killed = false;

		removeAll();
		add(load, BorderLayout.CENTER);
		add(keys, BorderLayout.SOUTH);
		add(info, BorderLayout.NORTH);
		validate();
		repaint();

		readLevel("../" + level);
		if (music != null) {
			music.load("aoogahorn.wav");
			music.load("ping.wav");
			music.load("intruderalert.wav");
			music.load("shutdown.wav");
		}

		player= new Player();
		savedPlayer = null;

		listener= new GLGameListener(player);
		canvas = new GLCanvas(glcaps);
		canvas.addKeyListener(listener);
		canvas.addGLEventListener(listener);   // add event listener
		load.setIcon(null);
		load.setText("Press SPACE to start");
		addKeyListener(listener);
		requestFocus();
		requestFocus();
		while (!key) Utils.sleep(50L);
		removeKeyListener(listener);
		load.setText("Starting!");

		time = stop = effectEnd = effectStart = 0;
		fade = 0;
		
		if (music != null)
			music.stopMusic();
		physics= new PhysicsThread();
		anim=new Animator(canvas);
		removeAll();
		add(canvas, BorderLayout.CENTER);
		validate();
		canvas.requestFocus();
		physics.start();
		anim.start();
	}
	public void readLevel(String levelName) {
		try {
			FilesystemResources res = new FilesystemResources(null, new java.io.File("res/"));
			LevelReader lreader = new LevelReader(res, levelName);
			level = lreader.getLevel();
		} catch (Exception e) {
			Errors.userError(levelName + " was not found. Try copy+paste to project folder.");
		}
		//identify level blocks and stores them in the correct list
		block = level.blockIterator().next();
		elements = new ArrayList<GameObject>(block.getElements().size());
		lasers = new ArrayList<GameObject>(block.getElements().size());
		cameras = new ArrayList<GameObject>(block.getElements().size());
		interactives = new ArrayList<GameObject>(block.getElements().size());
		Iterator<GameObject> itr = block.getElements().iterator();
		while(itr.hasNext()) {
			GameObject element = itr.next();
			if (element.getSource().getVertexArray() == null)
				element.getSource().loadGeometry(res);
			String attrib = element.getAttribute("motion", "");
			if (attrib != null && attrib.length() > 0) {
				StringBuilder newAttrib = new StringBuilder(attrib.length() * 2);
				for (int i = 0; i < attrib.length(); i++) {
					newAttrib.append(attrib.charAt(i));
					newAttrib.append(attrib.charAt(i));
				}
				element.putAttribute("motion", newAttrib.toString());
			}
			if(element.getZ() == 0.)
				elements.add(element);
			else if(element.getSpecialBit() > 3 && element.getSpecialBit() < 7)
				lasers.add(element);
			else if(element.getSpecialBit() == 7)
				cameras.add(element);
			else if(element.getSpecialBit() == 8) {
				String song = element.getAttribute("name", "");
				if (song != null && song.length() > 0)
					music.load(song);
				interactives.add(element);
			} else if(element.getSpecialBit() != 0)
				interactives.add(element);
		}
	}
	private void playSound(String name) {
		if (music != null && !music.isPlaying(name))
			music.startSFX(name);
	}
	private void playSoundtrack(String name) {
		if (music != null && !music.inQueue(name) && time % 10 == 0) {
			music.stopMusic();
			music.queueMusic(name);
		}
	}
	private void stopSound(String name) {
		if (music != null)
			music.stopSFX(name);
	}
	//***************************PHYSICS THREAD***********************
	public class PhysicsThread extends Thread {
		private double kair=.5;
		private double muk=6;
		private double gravity=.03;
		private long dt;
		
		public PhysicsThread() {
			super("Physics and Simulation");
			setPriority(Thread.MAX_PRIORITY);
			dt=1;
		}
		public void run() {
			double dx, dy;
			while(true) {
				while (paused || fade > 0) {
					Utils.sleep(1L);
					if (KILL || killed) return;
				}
				if (KILL || killed) return;
				time+=dt;
				
				synchronized (sync) {
					respawn = false;
					//player state determination
					if(player.status!=INVINCIBLE) {
						if(player.walls[DOWN] && player.vy<=0) {
							player.groundJump=true;
							player.airJump=true;
							player.wallJumps=2;
							if(down && player.ability==null && Math.abs(player.vy) < 1e-5) {
								player.status=DUCKING;
								player.top=.9;
								player.left=-.35;
								player.right=1.35;
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
					}
					//****************KEY RESPONSE*******************
					//refresh when touching ground, tell the player where (s)he is
					
					if(player.ability==null) {
						if(act && player.status!=LADDER && player.status!=HELPLESS) {
							//basic abilities
							if(!player.walls[DOWN]) {
								player.ability=new AirDodge(player);
							} else if(player.walls[DOWN]){
								if(down) {
									player.ability=new Dodge(player);
								} else if(up && player.status!=DUCKING) {
									player.ability=new Dive(player);
								} else if(left && player.status!=DUCKING) {
									player.ability=new Roll(player,LEFT);
								} else if(right && player.status!=DUCKING) {
									player.ability=new Roll(player,RIGHT);
								} else
									player.ability=new Activate(player);
							}	
							act=false;
						} else if(move && player.status!=LADDER && player.status!=HELPLESS) {
							//movement abilities
							if(up) {
								player.ability=new ThirdJump(player);
							} else if(right) {
								if(player.walls[DOWN])
									player.ability=new Dash(player,RIGHT);
							} else if(left) {
								if(player.walls[DOWN])
									player.ability=new Dash(player,LEFT);
							} else if(down) {
								if(!player.walls[DOWN])
									player.ability=new Glide(player);
							}
							move=false;
						} else if(sneak && player.status!=LADDER && player.status!=HELPLESS) {
							//stealth abilities
							if(left) {
								if(!player.walls[DOWN])
									player.ability=new MovingAirDodge(player,LEFT);
							} else if(right) {
								if(!player.walls[DOWN])
									player.ability=new MovingAirDodge(player,RIGHT);
							} else if(up)
								player.ability=new Scout(player);
							else if ((!up && !down && !left && !right) &&
									player.walls[DOWN] && canHide)
								player.ability=new Hide(player);
							sneak=false;
						}
					}
					if(player.ability==null || player.ability instanceof Glide) {
						//basic movement
						if(left) {
							player.facingRight=false;
							if(player.status==DUCKING) {
								player.ax=-.04;
							} else if(player.status==LADDER) {
								player.ax=-.1;
								player.ay=.1;
								player.status=NORMAL;								
							} else if(player.walls[DOWN]) {
								player.ax=-.15;
								if(player.status==NORMAL)
									player.status=WALKING;
							} else if(player.status!=HELPLESS && player.walls[RIGHT] &&
									(player.wallJumps==-1 || player.wallJumps==2)) {
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
								player.ax=.1;
								player.ay=.1;
								player.status=NORMAL;
							} else if(player.walls[DOWN]) {
								player.ax=.15;
								if(player.status==NORMAL)
									player.status=WALKING;
							} else if(player.status!=HELPLESS && player.walls[LEFT] &&
									(player.wallJumps==1 || player.wallJumps==2)) {
								player.wallJumps=-1;
								player.vy=.4;
								player.ax=.1;
							} else if(!player.walls[DOWN])
								player.ax=.02;
						}
						
						if(up && player.status != HELPLESS && player.status != LADDER && !upDone) {
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
					if(player.status == WALKING && player.ax < 0 && player.walls[LEFT])
						player.status = NORMAL;
					if(player.status == WALKING && player.ax > 0 && player.walls[RIGHT])
						player.status = NORMAL;
				}
				
				//working with character moves
				if(player.ability!=null) {
					if(player.ability.started) {
						if (player.ability.causesSuspicion())
							player.suspicion+=player.ability.duration/4;
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
					
					if(time>=effectStart && time<=effectEnd)
						player.ability.continuous(GameWindow.this);
					
					//end move effect at its end frame
					if(time==effectEnd) {
						player.ability.endEffect();
					}
					
					if(time==stop) {
						player.ability.linger();
						player.ability=null;
					}
				}
				collideTest();
				
				//acceleration calculations
				if(!player.walls[DOWN]) {		//air friction
					if(player.vx>0)
						player.ax+= Math.max(-player.vx/dt,-kair*player.vx/player.m);
					else if(player.vx<0)
						player.ax+= Math.min(-player.vx/dt,-kair*player.vx/player.m);
				} else {		//ground friction
					if(player.vx>0)
						player.ax+= Math.max(-player.vx/dt,-muk*gravity);
					else if(player.vx<0)
						player.ax+= Math.min(-player.vx/dt,muk*gravity);
				}

				if(player.status!= LADDER && !(player.walls[UP] && player.vy>0) &&
					!(player.walls[DOWN] && player.vy<=0)) {
					player.ay+=-gravity;
					player.vy+=player.ay*dt;
					dy = Math.min(.3,Math.max(-.22, player.vy))*dt;
					player.y+=dy;
				} else {
					player.vy=0;
					player.ay=0;
				}
				
				player.vx+=player.ax*dt;
				//move with velocity (with speed limit)
				if(player.status!= LADDER && !(player.walls[LEFT] && player.vx<0) &&
					!(player.walls[RIGHT] && player.vx>0)) {
					dx = Math.signum(player.vx)*Math.min(.3,Math.abs(player.vx))*dt;
					player.x+=dx;
				} else {
					player.vx=0;
					player.ax=0;
				}

				player.ax=0;
				player.ay=0;

				synchronized (mSyn) {
					moveObjects();
				}
				//recover suspicion

				if (time % 3 == 0) checkSounds();
				if (player.suspicion > 360 && time % 5 == 0)
					player.suspicion = Math.min(720, player.suspicion+(player.suspicion-270)/90);
				// catch the player?
				if (player.suspicion >= 720) {
					playSound("shutdown.wav");
					reset();
					respawn = true;
					fade = 60;
				}

				try {				
					Thread.sleep(15L);
				} catch (InterruptedException e) {}
			}
		}
		/**
		 * Plays appropriate sound effects.
		 */
		public void checkSounds() {
			if (player.suspicion >= 360) playSound("aoogahorn.wav");
			else stopSound("aoogahorn.wav");
		}
		/**
		 * Returns the current movement character for the specified element.
		 * 
		 * @param element the element to look at
		 * @param inc whether the pointer should seek ahead
		 */
		public char getMovement(GameObject element, boolean inc) {
			String attrib = element.getAttribute("motion");
			char ret = 'W';
			if (attrib != null && attrib.length() > 0) {
				int current = element.getIteration();
				if (current < 0 || current >= attrib.length()) current = 0;
				ret = attrib.charAt(current);
				if (inc) {
					current = (current + 1) % attrib.length();
					element.setIteration(current);
				}
			}
			return ret;
		}
		/**
		 * Moves all movable objects.
		 */
		public void moveObjects() {
			Iterator<GameObject> itr = block.getElements().iterator();
			while(itr.hasNext()) {
				GameObject element = itr.next();
				char dir = getMovement(element, true);
				double nx, ny, nr;
				switch (dir) {
				case 'l':
				case 'L':
					nx = element.getX() - INC;
					nx = Math.round(1e4 * nx) / 10000.0;
					element.getLocation().setX(nx);
					break;
				case 'r':
				case 'R':
					nx = element.getX() + INC;
					nx = Math.round(1e4 * nx) / 10000.0;
					element.getLocation().setX(nx);
					break;
				case 'u':
				case 'U':
					ny = element.getY() + INC;
					ny = Math.round(1e4 * ny) / 10000.0;
					element.getLocation().setY(ny);
					break;
				case 'd':
				case 'D':
					ny = element.getY() - INC;
					ny = Math.round(1e4 * ny) / 10000.0;
					element.getLocation().setY(ny);
					break;
				case '<':
					nr = element.getRotation() - 2.5;
					nr = Math.round(1e4 * nr) / 10000.0;
					element.getRotFlip().setZ(nr);
					break;
				case '>':
					nr = element.getRotation() + 2.5;
					nr = Math.round(1e4 * nr) / 10000.0;
					element.getRotFlip().setZ(nr);
					break;
				case 'w':
				case 'W':
				default:
				}
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
			player.vx = Math.round(1e4 * player.vx)/10000.0;
			player.vy = Math.round(1e4 * player.vy)/10000.0;
			Iterator<GameObject> itr = elements.iterator();
			boolean wallUp=false,wallDown=false,wallLeft=false,wallRight=false,guideX=false,guideY=false;
			double xtemp = player.x, dx = 0;
			double ytemp = player.y, dy = 0;
			while(itr.hasNext()) {
				GameObject element = itr.next();
				Element source = element.getSource();
				//floor detection
				if(rightFuture > element.getX() && leftFuture < element.getX()+source.getWidth()
					&& right > element.getX() && left < element.getX()+source.getWidth()
					&& bottomFuture+.9 >= element.getY()+source.getHeight() && bottomFuture <=
					element.getY()+source.getHeight()) {
					wallDown=true;
					ytemp=element.getY()+source.getHeight()+player.bottom;
					
					//this section keeps the player still on a moving platform
					char dir = getMovement(element, false);
					switch (dir) {
					case 'L':
					case 'l':
						if (!guideX) {
							guideX = true;
							dx -= INC;
						}
						break;
					case 'R':
					case 'r':
						if (!guideX) {
							guideX = true;
							dx += INC;
						}
						break;
					case 'U':
					case 'u':
						if (!guideY) {
							guideY = true;
							dy += INC;
						}
						break;
					case 'D':
					case 'd':
						if (!guideY) {
							guideY = true;
							dy -= INC;
						}
						break;
					case '>':
					case '<':
						System.out.println("Oh no! Player standing on rotating block!");
						break;
					case 'W':
					case 'w':
					default:
					}
				}
				//ceiling detection
				if(rightFuture > element.getX() && leftFuture < element.getX()+source.getWidth()
					&& right > element.getX() && left < element.getX()+source.getWidth()
					&& topFuture-.9 <= element.getY() && topFuture >= element.getY()) {
					wallUp=true;
					ytemp=element.getY()-player.top;
				}
				//left wall detection
				if(topFuture > element.getY() && bottomFuture < element.getY()+source.getHeight()
					&& top > element.getY() && bottom < element.getY()+source.getHeight()
					&& rightFuture >= element.getX()+source.getWidth() && leftFuture <=
					element.getX()+source.getWidth()) {
					wallLeft=true;
					xtemp=element.getX()+source.getWidth()-player.left;
				}
				//right wall detection				
				if(topFuture > element.getY() && bottomFuture < element.getY()+source.getHeight()
					&& top > element.getY() && bottom < element.getY()+source.getHeight()
					&& rightFuture >= element.getX() && leftFuture <= element.getX()) {
					wallRight=true;
					xtemp=element.getX()+(1-player.right)-1;
				}
			}
			xtemp += dx;
			ytemp += dy;
			player.guideX = Math.round(1e4 * dx)/10000.0;
			player.guideY = Math.round(1e4 * dy)/10000.0;
			xtemp = Math.round(1e4 * xtemp)/10000.0;
			ytemp = Math.round(1e4 * ytemp)/10000.0;
			//corrects collisions by moving the player instantly
			if (wallLeft != wallRight || guideX) {
				player.x=xtemp;
				// anti earthquake?
				player.vx=Math.signum(player.vx)*1e-3;
			} if(wallUp != wallDown || guideY) {
				player.y=ytemp;
				// anti earthquake?
				player.vy=Math.signum(player.vy)*1e-3;
			} if(wallUp && wallDown) {
				player.status = DUCKING;
				player.top=.9;
				player.left=-.3;
				player.right=1.4;
			}

			boolean busted = false;
			/**
			 * laser detection
			 */
			itr = lasers.iterator();
			while(itr.hasNext()) {
				GameObject element = itr.next();
				Element source = element.getSource();
				if (element.getSpecialBit() == 5) {
					//for lasers along the Z axis
					double centerx=(element.getX()+source.getWidth()/2.0);
					double centery=(element.getY()+source.getHeight()/2.0);
					if(centerx<=right && centerx>=left && centery>=bottom && centery<=top) {
						player.suspicion += Math.min(Math.max(4, 90*Math.hypot(player.vx,
							player.vy)), 24);
						busted = true;
					}
				} else {
					//for lasers along the X or Y axis
					if (element.getRotation() % 180 == 0) {
						if( //floor
							right > element.getX()+.43 && left < element.getX()+source.getWidth()-.43
							&& bottom+.9 >= element.getY()+source.getHeight() && bottom <=
							element.getY()+source.getHeight()
							||
							//ceiling
							right > element.getX()+.43 && left < element.getX()+source.getWidth()-.43
							&& top-.9 <= element.getY() && top >= element.getY()
							||
							//left
							top > element.getY() && bottom < element.getY()+source.getHeight()
							&& right >= element.getX()+source.getWidth()-.43 && left <=
							element.getX()+source.getWidth()-.43
							||
							//right
							top > element.getY() && bottom < element.getY()+source.getHeight()
							&& right >= element.getX()+.43 && left <= element.getX()+.43) {
							if (element.getSpecialBit() == 6) {
								fade = 60;
								reset();
								respawn = true;
								playSound("shutdown.wav");
							} else if (player.status != INVINCIBLE) {
								player.suspicion += Math.min(Math.max(3,Math.abs(90*player.vx)),24);
								busted = true;
							}
						}
					} else {
						if(//bottom
							right > element.getX() && left < element.getX()+source.getWidth()
							&& bottom+.9 >= element.getY()+source.getHeight()-.43 && bottom <=
							element.getY()+source.getHeight()-.43
							//ceiling
							||
							right > element.getX() && left < element.getX()+source.getWidth()
							&& top-.9 <= element.getY()+.43 && top >= element.getY()+.43
							//left
							||
							top > element.getY()+.43 && bottom < element.getY()+source.getHeight()-.43
							&& right >= element.getX()+source.getWidth() && left <=
							element.getX()+source.getWidth()
							||
							//right
							top > element.getY()+.43 && bottom < element.getY()+source.getHeight()-.43
							&& right >= element.getX() && left <= element.getX()) {
							if (element.getSpecialBit() == 6) {
								fade = 60;
								reset();
								respawn = true;
								playSound("shutdown.wav");
							} else if (player.status != INVINCIBLE) {
								busted = true;
								player.suspicion += Math.min(Math.max(3,Math.abs(90*player.vy)),24);
							}
						}
					}
				}
			}
			/**
			 * camera detection
			 */
			itr = cameras.iterator();
			while(itr.hasNext()) {
				GameObject element = itr.next();
				Element source = element.getSource();
				if(Math.hypot((left+right)/2.0-(element.getX()+source.getWidth()/2.0),
					(bottom+top)/2.0-(element.getY()+source.getHeight()/2.0))<2
					&& Math.abs(180.0/Math.PI*Math.atan2((bottom+top)/2.0-(element.getY()+source.getHeight()/2.0),
					(left+right)/2.0-(element.getX()+source.getWidth()/2.0))-(element.getRotation()-90))<=30) {
					player.suspicion += Math.min(Math.max(3,110*Math.hypot(player.vx,player.vy)),24);
					busted = true;
				}
			}
			if (busted)
				playSound("intruderalert.wav");
			
			/**
			 * interactive element detection
			 */
			boolean ladder=false;
			canHide = false;
			if(player.ability==null || player.ability instanceof Activate) {
				itr = interactives.iterator();
				while(itr.hasNext()) {
					GameObject element = itr.next();
					Element source = element.getSource();
					if(left > element.getX()-.2 && right < element.getX()+source.getWidth()+.2
						&& top > element.getY() && bottom < element.getY()+source.getHeight()) {
						if(element.getSpecialBit() == 3 && player.ability instanceof Activate) {
							playSound("ping.wav");
							save();
						} else if (element.getSpecialBit() == 2 && player.ability instanceof Activate) {
							String myName = element.getAttribute("name", "");
							if (myName.equalsIgnoreCase("end")) {
								//saving game
								playSound("ping.wav");
								fade = 60;
								savedPlayer = null;
								reset();
								done = true;
								paused = true;
								KILL = true;
							} else {
								//using a door
								String target = element.getAttribute("target", myName + "exit");
								Iterator<GameObject> it = interactives.iterator();
								GameObject o, dest = null;
								while (it.hasNext()) {
									o = it.next();
									if (o.getAttribute("name", "").equalsIgnoreCase(target))
										dest = o;
								}
								if (dest == null)
									System.out.println("You just tried to win. But you cannot win.");
								else {
									fade = 60;
									playSound("ping.wav");
									int suspicion = player.suspicion;
									reset();
									player.suspicion = suspicion;
									player.x = dest.getX();
									player.y = dest.getY();
								}
							}
						} else if (element.getSpecialBit() == 1) {
							//ladders
							ladder=true;
							if(up||down) {
								player.status = LADDER;
								player.x=element.getX();
								player.left = .1;
								player.right = .9;
								player.top = 1.75;
								player.bottom = 0;			
								player.groundJump=false;
								player.airJump=true;
							}
						} else if (element.getSpecialBit() == 10)
							canHide = true;
					}
					if (element.getSpecialBit() == 8) {
						String music = element.getAttribute("name");
						double ratio = (double)width / (double)height;
						if (music != null && music.length() > 1 && element.getX() > player.x - 6*ratio &&
								element.getX() + source.getWidth() < player.x + 6*ratio &&
								element.getY() > player.y - 6 && element.getY() + source.getHeight() < player.y + 6) {
							playSoundtrack(music);
						}
					}
				}
				
				if (!ladder && player.status==LADDER) {
					if(player.facingRight)
						player.ax+=.1;
					else
						player.ax-=.1;
					player.ay=.3;
					player.status = NORMAL;
				}
			}
			if (fade <= 0) {
				player.walls[UP]=wallUp;
				player.walls[DOWN]=wallDown;
				player.walls[LEFT]=wallLeft;
				player.walls[RIGHT]=wallRight;
			}
			
		}
	}
	/**
	 * Reload helper.
	 */
	public void clear() {
		try {
			anim.stop();
		} catch (Exception e) { }
		requestFocus();
		removeAll();
		validate();
	}
	/**
	 * Resets the player and all objects to defaults.
	 */
	public void reset() {
		player.reset();
		act = move = sneak = false;
		// will fail until positions are also reset
		/*Iterator<GameObject> it = block.getElements().iterator();
		while (it.hasNext()) {
			GameObject o = it.next();
			o.setIteration(0);
		}*/
	}
	/**
	 * Saves the game (check point).
	 */
	public void save() {
		if (savedPlayer == null)
			savedPlayer = new Player();
		//savedPlayer.suspicion = player.suspicion;
		savedPlayer.suspicion = 0;
		savedPlayer.x = player.x;
		savedPlayer.y = player.y;
		savedPlayer.vx = player.vx;
		savedPlayer.vy = player.vy;
		savedPlayer.status = player.status;
		savedPlayer.airJump = player.airJump;
		savedPlayer.facingRight = player.facingRight;
		savedPlayer.wallJumps = player.wallJumps;
		savedPlayer.groundJump = player.groundJump;
	}
	/**
	 * Loads the game from save.
	 */
	public void load() {
		reset();
		if (savedPlayer == null) return;
		player.suspicion = savedPlayer.suspicion;
		player.x = savedPlayer.x;
		player.y = savedPlayer.y;
		player.vx = savedPlayer.vx;
		player.vy = savedPlayer.vy;
		player.status = savedPlayer.status;
		player.airJump = savedPlayer.airJump;
		player.facingRight = savedPlayer.facingRight;
		player.wallJumps = savedPlayer.wallJumps;
		player.groundJump = savedPlayer.groundJump;
		player.guideX = player.guideY = 0.0;
	}

	public class GLGameListener implements GLEventListener, KeyListener {
		private FloatBuffer suspicion;

		private double player_x;
		private double player_y;
		
		public GLGameListener(Player you) {
			player=you;
		}
		
		public void display(GLAutoDrawable drawable) {
			if (killed) return;
			GL gl = drawable.getGL();
			if (KILL) {
				Iterator<GameObject> itr = block.getElements().iterator();
				while(itr.hasNext()) {
					GameObject element = itr.next();
					element.getSource().releaseGeometry();
					element.getSource().releaseTexture();
				}
				killed = true;
				return;
			}
			if (fade > 0) {
				Element.clearOptions(gl);
				gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
				gl.glLoadIdentity();
				gl.glColor4f(0.0f, 0.0f, 0.0f, 0.05f);
				gl.glBegin(GL.GL_QUADS);
				gl.glVertex3d(-30, -30, 0);
				gl.glVertex3d(-30, 30, 0);
				gl.glVertex3d(30, 30, 0);
				gl.glVertex3d(30, -30, 0);
				gl.glEnd();
				fade--;
				if (fade == 0 && respawn) load();
				Element.setOptions(gl);
			} else {
			 	gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
			 	gl.glLoadIdentity();
			 	synchronized (mSyn) {
				 	player_x = player.x;
				 	player_y = player.y;
				 	if(player.ability!=null && player.ability instanceof Scout)
					 	glu.gluLookAt(player.scoutx, player.scouty, 10, player.scoutx, player.scouty, -10, 0, 1, 0);
				 	else
					 	glu.gluLookAt(player_x, player_y, 10, player_x, player_y, -10, 0, 1, 0);
					renderScene(gl);
				}
				drawCharacter(gl);
				drawSuspicion(gl,player.suspicion);
			}
			Utils.sleep(2L);
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
		 * Draw the character onto the screen.
		 * @param gl
		 */
		public void drawCharacter(GL gl) {
			//drawing the player
			//colors will be obsolete when rendering improves
			Element.clearOptions(gl);
			gl.glDisable(GL.GL_DEPTH_TEST);
			synchronized (sync) {
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
				else if(player.ability instanceof Scout)
					gl.glColor3f(0f,0f,0f);
				else if(player.ability instanceof Hide)
					gl.glColor4f(1f,1f,1f,0.3f);
				else if(player.ability instanceof Glide)
					gl.glColor3f(0f,1f,1f);
				else if(player.ability instanceof MovingAirDodge)
					gl.glColor3f(1f,.1f,1f);
				else
					gl.glColor3f(1f,1f,1f);
				if(player.status==WALKING) {
					player.walk[((int)(Math.abs(player.x)))%8].bind();
				} else if(player.status==LADDER) {
					player.ladder[((int)Math.abs(player.y))%2].bind();
				} else if(player.status==DUCKING) {
					if(player.vx!=0 && (left || right)) {
						player.crawl[((int)(Math.abs(player.x)))%4].bind();
					} else
						player.duck.bind(); 
				} else if(!player.walls[DOWN]) {
					player.air.bind();
				}else
					player.stand.bind();
				gl.glBegin(GL.GL_QUADS);
				if(player.status==DUCKING) {
					if(player.facingRight) {
						gl.glTexCoord2d(0,1);
						gl.glVertex3d(player_x-.25,player_y,0);
						gl.glTexCoord2d(1,1);
						gl.glVertex3d(player_x+1.75,player_y,0);
						gl.glTexCoord2d(1,0);
						gl.glVertex3d(player_x+1.75,player_y+1,0);
						gl.glTexCoord2d(0,0);
						gl.glVertex3d(player_x-.25,player_y+1,0);
					} else {
						gl.glTexCoord2d(1,1);
						gl.glVertex3d(player_x-.75,player_y,0);
						gl.glTexCoord2d(0,1);
						gl.glVertex3d(player_x+1.25,player_y,0);
						gl.glTexCoord2d(0,0);
						gl.glVertex3d(player_x+1.25,player_y+1,0);
						gl.glTexCoord2d(1,0);
						gl.glVertex3d(player_x-.75,player_y+1,0);
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
			}
			gl.glEnd();
			gl.glEnable(GL.GL_DEPTH_TEST);
			gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
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
			gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 2 * (Math.min(360, suspicion) / 5) + 3);
			if (suspicion > 360) {
				gl.glColor4f(0f, 0f, 0.7f, 0.3f);
				gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 2 * (Math.min(360, suspicion - 360) / 5) + 3);
			}
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
				// emergency check
				if (el.getVertexArray() == null)
					el.loadGeometry(res);
				if (!el.hasTexture())
					el.loadTexture(res);
			}
		 	suspicion();
		 	Element.setOptions(gl);
		 	
		 	//player texture setting
			player.stand = res.getTexture("gunther.png");
			player.walk[0]= res.getTexture("gunther.png");
			player.walk[1]= res.getTexture("guntherwalk1.png");
			player.walk[2]= res.getTexture("guntherwalk2.png");
			player.walk[3]= res.getTexture("guntherwalk3.png");
			player.walk[4]= res.getTexture("guntherwalk4.png");
			player.walk[5]= res.getTexture("guntherwalk5.png");
			player.walk[6]= res.getTexture("guntherwalk6.png");
			player.walk[7]= res.getTexture("guntherwalk7.png");
			player.ladder[0]= res.getTexture("guntherladder1.png");
			player.ladder[1]= res.getTexture("guntherladder2.png");
			player.air= res.getTexture("guntherair.png");
			player.duck = res.getTexture("guntherduck.png");
			player.crawl[0]= res.getTexture("guntherduck.png");
			player.crawl[1]= res.getTexture("gunthercrawl1.png");
			player.crawl[2]= res.getTexture("gunthercrawl2.png");
			player.crawl[3]= res.getTexture("gunthercrawl3.png");
			
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
			if (paused) return;
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
			if(e.getKeyCode()==action)
				act=true;
			if(e.getKeyCode()==movement)
				move=true;
			if(e.getKeyCode()==stealth)
				sneak=true;
		}
		public void keyReleased(KeyEvent e) {
			if(e.getKeyCode()==pause)
				paused^=true;
			if (!key && e.getKeyCode()==KeyEvent.VK_SPACE) key = true;
			if (paused) return;
			if(e.getKeyCode()==KeyEvent.VK_LEFT)
				left=false;
			if(e.getKeyCode()==KeyEvent.VK_RIGHT)
				right=false;
			if(e.getKeyCode()==KeyEvent.VK_UP)
				up=false;
			if(e.getKeyCode()==KeyEvent.VK_DOWN)
				down=false;
			if(e.getKeyCode()==stealth && player.ability!=null && !player.ability.causesSuspicion()) {
				effectEnd=time;
				stop=time+player.ability.duration-player.ability.end;
				sneak=false;
			}
			if(e.getKeyCode()==movement && player.ability!=null && !player.ability.causesSuspicion()) {
				effectEnd=time;
				stop=time+player.ability.duration-player.ability.end;
				move=false;
			}
		}
		public void keyTyped(KeyEvent e) { }
	}
}