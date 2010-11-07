package com.slavi.improc;

import java.util.Map;
import java.util.StringTokenizer;

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
	
	public boolean validatePairBad = false; // filled by ValidateKeyPointPairList
	
	public boolean panoBad = false; // filled by PanoTransformer 

	public KeyPointPair() {
		super();
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

	public int getUnmatchingCount() {
		int result = 0;
		for (int i = 0; i < KeyPoint.descriptorSize; i++)
			for (int j = 0; j < KeyPoint.descriptorSize; j++)
				for (int k = 0; k < KeyPoint.numDirections; k++) {
					if (
						(sourceSP.getItem(i, j, k) == 0) ^ 
						(targetSP.getItem(i, j, k) == 0)) 
						result++;
				}
		return result;
	}
	
	public int getMaxDifference() {
		int result = 0;
		for (int i = 0; i < KeyPoint.descriptorSize; i++)
			for (int j = 0; j < KeyPoint.descriptorSize; j++)
				for (int k = 0; k < KeyPoint.numDirections; k++) {
					int dif = Math.abs(sourceSP.getItem(i, j, k) - targetSP.getItem(i, j, k));
					if (result < dif)
						result = dif;
				}
		return result;
	}

	public String toString() {
		return
			Double.toString(distanceToNearest) + ":" + 
			Double.toString(distanceToNearest2) + ":" +
			sourceSP.toString() + ":" + targetSP.toString();
	}
	
	public static KeyPointPair fromString(String str) {
		StringTokenizer st = new StringTokenizer(str, ":");
		double d1 = Double.parseDouble(st.nextToken());
		double d2 = Double.parseDouble(st.nextToken());
		return new KeyPointPair(
				KeyPoint.fromString(st.nextToken()),
				KeyPoint.fromString(st.nextToken()), d1, d2);
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
