import java.util.*;

/**
 * A class holding information about an entire level.
 * 
 * @author Stephen
 */
public class Level implements java.io.Serializable {
	private static final long serialVersionUID = 238146789346781280L;

	/**
	 * The plane sweep to sort by X.
	 */
	public static final PlaneSweep xComparator = new PlaneSweep(0);
	/**
	 * The plane sweep to sort by Y.
	 */
	public static final PlaneSweep yComparator = new PlaneSweep(1);
	/**
	 * The flat array of blocks, will be unrolled at runtime.
	 */
	private List<Block> blocks;
	/**
	 * The blocks sorted by beginning Y coordinate.
	 */
	private transient List<Block> blocksByY;
	/**
	 * The blocks sorted by beginning X coordinate.
	 */
	private transient List<Block> blocksByX;

	/**
	 * Creates a new level with no blocks.
	 */
	public Level() {
		blocks = new ArrayList<Block>(128);
		blocksByX = new ArrayList<Block>(128);
		blocksByY = new ArrayList<Block>(128);
	}
	/**
	 * Iterates over level blocks.
	 * 
	 * @return the iterator over the blocks
	 */
	public Iterator<Block> blockIterator() {
		return blocks.iterator();
	}
	/**
	 * Unrolls the flat array into plane swept blocks.
	 */
	private synchronized void unroll() {
		if (blocksByX.isEmpty() && !blocks.isEmpty()) return;
		blocksByX.clear();
		blocksByY.clear();
		blocksByX.addAll(blocks);
		Collections.sort(blocksByX, xComparator);
		blocksByY.addAll(blocks);
		Collections.sort(blocksByY, yComparator);
	}
	/**
	 * Returns the blocks sorted by X coordinate.
	 * 
	 * @return the block list
	 */
	public List<Block> blocksByX() {
		unroll();
		return blocksByX;
	}
	/**
	 * Returns the blocks sorted by Y coordinate.
	 * 
	 * @return the block list
	 */
	public List<Block> blocksByY() {
		unroll();
		return blocksByY;
	}

	/**
	 * A class that plane sweep sorts blocks by bounds.
	 */
	private static class PlaneSweep implements Comparator<Block> {
		/**
		 * 0 is X, 1 is Y
		 */
		private int direction;

		public PlaneSweep(int direction) {
			this.direction = direction;
		}
		public int compare(Block one, Block two) {
			if (direction == 0)
				return (int)Math.signum(one.getMinX() - two.getMinX());
			else if (direction == 1)
				return (int)Math.signum(one.getMinY() - two.getMinY());
			return 0;
		}
	}
}