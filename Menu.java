import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

public class Menu extends JFrame {
	private static final long serialVersionUID = 0L;

	public static final Color BB = new Color(0, 0, 216);
	public static final Color BF = new Color(0, 0, 63);
	public static final Border BORDER = BorderFactory.createCompoundBorder(
		BorderFactory.createLineBorder(BB), BorderFactory.createEmptyBorder(5, 20, 5, 20));

	public static void main(String[] args) {
		new Menu().start();
	}

	public Menu() {
		super("Menu test");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setUndecorated(true);
	}
	public void start() {
		FilesystemResources res = new FilesystemResources(null, new java.io.File("res/"));
		GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		DisplayMode[] modes = dev.getDisplayModes();
		DisplayMode curMode = dev.getDisplayMode();
		int rate = curMode.getRefreshRate();
		int depth = curMode.getBitDepth();
		for (int i = 0; i < modes.length; i++)
			if (modes[i].getRefreshRate() == rate && modes[i].getBitDepth() == depth)
				System.out.println(modes[i].getWidth() + "x" + modes[i].getHeight());
		setBackground(Color.BLACK);
		setLayout(new BorderLayout(10, 10));
		JLabel header = new JLabel(res.getIcon("title.png"));
		header.setHorizontalAlignment(SwingConstants.CENTER);
		header.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
		header.setForeground(Color.WHITE);
		add(header, BorderLayout.NORTH);
		JComponent vert = new Box(BoxLayout.Y_AXIS);
		vert.add(getMenuItem("Campaign", "campaign"));
		vert.add(Box.createVerticalStrut(30));
		vert.add(getMenuItem("Single Mission", "mission"));
		vert.add(Box.createVerticalStrut(30));
		vert.add(getMenuItem("Options", "options"));
		vert.add(Box.createVerticalStrut(30));
		vert.add(getMenuItem("Exit", "exit"));
		add(vert, BorderLayout.CENTER);
		dev.setFullScreenWindow(this);
	}
	private JButton getMenuItem(String text, String code) {
		JButton myButton = new JButton(text);
		myButton.setBorder(BORDER);
		myButton.setFocusable(false);
		myButton.setForeground(Color.WHITE);
		myButton.setFont(myButton.getFont().deriveFont(Font.PLAIN, 26.f));
		//myButton.setContentAreaFilled(false);
		myButton.setOpaque(true);
		myButton.setBackground(BF);
		myButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		myButton.setPreferredSize(new Dimension(300, myButton.getPreferredSize().height));
		myButton.setMinimumSize(myButton.getPreferredSize());
		myButton.setMaximumSize(myButton.getPreferredSize());
		return myButton;
	}
}