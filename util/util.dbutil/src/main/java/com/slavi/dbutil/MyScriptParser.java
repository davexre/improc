package com.slavi.dbutil;

import java.io.LineNumberReader;
import java.io.Reader;

public class MyScriptParser {
	public void onPrepare() throws Exception {
	}

	public void onException(int cmdLineNumber, String command, Exception e) throws Exception {
		throw e;
	}

	public void onFinally() throws Exception {
	}
	
	public void execSqlCommand(int cmdLineNumber, String command) throws Exception {
	}

	int cmdLineNumber = 1;
	LineNumberReader r;
	StringBuilder cmd;
	
	void process() throws Exception {
		String c = cmd.toString().trim();
		try {
			if (!c.isEmpty()) {
				execSqlCommand(cmdLineNumber, c);
			}
		} catch (Exception e) {
			onException(cmdLineNumber, c, e);
		}
		cmd.setLength(0);
		cmdLineNumber = r.getLineNumber();
	}

	public void process(Reader fin) throws Exception {
		r = new LineNumberReader(fin);
		cmdLineNumber = 1;
		cmd = new StringBuilder();
		boolean blockComment = false;
		try {
			onPrepare();
			while (r.ready()) {
				String line = r.readLine();
				String tline = line.trim();
				if (tline.startsWith("/*")) {
					blockComment = true;
				}
				if (blockComment) {
					if (tline.endsWith("*/"))
						blockComment = false;
					continue;
				}
				if (tline.startsWith("--"))
					continue;
				if (!tline.endsWith(";")) {
					cmd.append(line);
					cmd.append("\n");
					continue;
				}
				line = line.substring(0, line.lastIndexOf(";"));
				cmd.append(line);
				process();
			}
			process();
		} finally {
			r.close();
			r = null;
			cmd = null;
			onFinally();
		}
	}
}
