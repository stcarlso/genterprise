import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import java.util.List;

/**
 * Main runner for the entire program in a frame.
 *  All other classes are referenced, directly or indirectly, by this one.
 * 
 * @author Stephen
 */
public class GEnterprise extends JFrame implements Runnable, MouseListener {
	private static final long serialVersionUID = 0L;

	/**
	 * Default game window width if not chosen in graphics preferences.
	 */
	public static final int WIDTH = 1024;
	/**
	 * Default game window height if not chosen in graphics preferences.
	 */
	public static final int HEIGHT = 768;
	/**
	 * Filters out level files.
	 */
	public static final LevelFilter LEVEL_FILTER = new LevelFilter();

	/**
	 * Called when the program is first run.
	 */
	public static void main(String[] args) {
		Utils.staticInit();
		new GEnterprise().start();
	}

	/**
	 * Listens for events on the menus.
	 */
	private EventListener events;
	/**
	 * The current menu.
	 */
	private JComponent current;
	/**
	 * The main menu.
	 */
	private Menu main;
	/**
	 * The pause menu.
	 */
	private Menu pause;
	/**
	 * The settings menu.
	 */
	private Menu settings;
	/**
	 * Actual pause menu as laid out on screen (fix layout issues)
	 */
	private JComponent pMenu;
	/**
	 * Normal menu header.
	 */
	private JLabel header;
	/**
	 * Pause menu header.
	 */
	private JLabel headerPause;
	/**
	 * The sound and sfx player for the entire game.
	 */
	private MusicThread player;
	/**
	 * The resource fetcher for the entire game.
	 */
	private ResourceGetter res;
	/**
	 * The game window for actual playing.
	 */
	private GameWindow gameWindow;
	/**
	 * The credits.
	 */
	private Credits comp;
	/**
	 * This level will be loaded when the game starts.
	 */
	private String level;
	/**
	 * Available levels.
	 */
	private List<String> levels;
	/**
	 * List of available levels.
	 */
	private JList levelList;
	/**
	 * Level select screen.
	 */
	private JComponent levelSelect;
	/**
	 * Screen resolution.
	 */
	private int height;
	private int width;
	/**
	 * The settings for the game.
	 */
	private Settings set;
	/**
	 * Graphics options.
	 */
	private JComponent graphics;
	/**
	 * Swaps the graphics from full screen to windowed.
	 */
	private JButton swapGraphics;

