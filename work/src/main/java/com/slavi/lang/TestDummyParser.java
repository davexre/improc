package com.slavi.lang;

import java.io.InputStream;
import java.nio.charset.Charset;

import com.slavi.tools.test.parser.DummyParser;

public class TestDummyParser {

	void doIt() throws Exception {
		try (InputStream is = getClass().getResourceAsStream("TestDummyParser.txt")) {
			DummyParser p = new DummyParser(is, Charset.forName("UTF8"));
			p.parse();
		}
	}

	public static void main(String[] args) throws Exception {
		new TestDummyParser().doIt();
		System.out.println("Done.");
	}
}
