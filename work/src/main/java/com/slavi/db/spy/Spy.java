package com.slavi.db.spy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Spy<TT> {

	TT t;

	static Logger logSql = LoggerFactory.getLogger("jdbc.spy.sql");

	public Spy(TT delegate) {
		this.t = delegate;
	}
}
