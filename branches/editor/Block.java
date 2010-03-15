import java.awt.Rectangle;
import java.awt.Point;
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
	private transient Rectangle bounds;
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
		bounds = new Rectangle(0, 0, 0, 0);
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
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
		int x, y;
		for (GameObject el : elements) {
			x = el.getX();
			y = el.getY();
			if (minX > x) minX = x;
			if (minY > y) minY = y;
			if (maxX < x) maxX = x;
			if (maxY < y) maxY = y;
		}
		if (elements.size() == 0)
			bounds = new Rectangle(0, 0, 0, 0);
		else
			bounds = new Rectangle(minX, minY, maxX - minX, maxY - minY);
		boundsDirty = false;
	}
	/**
	 * Gets the bounds of this block.
	 * 
	 * @return the bounds
	 */
	public Rectangle getBounds() {
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
	public boolean inBounds(Point xy) {
		computeBounds();
		return bounds.contains(xy);
	}
	/**
	 * Checks to see if the rectangle is in bounds.
	 * 
	 * @param rect the bounding rectangle of the target
	 * @return whether any part of the rectangle is inside this block
	 */
	public boolean inBounds(Rectangle rect) {
		computeBounds();
		return bounds.intersects(rect);
	}
	/**
	 * Gets the upper left coordinate.
	 * 
	 * @return the upper left coordinate
	 */
	public Point getUpperLeft() {
		computeBounds();
		return new Point(bounds.x, bounds.y);
	}
	/**
	 * Gets the minimum X extent.
	 * 
	 * @return the minimum X coordinate
	 */
	public int getMinX() {
		computeBounds();
		return bounds.x;
	}
	/**
	 * Gets the minimum Y extent.
	 * 
	 * @return the minimum Y coordinate
	 */
	public int getMinY() {
		computeBounds();
		return bounds.y;
	}
	/**
	 * Gets the maximum X extent.
	 * 
	 * @return the maximum X coordinate
	 */
	public int getMaxX() {
		computeBounds();
		return bounds.x + bounds.width;
	}
	/**
	 * Gets the maximum Y extent.
	 * 
	 * @return the maximum Y coordinate
	 */
	public int getMaxY() {
		computeBounds();
		return bounds.y + bounds.height;
	}
}