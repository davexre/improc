package com.slavi.jdbcspy;

import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.sql.XADataSource;

public class SpyXADataSource<TT extends XADataSource> extends SpyCommonDataSource<TT> implements XADataSource {

	public SpyXADataSource(TT delegate) {
		super(delegate);
	}

	@Override
	public XAConnection getXAConnection() throws SQLException {
		return new SpyXAConnection(t.getXAConnection());
	}

	@Override
	public XAConnection getXAConnection(String user, String password) throws SQLException {
		return new SpyXAConnection(t.getXAConnection(user, password));
	}
}
