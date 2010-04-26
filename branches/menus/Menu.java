import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

public class Menu {
	/**
	 * Button border color when not pressed.
	 */
	public static final Color BUT_BORDER = new Color(0, 0, 216);
	/**
	 * Button background color.
	 */
	public static final Color BUT_BG = new Color(0, 0, 63);
	/**
	 * Button border color when pressed.
	 */
	public static final Color BUT_PRESS = new Color(255, 255, 0);
	/**
	 * Button border when not pressed.
	 */
	public static final Border BORDER = BorderFactory.createCompoundBorder(
		BorderFactory.createLineBorder(BUT_BORDER), BorderFactory.createEmptyBorder(5, 20, 5, 20));
	/**
	 * Button border when pressed.
	 */
	public static final Border PBORDER = BorderFactory.createCompoundBorder(
		BorderFactory.createLineBorder(BUT_PRESS), BorderFactory.createEmptyBorder(5, 20, 5, 20));

	/**
	 * Listens for mouse button events.
	 */
	private EventListener events;
	/**
	 * Listens for clicks on buttons.
	 */
	private ActionListener listener;
	/**
	 * The actual menus.
	 */
	private JComponent menu;
	/**
	 * Each button is stored in this array.
	 */
	private JButton[] menuButtons;

	/**
	 * Creates a menu with the specified choices.
	 * 
	 * @param choices the list of choices
	 * @param commands the command triggered on choice
	 */
	public Menu(String[] choices, String[] commands) {
		if (choices == null || commands == null)
			throw new NullPointerException("commands and choices cannot be null");
		if (choices.length != commands.length)
			throw new IndexOutOfBoundsException("lengths of arrays do not match");
		events = new EventListener();
		menu = new Box(BoxLayout.Y_AXIS);
		// add all choices
		int n = choices.length;
		menuButtons = new JButton[n];
		for (int i = 0; i < n; i++) {
			menuButtons[i] = getMenuItem(choices[i], commands[i]);
			menu.add(menuButtons[i]);
			if (i < n - 1)
				menu.add(Box.createVerticalStrut(30));
		}
	}
	/**
	 * Registers an action listener for the menu buttons.
	 * 
	 * @param listener the listener to register
	 */
	public void setActionListener(ActionListener listener) {
		this.listener = listener;
	}
	/**
	 * Returns a component with all of the menu items in the correct orientation.
	 * 
	 * @return the menu items
	 */
	public JComponent layout() {
		return menu;
	}
	/**
	 * Deselects all items.
	 */
	public void deselectAll() {
		for (int i = 0; i < menuButtons.length; i++) {
			menuButtons[i].setBorder(BORDER);
			menuButtons[i].setIcon(null);
		}
	}
	/**
	 * Gets a button for the menu.
	 * 
	 * @param text the text on the button
	 * @param code the action code
	 * @return the button
	 */
	protected JButton getMenuItem(String text, String code) { 
		JButton myButton = new JButton(text);
		myButton.setUI(new ButtonUI());
		myButton.setBorder(BORDER);
		myButton.setFocusable(false);
		myButton.setActionCommand(code);
		myButton.setForeground(Color.WHITE);
		myButton.setFont(myButton.getFont().deriveFont(Font.PLAIN, 26.f));
		myButton.setOpaque(true);
		myButton.setBackground(BUT_BG);
		myButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		myButton.setPreferredSize(new Dimension(300, myButton.getPreferredSize().height));
		myButton.setMinimumSize(myButton.getPreferredSize());
		myButton.setMaximumSize(myButton.getPreferredSize());
		myButton.addMouseListener(events);
		myButton.addActionListener(events);
		myButton.setHorizontalTextPosition(SwingConstants.CENTER);
		myButton.setVerticalTextPosition(SwingConstants.CENTER);
		return myButton;
	}

	private class EventListener extends MouseAdapter implements ActionListener {
		public void mouseEntered(MouseEvent e) {
			JButton src = (JButton)e.getSource();
			src.setBorder(PBORDER);
		}
		public void mouseExited(MouseEvent e) {
			JButton src = (JButton)e.getSource();
			src.setBorder(BORDER);
		}
		public void mousePressed(MouseEvent e) {
		}
		public void mouseReleased(MouseEvent e) {
		}
		public void actionPerformed(ActionEvent e) {
			if (listener != null) listener.actionPerformed(e);
		}
	}
}