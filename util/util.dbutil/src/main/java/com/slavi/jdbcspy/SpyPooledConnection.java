package com.slavi.jdbcspy;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;

public class SpyPooledConnection<TT extends PooledConnection> extends Spy<TT> implements PooledConnection {

	public SpyPooledConnection(TT delegate) {
		super(delegate);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return new SpyConnection(t.getConnection());
	}

	@Override
	public void close() throws SQLException {
		t.close();
	}

	@Override
	public void addConnectionEventListener(ConnectionEventListener listener) {
		t.addConnectionEventListener(listener);
	}

	@Override
	public void removeConnectionEventListener(ConnectionEventListener listener) {
		t.removeConnectionEventListener(listener);
	}

	@Override
	public void addStatementEventListener(StatementEventListener listener) {
		t.addStatementEventListener(listener);
	}

	@Override
	public void removeStatementEventListener(StatementEventListener listener) {
		t.removeStatementEventListener(listener);
	}
}
