import java.awt.*;
import java.nio.*;
import javax.media.opengl.*;
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
	 * The element name.
	 */
	private String name;
	/**
	 * The default Z buffer value.
	 */
	private double defaultZ;
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
	 * The number of vertices.
	 */
	private transient int size;

	/**
	 * Creates a blank element (serialization)
	 */
	protected Element() {
		textureSrc = null;
		geometrySrc = null;
		name = null;
		width = height = 0;
	}
	/**
	 * Creates an element referencing data from data sources.
	 * 
	 * @param texSrc the texture data source
	 * @param geoSrc the geometry data source
	 */
	public Element(String texSrc, String geoSrc) {
		this(texSrc, geoSrc, geoSrc, 0);
	}
	/**
	 * Creates an element referencing data from data sources.
	 * 
	 * @param texSrc the texture data source
	 * @param geoSrc the geometry data source
	 * @param name the element name
	 */
	public Element(String texSrc, String geoSrc, String name) {
		this(texSrc, geoSrc, name, 0);
	}
	/**
	 * Creates an element referencing data from data sources.
	 * 
	 * @param texSrc the texture data source
	 * @param geoSrc the geometry data source
	 * @param name the element name
	 * @param z the default z buffer coordinate
	 */
	public Element(String texSrc, String geoSrc, String name, double z) {
		textureSrc = texSrc;
		geometrySrc = geoSrc;
		this.name = name;
		defaultZ = z;
		width = height = 0;
	}
	public boolean equals(Object o) {
		Element other = (Element)o;
		return name.equals(other.getName());
	}
	/**
	 * Gets the default Z buffer coordinate.
	 * 
	 * @return the default Z
	 */
	public double getDefaultZ() {
		return defaultZ;
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
	 * Gets the object name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
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
	 * Gets whether textures have been loaded.
	 * 
	 * @return whether the texture was loaded
	 */
	public boolean hasTexture() {
		return texture != null;
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
		if (size < 1 || array.length < 12 + 8 * 4 * size)
			throw new RuntimeException("Geometry file size is invalid, there is not enough data");
		this.size = size;
		ByteBuffer buf = ByteBuffer.wrap(array);
		buf.position(12);
		vertex = BufferUtil.newByteBuffer(3 * 4 * size).asFloatBuffer();
		color = BufferUtil.newByteBuffer(3 * 4 * size).asFloatBuffer();
		texCoords = BufferUtil.newByteBuffer(2 * 4 * size).asFloatBuffer();
		for (int i = 0; i < size; i++) {
			vertex.put(buf.getFloat());
			vertex.put(buf.getFloat());
			vertex.put(buf.getFloat());
		}
		for (int i = 0; i < size; i++) {
			color.put(buf.getFloat());
			color.put(buf.getFloat());
			color.put(buf.getFloat());
		}
		for (int i = 0; i < size; i++) {
			texCoords.put(buf.getFloat());
			texCoords.put(buf.getFloat());
		}
		vertex.rewind();
		color.rewind();
		texCoords.rewind();
		array = null;
	}
	/**
	 * Renders the object. For speed's sake, options are assumed to be unset.
	 *  Use setOptions(GL) to set options.
	 * 
	 * @param gl the OpenGL context
	 */
	public void render(GL gl) {
		if (vertex == null)
			throw new RuntimeException("Cannot render before loading");
		setTexture(gl);
		renderModel(gl);
		draw(gl);
	}
	/**
	 * Sets the texture to this texture.
	 * 
	 * @param gl the OpenGL context
	 */
	public void setTexture(GL gl) {
		if (texture != null)
			gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getTextureObject());
		else
			gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
	}
	/**
	 * Draws the array on the screen.
	 * 
	 * @param gl the OpenGL context
	 */
	public void draw(GL gl) {
		gl.glDrawArrays(GL.GL_TRIANGLES, 0, size);
	}
	/**
	 * Only loads the model into memory; good for repeatedly drawing the same model.
	 * 
	 * @param gl the OpenGL context
	 */
	public void renderModel(GL gl) {
		gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertex);
		gl.glColorPointer(3, GL.GL_FLOAT, 0, color);
		gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, texCoords);
	}
	/**
	 * Sets the rendering-specific options on the GL. Use before rendering a bunch of elements.
	 * 
	 * @param gl the OpenGL context
	 */
	public static void setOptions(GL gl) {
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL.GL_COLOR_ARRAY);
	}
	/**
	 * Clears the rendering-specific options on the GL. Use after rendering a bunch of elements.
	 * 
	 * @param gl the OpenGL context
	 */
	public static void clearOptions(GL gl) {
		gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL.GL_COLOR_ARRAY);
		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
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
		if (textureSrc != null) {
			texture = res.getTexture(textureSrc);
			texture.bind();
			return texture.getTextureObject();
		} else
			return 0;
	}
	/**
	 * Releases the loaded texture.
	 */
	public void releaseTexture() {
		if (texture != null) {
			texture.dispose();
			texture = null;
		}
	}
	public String toString() {
		return name;
	}
}