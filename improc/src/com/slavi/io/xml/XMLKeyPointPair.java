package com.slavi.io.xml;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.improc.KeyPointPair;

public class XMLKeyPointPair {

	public static final XMLKeyPointPair instance = new XMLKeyPointPair();
	
	public void toXML(KeyPointPair item, Element dest) {
		Element e;
		
		dest.addContent(XMLHelper.makeAttrEl("dist1", Double.toString(item.distanceToNearest)));
		dest.addContent(XMLHelper.makeAttrEl("dist2", Double.toString(item.distanceToNearest2)));
		
		e = new Element("sourceSP");
		XMLKeyPoint.instance.toXML(item.sourceSP, e);
		dest.addContent(e);

		e = new Element("targetSP");
		XMLKeyPoint.instance.toXML(item.targetSP, e);
		dest.addContent(e);
	}
	
	public static KeyPointPair fromXML(Element source) throws JDOMException {
		KeyPointPair r = new KeyPointPair();
		Element e;

		r.distanceToNearest = Double.parseDouble(XMLHelper.getAttrEl(source, "dist1"));
		r.distanceToNearest2 = Double.parseDouble(XMLHelper.getAttrEl(source, "dist2"));
		
		e = source.getChild("sourceSP");
		r.sourceSP = XMLKeyPoint.instance.fromXML(e);

		e = source.getChild("targetSP");
		r.targetSP = XMLKeyPoint.instance.fromXML(e);
		return r;
	}
}
