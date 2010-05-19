import java.io.*;
import java.util.*;

/**
 * A class which holds program settings.
 * 
 * @author Stephen
 */
public class Settings extends Properties {
	private static final long serialVersionUID = 0L;

	/**
	 * The file where settings are held.
	 */
	private String file;

	/**
	 * Reads the settings from the given file.
	 * 
	 * @param res the settings file name
	 */
	public Settings(String res) {
		file = res;
		try {
			load(new FileInputStream(file));
		} catch (Exception e) {
			try {
				new File(file).createNewFile();
				clear();
			} catch (Exception e2) {
				throw new RuntimeException("Cannot read preferences.", e2);
			}
		}
	}
	/**
	 * Reads the given setting as an integer.
	 * 
	 * @param name the property name
	 * @param def the default value
	 * @return the setting value
	 */
	public int getInt(String name, int def) {
		try {
			return Integer.parseInt(getProperty(name, Integer.toString(def)));
		} catch (Exception e) {
			return def;
		}
	}
	/**
	 * Saves the settings to file.
	 * 
	 * @return whether it worked or not
	 */
	public boolean writeOut() {
		try {
			store(new FileOutputStream(file), null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}