package com.slavi.jdbcspy;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

public class SpyCallableStatement<TT extends CallableStatement> extends SpyPreparedStatement<TT> implements CallableStatement {

	public SpyCallableStatement(TT delegate, String sql) {
		super(delegate, sql);
	}

	@Override
	public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
		t.registerOutParameter(parameterIndex, sqlType);
	}

	@Override
	public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
		t.registerOutParameter(parameterIndex, sqlType, scale);
	}

	@Override
	public boolean wasNull() throws SQLException {
		return t.wasNull();
	}

	@Override
	public String getString(int parameterIndex) throws SQLException {
		return t.getString(parameterIndex);
	}

	@Override
	public boolean getBoolean(int parameterIndex) throws SQLException {
		return t.getBoolean(parameterIndex);
	}

	@Override
	public byte getByte(int parameterIndex) throws SQLException {
		return t.getByte(parameterIndex);
	}

	@Override
	public short getShort(int parameterIndex) throws SQLException {
		return t.getShort(parameterIndex);
	}

	@Override
	public int getInt(int parameterIndex) throws SQLException {
		return t.getInt(parameterIndex);
	}

	@Override
	public long getLong(int parameterIndex) throws SQLException {
		return t.getLong(parameterIndex);
	}

	@Override
	public float getFloat(int parameterIndex) throws SQLException {
		return t.getFloat(parameterIndex);
	}

	@Override
	public double getDouble(int parameterIndex) throws SQLException {
		return t.getDouble(parameterIndex);
	}

	@Override
	public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
		return t.getBigDecimal(parameterIndex, scale);
	}

	@Override
	public byte[] getBytes(int parameterIndex) throws SQLException {
		return t.getBytes(parameterIndex);
	}

	@Override
	public Date getDate(int parameterIndex) throws SQLException {
		return t.getDate(parameterIndex);
	}

	@Override
	public Time getTime(int parameterIndex) throws SQLException {
		return t.getTime(parameterIndex);
	}

	@Override
	public Timestamp getTimestamp(int parameterIndex) throws SQLException {
		return t.getTimestamp(parameterIndex);
	}

	@Override
	public Object getObject(int parameterIndex) throws SQLException {
		return t.getObject(parameterIndex);
	}

	@Override
	public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
		return t.getBigDecimal(parameterIndex);
	}

	@Override
	public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
		return t.getObject(parameterIndex, map);
	}

	@Override
	public Ref getRef(int parameterIndex) throws SQLException {
		return t.getRef(parameterIndex);
	}

	@Override
	public Blob getBlob(int parameterIndex) throws SQLException {
		return t.getBlob(parameterIndex);
	}

	@Override
	public Clob getClob(int parameterIndex) throws SQLException {
		return t.getClob(parameterIndex);
	}

	@Override
	public Array getArray(int parameterIndex) throws SQLException {
		return t.getArray(parameterIndex);
	}

	@Override
	public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
		return t.getDate(parameterIndex, cal);
	}

	@Override
	public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
		return t.getTime(parameterIndex, cal);
	}

	@Override
	public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
		return t.getTimestamp(parameterIndex, cal);
	}

	@Override
	public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException {
		t.registerOutParameter(parameterIndex, sqlType, typeName);
	}

	@Override
	public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
		t.registerOutParameter(parameterName, sqlType);
	}

	@Override
	public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
		t.registerOutParameter(parameterName, sqlType, scale);
		params.put(parameterName, "(out:" + sqlTypes.get(sqlType) + ")");
	}

	@Override
	public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
		t.registerOutParameter(parameterName, sqlType);
		params.put(parameterName, "(out:" + sqlTypes.get(sqlType) + ")");
	}

	@Override
	public URL getURL(int parameterIndex) throws SQLException {
		return t.getURL(parameterIndex);
	}

	@Override
	public void setURL(String parameterName, URL val) throws SQLException {
		t.setURL(parameterName, val);
		params.put(parameterName, val);
	}

	@Override
	public void setNull(String parameterName, int sqlType) throws SQLException {
		t.setNull(parameterName, sqlType);
		params.put(parameterName, null);
	}

	@Override
	public void setBoolean(String parameterName, boolean x) throws SQLException {
		t.setBoolean(parameterName, x);
		params.put(parameterName, x);
	}

	@Override
	public void setByte(String parameterName, byte x) throws SQLException {
		t.setByte(parameterName, x);
		params.put(parameterName, x);
	}

	@Override
	public void setShort(String parameterName, short x) throws SQLException {
		t.setShort(parameterName, x);
		params.put(parameterName, x);
	}

	@Override
	public void setInt(String parameterName, int x) throws SQLException {
		t.setInt(parameterName, x);
		params.put(parameterName, x);
	}

	@Override
	public void setLong(String parameterName, long x) throws SQLException {
		t.setLong(parameterName, x);
		params.put(parameterName, x);
	}

	@Override
	public void setFloat(String parameterName, float x) throws SQLException {
		t.setFloat(parameterName, x);
		params.put(parameterName, x);
	}

	@Override
	public void setDouble(String parameterName, double x) throws SQLException {
		t.setDouble(parameterName, x);
		params.put(parameterName, x);
	}

	@Override
	public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
		t.setBigDecimal(parameterName, x);
		params.put(parameterName, x);
	}

	@Override
	public void setString(String parameterName, String x) throws SQLException {
		t.setString(parameterName, x);
		params.put(parameterName, x);
	}

	@Override
	public void setBytes(String parameterName, byte[] x) throws SQLException {
		t.setBytes(parameterName, x);
		params.put(parameterName, x);
	}

	@Override
	public void setDate(String parameterName, Date x) throws SQLException {
		t.setDate(parameterName, x);
		params.put(parameterName, x);
	}

	@Override
	public void setTime(String parameterName, Time x) throws SQLException {
		t.setTime(parameterName, x);
		params.put(parameterName, x);
	}

	@Override
	public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
		t.setTimestamp(parameterName, x);
		params.put(parameterName, x);
	}

	@Override
	public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
		t.setAsciiStream(parameterName, x, length);
		params.put(parameterName, "AsciiStream:" + length);
	}

	@Override
	public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
		t.setBinaryStream(parameterName, x, length);
		params.put(parameterName, "BinaryStream:" + length);
	}

	@Override
	public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
		t.setObject(parameterName, x, targetSqlType, scale);
		params.put(parameterName, x);
	}

	@Override
	public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
		t.setObject(parameterName, x, targetSqlType);
		params.put(parameterName, x);
	}

	@Override
	public void setObject(String parameterName, Object x) throws SQLException {
		t.setObject(parameterName, x);
		params.put(parameterName, x);
	}

	@Override
	public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
		t.setCharacterStream(parameterName, reader, length);
		params.put(parameterName, "CharacterStream:" + length);
	}

	@Override
	public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
		t.setDate(parameterName, x, cal);
		params.put(parameterName, x);
	}

	@Override
	public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
		t.setTime(parameterName, x, cal);
		params.put(parameterName, x);
	}

	@Override
	public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
		t.setTimestamp(parameterName, x, cal);
		params.put(parameterName, x);
	}

	@Override
	public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
		t.setNull(parameterName, sqlType, typeName);
		params.put(parameterName, null);
	}

	@Override
	public String getString(String parameterName) throws SQLException {
		return t.getString(parameterName);
	}

	@Override
	public boolean getBoolean(String parameterName) throws SQLException {
		return t.getBoolean(parameterName);
	}

	@Override
	public byte getByte(String parameterName) throws SQLException {
		return t.getByte(parameterName);
	}

	@Override
	public short getShort(String parameterName) throws SQLException {
		return t.getShort(parameterName);
	}

	@Override
	public int getInt(String parameterName) throws SQLException {
		return t.getInt(parameterName);
	}

	@Override
	public long getLong(String parameterName) throws SQLException {
		return t.getLong(parameterName);
	}

	@Override
	public float getFloat(String parameterName) throws SQLException {
		return t.getFloat(parameterName);
	}

	@Override
	public double getDouble(String parameterName) throws SQLException {
		return t.getDouble(parameterName);
	}

	@Override
	public byte[] getBytes(String parameterName) throws SQLException {
		return t.getBytes(parameterName);
	}

	@Override
	public Date getDate(String parameterName) throws SQLException {
		return t.getDate(parameterName);
	}

	@Override
	public Time getTime(String parameterName) throws SQLException {
		return t.getTime(parameterName);
	}

	@Override
	public Timestamp getTimestamp(String parameterName) throws SQLException {
		return t.getTimestamp(parameterName);
	}

	@Override
	public Object getObject(String parameterName) throws SQLException {
		return t.getObject(parameterName);
	}

	@Override
	public BigDecimal getBigDecimal(String parameterName) throws SQLException {
		return t.getBigDecimal(parameterName);
	}

	@Override
	public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException {
		return t.getObject(parameterName, map);
	}

	@Override
	public Ref getRef(String parameterName) throws SQLException {
		return t.getRef(parameterName);
	}

	@Override
	public Blob getBlob(String parameterName) throws SQLException {
		return t.getBlob(parameterName);
	}

	@Override
	public Clob getClob(String parameterName) throws SQLException {
		return t.getClob(parameterName);
	}

	@Override
	public Array getArray(String parameterName) throws SQLException {
		return t.getArray(parameterName);
	}

	@Override
	public Date getDate(String parameterName, Calendar cal) throws SQLException {
		return t.getDate(parameterName, cal);
	}

	@Override
	public Time getTime(String parameterName, Calendar cal) throws SQLException {
		return t.getTime(parameterName, cal);
	}

	@Override
	public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
		return t.getTimestamp(parameterName, cal);
	}

	@Override
	public URL getURL(String parameterName) throws SQLException {
		return t.getURL(parameterName);
	}

	@Override
	public RowId getRowId(int parameterIndex) throws SQLException {
		return t.getRowId(parameterIndex);
	}

	@Override
	public RowId getRowId(String parameterName) throws SQLException {
		return t.getRowId(parameterName);
	}

	@Override
	public void setRowId(String parameterName, RowId x) throws SQLException {
		t.setRowId(parameterName, x);
		params.put(parameterName, "RowId");
	}

	@Override
	public void setNString(String parameterName, String value) throws SQLException {
		t.setNString(parameterName, value);
		params.put(parameterName, value);
	}

	@Override
	public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
		t.setNCharacterStream(parameterName, value, length);
		params.put(parameterName, "NCharacterStream:" + length);
	}

	@Override
	public void setNClob(String parameterName, NClob value) throws SQLException {
		t.setNClob(parameterName, value);
		params.put(parameterName, "NClob");
	}

	@Override
	public void setClob(String parameterName, Reader reader, long length) throws SQLException {
		t.setClob(parameterName, reader, length);
		params.put(parameterName, "Clob");
	}

	@Override
	public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
		t.setBlob(parameterName, inputStream, length);
		params.put(parameterName, "Blob");
	}

	@Override
	public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
		t.setNClob(parameterName, reader, length);
		params.put(parameterName, "NClob");
	}

	@Override
	public NClob getNClob(int parameterIndex) throws SQLException {
		return t.getNClob(parameterIndex);
	}

	@Override
	public NClob getNClob(String parameterName) throws SQLException {
		return t.getNClob(parameterName);
	}

	@Override
	public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
		t.setSQLXML(parameterName, xmlObject);
		params.put(parameterName, "XML");
	}

	@Override
	public SQLXML getSQLXML(int parameterIndex) throws SQLException {
		return t.getSQLXML(parameterIndex);
	}

	@Override
	public SQLXML getSQLXML(String parameterName) throws SQLException {
		return t.getSQLXML(parameterName);
	}

	@Override
	public String getNString(int parameterIndex) throws SQLException {
		return t.getNString(parameterIndex);
	}

	@Override
	public String getNString(String parameterName) throws SQLException {
		return t.getNString(parameterName);
	}

	@Override
	public Reader getNCharacterStream(int parameterIndex) throws SQLException {
		return t.getNCharacterStream(parameterIndex);
	}

	@Override
	public Reader getNCharacterStream(String parameterName) throws SQLException {
		return t.getNCharacterStream(parameterName);
	}

	@Override
	public Reader getCharacterStream(int parameterIndex) throws SQLException {
		return t.getCharacterStream(parameterIndex);
	}

	@Override
	public Reader getCharacterStream(String parameterName) throws SQLException {
		return t.getCharacterStream(parameterName);
	}

	@Override
	public void setBlob(String parameterName, Blob x) throws SQLException {
		t.setBlob(parameterName, x);
		params.put(parameterName, "Blob");
	}

	@Override
	public void setClob(String parameterName, Clob x) throws SQLException {
		t.setClob(parameterName, x);
		params.put(parameterName, "Clob");
	}

	@Override
	public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
		t.setAsciiStream(parameterName, x, length);
		params.put(parameterName, "AsciiStream:" + length);
	}

	@Override
	public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
		t.setBinaryStream(parameterName, x, length);
		params.put(parameterName, "BinaryStream:" + length);
	}

	@Override
	public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
		t.setCharacterStream(parameterName, reader, length);
		params.put(parameterName, "CharacterStream:" + length);
	}

	@Override
	public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
		t.setAsciiStream(parameterName, x);
		params.put(parameterName, "AsciiStream");
	}

	@Override
	public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
		t.setBinaryStream(parameterName, x);
		params.put(parameterName, "BinaryStream");
	}

	@Override
	public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
		t.setCharacterStream(parameterName, reader);
		params.put(parameterName, "CharacterStream");
	}

	@Override
	public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
		t.setNCharacterStream(parameterName, value);
		params.put(parameterName, "NCharacterStream");
	}

	@Override
	public void setClob(String parameterName, Reader reader) throws SQLException {
		t.setClob(parameterName, reader);
		params.put(parameterName, "Clob");
	}

	@Override
	public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
		t.setBlob(parameterName, inputStream);
		params.put(parameterName, "Blob");
	}

	@Override
	public void setNClob(String parameterName, Reader reader) throws SQLException {
		t.setNClob(parameterName, reader);
		params.put(parameterName, "NClob");
	}

	@Override
	public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
		return t.getObject(parameterIndex, type);
	}

	@Override
	public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
		return t.getObject(parameterName, type);
	}
}
