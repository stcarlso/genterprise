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
	 */
	public LevelReader(ResourceGetter res) {
		this(res, "level.dat");
	}
	/**
	 * Reads the level from the given resource location.
	 * 
	 * @param res the location of level resources
	 * @param level the level file name
	 * @throws RuntimeException if something goes wrong
	 */
	public LevelReader(ResourceGetter res, String level) {
		resources = res;
		try {
			readLevel(level);
		} catch (Exception e) {
			throw new RuntimeException("Could not read level.", e);
		}
	}
	/**
	 * Reads the level from disk.
	 * 
	 * @param level the file name
	 * @throws Exception if the level cannot be read
	 */
	private void readLevel(String level) throws Exception {
		ObjectInputStream ois = new ObjectInputStream(resources.getResource(level));
		this.level = (Level) ois.readObject();
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
	/**
	 * Gets the resources for this level.
	 * 
	 * @return the level resources
	 */
	public ResourceGetter getResources() {
		return resources;
	}
}