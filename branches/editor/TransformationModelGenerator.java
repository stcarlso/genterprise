import java.util.*;
import java.io.*;
import java.nio.*;

/**
 * NOTE: File is for testing purposes only.
 * 
 * This is NOT to belong in the final product.
 */
public class TransformationModelGenerator {
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("File to Modify: ");
		String name = br.readLine();
		System.out.print("Translate By [3f, frac GU]: ");
		StringTokenizer str = new StringTokenizer(br.readLine(), ",");
		float x = Float.parseFloat(str.nextToken());
		float y = Float.parseFloat(str.nextToken());
		float z = Float.parseFloat(str.nextToken());
		ByteBuffer info = ByteBuffer.allocate(12);
		FileInputStream fis = new FileInputStream(name);
		fis.read(info.array());
		info.rewind();
		int size = info.getInt(0);
		ByteBuffer buf = ByteBuffer.allocate(8 * 4 * size);
		fis.read(buf.array());
		fis.close();
		ByteBuffer vv = ByteBuffer.allocate(3 * 4 * size);
		ByteBuffer cc = ByteBuffer.allocate(3 * 4 * size);
		ByteBuffer tt = ByteBuffer.allocate(2 * 4 * size);
		FloatBuffer vertex = vv.asFloatBuffer();
		FloatBuffer color = cc.asFloatBuffer();
		FloatBuffer texCoord = tt.asFloatBuffer();
		for (int i = 0; i < size; i++) {
			vertex.put(buf.getFloat() + x);
			vertex.put(buf.getFloat() + y);
			vertex.put(buf.getFloat() + z);
		}
		for (int i = 0; i < size; i++) {
			color.put(buf.getFloat());
			color.put(buf.getFloat());
			color.put(buf.getFloat());
		}
		for (int i = 0; i < size; i++) {
			texCoord.put(buf.getFloat());
			texCoord.put(buf.getFloat());
		}
		vertex.rewind();
		color.rewind();
		texCoord.rewind();
		System.out.println("Transformed " + size + " vertices successfully.");
		System.out.print("New file: ");
		name = br.readLine();
		FileOutputStream out = new FileOutputStream(name);
		info.rewind();
		out.write(info.array());
		out.write(vv.array());
		out.write(cc.array());
		out.write(tt.array());
		out.close();
	}
}