package com.slavi.db.oracle;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import oracle.jdbc.pool.OracleDataSource;


public class ConnectToOracle {

	Properties prop;
	String connectStr;
	String username;
	String password;
	
	public ConnectToOracle() throws IOException {
		prop = new Properties();
		prop.load(new InputStreamReader(getClass().getResourceAsStream(getClass().getSimpleName() + ".properties")));
		connectStr = prop.getProperty("connectStr");
		username = prop.getProperty("username");
		password = prop.getProperty("password");
	}

	public void doIt(String[] args) throws Exception {
		OracleDataSource ods = new OracleDataSource();
		ods.setURL(connectStr);
		ods.setUser(username);
		ods.setPassword(password);
		QueryRunner qr = new QueryRunner(ods);
		Object o = qr.query("select sysdate from dual", new ScalarHandler());
		System.out.println(o);
		ods.close();
	}

	public void doIt1(String[] args) throws Exception {
		DriverManager.registerDriver (new oracle.jdbc.OracleDriver());
		try (Connection conn = DriverManager.getConnection(connectStr, username, password)) {
			QueryRunner qr = new QueryRunner();
			Object o = qr.query(conn, "select sysdate from dual", new ScalarHandler());
			System.out.println(o);
		}
	}

	public static void main(String[] args) throws Exception {
		new ConnectToOracle().doIt(args);
		System.out.println("Done.");
	}
}
