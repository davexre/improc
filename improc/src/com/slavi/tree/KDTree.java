package com.slavi.tree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.utils.XMLHelper;

public class KDTree<E extends KDNode<E>> implements Iterable<E>{
	protected int dimensions;

	protected E root;
	
	private double[][] hyperRectangle; // used by getNearestNeighbourhood()

	private int m_size;
	
	private int treeDepth;
	
	private KDTree() {
		m_size = 0;
		treeDepth = 0;
	};
	
	public KDTree(int dimensions) {
		this.dimensions = dimensions;
		this.m_size = 0;
		this.treeDepth = 0;
		this.hyperRectangle = new double[dimensions][2];
		this.root = null;
	}

	/**
	 * Removes all elements in the tree.
	 */
	public void clear() {
		m_size = 0;
		treeDepth = 0;
		root = null;
	}

	private void addToList_recursive(ArrayList<E>list, E node) {
		if (node == null)
			return;
		list.add(node);
		addToList_recursive(list, node.getLeft()); 
		addToList_recursive(list, node.getRight()); 
	}

	/**
	 * Returns a list of all items in the tree. The order of the elements 
	 * in the list is undefined. 
	 */
	public ArrayList<E>toList() {
		ArrayList<E>list = new ArrayList<E>();
		addToList_recursive(list, root); 
		return list;
	}

	/**
	 * Balances the tree. The prefered way to do this is 
	 * to use {@link #balanceIfNeeded()}
	 */
	public void balance() {
		ArrayList<E>list = toList();
		this.treeDepth = 0;
		this.root = balanceSegment(list, dimensions, 0, list.size() - 1, 0, 0);
	}

	/**
	 * Balances the tree only if needed.
	 * 
	 * @see #isBalanceNeeded()
	 */
	public void balanceIfNeeded() {
		if (isBalanceNeeded())
			balance();
	}

	/**
	 * Returns true - balance is needed if the tree depth 
	 * {@link #getTreeDepth()} > perfectDepth + maxDepthDeviation.
	 * 
	 * where
	 * 
	 * maxDepthDeviation = {@link #size()} * 0.05 (5%)
	 * and also 3 <= maxDepthDeviation <= 7 
	 */
	public boolean isBalanceNeeded() {
		final double log2 = Math.log(2.0);
		int curSize = size();
		int perfectDepth = (int)(Math.ceil(Math.log(curSize) / log2));
		int maxDepthDeviation = (int)(curSize * 0.05);
		if (maxDepthDeviation < 3)
			maxDepthDeviation = 3;
		if (maxDepthDeviation > 7)
			maxDepthDeviation = 7;
		return perfectDepth + maxDepthDeviation < getTreeDepth();
	}

	/**
	 * The numberOfUnsuccessfullSorts is used to avoid endles recurssion if the list of items contains
	 * more than [dimensions] number of elements with equal values for ALL dimensions.
	 * 
	 * If the items list contains more than one element with equal value in a speciffic dimension, the
	 * list is recursively sorted using the next dimension.  
	 */
	private static int deepSort(ArrayList items, int dimensions, int left, int right, int curDimension, int numberOfUnsuccessfullSorts) {
		if (left > right)
			return -1;
		int midIndex = (left + right) >> 1;

		int segmentEndIndex = right;
		double segmentEndValue = ((KDNode)items.get(segmentEndIndex)).getValue(curDimension);
		
		int segmentStartIndex = right;
		for (; segmentStartIndex >= left; segmentStartIndex--) {
			int minIndex = segmentStartIndex;
			double segmentStartValue = ((KDNode)items.get(segmentStartIndex)).getValue(curDimension);
			
			for (int j = segmentStartIndex - 1; j >= left; j--) {
				double tmp = ((KDNode)items.get(j)).getValue(curDimension);
				if (tmp >= segmentStartValue) {
					segmentStartValue = tmp;
					minIndex = j;
				}
			}
			if (minIndex != segmentStartIndex) {
				Object tmp = items.get(minIndex);
				items.set(minIndex, items.get(segmentStartIndex));
				items.set(segmentStartIndex, tmp);
			}
			if ((segmentStartIndex < midIndex) && (segmentStartValue < segmentEndValue)) {
				break;
			}
			if (segmentStartValue != segmentEndValue) {
				segmentEndValue = segmentStartValue;
				segmentEndIndex = segmentStartIndex;
			}
		}
		segmentStartIndex++;
		
		if (segmentStartIndex == segmentEndIndex)
			return segmentStartIndex;
		if ((left == segmentStartIndex) && (right == segmentEndIndex)) {
			numberOfUnsuccessfullSorts++;
			if (numberOfUnsuccessfullSorts >= dimensions)
				return midIndex;
		} else
			numberOfUnsuccessfullSorts = 0;
		int nextDimension = (curDimension + 1) % dimensions;
		return deepSort(items, dimensions, segmentStartIndex, segmentEndIndex, nextDimension, numberOfUnsuccessfullSorts);
	}
	
