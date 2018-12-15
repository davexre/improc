package com.slavi.reporting.velocity;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.dbutils.DbUtils;

public class ResultSetToIteratorMap implements Iterator<HashMap<String, Object>> {
	ResultSet rs;
	ResultSetMetaData meta;
	HashMap<String, Object> row = new HashMap<>();
	boolean didNext = false;
	boolean hasNext = false;


	public ResultSetToIteratorMap(ResultSet rs) throws SQLException {
		this.rs = rs;
		meta = rs.getMetaData();
	}

	public HashMap<String, Object> next(){
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
			for (int i = 1; i <= meta.getColumnCount(); i++) {
				row.put(meta.getColumnName(i), rs.getObject(i));
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
}
