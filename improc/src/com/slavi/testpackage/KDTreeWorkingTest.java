package com.slavi.testpackage;

import java.util.ArrayList;

import com.slavi.img.working.KDNode;
import com.slavi.img.working.KDNodeBase;
import com.slavi.img.working.KDTree;
import com.slavi.img.working.NearestNeighbours;


public class KDTreeWorkingTest {
	public static class KDNodeTest extends KDNodeBase {
		public double value[];
		
		public KDNodeTest(double value[]) {
			this.value = new double[value.length];
			for (int i = value.length - 1; i >= 0; i--)
				this.value[i] = value[i];
		}
		
		public int getDimensions() {
			return value.length;
		}
		
		public double getValue(int dimensionIndex) {
			return value[dimensionIndex]; 
		}
		
		public String toString() {
			StringBuilder buf = new StringBuilder();
			String prefix = "";
			for (int i = 0; i < value.length; i++) {
				buf.append(prefix);
				buf.append(value[i]);
				prefix = ", ";
			}
			return buf.toString();
		}
	}
	
	protected static void generateItems(ArrayList items, double[] value, int dimension, int itemsPerDimension) {
		if (dimension < 0) {
			items.add(new KDNodeTest(value));
		} else {
			for (int i = itemsPerDimension - 1; i >= 0; i--) {
				value[dimension] = i;
				generateItems(items, value, dimension - 1, itemsPerDimension);
			}
		}
	}
	
	public static void printNode(KDNode node, int level) {
		String s = "";
		for (int i = 0; i < level; i++)
			s += "|  ";

		if (node == null) {
			System.out.println(s + "null");
		} else {
			System.out.println(s + node.toString());
			printNode(node.getLeft(), level + 1);
			printNode(node.getRight(), level + 1);
		}
	}
	
	public static void printNodesList(ArrayList items) {
		for (int i = 0; i < items.size(); i++) {
			KDNode n = (KDNode) items.get(i);
			System.out.printf("%5d  %s\n", i, n.toString());
		}
		System.out.println();
	}
	
	public static void main(String[] args) {
		int dimensions = 2;
		int itemsPerDimension = 5;
		ArrayList items = new ArrayList();
		generateItems(items, new double[dimensions], dimensions - 1, itemsPerDimension);
//		generateItems(items, new double[dimensions], dimensions - 1, itemsPerDimension);

		printNodesList(items);
		
		KDNode thePoint = (KDNode)items.get(2);

		KDTree tree = new KDTree(items, dimensions);
		printNode(tree.root, 0);

		NearestNeighbours nnlst = tree.getNearestNeighbours(thePoint, 5);
		System.out.println("--------");
		System.out.println("Point is:" + nnlst.getTarget().toString());
		for (int i = 0; i < nnlst.size(); i++) {
			System.out.println("Distance=" + Math.sqrt(nnlst.getValue(i)) + " value=" + nnlst.getItem(i).toString());
		}
		System.out.println("Used search steps=" + tree.usedSearchSteps);
		for (int i = 0; i < items.size(); i++) {
		  nnlst = tree.getNearestNeighbours((KDNode)items.get(i), 5);
		}
		
		System.out.println("Numer of items=" + items.size());
		System.out.println("Max used steps=" + tree.maxUsedSearchSteps);
	}
}
