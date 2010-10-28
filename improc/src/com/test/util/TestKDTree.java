package com.test.util;

import java.util.ArrayList;
import java.util.List;

import com.slavi.math.adjust.Statistics;
import com.slavi.util.Marker;
import com.slavi.util.tree.KDTree;
import com.slavi.util.tree.NearestNeighbours;
import com.slavi.util.tree.TreeNode;

public class TestKDTree {
	
	public static class MyKDTree extends KDTree<MyKDData> {
		public MyKDTree(int dimensions, List<MyKDData> items) {
			super(dimensions, items, false);
		}

		public boolean canFindDistanceBetween(MyKDData fromNode, MyKDData toNode) {
			return true;
		}

		public double getValue(MyKDData node, int dimensionIndex) {
			return node.value[dimensionIndex];
		}
	}
	
	static int idCounter = 10000;
	public static class MyKDData {
		public int id;
		public double value[];
		
		public MyKDData(double value[]) {
			id = idCounter++; 
			this.value = new double[value.length];
			for (int i = value.length - 1; i >= 0; i--)
				this.value[i] = value[i];
		}
		
		public String toString() {
			StringBuilder buf = new StringBuilder();
			String prefix = "";
			for (int i = 0; i < value.length; i++) {
				buf.append(prefix);
				buf.append(value[i]);
				prefix = ", ";
			}
			return id + " " + buf.toString();
		}
	}
	
	protected static void generateItems(ArrayList<MyKDData> items, int dimensions, int itemsToGenerate) {
		double[] value = new double[dimensions];
		int itemsPerDimension = (int) Math.ceil(Math.pow(itemsToGenerate, 1.0 / dimensions));
		itemsPerDimension = Math.max(itemsPerDimension, 1);
		idCounter = 10000;
		recursiveGenerateItems(items, value, dimensions - 1, itemsPerDimension, itemsToGenerate);
	}
	
	protected static void recursiveGenerateItems(ArrayList<MyKDData> items, double[] value, int dimension, int itemsPerDimension, int itemsToGenerate) {
		if (items.size() >= itemsToGenerate)
			return;
		if (dimension < 0) {
			items.add(new MyKDData(value));
		} else {
			for (int i = itemsPerDimension - 1; i >= 0; i--) {
				value[dimension] = i;
				recursiveGenerateItems(items, value, dimension - 1, itemsPerDimension, itemsToGenerate);
			}
		}
	}
	
	public static void printNode(TreeNode<MyKDData> node, int level) {
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
	
	public static void printNodesList(ArrayList<MyKDData> items) {
		for (int i = 0; i < items.size(); i++) {
			MyKDData n = items.get(i);
			System.out.printf("%5d  %s\n", i, n.toString());
		}
		System.out.println();
	}
	
	public static void main2() {
		int dimensions = 2;
		int itemsToGenerate = 10;
		ArrayList<MyKDData> items = new ArrayList<MyKDData>();
		generateItems(items, dimensions, itemsToGenerate);
//		generateItems(items, dimensions, itemsToGenerate);

		printNodesList(items);
		
		MyKDData thePoint = items.get(2);

		MyKDTree tree = new MyKDTree(dimensions, items);
		printNode(tree.getRoot(), 0);

		NearestNeighbours<MyKDData> nnlst = tree.getNearestNeighbours(thePoint, 5);
		int maxUsedSearchSteps = nnlst.getUsedSearchSteps();
		
		System.out.println("--------");
		System.out.println("Point is:" + nnlst.getTarget().toString());
		for (int i = 0; i < nnlst.getSize(); i++) {
			System.out.println("Distance=" + Math.sqrt(nnlst.getDistanceToTarget(i)) + " value=" + nnlst.getItem(i).toString());
		}
		System.out.println("Used search steps=" + nnlst.getUsedSearchSteps());
		for (int i = 0; i < items.size(); i++) {
		  nnlst = tree.getNearestNeighbours(items.get(i), 5);
		  if (maxUsedSearchSteps < nnlst.getUsedSearchSteps())
			  maxUsedSearchSteps = nnlst.getUsedSearchSteps();
		}
		
		System.out.println("Numer of items=" + items.size());
		System.out.println("Max used steps=" + maxUsedSearchSteps);
	}
	
	public NearestNeighbours<MyKDData> findNearest(MyKDTree tree, MyKDData item) {
		int maxSearchSteps = tree.getTreeDepth() / 2;
		return tree.getNearestNeighboursBBF(item, 2, maxSearchSteps);
	}
	
	public static void main(String[] args) {
		int dimensions = 128;
		int itemsToGenerate = 3000;
		ArrayList<MyKDData> items = new ArrayList<MyKDData>();
		generateItems(items, dimensions, itemsToGenerate);
		MyKDTree tree = new MyKDTree(dimensions, items);
//		printNodesList(items);
//		printNode(tree.getRoot(), 0);
		Marker.mark();
		System.out.println("Tree size : " + tree.getSize());
		System.out.println("Tree depth: " + tree.getTreeDepth());
//		tree.balance();
		System.out.println("Tree size : " + tree.getSize());
		System.out.println("Tree depth: " + tree.getTreeDepth());
		Marker.release();
		MyKDData item = items.get(items.size() - 1);
		System.out.println("Item to find:");
		System.out.println(item);
		System.out.println(tree.contains(item));
		int maxSearchSteps = 2 * itemsToGenerate; //tree.getTreeDepth() / 2;
		Statistics stat = new Statistics();
		stat.start();
		for (MyKDData i : items) {
//			NearestNeighbours<MyKDData> nn = tree.getNearestNeighboursMy(i, 2, 1);
//			NearestNeighbours<MyKDData> nn = tree.getNearestNeighboursMyBBF(i, 2, 1, maxSearchSteps);
			NearestNeighbours<MyKDData> nn = tree.getNearestNeighboursBBF(i, 2, maxSearchSteps);
//			NearestNeighbours<MyKDData> nn = tree.getNearestNeighbours(i, 2);
			stat.addValue(nn.getUsedSearchSteps());
			if (i != nn.getItem(0)) {
				System.out.println("NOT FOUND " + i + "|" + nn.getItem(0));
			}				
		}
		stat.stop();
		System.out.println(stat);
		NearestNeighbours<MyKDData> nn = tree.getNearestNeighboursMy(item, 3, 5);
		System.out.println("Neares neighbours:");
		for (int i = 0; i < nn.getSize(); i++)
			System.out.println(nn.getItem(i));
	}
	
	public static void main2(String[] args) throws Exception {
		int dimensions = 3;
		int itemsToGenerate = 3;
		ArrayList<MyKDData> items = new ArrayList<MyKDData>();
		generateItems(items, dimensions, itemsToGenerate);
		MyKDTree tree = new MyKDTree(dimensions, items);
		int itemsCount = tree.getSize();
		if (items.size() != itemsCount)
			throw new Exception("Incorrect tree addition");
		for (MyKDData item : items)
			tree.add(item);
		itemsCount = tree.getSize();
		if (items.size() != itemsCount)
			throw new Exception("Duplicated items exist in tree");
		System.out.println("OK");
	}
}
