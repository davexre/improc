package com.slavi.db.dataloader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.slavi.db.dataloader.cfg.Config;
import com.slavi.db.dataloader.cfg.EntityDef;
import com.slavi.util.DateFormats;
import com.slavi.util.concurrent.CloseableBlockingQueue;

public class CsvDataReaderTask implements Callable<Void> {
	static Logger log = DataLoader.log;

	Config cfg;
	CloseableBlockingQueue<Map> rows;
	List<Map> bufferRows;
	List<ColumnFormat> columns = new ArrayList<>();
	CSVParser parser = null;
	int maxRecordsToCheck = 100;
	String fileName;
	String tableName;

	static class ColumnFormat {
		public String name;
		public boolean notDate;
		public boolean notDouble;
		public boolean notInteger;
		public int length;
	}

	static String fixIdentifier(String name) {
		if (name == null)
			return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_#".indexOf(c) >= 0)
				sb.append(c);
			else if ("0123456789".indexOf(c) >= 0 && sb.length() > 0)
				sb.append(c);
			else if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '_')
				sb.append('_');
			if (sb.length() >= 20)
				break;
		}
		if (sb.length() > 1 && sb.charAt(sb.length() - 1) == '_')
			sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	String stringValueOf(Object o) {
		return o == null ? null : o.toString();
	}

	public CsvDataReaderTask(Config cfg, CloseableBlockingQueue<Map> rows, InputStream is, String fileName) throws Exception {
		this.cfg = cfg;
		this.rows = rows;
		this.fileName = fileName;

		CSVFormat f;
		switch (cfg.getFormat()) {
		case "csv-excel":
			f = CSVFormat.EXCEL; break;
		case "csv-informix-unload":
			f = CSVFormat.INFORMIX_UNLOAD; break;
		case "csv-informix-unload-csv":
			f = CSVFormat.INFORMIX_UNLOAD_CSV; break;
		case "csv-mysql":
			f = CSVFormat.MYSQL; break;
		case "csv-oracle":
			f = CSVFormat.ORACLE; break;
		case "csv-postgresql-csv":
			f = CSVFormat.POSTGRESQL_CSV; break;
		case "csv-postgresql-text":
			f = CSVFormat.POSTGRESQL_TEXT; break;
		case "csv-rfc4180":
			f = CSVFormat.RFC4180; break;
		case "csv-tdf":
			f = CSVFormat.TDF; break;
		case "csv":
		default:
			f = CSVFormat.DEFAULT; break;
		}

		if (cfg.getFormatOptions() != null)
			for (Map.Entry i : cfg.getFormatOptions().entrySet()) {
				String k = StringUtils.lowerCase((String) i.getKey());
				switch (k) {
				case "allowmissingcolumnnames": f = f.withAllowMissingColumnNames(Boolean.parseBoolean(stringValueOf(i.getValue()))); break;
				case "commentmarker": f = f.withCommentMarker((stringValueOf(i.getValue()) + "#").charAt(0)); break;
				case "delimiter": f = f.withDelimiter((stringValueOf(i.getValue()) + ",").charAt(0)); break;
				case "escapecharacter": f = f.withEscape((stringValueOf(i.getValue()) + "\\").charAt(0)); break;
				case "ignoreemptylines": f = f.withIgnoreEmptyLines(Boolean.parseBoolean(stringValueOf(i.getValue()))); break;
				case "ignoresurroundingspaces": f = f.withIgnoreSurroundingSpaces(Boolean.parseBoolean(stringValueOf(i.getValue()))); break;
				case "nullstring": f = f.withNullString(stringValueOf(i.getValue())); break;
				case "quotecharacter": f = f.withQuote((stringValueOf(i.getValue()) + "\"").charAt(0)); break;
				case "recordseparator": f = f.withEscape((stringValueOf(i.getValue()) + "\n").charAt(0)); break;
				case "skipheaderrecord": f = f.withSkipHeaderRecord(Boolean.parseBoolean(stringValueOf(i.getValue()))); break;
				case "header": f = f.withHeader(); break;
				case "trailingdelimiter": f = f.withTrailingDelimiter(Boolean.parseBoolean(stringValueOf(i.getValue()))); break;
				case "trim": f = f.withTrim(Boolean.parseBoolean(stringValueOf(i.getValue()))); break;

				case "maxrecordstocheck": maxRecordsToCheck = Integer.parseInt(stringValueOf(i.getValue())); break;
				case "table": tableName = stringValueOf(i.getValue()); break;
				default: throw new Exception("Invalid format option " + i.getKey());
				}
			}

		if (cfg.defs.size() == 0) {
			bufferRows = new ArrayList();
			if (maxRecordsToCheck <= 10)
				maxRecordsToCheck = 10;
		} else {
			maxRecordsToCheck = 0;
			bufferRows = null;
		}

		parser = f.parse(new InputStreamReader(is));
	}

	void checkRecordAnalysisDone() throws Exception {
		if (bufferRows == null)
			return;

		String tableName = fixIdentifier(this.tableName);
		if ("".equals(tableName))
			tableName = fixIdentifier(FilenameUtils.getName(fileName));
		if ("".equals(tableName))
			tableName = "CSV_IMPORT";

		var ctx = DataLoader.makeContext();
		try (Connection conn = DriverManager.getConnection(
				DataLoader.applyTemplate(ctx, cfg.getUrlTemplate()),
				DataLoader.applyTemplate(ctx, cfg.getUsernameTemplate()),
				DataLoader.applyTemplate(ctx, cfg.getPasswordTemplate()))) {
			DatabaseMetaData meta = conn.getMetaData();
			int autoName = 1;
			String tmp = tableName;
			while (true) {
				var rs = meta.getTables(null, null, tmp, null);
				boolean tableExists = rs.next();
				rs.close();
				if (!tableExists) {
					tableName = tmp;
					break;
				}
				tmp = tableName + "_" + autoName;
				autoName++;
			}

			List<String> params = new ArrayList<>();
			StringBuilder sql = new StringBuilder();
			sql.append("create table ").append(tableName).append("(");
			String prefix = "\n";
			for (int i = 0; i < columns.size(); i++) {
				params.add("${rec[" + i + "]}");
				ColumnFormat cf = columns.get(i);
				sql.append(prefix).append("  ").append(cf.name).append(" ");
				if (!cf.notDate) {
					sql.append("date");
				} else if (!cf.notInteger) {
					sql.append("decimal(").append(cf.length).append(")");
				} else if (!cf.notDouble) {
					sql.append("decimal(10,4)");
				} else {
					sql.append("varchar(").append(cf.length).append(")");
				}
				prefix = ",\n";
			}
			sql.append("\n)");

			String theSql = sql.toString();
			log.info("Creating table {} using \n{}", tableName, theSql);
			PreparedStatement ps = conn.prepareStatement(theSql);
			ps.execute();
			ps.close();

			sql.setLength(0);
			sql.append("insert into ").append(tableName).append("(");
			prefix = "";
			for (int i = 0; i < columns.size(); i++) {
				ColumnFormat cf = columns.get(i);
				sql.append(prefix).append(cf.name);
				prefix = ",";
			}
			sql.append(") values (");
			prefix = "";
			for (int i = 0; i < columns.size(); i++) {
				sql.append(prefix).append("?");
				prefix = ",";
			}
			sql.append(")");
			theSql = sql.toString();
			log.debug("SQL to insert\n{}", theSql);

			EntityDef def = new EntityDef(cfg.velocity);
			def.setPath(".*");
			def.setParams(params);
			def.setSql(theSql);
			cfg.defs.add(def); // Unprotected modification of a list, but still safe as all threads are waiting on rows queue.
		}

		for (var i : bufferRows)
			rows.put(i);
		bufferRows = null;
	}

	@Override
	public Void call() throws Exception {
		try (AutoCloseable dummy = rows) {
			DateFormats df;
			if (cfg.getDateFormats() == null || cfg.getDateFormats().isEmpty())
				df = new DateFormats(RowProcessTask.defaultDateFormats);
			else
				df = new DateFormats(cfg.getDateFormats());

			int columnAutoName = 1;
			var columnNames = new HashSet<String>();
			var header = parser.getHeaderMap();
			if (header != null) {
				for (String i : header.keySet()) {
					ColumnFormat cf = new ColumnFormat();
					cf.name = fixIdentifier(i);
					if (cf.name.startsWith("_") && (!cf.name.startsWith("__"))) {
						cf.name = "_" + cf.name;
					}
					while (columnNames.contains(cf.name)) {
						cf.name = "COLUMN_" + columnAutoName;
						columnAutoName++;
					}
					columns.add(cf);
				}
			}
			header = null;

			var iter = parser.iterator();
			while (iter.hasNext()) {
				var rec = iter.next();
				long recordNumber = parser.getRecordNumber();
				Map cur = new HashMap<>();
				cur.put(DataLoader.tagLine, parser.getCurrentLineNumber());
				cur.put(DataLoader.tagId, recordNumber);
				cur.put(DataLoader.tagPath, "/");
				cur.put(DataLoader.tagCol, 1);
				for (int i = 0; i < rec.size(); i++) {
					ColumnFormat cf;
					if (i < columns.size()) {
						cf = columns.get(i);
					} else {
						cf = new ColumnFormat();
						while (cf.name == null || columnNames.contains(cf.name)) {
							cf.name = "COLUMN_" + columnAutoName;
							columnAutoName++;
						}
						columns.add(cf);
					}

					String val = StringUtils.trimToNull(rec.get(i));
					cur.put(i, val);
					cur.put(cf.name, val);

					if (val != null && bufferRows != null) {
						if (cf.length < val.length())
							cf.length = val.length();
						if (!cf.notDouble) {
							try {
								Double.parseDouble(val);
							} catch (Throwable t) {
								cf.notDouble = true;
							}
						}
						if (!cf.notInteger) {
							try {
								Long.parseLong(val);
							} catch (Throwable t) {
								cf.notInteger = true;
							}
						}
						if (!cf.notDate) {
							try {
								Date dt = df.parse(val);
								if (dt == null)
									cf.notDate = true;
							} catch (Throwable t) {
								cf.notDate = true;
							}
						}
					}
				}

				if (recordNumber < maxRecordsToCheck) {
					bufferRows.add(cur);
				} else {
					checkRecordAnalysisDone();
					rows.put(cur);
				}
			}
			checkRecordAnalysisDone();
		}
		return null;
	}
}
