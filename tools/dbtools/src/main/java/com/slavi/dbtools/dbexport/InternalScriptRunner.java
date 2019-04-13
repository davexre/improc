package com.slavi.dbtools.dbexport;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.slavi.dbutil.DbUtil;
import com.slavi.dbutil.ScriptRunner2;

class InternalScriptRunner extends ScriptRunner2 {
	public String fnpattern;
	public String tables[];
	public boolean createTablesScript;
	public ExportResultSet exporter;

	InternalScriptRunner(Connection connection) {
		super(connection);
		setRemoveComments(true);
	}

	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	Set<String> usedNames = new HashSet<>();
	String getOutputName() {
		String replaceD = dateFormat.format(new Date());
		String replaceN = Integer.toString(sqlCounter+1);
		String replaceT = null;
		if (tables != null && sqlCounter < tables.length) {
			replaceT = tables[sqlCounter];
		} else {
			replaceT = "TABLE_" + replaceN;
		}

		String startName = StringUtils.trimToNull(fnpattern.replaceAll("%t", replaceT).replaceAll("%d", replaceD).replaceAll("%n", replaceN));
		String prefix = FilenameUtils.removeExtension(startName) + "_";
		String suffix = FilenameUtils.getExtension(startName);
		if (!"".equals(suffix))
			suffix = "." + suffix;
		String r = startName;
		int counter = 0;
		while (true) {
			if (r != null && !usedNames.contains(r)) {
				usedNames.add(r);
				break;
			}
			counter++;
			r = prefix + counter + suffix;
		}
		return r;
	}

	int sqlCounter = 0;
	public void runCmd(int line, String cmd) {
		cmd = StringUtils.trimToEmpty(cmd);
		if (!(cmd.startsWith("select") || cmd.startsWith("with"))) {
			if (isStopOnError()) {
				throw new ScriptRunner2.ScriptError("Not a select statement on line " + line, new SQLException("Error running " + cmd));
			} else {
				System.err.println("Not a select statement on line " + line + ". Statement is\n" + cmd);
				return;
			}
		}

		try {
			boolean hasResultSet = statement.execute(cmd);
			if (hasResultSet) {
				try (ResultSet rs = statement.getResultSet()) {
					if (rs == null) {
						// Report an error?
						return;
					}
					String fou = getOutputName();
					String tableName;
					if (tables != null && sqlCounter < tables.length) {
						tableName = tables[sqlCounter];
					} else {
						tableName = FilenameUtils.getBaseName(fou).toUpperCase();
					}
					if (createTablesScript) {
						ResultSetMetaData meta = rs.getMetaData();
						PrintWriter out = new PrintWriter(new File(FilenameUtils.removeExtension(fou) + "-create-table.sql"));
						out.println(DbUtil.resultSet2ddl(meta, tableName));
						out.close();
					}
					exporter.export(rs, fou, tableName);
				}
				sqlCounter++;
			}
		} catch (Exception e) {
			if (isStopOnError()) {
				throw new ScriptError("Error running sql command on line " + line, e);
			} else {
				System.err.println("Error running sql command on line " + line);
				e.printStackTrace();
			}
		}
	}
}
