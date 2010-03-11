import java.io.*;
import java.util.jar.*;

/**
 * Retrieves level info from the leve's JAR (production)
 * 
 * @author Stephen
 */
public class JarResources extends ResourceGetter {
	/**
	 * The root resource getter.
	 */
	public static final ResourceGetter PARENT = new JarResources();
	/**
	 * The source ZIP (JAR) file.
	 */
	private JarFile file;

	/**
	 * Creates the only root resource getter. All others call this one eventually.
	 */
	protected JarResources() {
		super(null);
		try {
			file = new JarFile(new File("resource.jar"));
		} catch (Exception e) {
			Utils.fatalError("Someone forgot to copy or include \"resource.jar\" in the program distribution.<br>" +
				"Please re-install the program or download again.");
		}
	}
	/**
	 * Creates a new JAR resources on the given level file.
	 * 
	 * @param parent the parent resource getter
	 * @param fileName the level file
	 */
	public JarResources(ResourceGetter parent, File fileName) throws IOException {
		super(parent);
		file = new JarFile(fileName);
	}
	protected InputStream openResource(String src) throws IOException {
		try {
			return file.getInputStream(new JarEntry("res/" + src));
		} catch (Exception e) {
			if (parent == null)
				return null;
			return parent.openResource(src);
		}
	}
}