package com.slavi.db.spy;

import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

public class SpyConnectionPoolDataSource<TT extends ConnectionPoolDataSource> extends SpyCommonDataSource<TT> implements ConnectionPoolDataSource {

	public SpyConnectionPoolDataSource(TT delegate) {
		super(delegate);
	}

	@Override
	public PooledConnection getPooledConnection() throws SQLException {
		return new SpyPooledConnection(t.getPooledConnection());
	}

	@Override
	public PooledConnection getPooledConnection(String user, String password) throws SQLException {
		return new SpyPooledConnection(t.getPooledConnection(user, password));
	}
}
