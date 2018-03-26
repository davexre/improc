package com.slavi.dbutil;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Locale;

public class ResultSetToString {
	abstract static class DBFieldFormat {
		public int typeCode;
		public String typeName;

		public String getFieldTypeString(ResultSetMetaData md, int column) throws SQLException {
			return typeName + "(" + md.getPrecision(column) + ")";
		}

		public int getPreferedColumnWidth(ResultSetMetaData md, int column) throws SQLException {
			return md.getPrecision(column);
		}

		public abstract String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException,
				IOException;
	}

	static class DBFieldString extends DBFieldFormat {
		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException, IOException {
			String r = rs.getString(column);
			return r == null ? "NULL" : toAlphaNumericString(r);
		}
	}

	static class DBFieldDouble extends DBFieldFormat {
		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException, IOException {
			return Double.toString(rs.getDouble(column));
		}
	}

	static class DBFieldInt extends DBFieldFormat {
		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException, IOException {
			return Long.toString(rs.getLong(column));
		}
	}

	static class DBFieldDate extends DBFieldFormat {
		public String getFieldTypeString(ResultSetMetaData md, int column) throws SQLException {
			return typeName;
		}

		public int getPreferedColumnWidth(ResultSetMetaData md, int column) throws SQLException {
			return 10;
		}

		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException, IOException {
			Date r = rs.getDate(column);
			return r == null ? "NULL" : r.toString();
		}
	}

	static class DBFieldTime extends DBFieldFormat {
		public String getFieldTypeString(ResultSetMetaData md, int column) throws SQLException {
			return typeName;
		}

		public int getPreferedColumnWidth(ResultSetMetaData md, int column) throws SQLException {
			return 10;
		}

		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException, IOException {
			Time r = rs.getTime(column);
			return r == null ? "NULL" : r.toString();
		}
	}

	static class DBFieldTimestamp extends DBFieldFormat {
		public String getFieldTypeString(ResultSetMetaData md, int column) throws SQLException {
			return typeName;
		}

		public int getPreferedColumnWidth(ResultSetMetaData md, int column) throws SQLException {
			return 21;
		}

		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException, IOException {
			Timestamp r = rs.getTimestamp(column);
			return r == null ? "NULL" : r.toString();
		}
	}

	static class DBFieldBoolean extends DBFieldFormat {
		public String getFieldTypeString(ResultSetMetaData md, int column) throws SQLException {
			return typeName;
		}

		public int getPreferedColumnWidth(ResultSetMetaData md, int column) throws SQLException {
			return 1;
		}

		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException, IOException {
			return rs.getBoolean(column) ? "T" : "F";
		}
	}

	static class DBFieldBlobAsStr extends DBFieldFormat {
		public String getFieldTypeString(ResultSetMetaData md, int column) throws SQLException {
			return typeName;
		}

		public int getPreferedColumnWidth(ResultSetMetaData md, int column) throws SQLException {
			return 20;
		}

		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException, IOException {
			InputStream is = rs.getBinaryStream(column);
			try {
				byte buf[] = new byte[maxStringLength];
				if (is != null) {
					int len = is.read(buf);
					return toAlphaNumericString(new String(buf, 0, len));
				} else {
					return "NULL";
				}
			} finally {
				is.close();
			}
		}
	}

	static class DBFieldBlobAsHexStr extends DBFieldFormat {
		public String getFieldTypeString(ResultSetMetaData md, int column) throws SQLException {
			return typeName;
		}

		static final int numOfChars = 8;

		public int getPreferedColumnWidth(ResultSetMetaData md, int column) throws SQLException {
			return numOfChars * 4 + (numOfChars + 3) / 4 - 1;
		}

		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException, IOException {
			InputStream is = rs.getBinaryStream(column);
			try {
				int colLen = maxStringLength / 4;
				if (colLen < 5)
					colLen = 5;
				byte buf[] = new byte[colLen];
				if (is != null) {
					int len = is.read(buf);
					return toHexString(buf, 0, len - 1, colLen);
				} else {
					return "NULL";
				}
			} finally {
				is.close();
			}
		}
	}

	static class DBFieldObject extends DBFieldFormat {
		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException, IOException {
			Object o = rs.getObject(column);
			return o == null ? "NULL" : toAlphaNumericString(o.toString());
		}
	}

	final HashMap<Integer, DBFieldFormat> formatter = new HashMap<Integer, ResultSetToString.DBFieldFormat>();

	private void addType(int typeCode, String typeName, Class<? extends DBFieldFormat> fmt) throws InstantiationException,
			IllegalAccessException {
		DBFieldFormat instance = fmt.newInstance();
		instance.typeCode = typeCode;
		instance.typeName = typeName;
		formatter.put(typeCode, instance);
	}

