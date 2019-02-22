package com.slavi.db.spy;

import org.slf4j.Logger;

public class SpyTimer implements AutoCloseable {

	String msg;
	long start;
	Logger log;

	public SpyTimer(Logger log, String msg) {
		this.log = log;
		this.msg = msg;
		start = System.currentTimeMillis();
	}

	@Override
	public void close() {
		long end = System.currentTimeMillis();
		log.debug("Spent {} ms to run: {}", (end - start), msg);
	}
}
