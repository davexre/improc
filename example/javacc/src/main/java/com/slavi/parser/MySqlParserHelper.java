package com.slavi.parser;

public class MySqlParserHelper {
	public StringBuilder cmd = new StringBuilder();


	boolean needsSlash = false;
	boolean slashBlocked = false;

	public void add2(String str) {
		if (!slashBlocked)
			needsSlash = true;
		System.out.println(">" + str + "<");
		doAdd(str);
	}

	public void add(String str) {
		slashBlocked = true;
		System.out.println("|" + str + "|");
		doAdd(str);
	}

	void execCmd() {
		System.out.println(">> " + cmd);
		cmd.setLength(0);
		needsSlash = false;
		slashBlocked = false;
	}

	void doAdd(String str) {
		if ("/".equals(str) ||
			((!needsSlash) && ";".equals(str))) {
			execCmd();
		} else {
			cmd.append(str).append(" ");
		}
	}
}
