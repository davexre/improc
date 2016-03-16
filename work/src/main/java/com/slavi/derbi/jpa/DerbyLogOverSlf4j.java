package com.slavi.derbi.jpa;

import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Code borrowed from
 * http://www.burnison.ca/articles/redirecting-derbylog-to-an-slf4j-logger
 * 
 * set the java or Derby property
 * derby.stream.error.method=com.cement.misc.DerbyLogOverSlf4j.getLogger
 */
public class DerbyLogOverSlf4j extends Writer {
	private final Logger logger = LoggerFactory.getLogger("Derby");

	public void write(final char[] cbuf, final int off, final int len) {
		logger.info(new String(cbuf, off, len));
	}

	public void flush() {
	}

	public void close() {
	}

	public static Writer getLogger() {
		return new DerbyLogOverSlf4j();
	}
}
