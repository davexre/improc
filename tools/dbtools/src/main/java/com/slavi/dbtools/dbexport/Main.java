package com.slavi.dbtools.dbexport;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.slavi.dbtools.dbcompare.JdbcCompare;

public class Main {
	public static int main0(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("h", "help", false, "Display this help");

		options.addOption("sql", null, true, "SQL to use export data");
		options.addOption("sf", "sqlFile", true, "Read sql commands from file");
		options.addOption("t", "tables", true, "List of tables to export. Separator can be space, comma, semi column or column");

		options.addOption("c", "createScript", false, "Generate a create table script");
		options.addOption("f", "file", true, "Pattern to use for output file names. Use %t (table name), %n (sql number), %d (date stamp)");
		options.addOption("mode", null, true, "Output format mode. Default is excel");

		options.addOption("url", "", true, "Connect string to database");
		options.addOption("u", "user", true, "User name to connect to database");
		options.addOption("p", "password", true, "Password to connect to database");

		options.addOption("turl", "targetUrl", true, "Connect string to target database");
		options.addOption("tu", "targetUser", true, "User name to connect to target database");
		options.addOption("tp", "targetPassword", true, "Password to connect to target database");
		options.addOption("et", "existingTables", true, "Action to take on existing tables in target database");
		CommandLineParser clp = new DefaultParser();
		boolean showHelp = true;
		CommandLine cl = null;
		try {
			cl = clp.parse(options, args, false);
		} catch (ParseException e) {
			// ignore
		}

		if (cl != null &&
			cl.hasOption("url") &&
			cl.getArgList().size() == 0 &&
			!cl.hasOption("h")
		) {
			Connection conn = null;
			if (cl.hasOption("u")) {
				conn = DriverManager.getConnection(cl.getOptionValue("url"), cl.getOptionValue("u"), cl.getOptionValue("p"));
			} else {
				conn = DriverManager.getConnection(cl.getOptionValue("url"));
			}

			String format = cl.getOptionValue("mode", "excel");
			String defaultExtension = ".csv";
			String defaultFileNamePattern = "output" + defaultExtension;
			ExportResultSet exporter = null;
			boolean sortRecords = true;
			switch (format) {
				case "targetDb":
					defaultExtension = "";
					defaultFileNamePattern = "%t";
					sortRecords = false;
					Connection targetConn = null;
					if (cl.hasOption("tu")) {
						targetConn = DriverManager.getConnection(cl.getOptionValue("turl"), cl.getOptionValue("tu"), cl.getOptionValue("tp"));
					} else {
						targetConn = DriverManager.getConnection(cl.getOptionValue("turl"));
					}
					exporter = new TargetDbExport(targetConn, cl.getOptionValue("et", "drop"), JdbcCompare.commitEveryNumRows);
					break;

				case "sql":
					defaultExtension = ".sql";
					defaultFileNamePattern = "output" + defaultExtension;
					exporter = new SQLExport();
					break;

				case "mysql":
					exporter = new CSVExport(CSVFormat.MYSQL);
					break;

				case "unload":
					exporter = new CSVExport(CSVFormat.INFORMIX_UNLOAD);
					break;

				case "unload_csv":
					exporter = new CSVExport(CSVFormat.INFORMIX_UNLOAD_CSV);
					break;

				case "rfc4180":
					exporter = new CSVExport(CSVFormat.RFC4180);
					break;

				case "excel":
				default:
					exporter = new CSVExport(CSVFormat.EXCEL);
					break;
			}

			InternalScriptRunner sr = new InternalScriptRunner(conn);
			sr.exporter = exporter;
			sr.fnpattern = cl.getOptionValue("f", defaultFileNamePattern);
			sr.createTablesScript = cl.hasOption("c");

			Reader fin;
			if (cl.hasOption("sql")) {
				fin = new StringReader(cl.getOptionValue("sql"));
			} else if (cl.hasOption("sf")) {
				String fname = cl.getOptionValue("sf");
				fin = new FileReader(fname);
				if (!cl.hasOption("f")) {
					sr.fnpattern = FilenameUtils.removeExtension(fname) + defaultExtension;
				}
			} else if (cl.hasOption("t")) {
				StringBuilder sb = new StringBuilder();
				String tables[] = StringUtils.split(cl.getOptionValue("t"), " ,;:");
				for (int i = 0; i < tables.length; i++) {
					String t = tables[i];
					String tt = StringUtils.trimToNull(t);
					if (tt == null)
						continue;
					if (tt.startsWith("\"") && tt.endsWith("\"") && tt.length() > 1) {
						tt = tt.substring(1, tt.length() - 1);
					} else {
						tt = tt.toUpperCase();
					}
					sb.append("select * from \"").append(tt).append("\"");
					if (sortRecords)
						sb.append(" order by 1");
					sb.append(";\n");
				}
				fin = new StringReader(sb.toString());
				sr.tables = tables;
				if (!cl.hasOption("f"))
					sr.fnpattern = "%t" + defaultExtension;
			} else {
				fin = new InputStreamReader(System.in);
			}

			sr.runScript(fin);

			exporter.close();
			fin.close();
			conn.close();
			showHelp = false;
		}

		if (showHelp) {
			//HelpFormatter formatter = new HelpFormatter();
			//formatter.printHelp("dbexport", "", options, IOUtils.toString(Main.class.getResourceAsStream("Main.help.txt"), "UTF8"));
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
