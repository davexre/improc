package com.slavi.jdbcspy;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.logging.Logger;

/**
 * @see com.slavi.jdbcspy
 */
public class SpyDriver implements Driver {
	static Logger log = Logger.getLogger("com.slavi.db.spy");

	public static final String JDBC_URL_PREFIX = "jdbc:spy:";

	static {
		try {
			DriverManager.registerDriver(new SpyDriver());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		if (!url.startsWith(JDBC_URL_PREFIX))
			return null;
		url = url.substring(JDBC_URL_PREFIX.length());
		Connection conn = DriverManager.getConnection(url, info);
		return conn == null ? null : new SpyConnection(conn);
	}

	@Override
	public boolean acceptsURL(String url) throws SQLException {
		return url.startsWith(JDBC_URL_PREFIX);
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		if (!url.startsWith(JDBC_URL_PREFIX))
			return null;
		url = url.substring(JDBC_URL_PREFIX.length());
		// Find delegate Driver
		for (Driver i : ServiceLoader.load(Driver.class)) {
			if (i.acceptsURL(url)) {
				return i.getPropertyInfo(url, info);
			}
		}
		return null;
	}

	@Override
	public int getMajorVersion() {
		return 0;
	}

	@Override
	public int getMinorVersion() {
		return 1;
	}

	@Override
	public boolean jdbcCompliant() {
		return false;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return log;
	}
}
