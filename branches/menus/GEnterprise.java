import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Main runner for the entire program in a frame.
 *  All other classes are referenced, directly or indirectly, by this one.
 * 
 * @author Stephen
 */
public class GEnterprise extends JFrame implements Runnable {
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
	 * Sets the title and window parameters.
	 */
	public GEnterprise() {
		super("Gunther's Enterprise");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setUndecorated(true);
	}
	/**
	 * Creates the game window.
	 */
	public void start() {
		res = new FilesystemResources(null, new java.io.File("res/"));
		LoadFrame frame = new LoadFrame(res);
		events = new EventListener();
		player = new MusicThread(res);
		setupMenus();
		player.load("click1.wav");
		player.load("aoogahorn.wav");
		player.load("watching.mp3");
		gameWindow = new GameWindow(player);
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
		GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice();
		DisplayMode[] modes = dev.getDisplayModes();
		DisplayMode curMode = dev.getDisplayMode();
		int rate = curMode.getRefreshRate();
		int depth = curMode.getBitDepth();
		for (int i = 0; i < modes.length; i++)
			if (modes[i].getRefreshRate() == rate && modes[i].getBitDepth() == depth &&
				(modes[i].getWidth() == WIDTH || modes[i].getHeight() == HEIGHT)) {
				curMode = modes[i];
				break;
			}
		dev.setFullScreenWindow(this);
		try {
			dev.setDisplayMode(curMode);
		} catch (Exception e) { }
	}
	/**
	 * Builds the menus on the screen.
	 */
	private void setupMenus() {
		Container c = getContentPane();
		c.setBackground(Color.BLACK);
		c.setLayout(new BorderLayout(10, 10));
		header = new JLabel(res.getIcon("title.png"));
		header.setHorizontalAlignment(SwingConstants.CENTER);
		header.setFont(header.getFont().deriveFont(56.f));
		header.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
		header.setForeground(Color.WHITE);
		c.add(header, BorderLayout.NORTH);
		headerPause = new JLabel("Game Paused");
		headerPause.setHorizontalAlignment(SwingConstants.CENTER);
		headerPause.setFont(header.getFont());
		headerPause.setBorder(header.getBorder());
		headerPause.setForeground(Color.WHITE);
		main = new Menu(new String[] {
			"Campaign", "Single Mission", "Records", "Settings", "Exit"
		}, new String[] {
			"map", "game", "rec", "settings", "exit"
		});
		main.setActionListener(events);
		settings = new Menu(new String[] {
			"Game", "Keyboard", "Sound", "Graphics", "Back"
		}, new String[] {
			"options", "keyboard", "sound", "graphics", "main"
		});
		settings.setActionListener(events);
		pause = new Menu(new String[] {
			"Return to Game", "Keyboard Settings", "Sound", "Exit to Menu", "Exit Game"
		}, new String[] {
			"unpause", "keyboard", "sound", "menu", "exit"
		});
		pause.setActionListener(events);
		pMenu = new Box(BoxLayout.Y_AXIS);
		pMenu.add(pause.layout());
		pMenu.add(Box.createVerticalStrut(200));
		setMenu(main);
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
	 * Closes the program.
	 */
	public void close() {
		player.stopMusic();
		GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice();
		setVisible(false);
		// looks unnecessary for now
		/*try {
			dev.setDisplayMode(originalMode);
		} catch (Exception ex) { }*/
		dev.setFullScreenWindow(null);
		System.exit(0);
	}
	/**
	 * Runs the game!!!
	 */
	public void run() {
		Container c = getContentPane();
		Utils.sleep(100L);
		c.removeAll();
		c.add(gameWindow, BorderLayout.CENTER);
		c.validate();
		gameWindow.start();
		gameWindow.canvas.requestFocus();
		while (true) {
			// pause / un pause
			while (!gameWindow.paused) Utils.sleep(50L);
			gameWindow.setVisible(false);
			pause.deselectAll();
			c.add(headerPause, BorderLayout.NORTH);
			c.add(pMenu, BorderLayout.SOUTH);
			c.validate();
			while (gameWindow.paused) Utils.sleep(50L);
			c.remove(headerPause);
			c.remove(pMenu);
			c.validate();
			gameWindow.setVisible(true);
			gameWindow.canvas.requestFocus();
		}
	}

	/**
	 * Handles menu events and mouse events.
	 */
	private class EventListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd == null) return;
			player.startSFX("click1.wav");
			if (cmd.equals("main"))
				setMenu(main);
			else if (cmd.equals("settings"))
				setMenu(settings);
			else if (cmd.equals("game"))
				new Thread(GEnterprise.this).start();
			else if (cmd.equals("unpause"))
				gameWindow.paused = false;
			else if (cmd.equals("exit"))
				close();
		}
	}
}