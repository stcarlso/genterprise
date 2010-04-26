import java.io.*;

/**
 * Allows maintenance of the large and ever growing list of blocks.
 * 
 * @author Stephen
 */
public class MkList {
	private static BufferedReader br;

	public static void main(String[] args) {
		int option = 0;
		if (args != null && args.length > 0 && args[0].trim().toLowerCase().equals("-u"))
			option = 1;
		br = new BufferedReader(new InputStreamReader(System.in));
		if (option == 0) {
			System.out.println("1) Update List");
			System.out.println("2) Modify List");
		}
		while (option < 1 || option > 2) {
			System.out.print("Choice: ");
			option = readInt();
		}
		if (option == 1) {
			boolean auto = true;
			System.out.println("SVN Update");
			File curdir = new File("code").getAbsoluteFile().getParentFile();
			File dotSvn = new File(curdir, ".svn");
			if (!dotSvn.exists()) {
				curdir = new File(curdir, "code");
				dotSvn = new File(curdir, ".svn");
				if (!dotSvn.exists()) {
					auto = false;
					System.out.print("Please SVN Update manually. ENTER when done.");
					readLine();
				}
			}
			if (auto) try {
				Process p = Runtime.getRuntime().exec("svn up", null, curdir);
				System.out.println("Automatic SVN Update...");
				System.out.println();
				InputStream is = p.getInputStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(is));
				String line;
				while ((line = in.readLine()) != null)
					if ((line = line.trim()).length() > 0)
						System.out.println("[svn] " + line);
				in.close();
				System.out.println();
				System.out.println("Done!");
			} catch (Exception e) {
				auto = false;
				System.out.print("Please SVN Update manually. ENTER when done.");
				readLine();
			}
			System.out.println();
			System.out.print("Updating List... ");
			copyFile(new File(curdir, "branches/models/blocks.txt"), new File("blocks.txt"));
			System.out.println("Done!");
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
	private static String readLine() {
		try {
			return br.readLine();
		} catch (IOException e) {
			return null;
		}
	}
	private static int readInt() {
		while (true) try {
			return Integer.parseInt(readLine());
		} catch (Exception e) {
			System.out.println("Please enter a number.");
		}
	}
}