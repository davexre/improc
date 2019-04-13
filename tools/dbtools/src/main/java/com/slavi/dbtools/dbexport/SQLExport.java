package com.slavi.dbtools.dbexport;

import java.io.File;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

class SQLExport implements ExportResultSet {
	public void export(ResultSet rs, String fouName, String tableName) throws Exception {
		try (PrintWriter out = new PrintWriter(new File(fouName))) {
			ResultSetMetaData meta = rs.getMetaData();
			StringBuilder sb = new StringBuilder();
			sb.append("insert into ").append(tableName).append("(");
			int columnCount = meta.getColumnCount();
			String prefix = "";
			for (int i = 1; i < columnCount; i++) {
				sb.append(prefix).append(meta.getColumnName(i));
				prefix = ",";
			}
			sb.append(") values (");
			String sqlPrefix = sb.toString();

			while (rs.next()) {
				sb.setLength(0);
				prefix = "";
				for (int i = 1; i < columnCount; i++) {
					String value = rs.getString(i);
					if (value == null)
						value = "null";
					sb.append(prefix).append("'").append(value.replaceAll("'", "''")).append("'");
					prefix = ",";
				}
				sb.append(");\n");
				out.append(sqlPrefix).append(sb);
			}
		}
	}
}