package com.slavi.improc.myadjust;

import java.util.Map.Entry;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointPair;
import com.slavi.math.transform.Helmert2DTransformLearner;

public class KeyPointHelmertTransformLearner extends Helmert2DTransformLearner<KeyPoint, KeyPoint>{

	public double discrepancyThreshold = 5; // value is in pixels
	
	public KeyPointHelmertTransformLearner(Iterable<KeyPointPair> pointsPairList) {
		super(new KeyPointHelmertTransformer(), pointsPairList);
	}

	public KeyPoint createTemporaryTargetObject() {
		return new KeyPoint();
	}

	public double getDiscrepancy(Entry<KeyPoint, KeyPoint> item) {
		return ((KeyPointPair)item).discrepancy;
	}

	public double getWeight(Entry<KeyPoint, KeyPoint> item) {
		return ((KeyPointPair)item).weight;
	}

	public boolean isBad(Entry<KeyPoint, KeyPoint> item) {
		return ((KeyPointPair)item).bad;
	}

	public void setBad(Entry<KeyPoint, KeyPoint> item, boolean bad) {
		((KeyPointPair)item).bad = bad;
	}

	public void setDiscrepancy(Entry<KeyPoint, KeyPoint> item, double discrepancy) {
		((KeyPointPair)item).discrepancy = discrepancy;
	}
	
	public double getMinAllowedDiscrepancy() {
		return 0;
	}
	
	public double getMaxAllowedDiscrepancy() {
//		double result = (discrepancyStatistics.getAvgValue() + discrepancyStatistics.getMaxX()) / 2.0;
		double result = discrepancyStatistics.getAvgValue();
		if (result < discrepancyThreshold)
			result = discrepancyThreshold;
		return result;
	}
}
