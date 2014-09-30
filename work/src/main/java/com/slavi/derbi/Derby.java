package com.slavi.derbi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource40;
import org.apache.derby.jdbc.EmbeddedDataSource40;

import com.slavi.util.StringPrintStream;

public class Derby {

	public static void dbToXmlDemo() throws SQLException, IOException {
		EmbeddedConnectionPoolDataSource40 dd = new EmbeddedConnectionPoolDataSource40();
		dd.setDatabaseName("");
		EmbeddedDataSource40 ds = new EmbeddedDataSource40();
		ds.setDatabaseName("memory:MyDbTest");
		ds.setCreateDatabase("create");
		
		Connection conn = ds.getConnection();
		
		ScriptRunner sr = new ScriptRunner(conn, true, true);
		sr.setLogWriter(null);
		sr.runScript(new InputStreamReader(Derby.class.getResourceAsStream("Derby_sql.txt")));

		Platform  platform = PlatformFactory.createNewPlatformInstance(ds);
		Database db = platform.readModelFromDatabase(conn, "MyDbTest");
		
		DatabaseIO dbio = new DatabaseIO();
		StringPrintStream out = new StringPrintStream();
		Writer wr = new OutputStreamWriter(out);
		
		dbio.write(db, wr);
		wr.flush();
		System.out.println(out.toString());
		out.close();
		conn.close();
	}
	
	public static String driver = "org.apache.derby.jdbc.EmbeddedDriver";

	public void demoHowToObtainAutoId() throws Exception {
		dbToXmlDemo();
		Connection conn = DriverManager.getConnection("jdbc:derby:memory:MyDbTest;create=true");

		for (int iii = 1; iii < 5; iii++) {
			PreparedStatement ps = conn.prepareStatement("insert into channel(name) values(?)", new String[] { "ID" });
			ps.setString(1, "some name");
			ps.execute();
			ResultSet rs = ps.getGeneratedKeys();
			ResultSetMetaData meta = rs.getMetaData();
			while (rs.next()) {
				for (int i = 1; i <= meta.getColumnCount(); i++) {
					System.out.print(rs.getObject(i));
					System.out.print('\t');
				}
				System.out.println();
			}
		}
		conn.close();
	}
	
	public static class Emp implements Serializable {
		int id;
		String name;
		String position;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPosition() {
			return position;
		}

		public void setPosition(String position) {
			this.position = position;
		}
	}
	
	public static void main2(String[] args) throws Exception {
//		EmbeddedDriver driver = new EmbeddedDriver(); //Class.forName(driver).newInstance();

		dbToXmlDemo();
		EmbeddedDataSource40 ds = new EmbeddedDataSource40();
		ds.setDatabaseName("memory:MyDbTest");
		ds.setCreateDatabase("create");

		QueryRunner qr = new QueryRunner(ds);
		qr.update("insert into emp(name, position) values (?,?)", "kuku", "pipi");
		List<Emp> emps = (List) qr.query("select * from emp", new BeanListHandler(Emp.class));
		for (Emp e : emps) {
			System.out.println(e.id + "\t" + e.name + "\t" + e.position);
		}
		
		// DriverManager.getConnection("jdbc:derby:memory:MyDbTest;shutdown=true");
		// DriverManager.getConnection("jdbc:derby:;shutdown=true");
		System.out.println("Done.");
	}
	
	public static void main(String[] args) throws Exception {
		Connection conn = DriverManager.getConnection("jdbc:derby:memory:MyDbTest;create=true");
		System.out.println(conn.getClass());
	}
}
