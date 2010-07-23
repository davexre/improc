package com.slavi.improc;

import com.slavi.util.tree.KDTree;

public class KeyPointTreeImageSpace extends KDTree<KeyPoint> {
	public KeyPointTreeImageSpace() {
		super(2, false);
	}

	public boolean canFindDistanceBetween(KeyPoint fromNode, KeyPoint toNode) {
		if (fromNode.keyPointList == toNode.keyPointList)
			return false;
		return true;
	}

	public double getValue(KeyPoint node, int dimensionIndex) {
		return dimensionIndex == 0 ? node.doubleX : node.doubleY;
	}
}
