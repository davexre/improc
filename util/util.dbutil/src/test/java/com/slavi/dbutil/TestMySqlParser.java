package com.slavi.dbutil;

import com.slavi.dbutil.parser.MySqlParser;

public class TestMySqlParser {

	public static void main(String[] args) throws Exception {
		MySqlParser p = new MySqlParser(TestMySqlParser.class.getResourceAsStream("TestMySqlParser.txt")) {
			public void runCmd(int line, String cmd) {
				System.out.println(">> " + line + ": " + cmd);
				System.out.println();
			}
		};
		p.removeComments = false;
		p.parse();
/*		String str = "asd\nqwe\rzxc";
		System.out.println("|" + str.replaceAll("[^\n\r]", "") + "|");*/
	}
}
