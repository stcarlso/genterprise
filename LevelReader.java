import java.io.*;

/**
 * A class that reads levels!
 * 
 * @author Stephen
 */
public class LevelReader {
	/**
	 * The actual level.
	 */
	private Level level;
	/**
	 * The level resources.
	 */
	private ResourceGetter resources;

	/**
	 * Reads the level from the given resource location.
	 * 
	 * @param res the location of level resources
	 * @throws RuntimeException if something goes wrong
	 */
	public LevelReader(ResourceGetter res) {
		resources = res;
		try {
			readLevel();
		} catch (Exception e) {
			throw new RuntimeException("Could not read level.", e);
		}
	}
	/**
	 * Reads the level from disk.
	 * 
	 * @throws Exception if the level cannot be read
	 */
	private void readLevel() throws Exception {
		ObjectInputStream ois = new ObjectInputStream(resources.getResource("level.dat"));
		level = (Level) ois.readObject();
		ois.close();
	}
	/**
	 * Gets the level data.
	 * 
	 * @return the level data
	 */
	public Level getLevel() {
		return level;
	}
}