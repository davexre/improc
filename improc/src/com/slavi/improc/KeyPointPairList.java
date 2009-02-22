package com.slavi.improc;

import java.util.HashMap;

public class KeyPointPairList {
	/**
	 * Mapping between source points and KeyPointPairs
	 */
	public final HashMap<KeyPoint, KeyPointPair> items = new HashMap<KeyPoint, KeyPointPair>();
	
	public KeyPointList source = null;

	public KeyPointList target = null;
	
	public KeyPointPairList() {
	}

	// TODO: OK up to here

	public void leaveGoodElements(double maxDiscrepancy) {
		for (KeyPointPair sp : items.values()) {
			sp.bad = sp.discrepancy > maxDiscrepancy;
		}
	}
}
