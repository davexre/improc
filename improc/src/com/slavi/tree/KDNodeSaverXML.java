package com.slavi.tree;

import org.jdom.Element;
import org.jdom.JDOMException;

public interface KDNodeSaverXML {

	public void nodeToXML(KDNode node, Element dest);
	
	public KDNode nodeFromXML(Element source) throws JDOMException;
}
