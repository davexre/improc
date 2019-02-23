package com.slavi.jdbcspy;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SpyPreparedStatement<TT extends PreparedStatement> extends SpyStatement<TT> implements PreparedStatement {

	String sql;
	Map params = new HashMap();

	public SpyPreparedStatement(TT delegate, String sql) {
		super(delegate);
		this.sql = sql;
	}

	@Override
	public ResultSet executeQuery() throws SQLException {
		try (SpyTimer tt = new SpyTimer(log, sql, params)) {
			return t.executeQuery();
		}
	}

	@Override
	public int executeUpdate() throws SQLException {
		try (SpyTimer tt = new SpyTimer(log, sql, params)) {
			return t.executeUpdate();
		}
	}

	@Override
	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		t.setNull(parameterIndex, sqlType);
		params.put(parameterIndex, null);
	}

	@Override
	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		t.setBoolean(parameterIndex, x);
		params.put(parameterIndex, x);
	}

	@Override
	public void setByte(int parameterIndex, byte x) throws SQLException {
		t.setByte(parameterIndex, x);
		params.put(parameterIndex, x);
	}

	@Override
	public void setShort(int parameterIndex, short x) throws SQLException {
		t.setShort(parameterIndex, x);
		params.put(parameterIndex, x);
	}

	@Override
	public void setInt(int parameterIndex, int x) throws SQLException {
		t.setInt(parameterIndex, x);
		params.put(parameterIndex, x);
	}

	@Override
	public void setLong(int parameterIndex, long x) throws SQLException {
		t.setLong(parameterIndex, x);
		params.put(parameterIndex, x);
	}

	@Override
	public void setFloat(int parameterIndex, float x) throws SQLException {
		t.setFloat(parameterIndex, x);
		params.put(parameterIndex, x);
	}

	@Override
	public void setDouble(int parameterIndex, double x) throws SQLException {
		t.setDouble(parameterIndex, x);
		params.put(parameterIndex, x);
	}

	@Override
	public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
		t.setBigDecimal(parameterIndex, x);
		params.put(parameterIndex, x);
	}

	@Override
	public void setString(int parameterIndex, String x) throws SQLException {
		t.setString(parameterIndex, x);
		params.put(parameterIndex, x);
	}

	@Override
	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		t.setBytes(parameterIndex, x);
		params.put(parameterIndex, x);
	}

	@Override
	public void setDate(int parameterIndex, Date x) throws SQLException {
		t.setDate(parameterIndex, x);
		params.put(parameterIndex, x);
	}

	@Override
	public void setTime(int parameterIndex, Time x) throws SQLException {
		t.setTime(parameterIndex, x);
		params.put(parameterIndex, x);
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
		t.setTimestamp(parameterIndex, x);
		params.put(parameterIndex, x);
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
		t.setAsciiStream(parameterIndex, x, length);
		params.put(parameterIndex, "AsciiStream:" + length);
	}

	@Override
	public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
		t.setUnicodeStream(parameterIndex, x, length);
		params.put(parameterIndex, "UnicodeStream:" + length);
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
		t.setBinaryStream(parameterIndex, x, length);
		params.put(parameterIndex, "BinaryStream:" + length);
	}

	@Override
	public void clearParameters() throws SQLException {
		t.clearParameters();
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
		t.setObject(parameterIndex, x, targetSqlType);
		params.put(parameterIndex, x);
	}

	@Override
	public void setObject(int parameterIndex, Object x) throws SQLException {
		t.setObject(parameterIndex, x);
		params.put(parameterIndex, x);
	}

	@Override
	public boolean execute() throws SQLException {
		try (SpyTimer tt = new SpyTimer(log, sql, params)) {
			return t.execute();
		}
	}

	@Override
	public void addBatch() throws SQLException {
		t.addBatch();
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
		t.setCharacterStream(parameterIndex, reader, length);
		params.put(parameterIndex, "CharacterStream:" + length);
	}

	@Override
	public void setRef(int parameterIndex, Ref x) throws SQLException {
		t.setRef(parameterIndex, x);
		params.put(parameterIndex, "Ref");
	}

	@Override
	public void setBlob(int parameterIndex, Blob x) throws SQLException {
		t.setBlob(parameterIndex, x);
		params.put(parameterIndex, "Blob");
	}

	@Override
	public void setClob(int parameterIndex, Clob x) throws SQLException {
		t.setClob(parameterIndex, x);
		params.put(parameterIndex, "Clob");
	}

	@Override
	public void setArray(int parameterIndex, Array x) throws SQLException {
		t.setArray(parameterIndex, x);
		params.put(parameterIndex, x);
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return t.getMetaData();
	}

	@Override
	public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
		t.setDate(parameterIndex, x, cal);
		params.put(parameterIndex, x);
	}

	@Override
	public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
		t.setTime(parameterIndex, x, cal);
		params.put(parameterIndex, x);
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
		t.setTimestamp(parameterIndex, x, cal);
		params.put(parameterIndex, x);
	}

	@Override
	public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
		t.setNull(parameterIndex, sqlType, typeName);
		params.put(parameterIndex, null);
	}

	@Override
	public void setURL(int parameterIndex, URL x) throws SQLException {
		t.setURL(parameterIndex, x);
		params.put(parameterIndex, x);
	}

	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException {
		return t.getParameterMetaData();
	}

	@Override
	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		t.setRowId(parameterIndex, x);
		params.put(parameterIndex, "RowId");
	}

	@Override
	public void setNString(int parameterIndex, String value) throws SQLException {
		t.setNString(parameterIndex, value);
		params.put(parameterIndex, value);
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
		t.setNCharacterStream(parameterIndex, value, length);
		params.put(parameterIndex, "NCharacterStream:" + length);
	}

	@Override
	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		t.setNClob(parameterIndex, value);
		params.put(parameterIndex, "NClob");
	}

	@Override
	public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
		t.setClob(parameterIndex, reader, length);
		params.put(parameterIndex, "Clob:" + length);
	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
		t.setBlob(parameterIndex, inputStream, length);
		params.put(parameterIndex, "BClob:" + length);
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
		t.setNClob(parameterIndex, reader, length);
		params.put(parameterIndex, "NClob:" + length);
	}

	@Override
	public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
		t.setSQLXML(parameterIndex, xmlObject);
		params.put(parameterIndex, "XML");
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
		t.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
		params.put(parameterIndex, x);
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
		t.setAsciiStream(parameterIndex, x, length);
		params.put(parameterIndex, "AsciiStream:" + length);
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
		t.setBinaryStream(parameterIndex, x, length);
		params.put(parameterIndex, "BinaryStream:" + length);
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
		t.setCharacterStream(parameterIndex, reader, length);
		params.put(parameterIndex, "CharacterStream:" + length);
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
		t.setAsciiStream(parameterIndex, x);
		params.put(parameterIndex, "AsciiStream");
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
		t.setBinaryStream(parameterIndex, x);
		params.put(parameterIndex, "BinaryStream");
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
		t.setCharacterStream(parameterIndex, reader);
		params.put(parameterIndex, "CharacterStream");
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
		t.setNCharacterStream(parameterIndex, value);
		params.put(parameterIndex, "NCharacterStream");
	}

	@Override
	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		t.setClob(parameterIndex, reader);
		params.put(parameterIndex, "Clob");
	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
		t.setBlob(parameterIndex, inputStream);
		params.put(parameterIndex, "Blob");
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		t.setNCharacterStream(parameterIndex, reader);
		params.put(parameterIndex, "NClob");
	}
}
