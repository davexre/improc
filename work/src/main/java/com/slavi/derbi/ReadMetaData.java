package com.slavi.derbi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.slavi.db.oracle.ConnectToOracle;
import com.slavi.dbutil.DbUtil;
import com.slavi.dbutil.ResultSetToStringHandler;
import com.slavi.dbutil.ScriptRunner;

public class ReadMetaData {
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

	static void copyDatabaseMetadata(Connection sourceConnToOracle, Connection sqlite, String tableNameSuffix) throws SQLException {
		try (Statement st = sqlite.createStatement()) {
			st.execute("drop table if exists " + "TABLES" + tableNameSuffix);
			st.execute("drop table if exists " + "TAB_COLUMNS" + tableNameSuffix);
			st.execute("drop table if exists " + "INDEXES" + tableNameSuffix);
			st.execute("drop table if exists " + "IND_COLUMNS" + tableNameSuffix);
			st.execute("drop table if exists " + "CONSTRAINTS" + tableNameSuffix);
			st.execute("drop table if exists " + "CONS_COLUMNS" + tableNameSuffix);
			st.execute("drop table if exists " + "VIEWS" + tableNameSuffix);
			st.execute("drop table if exists " + "MVIEWS" + tableNameSuffix);
			st.execute("drop table if exists " + "TRIGGERS" + tableNameSuffix);
			st.execute("drop table if exists " + "SOURCE" + tableNameSuffix);
		}

		try (Statement st = sourceConnToOracle.createStatement()) {
			int commitEveryNumRows = 10000;
			DbUtil.copyResultSet(st.executeQuery("select table_name, temporary, secondary, nested, compression, default_collation, external from user_tables"), sqlite, "TABLES" + tableNameSuffix, commitEveryNumRows);
			DbUtil.copyResultSet(st.executeQuery("select table_name, column_name, data_type, data_length, data_precision, data_scale, nullable, column_id, collation from user_tab_columns"), sqlite, "TAB_COLUMNS" + tableNameSuffix, commitEveryNumRows);
			DbUtil.copyResultSet(st.executeQuery("select index_name, index_type, table_owner, table_name, table_type, uniqueness, compression, temporary, generated, secondary from user_indexes"), sqlite, "INDEXES" + tableNameSuffix, commitEveryNumRows);
			DbUtil.copyResultSet(st.executeQuery("select index_name, table_name, column_name, column_position, descend from user_ind_columns"), sqlite, "IND_COLUMNS" + tableNameSuffix, commitEveryNumRows);
			DbUtil.copyResultSet(st.executeQuery("select owner, constraint_name, constraint_type, table_name, search_condition, r_owner, r_constraint_name, delete_rule, status from user_constraints"), sqlite, "CONSTRAINTS" + tableNameSuffix, commitEveryNumRows);
			DbUtil.copyResultSet(st.executeQuery("select owner, constraint_name, table_name, column_name, position from user_cons_columns"), sqlite, "CONS_COLUMNS" + tableNameSuffix, commitEveryNumRows);
			DbUtil.copyResultSet(st.executeQuery("select view_name, text_length, text_vc from user_views"), sqlite, "VIEWS" + tableNameSuffix, commitEveryNumRows);
			DbUtil.copyResultSet(st.executeQuery("select owner, mview_name, container_name, query, query_len, updatable, update_log, refresh_mode, refresh_method, default_collation from user_mviews"), sqlite, "MVIEWS" + tableNameSuffix, commitEveryNumRows);
			DbUtil.copyResultSet(st.executeQuery("select trigger_name, trigger_type, triggering_event, table_owner, base_object_type, table_name, column_name, when_clause, status, trigger_body, crossedition, before_statement, before_row, after_row, after_statement, instead_of_row, fire_once from user_triggers"), sqlite, "TRIGGERS" + tableNameSuffix, commitEveryNumRows);
			DbUtil.copyResultSet(st.executeQuery("select name, type, line, text from user_source"), sqlite, "SOURCE" + tableNameSuffix, commitEveryNumRows);
		}
	}

