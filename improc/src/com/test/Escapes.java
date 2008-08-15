package com.test;
import java.util.Formatter;


public class Escapes {

	public static String cEncode(String s) {
		System.out.println("Length = " + s.length());
		int max = s.length();
		Formatter f = new Formatter();
		for (int i = 0; i < max; i++) {
			char c = s.charAt(i);
			switch (c) {
				case '\0': f.format("%s", "\\0"); break;
				case '\b': f.format("%s", "\\b"); break;
				case '\t': f.format("%s", "\\t"); break;
				case '\f': f.format("%s", "\\f"); break;
				case '\r': f.format("%s", "\\r"); break;
				case '\"': f.format("%s", "\\\""); break;
				case '\'': f.format("%s", "\\'"); break;
				case '\\': f.format("%s", "\\\\"); break;
				default:
					if (f == null)
						f = new Formatter();
					if (c < 32 || c > 200) {
						f.format("\\%03o|", (int)c);
					} else {
						f.format("\\u%1$04x (%1$c)|", (int)c);
//						b.append(f.format("\\u%1$04x (%1$c)|", (int)c));
					}
					break;
			}
		}
		return f.toString();
	}

/*
	public static String cEncode(String s) {
		System.out.println("Length = " + s.length());
		StringBuilder b = new StringBuilder();
		int max = s.length();
		Formatter f = null;
		for (int i = 0; i < max; i++) {
			char c = s.charAt(i);
			switch (c) {
				case '\0': b.append("\\0"); break;
				case '\b': b.append("\\b"); break;
				case '\t': b.append("\\t"); break;
				case '\f': b.append("\\f"); break;
				case '\r': b.append("\\r"); break;
				case '\"': b.append("\\\""); break;
				case '\'': b.append("\\'"); break;
				case '\\': b.append("\\\\"); break;
				default:
					if (f == null)
						f = new Formatter();
					if (c < 32 || c > 200) {
						b.append(f.format("\\%03o|", (int)c));
					} else {
						String tmp = f.format("\\u%1$04x (%2$d)|", (int)c, i).toString();
						System.out.println("[" + tmp + "]");
						b.append(tmp);
//						b.append(f.format("\\u%1$04x (%1$c)|", (int)c));
//						b.append(c);
					}
					break;
			}
		}
		return b.toString();
	}

 */	
	public static String cDecode(String s) {
		StringBuilder b = new StringBuilder();
		int max = s.length();
		int escape = 0;
		for (int i = 0; i < max; i++) {
			char c = s.charAt(i);
			if (escape == 1) {
				switch (c) {
					case 'b': b.append('\b'); escape = 0; break;
					case 't': b.append('\t'); escape = 0; break;
					case 'f': b.append('\f'); escape = 0; break;
					case 'r': b.append('\r'); escape = 0; break;
					case '\"': b.append('\"'); escape = 0; break;
					case '\'': b.append('\''); escape = 0; break;
					case 'u': escape = 2; break;
					case 'x': escape = 3; break;
					default:
//						if ((c >= '0') && (c <= ))
				}
				
			}
			
			if (c == '\\') {
				escape = 1;
				continue;
			}
		}
		return "";
	}
	
	public static void main(String[] args) {
		System.out.println(cEncode("\2"));
	}
}
