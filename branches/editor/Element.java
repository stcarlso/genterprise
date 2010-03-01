import java.awt.Dimension;
import java.nio.*;
import com.sun.opengl.util.*;
import com.sun.opengl.util.texture.*;

/**
 * An element of a level. It is not meant for placement, rather as a composition of Blocks.
 * 
 * @author Stephen
 */
public class Element implements java.io.Serializable {
	private static final long serialVersionUID = 238146789346781278L;

	/**
	 * The file name containing geometry information.
	 */
	private String geometrySrc;
	/**
	 * The file containing the texture. Texcoord win avoids the need for lots of textures.
	 */
	private String textureSrc;
	/**
	 * The cached texture object.
	 */
	private transient Texture texture;
	/**
	 * The cached arrays.
	 */
	private transient FloatBuffer vertex;
	private transient FloatBuffer color;
	private transient FloatBuffer texCoords;
	/**
	 * The bounding box width in units.
	 */
	private transient int width;
	/**
	 * The bounding box height in units.
	 */
	private transient int height;

	/**
	 * Creates a blank element (serialization)
	 */
	protected Element() {
		textureSrc = null;
		geometrySrc = null;
		width = height = 0;
	}
	/**
	 * Creates an element referencing data from data sources.
	 * 
	 * @param texSrc the texture data source
	 * @param geoSrc the geometry data source
	 */
	public Element(String texSrc, String geoSrc) {
		textureSrc = texSrc;
		geometrySrc = geoSrc;
		width = height = 0;
	}
	/**
	 * Gets the size as a dimension.
	 * 
	 * @return the height and width
	 */
	public Dimension getSize() {
		return new Dimension(width, height);
	}
	/**
	 * Gets the width in grid units.
	 * 
	 * @return the width (X)
	 */
	public int getWidth() {
		return width;
	}
	/**
	 * Gets the height in grid units.
	 * 
	 * @return the height (Y)
	 */
	public int getHeight() {
		return height;
	}
	/**
	 * Gets the geometry data file name.
	 * 
	 * @return the data file for geometry
	 */
	public String getGeometryLocation() {
		return geometrySrc;
	}
	/**
	 * Gets the texture data file name.
	 * 
	 * @return the data file for textures
	 */
	public String getTextureLocation() {
		return textureSrc;
	}
	/**
	 * Gets the vertex array if loaded.
	 * 
	 * @return the vertex array
	 */
	public FloatBuffer getVertexArray() {
		return vertex;
	}
	/**
	 * Gets the color array if loaded.
	 * 
	 * @return the color array
	 */
	public FloatBuffer getColorArray() {
		return color;
	}
	/**
	 * Gets the texture coordinate array if loaded.
	 * 
	 * @return the texture coordinate array
	 */
	public FloatBuffer getTexCoordArray() {
		return texCoords;
	}
	/**
	 * Loads the geometry data.
	 * 
	 * @param res the resource source
	 */
	public void loadGeometry(ResourceGetter res) {
		byte[] array = res.getBinary("models/" + geometrySrc);
		if (array.length < 12)
			throw new RuntimeException("Geometry file is invalid, it lacks size information");
		int width = Utils.createInt(array, 4);
		int height = Utils.createInt(array, 8);
		if (width < 0 || height < 0)
			throw new RuntimeException("Geometry file is invalid, sizes are negative");
		this.width = width;
		this.height = height;
		int size = Utils.createInt(array, 0);
		if (size < 1 || array.length < 12 + 9 * size)
			throw new RuntimeException("Geometry file size is invalid, there is not enough data");
		ByteBuffer buf = BufferUtil.newByteBuffer(size);
		buf.put(array, 12, 3 * size);
		vertex = buf.asFloatBuffer();
		buf = BufferUtil.newByteBuffer(size);
		buf.put(array, 3 * size + 12, 3 * size);
		color = buf.asFloatBuffer();
		buf = BufferUtil.newByteBuffer(size);
		buf.put(array, 6 * size + 12, 3 * size);
		texCoords = buf.asFloatBuffer();
		array = null;
	}
	/**
	 * Releases the loaded geometry.
	 */
	public void releaseGeometry() {
		vertex = null;
		color = null;
		texCoords = null;
	}
	/**
	 * Loads the texture data.
	 * 
	 * @param res the resource source
	 */
	public int loadTexture(ResourceGetter res) {
		texture = res.getTexture(textureSrc);
		texture.bind();
		return texture.getTextureObject();
	}
	/**
	 * Releases the loaded texture.
	 */
	public void releaseTexture() {
		texture.dispose();
		texture = null;
	}
}