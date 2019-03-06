package com.slavi.derbi.dbload;

import java.io.ByteArrayInputStream;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.HashMap;

import com.slavi.util.DateFormats;

public class DbDataParserTemplate {
	public final static String hexChars = "0123456789ABCDEF";

	public DateFormat dateFormat;

	public HashMap<Integer, ValueParser> formatter = new HashMap();

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

	public DbDataParserTemplate() {
		this("dd/MM/yyyy");
	}

	public DbDataParserTemplate(String... dateFormats) {
		dateFormat = new DateFormats(dateFormats);
		ValueParser p;

		p = (v) -> parseBoolean(v);
		formatter.put(Types.BIT, p);
		formatter.put(Types.BOOLEAN, p);

		p = (v) -> parseInt(v);
		formatter.put(Types.BIGINT, p);
		formatter.put(Types.INTEGER, p);
		formatter.put(Types.SMALLINT, p);
		formatter.put(Types.TINYINT, p);

		p = (v) -> parseLong(v);
		formatter.put(Types.DECIMAL, p);

		p = (v) -> parseFloat(v);
		formatter.put(Types.FLOAT, p);

		p = (v) -> parseDouble(v);
		formatter.put(Types.DOUBLE, p);
		formatter.put(Types.NUMERIC, p);
		formatter.put(Types.REAL, p);

		p = (v) -> preProcess(v);
		formatter.put(Types.VARCHAR, p);
		formatter.put(Types.NVARCHAR, p);
		formatter.put(Types.CHAR, p);
		formatter.put(Types.NCHAR, p);
		formatter.put(Types.LONGNVARCHAR, p);
		formatter.put(Types.LONGVARCHAR, p);
		formatter.put(Types.NCLOB, p);
		formatter.put(Types.CLOB, p);
		formatter.put(Types.SQLXML, p);
		formatter.put(Types.ROWID, p);
		formatter.put(Types.REF, p);
		formatter.put(Types.REF_CURSOR, p);
		formatter.put(Types.ARRAY, p);
		formatter.put(Types.STRUCT, p);
		formatter.put(Types.OTHER, p);
		formatter.put(Types.NULL, p);
		formatter.put(Types.JAVA_OBJECT, p);
		formatter.put(Types.DISTINCT, p);
		formatter.put(Types.DATALINK, p);

		p = (v) -> parseHex(v);
		formatter.put(Types.BLOB, p);
		formatter.put(Types.BINARY, p);
		formatter.put(Types.VARBINARY, p);
		formatter.put(Types.LONGVARBINARY, p);

		p = (v) -> parseDate(v);
		formatter.put(Types.DATE,  p);

		p = (v) -> parseTime(v);
		formatter.put(Types.TIME, p);
		formatter.put(Types.TIME_WITH_TIMEZONE, p);

		p = (v) -> parseTimestamp(v);
		formatter.put(Types.TIMESTAMP, p);
		formatter.put(Types.TIMESTAMP_WITH_TIMEZONE, p);
	}
}