	private E balanceSegment(ArrayList<E> items, int dimensions, int left, int right, int curDimension, int depthLevel) {
		if (left > right)
			return null;
		int midIndex = deepSort(items, dimensions, left, right, curDimension, 0);
		double midValue = items.get(midIndex).getValue(curDimension);
		int startIndex = midIndex - 1;
		for (; startIndex >= left; startIndex--) 
			if (items.get(startIndex).getValue(curDimension) != midValue) 
				break;
		startIndex++;
		if (startIndex != midIndex) {
			E tmp = items.get(midIndex);
			items.set(midIndex, items.get(startIndex));
			items.set(startIndex, tmp);
		}
		int nextDimension = (curDimension + 1) % dimensions;
		E result = items.get(startIndex);
		depthLevel++;
		if (depthLevel > treeDepth)
			treeDepth = depthLevel;
		result.setLeft(balanceSegment(items, dimensions, left, startIndex - 1, nextDimension, depthLevel));
		result.setRight(balanceSegment(items, dimensions, startIndex + 1, right, nextDimension, depthLevel));
		return result;
	}
	
	private double getDistanceSquared(E n1, E n2) {
		double result = 0;
		for (int i = dimensions - 1; i >= 0; i--) {
			double d = n1.getValue(i) - n2.getValue(i);
			result += d * d;
		}
		return result;
	}

	private double getDistanceSquaredToHR(double[][] hr, E target) {
		double result = 0;
		for (int i = dimensions - 1; i >= 0; i--) {
			double d = target.getValue(i);
			if (d < hr[i][0]) {
				d -= hr[i][0];
			} else if (d > hr[i][1]) {
				d -= hr[i][1];
			} else {
				d = 0;
			}
			result += d * d;
		}
		return result;
	}

	private void nearestSegment(NearestNeighbours nearest,
			double[][] hr, E target, E curNode, int dimension) {
		if (curNode == null) 
			return;
		usedSearchSteps++;

		if (target.canFindDistanceToPoint(curNode))
			nearest.add(curNode, getDistanceSquared(target, curNode));
		double curNodeValue = curNode.getValue(dimension);
		int nextDimension = (dimension + 1) % dimensions;

		if (target.getValue(dimension) < curNodeValue) {
			// Prepare and check the "nearer" hyper rectangle
			double tmp = hr[dimension][1];
			hr[dimension][1] = curNodeValue;
			nearestSegment(nearest, hr, target, curNode.getLeft(), nextDimension);
			hr[dimension][1] = tmp;
			// Prepare the "further" hyper rectangle
			tmp = hr[dimension][0];
			hr[dimension][0] = curNodeValue;
			// Check the "further" hyper rectangle:
			//   if capacity is not reached OR
			//   if distance from target to "further" hyper rectangle is smaller 
			//      than the maximum of the currently found neighbours
			if ((nearest.size() < nearest.getCapacity()) ||
					(getDistanceSquaredToHR(hr, target) < nearest.getDistanceToTarget(nearest.size() - 1))) 
				nearestSegment(nearest, hr, target, curNode.getRight(), nextDimension);
			// Restore the hyper rectangle
			hr[dimension][0] = tmp;
		} else {
			// Prepare and check the "nearer" hyper rectangle
			double tmp = hr[dimension][0];
			hr[dimension][0] = curNodeValue;			
			nearestSegment(nearest, hr, target, curNode.getRight(), nextDimension);
			hr[dimension][0] = tmp;
			// Prepare the "further" hyper rectangle
			tmp = hr[dimension][1];
			hr[dimension][1] = curNodeValue;
			// Check the "further" hyper rectangle:
			//   if capacity is not reached OR
			//   if distance from target to "further" hyper rectangle is smaller 
			//      than the maximum of the currently found neighbours
			if ((nearest.size() < nearest.getCapacity()) ||
					(getDistanceSquaredToHR(hr, target) < nearest.getDistanceToTarget(nearest.size() - 1))) 
				nearestSegment(nearest, hr, target, curNode.getLeft(), nextDimension);
			// Restore the hyper rectangle
			hr[dimension][1] = tmp;
		}
	}
	
