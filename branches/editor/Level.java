import java.util.*;

/**
 * A class holding information about an entire level.
 * 
 * @author Stephen
 */
public class Level {
	private static final long serialVersionUID = 238146789346781280L;

	/**
	 * The flat array of blocks, will be unrolled at runtime.
	 */
	private List<Block> blocks;

	/**
	 * Creates a new level with no blocks.
	 */
	public Level() {
		blocks = new ArrayList<Block>(128);
	}
	/**
	 * Iterates over level blocks.
	 * 
	 * @return the iterator over the blocks
	 */
	public Iterator<Block> blockIterator() {
		return blocks.iterator();
	}
}