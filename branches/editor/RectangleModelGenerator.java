import java.util.*;
import java.io.*;
import java.nio.*;

/**
 * NOTE: File is for testing purposes only.
 * 
 * This is NOT to belong in the final product.
 */
public class RectangleModelGenerator {
	public static final float[] VV = new float[] {
		0.f, 0.f, 0.f,
		1.f, 0.f, 0.f,
		1.f, 1.f, 0.f,
		0.f, 0.f, 0.f,
		0.f, 1.f, 0.f,
		1.f, 1.f, 0.f
	};
	public static final float[] TT = new float[] {
		0.f, 1.f,
		1.f, 1.f,
		1.f, 0.f,
		0.f, 1.f,
		0.f, 0.f,
		1.f, 0.f,
	};

	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Width (Grid Units): ");
		int width = Integer.parseInt(br.readLine());
		System.out.print("Height (Grid Units): ");
		int height = Integer.parseInt(br.readLine());
		// size = 6
		ByteBuffer vv = ByteBuffer.allocate(3 * 4 * 6);
		ByteBuffer cc = ByteBuffer.allocate(3 * 4 * 6);
		ByteBuffer tt = ByteBuffer.allocate(2 * 4 * 6);
		vv.rewind();
		cc.rewind();
		tt.rewind();
		FloatBuffer vertex = vv.asFloatBuffer();
		FloatBuffer color = cc.asFloatBuffer();
		FloatBuffer texCoord = tt.asFloatBuffer();
		float r = 1.f, g = 1.f, b = 1.f; StringTokenizer str;
		for (int i = 0; i < 6; i++) {
			vertex.put(VV[3 * i] * width);
			vertex.put(VV[3 * i + 1] * height);
			vertex.put(VV[3 * i + 2]);
			texCoord.put(TT[2 * i]);
			texCoord.put(TT[2 * i + 1]);
			System.out.println("I: " + (i + 1) + "/6");
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
		}
		ByteBuffer info = ByteBuffer.allocate(12);
		info.putInt(6);
		info.putInt(width);
		info.putInt(height);
		info.rewind();
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