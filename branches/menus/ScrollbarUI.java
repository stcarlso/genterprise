import javax.swing.*;
import javax.swing.plaf.basic.*;
import java.awt.*;

/**
 * A class which describes a UI for scroll bars such as those on the level select screen.
 * 
 * @author Stephen
 */
public class ScrollbarUI extends BasicScrollBarUI {
	protected void installDefaults() {
		super.installDefaults();
		scrollbar.setBorder(BorderFactory.createEmptyBorder());
		scrollbar.setForeground(Color.WHITE);
		scrollbar.setForeground(Color.BLACK);
		thumbHighlightColor = Color.BLACK;
		thumbLightShadowColor = Color.BLACK;
		thumbDarkShadowColor = Color.BLACK;
		thumbColor = Color.BLACK;
		trackColor = Color.BLACK;
		trackHighlightColor = Color.BLACK;
	}
	protected void installComponents() {
		super.installComponents();
		decrButton.setBorder(BorderFactory.createEmptyBorder());
		decrButton.setForeground(Color.WHITE);
		decrButton.setBackground(Color.BLACK);
		incrButton.setBorder(BorderFactory.createEmptyBorder());
		incrButton.setForeground(Color.WHITE);
		incrButton.setBackground(Color.BLACK);
	}
}