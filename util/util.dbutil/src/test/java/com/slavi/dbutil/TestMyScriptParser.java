package com.slavi.dbutil;

import java.io.InputStreamReader;

public class TestMyScriptParser extends MyScriptParser {
	public void execSqlCommand(int cmdLineNumber, String command) throws Exception {
		System.out.println(command);
	}

	void doIt() throws Exception {
		process(new InputStreamReader(getClass().getResourceAsStream("TestMyScriptParser.sql.txt")));
	}

	public static void main(String[] args) throws Exception {
		new TestMyScriptParser().doIt();
		System.out.println("Done.");
	}
}
