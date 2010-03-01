import java.awt.Point;

/**
 * A class representing an instance of an element placed on the screen.
 * 
 * @author Stephen
 */
public class PlacedElement implements java.io.Serializable {
	private static final long serialVersionUID = 238146789346781281L;

	/**
	 * The X coordinate.
	 */
	private int x;
	/**
	 * The Y coordinate.
	 */
	private int y;
	/**
	 * The source element.
	 */
	private Element src;

	/**
	 * For serialization.
	 */
	public PlacedElement() {
		x = y = 0;
		src = null;
	}
	/**
	 * Places an instance of src at x, y.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param src the source block
	 */
	public PlacedElement(int x, int y, Element src) {
		this.x = x;
		this.y = y;
		this.src = src;
	}
	/**
	 * Gets the element to be rendered here.
	 * 
	 * @return the element
	 */
	public Element getSource() {
		return src;
	}
	/**
	 * Gets the X coordinate.
	 * 
	 * @return the x (first, width) coordinate.
	 */
	public int getX() {
		return x;
	}
	/**
	 * Gets the Y coordinate.
	 * 
	 * @return the y (second, height) coordinate.
	 */
	public int getY() {
		return y;
	}
	/**
	 * Gets the location.
	 * 
	 * @return the X and Y coordinates
	 */
	public Point getLocation() {
		return new Point(x, y);
	}
}