import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.sun.opengl.util.*;
import java.util.*;
import java.nio.*;

/**
 * The GEnterprise Level Editor.
 * 
 * @author Stephen
 */
public class EditorUI extends JFrame implements GLEventListener {
	private static final long serialVersionUID = 0L;

	/**
	 * The width in grid units (HALF of it!)
	 */
	public static final int GW = 7;
	/**
	 * The height in grid units (HALF of it!)
	 */
	public static final int GH = 7;
	/**
	 * Z coordinate for blocks.
	 */
	public static final double Z = 1.0;
	/**
	 * Place the element.
	 */
	public static final int PLACE = 2;
	/**
	 * Place the element.
	 */
	public static final int PLACE_IFNOT = 3;
	/**
	 * Place the element.
	 */
	public static final int HIT_TEST = 4;
	/**
	 * Re-render the dropping element.
	 */
	public static final int RENDER = 1;
	/**
	 * The size of placable block buttons.
	 */
	public static final Dimension BLOCK_SIZE = new Dimension(84, 56);
	/**
	 * The rectangle of 0,0 size at the origin.
	 */
	public static final Rectangle ZEROZERO = new Rectangle(0, 0, 0, 0);
	/**
	 * The button border.
	 */
	public static final javax.swing.border.Border BORDER = BorderFactory.createCompoundBorder(
		BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(3, 3, 3, 3));

	/**
	 * Called when the program starts.
	 */
	public static void main(String[] args) {
		Utils.staticInit();
		new EditorUI().start();
	}

	/**
	 * The GL canvas to display the level.
	 */
	private GLCanvas canvas;
	/**
	 * GL Utilities.
	 */
	private GLU glu;
	/**
	 * The animator for the main screen.
	 */
	private Animator anim;
	/**
	 * The available elements.
	 */
	private JComponent available;
	/**
	 * Scroll pane for the block list.
	 */
	private JScrollPane pane;
	/**
	 * The event listener.
	 */
	private EventListener events;
	/**
	 * Whether the animator is running.
	 */
	private volatile boolean running;
	/**
	 * The synchronizer for window events.
	 */
	private Object winSync;
	/**
	 * The synchronizer for GL events.
	 */
	private Object eventSync;
	/**
	 * The current resource list.
	 */
	private ResourceGetter current;
	/**
	 * The elements that are placable.
	 */
	private Map<String, Element> elements;
	/**
	 * The one-stop shop for icons.
	 */
	private IconBuilder icons;
	/**
	 * The element to drop.
	 */
	private Element dropping;
	/**
	 * The coordinates of the item to be dropped.
	 */
	private Point3 coords;
	/**
	 * Last place location.
	 */
	private Point3 lastPlace;
	/**
	 * The mouse x.
	 */
	private int x;
	/**
	 * The mouse y.
	 */
	private int y;
	/**
	 * The GL event.
	 */
	private volatile int event;
	/**
	 * Whether snap to grid is on.
	 */
	private volatile boolean snapTo;
	/**
	 * Whether the grid is visible.
	 */
	private JCheckBox grid;
	/**
	 * Matrices for the modelview, projection, and positions. Also the viewport.
	 */
	private double[] modelview, pos, projection;
	private int[] viewport;
	/**
	 * A block for testing.
	 */
	private Block block;
	/**
	 * The center of the screen.
	 */
	private Point3 center;
	/**
	 * The velocity with which the screen is moving.
	 */
	private Vector3 vel;
	/**
	 * A suspicion meter (TESTING).
	 */
	private FloatBuffer suspicion;
	/**
	 * The current zoom level.
	 */
	private Dimension zoom;
	/**
	 * The aspect ratio.
	 */
	private double ratio;
	/**
	 * Where you are.
	 */
	private JLabel location;
	/**
	 * The rendering mode.
	 */
	private int mode;
	/**
	 * The hit test buffer.
	 */
	private IntBuffer hitBuffer;
	/**
	 * The selected object.
	 */
	private GameObject selected;

