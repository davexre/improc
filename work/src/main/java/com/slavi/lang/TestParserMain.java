package com.slavi.lang;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.apache.commons.text.lookup.StringLookupFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.tools.test.parser.TestParser;
import com.slavi.util.Util;

public class TestParserMain {

	public static class Content {
		public ArrayList content = new ArrayList();
		public String operation = "";
	}

	public static class Element extends Content {
		public String name;
	}

	public static class ChildElement {
		public String name;
		public String modifier = "";
	}

	void doIt() throws Exception {
		try (InputStream is = getClass().getResourceAsStream("TestParserMain.dtd")) {
			TestParser p = new TestParser(is, Charset.forName("UTF8"));
			p.parse();
			ObjectMapper m = Util.jsonMapper();
			System.out.println(m.writeValueAsString(p.elements));
		}
	}

	void doIt2() throws Exception {
		String s = "asd %QWE;zxc";
		Map<String, String> map = new HashMap<>();
		map.put("x", "XXX");
		map.put("qwe", "z%x;c");
		StringSubstitutor ss = new StringSubstitutor((key) -> key == null ? null : map.get(key.toLowerCase()), "%", ";", '\0');
		System.out.println(ss.replace(s));
	}

	public static void main(String[] args) throws Exception {
		new TestParserMain().doIt2();
		System.out.println("Done.");
	}
}
