import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.sun.opengl.util.*;
import java.util.*;
import java.util.List;
import java.io.*;
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
	 * Place the element.
	 */
	public static final int PLACE = 2;
	/**
	 * Place the element, if it's not there already.
	 */
	public static final int PLACE_IFNOT = 3;
	/**
	 * Select an element (hit test).
	 */
	public static final int HIT_TEST = 4;
	/**
	 * Re-render the dropping element.
	 */
	public static final int RENDER = 1;
	/**
	 * Load all textures in use.
	 */
	public static final int LOADALL = 5;
	/**
	 * Resize the screen.
	 */
	public static final int RESIZE = 6;
	/**
	 * Move the selected object.
	 */
	public static final int MOVE = 7;
	/**
	 * Renders a disk preview of the current element (dropping).
	 */
	public static final int PREVIEW = 8;
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
	 * Size of the GL canvas.
	 */
	private Rectangle size;
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
	 * The root resource list.
	 */
	private ResourceGetter root;
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
	 * Depth buffer for hit testing.
	 */
	private FloatBuffer depthBuffer;
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
	 * How much this object is rotated.
	 */
	private int rotation;
	/**
	 * Flipped in either direction?
	 */
	private boolean flipX, flipY;
	/**
	 * Flip vector.
	 */
	private Vector3 flipVec;
	/**
	 * When the most recent object was rendered.
	 */
	private long lastRender;
	/**
	 * The selected button.
	 */
	private JButton deselect;
	/**
	 * Open/Save dialog.
	 */
	private JFileChooser chooser;
	/**
	 * Drag activated?
	 */
	private boolean drag;
	/**
	 * The destination file for saving.
	 */
	private File fileName;
	/**
	 * The level code's name.
	 */
	private String code;
	/**
	 * Level code goes here.
	 */
	private JTextArea editCode;
	/**
	 * Edit code dialog.
	 */
	private JDialog codeDialog;
	/**
	 * The name of the object.
	 */
	private JTextField propName;
	/**
	 * The motion path information.
	 */
	private JTextField propMotion;
	/**
	 * Properties dialog.
	 */
	private JDialog propDialog;

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
		block = null;
		snapTo = true;
		lastPlace = coords = null;
		center = new Point3(0.0, 0.0, 0.0);
		vel = new Vector3(0.0, 0.0, 0.0);
		zoom = new Dimension(GW, GH);
		mode = GL.GL_RENDER;
		selected = null;
		deselect = null;
		lastRender = 0;
		flipX = flipY = false;
		flipVec = new Vector3();
		fileName = null;
	}
	/**
	 * Invoked to start the level editor.
	 */
	public void start() {
		// FIXME replace with the jar one
		root = new FilesystemResources(null, new File("res/"));
		current = new FilesystemResources(root, new File("res/"));
		depthBuffer = BufferUtil.newFloatBuffer(1);
		icons = new IconBuilder(current);
		dropping = null;
		coords = null;
		size = new Rectangle(ZEROZERO);
		// TODO temp
		addElement(new Element("checkerboard.png", "1x1square.dat", "checkerboard", -2));
		addElement(new Element("checkerboard.png", "1x1dark.dat", "darkcheck", -2));
		addElement(new Element("grass.png", "1x1square.dat", "grass", -2));
		addElement(new Element("angleblock.png", "1x1square.dat", "ramp", 0));
		addElement(new Element("angletransition.png", "1x1square.dat", "ramp-t", 0));
		addElement(new Element("bottomblock.png", "1x1square.dat", "bottom", 0));
		addElement(new Element("ceiling.png", "1x1square.dat", "ceiling", 0));
		addElement(new Element("ladder.png", "1x1square.dat", "ladder", 0));
		addElement(new Element("wall.png", "1x1square.dat", "wall", 0));
		addElement(new Element(null, "1x1black.dat", "filler", 0));
		addElement(new Element("laserbase.png", "1x1square.dat", "laserbase", 1));
		addElement(new Element("lasermid.png", "1x1square.dat", "lasermid", 1));
		addElement(new Element("savepoint.png", "2x1square.dat", "savepoint", 0));
		addElement(new Element("static-spot-1.png", "2x2square.dat", "static-spot", 2));
		addElement(new Element("door.png", "2x1square.dat", "door", -1));
		addElement(new Element("light-emitter.png", "1x2square.dat", "light-emitter", -1));
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
		newFile();
		setVisible(true);
		canvas.requestFocus();
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
		horiz.add(Box.createHorizontalStrut(5));
		horiz.add(cancel);
		horiz.add(Box.createHorizontalStrut(5));
		horiz.add(pane);
		horiz.add(Box.createHorizontalStrut(5));
		getContentPane().add(horiz, BorderLayout.NORTH);
		canvas.addMouseListener(events);
		canvas.addMouseMotionListener(events);
		canvas.addKeyListener(events);
		canvas.addMouseWheelListener(events);
		setupBottom();
		setupMenus();
		setupCode();
		setupProps();
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(".").getAbsoluteFile().getParentFile());
		chooser.setMultiSelectionEnabled(false);
	}
	/**
	 * Sets up the edit code dialog box.
	 */
	private void setupCode() {
		TextareaListener ta = new TextareaListener();
		codeDialog = new JDialog(this, "Edit Code");
		codeDialog.setModal(true);
		codeDialog.setResizable(true);
		codeDialog.addWindowListener(events);
		editCode = new JTextArea();
		editCode.setEditable(true);
		editCode.setFont(new Font("Lucida Console", Font.PLAIN, 10));
		editCode.setTabSize(4);
		editCode.addKeyListener(ta);
		Utils.fixShiftBackspace(editCode);
		JScrollPane sp = new JScrollPane(editCode);
		Container c = codeDialog.getContentPane();
		c.setLayout(new BorderLayout(0, 0));
		c.add(sp, BorderLayout.CENTER);
		JButton close = new JButton("Close");
		close.setFocusable(false);
		close.setActionCommand("nocode");
		close.addActionListener(events);
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
		bottom.setOpaque(false);
		bottom.add(close);
		c.add(bottom, BorderLayout.SOUTH);
		codeDialog.setSize(640, 480);
		Utils.centerWindow(codeDialog);
	}
	/**
	 * Sets up the properties dialog box.
	 */
	private void setupProps() {
		propDialog = new JDialog(this, "Properties");
		propDialog.setModal(true);
		propDialog.setResizable(false);
		propDialog.addWindowListener(events);
		propName = new JTextField(32);
		propName.setActionCommand("noprops");
		Utils.fixShiftBackspace(propName);
		propMotion = new JTextField(32);
		propMotion.setActionCommand("noprops");
		Utils.fixShiftBackspace(propMotion);
		Container c = propDialog.getContentPane();
		JComponent props = new Box(BoxLayout.Y_AXIS);
		// add rows
		props.add(addHorizontal("Name:", propName));
		props.add(addHorizontal("Motion:", propMotion));
		c.add(props, BorderLayout.CENTER);
		JPanel horiz = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
		horiz.setOpaque(false);
		JButton cancel = new JButton("Close");
		cancel.setFocusable(false);
		cancel.setActionCommand("noprops");
		cancel.addActionListener(events);
		horiz.add(cancel);
		c.add(horiz, BorderLayout.SOUTH);
		propDialog.pack();
		Utils.centerWindow(propDialog);
	}
	/**
	 * Creates a two-component horizontal entry box.
	 * 
	 * @param label the label for the text box
	 * @param entry the actual text box
	 * @return a panel containing both of them
	 */
	private JComponent addHorizontal(String label, JComponent entry) {
		JComponent ret = new Box(BoxLayout.X_AXIS);
		ret.add(new JLabel(label));
		ret.add(Box.createHorizontalStrut(5));
		ret.add(entry);
		return ret;
	}
	/**
	 * Sets up the status bar at the bottom of the screen.
	 */
	private void setupBottom() {
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
	 * Creates the menus at the top of the screen.
	 */
	private void setupMenus() {
		JMenuBar across = new JMenuBar();
		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		file.add(createMenuItem("New", "new", KeyEvent.VK_N));
		file.add(createMenuItem("Open...", "open", KeyEvent.VK_O));
		file.addSeparator();
		file.add(createMenuItem("Save", "save", KeyEvent.VK_S));
		file.add(createMenuItem("Save As...", "saveas", 0));
		file.add(createMenuItem("Pack...", "pack", KeyEvent.VK_P));
		file.addSeparator();
		file.add(createMenuItem("Exit", "exit", KeyEvent.VK_Q));
		across.add(file);
		JMenu edit = new JMenu("Edit");
		edit.setMnemonic(KeyEvent.VK_E);
		edit.add(createMenuItem("Rotate (R)", "rotate", 0));
		edit.add(createMenuItem("Flip X (X)", "flipx", 0));
		edit.add(createMenuItem("Flip Y (Y)", "flipy", 0));
		edit.addSeparator();
		edit.add(createMenuItem("Properties", "prop", KeyEvent.VK_1));
		edit.add(createMenuItem("Code", "code", KeyEvent.VK_E));
		across.add(edit);
		setJMenuBar(across);
	}
	/**
	 * Creates a menu item for the menus.
	 * 
	 * @param title the text to display
	 * @param action the command to send
	 * @param key the key mnemonic
	 */
	private JMenuItem createMenuItem(String title, String action, int key) {
		JMenuItem item = new JMenuItem(title);
		item.setActionCommand(action);
		item.addActionListener(events);
		item.setMnemonic(key);
		if (key > 0)
			item.setAccelerator(KeyStroke.getKeyStroke(key, KeyEvent.CTRL_MASK));
		return item;
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
			try {
				element.loadGeometry(current);
			} catch (Exception e) {
				Utils.showWarning("Unable to load geometry for " + element + ".");
			}
			available.add(new PlacableBlock(element));
		}
		if (elements.size() % 2 > 0)
			available.add(Box.createGlue());
		available.validate();
		available.repaint();
		available.scrollRectToVisible(ZEROZERO);
	}
	/**
	 * Rotates the current block.
	 */
	private void rotate(int amount) {
		if (dropping == null) return;
		rotation = (rotation + amount + 360) % 360;
		synchronized (eventSync) {
			event = RENDER;
		}
	}
	/**
	 * Drops the block at the current location.
	 */
	private void drop() {
		if (dropping == null || coords == null) return;
		if (event == PLACE_IFNOT) {
			if (lastPlace != null && Math.floor(lastPlace.x) == Math.floor(coords.x) &&
				Math.floor(lastPlace.y) == Math.floor(coords.y)) return;
		}
		Vector3 rotVec = new Vector3(flipX ? 180 : 0, flipY ? 180 : 0, rotation);
		GameObject newObject = new GameObject(coords.x, coords.y, dropping.getDefaultZ(),
			rotVec, dropping);
		synchronized (block) {
			block.addObject(newObject);
			if (event == PLACE_IFNOT)
				lastPlace = new Point3(coords.x, coords.y, dropping.getDefaultZ());
		}
	}
	/**
	 * Saves a preview icon for the element.
	 * 
	 * @param gl the OpenGL context
	 * @param e the element to draw
	 */
	private void savePreview(GL gl, Element e) {
		int width = e.getWidth(), height = e.getHeight();
		OffscreenRenderer render = new OffscreenRenderer(gl, width * 32, height * 32);
		render.setRender(gl);
		// set up rendering matrices
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrtho(0, width, 0, height, -10, 10);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		// render!
		if (!e.hasTexture()) e.loadTexture(current);
		e.render(gl);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPopMatrix();
		// write and finalize
		render.clearRender(gl);
		render.writeTexture(new File("res/images/" + e.getName() + ".png"));
		render.dispose();
	}
	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		long t = System.currentTimeMillis();
		if (dropping != null && !dropping.hasTexture())
			dropping.loadTexture(current);
		synchronized (eventSync) {
			switch (event) {
			case PREVIEW:
				if (dropping != null)
					savePreview(gl, dropping);
				break;
			case LOADALL:
				synchronized (block) {
					Element el;
					for (GameObject o : block.getElements()) {
						el = o.getSource();
						if (el.getVertexArray() == null)
							el.loadGeometry(current);
						if (!el.hasTexture())
							el.loadTexture(current);
					}
				}
				break;
			case MOVE:
				computeLocation(gl);
				if (selected != null) {
					selected.getLocation().setX(coords.getX());
					selected.getLocation().setY(coords.getY());
				}
				break;
			case RESIZE:
				reproject(gl);
				break;
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
		if (t - lastRender > 30L) {
			lastRender = t;
			gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
			updatePosition(gl);
			renderScene(gl);
			grid(gl);
		}
		Utils.sleep(15L);
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
	 * Correctly rotates the element.
	 * 
	 * @param gl the OpenGL context
	 * @param toRotate the object to rotate
	 * @param amount the amount to rotate
	 */
	private void doRotate(GL gl, Element toRotate) {
		flipVec.setZ(rotation);
		flipVec.setY(flipY ? 180 : 0);
		flipVec.setX(flipX ? 180 : 0);
		doRotate(gl, toRotate, flipVec);
	}
	/**
	 * Renders the currently dropping object.
	 * 
	 * @param gl the OpenGL context
	 */
	private void renderDropping(GL gl) {
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glPushMatrix();
		gl.glTranslated(coords.getX(), coords.getY(), dropping.getDefaultZ());
		doRotate(gl, dropping);
		dropping.render(gl);
		gl.glPopMatrix();
		gl.glEnable(GL.GL_DEPTH_TEST);
	}
	/**
	 * Renders the selection indicator.
	 * 
	 * @param gl the OpenGL context
	 * @param o the selected object
	 */
	private void renderSelection(GL gl, GameObject o) {
		float w = (float)o.getSource().getWidth(), h = (float)o.getSource().getHeight(),
			z = (float)o.getZ();
		Element.clearOptions(gl);
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glColor4f(0.0f, 0.0f, 0.0f, 0.3f);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex3f(0.0f, 0.0f, z);
		gl.glVertex3f(0.0f, h, z);
		gl.glVertex3f(w, h, z);
		gl.glVertex3f(w, 0.0f, z);
		gl.glEnd();
		gl.glEnable(GL.GL_DEPTH_TEST);
		Element.setOptions(gl);
	}
	/**
	 * Draws the entire level.
	 * 
	 * @param gl the OpenGL context
	 */
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
				if (mode == GL.GL_SELECT)
					gl.glLoadName(objectCount);
				if (!Utils.properCompare(lastTexture, element.getTextureLocation())) {
					element.setTexture(gl);
					lastTexture = element.getTextureLocation();
				}
				if (!lastModel.equals(element.getGeometryLocation())) {
					element.renderModel(gl);
					lastModel = element.getGeometryLocation();
				}
				element.draw(gl);
				if (mode == GL.GL_RENDER && selected == o) {
					renderSelection(gl, o);
					lastTexture = lastModel = "";
				}
				gl.glPopMatrix();
				objectCount++;
			}
			if (mode == GL.GL_RENDER && dropping != null && coords != null)
				renderDropping(gl);
		}
	}
	/**
	 * Renders the grid on the screen.
	 * 
	 * @param gl the OpenGL context
	 */
	private void grid(GL gl) {
		if (grid.isSelected()) {
			int gw = zoom.width, gh = zoom.height;
			int xc = -(int)Math.round(center.x), yc = -(int)Math.round(center.y);
			gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
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
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) { }
	public void init(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		hitBuffer = BufferUtil.newIntBuffer(48);
		gl.glSelectBuffer(12, hitBuffer);
		glu = new GLU();
		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, modelview, 0);
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
		gl.glInitNames();
		Element.setOptions(gl);
	}
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL gl = drawable.getGL();
		if (x != size.x || y != size.y || width != size.width || height != size.height) {
			size.x = x;
			size.y = y;
			size.width = width;
			size.height = height;
			ratio = (double)width / height;
			gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
			reproject(gl);
		}
	}
	/**
	 * Remakes the projection matrix.
	 * 
	 * @param gl the OpenGL context
	 */
	private void reproject(GL gl) {
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		zoom.width = (int)(ratio * (double)zoom.height) + 1;
		view(gl);
		gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, projection, 0);
		gl.glMatrixMode(GL.GL_MODELVIEW);
	}
	/**
	 * Does only the viewing transform.
	 * 
	 * @param gl the OpenGL context
	 */
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
		depthBuffer.rewind();
		// translate to opengl
		int y = canvas.getHeight() - this.y, x = this.x;
		// get depth
		gl.glReadPixels(x, y, 1, 1, GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, depthBuffer);
		// unproject
		if (glu.gluUnProject(x, y, depthBuffer.get(0), modelview, 0, projection, 0, viewport, 0, pos, 0)) {
			if (snapTo) {
				pos[0] = Math.floor(pos[0]);
				pos[1] = Math.floor(pos[1]);
				pos[2] = Math.floor(pos[2]);
			}
			if (coords == null)
				coords = new Point3(pos[0], pos[1], 0);
			else {
				coords.setX(pos[0]);
				coords.setY(pos[1]);
				//coords.setZ(pos[2]);
			}
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
		hitBuffer.rewind();
		renderScene(gl);
		// get hit count
		int num = setMode(gl, GL.GL_RENDER);
		int records, depthF, name;
		hitBuffer.rewind();
		int sel = -1; int frontZ = Integer.MIN_VALUE;
		for (int i = 0; i < num; i++) {
			// get hit depth
			records = hitBuffer.get();
			depthF = hitBuffer.get();
			hitBuffer.get();
			if (records > 0) {
				// retrieve name
				name = hitBuffer.get();
				hitBuffer.position(hitBuffer.position() + (records - 1));
				if (depthF >= frontZ && name >= 0) {
					// select this one
					frontZ = depthF;
					sel = name;
				}
			}
		}
		hitBuffer.rewind();
		GameObject selected;
		if (sel < 1) selected = null;
		else synchronized (block) {
			// search for the element that matches this one
			Iterator<GameObject> it = block.getElements().iterator();
			GameObject o; int objectCount = 1;
			selected = null;
			while (it.hasNext()) {
				o = it.next();
				if (objectCount == sel) {
					selected = o;
					break;
				}
				objectCount++;
			}
		}
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
	 * Saves the level with a dialog.
	 * 
	 * @param forceDialog whether the dialog must be shown
	 * @return true if saved, false if not saved
	 */
	private boolean save(boolean forceDialog) {
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setDialogTitle("Save Level");
		if (forceDialog || fileName == null) {
			if (chooser.showDialog(this, "Save") != JFileChooser.APPROVE_OPTION)
				return false;
			fileName = chooser.getSelectedFile();
		}
		if (fileName != null) try {
			code = editCode.getText();
			LevelWriter out = new LevelWriter(new FileOutputStream(fileName));
			out.writeLevel(new Level(Collections.nCopies(1, block)));
			out.close();
			Writer pr = new BufferedWriter(new FileWriter(getCodeFile()));
			pr.write(code);
			pr.close();
			return true;
		} catch (IOException e) {
			Utils.showWarning("Can't save level!");
		}
		return false;
	}
	/**
	 * Asks to save changes from the current dialog.
	 * 
	 * @return true to continue, false to abort close
	 */
	private boolean saveDialog() {
		if (fileName == null && block.getElements().size() == 0) return true;
		int choice = JOptionPane.showConfirmDialog(this, "Save changes to level?",
			"Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (choice == JOptionPane.YES_OPTION && save(false)) return true;
		if (choice == JOptionPane.NO_OPTION) return true;
		return false;
	}
	/**
	 * Sets properties on the selected block.
	 */
	private void doProps() {
		if (selected == null) return;
		String text = propName.getText();
		if (text == null || text.length() < 1)
			selected.putAttribute("name", null);
		else
			selected.putAttribute("name", text);
		text = propMotion.getText();
		if (text == null || text.length() < 1)
			selected.putAttribute("motion", null);
		else
			selected.putAttribute("motion", text);
	}
	/**
	 * Gets the properties from the block and puts them into the window.
	 */
	private void getProps() {
		if (selected == null) return;
		String attrib = selected.getAttribute("name");
		if (attrib == null)
			propName.setText("");
		else
			propName.setText(attrib);
		attrib = selected.getAttribute("motion");
		if (attrib == null)
			propMotion.setText("");
		else
			propMotion.setText(attrib);
	}
	/**
	 * Erases everything and makes a new file.
	 */
	private void newFile() {
		if (block != null && !saveDialog()) return;
		fileName = null;
		block = new Block();
		selected = null;
		dropping = null;
		if (deselect != null) deselect.setSelected(false);
		deselect = null;
		code = root.getString("default.java");
		editCode.setText(code);
	}
	/**
	 * Opens a level.
	 */
	private void open() {
		if (!saveDialog()) return;
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setDialogTitle("Open Level");
		if (chooser.showDialog(this, "Open") == JFileChooser.APPROVE_OPTION) try {
			File file = chooser.getSelectedFile();
			if (file == null) return;
			file = file.getAbsoluteFile();
			current = new FilesystemResources(root, file.getParentFile());
			LevelReader in = new LevelReader(current, file.getName());
			fileName = file;
			Level level = in.getLevel();
			selected = null;
			dropping = null;
			if (deselect != null) deselect.setSelected(false);
			icons = new IconBuilder(current);
			deselect = null;
			synchronized (eventSync) {
				block = level.blockIterator().next();
				event = LOADALL;
			}
			File code = getCodeFile();
			if (code.exists()) {
				this.code = Utils.readFile(code);
			} else
				this.code = root.getString("default.java");
			editCode.setText(this.code);
		} catch (Exception e) {
			Utils.showWarning("Can't open level!");
		}
	}
	/**
	 * Gets the file for level code, given that fileName is initialized.
	 * 
	 * @return the level code file
	 */
	private File getCodeFile() {
		if (fileName == null) return null;
		String name = fileName.getName();
		int dot = name.lastIndexOf('.');
		if (dot > 0)
			name = name.substring(0, dot);
		return new File(fileName.getParentFile(), name + ".java");
	}

	/**
	 * Listens for events in the edit code text area.
	 */
	private class TextareaListener extends KeyAdapter {
		public void keyTyped(KeyEvent e) {
			if (e.getKeyChar() == '\r' || e.getKeyChar() == '\n') {
				// Auto indent
				if (editCode.getSelectedText() != null)
					editCode.replaceSelection("");
				int pos = editCode.getCaretPosition();
				String text = editCode.getText();
				int lastLF = text.lastIndexOf('\n', pos - 2);
				if (lastLF < 0) return;
				int nextSpace = lastLF;
				while (text.charAt(nextSpace) <= ' ' && nextSpace < pos - 1)
					nextSpace++;
				text = text.substring(lastLF + 1, nextSpace);
				editCode.insert(text, pos);
			}
		}
	}
	/**
	 * Listens for events. All your events are belong to me.
	 */
	private class EventListener extends MouseAdapter implements WindowListener, WindowFocusListener,
			ActionListener, MouseMotionListener, KeyListener, MouseWheelListener {
		public void windowActivated(WindowEvent e) {
			if (e.getSource() == EditorUI.this)
				startRun();
		}
		public void windowClosed(WindowEvent e) {
			if (e.getSource() == EditorUI.this)
				dispose();
		}
		public void windowClosing(WindowEvent e) {
			if (e.getSource() == codeDialog)
				code = editCode.getText();
			else if (e.getSource() == propDialog)
				doProps();
			else if (saveDialog()) {
				windowClosed(e);
				System.exit(0);
			}
		}
		public void windowDeactivated(WindowEvent e) {
			if (e.getSource() == EditorUI.this)
				stopRun();
		}
		public void windowDeiconified(WindowEvent e) {
			if (e.getSource() == EditorUI.this)
				startRun();
		}
		public void windowGainedFocus(WindowEvent e) {
			if (e.getSource() == EditorUI.this)
				startRun();
		}
		public void windowIconified(WindowEvent e) {
			if (e.getSource() == EditorUI.this)
				stopRun();
		}
		public void windowLostFocus(WindowEvent e) {
			if (e.getSource() == EditorUI.this)
				stopRun();
		}
		public void windowOpened(WindowEvent e) { }
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd == null) return;
			else if (cmd.equals("done")) {
				dropping = null;
				selected = null;
				lastPlace = null;
				flipX = flipY = false;
				if (deselect != null) deselect.setSelected(false);
				deselect = null;
			} else if (cmd.equals("exit") && saveDialog())
				System.exit(0);
			else if (cmd.equals("flipx") && dropping != null)
				synchronized (eventSync) {
					flipX = !flipX;
					event = RENDER;
				}
			else if (cmd.equals("flipy") && dropping != null)
				synchronized (eventSync) {
					flipY = !flipY;
					event = RENDER;
				}
			else if (cmd.equals("rotate"))
				rotate(90);
			else if (cmd.equals("saveas")) save(true);
			else if (cmd.equals("save")) save(false);
			else if (cmd.equals("open")) open();
			else if (cmd.equals("new")) newFile();
			else if (cmd.equals("code")) {
				codeDialog.setVisible(true);
				editCode.requestFocus();
			} else if (cmd.equals("prop")) {
				if (selected == null) return;
				getProps();
				propDialog.setVisible(true);
				propName.requestFocus();
			} else if (cmd.equals("nocode")) {
				codeDialog.setVisible(false);
				code = editCode.getText();
			} else if (cmd.equals("noprops")) {
				propDialog.setVisible(false);
				doProps();
			}
		}
		public void mouseEntered(MouseEvent e) {
			mouseMoved(e);
		}
		public void mouseMoved(MouseEvent e) {
			x = e.getX();
			y = e.getY();
			synchronized (eventSync) {
				snapTo = !e.isShiftDown();
				if (event == 0) event = RENDER;
			}
		}
		public void mouseExited(MouseEvent e) {
			coords = null;
			center();
		}
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1 && !e.isControlDown())
				synchronized (eventSync) {
					if (drag)
						event = MOVE;
					drag = false;
					if (dropping != null && coords != null) event = PLACE_IFNOT;
					else if (dropping == null) event = HIT_TEST;
				}
			else if (e.getButton() != MouseEvent.BUTTON1 || e.isControlDown())
				rotate(e.isShiftDown() ? 15 : 90);
		}
		public void mouseDragged(MouseEvent e) {
			x = e.getX();
			y = e.getY();
			if (!e.isShiftDown())
				synchronized (eventSync) {
					if (dropping != null && coords != null) event = PLACE_IFNOT;
				}
			if (dropping == null && selected != null)
				synchronized (eventSync) {
					drag = true;
					event = MOVE;
					snapTo = !e.isShiftDown();
				}
		}
		public void keyPressed(KeyEvent e) { }
		public void keyReleased(KeyEvent e) {
			int code = e.getKeyCode();
			if (code == KeyEvent.VK_DELETE || code == KeyEvent.VK_BACK_SPACE) {
				if (selected == null) return;
				synchronized (block) {
					block.removeObject(selected);
				}
				selected = null;
			} else if (code == KeyEvent.VK_ESCAPE) {
				selected = null;
				dropping = null;
				flipX = flipY = false;
				synchronized (eventSync) {
					event = RENDER;
				}
				if (deselect != null) deselect.setSelected(false);
				deselect = null;
			} else if (code == KeyEvent.VK_R)
				rotate(e.isShiftDown() ? 15 : 90);
			else if (code == KeyEvent.VK_P && dropping != null)
				synchronized (eventSync) {
					event = PREVIEW;
				}
			else if (code == KeyEvent.VK_X && dropping != null)
				synchronized (eventSync) {
					flipX = !flipX;
					event = RENDER;
				}
			else if (code == KeyEvent.VK_Y && dropping != null)
				synchronized (eventSync) {
					flipY = !flipY;
					event = RENDER;
				}
		}
		public void keyTyped(KeyEvent e) { }
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
				zoom.height += e.getUnitsToScroll() / 2;
				if (zoom.height < 2) zoom.height = 2;
				if (zoom.height > 20) zoom.height = 20;
				synchronized (eventSync) {
					event = RESIZE;
				}
			}
		}
	}

	/**
	 * A block that can be placed on screen.
	 */
	private class PlacableBlock extends JButton implements ActionListener {
		private static final long serialVersionUID = 0L;

		/**
		 * The element for this block.
		 */
		private Element element;

		/**
		 * Sets up all the parameters.
		 * 
		 * @param element the element to represent
		 */
		public PlacableBlock(Element element) {
			super(element.getName(), icons.getPreviewIcon(element));
			this.element = element;
			setHorizontalTextPosition(SwingConstants.CENTER);
			setVerticalTextPosition(SwingConstants.BOTTOM);
			setHorizontalAlignment(SwingConstants.CENTER);
			setVerticalAlignment(SwingConstants.TOP);
			//setContentAreaFilled(false);
			//setBorder(BORDER);
			setSelected(false);
			setFocusable(false);
			setActionCommand("place");
			setFont(getFont().deriveFont(10.0f));
			setPreferredSize(BLOCK_SIZE);
			addActionListener(this);
		}
		public void actionPerformed(ActionEvent e) {
			if (deselect != null) deselect.setSelected(false);
			selected = null;
			dropping = element;
			rotation = 0;
			flipX = flipY = false;
			setSelected(true);
			deselect = this;
			synchronized (eventSync) {
				event = RENDER;
			}
		}
	}
}