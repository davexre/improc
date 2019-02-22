package com.slavi.db.spy;

import java.sql.SQLException;
import java.sql.Wrapper;

public class SpyWrapper<TT extends Wrapper> extends Spy<TT> implements Wrapper {
	public SpyWrapper(TT delegate) {
		super(delegate);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return t.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return t.isWrapperFor(iface);
	}
}
