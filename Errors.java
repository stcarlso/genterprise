import java.awt.GraphicsEnvironment;
import java.io.*;
import java.lang.Thread.*;
import java.util.regex.*;
import javax.swing.*;

/**
 * Error handlers handle errors in Gunther's Enterprise. These are entirely separate
 *  to minimize Gunther's Enterprise crashes working their way into the error handler too.
 */
public class Errors implements UncaughtExceptionHandler {
	public static final String[] RESUME_OPTIONS = new String[] { "Error Details", "Exit" };
	private static final Pattern LESS = Pattern.compile("<");
	private static final Pattern GREAT = Pattern.compile(">");

	/**
	 * Handles errors on all threads.
	 */
	public static void handleErrors() {
		Thread.setDefaultUncaughtExceptionHandler(new Errors());
	}
	/**
	 * Logs the error to the error log.
	 * 
	 * @param t the error
	 */
	public static void log(Throwable t) {
		if (t == null) return;
		System.runFinalization();
		System.gc();
		// Put it in the error log. <i>Not to be called in a loop.</i>
		try {
			PrintWriter out = new PrintWriter(new FileWriter("error.log", true));
			if (t.getMessage() != null)
				out.println(t.getMessage());
			t.printStackTrace(out);
			out.close();
		} catch (Exception e) {
			sorry(e);
		}
	}
	/**
	 * A user error brought the program down in a non-recoverable way.
	 *  This is <b>not</b> a programming error.
	 * 
	 * @param message the error message
	 */
	public static void userError(String message) {
		GraphicsEnvironment.getLocalGraphicsEnvironment().
			getDefaultScreenDevice().setFullScreenWindow(null);
		StringBuilder text = new StringBuilder(512);
		text.append("<html><body><b>A fatal error occurred:</b><br>");
		text.append(message);
		text.append("<br>Gunther's Enterprise will now exit.</body></html>");
		JOptionPane.showMessageDialog(null, text.toString(), "Gunther's Enterprise Error",
			JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}
	/**
	 * Ends the program with a sorry message.
	 * 
	 * @param t the error
	 */
	public static void sorry(Throwable t) {
		if (t == null) return;
		while (t.getCause() != null)
			t = t.getCause();
		try {
			System.runFinalization();
			System.gc();
			GraphicsEnvironment.getLocalGraphicsEnvironment().
				getDefaultScreenDevice().setFullScreenWindow(null);
			StringBuilder message = new StringBuilder(512);
			message.append("<html><body><font size=\"+2\">Oh no!</font><br>");
			message.append("While Gunther's Enterprise is pretty close to done, a fatal error occurred.<br>");
			message.append("Even worse, Gunther's Enterprise didn't know what to do with the problem.<br>");
			message.append("It's better to exit now than cause more errors.<br>");
			message.append("We're sorry if you lost any data.</body></html>");
			int option = JOptionPane.showOptionDialog(null, message, "Gunther's Enterprise Error",
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
				RESUME_OPTIONS, "Exit");
			if (option != 0)
				System.exit(1);
			StringWriter details = new StringWriter(512);
			PrintWriter out = new PrintWriter(details);
			out.print("<html><body><pre>");
			out.print(t.getClass().getSimpleName());
			out.print(": ");
			out.print(encode(t.getMessage()));
			out.print("<br>");
			StackTraceElement[] elements = t.getStackTrace();
			for (int i = 0; i < elements.length; i++)
				out.print(" at " + encode(elements[i].toString()) + "<br>");
			out.print("</pre></body></html>");
			out.flush();
			out.close();
			JOptionPane.showMessageDialog(null, details.toString(), "Error Details",
				JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) { }
		System.exit(1);
	}
	private static String encode(String text) {
		if (text == null) return "null";
		return GREAT.matcher(LESS.matcher(text).replaceAll("&lt;")).replaceAll("&gt;");
	}

	public void uncaughtException(Thread t, Throwable e) {
		sorry(e);
	}

	private Errors() { }
}