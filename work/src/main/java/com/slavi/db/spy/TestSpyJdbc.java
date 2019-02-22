package com.slavi.db.spy;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ServiceLoader;

public class TestSpyJdbc {

	String connectStr = "jdbc:spy:jdbc:oracle:thin:@//sofracpci.sofia.ifao.net:1677/devcytr_srv.sofia.ifao.net";
	String username = "SPETROV";
	String password = "spetrov";

	public void doIt2(String[] args) throws Exception {
		try (Connection conn = DriverManager.getConnection(connectStr, username, password)) {
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
