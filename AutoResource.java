import java.io.*;
import java.awt.*;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Generates the res/ folder. NOT to be included with the final program.
 */
public class AutoResource {
	public static void main(String[] args) {
		Errors.handleErrors();
		File branches = new File("branches/");
		if (!branches.exists())
			branches = new File("code/branches/");
		if (!branches.exists())
			die("Failed to find svn repository. It must be in code/branches or branches.");
		branches = branches.getAbsoluteFile();
		File res = new File("res/").getAbsoluteFile();
		if (!res.exists() && !res.mkdir())
			die("No res directory.");
		File models = new File(branches, "models");
		File textures = new File(models, "textures");
		File images = new File(models, "images");
		File sounds = new File(models, "sound");
		File resModels = new File(res, "models");
		if (!resModels.exists() && !resModels.mkdir())
			die("No res/models directory.");
		emptyDir(resModels);
		File resTextures = new File(res, "textures");
		if (!resTextures.exists() && !resTextures.mkdir())
			die("No res/textures directory.");
		emptyDir(resTextures);
		File resImages = new File(res, "images");
		if (!resImages.exists() && !resImages.mkdir())
			die("No res/images directory.");
		emptyDir(resImages);
		File resSounds = new File(res, "sound");
		if (!resSounds.exists() && !resSounds.mkdir())
			die("No res/sounds directory.");
		File defaultJava = new File(res, "default.java");
		try {
			if (!defaultJava.exists() && !defaultJava.createNewFile())
				die("No res/default.java file (it can be empty).");
		} catch (IOException e) {
			die("No res/default.java file (it can be empty).");
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
			copyFile(model, new File(resModels, model.getName().toLowerCase()));
			System.out.println("wrote res/models/" + model.getName());
		}
		Image img; File o1;
		for (File resource : textureList) {
			img = new ImageIcon(resource.getPath()).getImage();
			if (img == null)
				die("Could not load '" + resource.getPath() + "'; try opening and re-saving with Preview or Windows Picture and Fax Viewer.");
			o1 = new File(resTextures, resource.getName().toLowerCase());
			try {
				ImageIO.write(Utils.imageToBuffer(img), "png", o1);
			} catch (Exception e) {
				die("Could not write '" + o1.getAbsolutePath() + "'!");
			}
			System.out.println("wrote res/textures/" + resource.getName().toLowerCase());
			copyFile(o1, new File(resImages, resource.getName().toLowerCase()));
			System.out.println("wrote res/images/" + resource.getName().toLowerCase());
		}
		if (imageList != null) for (File resource : imageList) {
			img = new ImageIcon(resource.getPath()).getImage();
			if (img == null)
				die("Could not load '" + resource.getPath() + "'; try opening and re-saving with Preview or Windows Picture and Fax Viewer.");
			o1 = new File(resImages, resource.getName().toLowerCase());
			try {
				ImageIO.write(Utils.imageToBuffer(img), "png", o1);
			} catch (Exception e) {
				die("Could not write '" + o1.getAbsolutePath() + "'!");
			}
			System.out.println("wrote res/images/" + resource.getName().toLowerCase());
		}
		for (File sound : mp3List) {
			o1 = new File(resSounds, sound.getName().toLowerCase());
			if (!o1.exists()) copyFile(sound, o1);
			System.out.println("wrote res/sound/" + o1.getName());
		}
		for (File sound : soundList) {
			o1 = new File(resSounds, sound.getName().toLowerCase());
			if (!o1.exists()) copyFile(sound, o1);
			System.out.println("wrote res/sound/" + o1.getName());
		}
		copyFile(new File(models, "credits.txt"), new File(res, "credits.txt"));
	}
	private static void emptyDir(File dir) {
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.equals(".") || file.equals("..")) continue;
			if (file.isDirectory())
				emptyDir(file);
			if (!file.delete())
				die("Can't delete '" + file.getAbsolutePath() + "'; check read-only status.");
		}
	}
	private static boolean copyFile(File src, File dest) {
		try {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) > 0)
				out.write(buffer, 0, read);
			out.close();
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