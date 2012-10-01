package com.slavi.util.tree;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Thread SAFE implementaion of K-dimensional tree.
 */
public abstract class KDTree<E> implements Iterable<E>{
	
	public abstract boolean canFindDistanceBetween(E fromNode, E toNode);
	
	public abstract double getValue(E node, int dimensionIndex);

	Object synch = new Object();
	
	protected final int dimensions;
	
	protected volatile TreeNode<E> root;
	
	volatile int size;
	
	volatile int treeDepth;
	
	volatile int mutations;
	
	final boolean ignoreDuplicates;
	
	/**
	 * 
	 * @param	ignoreDuplicates
	 * 			Specifies whether to accept or to ignore duplicated items.
	 *			Setting to true will cause invocation of
	 *			{@link #compareItems(Object, Object)} at each {@link #add(Object)} and
	 *			thus slowing down the addition of new items.
	 */
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
		synchronized (synch) {
			size = 0;
			treeDepth = 0;
			root = null;
			mutations++;
		}
	}

	private void addToList_recursive(ArrayList<E>list, TreeNode<E> node) {
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
		int oldMutations = mutations;
		ArrayList<E>list = new ArrayList<E>(size);
		addToList_recursive(list, root);
		if (oldMutations != mutations)
			throw new ConcurrentModificationException();
		return list;
	}

	/**
	 * Balances the tree. The preferred way to do this is 
	 * to use {@link #balanceIfNeeded()}
	 */
	public void balance() {
		int oldMutations = mutations;
		ArrayList<E> list = toList();
		BalanceData data = new BalanceData();
		TreeNode<E> newRoot = balanceSegment(data, 0, list.size() - 1, 0, 0);
		synchronized (synch) {
			if (mutations == oldMutations) {
				treeDepth = data.newTreeDepth;
				mutations++;
				root = newRoot;
			} else
				throw new ConcurrentModificationException();
		}
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
	
	static class BalanceData<E> {
		ArrayList<E> items;
		int newTreeDepth = 0;
	}
	
	private TreeNode<E> balanceSegment(BalanceData<E> data, int left, int right, int curDimension, int depthLevel) {
		if (left > right)
			return null;
		int midIndex = deepSort(data.items, dimensions, left, right, curDimension, 0);
		double midValue = getValue(data.items.get(midIndex), curDimension);
		int startIndex = midIndex - 1;
		for (; startIndex >= left; startIndex--) 
			if (getValue(data.items.get(startIndex), curDimension) != midValue) 
				break;
		startIndex++;
		if (startIndex != midIndex) {
			E tmp = data.items.get(midIndex);
			data.items.set(midIndex, data.items.get(startIndex));
			data.items.set(startIndex, tmp);
		}
		int nextDimension = (curDimension + 1) % dimensions;
		TreeNode<E> result = new TreeNode<E>(data.items.get(startIndex));
		depthLevel++;
		if (depthLevel > data.newTreeDepth)
			data.newTreeDepth = depthLevel;
		result.left = balanceSegment(data, left, startIndex - 1, nextDimension, depthLevel);
		result.right = balanceSegment(data, startIndex + 1, right, nextDimension, depthLevel);
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

	private double getDistanceSquared(E n1, E n2, double maxDistancePerCoordinate) {
		double result = 0;
		for (int i = dimensions - 1; i >= 0; i--) {
			double d = getValue(n1, i) - getValue(n2, i);
			if (Math.abs(d) > maxDistancePerCoordinate)
				return Double.MAX_VALUE;
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

	public NearestNeighbours<E> getNearestNeighbours(E target, int maxNeighbours) {
		return getNearestNeighbours(target, maxNeighbours, Double.MAX_VALUE);
	}
	
	/**
	 * Returns [maxNeighbours] of elements closest to the [target] element.
	 * The list is sorted using the distance from the element to the target element.
	 * 
	 * @see #getNearestNeighboursBBF(Object, int, int)
	 */
	public NearestNeighbours<E> getNearestNeighbours(E target, int maxNeighbours, double maxDistanceToTarget) {
		NearestNeighbours<E> result = new NearestNeighbours<E>(target, maxNeighbours, dimensions, maxDistanceToTarget);
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
		TreeNode<E> curNode = root;
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
	
	private void nearestSegment(NearestNeighbours<E> nearest, E target, TreeNode<E> curNode, int dimension) {
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
				if (getDistanceSquaredToHR(nearest.hr, target) < nearest.getMaxDistanceToTarget()) 
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
				if (getDistanceSquaredToHR(nearest.hr, target) < nearest.getMaxDistanceToTarget()) 
					nearestSegment(nearest, target, curNode.left, nextDimension);
				// Restore the hyper rectangle
				nearest.hr[dimension][1] = tmp;
			}
		}
	}

	public NearestNeighbours<E> getNearestNeighboursBBF(E target, int maxNeighbours, int maxSearchSteps) {
		return getNearestNeighboursBBF(target, maxNeighbours, maxSearchSteps, Double.MAX_VALUE);
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
	public NearestNeighbours<E> getNearestNeighboursBBF(E target, int maxNeighbours, int maxSearchSteps, double maxDistanceToTarget) {
		NearestNeighbours<E> result = new NearestNeighbours<E>(target, maxNeighbours, dimensions, maxDistanceToTarget);
		result.searchSteps = maxSearchSteps;
		nearestSegment(result, target, root, 0);
		return result;
	}

	private void addInternal(E data, TreeNode<E> curNode, int dimension, int depthLevel) {
		while (true) {
			depthLevel++;
			double value = getValue(data, dimension);
			double curNodeValue = getValue(curNode.data, dimension);
			if (value < curNodeValue) {
				if (curNode.left == null) {
					synchronized (synch) {
						if (curNode.left == null) {
							TreeNode<E> newNode = new TreeNode<E>(data);
							curNode.left = newNode;
							mutations++;
							size++;
							if (depthLevel > treeDepth)
								treeDepth = depthLevel;
							break;
						}
					}
				}
				curNode = curNode.left;
				dimension = (dimension + 1) % dimensions;
			} else if ((!ignoreDuplicates) && (value == curNodeValue) && compareItems(data, curNode.data)) {
				return;
			} else {
				if (curNode.right == null) {
					synchronized (synch) {
						if (curNode.right == null) {
							TreeNode<E> newNode = new TreeNode<E>(data);
							curNode.right = newNode;
							mutations++;
							size++;
							if (depthLevel > treeDepth)
								treeDepth = depthLevel;
							break;
						}
					}
				}
				curNode = curNode.right;
				dimension = (dimension + 1) % dimensions;
			}
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
			synchronized (synch) {
				if (root == null) {
					TreeNode<E> newNode = new TreeNode<E>(item);
					root = newNode;
					mutations++;
					size++;
					return;
				}
			}
		}
		addInternal(item, root, 0, 1);
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
		
		private Stack<IteratorVisit<TreeNode<E>>>stack;

		private TreeNode<E> nextItem;

		Itr() {
			stack = new Stack<IteratorVisit<TreeNode<E>>>();
			nextItem = root;
			mutationsAtIteratorCreate = mutations;
			if (nextItem != null) {
				IteratorVisit<TreeNode<E>>v = new IteratorVisit<TreeNode<E>>();
				v.item = nextItem;
				v.status = 0;
				stack.push(v);
			}
		}
		
		private TreeNode<E> getNext() {
			while (!stack.empty()) {
				IteratorVisit<TreeNode<E>>v = stack.peek();
				if (v.status == 0) {
					v.status++;
					TreeNode<E> tmp = v.item.left;
					if (tmp != null) {
						v = new IteratorVisit<TreeNode<E>>();
						v.item = tmp;
						v.status = 0;
						stack.push(v);
						return tmp;
					}
				}
				if (v.status == 1) {
					v.status++;
					TreeNode<E> tmp = v.item.right;
					if (tmp != null) {
						v = new IteratorVisit<TreeNode<E>>();
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
			TreeNode<E> result = nextItem;
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
	
	public TreeNode<E> getRoot() {
		return root;
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
		return getNearestNeighboursMy(target, maxNeighbours, maxDistancePerCoordinate, Double.MAX_VALUE);
	}
		
	public NearestNeighbours<E> getNearestNeighboursMy(E target, int maxNeighbours, double maxDistancePerCoordinate, double maxDistanceToTarget) {
		NearestNeighbours<E> result = new NearestNeighbours<E>(target, maxNeighbours, dimensions, maxDistanceToTarget);
		result.searchSteps = -1;
		nearestSegmentMy(result, target, root, 0, Math.abs(maxDistancePerCoordinate));
		return result;
	}
	
	public NearestNeighbours<E> getNearestNeighboursMyBBF(E target, int maxNeighbours, double maxDistancePerCoordinate, int maxSearchSteps) {
		return getNearestNeighboursMyBBF(target, maxNeighbours, maxDistancePerCoordinate, maxSearchSteps, Double.MAX_VALUE);
	}
	
	public NearestNeighbours<E> getNearestNeighboursMyBBF(E target, int maxNeighbours, double maxDistancePerCoordinate, int maxSearchSteps, double maxDistanceToTarget) {
		NearestNeighbours<E> result = new NearestNeighbours<E>(target, maxNeighbours, dimensions, maxDistanceToTarget);
		result.searchSteps = maxSearchSteps;
		nearestSegmentMy(result, target, root, 0, Math.abs(maxDistancePerCoordinate));
		return result;
	}
	
	private void nearestSegmentMy(NearestNeighbours<E> nearest, E target, TreeNode<E> curNode, int dimension, double maxDistancePerCoordinate) {
		if (curNode == null) 
			return;
		nearest.usedSearchSteps++;

		if (canFindDistanceBetween(target, curNode.data))
			nearest.add(curNode.data, getDistanceSquared(target, curNode.data, maxDistancePerCoordinate));
		double curNodeValue = getValue(curNode.data, dimension);
		int nextDimension = (dimension + 1) % dimensions;
		double targetValue = getValue(target, dimension);
		
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
					if (getDistanceSquaredToHR(nearest.hr, target) < nearest.getMaxDistanceToTarget()) 
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
					if (getDistanceSquaredToHR(nearest.hr, target) < nearest.getMaxDistanceToTarget()) 
						nearestSegmentMy(nearest, target, curNode.left, nextDimension, maxDistancePerCoordinate);
					// Restore the hyper rectangle
					nearest.hr[dimension][1] = tmp;
				}
			}
		}
	}
}
