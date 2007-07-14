package com.slavi.tree;

import org.jdom.Element;
import org.jdom.JDOMException;

public interface KDNodeSaverXML<E extends KDNode<E>> {

	public void nodeToXML(E node, Element dest);
	
	public E nodeFromXML(Element source) throws JDOMException;
}