	/**
	 * Returns [maxNeighbours] of elements closest to the [target] element.
	 * The list is sorted using the distance from the element to the target element.
	 * 
	 * @see #nearestSegmentBBF(NearestNeighbours, double[][], KDNode, KDNode, int)
	 */
	public NearestNeighbours getNearestNeighbours(E target, int maxNeighbours) {
		NearestNeighbours result = new NearestNeighbours(target, maxNeighbours);
		for (int i = dimensions - 1; i >= 0; i--) {
			hyperRectangle[i][0] = Double.MIN_VALUE;
			hyperRectangle[i][1] = Double.MAX_VALUE;
		}
		usedSearchSteps = 0;
		nearestSegment(result, hyperRectangle, target, root, 0);
		if (usedSearchSteps > maxUsedSearchSteps)
			maxUsedSearchSteps = usedSearchSteps;
		return result;
	}

	public int searchSteps;
	public int usedSearchSteps;
	public int maxUsedSearchSteps = 0;

	private void nearestSegmentBBF(NearestNeighbours nearest,
			double[][] hr, E target, E curNode, int dimension) {
		if (curNode == null) 
			return;
		usedSearchSteps++;

		if (target.canFindDistanceToPoint(curNode))
			nearest.add(curNode, getDistanceSquared(target, curNode));
		double curNodeValue = curNode.getValue(dimension);
		int nextDimension = (dimension + 1) % dimensions;

		if (target.getValue(dimension) < curNodeValue) {
			// Prepare and check the "nearer" hyper rectangle
			double tmp = hr[dimension][1];
			hr[dimension][1] = curNodeValue;
			nearestSegmentBBF(nearest, hr, target, curNode.getLeft(), nextDimension);
			hr[dimension][1] = tmp;

			if (searchSteps > 0) {
				searchSteps--;
				// Prepare the "further" hyper rectangle
				tmp = hr[dimension][0];
				hr[dimension][0] = curNodeValue;
				// Check the "further" hyper rectangle:
				//   if capacity is not reached OR
				//   if distance from target to "further" hyper rectangle is smaller 
				//      than the maximum of the currently found neighbours
				if ((nearest.size() < nearest.getCapacity()) ||
						(getDistanceSquaredToHR(hr, target) < nearest.getDistanceToTarget(nearest.size() - 1))) 
					nearestSegmentBBF(nearest, hr, target, curNode.getRight(), nextDimension);
				// Restore the hyper rectangle
				hr[dimension][0] = tmp;
			}
		} else {
			// Prepare and check the "nearer" hyper rectangle
			double tmp = hr[dimension][0];
			hr[dimension][0] = curNodeValue;			
			nearestSegmentBBF(nearest, hr, target, curNode.getRight(), nextDimension);
			hr[dimension][0] = tmp;

			if (searchSteps > 0) {
				searchSteps--;
				// Prepare the "further" hyper rectangle
				tmp = hr[dimension][1];
				hr[dimension][1] = curNodeValue;
				// Check the "further" hyper rectangle:
				//   if capacity is not reached OR
				//   if distance from target to "further" hyper rectangle is smaller 
				//      than the maximum of the currently found neighbours
				if ((nearest.size() < nearest.getCapacity()) ||
						(getDistanceSquaredToHR(hr, target) < nearest.getDistanceToTarget(nearest.size() - 1))) 
					nearestSegmentBBF(nearest, hr, target, curNode.getLeft(), nextDimension);
				// Restore the hyper rectangle
				hr[dimension][1] = tmp;
			}
		}
	}

