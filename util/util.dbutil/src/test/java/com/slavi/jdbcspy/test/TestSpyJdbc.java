package com.slavi.jdbcspy.test;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ServiceLoader;

import org.apache.commons.dbutils.QueryRunner;

public class TestSpyJdbc {

	String connectStr = "jdbc:spy:jdbc:oracle:thin:@//sofracpci.sofia.ifao.net:1677/devcytr_srv.sofia.ifao.net";
	String username = "SPETROV";
	String password = "spetrov";

	Connection getConnection() throws SQLException {
		Connection conn = DriverManager.getConnection(connectStr, username, password);
		return conn;
	}

	Connection getConnection2() throws SQLException {
		Connection conn = DriverManager.getConnection("jdbc:spy:jdbc:sqlite::memory:");
		QueryRunner qr = new QueryRunner();
		qr.update(conn, "create table dual (sysdate integer primary key, data varchar(2000))");
		qr.update(conn, "insert into dual (sysdate, data) values (?,?)", 123, "qwe");
		return conn;
	}

	public void doIt2(String[] args) throws Exception {
		try (Connection conn = getConnection2()) {
			PreparedStatement ps = conn.prepareStatement("select sysdate from dual");
			ResultSet rs = ps.executeQuery();
			rs.next();
			System.out.println(rs.getObject(1));
		}
	}

	public void doIt(String[] args) throws Exception {
		ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
		for (Driver i : loadedDrivers) {
			System.out.println(i.getClass());
		}
	}

	public static void main(String[] args) throws Exception {
		new TestSpyJdbc().doIt2(args);
		System.out.println("Done.");
	}
}
