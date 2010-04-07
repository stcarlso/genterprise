import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

/**
 * Builds 32x32 icons from disk images. Also flattens GL images to icons and writes them.
 * 
 * @author Stephen
 */
public class IconBuilder {
	/**
	 * The actual icon size!
	 */
	public static final int ICON_SIZE = 32;

	/**
	 * The resource getter to fetch disk icons.
	 */
	private ResourceGetter res;

	/**
	 * Creates a new icon builder that will fetch from the specified resource getter.
	 * 
	 * @param get the resource
	 */
	public IconBuilder(ResourceGetter get) {
		res = get;
	}
	/**
	 * Gets a preview icon for a given element.
	 * 
	 * @param src the icon source
	 * @return a 32x32 icon of this object. <i>The 32x32 icon is not cached, unlike the source which is.</i>
	 */
	public Icon getPreviewIcon(Element src) {
		return getPreviewIcon(src.getName() + ".png");
	}
	/**
	 * Gets a preview icon.
	 * 
	 * @param src the icon source
	 * @return a 32x32 icon of this object. <i>The 32x32 icon is not cached, unlike the source which is.</i>
	 */
	public Icon getPreviewIcon(String src) {
		Icon ico = res.getIcon(src);
		if (ico.getIconHeight() == ICON_SIZE && ico.getIconWidth() == ICON_SIZE)
			return ico;
		Image img = res.getImage(src);
		Image newImage = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics g = newImage.getGraphics();
		g.drawImage(img, 0, 0, ICON_SIZE, ICON_SIZE, null);
		g.dispose();
		newImage.flush();
		return new ImageIcon(newImage);
	}
}