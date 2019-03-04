package com.slavi.dbutil;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

public class ResultSetToIteratorList implements Iterator<List<Object>> {
	ResultSet rs;
	int columns;
	List<Object> row;
	boolean didNext = false;
	boolean hasNext = false;
	List<String> columnNames;


	public ResultSetToIteratorList(ResultSet rs) throws SQLException {
		this.rs = rs;
		ResultSetMetaData meta = rs.getMetaData();
		columns = meta.getColumnCount();
		row = new ArrayList<>(columns);
		columnNames = new ArrayList<>(columns);
		for (int i = 1; i <= columns; i++)
			columnNames.add(meta.getColumnName(i));
	}

	public List<Object> next(){
		try {
			boolean hasNext = this.hasNext;
			if (!didNext) {
				hasNext = rs.next();
			}
			if (!hasNext) {
				DbUtils.closeQuietly(rs);
				return null;
			}
			didNext = false;
			row.clear();
			for (int i = 1; i <= columns; i++) {
				row.add(rs.getObject(i));
			}
			return row;
		} catch (SQLException e) {
			DbUtils.closeQuietly(rs);
			throw new Error(e);
		}
	}

	public boolean hasNext() {
		try {
			if (!didNext) {
				hasNext = rs.next();
				didNext = true;
			}
			if (!hasNext) {
				DbUtils.closeQuietly(rs);
			}
			return hasNext;
		} catch (SQLException e) {
			DbUtils.closeQuietly(rs);
			throw new Error(e);
		}
	}

	public List<String> getColumnNames() {
		return columnNames;
	}
}
