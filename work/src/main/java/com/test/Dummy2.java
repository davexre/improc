package com.test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

public class Dummy2 {
	public static void main2(String[] args) throws JDOMException, IOException {
		Document doc = new SAXBuilder(false).build(new StringReader(new String(
				"<users>   " + "<user id='13423'>"
						+ "<firstname>Andre</firstname>" + "</user>"
						+ "<user id='32424'>" + "<firstname>Peter</firstname>"
						+ "</user> " + "<user id='543534'>"
						+ "<firstname>Sandra</firstname>" + "</user>"
						+ "</users>")));

		// Build the xpath expression
		XPath xpathExpression = XPath.newInstance("//*[@id=32424]");
		

		// Apply xpath and fetch all matching nodes
		ArrayList<Element> userIds = (ArrayList<Element>) xpathExpression
				.selectNodes(doc);

		// Iterate over naodes and print the value
		for (int i = 0; i < userIds.size(); i++) {
			System.out.println((userIds.get(i)).getAttributeValue("id").trim());
		}
	}

}
