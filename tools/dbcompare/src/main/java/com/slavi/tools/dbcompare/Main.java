package com.slavi.tools.dbcompare;

import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class Main {
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
		options.addOption("jdbc", "", false, "Compare using metadata from JDBC driver");
		options.addOption("jdbcschema", "", true, "Schema for which to import metdata");
		CommandLineParser clp = new DefaultParser();
		CommandLine cl = clp.parse(options, args, false);

		String schema = StringUtils.trimToNull(cl.getOptionValue("jdbcschema", null));

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
					if (cl.hasOption("jdbc"))
						JdbcCompare.copyDatabaseMetadata(conn, sqlite, "1", schema);
					else
						DBCompare.copyDatabaseMetadata(conn, sqlite, "1");
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
					if (cl.hasOption("jdbc"))
						JdbcCompare.copyDatabaseMetadata(conn, sqlite, "2", schema);
					else
						DBCompare.copyDatabaseMetadata(conn, sqlite, "2");
					showHelp = false;
				}

				if (cl.hasOption("c")) {
					StringBuilder report = new StringBuilder();
					boolean hasErrors;
					if (cl.hasOption("jdbc"))
						hasErrors = JdbcCompare.compare(sqlite, report);
					else
						hasErrors = DBCompare.compare(sqlite, report);
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
			formatter.printHelp("dbcompare", "", options, IOUtils.toString(Main.class.getResourceAsStream("Main.help-footer.txt"), "UTF8"));
			return 255;
		}
		return 0;
	}

	public static void main(String[] args) throws Exception {
/*		args = new String[] {
				"-f", "target/MyDbTest.sqlite",
				"-s", "(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=sofracpci.sofia.ifao.net)(PORT=1677)))(CONNECT_DATA=(SERVICE_NAME=devcytr_srv.sofia.ifao.net)))",
				"-su", "spetrov",
				"-sp", "spetrov",
				//"-c",
				//"asd?"
			};
*/
		System.exit(main0(args));
	}
}
