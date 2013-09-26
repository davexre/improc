package com.slavi.io.xml;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.math.transform.PolynomialTransformer;
import com.slavi.util.xml.XMLHelper;

public class XMLPolynomialTransformer {

	public static final XMLPolynomialTransformer instance = new XMLPolynomialTransformer();
	
	public void toXML(PolynomialTransformer<?, ?> item, Element dest) {
		dest.addContent(XMLHelper.makeAttrEl("polynomPower", Integer.toString(item.polynomPower)));

		Element e;
		
		e = new Element("SourceOrigin");
		XMLMatrix.instance.toXML(item.sourceOrigin, e);
		dest.addContent(e);
		
		e = new Element("Coefs");
		XMLMatrix.instance.toXML(item.polynomCoefs, e);
		dest.addContent(e);

		e = new Element("Powers");
		XMLMatrix.instance.toXML(item.polynomPowers, e);
		dest.addContent(e);
	}

	public void fromXML(PolynomialTransformer<?, ?> item, Element source) throws JDOMException {
		int inputSize = item.getInputSize();
		int outputSize = item.getOutputSize();
		item.polynomPower = Integer.parseInt(XMLHelper.getAttrEl(source, "polynomPower"));
		item.sourceOrigin = XMLMatrix.instance.fromXML(source.getChild("SourceOrigin"));
		item.polynomCoefs = XMLMatrix.instance.fromXML(source.getChild("polynomCoefs"));
		if (
			(item.sourceOrigin.getSizeX() != inputSize) ||
			(item.sourceOrigin.getSizeY() != 1) ||
			(item.polynomCoefs.getSizeX() != outputSize) ||
			(item.polynomCoefs.getSizeY() != item.getNumberOfCoefsPerCoordinate()) )
			throw new IllegalArgumentException("XML file contains malformed PolynomialTransformer data");
	}
}
