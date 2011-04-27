package com.slavi.util.tree;

import java.util.ArrayList;
import java.util.List;

public class KDTreeTestImpl extends KDTree<KDTreeTestData> {
	public KDTreeTestImpl(int dimensions, List<KDTreeTestData> items) {
		super(dimensions, items, false);
	}

	public boolean canFindDistanceBetween(KDTreeTestData fromNode, KDTreeTestData toNode) {
		return true;
	}

	public double getValue(KDTreeTestData node, int dimensionIndex) {
		return node.value[dimensionIndex];
	}

	public static void printNode(TreeNode<KDTreeTestData> node, int level) {
		String s = "";
		for (int i = 0; i < level; i++)
			s += "|  ";

		if (node == null) {
			System.out.println(s + "null");
		} else {
			System.out.println(s + node.getData().toString());
			printNode(node.getLeft(), level + 1);
			printNode(node.getRight(), level + 1);
		}
	}
	
	public void printTree() {
		printNode(getRoot(), 0);
	}
	
	public static void printNodesList(List<KDTreeTestData> items) {
		int i = 0;
		for (KDTreeTestData n : items) {
			System.out.printf("%5d | %s\n", i++, n.toString());
		}
		System.out.println();
	}
	
	private boolean checkValidNode(TreeNode<KDTreeTestData> item, boolean onTheLeft, double value, int dimension) {
		if (item == null)
			return true;
		double val = getValue(item.data, dimension);
		boolean bad = (val < value) ^ onTheLeft; 
		if (bad)
			return false;
		if (!checkValidNode(item.left, onTheLeft, value, dimension))
			return false;
		if (!checkValidNode(item.right, onTheLeft, value, dimension))
			return false;
		return true;		
	}
	
	private boolean checkNode(TreeNode<KDTreeTestData> item, int dimension) {
		if (item == null)
			return true;
		double val = getValue(item.data, dimension);
		if (!checkValidNode(item.left, true, val, dimension))
			return false;
		if (!checkValidNode(item.right, false, val, dimension))
			return false;

		int nextDim = (dimension + 1) % dimensions;
		if (!checkNode(item.left, nextDim))
			return false;
		if (!checkNode(item.right, nextDim))
			return false;
		return true;
	}
	
	public boolean isValidTree() {
		return checkNode(getRoot(), 0);
	}

	private void dumpItemList(String msg, ArrayList<KDTreeTestData> list) {
		System.out.println("---------");
		System.out.println("Items list " + msg);
		int index = 0;
		for (KDTreeTestData i : list) {
			System.out.println(index + " " + i);
			index++;
		}
		System.out.println("=========");
	}
	
	///////////////////////////
	
	/**
     * @return a negative integer, zero, or a positive integer as the
     * 	       first argument is less than, equal to, or greater than the
     *	       second.
     * @see Comparator.compare()
	 */
	private int compare(KDTreeTestData i1, KDTreeTestData i2, int dimension) {
		for (int i = 0; i < dimensions; i++) {
			double v1 = getValue(i1, dimension);
			double v2 = getValue(i2, dimension);
			if (v1 < v2)
				return -1;
			if (v1 > v2)
				return 1;
			dimension = (dimension + 1) % dimensions;
		}
		return 0;
	}

	private int deepSort2(ArrayList<KDTreeTestData> items, int left, int right, int curDimension) {
		if (left > right)
			return -1;
		int midIndex = (left + right) >> 1;

		for (int i = left; i <= midIndex; i++) {
			// Find min item index
			int minItemIndex = i;
			KDTreeTestData minItem = items.get(minItemIndex);
			for (int j = i + 1; j <= right; j++) {
				int res = compare(items.get(j), minItem, curDimension);
				if (res < 0) {
					minItemIndex = j;
					minItem = items.get(j);
				}
			}
			if (minItemIndex != i) {
				// swap
				KDTreeTestData temp = items.get(i);
				items.set(i, minItem);
				items.set(minItemIndex, temp);
			}
		}
		// Now go back and check if on the left there are the "same" points and pick up the first in the row
		KDTreeTestData midItem = items.get(midIndex);
		for (int i = midIndex - 1; i >= left; i--) {
			KDTreeTestData cur = items.get(i);
			if (compare(midItem, cur, curDimension) != 0) {
				break;
			}
			midIndex = i;
		}
		return midIndex;
	}
	
	private TreeNode<KDTreeTestData> balanceSegment2(ArrayList<KDTreeTestData> items, int left, int right, int curDimension, int depthLevel) {
		if (left > right)
			return null;
		int midIndex = deepSort2(items, left, right, curDimension);
		System.out.println("segment L=" + left + " R=" + right + " DIM=" + curDimension + " MID=" + midIndex);
		dumpItemList("sort", items);
		int nextDimension = (curDimension + 1) % dimensions;
		TreeNode<KDTreeTestData> result = new TreeNode<KDTreeTestData>(items.get(midIndex));
		depthLevel++;
		if (depthLevel > treeDepth)
			treeDepth = depthLevel;
		result.left = balanceSegment(items, dimensions, left, midIndex - 1, nextDimension, depthLevel);
		result.right = balanceSegment(items, dimensions, midIndex + 1, right, nextDimension, depthLevel);
		return result;
	}
	
	public void balance2() {
		ArrayList<KDTreeTestData>list = toList();
		treeDepth = 0;
		mutations++;
		dumpItemList("before", list);
		root = balanceSegment2(list, 0, list.size() - 1, 0, 0);
		dumpItemList("after", list);
	}
	
	//////////////////////////
	
	private int deepSort(ArrayList<KDTreeTestData> items, int dimensions, int left, int right, int curDimension, int numberOfUnsuccessfullSorts) {
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
				KDTreeTestData tmp = items.get(minIndex);
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
	
	private TreeNode<KDTreeTestData> balanceSegment(ArrayList<KDTreeTestData> items, int dimensions, int left, int right, int curDimension, int depthLevel) {
		if (left > right)
			return null;
		int midIndex = deepSort(items, dimensions, left, right, curDimension, 0);
		System.out.println("segment L=" + left + " R=" + right + " DIM=" + curDimension + " MID=" + midIndex);
		dumpItemList("sort", items);
		double midValue = getValue(items.get(midIndex), curDimension);
		int startIndex = midIndex - 1;
		for (; startIndex >= left; startIndex--) 
			if (getValue(items.get(startIndex), curDimension) != midValue) 
				break;
		startIndex++;
		if (startIndex != midIndex) {
			KDTreeTestData tmp = items.get(midIndex);
			items.set(midIndex, items.get(startIndex));
			items.set(startIndex, tmp);
		}
		int nextDimension = (curDimension + 1) % dimensions;
		TreeNode<KDTreeTestData> result = new TreeNode<KDTreeTestData>(items.get(startIndex));
		depthLevel++;
		if (depthLevel > treeDepth)
			treeDepth = depthLevel;

		result.left = balanceSegment(items, dimensions, left, startIndex - 1, nextDimension, depthLevel);
		result.right = balanceSegment(items, dimensions, startIndex + 1, right, nextDimension, depthLevel);
		return result;
	}
	
	public void balance() {
		ArrayList<KDTreeTestData>list = toList();
		treeDepth = 0;
		mutations++;
		dumpItemList("before", list);
		root = balanceSegment(list, dimensions, 0, list.size() - 1, 0, 0);
		dumpItemList("after", list);
	}
}
