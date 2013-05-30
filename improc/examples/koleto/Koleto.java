package koleto;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Koleto {
	public static String stringToHex(String s) {
		StringBuilder sb = new StringBuilder();
		String prefix = "";
		for (int i = 0; i < s.length(); i++) {
			sb.append(prefix);
			prefix = ",";
			sb.append(String.format("%02X", (byte)s.charAt(i)));
		}
		return sb.toString();
	}
	
	public static String makeExtractPattern(String startSequence, String endSequence) {
		return startSequence + "(((?!" + startSequence + ")[^(" + endSequence + ")])+)" + endSequence;
	}

	public static String fileToString(String fileName) throws IOException {
		InputStream fin = new FileInputStream(fileName);
		StringBuilder sb = new StringBuilder();
		int b;
		while ((b = fin.read()) >= 0) {
			sb.append((char)b);
		}
		fin.close();
		return sb.toString();
	}

	public static void stringToFile(String str, String fileName) throws IOException {
		FileOutputStream fou = new FileOutputStream(fileName);
		for (int i = 0; i < str.length(); i++)
			fou.write(str.charAt(i));
		fou.close();
	}	

	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.out.println("Usage: <pattern file> <source file> <output file>");
			System.exit(1);
		}
		String patternFile = args[0];
		String sourceFile = args[1];
		String outputFile = args[2];

		String src = fileToString(sourceFile);
		
		src = src.replaceAll("E6000C", "N3900A");
		src = src.replaceAll("DE41304140", "DE41700453");

		String binStr = fileToString(patternFile);
		String binPattern = makeExtractPattern("AGSpecialInfo", ".SOR");
		Pattern pattern = Pattern.compile(binPattern);
		Matcher m = pattern.matcher(binStr);
		if (!m.find()) {
			throw new Exception("Pattern file does not contain expected sequence");
		}
		binStr = binStr.substring(m.start(), m.end());
		
		src.replaceAll(binPattern, binStr);
		stringToFile(src, outputFile);
	}
}
