import java.awt.geom.*;
import java.util.*;

/**
 * A class representing a large zone in a level with multiple enemies and elements.
 * 
 * @author Stephen
 */
public class Block implements java.io.Serializable {
	private static final long serialVersionUID = 238146789346781279L;

	/**
	 * The bounding rectangle of this zone.
	 */
	private transient Rectangle2D bounds;
	/**
	 * Whether bounds have been computed.
	 */
	private transient boolean boundsDirty;
	/**
	 * The list of elements in this block.
	 */
	private List<GameObject> elements;

	/**
	 * For serialization.
	 */
	public Block() {
		bounds = new Rectangle2D.Float(0.f, 0.f, 0.f, 0.f);
		elements = new ArrayList<GameObject>(64);
		boundsDirty = true;
	}
	/**
	 * Creates a new level block.
	 * 
	 * @param elements the elements in this block
	 */
	public Block(List<GameObject> elements) {
		this.elements = elements;
		boundsDirty = true;
		computeBounds();
	}
	/**
	 * Computes the bounds of this block.
	 */
	public synchronized void computeBounds() {
		if (!boundsDirty) return;
		double minX = -Double.MAX_VALUE, minY = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE, maxY = Double.MAX_VALUE;
		double x, y;
		for (GameObject el : elements) {
			x = el.getX();
			y = el.getY();
			if (minX > x) minX = x;
			if (minY > y) minY = y;
			if (maxX < x) maxX = x;
			if (maxY < y) maxY = y;
		}
		if (elements.size() == 0)
			bounds = new Rectangle2D.Double(0., 0., 0., 0.);
		else
			bounds = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
		boundsDirty = false;
	}
	/**
	 * Gets the bounds of this block.
	 * 
	 * @return the bounds
	 */
	public Rectangle2D getBounds() {
		return bounds;
	}
	/**
	 * Checks to see if the location is in bounds.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return whether the location is inside this block
	 */
	public boolean inBounds(int x, int y) {
		computeBounds();
		return bounds.contains(x, y);
	}
	/**
	 * Checks to see if the location is in bounds.
	 * 
	 * @param xy the location as a point
	 * @return whether the location is inside this block
	 */
	public boolean inBounds(Point2D xy) {
		computeBounds();
		return bounds.contains(xy);
	}
	/**
	 * Checks to see if the rectangle is in bounds.
	 * 
	 * @param rect the bounding rectangle of the target
	 * @return whether any part of the rectangle is inside this block
	 */
	public boolean inBounds(Rectangle2D rect) {
		computeBounds();
		return bounds.intersects(rect);
	}
	/**
	 * Gets the upper left coordinate.
	 * 
	 * @return the upper left coordinate
	 */
	public Point2D getUpperLeft() {
		computeBounds();
		return new Point2D.Double(bounds.getX(), bounds.getY());
	}
	/**
	 * Gets the minimum X extent.
	 * 
	 * @return the minimum X coordinate
	 */
	public double getMinX() {
		computeBounds();
		return bounds.getMinX();
	}
	/**
	 * Gets the minimum Y extent.
	 * 
	 * @return the minimum Y coordinate
	 */
	public double getMinY() {
		computeBounds();
		return bounds.getMinY();
	}
	/**
	 * Gets the maximum X extent.
	 * 
	 * @return the maximum X coordinate
	 */
	public double getMaxX() {
		computeBounds();
		return bounds.getMaxX();
	}
	/**
	 * Gets the maximum Y extent.
	 * 
	 * @return the maximum Y coordinate
	 */
	public double getMaxY() {
		computeBounds();
		return bounds.getMaxY();
	}
	/**
	 * Gets the elements contained by this block.
	 * 
	 * @return the elements
	 */
	public List<GameObject> getElements() {
		return elements;
	}
}