package com.slavi.db.speed;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;

public class TestDerbySpeed extends TestDbSpeedBase {
	Connection getConn() throws SQLException {
		return DriverManager.getConnection("jdbc:derby:target/MyDbTest3;create=true");
		//return DriverManager.getConnection("jdbc:derby:memory:MyDbTest;create=true");
	}
	
	void createTables(Connection conn) throws SQLException {
		QueryRunner qr = new QueryRunner();
		try {
			qr.update(conn, "drop table t");
		} catch (SQLException e) {
		}
		qr.update(conn, "create table t (id integer not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), data varchar(2000), primary key (id))");
		//qr.update(conn, "create table t (id integer not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), data varchar(2000))");
		//qr.update(conn, "create unique index t_unique on t(id)");
	}

	void doTest() throws Exception {
		createData(1000_000);
		readAllData(200);
	}

	public static void main(String[] args) throws Exception {
		new TestDerbySpeed().doIt();
		System.out.println("Done.");
	}
}
