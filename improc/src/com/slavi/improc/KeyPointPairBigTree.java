package com.slavi.improc;

import java.util.ArrayList;

import com.slavi.util.tree.KDTree;

public class KeyPointPairBigTree extends KDTree<KeyPoint> {
	public final ArrayList<KeyPointList> keyPointLists = new ArrayList<KeyPointList>();

	public KeyPointPairBigTree() {
		super(KeyPoint.featureVectorLinearSize);
	}

	public boolean canFindDistanceBetween(KeyPoint fromNode, KeyPoint toNode) {
		return fromNode.keyPointList != toNode.keyPointList;
	}

	public double getValue(KeyPoint node, int dimensionIndex) {
		return node.getValue(dimensionIndex);
	}
}
