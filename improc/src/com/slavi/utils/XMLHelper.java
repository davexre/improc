package com.slavi.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.ProcessingInstruction;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class XMLHelper {

	public static void writeXML(File fou, Element documentRoot, String styleSheet) throws FileNotFoundException, IOException {
		Document doc = new Document();
		if ((styleSheet != null) || (!styleSheet.equals(""))) {
			Content c = new ProcessingInstruction("xml-stylesheet", "href=\"" + styleSheet + "\" type=\"text/xsl\"");
			doc.addContent(c);
		}
		doc.setRootElement(documentRoot);
		XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
		xout.output(doc, new FileOutputStream(fou));
	}

	public static Element readXML(File fin) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder(false);
		Document doc = builder.build(fin);
		return doc.getRootElement();		
	}
	
	public static Element makeEl(String name, String attributeValue) {
		Element e = new Element(name);
		e.setText(attributeValue);
		return e;
	}
	
	public static String getEl(Element source) {
		if (source == null)
			return null;
		return source.getTextTrim();
	}
	
	public static String getEl(Element source, String defaultValue) {
		if (source == null)
			return defaultValue;
		String r = source.getTextTrim();
		if ((r == null) || (r.equals("")))
			r = defaultValue;				
		return r;
	}
	
	public static Element makeAttrEl(String name, String attributeValue) {
		Element e = new Element(name);
		e.setAttribute("v", attributeValue);
		return e;
	}
	
	public static String getAttrEl(Element source, String name) {
		if (source == null)
			return null;
		Element e = source.getChild(name);
		if (e == null)
			return null;
		return e.getAttributeValue("v");
	}
	
	public static String getAttrEl(Element source, String name, String defaultValue) {
		if (source == null)
			return defaultValue;
		Element e = source.getChild(name);
		if (e == null)
			return defaultValue;
		String r = e.getAttributeValue("v");
		if ((r == null) || (r.equals("")))
			r = defaultValue;				
		return r;
	}
	
	public static String chageFileExtension(String fileName, String newExtension) {
		int lastIndex = fileName.lastIndexOf(".");
		if (lastIndex < 0)
			return fileName + "." + newExtension;
		return fileName.substring(0, lastIndex) + "." + newExtension; 
	}
}
