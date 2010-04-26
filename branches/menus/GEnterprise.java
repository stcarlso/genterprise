import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GEnterprise extends JFrame {
	private static final long serialVersionUID = 0L;

	public static final int WIDTH = 1024;
	public static final int HEIGHT = 768;

	public static void main(String[] args) {
		Utils.staticInit();
		new GEnterprise().start();
	}

	private EventListener events;
	private DisplayMode originalMode;
	private JComponent current;
	private Menu main;
	private Menu pause;
	private Menu settings;
	private MusicThread player;
	private ResourceGetter res;

	public GEnterprise() {
		super("Gunther's Enterprise");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setUndecorated(true);
	}
	public void start() {
		res = new FilesystemResources(null, new java.io.File("SFX/"));
		events = new EventListener();
		player = new MusicThread(res);
		GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice();
		DisplayMode[] modes = dev.getDisplayModes();
		DisplayMode curMode = dev.getDisplayMode();
		originalMode = curMode;
		int rate = curMode.getRefreshRate();
		int depth = curMode.getBitDepth();
		for (int i = 0; i < modes.length; i++)
			if (modes[i].getRefreshRate() == rate && modes[i].getBitDepth() == depth &&
				(modes[i].getWidth() == WIDTH || modes[i].getHeight() == HEIGHT)) {
				curMode = modes[i];
				break;
			}
		Container c = getContentPane();
		c.setBackground(Color.BLACK);
		c.setLayout(new BorderLayout(10, 10));
		JLabel header = new JLabel("Gunther's Enterprise");
		header.setHorizontalAlignment(SwingConstants.CENTER);
		header.setFont(header.getFont().deriveFont(56.f));
		header.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
		header.setForeground(Color.WHITE);
		c.add(header, BorderLayout.NORTH);
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
			"game", "keyboard", "sound", "menu", "exit"
		});
		pause.setActionListener(events);
		setMenu(main);
		player.load("click1.wav");
		try {
			dev.setDisplayMode(curMode);
		} catch (Exception e) { }
		dev.setFullScreenWindow(this);
		player.start();
	}
	public void setMenu(Menu menu) {
		if (current != null)
			getContentPane().remove(current);
		menu.deselectAll();
		current = menu.layout();
		getContentPane().add(current, BorderLayout.CENTER);
		validate();
		repaint();
	}
	public void close() {
		GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice();
		dev.setFullScreenWindow(null);
		try {
			dev.setDisplayMode(originalMode);
		} catch (Exception ex) { }
		System.exit(0);
	}

	private class EventListener extends MouseAdapter implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd == null) return;
			player.startSFX("click1.wav");
			if (cmd.equals("main"))
				setMenu(main);
			else if (cmd.equals("settings"))
				setMenu(settings);
			else if (cmd.equals("exit")) {
				close();
			}
		}
	}
}