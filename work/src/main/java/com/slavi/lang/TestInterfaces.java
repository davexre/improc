package com.slavi.lang;

import java.io.IOException;
import java.io.StringReader;

import com.slavi.util.io.CommentAwareLineNumberReader;

public class TestInterfaces {

	@FunctionalInterface
	public interface LineReader {
		public String readLine() throws IOException;
	}

	private void asd(LineReader lr) throws IOException {
		System.out.println(lr.readLine());
	}

	public void doIt(String[] args) throws Exception {
		try (
			StringReader r = new StringReader("#A1\nA2\r#A3\r\nA4\n\rA5\n#A6\rA7");
			CommentAwareLineNumberReader rr = new CommentAwareLineNumberReader(r);
		) {
			asd(() -> rr.readLine());
		}
	}

	public static void main(String[] args) throws Exception {
		new TestInterfaces().doIt(args);
		System.out.println("Done.");
	}
}
