package com.slavi.derbi;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

import com.slavi.dbutil.MyDbScriptRunner;
import com.slavi.dbutil.ResultSetToString;

public class TestDerbyQueryWithArrays {

	Connection conn;

	void doIt() throws Exception {
		//conn = DriverManager.getConnection("jdbc:derby:memory:MyDbTest;create=true");
		//conn = DriverManager.getConnection("jdbc:sqlite::memory:");
		//conn = DriverManager.getConnection("jdbc:hsqldb:file:hsqlDbTest");
		conn = DriverManager.getConnection("jdbc:hsqldb:mem");

		conn.setAutoCommit(false);
		MyDbScriptRunner sr = new MyDbScriptRunner(conn);
		sr.process(getClass().getResourceAsStream("Derby_sql.txt"));
		//sr.process(getClass().getResourceAsStream("TestDerbyQueryWithArrays.sqlite.txt"));


		PreparedStatement ps = conn.prepareStatement("select * from emp where emp.id in (unnest(?))");
		Statement st = conn.createStatement();
		st.execute("delete from emp where name='delme'");

		Array arr = conn.createArrayOf("INTEGER", new Integer[] {1,2});
		ps.setArray(1, arr);
		System.out.println(ResultSetToString.resultSetToString(ps.executeQuery()));

		st.close();
		ps.close();
		conn.commit();
		conn.close();
	}

	public static void main(String[] args) throws Exception {
		new TestDerbyQueryWithArrays().doIt();
		System.out.println("Done.");
	}
}
