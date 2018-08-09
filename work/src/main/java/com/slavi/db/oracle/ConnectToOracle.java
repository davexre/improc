package com.slavi.db.oracle;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.slavi.dbutil.ResultSetToStringHandler;
import com.slavi.dbutil.StringRowProcessor;

import oracle.jdbc.pool.OracleDataSource;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

public class ConnectToOracle {

	public Properties prop;
	public String connectStr;
	public String username;
	public String password;

	public ConnectToOracle() throws IOException, SQLException {
		DriverManager.registerDriver (new oracle.jdbc.OracleDriver());
		prop = new Properties();
		prop.load(new InputStreamReader(getClass().getResourceAsStream(getClass().getSimpleName() + ".properties")));
		connectStr = prop.getProperty("connectStr");
		username = prop.getProperty("username");
		password = prop.getProperty("password");
	}

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(connectStr, username, password);
	}

	public void exampleUsingOracleArrays(String[] args) throws Exception {
		try (Connection conn = getConnection()) {
			QueryRunner qr = new QueryRunner();
			ArrayList<String> lst = new ArrayList<>();
			lst.add("NUMBER");
			lst.add("VARCHAR2");

			// select * from ALL_COLL_TYPES where owner = 'SYS' order by coll_type, type_name;
			//ArrayDescriptor ard = ArrayDescriptor.createDescriptor("SYS.ODCIVARCHAR2LIST", conn);
			ArrayDescriptor ard = ArrayDescriptor.createDescriptor("SYS.DBMS_DEBUG_VC2COLL", conn);
			ARRAY ar = new ARRAY(ard, conn, lst.toArray());
			String sql = "select count(*) from user_tab_columns where data_type not in (select * from table(?))";
			Object val = qr.query(conn, sql, new ScalarHandler(), ar);
			System.out.println(val);
		};
	}

	public static class MyXml {
		String xml;

		public String getXml() {
			return xml;
		}

		public void setXml(String xml) {
			this.xml = xml;
		}

		public String toString() {
			return xml;
		}
	}

	public void doIt3(String[] args) throws Exception {
		OracleDataSource ods = new OracleDataSource();
		ods.setURL(connectStr);
		ods.setUser(username);
		ods.setPassword(password);
		QueryRunner qr = new QueryRunner(ods);

		String sql = prop.getProperty("sql.xml");

		//Object o = qr.query(sql, new ScalarHandler());
		//Object o = qr.query(sql, new BeanListHandler(MyXml.class)); //, (new SQLXMLColumnHandler()));
		//System.out.println(o);

		Object o = qr.query(sql, new ArrayHandler(new StringRowProcessor())); // BeanListHandler(MyXml.class)); //, (new SQLXMLColumnHandler()));
		Object oo[] = (Object[]) o;
		System.out.println(Arrays.toString(oo));
/*
		try (Connection conn = ods.getConnection()) {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			rs.next();
			Object o = rs.getObject(1);
			oracle.xdb.XMLType oxml = (oracle.xdb.XMLType) o;
			System.out.println(o);
			System.out.println(oxml.getString());
			rs.close();
			ps.close();
		};*/

		ods.close();
	}

	public void doIt2(String[] args) throws Exception {
		OracleDataSource ods = new OracleDataSource();
		ods.setURL(connectStr);
		ods.setUser(username);
		ods.setPassword(password);
		QueryRunner qr = new QueryRunner(ods);
		Object o = qr.query("select sysdate from dual", new ScalarHandler());
		System.out.println(o);
		ResultSetToStringHandler rss = new ResultSetToStringHandler();
		System.out.println(qr.query("select * from user_tables", rss));

		ods.close();
	}

	public void doIt1(String[] args) throws Exception {
		try (Connection conn = getConnection()) {
			QueryRunner qr = new QueryRunner();
			Object o = qr.query(conn, "select sysdate from dual", new ScalarHandler());
			System.out.println(o);
		}
	}

	public void doIt4(String[] args) throws Exception {
		try (Connection conn = getConnection()) {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("select * from AA_TMP_CHANGE_FIRST_NAME");

			PrintWriter out = new PrintWriter(new File("target/csv.csv"));
			CSVFormat.EXCEL.withHeader(rs).print(out).printRecords(rs);
			out.close();
			rs.close();
			st.close();
		}
	}

	public static void main(String[] args) throws Exception {
		new ConnectToOracle().doIt4(args);
		System.out.println("Done.");
	}
}