	/**
	 * Sets the title and window parameters.
	 */
	public GEnterprise() {
		super("Gunther's Enterprise");
		set = new Settings("settings.ini");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setUndecorated(true);
		levels = new ArrayList<String>(32);
	}
	/**
	 * Creates the game window.
	 */
	public void start() {
		width = set.getInt("width", WIDTH);
		height = set.getInt("height", HEIGHT);
		res = JarResources.PARENT;
		LoadFrame frame = new LoadFrame(res);
		events = new EventListener();
		player = new MusicThread(res);
		setupUI();
		setupMenus();
		readLevels();
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
			new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(8, 8), "blank");
		player.load("click1.wav");
		player.load("watching.mp3");
		setIconImage(res.getImage("gunther.png"));
		gameWindow = new GameWindow(player);
		gameWindow.setCursor(blankCursor);
		comp = new Credits(this);
		comp.setCursor(blankCursor);
		frame.setVisible(false);
		frame.dispose();
		frame = null;
		setScreenSize();
		player.setLoop(true);
		player.start();
		player.queueMusic("watching.mp3");
	}
	/**
	 * Sets the display resolution to what the graphics preferences want.
	 */
	private void setScreenSize() {
		if (set.getInt("fullscreen", 1) != 0) {
			GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice();
			DisplayMode[] modes = dev.getDisplayModes();
			DisplayMode curMode = dev.getDisplayMode();
			int rate = curMode.getRefreshRate();
			int depth = curMode.getBitDepth();
			for (int i = 0; i < modes.length; i++)
				if (modes[i].getRefreshRate() == rate && modes[i].getBitDepth() == depth &&
					(modes[i].getWidth() == width || modes[i].getHeight() == height)) {
					curMode = modes[i];
					break;
				}
			dev.setFullScreenWindow(this);
			try {
				dev.setDisplayMode(curMode);
			} catch (Exception e) { }
		} else {
			setUndecorated(false);
			setSize(width, height);
			setVisible(true);
		}
	}
	/**
	 * Builds the menus on the screen.
	 */
	private void setupMenus() {
		main = new Menu(new String[] {
			"Single Mission", "Settings", "Exit"
		}, new String[] {
			"single", "settings", "exit"
		});
		main.setActionListener(events);
		settings = new Menu(new String[] {
			"Graphics", "Credits", "Back"
		}, new String[] {
			"graphics", "credits", "main"
		});
		settings.setActionListener(events);
		pause = new Menu(new String[] {
			"Return to Game","Exit to Menu", "Exit Game"
		}, new String[] {
			"unpause", "menu", "exit"
		});
		pause.setActionListener(events);
		pMenu = new Box(BoxLayout.Y_AXIS);
		pMenu.add(pause.layout());
		pMenu.add(Box.createVerticalStrut(200));
		setMenu(main);
	}
	/**
	 * Initializes other UI components.
	 */
	private void setupUI() {
		// headers and layout
		Container c = getContentPane();
		c.setBackground(Color.BLACK);
		c.setLayout(new BorderLayout(10, 10));
		header = new JLabel(res.getIcon("title.png"));
		header.setHorizontalAlignment(SwingConstants.CENTER);
		header.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
		header.setForeground(Color.WHITE);
		c.add(header, BorderLayout.NORTH);
		headerPause = new JLabel("Game Paused");
		headerPause.setHorizontalAlignment(SwingConstants.CENTER);
		headerPause.setFont(headerPause.getFont().deriveFont(56.f));
		headerPause.setBorder(header.getBorder());
		headerPause.setForeground(Color.WHITE);
		// list of levels
		levelList = new JList(new LevelListModel());
		levelList.setBackground(Color.BLACK);
		levelList.setForeground(Color.WHITE);
		levelList.setFont(levelList.getFont().deriveFont(18.f));
		levelList.setFocusable(false);
		levelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		levelList.addMouseListener(this);
		levelList.setSelectionBackground(Color.WHITE);
		levelList.setSelectionForeground(Color.BLACK);
		levelList.setPrototypeCellValue("00000000000000000000000000000000");
		JScrollPane sp = new JScrollPane(levelList);
		sp.setOpaque(false);
		sp.getViewport().setOpaque(false);
		sp.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		sp.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollBar bar = sp.getVerticalScrollBar();
		bar.setUI(new ScrollbarUI());
		// level select
		JLabel head = new JLabel("Select Level");
		head.setHorizontalAlignment(SwingConstants.CENTER);
		head.setFont(head.getFont().deriveFont(32.f));
		head.setForeground(Color.WHITE);
		head.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		JComponent vert = new Box(BoxLayout.Y_AXIS);
		vert.setOpaque(false);
		vert.add(head);
		vert.add(Box.createVerticalStrut(20));
		vert.add(sp);
		vert.add(Box.createVerticalStrut(20));
		JButton start = Menu.getButton("Start Game", "game");
		start.addActionListener(events);
		JButton back = Menu.getButton("Main Menu", "main");
		back.addActionListener(events);
		vert.add(start);
		vert.add(Box.createVerticalStrut(10));
		vert.add(back);
		levelSelect = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		levelSelect.setOpaque(false);
		levelSelect.add(vert);
		// graphics options
		graphics = new Box(BoxLayout.Y_AXIS);
		graphics.add(Box.createVerticalStrut(50));
		swapGraphics = Menu.getButton("Fullscreen: Yes", "fs");
		swapGraphics.addActionListener(events);
		graphics.add(swapGraphics);
		setFS();
		graphics.add(Box.createVerticalStrut(10));
		back = Menu.getButton("Options Menu", "settings");
		back.addActionListener(events);
		graphics.add(back);
		graphics.add(Box.createVerticalStrut(20));
		JLabel lbl = new JLabel("Changes take place on restart.");
		lbl.setForeground(Color.WHITE);
		lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		graphics.add(lbl);
		graphics.add(Box.createVerticalStrut(50));
	}
	/**
	 * Sets the current menu.
	 * 
	 * @param menu the new menu
	 */
	public void setMenu(Menu menu) {
		if (current != null)
			getContentPane().remove(current);
		menu.deselectAll();
		current = menu.layout();
		getContentPane().add(current, BorderLayout.CENTER);
		validate();
		repaint();
	}
	/**
	 * Shows graphics options.
	 */
	public void showGraphics() {
		if (current != null)
			getContentPane().remove(current);
		current = graphics;
		getContentPane().add(current, BorderLayout.CENTER);
		validate();
		repaint();
	}
	/**
	 * Sets the text on the button.
	 */
	private void setFS() {
		if (set.getInt("fullscreen", 1) != 0)
			swapGraphics.setText("Fullscreen: Yes");
		else
			swapGraphics.setText("Fullscreen: No");
	}
	/**
	 * Swaps full screen.
	 */
	private void swapFS() {
		if (set.getInt("fullscreen", 1) != 0)
			set.put("fullscreen", "0");
		else
			set.put("fullscreen", "1");
		setFS();
		set.writeOut();
	}
	/**
	 * Sets the screen to the level list.
	 */
	public void selectLevel() {
		levelList.setSelectedIndex(0);
		if (current != null)
			getContentPane().remove(current);
		current = levelSelect;
		getContentPane().add(levelSelect, BorderLayout.CENTER);
		validate();
		repaint();
	}
	/**
	 * Resets the whole screen.
	 */
	public void reset() {
		main.deselectAll();
		current = main.layout();
		getContentPane().add(current, BorderLayout.CENTER);
		getContentPane().add(header, BorderLayout.NORTH);
		validate();
		repaint();
	}
	/**
	 * Closes the program.
	 */
	public void close() {
		player.stopMusic();
		GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice();
		setVisible(false);
		dev.setFullScreenWindow(null);
		System.exit(0);
	}
	/**
	 * Runs the game!!!
	 */
	public void run() {
		if (level == null) level = "instr.dat";
		Container c = getContentPane();
		Utils.sleep(100L);
		c.removeAll();
		c.add(gameWindow, BorderLayout.CENTER);
		c.validate();
		gameWindow.action = set.getInt("action", KeyEvent.VK_A);
		gameWindow.stealth = set.getInt("stealth", KeyEvent.VK_S);
		gameWindow.movement = set.getInt("movement", KeyEvent.VK_D);
		gameWindow.pause = set.getInt("pause", KeyEvent.VK_ESCAPE);
		gameWindow.start(level);
		while (true) {
			// pause / un pause
			while (!gameWindow.paused) Utils.sleep(50L);
			player.startSFX("ping.wav");
			gameWindow.setVisible(false);
			if (!gameWindow.KILL && !gameWindow.done) {
				pause.deselectAll();
				c.add(headerPause, BorderLayout.NORTH);
				c.add(pMenu, BorderLayout.SOUTH);
				c.validate();
			}
			while (gameWindow.paused) {
				Utils.sleep(5L);
				if (gameWindow.KILL) {
					// close game
					c.remove(headerPause);
					c.remove(pMenu);
					c.validate();
					gameWindow.paused = false;
					gameWindow.setVisible(true);
					// give it a chance to dump textures
					while (!gameWindow.killed) Utils.sleep(50L);
					current = null;
					gameWindow.clear();
					// set up for the main window
					c.remove(gameWindow);
					c.validate();
					if (gameWindow.done)
						comp.start();
					else {
						reset();
						player.stopMusic();
						player.queueMusic("watching.mp3");
					}
					return;
				}
			}
			c.remove(headerPause);
			c.remove(pMenu);
			c.validate();
			gameWindow.setVisible(true);
			gameWindow.canvas.requestFocus();
		}
	}
	/**
	 * Unloads the game cleanly.
	 */
	private void unloadGame() {
		gameWindow.KILL = true;
	}
	/**
	 * Loads up the game in a new thread.
	 */
	private void playGame() {
		int index = levelList.getSelectedIndex();
		if (index < 0) return;
		level = levels.get(index);
		Thread t = new Thread(GEnterprise.this);
		t.setName("Pause and Menu Handler");
		t.setPriority(Thread.MIN_PRIORITY + 1);
		t.start();
	}
	/**
	 * Returns the music player thread.
	 * 
	 * @return a way to play music and sound effects
	 */
	public MusicThread getMusicThread() {
		return player;
	}
	/**
	 * Returns the system resource getter.
	 * 
	 * @return the system resource location
	 */
	public ResourceGetter getResources() {
		return res;
	}
	/**
	 * Reads the list of available levels. No SVN levels just yet.
	 */
	public void readLevels() {
		File svn = new File("branches/levels");
		if (!svn.exists()) {
			svn = new File("code/branches/levels");
			if (!svn.exists()) svn = null;
		}
		if (svn != null) svn = svn.getAbsoluteFile();
		File local = new File(".").getAbsoluteFile();
		levels.clear();
		if (svn != null) {
			//File[] svnLevels = svn.listFiles(LEVEL_FILTER);
			/*for (int i = 0; i < svnLevels.length; i++)
				levels.add("SVN " + svnLevels[i].getName());*/
			// no svn support yet //
		}
		File[] localLevels = local.listFiles(LEVEL_FILTER);
		for (int i = 0; i < localLevels.length; i++)
			levels.add(localLevels[i].getName());
		Collections.sort(levels);
	}

	/**
	 * Handles menu events and mouse events.
	 */
	private class EventListener extends KeyAdapter implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd == null) return;
			player.startSFX("click1.wav");
			if (cmd.equals("main"))
				setMenu(main);
			else if (cmd.equals("menu"))
				unloadGame();
			else if (cmd.equals("settings"))
				setMenu(settings);
			else if (cmd.equals("single"))
				selectLevel();
			else if (cmd.equals("game"))
				playGame();
			else if (cmd.equals("unpause"))
				gameWindow.paused = false;
			else if (cmd.equals("credits"))
				comp.start();
			else if (cmd.equals("fs"))
				swapFS();
			else if (cmd.equals("graphics"))
				showGraphics();
			else if (cmd.equals("exit"))
				close();
		}
		public void keyReleased(KeyEvent e) {
			
		}
	}

	/**
	 * A list model based on the loaded list of levels.
	 */ 
	private class LevelListModel extends AbstractListModel {
		private static final long serialVersionUID = 0L;

		public Object getElementAt(int index) {
			return levels.get(index);
		}
		public int getSize() {
			return levels.size();
		}
	}

	/**
	 * A class that filters only level (.dat) files.
	 */
	private static final class LevelFilter implements FileFilter {
		public boolean accept(File pathname) {
			String name = pathname.getName();
			return pathname.canRead() && !pathname.isHidden() && !pathname.isDirectory() &&
				!name.startsWith(".") && name.toLowerCase().endsWith(".dat");
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) playGame();
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
}