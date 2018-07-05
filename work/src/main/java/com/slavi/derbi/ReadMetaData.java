package com.slavi.derbi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.ann.test.Utils;
import com.slavi.db.oracle.ConnectToOracle;
import com.slavi.dbutil.DbUtil;
import com.slavi.dbutil.ResultSetToString;
import com.slavi.dbutil.ResultSetToStringHandler;
import com.slavi.dbutil.ScriptRunner;
import com.slavi.util.Marker;
import com.slavi.util.StringPrintStream;

public class ReadMetaData {
/*
	EmbeddedDataSource40 ds;
	ReadMetaData() {
		ds = new EmbeddedDataSource40();
		ds.setDatabaseName("memory:MyDbTest");
		ds.setCreateDatabase("create");
	}
*/
	Connection getMyConn() throws SQLException {
		return DriverManager.getConnection("jdbc:sqlite:target/MyDbTest.sqlite");
		//return DriverManager.getConnection("jdbc:sqlite::memory:");
	}

	Connection getConn() throws SQLException, IOException {
		ConnectToOracle o = new ConnectToOracle();
		return DriverManager.getConnection(o.prop.getProperty("connectStr.2"), o.prop.getProperty("username.2"), o.prop.getProperty("password.2"));
		//return o.getConnection();

		//return DriverManager.getConnection("jdbc:derby:target/MyDbTest;create=true");
		//return DriverManager.getConnection("jdbc:derby:memory:MyDbTest;create=true");
		//return ds.getConnection();
		//return DriverManager.getConnection("jdbc:sqlite:target/MyDbTest.sqlite");
		//return DriverManager.getConnection("jdbc:sqlite::memory:");
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

	void executeSqlQuietly(Connection conn, String sql) {
		try (Statement st = conn.createStatement()) {
			st.execute(sql);
		} catch (SQLException e) {
		}
	}

	void copyDatabaseMetadata(Connection sourceConnToOracle, Connection targetConnToSQLite, String tableNameSuffix) throws SQLException {
		int commitEveryNumRows = 10000;
		targetConnToSQLite.setAutoCommit(false);

		executeSqlQuietly(targetConnToSQLite, "drop table " + "TABLES" + tableNameSuffix);
		executeSqlQuietly(targetConnToSQLite, "drop table " + "TAB_COLUMNS" + tableNameSuffix);
		executeSqlQuietly(targetConnToSQLite, "drop table " + "INDEXES" + tableNameSuffix);
		executeSqlQuietly(targetConnToSQLite, "drop table " + "IND_COLUMNS" + tableNameSuffix);
		executeSqlQuietly(targetConnToSQLite, "drop table " + "CONSTRAINTS" + tableNameSuffix);
		executeSqlQuietly(targetConnToSQLite, "drop table " + "CONS_COLUMNS" + tableNameSuffix);
		executeSqlQuietly(targetConnToSQLite, "drop table " + "VIEWS" + tableNameSuffix);
		executeSqlQuietly(targetConnToSQLite, "drop table " + "MVIEWS" + tableNameSuffix);
		executeSqlQuietly(targetConnToSQLite, "drop table " + "TRIGGERS" + tableNameSuffix);
		executeSqlQuietly(targetConnToSQLite, "drop table " + "SOURCE" + tableNameSuffix);

		Statement st = sourceConnToOracle.createStatement();
		DbUtil.copyResultSet(st.executeQuery("select table_name, temporary, secondary, nested, compression, default_collation, external from user_tables"), targetConnToSQLite, "TABLES" + tableNameSuffix, commitEveryNumRows);
		DbUtil.copyResultSet(st.executeQuery("select table_name, column_name, data_type, data_length, data_precision, data_scale, nullable, column_id, collation from user_tab_columns"), targetConnToSQLite, "TAB_COLUMNS" + tableNameSuffix, commitEveryNumRows);
		DbUtil.copyResultSet(st.executeQuery("select index_name, index_type, table_owner, table_name, table_type, uniqueness, compression, temporary, generated, secondary from user_indexes"), targetConnToSQLite, "INDEXES" + tableNameSuffix, commitEveryNumRows);
		DbUtil.copyResultSet(st.executeQuery("select index_name, table_name, column_name, column_position, descend from user_ind_columns"), targetConnToSQLite, "IND_COLUMNS" + tableNameSuffix, commitEveryNumRows);
		DbUtil.copyResultSet(st.executeQuery("select owner, constraint_name, constraint_type, table_name, search_condition, r_owner, r_constraint_name, delete_rule, status from user_constraints"), targetConnToSQLite, "CONSTRAINTS" + tableNameSuffix, commitEveryNumRows);
		DbUtil.copyResultSet(st.executeQuery("select owner, constraint_name, table_name, column_name, position from user_cons_columns"), targetConnToSQLite, "CONS_COLUMNS" + tableNameSuffix, commitEveryNumRows);
		DbUtil.copyResultSet(st.executeQuery("select view_name, text_length, text_vc from user_views"), targetConnToSQLite, "VIEWS" + tableNameSuffix, commitEveryNumRows);
		DbUtil.copyResultSet(st.executeQuery("select owner, mview_name, container_name, query, query_len, updatable, update_log, refresh_mode, refresh_method, default_collation from user_mviews"), targetConnToSQLite, "MVIEWS" + tableNameSuffix, commitEveryNumRows);
		DbUtil.copyResultSet(st.executeQuery("select trigger_name, trigger_type, triggering_event, table_owner, base_object_type, table_name, column_name, when_clause, status, trigger_body, crossedition, before_statement, before_row, after_row, after_statement, instead_of_row, fire_once from user_triggers"), targetConnToSQLite, "TRIGGERS" + tableNameSuffix, commitEveryNumRows);
		DbUtil.copyResultSet(st.executeQuery("select name, type, line, text from user_source"), targetConnToSQLite, "SOURCE" + tableNameSuffix, commitEveryNumRows);
		targetConnToSQLite.commit();
		st.close();
	}

	void doIt() throws Exception {
		try (
				Connection sourceConnToOracle = getConn();
				Connection targetConnToSQLite = getMyConn();
			) {
			Marker.mark("Copy result set");
			copyDatabaseMetadata(sourceConnToOracle, targetConnToSQLite, "2");
			Marker.release();
		}
	}

	void doIt2() throws Exception {
		try (
			Connection conn = getConn();
			Connection myconn = getMyConn();
		) {
			QueryRunner qr = new QueryRunner();
			ResultSetToStringHandler rss = new ResultSetToStringHandler();

/*			ScriptRunner sr = new ScriptRunner(conn, true, true);
			sr.setLogWriter(null);
			sr.runScript(new InputStreamReader(Derby.class.getResourceAsStream("Derby_HR_schema.sql.txt")));

			System.out.println(qr.query(conn, "select * from departments", rss));
			//System.out.println(qr.query(conn, "select department_name from departments where department_id = 1", new ScalarHandler()));
*/
			DatabaseMetaData dbmeta = conn.getMetaData();
			//PreparedStatement ps = conn.prepareStatement("select * from departments");
			//ResultSet rs = ps.executeQuery();
			DbUtil.copyResultSet(dbmeta.getTables(null, null, null, null), myconn, "tables1", 0);
			DbUtil.copyResultSet(dbmeta.getColumns(null, null, null, null), myconn, "columns1", 0);
			DbUtil.copyResultSet(dbmeta.getIndexInfo(null, null, "employee", false, false), myconn, "indexinfo1", 0);

			System.out.println(qr.query(myconn, "select * from indexinfo1", rss));

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
