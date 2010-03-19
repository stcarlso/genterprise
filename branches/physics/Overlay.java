import java.awt.*;
import java.awt.image.*;
import javax.media.opengl.*;

import com.sun.opengl.util.texture.*;

/**
 * An overlay for JOGL windows based on textures.
 * 
 * @author Stephen Carlson
 */
public abstract class Overlay {
	/**
	 * The texture data holding the image.
	 */
	private TextureData data;
	/**
	 * The viewport, or null if none.
	 */
	private Rectangle view;
	/**
	 * The actual texture object.
	 */
	private Texture texture;
	/**
	 * The texture ID of the object.
	 */
	private int textureID;
	/**
	 * The image with the 2D graphics.
	 */
	private BufferedImage image;
	/**
	 * Whether painting needs to be done.
	 */
	private volatile boolean needsPainting;
	/**
	 * The graphics for drawing.
	 */
	private Graphics2D graphics;

	/**
	 * Creates a new overlay.
	 */
	public Overlay(int width, int height) {
		needsPainting = true;
		view = null;
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		buildGraphics();
		generateData();
		texture = TextureIO.newTexture(data);
		textureID = texture.getTextureObject();
	}
	/**
	 * Gets the texture used.
	 * 
	 * @return the texture
	 */
	public Texture getTexture() {
		return texture;
	}
	/**
	 * Creates the graphics object to be used for painting.
	 */
	private void buildGraphics() {
		if (graphics != null) graphics.dispose();
		graphics = image.createGraphics();
	}
	/**
	 * Remakes the texture data.
	 */
	private void generateData() {
		if (data != null) data.flush();
		data = new TextureData(0, 0, false, image);
		if (texture != null) texture.updateImage(data);
	}
	/**
	 * Sets the viewport for this overlay to the given coordinates.
	 * A null coordinate will use the entire screen.
	 * 
	 * @param rect the coordinates of the viewport.
	 * Note that these should be in OpenGL coordinates (with 0,0 at bottom left)
	 */
	public void setViewport(Rectangle rect) {
		if (rect == null)
			view = null;
		else
			view = new Rectangle(rect);
	}
	/**
	 * Sets the viewport for this overlay to the given coordinates.
	 * Note that these should be in OpenGL coordinates (with 0,0 at bottom left)
	 * 
	 * @param x the x coordinate of the viewport
	 * @param y the y coordinate of the viewport
	 * @param width the width of the viewport
	 * @param height the height of the viewport
	 */
	public void setViewport(int x, int y, int width, int height) {
		view = new Rectangle(x, y, width, height);
	}
	/**
	 * Sets the size of the overlay.
	 * 
	 * @param width the overlay width
	 * @param height the overlay height
	 */
	public void setSize(int width, int height) {
		image.flush();
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		buildGraphics();
	}
	/**
	 * Gets the width of this overlay.
	 * 
	 * @return the width
	 */
	public int getWidth() {
		return image.getWidth();
	}
	/**
	 * Gets the height of this overlay.
	 * 
	 * @return the height
	 */
	public int getHeight() {
		return image.getHeight();
	}
	/**
	 * Displays the overlay, using a GL viewport if necessary.
	 * 
	 * @param gl the 
	 */
	public void display(GL gl) {
		boolean paint = false;
		if (needsPainting) {
			paint = true;
			needsPainting = false;
		}
		if (paint) {
			// render
			paint(graphics);
			generateData();
		}
		// save state
		gl.glPushAttrib(GL.GL_CURRENT_BIT | GL.GL_TEXTURE_BIT | GL.GL_ENABLE_BIT | GL.GL_VIEWPORT_BIT);
		// viewport mode
		if (view != null)
			gl.glViewport(view.x, view.y, view.width, view.height);
		// turn off mipmapping (faster!)
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		gl.glTexEnvf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		// set state for drawing
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glDisable(GL.GL_LIGHTING);
		// save matrices
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrtho(0, 1, 0, 1, -1, 1);
		// bind texture
		gl.glColor4f(1, 1, 1, 1);
		gl.glBindTexture(GL.GL_TEXTURE_2D, textureID);
		// draw screen filling polygon
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, 1);
		gl.glVertex2f(0, 0);
		gl.glTexCoord2f(0, 0);
		gl.glVertex2f(0, 1);
		gl.glTexCoord2f(1, 0);
		gl.glVertex2f(1, 1);
		gl.glTexCoord2f(1, 1);
		gl.glVertex2f(1, 0);
		gl.glEnd();
		// restore state
		gl.glPopAttrib();
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPopMatrix();
	}
	/**
	 * Schedules this object for repainting. The next display() call will repaint.
	 */
	public void repaint() {
		needsPainting = true;
	}
	/**
	 * Clears the graphics (assumed to be from the image).
	 */
	protected void clear(Graphics2D g) {
		Color color = g.getColor();
		g.setColor(new Color(0, 0, 0, 0));
		// overwrite the graphics with transparent bits
		Composite composite = g.getComposite();
		g.setComposite(AlphaComposite.Src);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		// restore state
		g.setComposite(composite);
		g.setColor(color);
	}
	/**
	 * Draws the 2D portion of this overlay.
	 * 
	 * @param g the graphics used to draw into the overlay
	 */
	public abstract void paint(Graphics2D g);
}