package com.slavi.io.xml;

import java.util.List;

import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.util.tree.KDTree;
import com.slavi.util.tree.TreeNode;

public abstract class XMLKDTree<E> {

	public abstract void nodeToXML(E node, Element dest);
	
	public abstract E nodeFromXML(Element source) throws JDOMException;

	private void toXML_recursive(TreeNode<E> node, Element dest) {
		if (node == null)
			return;
		nodeToXML(node.getData(), dest);
		if (node.getLeft() != null) {
			Element left = XMLHelper.makeAttrEl("Item", "Left");
			toXML_recursive(node.getLeft(), left);
			dest.addContent(left);
		}
		if (node.getRight() != null) {
			Element right = XMLHelper.makeAttrEl("Item", "Right");
			toXML_recursive(node.getRight(), right);
			dest.addContent(right);
		}
	}
	
	/**
	 * Stores the tree in a hierarchical XML form. All the items in the tree are stored as
	 * XML elements called "Item" having one attribute value named "v". The "v" attribute
	 * can have one of the tree possible values, i.e. "Root", "Left" and "Right".
	 * <p> 
	 * All "Item" XML elements can have one sub "Item" sub element with the "v" attribute
	 * set to "Left" and one sub element with "v" attribute set to "Right"
	 * <p>
	 * Note: This method is thread UNSAFE even if called on {@link com.slavi.util.tree.ConcurrentKDTree}
	 * @see #fromXML(Element, KDNodeSaverXML)
	 * @see com.slavi.util.tree.KDTree#balanceIfNeeded();
	 */
	public void toXML(KDTree<E> tree, Element dest) {
		Element rootNode = XMLHelper.makeAttrEl("Item", "Root");
		toXML_recursive(tree.getRoot(), rootNode);
		dest.addContent(rootNode);
	}
	
	private void fromXML_ReadChildren(KDTree<E> tree, Element source) throws JDOMException {
		if (source == null)
			return;
		List<?> children = source.getChildren();
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
	 * @throws JDOMException
	 * @return The number of items read. 
	 */
	public int fromXML(KDTree<E> tree, Element source) throws JDOMException {
		int result = tree.getSize();
		fromXML_ReadChildren(tree, source);
		return tree.getSize() - result;
	}
}
