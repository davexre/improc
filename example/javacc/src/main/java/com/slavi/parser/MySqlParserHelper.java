package com.slavi.parser;

public class MySqlParserHelper {
	public StringBuilder cmd = new StringBuilder();

	int line;
	boolean needsSlash = false;
	boolean slashBlocked = false;

	public void add2(Token token) {
		if (!slashBlocked)
			needsSlash = true;
		//System.out.println(">" + str + "<");
		doAdd(token);
	}

	public void add(Token token) {
		slashBlocked = true;
		//System.out.println("|" + str + "|");
		doAdd(token);
	}

	public void addComment(Token token) {
		if (cmd.length() > 0)
			doAdd(token);
	}

	void execCmd(Token token) {
		if (cmd.length() > 0) {
			System.out.println(">> " + line + ": " + cmd);
			System.out.println();
		}
		cmd.setLength(0);
		needsSlash = false;
		slashBlocked = false;
		line = 0;
	}

	void doAdd(Token token) {
		if ("/".equals(token.image) ||
			((!needsSlash) && ";".equals(token.image))) {
			execCmd(token);
		} else {
			if (cmd.length() == 0)
				line = token.beginLine;
			cmd.append(token);
		}
	}
}
