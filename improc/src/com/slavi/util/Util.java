package com.slavi.util;

import java.io.File;
import java.util.Formatter;
import java.util.Locale;

/**
 * This class contains utility static methods for general purpose.  
 */
public class Util {
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
	 * <tr><td><b>c:\temp.tmp\somefile</b></td><td><b>txt</b></td><td><b>c:\temp.txt</b></td></tr>
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

	/**
	 * @see #cEncode(String, com.slavi.util.Utl.CENCODE, String) 
	 */
	public static String cEncode(String s) {
		return cEncode(s, CENCODE.Default, null);
	}
	
	/**
	 * @see #cEncode(String, com.slavi.util.Utl.CENCODE, String) 
	 */
	public static String cEncode(String s, CENCODE flag) {
		return cEncode(s, flag, null);
	}
	
	public enum CENCODE { Default, NoSpace, NoSpecialSymbolsShort, NoSpecialSymbols, NoSpecialSymbolsCustom, AlphaNumericOnly }
	
	private static final String specialSymbolsShort = " !#$%&,/:;@^|";
	
	private static final String specialSymbols = " !\"#$%&'()*+,-./:;<=>?@[]^`{|}~";
	
	private static final String alphaNumericSymbols = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	/**
	 * Encodes the specified string in the Java String style, i.e. the
	 * special symbols like " is replaced by \" and \n by new line etc. 
	 */
	public static String cEncode(String s, CENCODE flag, String customSpecialSymbols) {
		String curSpecialSymbols;
		switch (flag) {
			case NoSpace:
				curSpecialSymbols = " ";
				break;
			case NoSpecialSymbolsShort:
				curSpecialSymbols = specialSymbolsShort;
				break;
			case NoSpecialSymbols:
				curSpecialSymbols = specialSymbols;
				break;
			case NoSpecialSymbolsCustom:
				curSpecialSymbols = customSpecialSymbols;
				if (curSpecialSymbols == null)
					curSpecialSymbols = "";
				break;
			case Default:
			case AlphaNumericOnly:
			default:
				curSpecialSymbols = "";
				break;
		}
		
		int max = s.length();
		Formatter f = new Formatter();
		for (int i = 0; i < max; i++) {
			char c = s.charAt(i);
			int ic = (int) c;
			switch (c) {
				case '\0': 
					f.format("%s", "\\0"); 
					continue;
				case '\b': 
					f.format("%s", "\\b"); 
					continue;
				case '\t': 
					f.format("%s", "\\t"); 
					continue;
				case '\f': 
					f.format("%s", "\\f"); 
					continue;
				case '\r': 
					f.format("%s", "\\r"); 
					continue;
				case '\n': 
					f.format("%s", "\\n"); 
					continue;
				case '\\': 
					f.format("%s", "\\\\"); 
					continue;
				case '\"':
				case '\'': 
					if (curSpecialSymbols.indexOf(c) < 0) {
						f.format("\\%c", c); 
						continue;
					}
			}
			if ((flag == CENCODE.Default) && (ic >= 32) && (ic < 256)) {
				f.format("%c", c);
			} else if ((flag == CENCODE.AlphaNumericOnly) && (alphaNumericSymbols.indexOf(c) >= 0)) { 
				f.format("%c", c);
			} else if ((curSpecialSymbols != null) && (ic >= 32) && (ic < 256) && (curSpecialSymbols.indexOf(c) < 0)) {
				f.format("%c", c);
			} else {
				if ((ic >= 0) && (ic < 256))
					f.format("\\%03o", ic);
				else
					f.format("\\u%04x", ic);
			}
		}
		return f.toString();
	}

	private enum ESCAPE { None, NormalChar, Slash, Unicode, Octal }

	/**
	 * Decodes a string previously encoded by {@link #cEncode(String)} 
	 */
	public static String cDecode(String s) {
		StringBuilder b = new StringBuilder();
		int max = s.length();
		ESCAPE escape = ESCAPE.None;
		int digit = 0;
		int digitCount = 0;
		char c = ' ';
		int i = 0;
		while (true) {
			switch (escape) {
			case None:
				escape = ESCAPE.NormalChar;
				break;
				
			case NormalChar: 
				if (c == '\\') {
					escape = ESCAPE.Slash;
				} else {
					b.append(c);
				}
				break;
				
				
			case Slash: 
				switch (c) {
				case 'b': b.append('\b'); escape = ESCAPE.NormalChar; break;
				case 't': b.append('\t'); escape = ESCAPE.NormalChar; break;
				case 'f': b.append('\f'); escape = ESCAPE.NormalChar; break;
				case 'r': b.append('\r'); escape = ESCAPE.NormalChar; break;
				case 'n': b.append('\n'); escape = ESCAPE.NormalChar; break;
				case '\"': b.append('\"'); escape = ESCAPE.NormalChar; break;
				case '\'': b.append('\''); escape = ESCAPE.NormalChar; break;
				case '\\': b.append('\\'); escape = ESCAPE.NormalChar; break;
				case 'u': digitCount = 0; escape = ESCAPE.Unicode; break;
				default:
					if ((c >= '0') && (c <= '9')) {
						digit = (int) (c - '0');
						digitCount = 1;
						escape = ESCAPE.Octal;
					} else {
						b.append('\\');
						b.append(c);
						escape = ESCAPE.NormalChar;
					}
				}
				break;
				
			case Unicode: {
				digitCount++;
				int digit2;
				if ((c >= '0') && (c <= '9'))
					digit2 = (int) (c - '0');
				else if ((c >= 'A') && (c <= 'F'))
					digit2 = (int) (c - 'A') + 10;
				else if ((c >= 'a') && (c <= 'f'))
					digit2 = (int) (c - 'a') + 10;
				else
					digit2 = -1;
				
				if (digit2 >= 0) {
					digit = (digit << 4) | digit2;
					if (digitCount == 4) {
						b.append((char) digit);
						escape = ESCAPE.NormalChar;
					}						
				} else {
					b.append("\\u");
					for (int i2 = 0; i2 < digitCount; i2++)
						b.append(s.charAt(i - i2));
				}
				break;
			}
			
			case Octal: {
				digitCount++;
				int digit2 = (digit << 3) | (int) (c - '0');
				if ((c >= '0') && (c <= '9') && (digit2 <= 255) && (digitCount <= 3)) {
					digit = digit2; 
				} else {
					b.append((char) digit);
					escape = ESCAPE.NormalChar;
					continue; // check the char in c
				}
				break;
			}
			}
			
			if (i < max)
				c = s.charAt(i++);
			else
				break;
		}
		
		switch (escape) {
		case None:
			break;
		case NormalChar:
			break;
		case Slash:
			b.append('\\');
			break;
		case Unicode:
			if (digitCount == 4) { 
				b.append((char) digit);
			} else {
				b.append("\\u");
				for (int i2 = max - digitCount; i2 < max; i2++)
					b.append(s.charAt(i2));
			}
			break;
		case Octal:
			b.append((char) digit);
			break;
		}
		
		return b.toString();
	}
}
