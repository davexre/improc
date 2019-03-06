package com.slavi.derbi.dbload;

import java.io.ByteArrayInputStream;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

public class DbDataParser {
	public final static String hexChars = "0123456789ABCDEF";

	public DateFormat dateFormat;

	public HashMap<Integer, DbValueParse> formatter = new HashMap();

	public boolean interpretNullKeyword = true;

	public String preProcess(String value) {
		if (value == null)
			return null;
		value = value.trim();
		if ("".equals(value) ||
			(interpretNullKeyword && "NULL".equalsIgnoreCase(value)))
			return null;
		return value;
	}

	public Boolean parseBoolean(String value) throws Exception {
		if ((value = preProcess(value)) == null)
			return null;
		if (
			"F".equalsIgnoreCase(value) ||
			"FALSE".equalsIgnoreCase(value) ||
			"0".equalsIgnoreCase(value) ||
			"N".equalsIgnoreCase(value) ||
			"NO".equalsIgnoreCase(value) ||
			"DISABLE".equalsIgnoreCase(value) ||
			"DISABLED".equalsIgnoreCase(value)
		) return false;
		if (
			"T".equalsIgnoreCase(value) ||
			"TRUE".equalsIgnoreCase(value) ||
			"1".equalsIgnoreCase(value) ||
			"Y".equalsIgnoreCase(value) ||
			"YES".equalsIgnoreCase(value) ||
			"ENABLE".equalsIgnoreCase(value) ||
			"ENABLED".equalsIgnoreCase(value)
		) return true;
		throw new Exception("Invalid boolean value " + value);
	}

	public Date parseDate(String value) throws ParseException {
		if ((value = preProcess(value)) == null)
			return null;
		return new Date(dateFormat.parse(value).getTime());
	}

	public Time parseTime(String value) throws ParseException {
		if ((value = preProcess(value)) == null)
			return null;
		return new Time(dateFormat.parse(value).getTime());
	}

	public Timestamp parseTimestamp(String value) throws ParseException {
		if ((value = preProcess(value)) == null)
			return null;
		return new Timestamp(dateFormat.parse(value).getTime());
	}

	public Integer parseInt(String value) {
		if ((value = preProcess(value)) == null)
			return null;
		return Integer.parseInt(value);
	}

	public Long parseLong(String value) {
		if ((value = preProcess(value)) == null)
			return null;
		return Long.parseLong(value);
	}

	public Float parseFloat(String value) {
		if ((value = preProcess(value)) == null)
			return null;
		return Float.parseFloat(value);
	}

	public Double parseDouble(String value) {
		if ((value = preProcess(value)) == null)
			return null;
		return Double.parseDouble(value);
	}

	public static byte[] hexToBytes(String hex) throws NumberFormatException {
		if (hex.length() % 2 != 0)
			throw new NumberFormatException("Number of chars in hex string must be multiple of 2 but was " + hex.length());
		byte[] r = new byte[hex.length() / 2];
		for (int i = 0, ii = 0; i < r.length; i++) {
			int a1 = hexChars.indexOf(hex.charAt(ii));
			if (a1 < 0) throw new NumberFormatException("Invalid char " + hex.charAt(ii) + " at position " + ii);
			int a2 = hexChars.indexOf(hex.charAt(++ii));
			if (a2 < 0) throw new NumberFormatException("Invalid char " + hex.charAt(ii) + " at position " + ii);
			r[i] = (byte) (((a1 << 4) + a2) & 0xff);
		}
		return r;
	}

	public ByteArrayInputStream parseHex(String value) {
		if ((value = preProcess(value)) == null)
			return null;
		return new ByteArrayInputStream(hexToBytes(value));
	}

	public DbDataParser() {
		dateFormat = new SimpleDateFormat("dd/MM/yyyy");

		formatter.put(Types.BIT,
		formatter.put(Types.BOOLEAN,
				(v) -> parseBoolean(v)));

		formatter.put(Types.BIGINT,
		formatter.put(Types.INTEGER,
		formatter.put(Types.SMALLINT,
		formatter.put(Types.TINYINT,
				(v) -> parseInt(v)))));

		formatter.put(Types.DECIMAL, (v) -> parseLong(v));
		formatter.put(Types.FLOAT, (v) -> parseFloat(v));

		formatter.put(Types.DOUBLE,
		formatter.put(Types.NUMERIC,
		formatter.put(Types.REAL,
				(v) -> parseDouble(v))));

		formatter.put(Types.VARCHAR,
		formatter.put(Types.NVARCHAR,
		formatter.put(Types.CHAR,
		formatter.put(Types.NCHAR,
		formatter.put(Types.LONGNVARCHAR,
		formatter.put(Types.LONGVARCHAR,
		formatter.put(Types.NCLOB,
		formatter.put(Types.CLOB,
		formatter.put(Types.SQLXML,
		formatter.put(Types.ROWID,
		formatter.put(Types.REF,
		formatter.put(Types.REF_CURSOR,
		formatter.put(Types.ARRAY,
		formatter.put(Types.STRUCT,
		formatter.put(Types.OTHER,
		formatter.put(Types.NULL,
		formatter.put(Types.JAVA_OBJECT,
		formatter.put(Types.DISTINCT,
		formatter.put(Types.DATALINK,
				(v) -> preProcess(v))))))))))))))))))));

		formatter.put(Types.BLOB,
		formatter.put(Types.BINARY,
		formatter.put(Types.VARBINARY,
		formatter.put(Types.LONGVARBINARY,
				(v) -> parseHex(v)))));

		formatter.put(Types.DATE, (v) -> parseDate(v));

		formatter.put(Types.TIME,
		formatter.put(Types.TIME_WITH_TIMEZONE,
				(v) -> parseTime(v)));

		formatter.put(Types.TIMESTAMP,
		formatter.put(Types.TIMESTAMP_WITH_TIMEZONE,
				(v) -> parseTimestamp(v)));
	}
}
