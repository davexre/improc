package com.test.scripting;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

public class VelocityExample {

	public void doIt(String[] args) throws Exception {
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath"); 
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		ve.init();
		
		ArrayList rows = new ArrayList();
		for (int i = 0; i < 10; i++) {
			HashMap row = new HashMap();
			row.put("Col1", "Column 1, row " + i);
			row.put("Col2", "Column 2, row " + i);
			row.put("Col3", "Column 3, row " + i);
			rows.add(row);
		}
		Template t = ve.getTemplate("com/test/scripting/VelocityExample.vm");

		VelocityContext vc = new VelocityContext();
		vc.put("username", "John");
		vc.put("rows", rows);
		vc.put("strutl", StringUtils.class);

		StringWriter sw = new StringWriter();
		t.merge(vc, sw);

		System.out.println(sw);
	}

	public static void main(String[] args) throws Exception {
		new VelocityExample().doIt(args);
		System.out.println("Done.");
	}
}
