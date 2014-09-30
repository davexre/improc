package com.slavi.derbi;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbutils.ResultSetHandler;

public class ResultSetToProperties implements ResultSetHandler<List<Properties>> {

	public List<Properties> handle(ResultSet rs) throws SQLException {
		List<Properties> r = new ArrayList<Properties>();
		ResultSetMetaData meta = rs.getMetaData();
		int cols = meta.getColumnCount();
		while (rs.next()) {
			Properties item = new Properties();
			for (int i = 1; i <= cols; i++) {
				String key = meta.getColumnName(i).toLowerCase();
				Object value = rs.getObject(i);
				if (value == null) {
					item.remove(key);
				} else {
					item.setProperty(key, value.toString());
				}
			}
			r.add(item);
		}
		return r;
	}
}
