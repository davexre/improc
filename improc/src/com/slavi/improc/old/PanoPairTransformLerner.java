package com.slavi.improc.old;

import java.util.Map.Entry;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointPair;
import com.slavi.math.transform.AffineTransformLearner;

public class PanoPairTransformLerner extends AffineTransformLearner<KeyPoint, KeyPoint> {

	public boolean useWeight = true;
	
	public PanoPairTransformLerner(Iterable<KeyPointPair> pointsPairList) {
		super(new PanoPairTransformer(), pointsPairList);
	}

	public KeyPoint createTemporaryTargetObject() {
		return new KeyPoint();
	}

	public double getDiscrepancy(Entry<KeyPoint, KeyPoint> item) {
		return ((KeyPointPair) item).discrepancy;
	}

	public double getWeight(Entry<KeyPoint, KeyPoint> item) {
		double result;
		if (useWeight)
			result = 1.0;
		else {
			result = Math.abs(((KeyPointPair) item).discrepancy);
			if (result < 1.0)
				result = 1.0;
			result = 1.0 / result;
		}
		return result;
	}

	public boolean isBad(Entry<KeyPoint, KeyPoint> item) {
		return ((KeyPointPair) item).bad;
	}

	public void setBad(Entry<KeyPoint, KeyPoint> item, boolean bad) {
		((KeyPointPair) item).bad = bad;
	}

	public void setDiscrepancy(Entry<KeyPoint, KeyPoint> item, double discrepancy) {
		((KeyPointPair) item).discrepancy = discrepancy;
	}
}
