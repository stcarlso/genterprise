/**
 * Stephen Carlson
 * December 8, 2009
 * Period 6 3D Graphics
 * Mrs. Collins
 * 
 * A class that represents a 3D vector.
 */
public class Vector3 {
	/**
	 * The x component.
	 */
	public double x;
	/**
	 * The y component.
	 */
	public double y;
	/**
	 * The z component.
	 */
	public double z;

	/**
	 * Creates the zero vector.
	 */
	public Vector3() {
		this(0, 0, 0);
	}
	/**
	 * Creates a vector with the given components.
	 * 
	 * @param x the x component
	 * @param y the y component
	 * @param z the z component
	 */
	public Vector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	/**
	 * Gets the magnitude (rho). <i>Use lengthSq whenever possible.</i>
	 * 
	 * @return sqrt(x^2 + y^2 + z^2)
	 */
	public double length() {
		return Math.sqrt(lengthSq());
	}
	/**
	 * Gets the length squared. Fast!
	 * 
	 * @return x^2 + y^2 + z^2
	 */
	public double lengthSq() {
		return x * x + y * y + z * z;
	}
	/**
	 * Gets the xy angle (theta).
	 * 
	 * @return the angle between the vector and the +x axis
	 */
	public double theta() {
		return Math.atan2(y, x);
	}
	/**
	 * Gets the xy angle (phi).
	 * 
	 * @return the angle between the vector and the +y axis
	 */
	public double phi() {
		return Math.atan2(Math.hypot(x, y), z);
	}
	/**
	 * Returns the dot product of this vector with another.
	 * 
	 * @param other the other vector
	 * @return x*x + y*y + z*z
	 */
	public double dot(Vector3 other) {
		return x * other.x + y * other.y + z * other.z;
	}
	/**
	 * Returns the component of this vector in the direction of the other.
	 * 
	 * @param other the other vector
	 * @return dot(a, b) / mag(b)
	 */
	public double component(Vector3 other) {
		return dot(other) / other.length();
	}
	/**
	 * Returns the cosine of the angle between this vector and the other.
	 * 
	 * @param other the other vector
	 * @return dot(a, b) / (mag(a)*mag(b))
	 */
	public double cosAngle(Vector3 other) {
		return component(other) / length();
	}
	/**
	 * Returns a vector pointing in the opposite direction with the same length.
	 * 
	 * @return &lt;-x, -y, -z&gt;
	 */
	public Vector3 negate() {
		return new Vector3(-x, -y, -z);
	}
	/**
	 * Returns a unit vector pointing in the same direction as this one.
	 * 
	 * @return a / mag(a)
	 */
	public Vector3 unit() {
		double mag = length();
		return new Vector3(x / mag, y / mag, z / mag);
	}
	/**
	 * Returns the cross product of this vector with another.
	 * 
	 * @param other the other vector
	 * @return a x b
	 */
	public Vector3 cross(Vector3 other) {
		return new Vector3(y * other.z - z * other.y, z * other.x - x * other.z,
			x * other.y - y * other.x);
	}
	/**
	 * Returns the sine of the angle between this vector and the other.
	 * 
	 * @param other the other vector
	 * @return (a x b) / (mag(a) * mag(b))
	 */
	public double sinAngle(Vector3 other) {
		return length() * other.length() / cross(other).length();
	}
	/**
	 * Creates a point with the same values as this vector's components.
	 * 
	 * @return the endpoint of this vector (x, y, z)
	 */
	public Point3 asPoint() {
		return new Point3(x, y, z);
	}
	/**
	 * Gets the X component.
	 * 
	 * @return x
	 */
	public double getX() {
		return x;
	}
	/**
	 * Sets the X component.
	 * 
	 * @param x the new x
	 */
	public void setX(double x) {
		this.x = x;
	}
	/**
	 * Gets the Y component.
	 * 
	 * @return y
	 */
	public double getY() {
		return y;
	}
	/**
	 * Sets the Y component.
	 * 
	 * @param y the new y
	 */
	public void setY(double y) {
		this.y = y;
	}
	/**
	 * Gets the Z component.
	 * 
	 * @return z
	 */
	public double getZ() {
		return z;
	}
	/**
	 * Sets the Z component.
	 * 
	 * @param z the new z
	 */
	public void setZ(double z) {
		this.z = z;
	}
	public boolean equals(Object other) {
		if (other == null || !(other instanceof Vector3)) return false;
		Vector3 p = (Vector3)other;
		return p.x == x && p.y == y && p.z == z;
	}
	public String toString() {
		return "<" + x + ", " + y + ", " + z + ">";
	}
}