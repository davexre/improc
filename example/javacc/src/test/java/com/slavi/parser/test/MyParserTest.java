package com.slavi.parser.test;

import java.io.StringReader;

import com.slavi.parser.MyParser;

public class MyParserTest {

	public static void main(String[] args) throws Exception {
		String str = "(a=123)and(b=\"1\\\"c\")";
		MyParser p = new MyParser(new StringReader(str));
		p.parse();
		System.out.println(p.sb.toString());
		System.out.println(p.paramVals);
	}
}
