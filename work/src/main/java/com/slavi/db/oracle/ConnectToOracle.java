package com.slavi.db.oracle;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.slavi.dbutil.ResultSetToStringHandler;

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
	
	public static class StringRowProcessor extends BasicRowProcessor {
		public Object[] toArray(ResultSet rs) throws SQLException {
			ResultSetMetaData meta = rs.getMetaData();
			int cols = meta.getColumnCount();
			Object[] result = new Object[cols];

			for (int i = 0; i < cols; i++) {
				result[i] = rs.getString(i + 1);
			}

			return result;
		}

		@Override
		public Map<String, Object> toMap(ResultSet rs) throws SQLException {
			Map<String, Object> result = new HashMap();
			ResultSetMetaData rsmd = rs.getMetaData();
			int cols = rsmd.getColumnCount();

			for (int i = 1; i <= cols; i++) {
				String columnName = rsmd.getColumnLabel(i);
				if (null == columnName || 0 == columnName.length()) {
					columnName = rsmd.getColumnName(i);
				}
				result.put(columnName.toLowerCase(), rs.getString(i));
			}

			return result;
		}
	}

	public void doIt(String[] args) throws Exception {
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
