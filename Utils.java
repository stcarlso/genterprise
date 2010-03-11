import java.awt.*;
import java.awt.image.*;
import java.nio.*;
import javax.swing.*;

/**
 * Miscellaneous level editor utilities.
 * 
 * @author Stephen
 */
public final class Utils {
	/**
	 * The image loader.
	 */
	private static ImageIcon iconLoad;

	/**
	 * Copies the image to a buffered image.
	 * 
	 * @param img the image to copy
	 * @return the buffer image
	 */
	public static final BufferedImage imageToBuffer(Image img) {
		if (img == null) return null;
		loadFully(img);
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		if (w < 0 || h < 0) return null;
		BufferedImage buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics g = buf.getGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return buf;
	}
	/**
	 * Loads the specified image fully.
	 * 
	 * @param img the image to load
	 */
	public synchronized final static void loadFully(Image img) {
		if (img == null) return;
		if (iconLoad == null) iconLoad = new ImageIcon();
		iconLoad.setImage(img);
	}
	/**
	 * Gets the base path (one folder up) of a document.
	 * 
	 * @param doc the document URL
	 * @return its parent
	 */
	public static final String getBase(String doc) {
		if (doc == null || doc.length() < 1 || doc.equals("/"))
			return "/";
		int index = doc.indexOf('/');
		if (index < 0) return "/";
		String base = doc.substring(0, index);
		if (base.length() < 0 || base.indexOf('/') < 0) return "/";
		return base + "/";
	}
	/**
	 * Turns the specified byte array into a string.
	 * 
	 * @param bytes the ASCII bytes of the string
	 * @return the byte array turned into a string
	 */
	public static final String byteArrayToString(byte[] bytes) {
		if (bytes == null) return null;
		return new String(bytes);
	}
	/**
	 * Clones the specified byte array.
	 * 
	 * @param in the array to copy
	 * @return a copy of the array
	 */
	public static final byte[] cloneArray(byte[] in) {
		if (in == null) return null;
		byte[] output = new byte[in.length];
		// could use System.arraycopy, but this is guaranteed to be cryptographically stable
		for (int i = 0; i < in.length; i++)
			output[i] = in[i];
		return output;
	}
	/**
	 * Clones the specified integer array.
	 * 
	 * @param in the array to copy
	 * @return a copy of the array
	 */
	public static final int[] cloneArray(int[] in) {
		if (in == null) return null;
		int[] output = new int[in.length];
		// could use System.arraycopy, but this is guaranteed to be cryptographically stable
		for (int i = 0; i < in.length; i++)
			output[i] = in[i];
		return output;
	}
	/**
	 * Copies the source array into another array.
	 * 
	 * @param src the byte source
	 * @param dest the destination
	 * @param start the place to start in the destination
	 * @param num the number of bytes to copy
	 */
	public static final void byteblt(byte[] src, byte[] dest, int start, int num) {
		if (src == null || dest == null) return;
		// could use System.arraycopy, but this is guaranteed to be cryptographically stable
		for (int i = 0; i < Math.min(num, src.length); i++)
			dest[i + start] = src[i];
	}
	/**
	 * Copies the source array into another array.
	 * 
	 * @param src the integer source
	 * @param dest the destination
	 * @param start the place to start in the destination
	 * @param num the number of integers to copy
	 */
	public static final void intblt(int[] src, int[] dest, int start, int num) {
		if (src == null || dest == null) return;
		// could use System.arraycopy, but this is guaranteed to be cryptographically stable
		for (int i = 0; i < Math.min(num, src.length); i++)
			dest[i + start] = src[i];
	}
	/**
	 * Creates an integer array from the packed bytes of another array.
	 * 
	 * @param array the source array
	 * @return an array with the bytes packed into integers
	 */
	public static final int[] pack(byte[] array) {
		if (array == null) return null;
		// determine length
		int len = array.length / 4;
		int[] output = new int[len];
		// byte pack
		for (int i = 0; i < len; i++)
			output[i] = createInt(array[4 * i], array[4 * i + 1], array[4 * i + 2],
				array[4 * i + 3]);
		return output;
	}
	/**
	 * Packs the long into a byte array.
	 * 
	 * @param value the number to pack
	 * @return an 8 element byte array with its values
	 */
	public static final byte[] packLong(long value) {
		byte[] array = new byte[8];
		// bitwise win!
		array[0] = (byte)(value >>> 56);
		array[1] = (byte)(value >>> 48);
		array[2] = (byte)(value >>> 40);
		array[3] = (byte)(value >>> 32);
		array[4] = (byte)(value >>> 24);
		array[5] = (byte)(value >>> 16);
		array[6] = (byte)(value >>> 8);
		array[7] = (byte)value;
		return array;
	}
	/**
	 * Creates a long integer from eight bytes.
	 * 
	 * @param data the 8 bytes where the long's bytes are stored
	 * @return the value
	 */
	public static final long unpackLong(byte[] array) {
		return ((long)array[0] << 56) + ((long)(array[1] & 0xff) << 48) +
			((long)(array[2] & 0xff) << 40) + ((long)(array[3] & 0xff) << 32) +
			((long)(array[4] & 0xff) << 24) + ((long)(array[5] & 0xff) << 16) +
			((long)(array[6] & 0xff) << 8) + (long)(array[7] & 0xff);
	}
	/**
	 * Creates an integer from four bytes.
	 * 
	 * @param one highest byte
	 * @param two higher byte
	 * @param three lower byte
	 * @param four lowest byte
	 * @return the integer
	 */
	public static final int createInt(byte one, byte two, byte three, byte four) {
		return ((int)one << 24) + ((int)(two & 0xff) << 16) + ((int)(three & 0xff) << 8) +
			(int)(four & 0xff);
	}
	/**
	 * Creates an integer from a byte array.
	 * 
	 * @param array the source array
	 * @param start the starting coordinate
	 * @return the integer
	 */
	public static final int createInt(byte[] array, int start) {
		return createInt(array[start], array[start + 1], array[start + 2], array[start + 3]);
	}
	/**
	 * Adds an integer to the buffer.
	 * 
	 * @param buf the byte buffer
	 * @param value the value to add
	 */
	public static final void packInt(ByteBuffer buf, int value) {
		buf.put((byte)(value >>> 24));
		buf.put((byte)(value >>> 16));
		buf.put((byte)(value >>> 8));
		buf.put((byte)value);
	}
	/**
	 * Copies the contents of an array.
	 * 
	 * @param array the array to copy
	 * @return a copy of the array
	 */
	public static final boolean[] copyOf(boolean[] array) {
		if (array == null) return null;
		boolean[] answer = new boolean[array.length];
		System.arraycopy(array, 0, answer, 0, array.length);
		return answer;
	}
	/**
	 * Copies the contents of an array.
	 * 
	 * @param array the array to copy
	 * @return a copy of the array
	 */
	public static final byte[] copyOf(byte[] array) {
		if (array == null) return null;
		byte[] answer = new byte[array.length];
		System.arraycopy(array, 0, answer, 0, array.length);
		return answer;
	}
	/**
	 * Copies the contents of an array.
	 * 
	 * @param array the array to copy
	 * @return a copy of the array
	 */
	public static final short[] copyOf(short[] array) {
		if (array == null) return null;
		short[] answer = new short[array.length];
		System.arraycopy(array, 0, answer, 0, array.length);
		return answer;
	}
	/**
	 * Copies the contents of an array.
	 * 
	 * @param array the array to copy
	 * @return a copy of the array
	 */
	public static final char[] copyOf(char[] array) {
		if (array == null) return null;
		char[] answer = new char[array.length];
		System.arraycopy(array, 0, answer, 0, array.length);
		return answer;
	}
	/**
	 * Copies the contents of an array.
	 * 
	 * @param array the array to copy
	 * @return a copy of the array
	 */
	public static final int[] copyOf(int[] array) {
		if (array == null) return null;
		int[] answer = new int[array.length];
		System.arraycopy(array, 0, answer, 0, array.length);
		return answer;
	}
	/**
	 * Copies the contents of an array.
	 * 
	 * @param array the array to copy
	 * @return a copy of the array
	 */
	public static final long[] copyOf(long[] array) {
		if (array == null) return null;
		long[] answer = new long[array.length];
		System.arraycopy(array, 0, answer, 0, array.length);
		return answer;
	}
	/**
	 * Copies the contents of an array.
	 * 
	 * @param array the array to copy
	 * @return a copy of the array
	 */
	public static final float[] copyOf(float[] array) {
		if (array == null) return null;
		float[] answer = new float[array.length];
		System.arraycopy(array, 0, answer, 0, array.length);
		return answer;
	}
	/**
	 * Copies the contents of an array.
	 * 
	 * @param array the array to copy
	 * @return a copy of the array
	 */
	public static final double[] copyOf(double[] array) {
		if (array == null) return null;
		double[] answer = new double[array.length];
		System.arraycopy(array, 0, answer, 0, array.length);
		return answer;
	}
	/**
	 * Copies the contents of an array.
	 * 
	 * @param <T> the type of object in the array
	 * @param array the array to copy
	 * @return a copy of the array
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T[] copyOf(T[] array) {
		if (array == null) return null;
		T[] answer = (T[])java.lang.reflect.Array.newInstance(array.getClass().getComponentType(),
			array.length);
		System.arraycopy(array, 0, answer, 0, array.length);
		return answer;
	}
	/**
	 * Does all the static one-time init.
	 */
	public static final void staticInit() {
		Errors.handleErrors();
	}
	/**
	 * Shows an information message.
	 * 
	 * @param message the information message
	 */
	public static final void showMessage(String message) {
		JOptionPane.showMessageDialog(null, message, "Gunther's Enterprise", JOptionPane.INFORMATION_MESSAGE);
	}
	/**
	 * Shows a warning message.
	 * 
	 * @param message the warning message
	 */
	public static final void showWarning(String message) {
		JOptionPane.showMessageDialog(null, message, "Gunther's Enterprise", JOptionPane.WARNING_MESSAGE);
	}
	/**
	 * Shows a fatal error message. The program exits now!
	 * 
	 * @param message the fatal error message
	 */
	public static final void fatalError(String message) {
		Errors.userError(message);
	}
	/**
	 * Puts the window in the center of the screen.
	 * 
	 * @param win the window to center
	 */
	public static void centerWindow(Window win) {
		Dimension ss = win.getToolkit().getScreenSize();
		win.setLocation((ss.width - win.getWidth()) / 2, (ss.height - win.getHeight()) / 2);
	}
	/**
	 * Waits for the given number of milliseconds.
	 * 
	 * @param ms the time to wait for
	 */
	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e) { }
	}

	/**
	 * Not to be instantiated.
	 */
	private Utils() { }
}