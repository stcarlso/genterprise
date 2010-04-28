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
		getRootPane().putClientProperty("Window.shadow", Boolean.FALSE);
		setUndecorated(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		Container c = getContentPane();
		c.setBackground(Color.BLACK);
		JLabel lbl = new JLabel(res.getIcon("genterprise.png"));
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.setVerticalAlignment(SwingConstants.CENTER);
		lbl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		c.add(lbl);
		pack();
		Utils.centerWindow(this);
		setVisible(true);
	}
}