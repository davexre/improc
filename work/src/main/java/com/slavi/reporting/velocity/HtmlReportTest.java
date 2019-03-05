package com.slavi.reporting.velocity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.slavi.dbutil.ResultSetToIteratorList;
import com.slavi.derbi.hr.Derby;

public class HtmlReportTest {

	public static class VelocityUtils {
		Connection conn;

		public VelocityUtils(Connection conn) {
			this.conn = conn;
		}

		public String esc(String str) {
			return StringEscapeUtils.escapeHtml4(str);
		}

		public ResultSetToIteratorList sqlAsList(String sql) throws SQLException {
			return new ResultSetToIteratorList(conn.createStatement().executeQuery(sql));
		}
	}

	DataSource ds;

	void doIt() throws Exception {
		Properties p = new Properties();
		p.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		p.put(RuntimeConstants.INPUT_ENCODING, "UTF-8");
		p.put("output.encoding", "UTF-8");
		p.put(RuntimeConstants.ENCODING_DEFAULT, "UTF-8");
		p.put(RuntimeConstants.RESOURCE_LOADER, "classpath");
		VelocityEngine ve = new VelocityEngine(p);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		ds = Derby.generateDb();
		String fou = "target/report";
		try (
			Connection conn = ds.getConnection();
		) {
			Writer html = new StringWriter();
			VelocityContext ctx = new VelocityContext();
			ctx.put("su", StringUtils.class);
			ctx.put("date", df.format(new Date()));
			ctx.put("title", "HTML report test via Apache Velocity example");
			ctx.put("seu", StringEscapeUtils.class);
			ctx.put("utl", new VelocityUtils(conn));
			ctx.put("cssFile", "/resources/bootstrap/4.1.3/css/bootstrap.css");
//			ctx.put("cssFile", "/resources/bootstrap/2.3.2/css/bootstrap.css");
//			ctx.put("cssFile", "com/slavi/reporting/velocity/html2pdf.css");
			Template t = ve.getTemplate("com/slavi/reporting/velocity/RecordsetToHtml.vm");
			t.merge(ctx, html);

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