	/**
	 * Sets the window title.
	 */
	public EditorUI() {
		super("Gunther's Enterprise Level Editor");
		winSync = new Object();
		eventSync = new Object();
		elements = new TreeMap<String, Element>();
		pos = new double[4];
		modelview = new double[16];
		projection = new double[16];
		viewport = new int[4];
		block = new Block(new LinkedList<GameObject>());
		snapTo = true;
		lastPlace = coords = null;
		center = new Point3(0.0, 0.0, 0.0);
		vel = new Vector3(0.0, 0.0, 0.0);
		zoom = new Dimension(GW, GH);
		mode = GL.GL_RENDER;
		selected = null;
	}
	/**
	 * Invoked to start the level editor.
	 */
	public void start() {
		current = new FilesystemResources(null, new java.io.File("res/"));
		icons = new IconBuilder(current);
		dropping = null;
		coords = null;
		// TODO temp
		addElement(new Element("checkerboard.png", "1x1square.dat", "checkerboard"));
		addElement(new Element("grass.png", "1x1square.dat", "grass"));
		addElement(new Element("AngleBlock.png", "1x1square.dat", "ramp"));
		addElement(new Element("BottomBlock.png", "1x1square.dat", "bottom"));
		addElement(new Element("Ceiling.png", "1x1square.dat", "ceiling"));
		addElement(new Element("Ladder.png", "1x1square.dat", "ladder"));
		addElement(new Element("Wall.png", "1x1square.dat", "wall"));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBackground(Color.WHITE);
		setResizable(true);
		setSize(1024, 768);
		getContentPane().setLayout(new BorderLayout());
		setupGL();
		setupUI();
		loadBlocks();
		addWindowFocusListener(events);
		addWindowListener(events);
		Utils.centerWindow(this);
		validate();
		setVisible(true);
		center();
		startRun();
	}
	/**
	 * Adds the element to the list. Does NOT change the screen.
	 * 
	 * @param element the new element
	 */
	private void addElement(Element element) {
		elements.put(element.getName(), element);
	}
	/**
	 * Sets up the UI (JOGL/AWT)
	 */
	private void setupGL() {
		GLCapabilities glcaps = new GLCapabilities();
		glcaps.setSampleBuffers(false);
		canvas = new GLCanvas(glcaps);
		canvas.addGLEventListener(this);
		getContentPane().add(canvas, BorderLayout.CENTER);
		anim = new Animator(canvas);
	}
	/**
	 * Sets up the UI (swing/JFC)
	 */
	private void setupUI() {
		events = new EventListener();
		available = new JPanel(new GridLayout(2, 0, 10, 10));
		available.setOpaque(false);
		JPanel enclosing = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		enclosing.setOpaque(false);
		enclosing.add(available);
		pane = new JScrollPane(enclosing);
		pane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		pane.setOpaque(false);
		pane.getViewport().setOpaque(false);
		pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		JButton cancel = new JButton("Done");
		cancel.setFocusable(false);
		cancel.setActionCommand("done");
		cancel.addActionListener(events);
		JComponent horiz = new Box(BoxLayout.X_AXIS);
		horiz.add(cancel);
		horiz.add(pane);
		getContentPane().add(horiz, BorderLayout.NORTH);
		canvas.addMouseListener(events);
		canvas.addMouseMotionListener(events);
		canvas.addKeyListener(events);
		grid = new JCheckBox("Show Grid");
		grid.setSelected(true);
		grid.setFocusable(false);
		location = new JLabel("At ");
		location.setFont(location.getFont().deriveFont(10.f));
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
		bottom.setOpaque(false);
		bottom.add(grid);
		bottom.add(location);
		getContentPane().add(bottom, BorderLayout.SOUTH);
	}
	/**
	 * Loads the blocks.
	 */
	private void loadBlocks() {
		available.removeAll();
		Iterator<Element> it = elements.values().iterator();
		Element element;
		while (it.hasNext()) {
			element = it.next();
			element.loadGeometry(current);
			available.add(new PlacableBlock(element));
		}
		if (elements.size() % 2 > 0)
			available.add(Box.createGlue());
		available.validate();
		available.repaint();
		available.scrollRectToVisible(ZEROZERO);
	}
	/**
	 * Drops the block at the current location.
	 */
	private void drop() {
		if (dropping == null || coords == null) return;
		if (event == PLACE_IFNOT) {
			if (lastPlace != null) {
				if (Math.floor(lastPlace.x) == Math.floor(coords.x) &&
					Math.floor(lastPlace.y) == Math.floor(coords.y)) return;
			}
		}
		GameObject newObject = new GameObject(coords.x, coords.y, dropping);
		synchronized (block) {
			block.getElements().add(newObject);
			if (event == PLACE_IFNOT) lastPlace = new Point3(coords.x, coords.y, coords.z);
		}
	}
	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
		if (dropping != null && !dropping.hasTexture())
			dropping.loadTexture(current);
		synchronized (eventSync) {
			switch (event) {
			case RENDER:
				computeLocation(gl);
				break;
			case PLACE_IFNOT:
				computeLocation(gl);
			case PLACE:
				drop();
				break;
			case HIT_TEST:
				selected = doHitTest(gl);
				break;
			default:
			}
			event = 0;
		}
		updatePosition(gl);
		if (dropping != null && coords != null) {
			gl.glPushMatrix();
			gl.glTranslated(coords.getX(), coords.getY(), coords.getZ());
			dropping.render(gl);
			gl.glPopMatrix();
		}
		renderScene(gl);
		drawSuspicion(gl, 240);
		grid(gl);
		gl.glFlush();
		Utils.sleep(30L);
	}
	/**
	 * Draws the entire level.
	 * 
	 * @param gl the OpenGL context
	 */
	private void renderScene(GL gl) {
		int objectCount = 1;
		synchronized (block) {
			Iterator<GameObject> it = block.getElements().iterator();
			GameObject o;
			while (it.hasNext()) {
				gl.glPushMatrix();
				o = it.next();
				gl.glTranslated(o.getX(), o.getY(), Z);
				if (mode == GL.GL_SELECT)
					gl.glLoadName(objectCount);
				o.getSource().render(gl);
				if (mode == GL.GL_RENDER && selected == o) {
					float w = (float)o.getSource().getWidth(), h = (float)o.getSource().getHeight();
					Element.clearOptions(gl);
					gl.glColor4f(0.0f, 0.0f, 0.0f, 0.3f);
					gl.glBegin(GL.GL_QUADS);
					gl.glVertex3f(0.0f, 0.0f, 9.f);
					gl.glVertex3f(0.0f, w, 9.f);
					gl.glVertex3f(h, w, 9.f);
					gl.glVertex3f(h, 0.0f, 9.f);
					gl.glEnd();
					Element.setOptions(gl);
				}
				gl.glPopMatrix();
				objectCount++;
			}
		}
	}
	private void grid(GL gl) {
		if (grid.isSelected()) {
			int gw = zoom.width, gh = zoom.height;
			int xc = -(int)Math.round(center.x), yc = -(int)Math.round(center.y);
			gl.glColor3f(0.5f, 0.5f, 0.5f);
			gl.glBegin(GL.GL_LINES);
			for (int x = -gw - 1; x < gw + 1; x++) {
				gl.glVertex3f(xc + x, yc - gh - 1, 9.f);
				gl.glVertex3f(xc + x, yc + gh + 1, 9.f);
			}
			for (int y = -gh - 1; y < gh + 1; y++) {
				gl.glVertex3f(xc - gw - 1, yc + y, 9.f);
				gl.glVertex3f(xc + gw + 1, yc + y, 9.f);
			}
			gl.glEnd();
		}
	}
	private void drawSuspicion(GL gl, int suspicion) {
		int w = canvas.getWidth(), h = canvas.getHeight();
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
		gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 2 * suspicion / 5 + 3);
		gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL.GL_COLOR_ARRAY);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPopMatrix();
		gl.glViewport(0, 0, w, h);
	}
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
			suspicion.put(0.f);
			suspicion.put(x);
			suspicion.put(y);
			suspicion.put(0.f);
		}
		suspicion.rewind();
	}
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) { }
	public void init(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		suspicion();
		hitBuffer = BufferUtil.newIntBuffer(48);
		gl.glSelectBuffer(12, hitBuffer);
		glu = new GLU();
		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, modelview, 0);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glInitNames();
		Element.setOptions(gl);
	}
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		ratio = (double)width / height;
		GL gl = drawable.getGL();
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		zoom.width = (int)(ratio * (double)zoom.height) + 1;
		view(gl);
		gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, projection, 0);
		gl.glMatrixMode(GL.GL_MODELVIEW);
	}
	private void view(GL gl) {
		double gh = (double)zoom.height;
		gl.glOrtho(-gh * ratio, gh * ratio, -gh, gh, -10, 10);
	}
	/**
	 * Stops the updating stuff for better performance when hidden.
	 */
	private void stopRun() {
		synchronized (winSync) {
			if (!running) return;
			running = false;
			anim.stop();
		}
	}
	/**
	 * Restarts the updating stuff.
	 */
	private void startRun() {
		synchronized (winSync) {
			if (running) return;
			running = true;
			anim.start();
		}
	}
	/**
	 * Unprojects and computes the location of the mouse.
	 * 
	 * @param gl the OpenGL context
	 */
	private void computeLocation(GL gl) {
		FloatBuffer buf = BufferUtil.newFloatBuffer(1);
		buf.rewind();
		// translate to opengl
		int y = canvas.getHeight() - this.y, x = this.x;
		// get depth
		gl.glReadPixels(x, y, 1, 1, GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, buf);
		// unproject
		if (glu.gluUnProject(x, y, buf.get(0), modelview, 0, projection, 0, viewport, 0, pos, 0)) {
			if (snapTo) {
				pos[0] = Math.floor(pos[0]);
				pos[1] = Math.floor(pos[1]);
				pos[2] = Math.floor(pos[2]);
			}
			if (coords == null)
				coords = new Point3(pos[0], pos[1], pos[2]);
			else {
				coords.setX(pos[0]);
				coords.setY(pos[1]);
				coords.setZ(pos[2]);
			}
			coords.setZ(Z);
			location.setText("At " + coords.toRoundedString());
		}
	}
	/**
	 * Changes the render mode.
	 * 
	 * @param gl the OpenGL context
	 * @param newMode either GL.GL_RENDER or GL.GL_SELECT
	 * @return some critical hit test stuff
	 */
	private int setMode(GL gl, int newMode) {
		mode = newMode;
		return gl.glRenderMode(newMode);
	}
	/**
	 * Looks for the block at the given location.
	 * 
	 * @param gl the OpenGL context
	 * @return the block hit
	 */
	private GameObject doHitTest(GL gl) {
		// save the matrix
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		// pick matrix mode
		gl.glLoadIdentity();
		glu.gluPickMatrix(x, canvas.getHeight() - y, 1, 1, viewport, 0);
		view(gl);
		// selection mode
		gl.glMatrixMode(GL.GL_MODELVIEW);
		setMode(gl, GL.GL_SELECT);
		// render
		gl.glPushName(-1);
		renderScene(gl);
		// get hit count
		int num = setMode(gl, GL.GL_RENDER);
		int records, depthF, name;
		hitBuffer.rewind();
		int sel = -1; int frontZ = Integer.MAX_VALUE;
		for (int i = 0; i < num; i++) {
			// get hit depth
			records = hitBuffer.get();
			depthF = hitBuffer.get();
			hitBuffer.get();
			if (records > 0) {
				// retrieve name
				name = hitBuffer.get();
				hitBuffer.position(hitBuffer.position() + (records - 1));
				if (depthF < frontZ && name >= 0) {
					// select this one
					frontZ = depthF;
					sel = name;
				}
			}
		}
		hitBuffer.rewind();
		GameObject selected;
		if (sel < 1) selected = null;
		else selected = block.getElements().get(sel - 1);
		// restore matrix
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		return selected;
	}
	/**
	 * Updates the screen position.
	 * 
	 * @param gl the GL context
	 */
	private void updatePosition(GL gl) {
		if (vel.getX() != 0.0 || vel.getY() != 0.0) {
			moveRelativePosition(vel.getX() / 2, vel.getY() / 2, 0);
			gl.glLoadIdentity();
			gl.glTranslated(center.getX(), center.getY(), center.getZ());
			gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, modelview, 0);
			synchronized (eventSync) {
				computeLocation(gl);
			}
		}
		if (x < 60) {
			vel.setX(Math.min(0.5, vel.getX() + 0.1));
			if (y > canvas.getHeight() - 60)
				vel.setY(Math.min(0.5, vel.getY() + 0.1));
			else if (y < 60)
				vel.setY(Math.max(-0.5, vel.getY() - 0.1));
		} else if (x > canvas.getWidth() - 60) {
			vel.setX(Math.max(-0.5, vel.getX() - 0.1));
			if (y > canvas.getHeight() - 60)
				vel.setY(Math.min(0.5, vel.getY() + 0.1));
			else if (y < 60)
				vel.setY(Math.max(-0.5, vel.getY() - 0.1));
		} else if (y < 60)
			vel.setY(Math.max(-0.5, vel.getY() - 0.1));
		else if (y > canvas.getHeight() - 60)
			vel.setY(Math.min(0.5, vel.getY() + 0.1));
		vel.setY(vel.getY() - 0.05 * Math.signum(vel.getY()));
		vel.setX(vel.getX() - 0.05 * Math.signum(vel.getX()));
		if (Math.abs(vel.getX()) < 0.001) vel.setX(0);
		if (Math.abs(vel.getY()) < 0.001) vel.setY(0);
	}
	/**
	 * Moves the view to the given new location.
	 * 
	 * @param nx the new X coordinate
	 * @param ny the new Y coordinate
	 * @param nz the new Z coordinate
	 */
	private void moveToPosition(double nx, double ny, double nz) {
		center.setX(nx);
		center.setY(ny);
		center.setZ(nz);
	}
	/**
	 * Moves the view by the given amounts.
	 * 
	 * @param dx the delta X coordinate
	 * @param dy the delta Y coordinate
	 * @param dz the delta Z coordinate
	 */
	private void moveRelativePosition(double dx, double dy, double dz) {
		moveToPosition(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
	}
	/**
	 * Centers the mouse.
	 */
	private void center() {
		x = canvas.getWidth() / 2;
		y = canvas.getHeight() / 2;
	}

	/**
	 * Listens for events. All your events are belong to me.
	 */
	private class EventListener extends MouseAdapter implements WindowListener, WindowFocusListener,
			ActionListener, MouseMotionListener, KeyListener {
		public void windowActivated(WindowEvent e) {
			startRun();
		}
		public void windowClosed(WindowEvent e) {
			dispose();
		}
		public void windowClosing(WindowEvent e) {
			windowClosed(e);
			System.exit(0);
		}
		public void windowDeactivated(WindowEvent e) {
			stopRun();
		}
		public void windowDeiconified(WindowEvent e) {
			startRun();
		}
		public void windowGainedFocus(WindowEvent e) {
			startRun();
		}
		public void windowIconified(WindowEvent e) {
			stopRun();
		}
		public void windowLostFocus(WindowEvent e) {
			stopRun();
		}
		public void windowOpened(WindowEvent e) { }
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd == null) return;
			else if (cmd.startsWith("place") && cmd.length() > 5) {
				dropping = elements.get(cmd.substring(5));
				selected = null;
			} else if (cmd.equals("done")) {
				dropping = null;
				selected = null;
			}
		}
		public void mouseEntered(MouseEvent e) {
			mouseMoved(e);
		}
		public void mouseMoved(MouseEvent e) {
			x = e.getX();
			y = e.getY();
			synchronized (eventSync) {
				snapTo = true;
				if (e.isShiftDown()) snapTo = false;
				event = RENDER;
			}
		}
		public void mouseExited(MouseEvent e) {
			coords = null;
			center();
		}
		public void mouseReleased(MouseEvent e) {
			synchronized (eventSync) {
				if (dropping != null && coords != null) event = PLACE;
				else if (dropping == null) event = HIT_TEST;
			}
		}
		public void mouseDragged(MouseEvent e) {
			x = e.getX();
			y = e.getY();
			if (!e.isShiftDown())
				synchronized (eventSync) {
					if (dropping != null && coords != null) event = PLACE_IFNOT;
				}
		}
		public void keyPressed(KeyEvent e) { }
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
				if (selected == null) return;
				synchronized (block) {
					block.getElements().remove(selected);
				}
				selected = null;
			}
		}
		public void keyTyped(KeyEvent e) { }
	}

	/**
	 * A block that can be placed on screen.
	 */
	private class PlacableBlock extends JButton {
		private static final long serialVersionUID = 0L;

		/**
		 * Sets up all the parameters.
		 * 
		 * @param element the element to represent
		 */
		public PlacableBlock(Element element) {
			super(element.getName());
			setHorizontalTextPosition(SwingConstants.CENTER);
			setVerticalTextPosition(SwingConstants.BOTTOM);
			setHorizontalAlignment(SwingConstants.CENTER);
			setVerticalAlignment(SwingConstants.TOP);
			setContentAreaFilled(false);
			setBorder(BORDER);
			setFocusable(false);
			setIcon(icons.getPreviewIcon(element.getTextureLocation()));
			setActionCommand("place" + element.getName());
			setFont(getFont().deriveFont(10.0f));
			setPreferredSize(BLOCK_SIZE);
			addActionListener(events);
		}
	}
}