	/**
	 * Uses an algorithm for Approximate Nearest Neighbourhood search, called Best Bin First.
	 * The algorithm is developed by David G. Lowe and described in his paper
	 * "Distinctive Image Features from Scale-Invariant Keypoints" 
	 * 
	 * Returns [maxNeighbours] of elements closest to the [target] element using maxSearchSteps.
	 * The list is sorted using the distance from the element to the target element.
	 *
	 * @see #nearestSegment(NearestNeighbours, double[][], KDNode, KDNode, int)
	 */
	public NearestNeighbours getNearestNeighboursBBF(E target, int maxNeighbours, int maxSearchSteps) {
		NearestNeighbours result = new NearestNeighbours(target, maxNeighbours);
		for (int i = dimensions - 1; i >= 0; i--) {
			hyperRectangle[i][0] = Double.MIN_VALUE;
			hyperRectangle[i][1] = Double.MAX_VALUE;
		}
		usedSearchSteps = 0;
		this.searchSteps = maxSearchSteps; 
		nearestSegmentBBF(result, hyperRectangle, target, root, 0);
		if (usedSearchSteps > maxUsedSearchSteps)
			maxUsedSearchSteps = usedSearchSteps;
		return result;
	}

	private void nearestSegmentBBFOriginal(NearestNeighbours nearest,
			double[][] hr, E target, E curNode, int dimension) {
		if (curNode == null) 
			return;
		usedSearchSteps++;

		if (target.canFindDistanceToPoint(curNode))
			nearest.add(curNode, getDistanceSquared(target, curNode));
		double curNodeValue = curNode.getValue(dimension);
		int nextDimension = (dimension + 1) % dimensions;

		if (target.getValue(dimension) < curNodeValue) {
			// Prepare and check the "nearer" hyper rectangle
			double tmp = hr[dimension][1];
			hr[dimension][1] = curNodeValue;
			nearestSegmentBBFOriginal(nearest, hr, target, curNode.getLeft(), nextDimension);
			hr[dimension][1] = tmp;

			if (searchSteps > 0) {
				searchSteps--;
				// Prepare the "further" hyper rectangle
				tmp = hr[dimension][0];
				hr[dimension][0] = curNodeValue;
				// Check the "further" hyper rectangle:
				//   if capacity is not reached OR
				//   if distance from target to "further" hyper rectangle is smaller 
				//      than the maximum of the currently found neighbours
				if ((nearest.size() < nearest.getCapacity()) ||
						(getDistanceSquaredToHR(hr, target) < nearest.getDistanceToTarget(nearest.size() - 1))) 
					nearestSegmentBBFOriginal(nearest, hr, target, curNode.getRight(), nextDimension);
				// Restore the hyper rectangle
				hr[dimension][0] = tmp;
			}
		} else {
			// Prepare and check the "nearer" hyper rectangle
			double tmp = hr[dimension][0];
			hr[dimension][0] = curNodeValue;			
			nearestSegmentBBFOriginal(nearest, hr, target, curNode.getRight(), nextDimension);
			hr[dimension][0] = tmp;

			if (searchSteps > 0) {
				searchSteps--;
				// Prepare the "further" hyper rectangle
				tmp = hr[dimension][1];
				hr[dimension][1] = curNodeValue;
				// Check the "further" hyper rectangle:
				//   if capacity is not reached OR
				//   if distance from target to "further" hyper rectangle is smaller 
				//      than the maximum of the currently found neighbours
				if ((nearest.size() < nearest.getCapacity()) ||
						(getDistanceSquaredToHR(hr, target) < nearest.getDistanceToTarget(nearest.size() - 1))) 
					nearestSegmentBBFOriginal(nearest, hr, target, curNode.getLeft(), nextDimension);
				// Restore the hyper rectangle
				hr[dimension][1] = tmp;
			}
		}
	}
	
