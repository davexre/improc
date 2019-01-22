package com.slavi.lang;

import java.io.InputStream;
import java.nio.charset.Charset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.tools.test.parser.TestParser2;
import com.slavi.tools.test.parser.TestParser2Constants;
import com.slavi.tools.test.parser.Token;
import com.slavi.util.Util;

public class TestParser2Main {

	void doIt() throws Exception {
		try (InputStream is = getClass().getResourceAsStream("TestParser2Main.txt")) {
			TestParser2 p = new TestParser2(is, Charset.forName("UTF8"));
			p.setTabSize(4);
			Token t = p.token;
			p.parse();
			while ((t = t.next) != null) {
				if (t.specialToken != null) {
					System.out.println(t.specialToken.beginLine + ":" + t.specialToken.beginColumn + " " + TestParser2Constants.tokenImage[t.specialToken.kind] + " = " + t.specialToken.image);
				}
				System.out.println(t.beginLine + ":" + t.beginColumn + " " + TestParser2Constants.tokenImage[t.kind] + " = " + t.image);
			}
			ObjectMapper m = Util.jsonMapper();
			System.out.println(m.writeValueAsString(t));
		}
	}

	public static void main(String[] args) throws Exception {
		new TestParser2Main().doIt();
		System.out.println("Done.");
	}
}
