import java.awt.geom.*;
import java.util.*;

/**
 * A class representing an instance of an element placed on the screen.
 * 
 * @author Stephen
 */
public class GameObject implements java.io.Serializable {
	private static final long serialVersionUID = 238146789346781281L;

	/**
	 * The X coordinate.
	 */
	private double x;
	/**
	 * The Y coordinate.
	 */
	private double y;
	/**
	 * The source element.
	 */
	private Element src;
	/**
	 * Arbitrary attributes.
	 */
	private Map<String, String> attributes;

	/**
	 * For serialization.
	 */
	public GameObject() {
		this(0, 0, null);
	}
	/**
	 * Places an instance of src at x, y.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param src the source block
	 */
	public GameObject(double x, double y, Element src) {
		this.x = x;
		this.y = y;
		this.src = src;
		attributes = null;
	}
	/**
	 * Gets the attribute specified by name.
	 * 
	 * @param name the attribute name to get
	 * @return the attribute, or null if unset/not found
	 */
	public String getAttribute(String name) {
		if (attributes == null) return null;
		return attributes.get(name);
	}
	/**
	 * Gets the attribute specified by name.
	 * 
	 * @param name the attribute name to get
	 * @param def the default value
	 * @return the attribute, or def if unset/not found
	 */
	public String getAttribute(String name, String def) {
		String value = getAttribute(name);
		if (value == null) return def;
		return value;
	}
	/**
	 * Changes the attribute specified by name.
	 * 
	 * @param name the attribute name to change
	 * @param value the new value
	 */
	public void putAttribute(String name, String value) {
		if (attributes == null) attributes = new HashMap<String, String>(32);
		attributes.put(name, value);
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
	public double getX() {
		return x;
	}
	/**
	 * Gets the Y coordinate.
	 * 
	 * @return the y (second, height) coordinate.
	 */
	public double getY() {
		return y;
	}
	/**
	 * Gets the location.
	 * 
	 * @return the X and Y coordinates
	 */
	public Point2D getLocation() {
		return new Point2D.Double(x, y);
	}
}