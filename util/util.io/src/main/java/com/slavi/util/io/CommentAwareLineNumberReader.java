package com.slavi.util.io;

import java.io.IOException;
import java.io.Reader;

/**
 * Reader counting lines that skips lines beginning with any of the chars in commentChars list.
 * Line with comments are still counted.
 */
public class CommentAwareLineNumberReader extends Reader implements LineReader {
	String commentChars = "#";
	private int lineNumber = 1;
	Reader in;

	public CommentAwareLineNumberReader(Reader in) {
		this.in = in;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public void setCommentChars(String commentChars) {
		this.commentChars = commentChars == null ? "": commentChars;
	}

	public String getCommentChars() {
		return commentChars;
	}

	public String readLine() throws IOException {
		synchronized (lock) {
			if (curChar < 0)
				return null;
			StringBuilder sb = new StringBuilder();
			while (true) {
				read();
				if (curChar < 0)
					break;
				if (curChar == '\r' || curChar == '\n') {
					if (newLine)
						break;
				} else {
					sb.append((char) curChar);
				}
			}
			if (curChar < 0 && sb.length() == 0)
				return null;
			return sb.toString();
		}
	}

	int previousChar = 0;
	int curChar = '\n';
	boolean newLine = false;

	private void read0() throws IOException {
		if (newLine)
			lineNumber++;
		previousChar = curChar;
		curChar = in.read();
		if (curChar < 0)
			return;
		if (
			(curChar == '\n' && previousChar == '\r') ||
			(curChar == '\r' && previousChar == '\n')) {
			newLine = !newLine;
		} else if ( curChar == '\r' ||
					curChar == '\n') {
			newLine = true;
		} else {
			newLine = false;
		}
		return;
	}

	@Override
	public int read() throws IOException {
		synchronized (lock) {
			read0();
			while ((previousChar == '\r' || previousChar == '\n') && commentChars.indexOf(curChar) >= 0) {
				while (true) {
					read0();
					if (curChar < 0 || newLine) {
						read0();
						break;
					}
				}
			}
			return curChar;
		}
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		synchronized (lock) {
			int index = off;
			int maxIndex = off + len;
			int r;
			while ((index < maxIndex) && ((r = read()) >= 0)) {
				cbuf[index++] = (char) r;
			}
			return index - off;
		}
	}

	@Override
	public void close() throws IOException {
		in.close();
	}
}
