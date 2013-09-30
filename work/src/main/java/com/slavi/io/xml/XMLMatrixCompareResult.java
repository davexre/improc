package com.slavi.io.xml;

import org.jdom.Element;

import com.slavi.math.matrix.MatrixCompareResult;
import com.slavi.util.xml.XMLHelper;

public class XMLMatrixCompareResult {
	
	public static final XMLMatrixCompareResult instance = new XMLMatrixCompareResult();

	public void toXML(MatrixCompareResult item, Element dest) {
		dest.addContent(XMLHelper.makeAttrEl("AvgA", Double.toString(item.AvgA)));
		dest.addContent(XMLHelper.makeAttrEl("AvgB", Double.toString(item.AvgB)));
		dest.addContent(XMLHelper.makeAttrEl("SAA", Double.toString(item.SAA)));
		dest.addContent(XMLHelper.makeAttrEl("SBB", Double.toString(item.SBB)));
		dest.addContent(XMLHelper.makeAttrEl("SAB", Double.toString(item.SAB)));
		dest.addContent(XMLHelper.makeAttrEl("PearsonR", Double.toString(item.PearsonR)));
	}
}
