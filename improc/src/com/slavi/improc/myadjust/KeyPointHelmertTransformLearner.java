package com.slavi.improc.myadjust;

import java.util.Map.Entry;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.math.transform.Helmert2DTransformLearner;
import com.slavi.math.transform.TransformLearnerResult;

public class KeyPointHelmertTransformLearner extends Helmert2DTransformLearner<KeyPoint, KeyPoint>{

	public static double discrepancyThreshold = 15; // TODO: ???? value is in pixels
	KeyPointPairList pairList;
	
	public KeyPointHelmertTransformLearner(KeyPointPairList pairList) {
		super(new KeyPointHelmertTransformer(), pairList.items);
		this.pairList = pairList;
	}

	public KeyPoint createTemporaryTargetObject() {
		return new KeyPoint(pairList.target, 0, 0);
	}

	public double getDiscrepancy(Entry<KeyPoint, KeyPoint> item) {
		return ((KeyPointPair)item).discrepancy;
	}

	public double getWeight(Entry<KeyPoint, KeyPoint> item) {
		return ((KeyPointPair)item).weight;
	}

	public boolean isBad(Entry<KeyPoint, KeyPoint> item) {
		return ((KeyPointPair)item).validatePairBad;
	}

	public void setBad(Entry<KeyPoint, KeyPoint> item, boolean bad) {
		((KeyPointPair)item).validatePairBad = bad;
	}

	public void setDiscrepancy(Entry<KeyPoint, KeyPoint> item, double discrepancy) {
		((KeyPointPair)item).discrepancy = discrepancy;
	}
	
	public double getDiscrepancyThreshold(TransformLearnerResult result) {
		return discrepancyThreshold;
	}
}
