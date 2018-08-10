package com.slavi.util.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;

public interface LineReader extends Closeable {
	String readLine() throws IOException;

	static LineReader toLineReader(BufferedReader r) {
		return new LineReader() {
			public String readLine() throws IOException {
				return r.readLine();
			}
			public void close() throws IOException {
				r.close();
			}
		};
	}

	static LineReader toLineReader(java.io.Console r) {
		return new LineReader() {
			public String readLine() throws IOException {
				return r.readLine();
			}
			public void close() throws IOException {
			}
		};
	}

	static LineReader toLineReader(java.io.DataInput r) {
		return new LineReader() {
			public String readLine() throws IOException {
				return r.readLine();
			}
			public void close() throws IOException {
				if (r instanceof Closeable)
					((Closeable) r).close();
			}
		};
	}

	static LineReader toLineReader(java.io.DataInputStream r) {
		return new LineReader() {
			public String readLine() throws IOException {
				return r.readLine();
			}
			public void close() throws IOException {
				r.close();
			}
		};
	}

	static LineReader toLineReader(java.io.LineNumberReader r) {
		return new LineReader() {
			public String readLine() throws IOException {
				return r.readLine();
			}
			public void close() throws IOException {
				r.close();
			}
		};
	}

	static LineReader toLineReader(java.io.ObjectInputStream r) {
		return new LineReader() {
			public String readLine() throws IOException {
				return r.readLine();
			}
			public void close() throws IOException {
				r.close();
			}
		};
	}

	static LineReader toLineReader(java.io.RandomAccessFile r) {
		return new LineReader() {
			public String readLine() throws IOException {
				return r.readLine();
			}
			public void close() throws IOException {
				r.close();
			}
		};
	}

	static LineReader toLineReader(CommentAwareLineNumberReader r) {
		return r;
	}
}