	public ResultSetToString() throws InstantiationException, IllegalAccessException {
		addType(Types.BIT, "BIT", DBFieldBoolean.class);
		addType(Types.BOOLEAN, "BOOLEAN", DBFieldBoolean.class);
		addType(Types.BIGINT, "BIGINT", DBFieldInt.class);

		addType(Types.VARCHAR, "VARCHAR", DBFieldString.class);
		addType(Types.CHAR, "CHAR", DBFieldString.class);
		addType(Types.LONGNVARCHAR, "LONGNVARCHAR", DBFieldString.class);
		addType(Types.LONGVARCHAR, "LONGVARCHAR", DBFieldString.class);
		addType(Types.NCHAR, "NCHAR", DBFieldString.class);
		addType(Types.NCLOB, "NCLOB", DBFieldString.class);
		addType(Types.NVARCHAR, "NVARCHAR", DBFieldString.class);

		addType(Types.SQLXML, "SQLXML", DBFieldBlobAsStr.class);
		addType(Types.BINARY, "BINARY", DBFieldBlobAsStr.class);
		addType(Types.VARBINARY, "VARBINARY", DBFieldBlobAsStr.class);
		addType(Types.LONGVARBINARY, "LONGVARBINARY", DBFieldBlobAsHexStr.class);

		addType(Types.DATE, "DATE", DBFieldDate.class);
		addType(Types.TIME, "TIME", DBFieldTime.class);
		addType(Types.TIMESTAMP, "TIMESTAMP", DBFieldTimestamp.class);

		addType(Types.DECIMAL, "DECIMAL", DBFieldInt.class);
		addType(Types.DOUBLE, "DOUBLE", DBFieldDouble.class);
		addType(Types.NUMERIC, "NUMERIC", DBFieldDouble.class);
		addType(Types.REAL, "REAL", DBFieldDouble.class);
		addType(Types.FLOAT, "FLOAT", DBFieldDouble.class);

		addType(Types.INTEGER, "INTEGER", DBFieldInt.class);
		addType(Types.SMALLINT, "SMALLINT", DBFieldInt.class);
		addType(Types.TINYINT, "TINYINT", DBFieldInt.class);

		addType(Types.ROWID, "ROWID", DBFieldObject.class);
		addType(Types.REF, "REF", DBFieldObject.class);
		addType(Types.ARRAY, "ARRAY", DBFieldObject.class);
		addType(Types.STRUCT, "STRUCT", DBFieldObject.class);
		addType(Types.OTHER, "OTHER", DBFieldObject.class);
		addType(Types.NULL, "NULL", DBFieldObject.class);
		addType(Types.JAVA_OBJECT, "JAVA_OBJECT", DBFieldObject.class);
		addType(Types.DISTINCT, "DISTINCT", DBFieldObject.class);
		addType(Types.DATALINK, "DATALINK", DBFieldObject.class);
	}

	String getFieldValueString(ResultSet rs, ResultSetMetaData md, int column, int maxStringLength)
			throws SQLException, IOException {
		DBFieldFormat fmt = formatter.get(md.getColumnType(column));
		if (fmt == null)
			fmt = formatter.get(Types.OTHER);
		return fmt.valueToString(rs, column, maxStringLength);
	}

	int getPreferedColumnWidth(ResultSetMetaData md, int column) throws SQLException {
		DBFieldFormat fmt = formatter.get(md.getColumnType(column));
		if (fmt == null)
			fmt = formatter.get(Types.OTHER);
		return fmt.getPreferedColumnWidth(md, column);
	}

	String getFieldTypeString(ResultSetMetaData md, int column) throws SQLException {
		DBFieldFormat fmt = formatter.get(md.getColumnType(column));
		if (fmt == null)
			fmt = formatter.get(Types.OTHER);
		return fmt.getFieldTypeString(md, column);
	}

	public String resultSetToString(ResultSet rs) throws SQLException, IOException {
		return resultSetToString(rs, 0, 10, true, 40);
	}
	
	public String resultSetToString(ResultSet rs, int recordsToSkip, int recordsToShow, boolean showHeader,
			int maxColumnWidth) throws SQLException, IOException {
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
			int preferedWidth = getPreferedColumnWidth(md, c + 1);
			String columnName = md.getColumnName(c + 1);
			String columnType = getFieldTypeString(md, c + 1);
			int columnWidth = Math.max(columnName.length(), columnType.length());
			if (preferedWidth < maxColumnWidth) {
				columnWidth = Math.max(columnWidth, preferedWidth);
			}
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
				r.append(String.format(Locale.US, columnFormats[c], getFieldTypeString(md, c + 1)));
			}
			r.append("|");
			r.append("\n");
			r.append(delim);
		}

		while (recordsToShow > 0) {
			if (!rs.next())
				break;
			for (int c = 0; c < columns; c++) {
				r.append(String
						.format(Locale.US, columnFormats[c], getFieldValueString(rs, md, c + 1, columnWidths[c])));
			}
			r.append("|\n");
			recordsToShow--;
		}
		r.append(delim);
		rs.close();
		return r.toString();
	}

	public static char toAlphaNumericChar(char c) {
		return (c < 32) || (c > 250) ? '.' : c;
		// return Character.isLetterOrDigit(c) ? c : '.';
	}

	public static String toAlphaNumericString(String str) {
		StringBuilder r = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			r.append(toAlphaNumericChar(c));
		}
		return r.toString();
	}

	static final String hexChars = "0123456789ABCDEFGH";

	public static String toHexString(byte[] arr, int beginIndex, int endIndex, int minCharColumnWidth) {
		final int splitAtColumn = 4;

		int r1Len = (splitAtColumn + 1) * ((minCharColumnWidth + splitAtColumn - 1) / splitAtColumn) - 1;
		StringBuilder r = new StringBuilder();

		if (arr == null || arr.length == 0) {
			for (int i = 0; i < r1Len; i++)
				r.append(' ');
			r.append('|');
			return r.toString();
		}

		StringBuilder hex = new StringBuilder();
		int max = endIndex >= arr.length ? arr.length - 1 : endIndex;
		int min = beginIndex < 0 ? 0 : beginIndex;

		int count = 0;
		for (int i = min; i <= max; i++) {
			if (count != 0) {
				if (count % splitAtColumn == 0) {
					r.append(" ");
					hex.append("-");
				} else {
					hex.append(' ');
				}
			}
			count++;
			char c = (char) arr[i];
			r.append(toAlphaNumericChar(c));
			hex.append(hexChars.charAt(arr[i] & 0x0f));
			hex.append(hexChars.charAt((arr[i] >> 4) & 0x0f));
		}
		for (int i = r.length(); i < r1Len; i++)
			r.append(' ');
		r.append('|');
		r.append(hex);
		return r.toString();
	}
}
