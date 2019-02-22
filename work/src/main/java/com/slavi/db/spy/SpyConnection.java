package com.slavi.db.spy;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class SpyConnection<TT extends Connection> extends SpyWrapper<TT> implements Connection {

	public SpyConnection(TT delegate) {
		super(delegate);
	}

	@Override
	public Statement createStatement() throws SQLException {
		return new SpyStatement(t.createStatement());
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return new SpyPreparedStatement(t.prepareStatement(sql), sql);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		return new SpyCallableStatement(t.prepareCall(sql), sql);
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		try (SpyTimer tt = new SpyTimer(logSql, sql)) {
			return t.nativeSQL(sql);
		}
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		t.setAutoCommit(autoCommit);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return t.getAutoCommit();
	}

	@Override
	public void commit() throws SQLException {
		t.commit();
	}

	@Override
	public void rollback() throws SQLException {
		t.rollback();
	}

	@Override
	public void close() throws SQLException {
		t.close();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return t.isClosed();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return t.getMetaData();
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		t.setReadOnly(readOnly);
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return t.isReadOnly();
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		t.setCatalog(catalog);
	}

	@Override
	public String getCatalog() throws SQLException {
		return t.getCatalog();
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		t.setTransactionIsolation(level);
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return t.getTransactionIsolation();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return t.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		t.clearWarnings();
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return new SpyStatement(t.createStatement(resultSetType, resultSetConcurrency));
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return new SpyPreparedStatement(t.prepareStatement(sql, resultSetType, resultSetConcurrency), sql);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return new SpyCallableStatement(t.prepareCall(sql, resultSetType, resultSetConcurrency), sql);
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return t.getTypeMap();
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		t.setTypeMap(map);
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		t.setHoldability(holdability);
	}

	@Override
	public int getHoldability() throws SQLException {
		return t.getHoldability();
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return t.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return t.setSavepoint(name);
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		t.rollback(savepoint);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		t.releaseSavepoint(savepoint);
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return new SpyStatement(t.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return new SpyPreparedStatement(t.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability), sql);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return new SpyCallableStatement(t.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability), sql);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return new SpyPreparedStatement(t.prepareStatement(sql, autoGeneratedKeys), sql);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		return new SpyPreparedStatement(t.prepareStatement(sql, columnIndexes), sql);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		return new SpyPreparedStatement(t.prepareStatement(sql, columnNames), sql);
	}

	@Override
	public Clob createClob() throws SQLException {
		return t.createClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		return t.createBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return t.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return t.createSQLXML();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return t.isValid(timeout);
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		t.setClientInfo(name, value);
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		t.setClientInfo(properties);
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return t.getClientInfo(name);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return t.getClientInfo();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return t.createArrayOf(typeName, elements);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return t.createStruct(typeName, attributes);
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		t.setSchema(schema);
	}

	@Override
	public String getSchema() throws SQLException {
		return t.getSchema();
	}

	@Override
	public void abort(Executor executor) throws SQLException {
		t.abort(executor);
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		t.setNetworkTimeout(executor, milliseconds);
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return t.getNetworkTimeout();
	}
}
