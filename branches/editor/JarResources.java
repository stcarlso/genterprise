import java.io.*;
import java.util.jar.*;

/**
 * Retrieves level info from the leve's JAR (production)
 * 
 * @author Stephen
 */
public class JarResources extends ResourceGetter {
	/**
	 * The source ZIP (JAR) file.
	 */
	private JarFile file;

	/**
	 * Creates a new JAR resources on the given level file.
	 * 
	 * @param fileName the level file
	 */
	public JarResources(File fileName) throws IOException {
		file = new JarFile(fileName);
	}
	protected InputStream openResource(String src) throws IOException {
		return file.getInputStream(new JarEntry("res/" + src));
	}
}