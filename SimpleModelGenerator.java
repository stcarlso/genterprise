import java.util.*;
import java.io.*;
import java.nio.*;

/**
 * NOTE: File is for testing purposes only.
 * 
 * This is NOT to belong in the final product.
 */
public class SimpleModelGenerator {
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Width (Grid Units): ");
		int width = Integer.parseInt(br.readLine());
		System.out.print("Height (Grid Units): ");
		int height = Integer.parseInt(br.readLine());
		System.out.print("# Vertices: ");
		int size = Integer.parseInt(br.readLine());
		ByteBuffer vv = ByteBuffer.allocate(3 * 4 * size);
		ByteBuffer cc = ByteBuffer.allocate(3 * 4 * size);
		ByteBuffer tt = ByteBuffer.allocate(2 * 4 * size);
		vv.rewind();
		cc.rewind();
		tt.rewind();
		FloatBuffer vertex = vv.asFloatBuffer();
		FloatBuffer color = cc.asFloatBuffer();
		FloatBuffer texCoord = tt.asFloatBuffer();
		float x = 0.f, y = 0.f, z = 0.f, r = 1.f, g = 1.f, b = 1.f, tx = 1.f, ty = 1.f; StringTokenizer str;
		for (int i = 0; i < size; i++) {
			System.out.println("I: " + (i + 1) + "/" + size);
			System.out.print("V [3f, frac GU] (LAST): ");
			str = new StringTokenizer(br.readLine(), ",");
			if (str.countTokens() >= 3) {
				x = Float.parseFloat(str.nextToken());
				y = Float.parseFloat(str.nextToken());
				z = Float.parseFloat(str.nextToken());
			} else System.err.println("Warning, not 3 coordinates, putting last.");
			vertex.put(x);
			vertex.put(y);
			vertex.put(z);
			System.out.print("C [3f, 0-1 RGB] (LAST): ");
			str = new StringTokenizer(br.readLine(), ",");
			if (str.countTokens() >= 3) {
				r = Float.parseFloat(str.nextToken());
				g = Float.parseFloat(str.nextToken());
				b = Float.parseFloat(str.nextToken());
			} else System.err.println("Warning, not 3 values, putting last.");
			color.put(r);
			color.put(g);
			color.put(b);
			System.out.print("TC[2f, 0-1 IMG] (LAST): ");
			str = new StringTokenizer(br.readLine(), ",");
			if (str.countTokens() >= 2) {
				tx = Float.parseFloat(str.nextToken());
				ty = Float.parseFloat(str.nextToken());
			} else System.err.println("Warning, not 2 values, putting last.");
			texCoord.put(tx);
			texCoord.put(ty);
		}
		ByteBuffer info = ByteBuffer.allocate(12);
		info.position(0);
		Utils.packInt(info, size);
		Utils.packInt(info, width);
		Utils.packInt(info, height);
		System.out.print("Write to file: ");
		String name = br.readLine();
		FileOutputStream out = new FileOutputStream(name);
		out.write(info.array());
		out.write(vv.array());
		out.write(cc.array());
		out.write(tt.array());
		out.close();
	}
}