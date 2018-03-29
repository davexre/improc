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

		public abstract String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException;
	}

	static class DBFieldString extends DBFieldFormat {
		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException {
			String r = rs.getString(column);
			return r == null ? "NULL" : toAlphaNumericString(r);
		}
	}

	static class DBFieldDouble extends DBFieldFormat {
		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException {
			return Double.toString(rs.getDouble(column));
		}
	}

	static class DBFieldInt extends DBFieldFormat {
		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException {
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

		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException {
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

		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException {
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

		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException {
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

		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException {
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

		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException {
			try (InputStream is = rs.getBinaryStream(column)) {
				byte buf[] = new byte[maxStringLength];
				if (is != null) {
					int len = is.read(buf);
					return toAlphaNumericString(new String(buf, 0, len));
				} else {
					return "NULL";
				}
			} catch (IOException e) {
				throw new SQLException(e);
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

		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException {
			try (InputStream is = rs.getBinaryStream(column)) {
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
			} catch (IOException e) {
				throw new SQLException(e);
			}
		}
	}

	static class DBFieldObject extends DBFieldFormat {
		public String valueToString(ResultSet rs, int column, int maxStringLength) throws SQLException {
			Object o = rs.getObject(column);
			return o == null ? "NULL" : toAlphaNumericString(o.toString());
		}
	}

	final HashMap<Integer, DBFieldFormat> formatter = new HashMap<Integer, ResultSetToString.DBFieldFormat>();

	private void addType(int typeCode, String typeName, DBFieldFormat instance) {
		instance.typeCode = typeCode;
		instance.typeName = typeName;
		formatter.put(typeCode, instance);
	}
	
	public ResultSetToString() {
		addType(Types.BIT, "BIT", new DBFieldBoolean());
		addType(Types.BOOLEAN, "BOOLEAN", new DBFieldBoolean());
		addType(Types.BIGINT, "BIGINT", new DBFieldInt());

		addType(Types.VARCHAR, "VARCHAR", new DBFieldString());
		addType(Types.CHAR, "CHAR", new DBFieldString());
		addType(Types.LONGNVARCHAR, "LONGNVARCHAR", new DBFieldString());
		addType(Types.LONGVARCHAR, "LONGVARCHAR", new DBFieldString());
		addType(Types.NCHAR, "NCHAR", new DBFieldString());
		addType(Types.NCLOB, "NCLOB", new DBFieldString());
		addType(Types.NVARCHAR, "NVARCHAR", new DBFieldString());

		addType(Types.SQLXML, "SQLXML", new DBFieldBlobAsStr());
		addType(Types.BINARY, "BINARY", new DBFieldBlobAsStr());
		addType(Types.VARBINARY, "VARBINARY", new DBFieldBlobAsStr());
		addType(Types.LONGVARBINARY, "LONGVARBINARY", new DBFieldBlobAsHexStr());

		addType(Types.DATE, "DATE", new DBFieldDate());
		addType(Types.TIME, "TIME", new DBFieldTime());
		addType(Types.TIMESTAMP, "TIMESTAMP", new DBFieldTimestamp());

		addType(Types.DECIMAL, "DECIMAL", new DBFieldInt());
		addType(Types.DOUBLE, "DOUBLE", new DBFieldDouble());
		addType(Types.NUMERIC, "NUMERIC", new DBFieldDouble());
		addType(Types.REAL, "REAL", new DBFieldDouble());
		addType(Types.FLOAT, "FLOAT", new DBFieldDouble());

		addType(Types.INTEGER, "INTEGER", new DBFieldInt());
		addType(Types.SMALLINT, "SMALLINT", new DBFieldInt());
		addType(Types.TINYINT, "TINYINT", new DBFieldInt());

		addType(Types.ROWID, "ROWID", new DBFieldObject());
		addType(Types.REF, "REF", new DBFieldObject());
		addType(Types.ARRAY, "ARRAY", new DBFieldObject());
		addType(Types.STRUCT, "STRUCT", new DBFieldObject());
		addType(Types.OTHER, "OTHER", new DBFieldObject());
		addType(Types.NULL, "NULL", new DBFieldObject());
		addType(Types.JAVA_OBJECT, "JAVA_OBJECT", new DBFieldObject());
		addType(Types.DISTINCT, "DISTINCT", new DBFieldObject());
		addType(Types.DATALINK, "DATALINK", new DBFieldObject());
	}

	String getFieldValueString(ResultSet rs, ResultSetMetaData md, int column, int maxStringLength)
			throws SQLException {
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

	public String resultSetToString(ResultSet rs) throws SQLException {
		return resultSetToString(rs, 0, 10, true, 40);
	}
	
	public String resultSetToString(ResultSet rs, int recordsToSkip, int recordsToShow, boolean showHeader,
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
