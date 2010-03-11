import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.media.opengl.*;
import com.sun.opengl.util.*;
import java.util.*;

/**
 * The GEnterprise Level Editor.
 * 
 * @author Stephen
 */
public class EditorUI extends JFrame implements GLEventListener {
	private static final long serialVersionUID = 0L;

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
	 * The current resource list.
	 */
	private ResourceGetter current;
	/**
	 * The elements that are placable.
	 */
	private Map<String, Element> elements;

	/**
	 * Sets the window title.
	 */
	public EditorUI() {
		super("Gunther's Enterprise Level Editor");
		winSync = new Object();
		elements = new TreeMap<String, Element>();
	}
	/**
	 * Invoked to start the level editor.
	 */
	public void start() {
		current = new FilesystemResources(null, new java.io.File("res/"));
		// TODO temp
		Element element = new Element("checkerboard.png", "checkerboard.dat", "checkerboard");
		addElement(element);
		for (int i = 0; i < 5; i++)
			addElement(new Element("checkerboard.png", "checkerboard.dat", "checkerboard" + i));
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
		getContentPane().add(pane, BorderLayout.NORTH);
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
			available.add(new PlacableBlock(element));
		}
		if (elements.size() % 2 > 0)
			available.add(Box.createGlue());
		available.validate();
		available.repaint();
		available.scrollRectToVisible(ZEROZERO);
	}
	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
		Utils.sleep(30L);
	}
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) { }
	public void init(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_BLEND);
	}
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		double ratio = (double)width / height;
		GL gl = drawable.getGL();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(-20 * ratio, 20 * ratio, -20, 20, 0.1, 10);
		gl.glMatrixMode(GL.GL_MODELVIEW);
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
	 * Listens for events.
	 */
	private class EventListener implements WindowListener, WindowFocusListener, ActionListener {
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
		}
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
			setIcon(current.getIcon(element.getTextureLocation()));
			setActionCommand(element.getName());
			setFont(getFont().deriveFont(10.0f));
			addActionListener(events);
		}
	}
}