package com.slavi.db.speed;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.dbutils.QueryRunner;

public class TestSQLiteSpeed extends TestDbSpeedBase {
	
	private static class CloseShieldConnection extends DelegatingConnection {
		public CloseShieldConnection(Connection c) {
			super(c);
		}

		public void close() throws SQLException {
			passivate();
		}
	}
	
	
	Connection conn = null;
	
	TestSQLiteSpeed() throws SQLException {
		//conn = DriverManager.getConnection("jdbc:sqlite::memory:");
		conn = DriverManager.getConnection("jdbc:sqlite:target/MyDbTest.sqlite");
	}
	
	Connection getConn() throws SQLException {
		return new CloseShieldConnection(conn);
		//return DriverManager.getConnection("jdbc:sqlite::memory:");
	}

	void createTables(Connection conn) throws SQLException {
		QueryRunner qr = new QueryRunner();
		try {
			qr.update(conn, "drop table t");
		} catch (SQLException e) {
		}
		qr.update(conn, "create table t (id integer primary key, data varchar(2000))");
	}
	
	void doTest() throws Exception {
		createData(1000_000);
		readAllData(1);
	}
	
	public static void main(String[] args) throws Exception {
		new TestSQLiteSpeed().doIt();
		System.out.println("Done.");
	}
}
