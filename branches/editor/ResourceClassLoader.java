/**
 * A class to load level code from its specified location.
 * 
 * @author Stephen
 */
public class ResourceClassLoader extends ClassLoader {
	/**
	 * Gets classes from a resource framework.
	 */
	private ResourceGetter res;

	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			String newName = name.replace('.', '/');
			byte[] classData = res.getBinary(newName);
			Class<?> clazz = defineClass(name, classData, 0, classData.length);
			resolveClass(clazz);
			return clazz;
		} catch (Exception e) {
			throw new ClassNotFoundException(name);
		}
	}
}