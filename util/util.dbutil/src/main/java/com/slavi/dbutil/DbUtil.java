package com.slavi.dbutil;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbUtil {
	static final int MIN_NUMERIC_PRECISION = 6;

	abstract static class DBFieldFormat {
		public int typeCode;
		public String typeName;

		public String getFieldTypeString(ResultSetMetaData md, int column) throws SQLException {
			return typeName;
		}

		public int getPreferedColumnWidth(ResultSetMetaData md, int column) throws SQLException {
			return md.getPrecision(column);
		}

		public abstract String valueToString(ResultSet rs, ResultSetMetaData md, int column, int maxStringLength) throws SQLException;
	}

	static class DBFieldString extends DBFieldFormat {
		public String getFieldTypeString(ResultSetMetaData md, int column) throws SQLException {
			return typeName + "(" + md.getPrecision(column) + ")";
		}

		public String valueToString(ResultSet rs, ResultSetMetaData md, int column, int maxStringLength) throws SQLException {
			String r = rs.getString(column);
			return r == null ? "NULL" : toAlphaNumericString(r);
		}
	}

	static class DBFieldCLOB extends DBFieldFormat {
		public String getFieldTypeString(ResultSetMetaData md, int column) throws SQLException {
			return typeName;
		}

		public String valueToString(ResultSet rs, ResultSetMetaData md, int column, int maxStringLength) throws SQLException {
			String r = rs.getString(column);
			return r == null ? "NULL" : toAlphaNumericString(r);
		}
	}

	static class DBFieldNumeric extends DBFieldFormat {
		public int getPreferedColumnWidth(ResultSetMetaData md, int column) throws SQLException {
			return
				Math.max(MIN_NUMERIC_PRECISION, md.getPrecision(column)) +
				Math.max(0, md.getScale(column)) + 1;
		}

		public String getFieldTypeString(ResultSetMetaData md, int column) throws SQLException {

			return typeName +
				(md.getPrecision(column) < 1 ? "" : (
					"(" + md.getPrecision(column) +
						(md.getScale(column) < 1 ? "" :
							("," + md.getScale(column))
						) + ")"
					)
				);
		}

		public String valueToString(ResultSet rs, ResultSetMetaData md, int column, int maxStringLength) throws SQLException {
			int prec = Math.max(MIN_NUMERIC_PRECISION, md.getPrecision(column));
			int scale = Math.max(0, md.getScale(column));
			if (prec + scale + 1 > maxStringLength) {
				scale = maxStringLength * scale / (prec + scale + 1);
				prec = maxStringLength - scale - 1;
			}
			String format = "%" + Integer.toString(maxStringLength) + "." + Integer.toString(scale) + "f";
			return String.format(Locale.US, format, rs.getDouble(column));
			//return Double.toString(rs.getDouble(column));
		}
	}

	static class DBFieldDouble extends DBFieldFormat {
		public String valueToString(ResultSet rs, ResultSetMetaData md, int column, int maxStringLength) throws SQLException {
			return Double.toString(rs.getDouble(column));
		}
	}

	static class DBFieldInt extends DBFieldFormat {
		public String valueToString(ResultSet rs, ResultSetMetaData md, int column, int maxStringLength) throws SQLException {
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

		public String valueToString(ResultSet rs, ResultSetMetaData md, int column, int maxStringLength) throws SQLException {
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

		public String valueToString(ResultSet rs, ResultSetMetaData md, int column, int maxStringLength) throws SQLException {
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

		public String valueToString(ResultSet rs, ResultSetMetaData md, int column, int maxStringLength) throws SQLException {
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

		public String valueToString(ResultSet rs, ResultSetMetaData md, int column, int maxStringLength) throws SQLException {
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

		public String valueToString(ResultSet rs, ResultSetMetaData md, int column, int maxStringLength) throws SQLException {
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

		public String valueToString(ResultSet rs, ResultSetMetaData md, int column, int maxStringLength) throws SQLException {
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
		public String valueToString(ResultSet rs, ResultSetMetaData md, int column, int maxStringLength) throws SQLException {
			Object o = rs.getObject(column);
			return o == null ? "NULL" : toAlphaNumericString(o.toString());
		}
	}

	public static final HashMap<Integer, DBFieldFormat> formatter = new HashMap<Integer, DBFieldFormat>();

	private static void addType(int typeCode, String typeName, DBFieldFormat instance) {
		instance.typeCode = typeCode;
		instance.typeName = typeName;
		formatter.put(typeCode, instance);
	}

/*
List of all constants defined in java.sql.Type
Types.BIT
Types.TINYINT
Types.SMALLINT
Types.INTEGER
Types.BIGINT
Types.FLOAT
Types.REAL
Types.DOUBLE
Types.NUMERIC
Types.DECIMAL
Types.CHAR
Types.VARCHAR
Types.LONGVARCHAR
Types.DATE
Types.TIME
Types.TIMESTAMP
Types.BINARY
Types.VARBINARY
Types.LONGVARBINARY
Types.NULL
Types.OTHER
Types.JAVA_OBJECT
Types.DISTINCT
Types.STRUCT
Types.ARRAY
Types.BLOB
Types.CLOB
Types.REF
Types.DATALINK
Types.BOOLEAN
Types.ROWID
Types.NCHAR
Types.NVARCHAR
Types.LONGNVARCHAR
Types.NCLOB
Types.SQLXML
Types.REF_CURSOR
Types.TIME_WITH_TIMEZONE
Types.TIMESTAMP_WITH_TIMEZONE
*/

	static {
		addType(Types.BIT, "BIT", new DBFieldBoolean());
		addType(Types.BOOLEAN, "BOOLEAN", new DBFieldBoolean());
		addType(Types.BIGINT, "BIGINT", new DBFieldInt());

		addType(Types.VARCHAR, "VARCHAR", new DBFieldString());
		addType(Types.NVARCHAR, "NVARCHAR", new DBFieldString());
		addType(Types.CHAR, "CHAR", new DBFieldString());
		addType(Types.NCHAR, "NCHAR", new DBFieldString());

		addType(Types.LONGNVARCHAR, "LONG NVARCHAR", new DBFieldCLOB());
		addType(Types.LONGVARCHAR, "LONG VARCHAR", new DBFieldCLOB());
		addType(Types.NCLOB, "NCLOB", new DBFieldCLOB());
		addType(Types.CLOB, "CLOB", new DBFieldCLOB());

		addType(Types.BLOB, "BLOB", new DBFieldBlobAsStr());
		addType(Types.SQLXML, "SQLXML", new DBFieldBlobAsStr());
		addType(Types.BINARY, "BINARY", new DBFieldBlobAsStr());
		addType(Types.VARBINARY, "VARBINARY", new DBFieldBlobAsStr());
		addType(Types.LONGVARBINARY, "LONG VARBINARY", new DBFieldBlobAsHexStr());

		addType(Types.DATE, "DATE", new DBFieldDate());
		addType(Types.TIME, "TIME", new DBFieldTime());
		addType(Types.TIME_WITH_TIMEZONE, "TIME_WITH_TIMEZONE", new DBFieldTimestamp());
		addType(Types.TIMESTAMP, "TIMESTAMP", new DBFieldTimestamp());
		addType(Types.TIMESTAMP_WITH_TIMEZONE, "TIMESTAMP_WITH_TIMEZONE", new DBFieldTimestamp());

		addType(Types.DECIMAL, "DECIMAL", new DBFieldInt());
		addType(Types.DOUBLE, "DOUBLE", new DBFieldDouble());
		addType(Types.NUMERIC, "NUMERIC", new DBFieldNumeric());
		addType(Types.REAL, "REAL", new DBFieldDouble());
		addType(Types.FLOAT, "FLOAT", new DBFieldDouble());

		addType(Types.INTEGER, "INTEGER", new DBFieldInt());
		addType(Types.SMALLINT, "SMALLINT", new DBFieldInt());
		addType(Types.TINYINT, "TINYINT", new DBFieldInt());

		addType(Types.ROWID, "ROWID", new DBFieldObject());
		addType(Types.REF, "REF", new DBFieldObject());
		addType(Types.REF_CURSOR, "REF_CURSOR", new DBFieldObject());
		addType(Types.ARRAY, "ARRAY", new DBFieldObject());
		addType(Types.STRUCT, "STRUCT", new DBFieldObject());
		addType(Types.OTHER, "OTHER", new DBFieldObject());
		addType(Types.NULL, "NULL", new DBFieldObject());
		addType(Types.JAVA_OBJECT, "JAVA_OBJECT", new DBFieldObject());
		addType(Types.DISTINCT, "DISTINCT", new DBFieldObject());
		addType(Types.DATALINK, "DATALINK", new DBFieldObject());
	}

	public static String getFieldValueString(ResultSet rs, ResultSetMetaData md, int column, int maxStringLength)
			throws SQLException {
		DBFieldFormat fmt = formatter.get(md.getColumnType(column));
		if (fmt == null)
			fmt = formatter.get(Types.OTHER);
		return fmt.valueToString(rs, md, column, maxStringLength);
	}

	static int getPreferedColumnWidth(ResultSetMetaData md, int column) throws SQLException {
		DBFieldFormat fmt = formatter.get(md.getColumnType(column));
		if (fmt == null)
			fmt = formatter.get(Types.OTHER);
		return fmt.getPreferedColumnWidth(md, column);
	}

	public static String getFieldTypeString(ResultSetMetaData md, int column) throws SQLException {
		DBFieldFormat fmt = formatter.get(md.getColumnType(column));
		if (fmt == null)
			fmt = formatter.get(Types.OTHER);
		return fmt.getFieldTypeString(md, column);
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

	public static String resultSet2ddl(ResultSetMetaData meta, String destTableName) throws SQLException {
		Set<String> columns = new HashSet<>();
		StringBuilder r = new StringBuilder();
		r.append("create table \"").append(destTableName).append("\" (");
		String prefix = "\n";
		for (int i = 1; i <= meta.getColumnCount(); i++) {
			String column = meta.getColumnName(i);
			int columnCount = 1;
			while (columns.contains(column)) {
				column = meta.getColumnName(i) + columnCount;
				columnCount++;
			}
			columns.add(column);
			r.append(prefix).append("    \"").append(column).append("\" ")
				.append(getFieldTypeString(meta, i));
			prefix = ",\n";
		}
		r.append("\n)");
		return r.toString();
	}

	public static void copyResultSet(ResultSet rs, Connection targetConn, String targetTable, int commitEveryNumRows, boolean useColumnNames) throws SQLException {
		Logger log = LoggerFactory.getLogger(DbUtil.class);
		ResultSetMetaData md = rs.getMetaData();

		int numColumns = md.getColumnCount();
		StringBuilder sb = new StringBuilder();
		sb.append("insert into \"").append(targetTable).append("\"");
		if (useColumnNames) {
			sb.append(" (");
			String prefix = "";
			for (int i = 1; i <= numColumns; i++) {
				sb.append(prefix).append("\"").append(md.getColumnName(i)).append("\"");
				prefix = ",";
			}
			sb.append(")");
		}
		sb.append(" values (");
		String prefix = "";
		for (int i = 1; i <= numColumns; i++) {
			sb.append(prefix).append("?");
			prefix = ",";
		}
		sb.append(")");

		String sql = sb.toString();
		log.debug(sql);
		PreparedStatement ps = targetConn.prepareStatement(sql);
		int numRows = 0;
		while (rs.next()) {
			for (int i = 1; i <= numColumns; i++) {
				ps.setObject(i, rs.getObject(i));
			}
			ps.execute();
			numRows++;
			if (!targetConn.getAutoCommit() && (commitEveryNumRows > 0) && (numRows % commitEveryNumRows == 0)) {
				targetConn.commit();
				log.debug("Inserted {} rows into {}", numRows, targetTable);
			}
		}
		if (!targetConn.getAutoCommit() && (commitEveryNumRows > 0)) {
			targetConn.commit();
			log.info("Total of {} rows inserted into {}", numRows, targetTable);
		}
		rs.close();
	}

	public static void createResultSetTable(ResultSet rs, Connection targetConn, String targetTable) throws SQLException {
		Logger log = LoggerFactory.getLogger(DbUtil.class);

		ResultSetMetaData md = rs.getMetaData();
		try (Statement st = targetConn.createStatement()) {
			String sql = DbUtil.resultSet2ddl(md, targetTable);
			log.debug(sql);
			st.execute(sql);
		}
	}

	public static void createResultSetSnapshot(ResultSet rs, Connection targetConn, String targetTable, int commitEveryNumRows) throws SQLException {
		createResultSetTable(rs, targetConn, targetTable);
		copyResultSet(rs, targetConn, targetTable, commitEveryNumRows, false);
	}
}
