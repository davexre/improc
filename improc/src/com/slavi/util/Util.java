package com.slavi.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
	 * Example:
	 * <p>
	 * <table border=1>
	 * <tr><th>fileName</th><th>newExtension</th><th>result</th></tr>
	 * <tr><td>c:\temp\somefile.log</td><td>txt</td><td>c:\temp\somefile.txt</td></tr>
	 * <tr><td>c:\temp\somefile.log</td><td>&nbsp;</td><td>c:\temp\somefile.</td></tr>
	 * <tr><td>c:\temp\somefile</td><td>txt</td><td>c:\temp\somefile.txt</td></tr>
	 * <tr><td>c:\temp.tmp\somefile.log</td><td>txt</td><td>c:\temp.tmp\somefile.txt</td></tr>
	 * <tr><td><b>c:\temp.tmp\somefile</b></td><td><b>txt</b></td><td><b>c:\temp.tmp\somefile.txt</b></td></tr>
	 * </table>
	 */
	public static String changeFileExtension(String fileName, String newExtension) {
		int lastPath = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
		int lastIndex = fileName.lastIndexOf('.');
		if ((lastPath > lastIndex) || (lastIndex < 0))
			return fileName + '.' + newExtension;
		return fileName.substring(0, lastIndex) + '.' + newExtension; 
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

	/**
	 * Replaces a set of files in a zip file in one zip copy operation.
	 * If zipfin = null then a new zip file is generated in zipfou.
	 */
	public static void replaceFilesInZip(InputStream zipfin, OutputStream zipfou, Map<String, InputStream> filesToReplace) throws IOException {
		byte buf[] = new byte[1024];
		ZipInputStream zin = null;
		ZipOutputStream zou = new ZipOutputStream(zipfou);
		try {
			if (zipfin != null) {
				zin = new ZipInputStream(zipfin);
				ZipEntry entryIn = null;
				while ((entryIn = zin.getNextEntry()) != null) {
					if (!filesToReplace.containsKey(entryIn.getName())) {
						ZipEntry entryOut = (ZipEntry) entryIn.clone();
						entryOut.setCompressedSize(-1);
						zou.putNextEntry(entryOut);
						while (zin.available() > 0) {
							int len = zin.read(buf);
							if (len > 0)
								zou.write(buf, 0, len);
						}
						zou.closeEntry();
					}
				}
				zin.close();
				zin = null;
			}
	        for (Map.Entry<String, InputStream> item : filesToReplace.entrySet()) {
				ZipEntry entryOut = new ZipEntry(item.getKey());
				entryOut.setCompressedSize(-1);
				zou.putNextEntry(entryOut);
				InputStream itemfin = item.getValue();
				itemfin.reset();
				while (itemfin.available() > 0) {
					int len = itemfin.read(buf);
					if (len > 0)
						zou.write(buf, 0, len);
				}
				zou.closeEntry();
			}
		} finally {
			if (zin != null)
				zin.close();
			zou.close();
		}
	}

	/**
	 * Sets global proxy settings. The string has the following format:
	 * <code>
	 * [proxy type]:[proxy address]:[port]:[usename]:[password]:[no proxy address list]
	 * </code>
	 * Where:<br>
	 * [proxy type] is one of the following HTTP or SOCKS<br>
	 * [proxy address] is the name or ip of the proxy<br>
	 * [port] is the proxy port. default is 80<br>
	 * [username] is the proxy username. if empty - no user required for proxy<br>
	 * [password] is the proxy password. if empty - no password required for proxy<br>
	 * [no proxy address list] hosts which should be connected too directly and 
	 * 		not through the proxy server. The value can be a list of hosts, each 
	 * 		seperated by a |, and in addition a wildcard character (*) can be 
	 * 		used for matching. For example: "*.foo.com|localhost"<br>
	 * <code>
	 * Examples:
	 * "HTTP:proxy:80:ivan:ivan123:*.foo.com|localhost"
	 * "HTTP:proxy:80:::localhost"
	 * "SOCKS:proxy5:1080:ivan:ivan123:*.foo.com|localhost"
	 * "SOCKS:proxy4:1080:::localhost"
	 * "" -> no proxy/clear proxy settings
	 * </code>
	 */
	public static void setProxy(String proxySettings) {
		String settings[] = proxySettings == null ? null : proxySettings.split(":");
		int count = settings == null ? 0 : settings.length;
		String type = 0 < count ? settings[0] : "";
		String host = 1 < count ? settings[1] : "";
		String port = 2 < count ? settings[2] : "";
		String user = 3 < count ? settings[3] : "";
		String pass = 4 < count ? settings[4] : "";
		String noProxyList = 5 < count ? settings[5] : "";
		
		Properties props = System.getProperties();
		
		props.remove("http.proxyHost");
		props.remove("http.proxyPort");
		props.remove("http.proxyUser");
		props.remove("http.proxyPassword");
		props.remove("http.http.nonProxyHosts");
		
		props.remove("https.proxyHost");
		props.remove("https.proxyPort");
		props.remove("https.http.nonProxyHosts");
		
		props.remove("ftp.proxyHost");
		props.remove("ftp.proxyPort");
		props.remove("ftp.nonProxyHosts");
		
		props.remove("socksProxyHost");
		props.remove("socksProxyPort");
		props.remove("java.net.socks.username");
		props.remove("java.net.socks.password");

		if ("HTTP".equalsIgnoreCase(type)) {
			if (!"".equals(host)) {
				props.put("http.proxyHost", host);
				props.put("https.proxyHost", host);
				props.put("ftp.proxyHost", host);
			}
			if (!"".equals(port)) {
				props.put("http.proxyPort", port);
				props.put("https.proxyPort", port);
				props.put("ftp.proxyPort", port);
			}
			if (!"".equals(user)) {
				props.put("http.proxyUser", user);
			}
			if (!"".equals(pass)) {
				props.put("http.proxyPassword", pass);
			}
			if (!"".equals(noProxyList)) {
				props.put("http.nonProxyHosts", noProxyList);
				props.put("https.nonProxyHosts", noProxyList);
				props.put("ftp.nonProxyHosts", noProxyList);
			}
		} else if ("SOCKS".equalsIgnoreCase(type)) {
			if (!"".equals(host)) {
				props.put("socksProxyHost", host);
			}
			if (!"".equals(port)) {
				props.put("socksProxyPort", port);
			}
			if (!"".equals(user)) {
				props.put("java.net.socks.username", user);
			}
			if (!"".equals(pass)) {
				props.put("java.net.socks.password", pass);
			}
		}
	}
}
