package com.slavi.db.speed;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;

import com.slavi.util.Marker;

public class TestHsqldbSpeed extends TestDbSpeedBase {
	Connection getConn() throws SQLException {
		return DriverManager.getConnection("jdbc:hsqldb:file:target/hsqldbTest");
		//return DriverManager.getConnection("jdbc:hsqldb:mem");
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
		try (Connection conn = getConn()) {
			QueryRunner qr = new QueryRunner();
			Marker.mark("Compact");
			qr.update(conn, "SHUTDOWN COMPACT");
			Marker.release();
		}
	}

	public static void main(String[] args) throws Exception {
		new TestHsqldbSpeed().doIt();
		System.out.println("Done.");
	}
}
