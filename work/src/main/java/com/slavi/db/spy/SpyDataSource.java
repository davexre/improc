package com.slavi.db.spy;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

public class SpyDataSource<TT extends DataSource> extends SpyCommonDataSource<TT> implements DataSource {

	public SpyDataSource(TT delegate) {
		super(delegate);
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return t.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return t.isWrapperFor(iface);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return new SpyConnection(t.getConnection());
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return new SpyConnection(t.getConnection(username, password));
	}
}
