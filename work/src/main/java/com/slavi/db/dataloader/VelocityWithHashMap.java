package com.slavi.db.dataloader;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

public class VelocityWithHashMap {

	public static class MyTools {
		public static String eval(String path) {
			return "evaluated [" + path + "]";
		}
	}

	void doIt() throws Exception {
		Properties p = new Properties();
		p.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		p.put("string.resource.loader.class", VelocityStringResourceLoader.class.getName());
		p.put(RuntimeConstants.INPUT_ENCODING, "UTF-8");
		p.put("output.encoding", "UTF-8");
		p.put(RuntimeConstants.ENCODING_DEFAULT, "UTF-8");
		p.put(RuntimeConstants.RESOURCE_LOADER, List.of("classpath", "string"));
		VelocityEngine ve = new VelocityEngine(p);

		Map map = new HashMap();
		map.put("asd", "qwe");
		Map map1 = new HashMap();
		map1.put("data", "data-value");
		map.put("map", map1);
		map.put("d", MyTools.class);
		VelocityContext ctx = new VelocityContext(map);

//		Template tt = ve.getTemplate("com/slavi/reporting/velocity/RecordsetToHtml.vm");
//		Template t = ve.getTemplate("$map.data");
//		Template t = ve.getTemplate("$map['data']");
//		Template t = ve.getTemplate("$map.get('data')");
//		Template t = ve.getTemplate("${map.get('data')}");
		Template t = ve.getTemplate("$map.get('data')");

		Writer out = new StringWriter();
		t.merge(ctx, out);
		System.out.println(out.toString());
	}

	public static void main(String[] args) throws Exception {
		new VelocityWithHashMap().doIt();
		System.out.println("Done.");
	}
}
