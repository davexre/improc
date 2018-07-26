package com.slavi.dbutil;

import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import com.slavi.dbutil.parser.MySqlParser;

public class ScriptRunner2 {
	protected Connection connection;
	private boolean stopOnError = true;
	private int commitEveryNumSqls = 1000;
	private boolean removeComments = false;
	private Map<String, String> substVariables = null;

	public static class ScriptError extends RuntimeException {
		public ScriptError(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public ScriptRunner2(Connection connection) {
		this.connection = connection;
	}

	protected Statement statement;
	protected int countSqls = 0;
	public void runCmd(int line, String cmd) {
		try {
			boolean hasResultSet = statement.execute(cmd);
			if (hasResultSet) {
				try (ResultSet rs = statement.getResultSet()) {
					if (rs != null)
						System.out.println(ResultSetToString.resultSetToString(rs, 0, Integer.MAX_VALUE, true, 200));
				}
			} else {
				if (!connection.getAutoCommit() &&
					commitEveryNumSqls > 0 &&
					commitEveryNumSqls < ++countSqls) {
					connection.commit();
					countSqls = 0;
				};
			}
		} catch (SQLException e) {
			if (stopOnError) {
				throw new ScriptError("Error running sql command on line " + line, e);
			} else {
				System.err.println("Error running sql command on line " + line);
				e.printStackTrace();
			}
		}
	}

	public void runScript(Reader reader) throws Exception {
		try {
			statement = connection.createStatement();
			StrSubstitutor ss = new StrSubstitutor(substVariables);
			countSqls = 0;
			MySqlParser parser = new MySqlParser(reader) {
				public void runCmd(int line, String cmd) {
					cmd = ss.replace(cmd);
					ScriptRunner2.this.runCmd(line, cmd);
				}
			};
			parser.removeComments = removeComments;
			parser.parse();
		} finally {
			DbUtils.closeQuietly(statement);
			statement = null;
		}
	}

	public boolean isStopOnError() {
		return stopOnError;
	}

	public void setStopOnError(boolean stopOnError) {
		this.stopOnError = stopOnError;
	}

	public boolean isRemoveComments() {
		return removeComments;
	}

	public void setRemoveComments(boolean removeComments) {
		this.removeComments = removeComments;
	}

	public Map<String, String> getSubstVariables() {
		return substVariables;
	}

	public void setSubstVariables(Map<String, String> substVariables) {
		this.substVariables = substVariables;
	}

	public int getCommitEveryNumSqls() {
		return commitEveryNumSqls;
	}

	public void setCommitEveryNumSqls(int commitEveryNumSqls) {
		this.commitEveryNumSqls = commitEveryNumSqls;
	}
}
