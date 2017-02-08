package com.slavi.dbutil;

import java.sql.Connection;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyDbScriptRunner extends MyScriptParser {
	Logger log = LoggerFactory.getLogger(getClass());
	
	Connection conn;
	int numberOfStatementsInBatch = 20;
	
	public MyDbScriptRunner(Connection conn) {
		this.conn = conn;
	}

	public int getNumberOfStatementsInBatch() {
		return numberOfStatementsInBatch;
	}

	/**
	 * Sets the number of SQL statements that are executed 
	 * between two explicit conn.commit(). If negative then batch commit is disabled.
	 * It will have no effect if the conn.getAutoCommit() is true.
	 */
	public void setNumberOfStatementsInBatch(int numberOfStatementsInBatch) {
		this.numberOfStatementsInBatch = numberOfStatementsInBatch;
	}
	
	Statement statement;
	int cmdCount;
	
	public void onPrepare() throws Exception {
		statement = conn.createStatement();
		cmdCount = 0;
	}

	public void execSqlCommand(int cmdLineNumber, String command) throws Exception {
		cmdCount++;
		log.debug("Running SQL command line: {}, {}", cmdLineNumber, command);
		statement.execute(command);
		if (numberOfStatementsInBatch >= 0 && cmdCount >= numberOfStatementsInBatch) {
			cmdCount = 0;
			conn.commit();
		}
	}

	public void onException(int cmdLineNumber, String command, Exception e) throws Exception {
		log.error("Error executing SQL command on line {}, {}", cmdLineNumber, command);
		throw e;
	}

	public void onFinally() throws Exception {
		statement.close();
		if (cmdCount > 0)
			conn.commit();
	}
}
