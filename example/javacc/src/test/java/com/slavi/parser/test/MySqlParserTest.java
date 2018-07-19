package com.slavi.parser.test;

import com.slavi.parser.MySqlParser;

public class MySqlParserTest {

	public static void main(String[] args) throws Exception {
		MySqlParser p = new MySqlParser(MySqlParserTest.class.getResourceAsStream("MySqlParser.txt"));
		p.parse();
	}
}
