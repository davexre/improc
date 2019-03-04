package com.slavi.dbutil;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

public class ResultSetToObjectMap implements ResultSetHandler<List<Map<String, Object>>> {

	public static final ResultSetToObjectMap instance = new ResultSetToObjectMap();

	public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
		List<Map<String, Object>> r = new ArrayList<Map<String, Object>>();
		ResultSetMetaData meta = rs.getMetaData();
		int cols = meta.getColumnCount();
		while (rs.next()) {
			Map<String, Object> item = new HashMap<String, Object>();
			for (int i = 1; i <= cols; i++) {
				item.put(meta.getColumnName(i).toLowerCase(), rs.getObject(i));
			}
			r.add(item);
		}
		return r;
	}
}
