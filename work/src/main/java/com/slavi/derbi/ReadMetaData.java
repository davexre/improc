package com.slavi.derbi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.derby.jdbc.EmbeddedDataSource40;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.ann.test.Utils;
import com.slavi.dbutil.ResultSetToString;
import com.slavi.dbutil.ResultSetToStringHandler;
import com.slavi.dbutil.ScriptRunner;
import com.slavi.util.StringPrintStream;

public class ReadMetaData {

	EmbeddedDataSource40 ds;
	ReadMetaData() {
		ds = new EmbeddedDataSource40();
		ds.setDatabaseName("memory:MyDbTest");
		ds.setCreateDatabase("create");
	}

	Connection getConn() throws SQLException {
		//return DriverManager.getConnection("jdbc:derby:target/MyDbTest;create=true");
		//return DriverManager.getConnection("jdbc:derby:memory:MyDbTest;create=true");
		return ds.getConnection();
	}

	MapListHandler handler = new MapListHandler();

	List<Map<String, Object>> readRS(ResultSet rs) throws SQLException {
		try {
			return handler.handle(rs);
		} finally {
			DbUtils.closeQuietly(rs);
		}
	}

	ObjectMapper mapper = Utils.jsonMapper();
	String toJsonStr(Object o) throws JsonGenerationException, JsonMappingException, IOException {
		StringPrintStream out = new StringPrintStream();
		mapper.writeValue(out, o);
		return out.toString();
	}

	String resultSet2dml(ResultSet rs, String destTableName) throws Exception {
		StringBuilder r = new StringBuilder();
		r.append("insert into ").append(destTableName).append("(");
		ResultSetMetaData meta = rs.getMetaData();

		String prefix = "";
		for (int i = 1; i <= meta.getColumnCount(); i++) {
			r.append(prefix).append(meta.getColumnName(i));
			prefix = ",";
		}
		r.append(") values (");

		StringBuilder res = new StringBuilder();
		while (rs.next()) {
			res.append(r);
			prefix = "";
			for (int i = 1; i <= meta.getColumnCount(); i++) {
				Object o = rs.getObject(i);
				r.append(prefix);

				if (o instanceof Number)
					r.append(o);
				else if (o instanceof String || o instanceof Character)
					;
				prefix = ",";
			}
		}

		return res.toString();
	}

	void doIt() throws Exception {
		try (Connection conn = getConn()) {
			ScriptRunner sr = new ScriptRunner(conn, true, true);
			sr.setLogWriter(null);
			sr.runScript(new InputStreamReader(Derby.class.getResourceAsStream("Derby_HR_schema.sql.txt")));

			QueryRunner qr = new QueryRunner();
			ResultSetToStringHandler rss = new ResultSetToStringHandler();
			System.out.println(qr.query(conn, "select * from departments", rss));
			System.out.println(qr.query(conn, "select department_name from departments where department_id = 1", new ScalarHandler()));
/*
			try (Statement st = conn.createStatement()) {
				ResultSet rs = st.executeQuery("select * from employees");
				System.out.println(resultSet2dml(rs, "asd"));
				rs.close();
			}
*/
/*
			List<Map<String, Object>> l = qr.query(conn, "select * from emp", handler);
			System.out.println(toJsonStr(l));
*/
/*
			DatabaseMetaData dbmeta = conn.getMetaData();
			System.out.println(toJsonStr(readRS(dbmeta.getTables(null, null, null, null))));
*/
		}
	}

	public static void main(String[] args) throws Exception {
		new ReadMetaData().doIt();
		System.out.println("Done.");
	}
}
