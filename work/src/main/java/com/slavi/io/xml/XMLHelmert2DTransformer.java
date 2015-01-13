package com.slavi.io.xml;

import org.jdom2.Element;

import com.slavi.math.transform.Helmert2DTransformer;
import com.slavi.util.xml.XMLHelper;

public class XMLHelmert2DTransformer {

	public static final XMLHelmert2DTransformer instance = new XMLHelmert2DTransformer();
	
	public void toXML(Helmert2DTransformer<?, ?> item, Element dest) {
		dest.addContent(XMLHelper.makeAttrEl("A", Double.toString(item.a)));
		dest.addContent(XMLHelper.makeAttrEl("B", Double.toString(item.b)));
		dest.addContent(XMLHelper.makeAttrEl("C", Double.toString(item.c)));
		dest.addContent(XMLHelper.makeAttrEl("D", Double.toString(item.d)));
	}
	
	public void fromXML(Helmert2DTransformer<?, ?> item, Element source) {
		item.a = Double.parseDouble(XMLHelper.getAttrEl(source, "A"));
		item.b = Double.parseDouble(XMLHelper.getAttrEl(source, "B"));
		item.c = Double.parseDouble(XMLHelper.getAttrEl(source, "C"));
		item.d = Double.parseDouble(XMLHelper.getAttrEl(source, "D"));
	}
}
