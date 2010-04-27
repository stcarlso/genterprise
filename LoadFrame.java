import java.awt.*;
import javax.swing.*;

/**
 * Displays the loading message.
 * 
 * @author Stephen
 */
public class LoadFrame extends JFrame {
	private static final long serialVersionUID = 0L;

	/**
	 * Creates a loading frame.
	 * 
	 * @param res the resource fetcher for the load image
	 */
	public LoadFrame(ResourceGetter res) {
		super("Loading Gunther's Enterprise");
		setUndecorated(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		Container c = getContentPane();
		c.setBackground(Color.BLACK);
		c.add(new JLabel(res.getIcon("genterprise.png")));
		pack();
		Utils.centerWindow(this);
		setVisible(true);
	}
}