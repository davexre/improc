package com.slavi.eclipse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

// https://sourceforge.net/projects/practicalmacro/files/0.4.9/
public class Macros {

	public void doIt(String[] args) throws Exception {
		String finName = ".metadata/.plugins/org.eclipse.core.runtime/.settings/PracticallyMacro.prefs";
		String fouName = "target/Eclipse macros.xml";
		File xmlFile = new File(fouName);
		File propFile = new File(args[0], finName);
		if (!propFile.isFile()) {
			System.out.println("Practically Macro preferences file not found. " + propFile);
			System.exit(1);
		}
		if (xmlFile.isFile() && xmlFile.lastModified() > propFile.lastModified()) {
			fromXml(xmlFile, propFile);
		} else {
			toXml(xmlFile, propFile);
		}
	}

	public void fromXml(File xmlFile, File propFile) throws Exception {
		String xml;
		try (InputStream is = new FileInputStream(xmlFile)) {
			Document doc = new SAXBuilder(XMLReaders.NONVALIDATING).build(is);
			XMLOutputter xout = new XMLOutputter(Format.getCompactFormat().setLineSeparator("\n"));
			StringWriter writer = new StringWriter();
			xout.output(doc.getRootElement(), writer);
			xml = writer.toString();
		}

		Properties prop = new Properties();
		try (FileInputStream is = new FileInputStream(propFile)) {
			prop.load(is);
		}
		prop.setProperty("Preference_UserMacroDefinitions", xml);
		try (OutputStream out = new FileOutputStream(propFile)) {
//		try (OutputStream out = System.out) {
			prop.store(out, null);
		}
		System.out.println("--- " + propFile.getAbsolutePath());
	}

	public void toXml(File xmlFile, File propFile) throws Exception {
		Properties prop = new Properties();
		try (FileInputStream is = new FileInputStream(propFile)) {
			prop.load(is);
		}
		String xml = prop.getProperty("Preference_UserMacroDefinitions");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringElementContentWhitespace(true);
		Document doc = new SAXBuilder(XMLReaders.NONVALIDATING).build(new ByteArrayInputStream(xml.getBytes()));
		XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
		StringWriter writer = new StringWriter();
		xout.output(doc, writer);
		xml = writer.toString();

		System.out.println(xml);

		System.out.println("--- " + xmlFile.getAbsolutePath());
		try (PrintWriter fou = new PrintWriter(xmlFile)) {
			fou.println(xml);
		}
		xmlFile.setLastModified(propFile.lastModified());
	}


	public static void main(String[] args) throws Exception {
		new Macros().doIt(args);
		//new Macros().doIt(new String[] { "/home/spetrov/.S/workspace" });
		System.out.println("Done.");
	}
}