	public NearestNeighbours getNearestNeighboursBBFOriginal(E target, int maxNeighbours, int maxSearchSteps) {
		NearestNeighbours result = new NearestNeighbours(target, maxNeighbours);
		for (int i = dimensions - 1; i >= 0; i--) {
			hyperRectangle[i][0] = Double.MIN_VALUE;
			hyperRectangle[i][1] = Double.MAX_VALUE;
		}
		usedSearchSteps = 0;
		this.searchSteps = maxSearchSteps; 
		nearestSegmentBBFOriginal(result, hyperRectangle, target, root, 0);
		if (usedSearchSteps > maxUsedSearchSteps)
			maxUsedSearchSteps = usedSearchSteps;
		return result;
	}
	
	private void toXML_recursive(E node, Element dest, KDNodeSaverXML<E> saver) {
		if (node == null)
			return;
		saver.nodeToXML(node, dest);
		if (node.getLeft() != null) {
			Element left = XMLHelper.makeAttrEl("Item", "Left");
			toXML_recursive(node.getLeft(), left, saver);
			dest.addContent(left);
		}
		if (node.getRight() != null) {
			Element right = XMLHelper.makeAttrEl("Item", "Right");
			toXML_recursive(node.getRight(), right, saver);
			dest.addContent(right);
		}
	}
	
	/**
	 * Stores the tree in a hierarchical XML form. All the items in the tree are stored as
	 * XML elements called "Item" having one attribute value named "v". The "v" attribute
	 * can have one of the tree possible values, i.e. "Root", "Left" and "Right".
	 * 
	 * All "Item" XML elements can have one sub "Item" sub element with the "v" attribute
	 * set to "Left" and one sub element with "v" attibute set to "Right"
	 * 
	 * Before saving the tree the {@link #balanceIfNeeded()} is called.
	 * 
	 * @see #fromXML(Element, KDNodeSaverXML)
	 */
	public void toXML(Element dest, KDNodeSaverXML<E> saver) {
		balanceIfNeeded();
		dest.addContent(XMLHelper.makeAttrEl("Dimensions", Integer.toString(dimensions)));
		Element rootNode = XMLHelper.makeAttrEl("Item", "Root");
		toXML_recursive(root, rootNode, saver);
		dest.addContent(rootNode);
	}
	
