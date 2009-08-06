package com.slavi.improc;

import java.util.ArrayList;

import com.slavi.util.tree.ConcurrentKDTree;

public class KeyPointBigTree extends ConcurrentKDTree<KeyPoint> {
	public final ArrayList<KeyPointList> keyPointLists = new ArrayList<KeyPointList>();

	public KeyPointBigTree() {
		super(KeyPoint.featureVectorLinearSize, false);
	}

	public static final double persentMaxDiscrepancy = 0.20;
	public static final int maxAbsoluteDiscrepancyPerCoordinate = (int)(256 * persentMaxDiscrepancy);
	
	public boolean canFindDistanceBetween(KeyPoint fromNode, KeyPoint toNode) {
		if (fromNode.keyPointList == toNode.keyPointList)
			return false;
		for (int i = 0; i < KeyPoint.descriptorSize; i++)
			for (int j = 0; j < KeyPoint.descriptorSize; j++)
				for (int k = 0; k < KeyPoint.numDirections; k++) {
					int d = fromNode.getItem(i, j, k) - toNode.getItem(i, j, k);
					if (Math.abs(d) > maxAbsoluteDiscrepancyPerCoordinate)
						return false;
				}
		return true;
	}

	public double getValue(KeyPoint node, int dimensionIndex) {
		return node.getValue(dimensionIndex);
	}
}
