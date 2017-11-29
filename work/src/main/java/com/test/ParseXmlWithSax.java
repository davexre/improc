package com.test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPath;

public class ParseXmlWithSax {
	public static void main(String[] args) throws JDOMException, IOException {
		Document doc = new SAXBuilder(false).build(new StringReader(new String(
				"<users>   " + "<user id='13423'>"
						+ "<firstname>Andre</firstname>" + "</user>"
						+ "<user id='32424'>" + "<firstname>Peter</firstname>"
						+ "</user> " + "<user id='543534'>"
						+ "<firstname>Sandra</firstname>" + "</user>"
						+ "</users>")));

		// Build the xpath expression
		//XPath xpathExpression = XPath.newInstance("//*[@id=32424]");
		XPath xpathExpression = XPath.newInstance("//user");
		

		// Apply xpath and fetch all matching nodes
		ArrayList<Element> userIds = (ArrayList<Element>) xpathExpression
				.selectNodes(doc);

		// Iterate over naodes and print the value
		for (int i = 0; i < userIds.size(); i++) {
			System.out.println((userIds.get(i)).getAttributeValue("id").trim());
		}
	}

}
