package com.slavi.io.props;

import java.util.Properties;

public class XmlProperties {

	public void doIt(String[] args) throws Exception {
		Properties p = new Properties();
		p.loadFromXML(getClass().getResourceAsStream("XmlProperties.xml"));
		p.storeToXML(System.out, "My comment");
	}

	public void doIt1(String[] args) throws Exception {
		Properties p = new Properties();
		for (int i = 0; i < 10; i++)
			p.setProperty("MyProp" + i, "Value " + i);
		p.storeToXML(System.out, "My comment");
	}

	public static void main(String[] args) throws Exception {
		new XmlProperties().doIt(args);
		System.out.println("Done.");
	}
}
