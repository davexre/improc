package com.slavi.dbtools.dataload.test;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.slavi.dbtools.dataload.DataLoad;

public class VelocityWithHashMapTest {

	public static class MyTools {
		public static String eval(String path) {
			return "evaluated [" + path + "]";
		}
	}

	void doIt() throws Exception {
		VelocityEngine ve = DataLoad.makeVelocityEngine();

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
		new VelocityWithHashMapTest().doIt();
		System.out.println("Done.");
	}
}
