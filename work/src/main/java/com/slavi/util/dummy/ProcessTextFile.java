package com.slavi.util.dummy;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessTextFile {

	public static class CsvTokenizer {

		public static Pattern makePattern(String separatorChars, String quoteChars) {
			StringBuilder b = new StringBuilder();
			b.append("(");
			for (char c : quoteChars.toCharArray()) {
				b.append("(").append(c)
					.append("([^").append(c).append("]|(").append(c).append(c).append("))*")
					.append(c).append(")|");
			}
			b.append("[^").append(separatorChars).append("])*");
			// separatorChars = ",";
			// quoteChars = "'";
			// return Pattern.compile("(('([^']|(''))*')|[^,])*");
			return Pattern.compile(b.toString());
		}

		final String separatorChars;
		final String quoteChars;
		Pattern pattern;
		String line;
		int column;
		int index;
		Matcher m;

		public CsvTokenizer() {
			this("\t,");
		}

		public CsvTokenizer(String separatorChars) {
			this(separatorChars, "\"'`");
		}

		public CsvTokenizer(String separatorChars, String quoteChars) {
			this.separatorChars = separatorChars;
			this.quoteChars = quoteChars;
			this.pattern = makePattern(separatorChars, quoteChars);
			setLine(null);
		}

		public void setLine(String line) {
			index = 0;
			column = 0;
			this.line = line;
			this.m = line == null ? null : pattern.matcher(line);
		}

		public String nextColumn() {
			if (line != null && index < line.length()) {
				if (m.find(index)) {
					index = m.end() + 1;
					column++;
					return m.group().trim();
				}
			}
			index = Integer.MAX_VALUE;
			return null;
		}

		public String nextColumnUnquote() {
			return unquote(nextColumn());
		}

		public int getColumn() {
			return column;
		}

		public String unquote(String token) {
			if (token == null || "".equals(token))
				return token;
			StringBuilder sb = new StringBuilder();
			boolean inQuote = false;
			char quote = 0;
			char c = 0;
			char prevChar = 0;
			for (int i = 0; i < token.length(); i++) {
				prevChar = c;
				c = token.charAt(i);
				if (inQuote) {
					if (c == quote) {
						inQuote = false;
					} else {
						sb.append(c);
					}
				} else if (quoteChars.indexOf(c) >= 0) {
					if (prevChar == c)
						sb.append(c);
					inQuote = true;
					quote = c;
				} else {
					sb.append(c);
				}
			}
			return sb.toString();
		}

		public String quote(String token) {
			return quote(token, quoteChars == null || quoteChars.length() == 0 ? '\'' : quoteChars.charAt(0));
		}

		public String quote(String token, char quote) {
			if (token == null || "".equals(token))
				return token;
			StringBuilder sb = new StringBuilder();
			sb.append(quote);
			for (int i = 0; i < token.length(); i++) {
				char c = token.charAt(i);
				if (c == quote)
					sb.append(c);
				sb.append(c);
			}
			return sb.toString();
		}
	}

	void doIt() throws Exception {
		CsvTokenizer t = new CsvTokenizer();
		try (
			LineNumberReader fin = new LineNumberReader(new InputStreamReader(getClass().getResourceAsStream("ProcessTextFile.txt")));
		) {
			String line, s;
			StringBuilder b = new StringBuilder();
			while ((line = fin.readLine()) != null) {
				t.setLine(line);
				b.setLength(0);
				while ((s = t.nextColumn()) != null) {
					if (
						t.getColumn() == 4 ||
						t.getColumn() == 13
					) {
						s = t.unquote(s);
					}
					b.append(s);
					b.append(",");
				}
				System.out.println(b.toString());
			}
		}
	}

	void doIt2() throws Exception {
		CsvTokenizer t = new CsvTokenizer();
		//t.pattern = Pattern.compile("(([^']|(''))|([^\"]|(\"\")))*");
		String s = "aaa,'qqq''zzz',`53, qwe`,asd,sss\"sss\"sss,,ddd,\"fff\"),";
//		String s = "'a'),,a";
		t.setLine(s);
		System.out.println(s);
		while ((s = t.nextColumn()) != null) {
			System.out.println(t.unquote(s));
		}
	}

	public static void main(String[] args) throws Exception {
		new ProcessTextFile().doIt();
		System.out.println("Done.");
	}
}
