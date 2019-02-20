package com.slavi.util;

import java.util.Formatter;

/**
 * @deprecated Use org.apache.commons:commons-lang3:org.apache.commons.lang3.StringEscapeUtils
 */
public class CEncoder {
	private static final String specialSymbolsShort = " !#$%&,/:;@^|";

	private static final String specialSymbols = " !\"#$%&'()*+,-./:;<=>?@[]^`{|}~";

	private static final String alphaNumericSymbols = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	private enum ESCAPE { None, NormalChar, Slash, Unicode, Octal }

	public enum CENCODE { Default, NoSpace, NoSpecialSymbolsShort, NoSpecialSymbols, NoSpecialSymbolsCustom, AlphaNumericOnly }

	/**
	 * @see #encode(String, com.slavi.util.CEncoder.CENCODE, String)
	 */
	public static String encode(String s) {
		return encode(s, CENCODE.Default, null);
	}

	/**
	 * @see #encode(String, com.slavi.util.CEncoder.CENCODE, String)
	 */
	public static String encode(String s, CENCODE flag) {
		return encode(s, flag, null);
	}

	/**
	 * Encodes the specified string in the Java String style, i.e. the
	 * special symbols like " is replaced by \" and \n by new line etc.
	 */
	public static String encode(String s, CENCODE flag, String customSpecialSymbols) {
		if (s == null)
			return null;
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
		String result = f.toString();
		f.close();
		return result;
	}

	/**
	 * Decodes a string previously encoded by {@link #encode(String)}
	 */
	public static String decode(String s) {
		if (s == null)
			return null;
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
