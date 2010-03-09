import java.io.*;

/**
 * Retrieves level info from the file system (testing)
 * 
 * @author Stephen
 */
public class FilesystemResources extends ResourceGetter {
	/**
	 * The base directory of the file.
	 */
	private File baseDir;

	/**
	 * Creates a new file system resource.
	 * 
	 * @param parent the parent resources
	 * @param base the base directory (including res/)
	 */
	public FilesystemResources(ResourceGetter parent, File base) {
		super(parent);
		baseDir = base;
	}
	protected InputStream openResource(String src) throws IOException {
		try {
			return new FileInputStream(new File(baseDir, src));
		} catch (IOException e) {
			return parent.openResource(src);
		}
	}
}