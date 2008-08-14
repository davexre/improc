package com.slavi.improc;

import java.util.ArrayList;

import com.slavi.util.tree.KDTree;

public class KeyPointPairBigTree {
	public final ArrayList<KeyPointList> keyPointLists = new ArrayList<KeyPointList>();

	public final KeyPointBigTree kdtree = new KeyPointBigTree();
	
	public static class KeyPointBigTree extends KDTree<KeyPoint> {
		public KeyPointBigTree() {
			super(KeyPoint.featureVectorLinearSize);
		}

		public boolean canFindDistanceBetween(KeyPoint fromNode, KeyPoint toNode) {
			return fromNode.keyPointList != toNode.keyPointList;
		}

		public double getValue(KeyPoint node, int dimensionIndex) {
			return node.getValue(dimensionIndex);
		}
	}
}
