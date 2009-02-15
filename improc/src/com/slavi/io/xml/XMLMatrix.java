package com.slavi.io.xml;

import java.util.List;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.math.matrix.Matrix;

public class XMLMatrix {

	public static final XMLMatrix instance = new XMLMatrix();
	
	public void toXML(Matrix item, Element dest) {
		for (int j = 0; j < item.getSizeY(); j++) {
			Element row = new Element("row");
			for (int i = 0; i < item.getSizeX(); i++)
				row.addContent(XMLHelper.makeAttrEl("item", Double.toString(item.getItem(i, j))));
			dest.addContent(row);
		}
	}
	
	public Matrix fromXML(Element source) throws JDOMException {
		// Determine matrix size;
		List<?> rows = source.getChildren("row");
		int cols = 0;
		for (int i = 0; i < rows.size(); i++)
			cols = Math.max(cols, ((Element)rows.get(i)).getChildren("item").size());
		if ((rows.size() <= 0) || (cols <= 0))
			throw new JDOMException("Invalid matrix");
			
		Matrix result = new Matrix(cols, rows.size());
		result.make0();
		for (int j = rows.size() - 1; j >= 0; j--) {
			List<?> items = ((Element)rows.get(j)).getChildren("item");
			for (int i = items.size() - 1; i >= 0; i--) {
				Element item = (Element)items.get(i);
				String v = item.getAttributeValue("v");
				if ((v != null) && (!v.equals("")))
					try {
						result.setItem(i, j, Double.parseDouble(v));
					} catch (Exception e) {
						throw new JDOMException("Invalid matrix value ar row " + j + ", column " + i);
					}
			}
		}
		return result;
	}
}
