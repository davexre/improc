package com.slavi.db.spy;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.CommonDataSource;

public class SpyCommonDataSource<TT extends CommonDataSource> extends Spy<TT> implements CommonDataSource {

	public SpyCommonDataSource(TT delegate) {
		super(delegate);
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return t.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		t.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		t.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return t.getLoginTimeout();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return t.getParentLogger();
	}
}
