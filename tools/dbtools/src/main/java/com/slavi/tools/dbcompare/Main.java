package com.slavi.tools.dbcompare;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.slavi.dbutil.DbUtil;
import com.slavi.dbutil.ScriptRunner2;

public class Main {
	static int commitEveryNumRows = 10000;

	static void copyDb(Connection src, Connection dest, String tableSuffix) throws SQLException {
		DatabaseMetaData meta = src.getMetaData();
		Statement st = src.createStatement();
		ResultSet tablesrs = meta.getTables(null, null, null, null);
		while (tablesrs.next()) {
			String table = tablesrs.getString("table_name");
			ResultSet rs = st.executeQuery("select * from " + table);
			DbUtil.createResultSetSnapshot(rs, dest, table + tableSuffix, commitEveryNumRows);
		}
		tablesrs.close();
		st.close();
	}

	static QueryRunner queryRunner = new QueryRunner();
	static String getSetting(Connection conn, String name) throws SQLException {
		return (String) queryRunner.query(conn, "select value from settings where name=?", new ScalarHandler(), name);
	}

	static void addSetting(Connection conn, String name, String value) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("insert into settings(name, value) values(?,?)");
		ps.setString(1, name);
		ps.setString(2, value);
		ps.executeUpdate();
		ps.close();
	}

	static void initMetaDb(Connection conn) throws SQLException {
		Statement st = conn.createStatement();
		st.execute("drop table if exists settings");
		st.execute("create table SETTINGS(name varchar not null primary key, value varchar)");
		st.close();
	}

	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	static boolean compare(Connection sqlite, StringBuilder report, String compareScript) throws Exception {
		ScriptRunner2 sr = new ScriptRunner2(sqlite);
		sr.runScript(new InputStreamReader(Main.class.getResourceAsStream(compareScript)));

		List<AbstractMap.SimpleEntry<String, List<String>>> r = new ArrayList<>();
		String msg = null;
		List<String> lst = null;
		Statement st = sqlite.createStatement();
		ResultSet rs = st.executeQuery("select m.message, c.obj_name from compare c join compare_msg m on m.err_code = c.err_code order by c.err_code, c.obj_name");
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
		Velocity.evaluate(velocityContext, content, "", new InputStreamReader(Main.class.getResourceAsStream("CompareReport.vm")));
		report.append(content.toString());
		return hasErrors;
	}

	public static int main0(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("h", "help", true, "Display this help");
		options.addOption("f", "file", true, "File to store database metadata. Default is dbcompare.sqlite for command get and empty for command compare");
		options.addOption("url", "", true, "Connect string to database");
		options.addOption("u", "user", true, "User name to connect to database");
		options.addOption("p", "password", true, "Password to connect to database");
		options.addOption("mode", "", true, "Compare using metadata from JDBC driver. Use jdbc or oracle. Default is jdbc.");
		options.addOption("jdbcschema", "", true, "Schema for which to import metdata");
		options.addOption("sdb", "sourcedb", true, "File containing source db metadata");
		options.addOption("tdb", "targetdb", true, "File containing target db metadata");
		CommandLineParser clp = new DefaultParser();
		boolean showHelp = true;
		CommandLine cl = null;
		String cmd = null;
		try {
			cl = clp.parse(options, args, false);
			cmd = cl.getArgList().size() == 1 ? cl.getArgList().get(0) : "";
		} catch (ParseException e) {
			// ignore
		}

		if (cl != null &&
			("get".equals(cmd) || "compare".equals(cmd)) &&
			(!cl.hasOption("h"))
		) {
			switch (cmd) {
			case "get":
				String schema = StringUtils.trimToNull(cl.getOptionValue("jdbcschema", null));
				String mode = cl.getOptionValue("mode", "jdbc");
				if (cl.hasOption("url") &&
					("oracle".equals(mode) || "jdbc".equals(mode))
				) {
					String url = cl.getOptionValue("url");
					String fname = cl.getOptionValue("f", "dbcompare.sqlite");
					Connection sqlite = DriverManager.getConnection("jdbc:sqlite:" + fname);
					sqlite.setAutoCommit(false);

					initMetaDb(sqlite);
					addSetting(sqlite, "create_date", dateFormat.format(new Date()));
					addSetting(sqlite, "url", url);
					addSetting(sqlite, "user", cl.getOptionValue("u"));
					addSetting(sqlite, "begin_meta", dateFormat.format(new Date()));
					addSetting(sqlite, "mode", mode);

					if ("oracle".equals(mode))
						url = "jdbc:oracle:thin:@" + url;
					Connection conn = null;
					if (cl.hasOption("u")) {
						conn = DriverManager.getConnection(url, cl.getOptionValue("u"), cl.getOptionValue("p"));
					} else {
						conn = DriverManager.getConnection(url);
					}

					if ("oracle".equals(mode))
						OracleCompare.copyDatabaseMetadata(conn, sqlite);
					else
						JdbcCompare.copyDatabaseMetadata(conn, sqlite, schema);

					addSetting(sqlite, "end_meta", dateFormat.format(new Date()));
					sqlite.commit();
					sqlite.close();

					showHelp = false;
				}
				break;
			case "compare":
				if (cl.hasOption("sdb") && cl.hasOption("tdb")) {
					Connection sourceConn = DriverManager.getConnection("jdbc:sqlite:" + cl.getOptionValue("sdb"));
					Connection targetConn = DriverManager.getConnection("jdbc:sqlite:" + cl.getOptionValue("tdb"));
					String sourceMode = StringUtils.trimToEmpty(getSetting(sourceConn, "mode"));
					String targetMode = StringUtils.trimToEmpty(getSetting(targetConn, "mode"));
					if (!sourceMode.equals(targetMode)) {
						System.out.println("The mode of the source (" + sourceMode + ") and target (" + targetMode + ") database do not match.");
						return 2;
					}

					String fname = cl.getOptionValue("f", ":memory:");
					Connection sqlite = DriverManager.getConnection("jdbc:sqlite:" + fname);
					sqlite.setAutoCommit(false);

					copyDb(sourceConn, sqlite, "1");
					copyDb(targetConn, sqlite, "2");

					sourceConn.close();
					targetConn.close();
					sourceConn = null;
					targetConn = null;

					String compareScript = "oracle".equals(sourceMode) ? "OracleCompare.sql" : "JdbcCompare.sql";
					StringBuilder report = new StringBuilder();
					boolean hasErrors = compare(sqlite, report, compareScript);
					System.out.print(report);

					sqlite.commit();
					sqlite.close();

					if (hasErrors)
						return 1;
					showHelp = false;
				}

				break;
			default:
				break;
			}
		}

		if (showHelp) {
			//HelpFormatter formatter = new HelpFormatter();
			//formatter.printHelp("dbcompare", "", options, IOUtils.toString(Main.class.getResourceAsStream("Main.help-footer.txt"), "UTF8"));
			System.out.println(IOUtils.toString(Main.class.getResourceAsStream("Main.help.txt"), "UTF8"));
			return 254;
		}
		return 0;
	}

	public static void main(String[] args) throws Exception {
		try {
			System.exit(main0(args));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(255);
		}
	}
}
