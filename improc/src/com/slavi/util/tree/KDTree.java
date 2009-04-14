package com.slavi.util.tree;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public abstract class KDTree<E> implements Iterable<E>{
	
	public abstract boolean canFindDistanceBetween(E fromNode, E toNode);
	
	public abstract double getValue(E node, int dimensionIndex);

	public static class NearestNeighbours<E>{
		// TODO: Fix this warning
		@SuppressWarnings("unchecked")
		NearestNeighbours(E target, int maxCapacity, int dimensions) {
			this.target = target;
			distancesToTarget = new double[maxCapacity];
			items = (E[])new Object[maxCapacity];
			m_size = 0;
			searchSteps = 0;
			usedSearchSteps = 0;
			hr = new double[dimensions][2];

			for (int i = dimensions - 1; i >= 0; i--) {
				hr[i][0] = Double.MIN_VALUE;
				hr[i][1] = Double.MAX_VALUE;
			}
		}
		
		int searchSteps;
		
		int usedSearchSteps;

		double[][] hr;

		E target;
		
		double[] distancesToTarget;
		
		E[] items;
		
		volatile int m_size;
		
		public int size() {
			return m_size;
		}
		
		public E getItem(int atIndex) {
			return items[atIndex];
		}
		
		public double getDistanceToTarget(int atIndex) {
			return distancesToTarget[atIndex];
		}
		
		public int getCapacity() {
			return distancesToTarget.length;
		}
		
		public E getTarget() {
			return target;
		}
		
		public int countAdds = 0;

		public int getSearchSteps() {
			return searchSteps;
		}
		
		public int getUsedSearchSteps() {
			return usedSearchSteps;
		}
		
		void add(E item, double distanceToTarget) {
			countAdds++;
			int insertAt = 0;
			// Find insert position
			for (; insertAt < m_size; insertAt++) {
				if (distanceToTarget < distancesToTarget[insertAt])
					break;
			}
			// If value is greater than all elements then ignore it.
			if (insertAt >= distancesToTarget.length)
				return;
			// Increase size if limit not reached
			if (m_size < distancesToTarget.length)
				m_size++;
			// Make room to insert
			int itemsToMove = m_size - insertAt - 1;
			if (itemsToMove > 0) {
				System.arraycopy(distancesToTarget, insertAt, distancesToTarget, insertAt + 1, itemsToMove);
				System.arraycopy(items, insertAt, items, insertAt + 1, itemsToMove);
			}
			// Put item in insertAt position
			distancesToTarget[insertAt] = distanceToTarget;
			items[insertAt] = item;
		}
	}
	
	public static class Node<E> {
		protected E data;
		protected Node<E> left, right;
		
		Node(E data) {
			this.data = data;
			left = null;
			right = null;
		}
		
		public E getData() {
			return data;
		}
		
		public Node<E> getLeft() {
			return left;
		}
		
		public Node<E> getRight() {
			return right;
		}
	}	
	
	protected int dimensions;

	protected Node<E> root;
	
	volatile int size;
	
	volatile int treeDepth;
	
	volatile int mutations;
	
	boolean ignoreDuplicates;
	
	public KDTree(int dimensions, boolean ignoreDuplicates) {
		this.dimensions = dimensions;
		this.size = 0;
		this.treeDepth = 0;
		this.root = null;
		this.mutations = 0;
		this.ignoreDuplicates = ignoreDuplicates;
	}

	public KDTree(int dimensions, List<E> items, boolean ignoreDuplicates) {
		this(dimensions, ignoreDuplicates);
		for (E item : items)
			add(item);
	}
	
	/**
	 * Removes all elements in the tree.
	 */
	public void clear() {
		size = 0;
		treeDepth = 0;
		root = null;
		mutations++;
	}

	private void addToList_recursive(ArrayList<E>list, Node<E> node) {
		if (node == null)
			return;
		list.add(node.data);
		addToList_recursive(list, node.left); 
		addToList_recursive(list, node.right); 
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
	 * Balances the tree. The preferred way to do this is 
	 * to use {@link #balanceIfNeeded()}
	 */
	public void balance() {
		ArrayList<E>list = toList();
		treeDepth = 0;
		mutations++;
		root = balanceSegment(list, dimensions, 0, list.size() - 1, 0, 0);
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
	 * maxDepthDeviation = {@link #getTreeDepth()} * 0.05 (5%)
	 * and also 3 <= maxDepthDeviation
	 */
	public boolean isBalanceNeeded() {
		int curDepth = getTreeDepth();
		int perfectDepth = getPerfectTreeDepth();
		int maxDepthDeviation = (int)(curDepth * 0.05);
		if (maxDepthDeviation < 3)
			maxDepthDeviation = 3;
		return perfectDepth + maxDepthDeviation < curDepth;
	}

	/**
	 * The numberOfUnsuccessfullSorts is used to avoid endless recursion if the list of items contains
	 * more than [dimensions] number of elements with equal values for ALL dimensions.
	 * 
	 * If the items list contains more than one element with equal value in a specific dimension, the
	 * list is recursively sorted using the next dimension.  
	 */
	private int deepSort(ArrayList<E> items, int dimensions, int left, int right, int curDimension, int numberOfUnsuccessfullSorts) {
		if (left > right)
			return -1;
		int midIndex = (left + right) >> 1;

		int segmentEndIndex = right;
		double segmentEndValue = getValue(items.get(segmentEndIndex), curDimension);
		
		int segmentStartIndex = right;
		for (; segmentStartIndex >= left; segmentStartIndex--) {
			int minIndex = segmentStartIndex;
			double segmentStartValue = getValue(items.get(segmentStartIndex), curDimension);
			
			for (int j = segmentStartIndex - 1; j >= left; j--) {
				double tmp = getValue(items.get(j), curDimension);
				if (tmp >= segmentStartValue) {
					segmentStartValue = tmp;
					minIndex = j;
				}
			}
			if (minIndex != segmentStartIndex) {
				E tmp = items.get(minIndex);
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
	
	private Node<E> balanceSegment(ArrayList<E> items, int dimensions, int left, int right, int curDimension, int depthLevel) {
		if (left > right)
			return null;
		int midIndex = deepSort(items, dimensions, left, right, curDimension, 0);
		double midValue = getValue(items.get(midIndex), curDimension);
		int startIndex = midIndex - 1;
		for (; startIndex >= left; startIndex--) 
			if (getValue(items.get(startIndex), curDimension) != midValue) 
				break;
		startIndex++;
		if (startIndex != midIndex) {
			E tmp = items.get(midIndex);
			items.set(midIndex, items.get(startIndex));
			items.set(startIndex, tmp);
		}
		int nextDimension = (curDimension + 1) % dimensions;
		Node<E> result = new Node<E>(items.get(startIndex));
		depthLevel++;
		if (depthLevel > treeDepth)
			treeDepth = depthLevel;
		result.left = balanceSegment(items, dimensions, left, startIndex - 1, nextDimension, depthLevel);
		result.right = balanceSegment(items, dimensions, startIndex + 1, right, nextDimension, depthLevel);
		return result;
	}
	
	private double getDistanceSquared(E n1, E n2) {
		double result = 0;
		for (int i = dimensions - 1; i >= 0; i--) {
			double d = getValue(n1, i) - getValue(n2, i);
			result += d * d;
		}
		return result;
	}

	private double getDistanceSquaredToHR(double[][] hr, E target) {
		double result = 0;
		for (int i = dimensions - 1; i >= 0; i--) {
			double d = getValue(target, i);
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
	
	/**
	 * Returns [maxNeighbours] of elements closest to the [target] element.
	 * The list is sorted using the distance from the element to the target element.
	 * 
	 * @see #getNearestNeighboursBBF(Object, int, int)
	 */
	public NearestNeighbours<E> getNearestNeighbours(E target, int maxNeighbours) {
		NearestNeighbours<E> result = new NearestNeighbours<E>(target, maxNeighbours, dimensions);
		result.searchSteps = -1;
		nearestSegment(result, target, root, 0);
		return result;
	}

	/**
	 * Compares the corresponding dimension values two items and returns true if all are equal.
	 * 
	 * The items a and b may not be included in the tree.
	 */
	public boolean compareItems(E a, E b) {
		for (int i = dimensions - 1; i >= 0; i--)
			if (getValue(a, i) != getValue(b, i))
				return false;		
		return true;
	}
	
	/**
	 * Searches for an item in the tree that has all the dimension values equal to the
	 * values in target item.
	 * The returned value is a reference to the first found matching item. 
	 */
	public E findMatching(E target) {
		Node<E> curNode = root;
		int dimension = 0;
		while (curNode != null) {
			double curNodeValue = getValue(curNode.data, dimension);
			double curDimTarget = getValue(target, dimension);
			
			if (curDimTarget < curNodeValue) {
				curNode = curNode.left;
			} else {
				if (curDimTarget == curNodeValue) {
					if (compareItems(target, curNode.data))
						return curNode.data;
				}
				curNode = curNode.right;
			}
			dimension = (dimension + 1) % dimensions;
		}		
		return null;
	}	

	/**
	 * Returns true if an item having the same values in all of its 
	 * dimensions is contained in the tree. 
	 */
	public boolean contains(E item) {
		return findMatching(item) != null;
	}
	
	private void nearestSegment(NearestNeighbours<E> nearest, E target, Node<E> curNode, int dimension) {
		if (curNode == null) 
			return;
		nearest.usedSearchSteps++;

		if (canFindDistanceBetween(target, curNode.data))
			nearest.add(curNode.data, getDistanceSquared(target, curNode.data));
		double curNodeValue = getValue(curNode.data, dimension);
		int nextDimension = (dimension + 1) % dimensions;

		if (getValue(target, dimension) < curNodeValue) {
			// Prepare and check the "nearer" hyper rectangle
			double tmp = nearest.hr[dimension][1];
			nearest.hr[dimension][1] = curNodeValue;
			nearestSegment(nearest, target, curNode.left, nextDimension);
			nearest.hr[dimension][1] = tmp;

			if (nearest.searchSteps != 0) {
				if (nearest.searchSteps > 0)
					nearest.searchSteps--;
				// Prepare the "further" hyper rectangle
				tmp = nearest.hr[dimension][0];
				nearest.hr[dimension][0] = curNodeValue;
				// Check the "further" hyper rectangle:
				//   if capacity is not reached OR
				//   if distance from target to "further" hyper rectangle is smaller 
				//      than the maximum of the currently found neighbours
				if ((nearest.size() < nearest.getCapacity()) ||
						(getDistanceSquaredToHR(nearest.hr, target) < nearest.getDistanceToTarget(nearest.size() - 1))) 
					nearestSegment(nearest, target, curNode.right, nextDimension);
				// Restore the hyper rectangle
				nearest.hr[dimension][0] = tmp;
			}
		} else {
			// Prepare and check the "nearer" hyper rectangle
			double tmp = nearest.hr[dimension][0];
			nearest.hr[dimension][0] = curNodeValue;			
			nearestSegment(nearest, target, curNode.right, nextDimension);
			nearest.hr[dimension][0] = tmp;

			if (nearest.searchSteps != 0) {
				if (nearest.searchSteps > 0)
					nearest.searchSteps--;
				// Prepare the "further" hyper rectangle
				tmp = nearest.hr[dimension][1];
				nearest.hr[dimension][1] = curNodeValue;
				// Check the "further" hyper rectangle:
				//   if capacity is not reached OR
				//   if distance from target to "further" hyper rectangle is smaller 
				//      than the maximum of the currently found neighbours
				if ((nearest.size() < nearest.getCapacity()) ||
						(getDistanceSquaredToHR(nearest.hr, target) < nearest.getDistanceToTarget(nearest.size() - 1))) 
					nearestSegment(nearest, target, curNode.left, nextDimension);
				// Restore the hyper rectangle
				nearest.hr[dimension][1] = tmp;
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
	 * @see #getNearestNeighbours(Object, int)
	 */
	public NearestNeighbours<E> getNearestNeighboursBBF(E target, int maxNeighbours, int maxSearchSteps) {
		NearestNeighbours<E> result = new NearestNeighbours<E>(target, maxNeighbours, dimensions);
		result.searchSteps = maxSearchSteps;
		nearestSegment(result, target, root, 0);
		return result;
	}

	private void add_recursive(E data, Node<E> curNode, int dimension, int depthLevel) {
		depthLevel++;
		double value = getValue(data, dimension);
		double curNodeValue = getValue(curNode.data, dimension);
		int nextDimension = (dimension + 1) % dimensions;
		if (value < curNodeValue) {
			if (curNode.left == null) {
				if (treeDepth < depthLevel)
					treeDepth = depthLevel;
				curNode.left = new Node<E>(data);
				mutations++;
				size++;
			} else
				add_recursive(data, curNode.left, nextDimension, depthLevel);
		} else if ((!ignoreDuplicates) && (value == curNodeValue) && compareItems(data, curNode.data)) {
			return;
		} else {
			if (curNode.right == null) {
				if (treeDepth < depthLevel) 
					treeDepth = depthLevel;
				curNode.right = new Node<E>(data);
				mutations++;
				size++;
			} else
				add_recursive(data, curNode.right, nextDimension, depthLevel);
		}		
	}

	/**
	 * Adds a node to the tree.
	 *  
	 * The tree might get disbalanced using this method.
	 * <p>
	 * To balance the tree use {@link #balance()} or better {@link #balanceIfNeeded()}.
	 * <p>
	 * Setting the property ignoreDuplicates to true will cause the add to invoke
	 * {@link #contains(Object)} to check is such an item exists in the tree. If 
	 * {@link #contains(Object)} returns true, i.e. the item being add is duplicated
	 * the method will ignore the item and will silently return.
	 * <p>
	 * Null items are discarded/ignored.   
	 */
	public void add(E item) {
		if (item == null)
			return;
		if (root == null) { 
			root = new Node<E>(item);
			mutations++;
			size++;
		} else {
			add_recursive(item, root, 0, 1);
		}
	}
	
	/**
	 * Returns the number of the elements in the tree. 
	 */
	public int getSize() {
		return size;
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
	
	private static final double log2 = Math.log(2.0);
	public int getPerfectTreeDepth() {
		return (int)(Math.ceil(Math.log(size) / log2));
	}
	
	static class IteratorVisit<E> {
		E item;
		byte status;
	}

	private class Itr implements Iterator<E> {
		
		private final int mutationsAtIteratorCreate;
		
		private Stack<IteratorVisit<Node<E>>>stack;

		private Node<E> nextItem;

		Itr() {
			stack = new Stack<IteratorVisit<Node<E>>>();
			nextItem = root;
			mutationsAtIteratorCreate = mutations;
			if (root != null) {
				IteratorVisit<Node<E>>v = new IteratorVisit<Node<E>>();
				v.item = root;
				v.status = 0;
				stack.push(v);
			}
		}
		
		private Node<E> getNext() {
			while (!stack.empty()) {
				IteratorVisit<Node<E>>v = stack.peek();
				if (v.status == 0) {
					v.status++;
					Node<E> tmp = v.item.left;
					if (tmp != null) {
						v = new IteratorVisit<Node<E>>();
						v.item = tmp;
						v.status = 0;
						stack.push(v);
						return tmp;
					}
				}
				if (v.status == 1) {
					v.status++;
					Node<E> tmp = v.item.right;
					if (tmp != null) {
						v = new IteratorVisit<Node<E>>();
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

		/*
		 * Idea borrowed from java.util.AbstractList#iterator()
		 */
		private void checkForModification() {
			if (mutationsAtIteratorCreate != mutations)
				throw new ConcurrentModificationException();
		}
		
		public boolean hasNext() {
			checkForModification();
			if (nextItem == null) 
				nextItem = getNext();
			return nextItem != null;
		}

		public E next() {
			checkForModification();
			Node<E> result = nextItem;
			nextItem = null;
			if (result == null) 
				result = getNext();
			return result == null ? null : result.data;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public Iterator<E> iterator() {
		return new Itr();
	}
	
	public Iterator<E> snapshotIterator() {
		return toList().iterator();
	}
	
	public Node<E> getRoot() {
		return root;
	}

	/**
	 * Specifies whether to accept or to ignore duplicated items.
	 * 
	 * Setting this property to true will cause invocation of
	 * {@link #contains(Object)} at each {@link #add(Object)} and
	 * thus slowing down the addition of new items.
	 * <p>
	 * Changing this value affects only newly added items.
	 * <p>
	 * The default value is false, i.e. duplicates are accepted and
	 * adding new objects is faster. 
	 * @see #add(Object)  
	 */
	public void setIgnoreDuplicates(boolean ignoreDuplicates) {
		this.ignoreDuplicates = ignoreDuplicates;
	}
	
	/**
	 * @see #setIgnoreDuplicates(boolean)
	 * @see #add(Object)  
	 */
	public boolean getIgnoreDuplicates() {
		return ignoreDuplicates;
	}
	

	////////////////////////////////////////////////////////////////////////////////////
	
	public NearestNeighbours<E> getNearestNeighboursMy(E target, int maxNeighbours, double maxDistancePerCoordinate) {
		NearestNeighbours<E> result = new NearestNeighbours<E>(target, maxNeighbours, dimensions);
		result.searchSteps = -1;
		nearestSegmentMy(result, target, root, 0, Math.abs(maxDistancePerCoordinate));
		return result;
	}
	
	public NearestNeighbours<E> getNearestNeighboursMyBBF(E target, int maxNeighbours, double maxDistancePerCoordinate, int maxSearchSteps) {
		NearestNeighbours<E> result = new NearestNeighbours<E>(target, maxNeighbours, dimensions);
		result.searchSteps = maxSearchSteps;
		nearestSegmentMy(result, target, root, 0, Math.abs(maxDistancePerCoordinate));
		return result;
	}
	
	private void nearestSegmentMy(NearestNeighbours<E> nearest, E target, Node<E> curNode, int dimension, double maxDistancePerCoordinate) {
		if (curNode == null) 
			return;
		nearest.usedSearchSteps++;

		double curNodeValue = getValue(curNode.data, dimension);
		double targetValue = getValue(target, dimension);
		double min = targetValue - maxDistancePerCoordinate;
		double max = targetValue + maxDistancePerCoordinate;
		
		if ((min <= curNodeValue) && (curNodeValue <= max) && canFindDistanceBetween(target, curNode.data))
			nearest.add(curNode.data, getDistanceSquared(target, curNode.data));
		int nextDimension = (dimension + 1) % dimensions;
		
		if (targetValue < curNodeValue) {
			// Prepare and check the "nearer" hyper rectangle
			double tmp = nearest.hr[dimension][1];
			nearest.hr[dimension][1] = curNodeValue;
			nearestSegmentMy(nearest, target, curNode.left, nextDimension, maxDistancePerCoordinate);
			nearest.hr[dimension][1] = tmp;

			if (targetValue + maxDistancePerCoordinate >= curNodeValue) {
				if (nearest.searchSteps != 0) {
					if (nearest.searchSteps > 0)
						nearest.searchSteps--;
					// Prepare the "further" hyper rectangle
					tmp = nearest.hr[dimension][0];
					nearest.hr[dimension][0] = curNodeValue;
					// Check the "further" hyper rectangle:
					//   if capacity is not reached OR
					//   if distance from target to "further" hyper rectangle is smaller 
					//      than the maximum of the currently found neighbours
					if ((nearest.size() < nearest.getCapacity()) ||
							(getDistanceSquaredToHR(nearest.hr, target) < nearest.getDistanceToTarget(nearest.size() - 1))) 
						nearestSegmentMy(nearest, target, curNode.right, nextDimension, maxDistancePerCoordinate);
					// Restore the hyper rectangle
					nearest.hr[dimension][0] = tmp;
				}
			}
		} else {
			// Prepare and check the "nearer" hyper rectangle
			double tmp = nearest.hr[dimension][0];
			nearest.hr[dimension][0] = curNodeValue;			
			nearestSegmentMy(nearest, target, curNode.right, nextDimension, maxDistancePerCoordinate);
			nearest.hr[dimension][0] = tmp;

			if (targetValue - maxDistancePerCoordinate <= curNodeValue) {
				if (nearest.searchSteps != 0) {
					if (nearest.searchSteps > 0)
						nearest.searchSteps--;
					// Prepare the "further" hyper rectangle
					tmp = nearest.hr[dimension][1];
					nearest.hr[dimension][1] = curNodeValue;
					// Check the "further" hyper rectangle:
					//   if capacity is not reached OR
					//   if distance from target to "further" hyper rectangle is smaller 
					//      than the maximum of the currently found neighbours
					if ((nearest.size() < nearest.getCapacity()) ||
							(getDistanceSquaredToHR(nearest.hr, target) < nearest.getDistanceToTarget(nearest.size() - 1))) 
						nearestSegmentMy(nearest, target, curNode.left, nextDimension, maxDistancePerCoordinate);
					// Restore the hyper rectangle
					nearest.hr[dimension][1] = tmp;
				}
			}
		}
	}

}
