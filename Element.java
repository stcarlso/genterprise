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
	 * Creates a blank element (serialization)
	 */
	protected Element() {
		textureSrc = null;
		geometrySrc = null;
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
		if (array.length < 4)
			throw new RuntimeException("Geometry file is invalid");
		int size = Utils.createInt(array[0], array[1], array[2], array[3]);
		if (size < 1 || array.length < 4 + 9 * size)
			throw new RuntimeException("Geometry file size is invalid");
		ByteBuffer buf = BufferUtil.newByteBuffer(size);
		buf.put(array, 4, 3 * size);
		vertex = buf.asFloatBuffer();
		buf = BufferUtil.newByteBuffer(size);
		buf.put(array, 3 * size + 4, 3 * size);
		color = buf.asFloatBuffer();
		buf = BufferUtil.newByteBuffer(size);
		buf.put(array, 6 * size + 4, 3 * size);
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