/**
 * <pre>
 * Enable logging and timing of jdbc.
 *
 * Usage:
 * 1) Connect string
 *      String dbConnectString = "jdbc:sqlite::memory:";
 *      String spyDbConnectString = "jdbc:spy:" + dbConnectString;
 * 2) ConnectionPoolDataSource
 *      ConnectionPoolDataSource spyDS = new SpyConnectionPoolDataSource(new SQLiteConnectionPoolDataSource());
 * 3) DataSource
 *      DataSource spyDS = new SpyDataSource(new SQLiteDataSource());
 * 4) Connection
 *      Connection spyConn = new SpyConnection(DriverManager.getConnection("jdbc:sqlite::memory:"));
 * 5) XADataSource
 *      XADataSource spyDS = new SpyXADataSource(new org.h2.jdbcx.JdbcDataSource());
 * 6) XAConnection
 *      XADataSource ds = new org.h2.jdbcx.JdbcDataSource();
 *      XAConnection spyXAConn = new SpyXAConnection(ds.getXAConnection());
 *
 * Logging: Uses SLF4J logger named "jdbc.spy". SQL statements are logged with INFO level. SQL parameters are logged with DEBUG.
 * </pre>
*/
package com.slavi.jdbcspy;
