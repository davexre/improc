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

/**
 * This class contains utility methods for reading/writing XML files using the org.jdom library.
 * The org.jdom library is available at <a href="www.jdom.org" />.  
 */
public class XMLHelper {

	/**
	 * Stores the XML document into a file. 
	 * <p>
	 * Adds an "xml-stylesheet" tag is a stylesheet is specified.
	 */
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

	/**
	 * Reads the specified XML file and returs the root element 
	 */
	public static Element readXML(File fin) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder(false);
		Document doc = builder.build(fin);
		return doc.getRootElement();		
	}
	
	/**
	 * Creates an org.jdom.Element with "name" and assigned text value "text".
	 * <p>
	 * Example:
	 * <p>
	 * XMLHelper.makeEl("elementName", "Element value");
	 * <p>
	 * will create the following XML
	 * <p>
	 * &lt;elementName&gt;Element value&lt;/elementName&gt;
	 */
	public static Element makeEl(String name, String text) {
		Element e = new Element(name);
		e.setText(text);
		return e;
	}

	/**
	 * Retrieves the text of the specified element name, located as child 
	 * element of the source XML element.
	 * <p>Example:
	 * <pre>
	 * &lt;sourceElement&gt;
	 *     &lt;elementName&gt;Element value&lt;/elementName&gt;
	 * &lt;/sourceElement&gt;
	 * @param source	the source element where the element with "name" should be located.
	 * @return			returns the "Element value" or null if the element can not be located.
	 * @see #makeEl(String, String)
	 */
	public static String getEl(Element source, String name) {
		if (source == null)
			return null;
		Element e = source.getChild(name);
		if (e == null)
			return null;
		return e.getTextTrim();
	}
	
	/**
	 * Retrieves the text of the specified element name, located as child 
	 * element of the source XML element.
	 * <p>Example:
	 * <pre>
	 * &lt;sourceElement&gt;
	 *     &lt;elementName&gt;Element value&lt;/elementName&gt;
	 * &lt;/sourceElement&gt;
	 * @param source	the source element where the element with "name" should be located.
	 * @return			returns the "Element value" or defaultValue if the element 
	 * 					can not be located.
	 * @see #makeEl(String, String)
	 */
	public static String getEl(Element source, String name, String defaultValue) {
		if (source == null)
			return defaultValue;
		Element e = source.getChild(name);
		if (e == null)
			return null;
		String result = e.getTextTrim();
		if ((result == null) || (result.equals("")))
			result = defaultValue;				
		return result;
	}
	
	/**
	 * Creates an empty org.jdom.Element with "name" and 
	 * attribute named "v" with value "text".
	 * <p>
	 * Example:
	 * <p>
	 * XMLHelper.makeAttrEl("elementName", "Attribute value");
	 * <p>
	 * will create the following XML
	 * <p>
	 * &lt;elementName v="Attribute value" /&gt;
	 */
	public static Element makeAttrEl(String name, String attributeValue) {
		Element e = new Element(name);
		e.setAttribute("v", attributeValue);
		return e;
	}
	
	/**
	 * Retrieves the value of an attibute named "v" belonging to an XML 
	 * element named "name", that is a child element to the source element. 
	 * <p>Example:
	 * <pre>
	 * &lt;sourceElement&gt;
	 *     &lt;elementName v="Attribute value" /&gt;
	 * &lt;/sourceElement&gt;
	 * @param source	the source element where the element with "name" should be located.
	 * @return			returns the "Attribute value" or null if the element or 
	 * 					the attribute "v" can not be located.
	 * @see #makeAttrEl(String, String)
	 */
	public static String getAttrEl(Element source, String name) {
		if (source == null)
			return null;
		Element e = source.getChild(name);
		if (e == null)
			return null;
		return e.getAttributeValue("v");
	}
	
	/**
	 * Retrieves the value of an attibute named "v" belonging to an XML 
	 * element named "name", that is a child element to the source element. 
	 * <p>Example:
	 * <pre>
	 * &lt;sourceElement&gt;
	 *     &lt;elementName v="Attribute value" /&gt;
	 * &lt;/sourceElement&gt;
	 * @param source	the source element where the element with "name" should be located.
	 * @return			returns the "Attribute value" or defaultValue if the element or 
	 * 					the attribute "v" can not be located.
	 * @see #makeAttrEl(String, String)
	 */
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
}
