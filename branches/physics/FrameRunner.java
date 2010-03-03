/*
 * Basic Frame
 */

import javax.swing.*;

public class FrameRunner extends JFrame {
	private static GameWindow game;
	public FrameRunner () {
		game= new GameWindow();
		setSize(800,800);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	public static void main(String[] args) {
		FrameRunner runner= new FrameRunner();
		runner.getContentPane().add(game);
		runner.setVisible(true);
	}
}
