package com.slavi.io.xml;

import org.jdom2.Element;

import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.util.xml.XMLHelper;

public class XMLLeastSquaresAdjust {

	public static final XMLLeastSquaresAdjust instance = new XMLLeastSquaresAdjust();
	
	public void toXML(LeastSquaresAdjust item, Element dest) {
		dest.addContent(XMLHelper.makeAttrEl("RequiredPoints", Integer.toString(item.getRequiredPoints())));
		dest.addContent(XMLHelper.makeAttrEl("RequiredMeasurements", Integer.toString(item.getRequiredMeasurements())));
		dest.addContent(XMLHelper.makeAttrEl("MeasurementCount", Integer.toString(item.getMeasurementCount())));
		dest.addContent(XMLHelper.makeAttrEl("SumPLL", Double.toString(item.getSumPLL())));
		dest.addContent(XMLHelper.makeAttrEl("SumP", Double.toString(item.getSumP())));
		dest.addContent(XMLHelper.makeAttrEl("SumLL", Double.toString(item.getSumLL())));
		dest.addContent(XMLHelper.makeAttrEl("MedianSquareError", Double.toString(item.getMedianSquareError())));
		Element e;

		e = new Element("NormalMatrix");
		XMLSymmetricMatrix.instance.toXML(item.getNm(), e);
		dest.addContent(e);

		e = new Element("APL");
		XMLMatrix.instance.toXML(item.getApl(), e);
		dest.addContent(e);

		e = new Element("Unknown");
		XMLMatrix.instance.toXML(item.getUnknown(), e);
		dest.addContent(e);
	}
}
