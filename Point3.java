import java.awt.geom.*;

/**
 * Stephen Carlson
 * December 8, 2009
 * Period 6 3D Graphics
 * Mrs. Collins
 * 
 * A class that represents a point in 3D coordinate space.
 */
public class Point3 {
	/**
	 * The x coordinate.
	 */
	public double x;
	/**
	 * The y coordinate.
	 */
	public double y;
	/**
	 * The z coordinate.
	 */
	public double z;

	/**
	 * Creates a point at the origin.
	 */
	public Point3() {
		this(0, 0, 0);
	}
	/**
	 * Creates a point at the given location.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 */
	public Point3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	/**
	 * Gets the X and Y coordinates as a 2D point.
	 * 
	 * @return (x, y)
	 */
	public Point2D getXY() {
		return new Point2D.Double(x, y);
	}
	/**
	 * Gets the Y and Z coordinates as a 2D point.
	 * 
	 * @return (y, z)
	 */
	public Point2D getYZ() {
		return new Point2D.Double(y, z);
	}
	/**
	 * Gets the X and Z coordinates as a 2D point.
	 * 
	 * @return (x, z)
	 */
	public Point2D getXZ() {
		return new Point2D.Double(x, z);
	}
	/**
	 * Creates a vector from here to the destination.
	 * 
	 * @param dest the destination point
	 * @return &lt;x2 - x1, y2 - y1, z2 - z1&gt;
	 */
	public Vector3 createVector(Point3 dest) {
		return new Vector3(dest.getX() - x, dest.getY() - y, dest.getZ() - z);
	}
	/**
	 * Gets the X coordinate.
	 * 
	 * @return x
	 */
	public double getX() {
		return x;
	}
	/**
	 * Sets the X coordinate.
	 * 
	 * @param x the new x
	 */
	public void setX(double x) {
		this.x = x;
	}
	/**
	 * Gets the Y coordinate.
	 * 
	 * @return y
	 */
	public double getY() {
		return y;
	}
	/**
	 * Sets the Y coordinate.
	 * 
	 * @param y the new y
	 */
	public void setY(double y) {
		this.y = y;
	}
	/**
	 * Gets the Z coordinate.
	 * 
	 * @return z
	 */
	public double getZ() {
		return z;
	}
	/**
	 * Sets the Z coordinate.
	 * 
	 * @param z the new z
	 */
	public void setZ(double z) {
		this.z = z;
	}
	public boolean equals(Object other) {
		if (other == null || !(other instanceof Point3)) return false;
		Point3 p = (Point3)other;
		return p.x == x && p.y == y && p.z == z;
	}
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}
	public String toRoundedString() {
		return "(" + Utils.round1(x) + ", " + Utils.round1(y) + ", " + Utils.round1(z) + ")";
	}
}