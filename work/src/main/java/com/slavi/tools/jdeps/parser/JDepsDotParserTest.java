package com.slavi.tools.jdeps.parser;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

public class JDepsDotParserTest {

	public void doIt(String[] args) throws Exception {
		FileReader is = new FileReader("target/example.javacc.dot/classes.dot");
		JDepsDotParser p = new JDepsDotParser(is);
		p.parse();
		is.close();
		System.out.println(p.modules);
	}

	void genPom() {
		StringWriter content = new StringWriter();
		Velocity.init();
		VelocityContext velocityContext = new VelocityContext();
		//velocityContext.put("errors", r);
		//velocityContext.put("su", StringUtils.class);
		//Velocity.evaluate(velocityContext, content, "", new InputStreamReader(Main.class.getResourceAsStream("CompareReport.vm")));
		//report.append(content.toString());
	}

	public static void main(String[] args) throws Exception {
		new JDepsDotParserTest().doIt(args);
		System.out.println("Done.");
	}
}
