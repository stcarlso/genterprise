import java.io.*;

/**
 * A class that writes levels!
 * 
 * @author Stephen
 */
public class LevelWriter {
	/**
	 * The object output stream.
	 */
	private ObjectOutputStream oos;

	/**
	 * Creates a level writer which will write levels to the given output stream.
	 * 
	 * @param os the output stream where levels will be written
	 */
	public LevelWriter(OutputStream os) throws IOException {
		oos = new ObjectOutputStream(os);
	}
	/**
	 * Writes the level to disk.
	 * 
	 * @param level the level to write
	 * @throws Exception if the level cannot be read
	 */
	public void writeLevel(Level level) throws IOException {
		oos.reset();
		oos.writeObject(level);
		oos.flush();
	}
	/**
	 * Closes the level writer.
	 */
	public void close() throws IOException {
		oos.close();
	}
}