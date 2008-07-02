package com.slavi.tree;

import java.util.List;

import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.tree.KDTree.Node;
import com.slavi.utils.XMLHelper;

public abstract class KDNodeSaverXML<E> {

	public abstract void nodeToXML(E node, Element dest);
	
	public abstract E nodeFromXML(Element source) throws JDOMException;

	public abstract KDTree<E>getTree(int dimensions);
	
	private void toXML_recursive(Node<E> node, Element dest) {
		if (node == null)
			return;
		nodeToXML(node.data, dest);
		if (node.left != null) {
			Element left = XMLHelper.makeAttrEl("Item", "Left");
			toXML_recursive(node.left, left);
			dest.addContent(left);
		}
		if (node.right != null) {
			Element right = XMLHelper.makeAttrEl("Item", "Right");
			toXML_recursive(node.right, right);
			dest.addContent(right);
		}
	}
	
	/**
	 * Stores the tree in a hierarchical XML form. All the items in the tree are stored as
	 * XML elements called "Item" having one attribute value named "v". The "v" attribute
	 * can have one of the tree possible values, i.e. "Root", "Left" and "Right".
	 * 
	 * All "Item" XML elements can have one sub "Item" sub element with the "v" attribute
	 * set to "Left" and one sub element with "v" attribute set to "Right"
	 * 
	 * Before saving the tree the {@link #balanceIfNeeded()} is called.
	 * 
	 * @see #fromXML(Element, KDNodeSaverXML)
	 */
	public void toXML(KDTree<E> tree, Element dest) {
		if (tree instanceof ConcurrentKDTree) {
			((ConcurrentKDTree)tree).lock.readLock().lock();
		}
		try {
			tree.balanceIfNeeded();
			dest.addContent(XMLHelper.makeAttrEl("Dimensions", Integer.toString(tree.dimensions)));
			Element rootNode = XMLHelper.makeAttrEl("Item", "Root");
			toXML_recursive(tree.root, rootNode);
			dest.addContent(rootNode);
		} finally {
			if (tree instanceof ConcurrentKDTree) {
				((ConcurrentKDTree)tree).lock.readLock().unlock();
			}
		}
	}
	
	private void fromXML_ReadChildren(KDTree<E> tree, Element source) throws JDOMException {
		if (source == null)
			return;
		List children = source.getChildren();
		for (int i = children.size() - 1; i >= 0; i++) {
			Content child_content = (Content) children.get(i);
			if (child_content instanceof Element) {
				Element child = (Element)child_content;
				if (child.getName().equals("Item")) {
					E node = nodeFromXML(child);
					if (node != null)
						tree.add(node);
					fromXML_ReadChildren(tree, child);
				}
			}
		}
	}
	
	/**
	 * Reads the tree from XML (see {@link #toXML(Element, KDNodeSaverXML)}).
	 *  
	 * The tree is read ignoring the "v" attributes of the "Item" elements.
	 * An "Item" element is ALLOWED to have more than two "Item" sub elements.
	 * If the node reader fails in preparing a node it can return null and the node
	 * will ignored.
	 * 
	 * After reading the tree the {@link #balanceIfNeeded()} is called.
	 *   
	 * @throws JDOMException
	 * @return The number of items read. 
	 */
	public KDTree<E> fromXML(KDTree<E> tree, Element source) throws JDOMException {
		int dimensions = Integer.parseInt(XMLHelper.getAttrEl(source, "Dimensions"));
		KDTree<E> result = getTree(dimensions);
		fromXML_ReadChildren(result, source);
		result.balanceIfNeeded();
		return result;
	}
}
