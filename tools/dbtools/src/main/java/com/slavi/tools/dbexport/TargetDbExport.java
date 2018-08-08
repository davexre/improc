package com.slavi.tools.dbexport;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.slavi.dbutil.DbUtil;

class TargetDbExport implements ExportResultSet {

	Connection conn;
	String existingTables;
	int commitEveryNumRows;

	public TargetDbExport(Connection conn, String existingTables, int commitEveryNumRows) {
		this.conn = conn;
		this.existingTables = existingTables;
		this.commitEveryNumRows = commitEveryNumRows;
	}

	public void export(ResultSet rs, String fouName, String tableName) throws Exception {
		conn.setAutoCommit(false);
		Statement tst = conn.createStatement();
		boolean tableExists = true;
		try (ResultSet trs = tst.executeQuery("select * from " + tableName + " where 1=0")) {
		} catch (SQLException e) {
			tableExists = false;
		};

		boolean useColumnNames = true;
		if ("truncate".equals(existingTables)) {
			if (tableExists) {
				tst.executeUpdate("delete from " + tableName);
			}
		} else if ("append".equals(existingTables)) {
			// Do nothing
		} else { // if ("drop".equals(existingTables)) {
			if (tableExists) {
				tst.executeUpdate("drop table " + tableName);
				tableExists = false;
			}
		}

		ResultSetMetaData meta = rs.getMetaData();
		if (!tableExists) {
			String sql = DbUtil.resultSet2ddl(meta, tableName);
			tst.executeUpdate(sql);
			useColumnNames = false;
		}
		tst.close();

		DbUtil.copyResultSet(rs, conn, tableName, commitEveryNumRows, useColumnNames);
	}
}