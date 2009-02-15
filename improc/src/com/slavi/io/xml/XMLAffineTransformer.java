package com.slavi.io.xml;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.math.transform.AffineTransformer;

public class XMLAffineTransformer {
	
	public static final XMLAffineTransformer instance = new XMLAffineTransformer();
	
	public void toXML(AffineTransformer<?, ?> item, Element dest) {
		Element e;
		
		e = new Element("Origin");
		XMLMatrix.instance.toXML(item.origin, e);
		dest.addContent(e);

		e = new Element("Coefs");
		XMLMatrix.instance.toXML(item.affineCoefs, e);
		dest.addContent(e);
	}
	
	public void fromXML(AffineTransformer<?, ?> item, Element source) throws JDOMException {
		int inputSize = item.getInputSize();
		int outputSize = item.getOutputSize();
		Element e;

		e = source.getChild("Origin");
		item.origin = XMLMatrix.instance.fromXML(e);

		e = source.getChild("Coefs");
		item.affineCoefs = XMLMatrix.instance.fromXML(e);
		
		if (
			(item.affineCoefs.getSizeX() != inputSize) ||
			(item.affineCoefs.getSizeY() != outputSize) ||
			(item.origin.getSizeX() != outputSize) ||
			(item.origin.getSizeY() != 1))
			throw new IllegalArgumentException("Invalid data for affine transformer");
	}
}
