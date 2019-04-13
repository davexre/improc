package com.slavi.derbi.northwind;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.derby.jdbc.EmbeddedDataSource;

import com.slavi.dbutil.DbDataParser;
import com.slavi.dbutil.DbDataParserTemplate;
import com.slavi.dbutil.DbDataValueParser;
import com.slavi.dbutil.MyDbScriptRunner;

public class CreateDb {
	public final static Charset utf8 = Charset.forName("UTF8");

	public static ByteArrayInputStream parseCrappyBmpImage(String str) throws ParseException {
		byte[] r = DbDataParserTemplate.hexToBytes(str);
		if (r == null || r.length <= 78)
			return null;
		return new ByteArrayInputStream(r, 78, r.length - 78);
	}

	public static DataSource generateDb() throws Exception {
		return new CreateDb().createDb();
	}

	Connection conn;
	Properties sql;
	DbDataParserTemplate dbDataParserTemplate;

	void processCSV(String fin, Object... dpParams) throws Exception {
		PreparedStatement ps = conn.prepareStatement(sql.getProperty(fin));
		DbDataParser dp = new DbDataParser(ps, dbDataParserTemplate);
		if (dpParams != null) {
			int i = 0;
			while (i < dpParams.length) {
				int index = (Integer) (dpParams[i++]);
				DbDataValueParser p = (DbDataValueParser) (dpParams[i++]);
				dp.parsers.set(index, p);
			}
		}

		CSVParser p = CSVParser.parse(Derby.class.getResourceAsStream("data/" + fin), utf8, CSVFormat.EXCEL.withQuote('\''));
		try {
			p.iterator().next();
			while (p.iterator().hasNext()) {
				CSVRecord r = p.iterator().next();
				dp.reset();
				for (int i = 0; i < dp.size(); i++) {
					String v = i < r.size() ? r.get(i) : null;
					dp.set(v);
				}
				dp.ps.executeUpdate();
			}
		} catch (Throwable t) {
			System.out.println("Error in file " + fin + " at line " + p.getCurrentLineNumber());
			throw t;
		}
		p.close();
		dp.ps.close();
	}

	public DataSource createDb() throws Exception {
		EmbeddedDataSource ds = new EmbeddedDataSource();
		ds.setDatabaseName("memory:northwind;create=true");

		DbDataValueParser crappyImageParser = (v) -> parseCrappyBmpImage(v);
		sql = new Properties();
		sql.load(Derby.class.getResourceAsStream("data/sql.properties"));
		conn = ds.getConnection();
		dbDataParserTemplate = new DbDataParserTemplate("MM/dd/yyyy");

		MyDbScriptRunner sr = new MyDbScriptRunner(conn);
		sr.process(Derby.class.getResourceAsStream("Derby_create_schema.sql.txt"));

		processCSV("Employees.csv", 14, crappyImageParser);
		processCSV("Categories.csv", 3, crappyImageParser);
		processCSV("Customers.csv");
		processCSV("Shippers.csv");
		processCSV("Suppliers.csv");
		processCSV("Orders.csv");
		processCSV("Products.csv");
		processCSV("Order Details.csv");
		processCSV("Region.csv");
		processCSV("Territories.csv");
		processCSV("EmployeeTerritories.csv");

		sr.process(Derby.class.getResourceAsStream("Derby_create_schema_views.sql.txt"));
		return ds;
	}
}
