package com.slavi.jdbcspy;

import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

public class SpyXAConnection<TT extends XAConnection> extends SpyPooledConnection<TT> implements XAConnection {

	public SpyXAConnection(TT delegate) {
		super(delegate);
	}

	@Override
	public XAResource getXAResource() throws SQLException {
		return new SpyXAResource(t.getXAResource());
	}
}
