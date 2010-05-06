import java.io.*;
import java.util.zip.*;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Generates resource.jar. NOT to be included with the final program.
 */
public class JarAutoResource {
	public static void main(String[] args) {
		Errors.handleErrors();
		File branches = new File("branches/");
		if (!branches.exists())
			branches = new File("code/branches/");
		if (!branches.exists())
			die("Failed to find svn repository. It must be in code/branches or branches.");
		branches = branches.getAbsoluteFile();
		File resJar = new File("resource.jar");
		if (resJar.exists() && !resJar.canWrite())
			die("resource.jar is read only.");
		ZipOutputStream res;
		try {
			res = new ZipOutputStream(new FileOutputStream(resJar));
		} catch (Exception e) {
			die("resource.jar is read only.");
			return;
		}
		File models = new File(branches, "models");
		File textures = new File(models, "textures");
		File images = new File(models, "images");
		File sounds = new File(models, "sound");
		try {
			res.putNextEntry(new ZipEntry("default.java"));
			res.closeEntry();
		} catch (Exception e) {
			die("resource.jar could not be opened.");
			return;
		}
		if (!models.exists() || !textures.exists() || !sounds.exists())
			die("Could not find models, sounds, or textures. Please SVN Update.");
		FileFilter ifilter = new ExtensionFilter(".png");
		File[] modelList = models.listFiles(new ExtensionFilter(".dat"));
		File[] textureList = textures.listFiles(ifilter);
		File[] soundList = sounds.listFiles(new ExtensionFilter(".wav"));
		File[] mp3List = sounds.listFiles(new ExtensionFilter(".mp3"));
		File[] imageList = null;
		if (images.canRead())
			imageList = images.listFiles(ifilter);
		for (File model : modelList) {
			copyFile(res, model, "models/" + model.getName().toLowerCase());
			System.out.println("wrote resource.jar/models/" + model.getName());
		}
		Image img; String o1;
		for (File resource : textureList) {
			img = new ImageIcon(resource.getPath()).getImage();
			if (img == null)
				die("Could not load '" + resource.getPath() + "'; try opening and re-saving with Preview or Windows Picture and Fax Viewer.");
			o1 = resource.getName().toLowerCase();
			try {
				res.putNextEntry(new ZipEntry("textures/" + o1));
				ImageIO.write(Utils.imageToBuffer(img), "png", res);
				res.closeEntry();
			} catch (Exception e) {
				die("Could not write '" + o1 + "'!");
			}
			System.out.println("wrote resource.jar/textures/" + o1);
			copyFile(res, resource, "images/" + o1);
			System.out.println("wrote resource.jar/images/" + o1);
		}
		if (imageList != null) for (File resource : imageList) {
			img = new ImageIcon(resource.getPath()).getImage();
			if (img == null)
				die("Could not load '" + resource.getPath() + "'; try opening and re-saving with Preview or Windows Picture and Fax Viewer.");
			o1 = resource.getName().toLowerCase();
			try {
				res.putNextEntry(new ZipEntry("images/" + o1));
				ImageIO.write(Utils.imageToBuffer(img), "png", res);
				res.closeEntry();
			} catch (Exception e) {
				die("Could not write '" + o1 + "'!");
			}
			System.out.println("wrote resource.jar/images/" + o1);
		}
		for (File sound : mp3List) {
			o1 = sound.getName().toLowerCase();
			copyFile(res, sound, "sound/" + o1);
			System.out.println("wrote resource.jar/sound/" + o1);
		}
		for (File sound : soundList) {
			o1 = sound.getName().toLowerCase();
			copyFile(res, sound, "sound/" + o1);
			System.out.println("wrote resource.jar/sound/" + o1);
		}
		copyFile(res, new File(models, "credits.txt"), "credits.txt");
		try {
			res.close();
		} catch (Exception e) { }
	}
	private static boolean copyFile(ZipOutputStream os, File src, String dest) {
		try {
			InputStream in = new FileInputStream(src);
			os.putNextEntry(new ZipEntry(dest));
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) > 0)
				os.write(buffer, 0, read);
			os.closeEntry();
			in.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	private static void die(String message) {
		System.err.println(message);
		System.exit(1);
	}

	private static final class ExtensionFilter implements FileFilter {
		private String ext;

		public ExtensionFilter(String ext) {
			this.ext = ext;
		}
		public boolean accept(File pathname) {
			String name = pathname.getName();
			return pathname.canRead() && !pathname.isHidden() && !pathname.isDirectory() && name.toLowerCase().endsWith(ext);
		}
	}
}