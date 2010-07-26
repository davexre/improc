package com.slavi.improc.myadjust;

import java.util.Map.Entry;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.math.transform.Helmert2DTransformLearner;
import com.slavi.math.transform.TransformLearnerResult;

public class KeyPointHelmertTransformLearner extends Helmert2DTransformLearner<KeyPoint, KeyPoint>{

	public static double discrepancyThreshold = 55; // TODO: ???? value is in pixels
	KeyPointPairList pairList;
	
	public KeyPointHelmertTransformLearner(KeyPointPairList pairList) {
		super(new KeyPointHelmertTransformer(), pairList.items);
		this.pairList = pairList;
	}

	public KeyPoint createTemporaryTargetObject() {
		KeyPoint result = new KeyPoint();
		result.keyPointList = pairList.target;
		return result; 
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
	
	public double getMaxAllowedDiscrepancy(TransformLearnerResult result) {
//		double r = (result.discrepancyStatistics.getAvgValue() + result.discrepancyStatistics.getMaxX()) / 2.0;
		double r = result.discrepancyStatistics.getAvgValue();
		double je = result.discrepancyStatistics.getJ_End();
		if (je < result.discrepancyStatistics.getMaxX())
			r = je;
		if (r < discrepancyThreshold)
			r = discrepancyThreshold;
		return r;
	}
}
