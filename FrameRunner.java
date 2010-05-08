/*
 * Basic Frame
 */

import javax.swing.*;

public class FrameRunner extends JFrame {
	private static final long serialVersionUID = 0L;

	private GameWindow game;

	public FrameRunner() {
		super("Gunther's Enterprise Test Window");
	}
	public void start() {
		game = new GameWindow();
		setSize(800,800);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().add(game);
		setVisible(true);
		game.start("peter.dat");
	}
	public static void main(String[] args) {
		Utils.staticInit();
		FrameRunner runner = new FrameRunner();
		runner.start();
	}
}