	private void fromXML_ReadChildren(Element source, KDNodeSaverXML<E> reader) throws JDOMException {
		if (source == null)
			return;
		List children = source.getChildren();
		for (int i = children.size() - 1; i >= 0; i++) {
			Content child_content = (Content) children.get(i);
			if (child_content instanceof Element) {
				Element child = (Element)child_content;
				if (child.getName().equals("Item")) {
					E node = reader.nodeFromXML(child);
					if (node != null)
						add(node);
					fromXML_ReadChildren(child, reader);
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
	 */
	public static KDTree fromXML(Element source, KDNodeSaverXML reader) throws JDOMException {
		KDTree result = new KDTree();
		result.dimensions = Integer.parseInt(XMLHelper.getAttrEl(source, "Dimensions"));
		result.hyperRectangle = new double[result.dimensions][2];
		result.root = null;
		result.fromXML_ReadChildren(source, reader);
		result.balanceIfNeeded();
		return result;
	}

	private void toTextStream_recursive(PrintWriter fou, E node, KDNodeSaver saver) {
		if (node == null)
			return;
		fou.println(saver.nodeToString(node));
		toTextStream_recursive(fou, node.getLeft(), saver);
		toTextStream_recursive(fou, node.getRight(), saver);
	}
	
	/**
	 * The tree is stored to a text stream one node at a line.
	 * After reading the tree the {@link #balanceIfNeeded()} is called.
	 */
	public void toTextStream(PrintWriter fou, KDNodeSaver saver) {
		toTextStream_recursive(fou, root, saver);
	}
	
	/**
	 * Reads a tree from a text stream one node at a line. The method {@link #balanceIfNeeded()}
	 * is invoked 
	 *  
	 */
	public static KDTree fromTextStream(int dimensions, BufferedReader fin, KDNodeSaver reader) throws IOException {
		KDTree result = new KDTree();
		result.dimensions = dimensions;
		result.hyperRectangle = new double[result.dimensions][2];
		while (fin.ready()) {
			String str = fin.readLine().trim();
			if ((str.length() > 0) && (str.charAt(0) != '#'))
				result.add((KDNode) reader.nodeFromString(str));
		}
		result.balanceIfNeeded();
		return result;
	}

	private void add_recursive(E node, E curNode, int dimension, int depthLevel) {
		depthLevel++;
		double value = node.getValue(dimension);
		int nextDimension = (dimension + 1) % dimensions;
		if (value < curNode.getValue(dimension)) {
			if (curNode.getLeft() == null) {
				if (treeDepth < depthLevel)
					treeDepth = depthLevel;
				curNode.setLeft(node);
			} else
				add_recursive(node, curNode.getLeft(), nextDimension, depthLevel);
		} else {
			if (curNode.getRight() == null) {
				if (treeDepth < depthLevel) 
					treeDepth = depthLevel;
				curNode.setRight(node);
			} else
				add_recursive(node, curNode.getRight(), nextDimension, depthLevel);
		}		
	}

	/**
	 * Add a node to the tree. The tree might get disbalanced using this method.
	 * To balance the tree use {@link #balance()} or better {@link #balanceIfNeeded()}. 
	 */
	public void add(E node) {
		if (node == null)
			return;
		node.setLeft(null);
		node.setRight(null);
		if (root == null) { 
			root = node;
		} else {
			add_recursive(node, root, 0, 1);
		}
		m_size++;
	}
	
	/**
	 * Returns the number of the elements in the tree. 
	 */
	public int size() {
		return m_size;
	}
	
	/**
	 * Returns the max depth (or also called height) of the tree.
	 * 
	 * If the tree has no elements returns 0,
	 * 
	 * if the tree has one element, i.e. only root it has a depth of 1.
	 */
	public int getTreeDepth() {
		return treeDepth;
	}
	
	private static class IteratorVisit<E> {
		E item;
		byte status;
	}

	private class Itr implements Iterator<E> {
		
		private Stack<IteratorVisit<E>>stack;

		private E nextItem;

		Itr() {
			stack = new Stack<IteratorVisit<E>>();
			nextItem = root;
			if (root != null) {
				IteratorVisit<E>v = new IteratorVisit<E>();
				v.item = root;
				v.status = 0;
				stack.push(v);
			}
		}
		
		private E getNext() {
			while (!stack.empty()) {
				IteratorVisit<E>v = stack.peek();
				if (v.status == 0) {
					v.status++;
					E tmp = v.item.getLeft();
					if (tmp != null) {
						v = new IteratorVisit<E>();
						v.item = tmp;
						v.status = 0;
						stack.push(v);
						return tmp;
					}
				}
				if (v.status == 1) {
					v.status++;
					E tmp = v.item.getRight();
					if (tmp != null) {
						v = new IteratorVisit<E>();
						v.item = tmp;
						v.status = 0;
						stack.push(v);
						return tmp;
					}
				}
				stack.pop();
			}
			return null;
		}
		
		public boolean hasNext() {
			if (nextItem == null) 
				nextItem = getNext();
			return nextItem != null;
		}

		public E next() {
			E result = nextItem;
			nextItem = null;
			if (result == null) 
				result = getNext();
			return result;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public Iterator<E> iterator() {
		return new Itr();
	}
}
