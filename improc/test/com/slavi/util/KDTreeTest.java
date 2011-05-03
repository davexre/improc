package com.slavi.util;

import java.util.ArrayList;

import org.junit.Test;

import com.slavi.TestUtils;
import com.slavi.math.adjust.Statistics;
import com.slavi.util.tree.KDTreeTestData;
import com.slavi.util.tree.KDTreeTestImpl;
import com.slavi.util.tree.NearestNeighbours;

public class KDTreeTest {

	protected static void recursiveGenerateTestDataItems(int dimension, int itemsPerDimension, int itemsToGenerate, double[] value, ArrayList<KDTreeTestData> items) {
		if (items.size() >= itemsToGenerate)
			return;
		if (dimension < 0) {
			items.add(new KDTreeTestData(value));
		} else {
			for (int i = itemsPerDimension - 1; i >= 0; i--) {
				value[dimension] = i;
				recursiveGenerateTestDataItems(dimension - 1, itemsPerDimension, itemsToGenerate, value, items);
			}
		}
	}

	public static ArrayList<KDTreeTestData> generateTestDataItems(int dimensions, int itemsToGenerate) {
		ArrayList<KDTreeTestData> items = new ArrayList<KDTreeTestData>();
		double[] value = new double[dimensions];
		int itemsPerDimension = (int) Math.ceil(Math.pow(itemsToGenerate, 1.0 / dimensions));
		itemsPerDimension = Math.max(itemsPerDimension, 1);
		recursiveGenerateTestDataItems(dimensions - 1, itemsPerDimension, itemsToGenerate, value, items);
		return items;
	}

	@Test
	public void testIgnoreDuplicates() {
		int dimensions = 3;
		int itemsToGenerate = 3;
		ArrayList<KDTreeTestData> items = KDTreeTest.generateTestDataItems(dimensions, itemsToGenerate);
		KDTreeTestImpl tree = new KDTreeTestImpl(dimensions, items);
		int itemsCount = tree.getSize();
		TestUtils.assertEqual("Incorrect tree addition", items.size(), itemsCount);

		for (KDTreeTestData item : items)
			tree.add(item);
		itemsCount = tree.getSize();
		TestUtils.assertEqual("Duplicated items exist in tree", items.size(), itemsCount);
	}
	
	@Test
	public void testContains() {
		int dimensions = 4;
		int itemsToGenerate = 20;
		ArrayList<KDTreeTestData> items = generateTestDataItems(dimensions, itemsToGenerate);
		KDTreeTestImpl tree = new KDTreeTestImpl(dimensions, items);
		
		for (KDTreeTestData i : items) {
			TestUtils.assertTrue("Item not found", tree.contains(i));
		}
	}
	
	private static abstract class FindNearest {
		public abstract NearestNeighbours<KDTreeTestData> findNN(KDTreeTestImpl tree, KDTreeTestData item);
	}
	
	private void doTestNearestNeighbourhood(ArrayList<KDTreeTestData> items, KDTreeTestImpl tree, String method, FindNearest find) {
		System.out.println("Testing method " + method);
		Statistics stat = new Statistics();
		stat.start();
		for (KDTreeTestData i : items) {
			NearestNeighbours<KDTreeTestData> nn;
			nn = find.findNN(tree, i);
			TestUtils.assertTrue("", i == nn.getItem(0));
			stat.addValue(nn.getUsedSearchSteps());
		}
		stat.stop();
		System.out.println("Statistics on <Used search steps>:");
		System.out.println(stat);
		System.out.println();
	}
	
	@Test
	public void testNearestNeighbourhood() {
		int dimensions = 20;
		int itemsToGenerate = 222;
		ArrayList<KDTreeTestData> items = generateTestDataItems(dimensions, itemsToGenerate);
		KDTreeTestImpl tree = new KDTreeTestImpl(dimensions, items);
		final int maxSearchSteps = 2 * itemsToGenerate; //tree.getTreeDepth() / 2;
		
		doTestNearestNeighbourhood(items, tree, "getNearestNeighbours", new FindNearest() {
			public NearestNeighbours<KDTreeTestData> findNN(KDTreeTestImpl tree, KDTreeTestData item) {
				return tree.getNearestNeighbours(item, 2);
			}
		});
		
		doTestNearestNeighbourhood(items, tree, "getNearestNeighboursBBF", new FindNearest() {
			public NearestNeighbours<KDTreeTestData> findNN(KDTreeTestImpl tree, KDTreeTestData item) {
				return tree.getNearestNeighboursBBF(item, 2, maxSearchSteps);
			}
		});
		
		doTestNearestNeighbourhood(items, tree, "getNearestNeighboursMy", new FindNearest() {
			public NearestNeighbours<KDTreeTestData> findNN(KDTreeTestImpl tree, KDTreeTestData item) {
				return tree.getNearestNeighboursMy(item, 2, Double.MAX_VALUE);
			}
		});
		
		doTestNearestNeighbourhood(items, tree, "getNearestNeighboursMyBBF", new FindNearest() {
			public NearestNeighbours<KDTreeTestData> findNN(KDTreeTestImpl tree, KDTreeTestData item) {
				return tree.getNearestNeighboursMyBBF(item, 2, Double.MAX_VALUE, maxSearchSteps);
			}
		});
	}
	
	@Test
	public void testBalance() {
		int dimensions = 5;
		int itemsToGenerate = 10;
		ArrayList<KDTreeTestData> items = generateTestDataItems(dimensions, itemsToGenerate);
		KDTreeTestImpl tree = new KDTreeTestImpl(dimensions, items);

		int oldDepth = tree.getTreeDepth();
		int perfectTreeDepth = tree.getPerfectTreeDepth();
		
		TestUtils.assertTrue("Tree is broken", tree.isValidTree());
		System.out.println("Tree BEFORE balance");
		tree.printTree();
		tree.balance();
		System.out.println("Tree AFTER balance");
		tree.printTree();
		TestUtils.assertTrue("Tree is broken", tree.isValidTree());
		
		int newDepth = tree.getTreeDepth();
		System.out.println("Old depth " + oldDepth);
		System.out.println("New depth " + newDepth);
		System.out.println("Perfect   " + perfectTreeDepth);
		TestUtils.assertEqual("", newDepth, perfectTreeDepth);
	}
	
	public static void main(String[] args) {
		new KDTreeTest().testBalance();
	}
}
