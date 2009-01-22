package com.slavi.improc;

import java.util.Map.Entry;

import com.slavi.math.transform.AffineTransformLearner;

public class PanoPairTransformLerner extends AffineTransformLearner<KeyPoint, KeyPoint> {

	public PanoPairTransformLerner(Iterable<KeyPointPair> pointsPairList) {
		super(new PanoPairTransformer(), (Iterable) pointsPairList);
	}

	public KeyPoint createTemporaryTargetObject() {
		throw new RuntimeException("Method not allowed");
	}

	public double getDiscrepancy(Entry<KeyPoint, KeyPoint> item) {
		return ((KeyPointPair) item).getDiscrepancy();
	}

	public double getWeight(Entry<KeyPoint, KeyPoint> item) {
		return ((KeyPointPair) item).getWeight();
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
