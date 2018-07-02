package com.slavi.derbi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.ann.test.Utils;
import com.slavi.dbutil.ResultSetToStringHandler;
import com.slavi.dbutil.ScriptRunner;
import com.slavi.util.StringPrintStream;

public class ReadMetaData {

	Connection getConn() throws SQLException {
		//return DriverManager.getConnection("jdbc:derby:target/MyDbTest;create=true");
		return DriverManager.getConnection("jdbc:derby:memory:MyDbTest;create=true");
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

	void doIt() throws Exception {
		try (Connection conn = getConn()) {
			ScriptRunner sr = new ScriptRunner(conn, true, true);
			sr.setLogWriter(null);
			sr.runScript(new InputStreamReader(Derby.class.getResourceAsStream("Derby_HR_schema.sql.txt")));

			QueryRunner qr = new QueryRunner();
			ResultSetToStringHandler rss = new ResultSetToStringHandler();
			System.out.println(qr.query(conn, "select * from departments", rss));

//			List<Map<String, Object>> l = qr.query(conn, "select * from emp", handler);
//			System.out.println(toJsonStr(l));

			DatabaseMetaData dbmeta = conn.getMetaData();
			System.out.println(toJsonStr(readRS(dbmeta.getTables(null, null, null, null))));

		}
	}

	public static void main(String[] args) throws Exception {
		new ReadMetaData().doIt();
		System.out.println("Done.");
	}
}
