package com.slavi.utils;

import java.io.File;
import java.util.Locale;

/**
 * This class contains utility static methods for general purpose.  
 */
public class Utl {
	/**
	 * Returns the "human" representation of a time delta specified in milliseconds
	 * like "1 hour 23 minutes 45.6 seconds".
	 * @param millis	the time delta in milliseconds
	 */
	public static String getFormatedMilliseconds(long millis) {
		final long[] divisors = { (1000 * 60 * 60 * 24), (1000 * 60 * 60),
				(1000 * 60), (1000) };
		final String[][] texts = { { " day ", " days " },
				{ " hour ", " hours " }, { " minute ", " minutes " },
				{ " second", " seconds" } };
		String s = new String("");
		for (int i = 0; i < 3; i++) {
			long tmp = millis / divisors[i];
			millis %= divisors[i];
			if (tmp > 0)
				s += Long.toString(tmp) + texts[i][tmp == 1 ? 0 : 1];
		}
		return s + String.format(Locale.US, "%1$.3f", new Object[] { new Double((double) (millis) / divisors[3]) } )
				+ texts[3][1];
	}

	/**
	 * Returns the "human" representation of a size in bytes
	 * like "123 bytes", "1.2 K", "2.3 M" or "3.4 G".
	 * @param sizeInBytes	the size in bytes.
	 */
	public static String getFormatBytes(long sizeInBytes) {
		String dim = "bytes";
		double size = sizeInBytes;
		if (Math.abs(size) >= 1000.0) {
			dim = "K";
			size /= 1000.0;
		}
		if (Math.abs(size) >= 800.0) {
			dim = "M";
			size /= 1000.0;
		}
		if (Math.abs(size) >= 800.0) {
			dim = "G";
			size /= 1000.0;
		}
		if (Math.abs(size) >= 800.0) {
			dim = "T";
			size /= 1000.0;
		}
		if (Math.floor(size) == size) 
			return String.format(Locale.US, "%d %s", new Object[] { new Integer((int)size), dim } );
		return String.format(Locale.US, "%.1f %s", new Object[] { new Double(size), dim } );
	}

	/**
	 * Returns the absolute path of the current directory.
	 */
	public static String getCurrentDir() {
		String result = ".";
		try {
			result = (new File(".")).getCanonicalPath();
		} catch (Exception e) {
		}
		return result;
	}
	
	/**
	 * Replaces the extension of fileName with the newExtension.
	 * <p>
	 * ex:
	 * <p>
	 * <table border=1>
	 * <tr><th>fileName</th><th>newExtension</th><th>result</th></tr>
	 * <tr><td>c:\temp\somefile.log</td><td>txt</td><td>c:\temp\somefile.txt</td></tr>
	 * <tr><td>c:\temp\somefile.log</td><td>&nbsp;</td><td>c:\temp\somefile.</td></tr>
	 * <tr><td>c:\temp\somefile</td><td>txt</td><td>c:\temp\somefile.txt</td></tr>
	 * <tr><td>c:\temp.tmp\somefile.log</td><td>txt</td><td>c:\temp.tmp\somefile.txt</td></tr>
	 * <tr><td>c:\temp.tmp\somefile</td><td>txt</td><td><b>c:\temp.txt</b></td></tr>
	 * </table>
	 */
	public static String chageFileExtension(String fileName, String newExtension) {
		int lastIndex = fileName.lastIndexOf(".");
		if (lastIndex < 0)
			return fileName + "." + newExtension;
		return fileName.substring(0, lastIndex) + "." + newExtension; 
	}

	/**
	 * The code bellow is borrowed from WedSphinx
	 * http://www.cs.cmu.edu/~rcm/websphinx
	 * and slightly modified 
	 * 
	 * Gets a wildcard pattern and returns a Regexp equivalent.  
	 * 
	 * Wildcards are similar to sh-style file globbing.
	 * A wildcard pattern is implicitly anchored, meaning that it must match the entire string.
	 * The wildcard operators are:
	 * <pre>
	 *    ? matches one arbitrary character
	 *    * matches zero or more arbitrary characters
	 *    [xyz] matches characters x or y or z
	 *    {foo,bar,baz}   matches expressions foo or bar or baz
	 *    ()  grouping to extract fields
	 *    \ escape one of these special characters
	 * </pre>
	 * Escape codes (like \n and \t) and Perl5 character classes (like \w and \s) may also be used.
	 */
	public static String toRegexpStr(String wildcard) {
		String s = wildcard;

		int inAlternative = 0;
		int inSet = 0;
		boolean inEscape = false;

		StringBuilder output = new StringBuilder();

		int len = s.length();
		for (int i = 0; i < len; ++i) {
			char c = s.charAt(i);
			if (inEscape) {
				output.append(c);
				inEscape = false;
			} else {
				switch (c) {
				case '\\':
					output.append(c);
					inEscape = true;
					break;
				case '?':
					output.append('.');
					break;
				case '*':
					output.append(".*");
					break;
				case '[':
					output.append(c);
					++inSet;
					break;
				case ']':
					// FIX: handle [] case properly
					output.append(c);
					--inSet;
					break;
				case '{':
					output.append("(?:");
					++inAlternative;
					break;
				case ',':
					if (inAlternative > 0)
						output.append("|");
					else
						output.append(c);
					break;
				case '}':
					output.append(")");
					--inAlternative;
					break;
				case '^':
					if (inSet > 0) {
						output.append(c);
					} else {
						output.append('\\');
						output.append(c);
					}
					break;
				case '$':
				case '.':
				case '|':
				case '+':
					output.append('\\');
					output.append(c);
					break;
				default:
					output.append(c);
					break;
				}
			}
		}
		if (inEscape)
			output.append('\\');

		return output.toString();
	}
}
