package com.slavi.derbi;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;

import javax.sql.DataSource;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.derby.jdbc.EmbeddedDataSource;

import com.slavi.dbutil.MyDbScriptRunner;
import com.slavi.derbi.dbload.DbDataParserTemplate;
import com.slavi.derbi.dbload.DbDataParser;
import com.slavi.derbi.northwind.Derby;

public class TestParameterMetaData {
	public final static Charset utf8 = Charset.forName("UTF8");

	public static DataSource generateDb() throws Exception {
		DbDataParserTemplate dbDataParser = new DbDataParserTemplate("MM/dd/yyyy");
		EmbeddedDataSource ds = new EmbeddedDataSource();
		ds.setDatabaseName("memory:northwind;create=true");
		long line = 0;
		try (Connection conn = ds.getConnection()) {
			MyDbScriptRunner sr = new MyDbScriptRunner(conn);
			sr.process(Derby.class.getResourceAsStream("Derby_create_schema.sql.txt"));

			PreparedStatement ps = conn.prepareStatement(IOUtils.toString(Derby.class.getResourceAsStream("data/Employees.sql"), utf8));
			DbDataParser psf = new DbDataParser(ps, dbDataParser);
			psf.parsers.set(14, (v) -> dbDataParser.parseHex(v));
			/*
			CsvTokenizer t = new CsvTokenizer(",", "'");
			LineNumberReader r = new LineNumberReader(new InputStreamReader(Derby.class.getResourceAsStream("data/Employees.csv")));
			String line = r.readLine();
			t.setLine(line);
			while ((line = r.readLine()) != null) {
				t.setLine(line);
			*/

			CSVParser p = CSVParser.parse(Derby.class.getResourceAsStream("data/Employees.csv"), utf8, CSVFormat.EXCEL.withQuote('\''));
			p.iterator().next();
			while (p.iterator().hasNext()) {
				CSVRecord r = p.iterator().next();
				psf.reset();
				line = p.getCurrentLineNumber();
				int maxCol = Math.min(psf.size(), r.size());
				for (int i = 0; i < maxCol; i++)
					psf.set(r.get(i));
				ps.executeUpdate();
			}
		} catch (Throwable t) {
			System.out.println("Error on line " + line);
			throw t;
		}
		return ds;
	}

	public void doIt(String[] args) throws Exception {
		DataSource ds = generateDb();
		Connection conn = ds.getConnection();
/*		PreparedStatement ps = conn.prepareStatement("insert  into orders(orderNumber,orderDate,requiredDate,shippedDate,status,comments,customerNumber) values (?,?,?,?,?,?,?)");
		ParameterMetaData md = ps.getParameterMetaData();
		for (int i = 1; i <= md.getParameterCount(); i++) {
			System.out.println(md.getParameterTypeName(i));
		}*/
	}

	public static void main(String[] args) throws Exception {
		new TestParameterMetaData().doIt(args);
		System.out.println("Done.");
	}
}
