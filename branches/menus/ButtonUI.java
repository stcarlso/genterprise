import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

/**
 * Ensures that buttons depress when clicked.
 * 
 * @author Stephen
 */
public class ButtonUI extends BasicButtonUI {
	protected void installDefaults(AbstractButton b) {
		super.installDefaults(b);
		defaultTextShiftOffset = 1;
	}
	protected void paintButtonPressed(Graphics g, AbstractButton b) {
		setTextShiftOffset();
	}
}