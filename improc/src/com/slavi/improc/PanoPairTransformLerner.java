package com.slavi.improc;

import java.util.Map.Entry;

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
		return ((KeyPointPair) item).getDiscrepancy();
	}

	public double getWeight(Entry<KeyPoint, KeyPoint> item) {
		double result;
		if (useWeight)
			result = ((KeyPointPair) item).getWeight();
		else {
			result = Math.abs(((KeyPointPair) item).getDiscrepancy());
			if (result < 1.0)
				result = 1.0;
			result = 1.0 / result;
		}
		return result;
	}

	public boolean isBad(Entry<KeyPoint, KeyPoint> item) {
		return ((KeyPointPair) item).isBad();
	}

	public void setBad(Entry<KeyPoint, KeyPoint> item, boolean bad) {
		((KeyPointPair) item).setBad(bad);
	}

	public void setDiscrepancy(Entry<KeyPoint, KeyPoint> item, double discrepancy) {
		((KeyPointPair) item).setDiscrepancy(discrepancy);
	}
}
