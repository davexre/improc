package com.slavi.io.xml;

import java.util.List;

import org.jdom2.Element;
import org.jdom2.JDOMException;

import com.slavi.math.matrix.SymmetricMatrix;
import com.slavi.util.xml.XMLHelper;

public class XMLSymmetricMatrix {

	public static final XMLSymmetricMatrix instance = new XMLSymmetricMatrix();

	public void toXML(SymmetricMatrix item, Element dest) {
		for (int j = 0; j < item.getSizeY(); j++) {
			Element row = new Element("row");
			for (int i = 0; i <= j; i++)
				row.addContent(XMLHelper.makeAttrEl("item", Double.toString(item.getItem(i, j))));
			dest.addContent(row);
		}
	}

	public static SymmetricMatrix fromXML(Element source) throws JDOMException {
		// Determine matrix size;
		List<?> rows = source.getChildren("row");
		int size = rows.size();
		for (int i = 0; i < rows.size(); i++)
			size = Math.max(size, ((Element)rows.get(i)).getChildren("item").size());
		if ((rows.size() <= 0) || (size <= 0))
			throw new JDOMException("Invalid matrix");

		SymmetricMatrix result = new SymmetricMatrix(size);
		result.make0();
		for (int j = rows.size() - 1; j >= 0; j--) {
			List<?> items = ((Element)rows.get(j)).getChildren("item");
			if (items.size() > j)
				throw new JDOMException("Number of elements at row " + j +
						" exceeds the maximum allowed (" + j + ")");
			for (int i = items.size() - 1; i >= 0; i--) {
				Element item = (Element)items.get(i);
				String v = item.getAttributeValue("v");
				if ((v != null) && (!v.equals("")))
					try {
						result.setItem(i, j, Double.parseDouble(v));
					} catch (Exception e) {
						e.printStackTrace();
						throw new JDOMException("Invalid matrix value ar row " + j + ", column " + i);
					}
			}
		}
		return result;
	}
}
