package com.slavi.reporting.freemarker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.slavi.dbutil.ResultSetToIteratorList;
import com.slavi.derbi.hr.Derby;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class HtmlReportTest {

	public static class RS {
		List cols;
		Iterator rows;

		public List getCols() {
			return cols;
		}

		public Iterator getRows() {
			return rows;
		}

		public RS(ResultSetToIteratorList rs) {
			cols = rs.getColumnNames();
			rows = rs;
		}
	}

	public static class Utils {
		Connection conn;

		public Utils(Connection conn) {
			this.conn = conn;
		}

		public String esc(String str) {
			return StringEscapeUtils.escapeHtml4(str);
		}

		public RS sqlAsList(String sql) throws SQLException {
			return new RS(new ResultSetToIteratorList(conn.createStatement().executeQuery(sql)));
		}
/*
		public Map sqlAsList(String sql) throws SQLException {
			Map r = new HashMap();
			ResultSetToIteratorList i = new ResultSetToIteratorList(conn.createStatement().executeQuery(sql));
			r.put("rows", i);
			r.put("cols", i.getColumnNames());
			return r;
		}*/
	}

	DataSource ds;

	void doIt() throws Exception {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ds = Derby.generateDb();

		Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
		//cfg.setClassForTemplateLoading(getClass(), "");
		cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "");
		Template template = cfg.getTemplate("/com/slavi/reporting/freemarker/RecordsetToHtml.ftlh");

		String fou = "target/report";
		try (
				Connection conn = ds.getConnection();
			) {
			Writer html = new StringWriter();

			Map<String, Object> ctx = new HashMap<>();
			ctx.put("su", StringUtils.class);
			ctx.put("date", df.format(new Date()));
			ctx.put("title", "HTML report test via Apache Freemaker example");
			ctx.put("items", Arrays.asList("One", "Two", "Three").iterator());
			ctx.put("seu", StringEscapeUtils.class);
			ctx.put("utl", new Utils(conn));
			ctx.put("cssFile", "/resources/bootstrap/4.1.3/css/bootstrap.css");

			template.process(ctx, html);

			System.out.println(html.toString());
			Writer fhtml = new FileWriter(fou + ".html");
			fhtml.write(html.toString());
			fhtml.close();

			// Html to pdf
			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocumentFromString(html.toString());
			renderer.layout();
			OutputStream pdf = new FileOutputStream(fou + ".pdf");
			renderer.createPDF(pdf, true);
			pdf.close();
		}
		System.out.println(new File(fou + ".html").getAbsolutePath());
	}

	public static void main(String[] args) throws Exception {
		new HtmlReportTest().doIt();
		System.out.println("Done.");
	}
}
