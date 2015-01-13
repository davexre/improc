package com.slavi.io.xml;

import org.jdom2.Element;

import com.slavi.math.adjust.Statistics;
import com.slavi.util.xml.XMLHelper;

public class XMLStatistics {

	public static final XMLStatistics instance = new XMLStatistics();
    
	public void toXML(Statistics item, Element dest) {
		Element conclusion = new Element("conclusion");
		conclusion.setText(item.hasBadValues() ? "*** There is/are BAD value(s)" : "All values are ok");
		dest.addContent(conclusion);
		dest.addContent(XMLHelper.makeAttrEl("Average", Double.toString(item.getAvgValue())));
		dest.addContent(XMLHelper.makeAttrEl("NumberOfItems", Integer.toString(item.getItemsCount())));
		dest.addContent(XMLHelper.makeAttrEl("B", Double.toString(item.getB())));
		dest.addContent(XMLHelper.makeAttrEl("J_Start", Double.toString(item.getJ_Start())));
		dest.addContent(XMLHelper.makeAttrEl("J_End", Double.toString(item.getJ_End())));
		dest.addContent(XMLHelper.makeAttrEl("A", Double.toString(item.getA())));
		dest.addContent(XMLHelper.makeAttrEl("E", Double.toString(item.getE())));
		dest.addContent(XMLHelper.makeAttrEl("MinX", Double.toString(item.getMinX())));
		dest.addContent(XMLHelper.makeAttrEl("MaxX", Double.toString(item.getMaxX())));
		dest.addContent(XMLHelper.makeAttrEl("MinAbsX", Double.toString(item.getAbsMinX())));
		dest.addContent(XMLHelper.makeAttrEl("MaxAbsX", Double.toString(item.getAbsMaxX())));
		dest.addContent(XMLHelper.makeAttrEl("Delta", Double.toString(item.getMaxX() - item.getMinX())));
		Element m = new Element("M");
    	for (int i = 2; i <= 4; i++)
    		m.addContent(XMLHelper.makeAttrEl("M" + i, Double.toString(item.getM(i))));
    	dest.addContent(m);
		Element d = new Element("D");
    	for (int i = 2; i <= 4; i++)
    		d.addContent(XMLHelper.makeAttrEl("D" + i, Double.toString(item.getD(i))));
    	dest.addContent(d);
	}
}
