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
	 * @param base the base directory (including res/)
	 */
	public FilesystemResources(File base) {
		super();
		baseDir = base;
	}
	protected InputStream openResource(String src) throws IOException {
		return new FileInputStream(new File(baseDir, src));
	}
}