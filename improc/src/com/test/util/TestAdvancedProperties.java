package com.test.util;

import java.util.Properties;

import com.slavi.util.Util;

public class TestAdvancedProperties {

	public static void main(String[] args) {
		Properties init = Util.makeProperties();
		Properties p = new Properties();
	    p.setProperty("V.0", "Value 0");
		p.setProperty("V.1", "Value 1");
		p.setProperty("V.2", "Value 2");

		p.setProperty("VAL$0", "Value 0");
		p.setProperty("Кирилица$1", "Value 1");
		p.setProperty("VAL$2", "Value 2");
		p.setProperty("UseVal", "1");
		p.setProperty("Z", "${ Кирилица$${UseVal} }");
		p.setProperty("W", "${ V.${UseVal} }");
		Util.mergeProperties(init, p);
		System.out.println(p);

		System.out.println("Z=" + Util.substituteVars("${Z}", p));
		System.out.println("Z=" + init.getProperty("Z"));
		System.out.println("W=" + init.getProperty("W"));
	}
	
}