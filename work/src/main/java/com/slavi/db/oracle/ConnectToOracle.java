package com.slavi.db.oracle;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.sql.DataSource;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.itextpdf.text.log.SysoCounter;
import com.slavi.dbutil.ResultSetToStringHandler;
import com.slavi.dbutil.StringRowProcessor;

import oracle.jdbc.pool.OracleDataSource;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

public class ConnectToOracle {

	public final static String propertiesSuffix = ".5";
	public Properties prop;
	public String connectStr;
	public String username;
	public String password;
	DataSource ds;

	public ConnectToOracle() throws IOException, SQLException {
		//DriverManager.registerDriver (new oracle.jdbc.OracleDriver());
		prop = new Properties();
		prop.load(new InputStreamReader(getClass().getResourceAsStream(getClass().getSimpleName() + ".properties")));
		connectStr = prop.getProperty("connectStr" + propertiesSuffix);
		username = prop.getProperty("username" + propertiesSuffix);
		password = prop.getProperty("password" + propertiesSuffix);

		PoolDataSource ds = PoolDataSourceFactory.getPoolDataSource();
		ds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
		ds.setURL(connectStr);
		ds.setUser(username);
		ds.setPassword(password);
		this.ds = ds;
	}

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(connectStr, username, password);
		//return ds.getConnection();
	}

	public void exampleUsingOracleArrays(String[] args) throws Exception {
		QueryRunner qr = new QueryRunner();
		try (Connection conn = getConnection()) {
			System.out.println(qr.query(conn, "select name_c from tlabel_t where sys_oid = 158951792", new ScalarHandler()));
		}
		try (Connection conn = getConnection()) {
			conn.setAutoCommit(false);
			System.out.println(qr.update(conn, "update tlabel_t set name_c = 'qqq' where sys_oid = 158951792"));
		}
		try (Connection conn = getConnection()) {
			System.out.println(qr.query(conn, "select name_c from tlabel_t where sys_oid = 158951792", new ScalarHandler()));
		}
		try (Connection conn = getConnection()) {

/*			QueryRunner qr = new QueryRunner();
			ArrayList<String> lst = new ArrayList<>();
			lst.add("NUMBER");
			lst.add("VARCHAR2");

			String sql = "select ? asd, count(*) from user_tab_columns";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, 123);
			ResultSet rs = ps.executeQuery();
			rs.next();
			Object val = rs.getObject(1);
			System.out.println(val);
*/
/*
			// select * from ALL_COLL_TYPES where owner = 'SYS' order by coll_type, type_name;
			ArrayDescriptor ard = ArrayDescriptor.createDescriptor("SYS.ODCIVARCHAR2LIST", conn);
			//ArrayDescriptor ard = ArrayDescriptor.createDescriptor("SYS.DBMS_DEBUG_VC2COLL", conn);
			ARRAY ar = new ARRAY(ard, conn, lst.toArray());
			String sql = "select count(*) from user_tab_columns where data_type not in (select * from table(?))";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setObject(1, ar);
			ResultSet rs = ps.executeQuery();
			rs.next();
			Object val = rs.getObject(1);
//			Object val = qr.query(conn, sql, new ScalarHandler(), ar);
			System.out.println(val);*/
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

	public static boolean isOjdbcLoggingEnabled() throws MalformedObjectNameException, InstanceNotFoundException, AttributeNotFoundException, ReflectionException, MBeanException {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ObjectName pattern  = new ObjectName("com.oracle.jdbc:type=diagnosability,*");
		ObjectName diag = ((ObjectName[]) (mbs.queryNames(pattern, null).toArray(new ObjectName[0])))[0];
		return (boolean) mbs.getAttribute(diag, "LoggingEnabled");
	}

	public static void setOjdbcLoggingEnabled(boolean enabled) throws MalformedObjectNameException, InstanceNotFoundException, AttributeNotFoundException, ReflectionException, MBeanException, InvalidAttributeValueException {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ObjectName pattern  = new ObjectName("com.oracle.jdbc:type=diagnosability,*");
		ObjectName diag = ((ObjectName[]) (mbs.queryNames(pattern, null).toArray(new ObjectName[0])))[0];
		mbs.setAttribute(diag, new Attribute("LoggingEnabled", enabled));
	}

	// -Doracle.jdbc.Trace=true
	public static void main(String[] args) throws Exception {
		SLF4JBridgeHandler.install();
/*		System.setProperty("oracle.jdbc.Trace", "true");
		System.setProperty("java.util.logging.config.file", "/OracleLog.properties");
		OracleDriver.registerMBeans();
		oracle.jdbc.driver.OracleLog.setTrace(true);
		//System.out.println(oracle.jdbc.driver.OracleLog.isEnabled());
		//System.out.println(isOjdbcLoggingEnabled());
		OracleDriver od = new oracle.jdbc.OracleDriver();
		Logger pl = od.getParentLogger();
		pl.setLevel(Level.FINEST);
		pl.getLogger("sql").setLevel(Level.FINE);
		pl.getLogger("oracle.sql").setLevel(Level.FINE);
		pl.getParent().setLevel(Level.FINE);
		pl.getParent().getLogger("sql").setLevel(Level.FINE);
		DriverManager.registerDriver (od);*/
		new ConnectToOracle().exampleUsingOracleArrays(args);
		System.out.println("Done.");
	}
}
