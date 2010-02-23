import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

/**
 * Miscellaneous level editor utilities.
 * 
 * @author Stephen
 */
public final class Utils {
	/**
	 * The image loader.
	 */
	private static final ImageIcon iconLoad = new ImageIcon();

	/**
	 * Copies the image to a buffered image.
	 * 
	 * @param img the image to copy
	 * @return the buffer image
	 */
	public static BufferedImage imageToBuffer(Image img) {
		if (img == null) return null;
		loadFully(img);
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		if (w < 0 || h < 0) return null;
		BufferedImage buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics g = buf.getGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return buf;
	}
	/**
	 * Loads the specified image fully.
	 * 
	 * @param img the image to load
	 */
	public synchronized static void loadFully(Image img) {
		if (img == null) return;
		iconLoad.setImage(img);
	}

	/**
	 * Not to be instantiated.
	 */
	private Utils() { }
}