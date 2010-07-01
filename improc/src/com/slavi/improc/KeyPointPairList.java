package com.slavi.improc;

import java.util.ArrayList;

import com.slavi.math.transform.TransformLearnerResult;

public class KeyPointPairList {
	/**
	 * Mapping between source points and KeyPointPairs
	 */
	public final ArrayList<KeyPointPair> items = new ArrayList<KeyPointPair>();
	
	public KeyPointList source = null;

	public KeyPointList target = null;

	// Helmert transformation parameters (Validate keypoint pairs)
	public double scale;
	public double angle;
	public double translateX;
	public double translateY;
	
	// Spherical pano adjust
	public double sphereRZ1;
	public double sphereRY;
	public double sphereRZ2;

	// ZYX and XYZ pano adjust
	public double rx, ry, rz; // source->target angles of rotation

	// Helmert pano adjust
	public double a;
	public double b;
	public double hTranslateX; // c
	public double hTranslateY; // d
	
	public double maxDiscrepancy;
	public double recoverDiscrepancy;
	public TransformLearnerResult transformResult;
	
	public KeyPointPairList() {
	}

	// TODO: OK up to here

	public void leaveGoodElements(double maxDiscrepancy) {
		for (KeyPointPair sp : items) {
			sp.bad = sp.discrepancy > maxDiscrepancy;
		}
	}
	
	public int getGoodCount() {
		int result = 0;
		for (KeyPointPair i : items)
			if (!i.bad)
				result++;
		return result;
	}
	
	public static ArrayList<KeyPointPairList> getImageChain(ArrayList<KeyPointPairList> items) {
		while (items.size() > 0) {
			KeyPointPairList start = items.remove(0);
			ArrayList<KeyPointPairList>result = new ArrayList<KeyPointPairList>();
			result.add(start);
			
			int curItemIndex = items.size() - 1;
			while (curItemIndex >= 0) {
				KeyPointPairList curItem = items.get(curItemIndex);
				
				for (int iIndex = result.size() - 1; iIndex >= 0; iIndex--) {
					KeyPointPairList i = result.get(iIndex);
					if (
							(i.source == curItem.source) ||
							(i.source == curItem.target) ||
							(i.target == curItem.source) ||
							(i.target == curItem.target)) {
						result.add(curItem);
						items.remove(curItemIndex);
						curItemIndex = items.size();
						break;
					}
				}
				curItemIndex--;
			}
			// Found a chain.
			return result;
		}
		return null;
	}
}