	static boolean compare(Connection sqlite, StringBuilder report) throws Exception {
		ScriptRunner sr = new ScriptRunner(sqlite, true, true);
		sr.setLogWriter(null);
		sr.runScript(new InputStreamReader(Derby.class.getResourceAsStream("ReadMetaData.compare.sql.txt")));

		List<AbstractMap.SimpleEntry<String, List<String>>> r = new ArrayList<>();
		String msg = null;
		List<String> lst = null;
		Statement st = sqlite.createStatement();
		ResultSet rs = st.executeQuery("select m.message, c.obj_name from compare c join compare_msg m on m.err_code = c.err_code order by 1,2");
		boolean hasErrors = false;
		while (rs.next()) {
			String tmp = StringUtils.trimToEmpty(rs.getString(1));
			if (!tmp.equals(msg)) {
				msg = tmp;
				lst = new ArrayList<>();
				r.add(new AbstractMap.SimpleEntry(msg, lst));
			}
			lst.add(StringUtils.trimToEmpty(rs.getString(2)));
			hasErrors = true;
		}

		StringWriter content = new StringWriter();
		Velocity.init();
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("errors", r);
		velocityContext.put("su", StringUtils.class);
		Velocity.evaluate(velocityContext, content, "", new InputStreamReader(ReadMetaData.class.getResourceAsStream("ReadMetaData.vm")));
		report.append(content.toString());
		return hasErrors;
	}

	void doIt1() throws Exception {
		try (
				Connection sourceConnToOracle = getConn();
				Connection targetConnToSQLite = getMyConn();
			) {
			copyDatabaseMetadata(sourceConnToOracle, targetConnToSQLite, "2");
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

	static int main0(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("h", "help", true, "Display this help");
		options.addOption("f", "file", true, "File to store database metadata");
		options.addOption("s", "src-url", true, "Connect string to source database");
		options.addOption("su", "src-user", true, "User name for source database");
		options.addOption("sp", "src-pass", true, "Password for source database");
		options.addOption("t", "target-url", true, "Connect string to target database");
		options.addOption("tu", "target-user", true, "User name for target database");
		options.addOption("tp", "target-pass", true, "Password for target database");
		options.addOption("c", "compare", false, "Compare database metadata");
		CommandLineParser clp = new DefaultParser();
		CommandLine cl = clp.parse(options, args, false);

		boolean showHelp = true;
		if (!(cl.hasOption("h") || !cl.getArgList().isEmpty() || args == null || args.length == 0)) {
			try {
				String fname = cl.getOptionValue("f", "dbcompare.sqlite");
				Connection sqlite = DriverManager.getConnection("jdbc:sqlite:" + fname);
				sqlite.setAutoCommit(false);

				if (cl.hasOption("s")) {
					String url = "jdbc:oracle:thin:@" + cl.getOptionValue("s");
					Connection conn = null;
					if (cl.hasOption("su")) {
						conn = DriverManager.getConnection(url, cl.getOptionValue("su"), cl.getOptionValue("sp"));
					} else {
						conn = DriverManager.getConnection(url);
					}
					copyDatabaseMetadata(conn, sqlite, "1");
					showHelp = false;
				}

				if (cl.hasOption("t")) {
					String url = "jdbc:oracle:thin:@" + cl.getOptionValue("t");
					Connection conn = null;
					if (cl.hasOption("tu")) {
						conn = DriverManager.getConnection(url, cl.getOptionValue("tu"), cl.getOptionValue("tp"));
					} else {
						conn = DriverManager.getConnection(url);
					}
					copyDatabaseMetadata(conn, sqlite, "2");
					showHelp = false;
				}

				if (cl.hasOption("c")) {
					StringBuilder report = new StringBuilder();
					boolean hasErrors = compare(sqlite, report);
					System.out.println(report);
					if (hasErrors)
						return 1;
					showHelp = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return 254;
			}
		}

		if (showHelp) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("dbcompare", "", options, IOUtils.toString(ReadMetaData.class.getResourceAsStream("ReadMetaData.help-footer.txt"), "UTF8"));
			return 255;
		}
		return 0;
	}

	public static void main(String[] args) throws Exception {
		System.exit(main0(new String[] {
			"-f", "target/MyDbTest.sqlite",
			"-s", "(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=sofracpci.sofia.ifao.net)(PORT=1677)))(CONNECT_DATA=(SERVICE_NAME=devcytr_srv.sofia.ifao.net)))",
			"-su", "spetrov",
			"-sp", "spetrov",
			//"-c",
			"asd?"
		}));
	}
}
