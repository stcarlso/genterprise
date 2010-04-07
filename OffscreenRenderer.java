import java.awt.image.*;
import java.io.*;
import javax.media.opengl.*;
import com.sun.opengl.util.texture.*;

/**
 * Renders images offscreen to textures.
 * 
 * @author Stephen
 */
public class OffscreenRenderer {
	/**
	 * The width of the texture.
	 */
	private int width;
	/**
	 * The height of the texture.
	 */
	private int height;
	/**
	 * The actual texture object.
	 */
	private Texture texture;

	/**
	 * Creates a new off screen renderer with the given attributes.
	 * 
	 * @param gl the OpenGL context
	 * @param width the texture width
	 * @param height the texture height
	 */
	public OffscreenRenderer(GL gl, int width, int height) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		texture = TextureIO.newTexture(img, false);
		this.width = width;
		this.height = height;
	}
	/**
	 * Sets this texture as the rendering target.
	 * 
	 * @param gl the OpenGL context
	 */
	public void setRender(GL gl) {
		gl.glPushAttrib(GL.GL_VIEWPORT_BIT | GL.GL_TEXTURE_BIT | GL.GL_COLOR_BUFFER_BIT);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		gl.glViewport(0, 0, width, height);
		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
	}
	/**
	 * Clears this texture from the rendering target.
	 * 
	 * @param gl the OpenGL context
	 */
	public void clearRender(GL gl) {
		texture.bind();
		gl.glCopyTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, 0, 0, width, height, 0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
		gl.glPopAttrib();
	}
	/**
	 * Cleans out this renderer if it won't be re-used.
	 */
	public void dispose() {
		texture.dispose();
		texture = null;
	}
	/**
	 * Writes the rendered picture to a file.
	 * 
	 * @param file the target file
	 */
	public void writeTexture(File file) {
		try {
			TextureIO.write(texture, file);
		} catch (Exception e) {
			Errors.sorry(e);
		}
	}
}