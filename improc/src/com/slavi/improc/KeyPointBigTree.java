package com.slavi.improc;

import java.util.ArrayList;

import com.slavi.util.tree.ConcurrentKDTree;

public class KeyPointBigTree extends ConcurrentKDTree<KeyPoint> {
	public final ArrayList<KeyPointList> keyPointLists = new ArrayList<KeyPointList>();

	public KeyPointBigTree() {
		super(KeyPoint.featureVectorLinearSize, false);
	}

	public boolean canFindDistanceBetween(KeyPoint fromNode, KeyPoint toNode) {
		return fromNode.keyPointList != toNode.keyPointList;
	}

	public double getValue(KeyPoint node, int dimensionIndex) {
		return node.getValue(dimensionIndex);
	}
}
