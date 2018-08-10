package com.slavi.io;

import java.io.IOException;
import java.io.StringReader;

import com.slavi.util.io.CommentAwareLineNumberReader;

public class TestCommentAwareLineNumberReader {
	public static void main(String[] args) throws IOException {
//		String str = "#A1\nA2\r#\n#\r#A3\r\nA4\n\rA5\n#A6\rA7";
//		String str = "#A1\nA2\r#aaa\nb3\r#A3\r\nA4\n\rA5\n#A6\rA7";
		String str = "\n\ra";
		try (
			StringReader r = new StringReader(str);
			CommentAwareLineNumberReader rr = new CommentAwareLineNumberReader(r);
		) {
			String l;
			while ((l = rr.readLine()) != null) {
				System.out.println(rr.getLineNumber() + ": " + l);
			}
		}
	}
}
