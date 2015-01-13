package com.slavi.io.xml;

import java.util.List;

import org.jdom2.Element;
import org.jdom2.JDOMException;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.util.xml.XMLHelper;

public class XMLKeyPoint {
	
	public static final XMLKeyPoint instance = new XMLKeyPoint();

	public void toXML(KeyPoint item, Element dest) {
		dest.addContent(XMLHelper.makeAttrEl("imgX", Integer.toString(item.imgX)));
		dest.addContent(XMLHelper.makeAttrEl("imgY", Integer.toString(item.imgY)));
		dest.addContent(XMLHelper.makeAttrEl("doubleX", Double.toString(item.getDoubleX())));
		dest.addContent(XMLHelper.makeAttrEl("doubleY", Double.toString(item.getDoubleY())));
		dest.addContent(XMLHelper.makeAttrEl("dogLevel", Double.toString(item.dogLevel)));
		dest.addContent(XMLHelper.makeAttrEl("adjS", Double.toString(item.adjS)));
		dest.addContent(XMLHelper.makeAttrEl("kpScale", Double.toString(item.kpScale)));
		dest.addContent(XMLHelper.makeAttrEl("degree", Double.toString(item.degree)));
		for (int k = 0; k < KeyPoint.numDirections; k++)
			for (int j = 0; j < KeyPoint.descriptorSize; j++)
				for (int i = 0; i < KeyPoint.descriptorSize; i++)
					dest.addContent(XMLHelper.makeEl("f", Integer.toString(item.getItem(i, j, k))));
	}

	public KeyPoint fromXML(KeyPointList keyPointList, Element source) throws JDOMException {
		double doubleX = Double.parseDouble(XMLHelper.getAttrEl(source, "doubleX"));
		double doubleY = Double.parseDouble(XMLHelper.getAttrEl(source, "doubleY"));
		KeyPoint r = new KeyPoint(keyPointList, doubleX, doubleY);
		r.imgX = Integer.parseInt(XMLHelper.getAttrEl(source, "imgX"));
		r.imgY = Integer.parseInt(XMLHelper.getAttrEl(source, "imgY"));
		r.dogLevel = Integer.parseInt(XMLHelper.getAttrEl(source, "dogLevel"));
		r.adjS = Double.parseDouble(XMLHelper.getAttrEl(source, "adjS"));
		r.kpScale = Double.parseDouble(XMLHelper.getAttrEl(source, "kpScale"));
		r.degree = Double.parseDouble(XMLHelper.getAttrEl(source, "degree"));
		List<?> fList = source.getChildren("f");
		if (fList.size() != KeyPoint.descriptorSize * KeyPoint.descriptorSize * KeyPoint.numDirections)
			throw new JDOMException("Number of feature elements goes not match.");
		int count = 0;
		for (int k = 0; k < KeyPoint.numDirections; k++)
			for (int j = 0; j < KeyPoint.descriptorSize; j++)				
				for (int i = 0; i < KeyPoint.descriptorSize; i++) {
					int tmp = Integer.parseInt(((Element)fList.get(count++)).getTextTrim());
					if (tmp > Byte.MAX_VALUE)
						tmp = Byte.MAX_VALUE;
					if (tmp < Byte.MIN_VALUE)
						tmp = Byte.MIN_VALUE;
					r.setItem(i, j, k, (byte)tmp);
				}
		return r;
	}
}
