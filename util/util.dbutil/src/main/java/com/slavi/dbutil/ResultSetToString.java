package com.slavi.dbutil;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Locale;

public class ResultSetToString {
	public static String resultSetToString(ResultSet rs) throws SQLException {
		return resultSetToString(rs, 0, 10, true, 40);
	}

	public static String resultSetToString(ResultSet rs, int recordsToSkip, int recordsToShow, boolean showHeader,
			int maxColumnWidth) throws SQLException {
		try (ResultSet dummyRsToClose = rs) {
			while (recordsToSkip > 0) {
				if (!rs.next())
					return "";
				recordsToSkip--;
			}
			StringBuilder r = new StringBuilder();
			ResultSetMetaData md = rs.getMetaData();
			int columns = md.getColumnCount();

			int columnWidths[] = new int[columns];
			String columnFormats[] = new String[columns];
			StringBuilder delim = new StringBuilder();
			for (int c = 0; c < columns; c++) {
				String columnName = md.getColumnName(c + 1);
				String columnType = DbUtil.getFieldTypeString(md, c + 1);
				int columnWidth = Math.max(columnName.length(), columnType.length());
				columnWidth = Math.max(columnWidth, DbUtil.getPreferedColumnWidth(md, c + 1));
				columnWidth = Math.min(columnWidth, maxColumnWidth);

				columnWidths[c] = columnWidth;
				columnFormats[c] = "|%" + columnWidth + "." + columnWidth + "s";

				delim.append("+");
				for (int i = 0; i < columnWidth; i++) {
					delim.append("-");
				}
			}
			delim.append("+\n");

			r.append(delim);
			if (showHeader) {
				for (int c = 0; c < columns; c++) {
					r.append(String.format(Locale.US, columnFormats[c], md.getColumnName(c + 1)));
				}
				r.append("|");
				r.append("\n");

				for (int c = 0; c < columns; c++) {
					r.append(String.format(Locale.US, columnFormats[c], DbUtil.getFieldTypeString(md, c + 1)));
				}
				r.append("|");
				r.append("\n");
				r.append(delim);
			}

			while (recordsToShow > 0) {
				if (!rs.next())
					break;
				for (int c = 0; c < columns; c++) {
					r.append(String.format(Locale.US, columnFormats[c], DbUtil.getFieldValueString(rs, md, c + 1, columnWidths[c])));
				}
				r.append("|\n");
				recordsToShow--;
			}
			r.append(delim);
			rs.close();
			return r.toString();
		}
	}
}
