import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * A class that displays the credits. Woo hoo.
 * 
 * @author Stephen
 */
public class Credits extends JComponent implements Runnable, KeyListener {
	private static final long serialVersionUID = 0L;

	/**
	 * Initial text typed out on the screen in monospace.
	 */
	private static final String INITIAL = "Credits comp = new Credits();\nremoveAll();\n" +
		"add(comp, BorderLayout.CENTER);\ncomp.start();\n";
	/**
	 * Space between lines.
	 */
	private static final int SP = 0;

	/**
	 * All of the credits text from the file.
	 */
	private String[] allText;
	/**
	 * Mono spaced font.
	 */
	private Font monospace;
	/**
	 * The music source.
	 */
	private MusicThread music;
	/**
	 * The main menu.
	 */
	private GEnterprise menu;
	/**
	 * Font sizes for the current component.
	 */
	private FontMetrics fm;
	/**
	 * Font sizes for the monospace font on the current component.
	 */
	private FontMetrics fmC;
	/**
	 * The timing counter for the credits.
	 */
	private volatile int t;

	/**
	 * Reads the credit text and starts scrolling.
	 * 
	 * @param parent the component to notify
	 */
	public Credits(GEnterprise parent) {
		ResourceGetter res = parent.getResources();
		menu = parent;
		music = parent.getMusicThread();
		String text = res.getString("credits.txt");
		if (text == null || text.length() < 0)
			throw new IndexOutOfBoundsException("No credits");
		allText = text.split("\n");
		monospace = new Font("Monospaced", Font.PLAIN, 20);
		fmC = getFontMetrics(monospace);
		setFont(new Font("Sans", Font.PLAIN, 26));
		fm = getFontMetrics(getFont());
		addKeyListener(this);
		setFocusable(true);
	}
	/**
	 * Starts scrolling.
	 */
	public void start() {
		Thread t = new Thread(this);
		t.setName("Scroll Credits");
		t.setPriority(Thread.MIN_PRIORITY);
		t.setDaemon(true);
		t.start();
	}
	public void paint(Graphics g) {
		int t0 = t;
		Graphics g2 = g.create();
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getWidth(), getHeight());
		if (t0 >= Integer.MAX_VALUE || t0 < 0) return;
		g2.setColor(Color.WHITE);
		g2.setFont(monospace);
		if (t0 < 60) {
			g2.drawString("_", 5, 5 + fmC.getHeight() + SP);
			return;
		} else t0 -= 60;
		String start;
		if (t0 / 2 > INITIAL.length()) {
			start = INITIAL;
			g2.translate(0, 2 * INITIAL.length() - t0);
		} else start = INITIAL.substring(0, t0 / 2) + "_";
		String[] text = start.split("\n");
		int last = renderText(g2, 5, fmC.getHeight() + SP, text, SwingConstants.LEFT);
		if (t0 / 2 > INITIAL.length()) {
			g2.setFont(getFont());
			renderText(g2, last, fm.getHeight() + SP, allText, SwingConstants.CENTER);
		}
		g2.dispose();
	}
	public void update(Graphics g) {
		paint(g);
	}
	/**
	 * Draws the given lines of text.
	 * 
	 * @param g the graphics context
	 * @param y the starting y coordinate
	 * @param height the height of each line
	 * @param text the lines to draw
	 * @param where the orientation (center or left)
	 * @return where the last line was rendered
	 */
	private int renderText(Graphics g, int y, int height, String[] text, int where) {
		int x;
		y += height;
		for (int i = 0; i < text.length; i++) {
			if (where == SwingConstants.CENTER)
				x = (getWidth() - fm.stringWidth(text[i])) / 2;
			else if (where == SwingConstants.RIGHT)
				x = getWidth() - fm.stringWidth(text[i]);
			else
				x = 5;
			g.drawString(text[i], x, y);
			y += height;
		}
		return y;
	}
	public void run() {
		int N = 60 + 2 * INITIAL.length() + (SP + fm.getHeight()) * (allText.length + 1)
			+ (SP + fmC.getHeight()) * (INITIAL.split("\n").length + 1);
		Utils.sleep(50L);
		music.stopMusic();
		music.setLoop(true);
		music.queueMusic("watching.mp3");
		menu.getContentPane().removeAll();
		menu.getContentPane().add(this, BorderLayout.CENTER);
		menu.validate();
		menu.repaint();
		menu.requestFocus();
		Utils.sleep(50L);
		t = 0;
		requestFocus();
		while (t < N && t >= 0) {
			Utils.sleep(15L);
			t++;
			repaint();
		}
		menu.getContentPane().removeAll();
		menu.reset();
	}
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {
		if (t > 60 || t < 0)
			t = Integer.MAX_VALUE - 10;
	}
	public void keyTyped(KeyEvent e) {}
}