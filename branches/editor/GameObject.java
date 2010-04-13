import java.util.*;

/**
 * A class representing an instance of an element placed on the screen.
 * 
 * @author Stephen
 */
public class GameObject implements java.io.Serializable, Comparable<GameObject> {
	private static final long serialVersionUID = 238146789346781281L;

	/**
	 * The coordinates.
	 */
	private Point3 coords;
	/**
	 * The rotation in degrees.
	 */
	private Vector3 rotation;
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
		this(0., 0., 0., new Vector3(), null);
	}
	/**
	 * Places an instance of src at x, y.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param rotation the rotation in degrees
	 * @param src the source block
	 */
	public GameObject(double x, double y, double z, Vector3 rotation, Element src) {
		coords = new Point3(x, y, z);
		this.src = src;
		this.rotation = rotation;
		attributes = null;
	}
	/**
	 * Compares this game object on depth. <b>Assumes the models are flat at z=0.</b>
	 */
	public int compareTo(GameObject o) {
		return (int)Math.round(Math.signum(o.coords.getZ() - coords.getZ()));
	}
	public boolean equals(Object o) {
		GameObject other = (GameObject)o;
		return src.equals(other.getSource()) && coords.equals(other.coords);
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
		if (value == null)
			attributes.remove(name);
		else
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
	 * Gets the amount of rotation.
	 * 
	 * @return the rotation in degrees
	 */
	public int getRotation() {
		return (int)Math.round(rotation.getZ());
	}
	/**
	 * Gets the rotation quaternion.
	 * 
	 * @return the rotation in all axes
	 */
	public Vector3 getRotFlip() {
		return rotation;
	}
	/**
	 * Gets the X coordinate.
	 * 
	 * @return the x (first, width) coordinate.
	 */
	public double getX() {
		return coords.getX();
	}
	/**
	 * Gets the Y coordinate.
	 * 
	 * @return the y (second, height) coordinate.
	 */
	public double getY() {
		return coords.getY();
	}
	/**
	 * Gets the Z coordinate.
	 * 
	 * @return the z (third, depth) coordinate.
	 */
	public double getZ() {
		return coords.getZ();
	}
	/**
	 * Gets the location.
	 * 
	 * @return the 3D location
	 */
	public Point3 getLocation() {
		return coords;
	}
	public String toString() {
		return src.toString() + "@" + coords.toString();
	}
}