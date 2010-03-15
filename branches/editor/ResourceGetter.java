import java.util.*;
import com.sun.opengl.util.texture.*;
import java.awt.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;

/**
 * Fetches resources for a level. They can be from JARs, memory, or the file system. 
 * 
 * @author Stephen
 */
public abstract class ResourceGetter {
	/**
	 * Cache of AWT images.
	 */
	private transient Map<String, Image> imageCache;
	/**
	 * Cache of JOGL images (textures).
	 */
	private transient Map<String, Texture> textureCache;
	/**
	 * The parent resource getter.
	 */
	protected transient ResourceGetter parent;

	/**
	 * Called by subclasses (implicitly) to initialize resource caches.
	 */
	protected ResourceGetter(ResourceGetter parent) {
		this.parent = parent;
		imageCache = new HashMap<String, Image>(256);
		textureCache = new HashMap<String, Texture>(256);
	}
	/**
	 * Subclasses will provide a stream where data can be read.
	 * 
	 * @param src the resource to get
	 * @return the resource stream
	 */
	protected abstract InputStream openResource(String src) throws IOException;
	/**
	 * Gets a stream reference to the data. Good for huge stuff.
	 *  Subclasses may want to provide a more efficient implementation.
	 * 
	 * @param src the resource to get
	 * @return the resource as a stream
	 */
	public synchronized InputStream getResource(String src) {
		try {
			return openResource(src);
		} catch (IOException e) {
			return null;
		}
	}
	/**
	 * Gets a serialized object from the resource.
	 *  Subclasses may want to provide a more efficient implementation.
	 * 
	 * @param src the resource to get
	 * @return the resource as a stream
	 */
	public synchronized Object getObject(String src) {
		try {
			ObjectInputStream ois = new ObjectInputStream(openResource(src));
			Object o = ois.readObject();
			ois.close();
			return o;
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * Loads a small resource as a byte array.
	 *  If you run out of memory, it will return null.
	 *  Subclasses may want to provide a more efficient implementation.
	 * 
	 * @param src the resource to get
	 * @return the full resource as a byte array
	 */
	public synchronized byte[] getBinary(String src) {
		try {
			InputStream in = openResource(src);
			if (in == null) return null;
			// fast block copy copies 1024 bytes at a time to memory
			ByteArrayOutputStream out = new ByteArrayOutputStream(32768);
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) > 0)
				out.write(buffer, 0, read);
			out.close();
			// slow and inefficient - avoid this method
			return out.toByteArray();
		} catch (IOException e) {
			return null;
		} catch (OutOfMemoryError e) {
			return null;
		}
	}
	/**
	 * Loads a small text resource as a string.
	 *  Subclasses may want to provide a more efficient implementation.
	 * 
	 * @param src the resource to get
	 * @return the full resource as a string
	 */
	public synchronized String getString(String src) {
		return new String(getBinary(src));
	}
	/**
	 * Gets the specified resource as an AWT icon.
	 * 
	 * @param src the image source
	 * @return the icon
	 */
	public synchronized Icon getIcon(String src) {
		return new ImageIcon(getImage(src));
	}
	/**
	 * Gets the specified resource as an AWT image.
	 * 
	 * @param src the image source
	 * @return the image
	 */
	public synchronized Image getImage(String src) {
		if (imageCache.containsKey(src))
			return imageCache.get(src);
		try {
			Image i = loadImage(src);
			imageCache.put(src, i);
			return i;
		} catch (IOException e) {
			imageCache.put(src, null);
			return null;
		}
	}
	/**
	 * Gets the specified resource as a JOGL texture.
	 * 
	 * @param src the texture source
	 * @return the texture
	 */
	public synchronized Texture getTexture(String src) {
		if (textureCache.containsKey(src))
			return textureCache.get(src);
		try {
			Texture t = loadTexture(src);
			textureCache.put(src, t);
			return t;
		} catch (IOException e) {
			textureCache.put(src, null);
			return null;
		}
	}
	/**
	 * Loads the image specified by src into the cache.
	 *  Subclasses may want to provide a more efficient implementation.
	 * 
	 * @param src the path to the image
	 * @return the image
	 */
	protected Image loadImage(String src) throws IOException {
		return ImageIO.read(openResource("images/" + src));
	}
	/**
	 * Loads the texture specified by src into the cache.
	 *  Subclasses may want to provide a more efficient implementation. 
	 * 
	 * @param src the path to the texture
	 * @return the texture
	 */
	protected Texture loadTexture(String src) throws IOException {
		return TextureIO.newTexture(ImageIO.read(openResource("textures/" + src)), true);
	}
}