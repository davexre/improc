package com.slavi.improc;

import java.util.Map;

public class KeyPointPair implements Map.Entry<KeyPoint, KeyPoint>{
	public KeyPoint sourceSP;

	public KeyPoint targetSP;

	// Distance to nearest KeyPoint as reported by nearestNeighbourhood
	public double distanceToNearest = 0.0;
	// Distance to second-nearest KeyPoint as reported by nearestNeighbourhood
	public double distanceToNearest2 = 0.0;

	public double discrepancy = 0.0;

	public double panoDiscrepancy = 0.0;

	public double weight = 0.0;

	public boolean validatePairBad = false;	// filled by ValidateKeyPointPairList

	public boolean panoBad = false;			// filled by PanoTransformer

	public KeyPointPair() {
		sourceSP = null;
		targetSP = null;
		distanceToNearest = 0;
		distanceToNearest2 = 0;
	}

	public KeyPointPair(KeyPoint sourceSP, KeyPoint targetSP, double distanceToNearest, double distanceToNearest2) {
		this.sourceSP = sourceSP;
		this.targetSP = targetSP;
		this.distanceToNearest = distanceToNearest;
		this.distanceToNearest2 = distanceToNearest2;
	}

	public KeyPoint getKey() {
		return sourceSP;
	}

	public KeyPoint getValue() {
		return targetSP;
	}

	public KeyPoint setValue(KeyPoint value) {
		throw new RuntimeException("Method not allowed");
	}
}
