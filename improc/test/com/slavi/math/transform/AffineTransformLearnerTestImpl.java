package com.slavi.math.transform;

import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Map.Entry;

import com.slavi.TestUtils;

public class AffineTransformLearnerTestImpl extends AffineTransformLearner<Point2D.Double, Point2D.Double> {

	public AffineTransformLearnerTestImpl(Iterable<? extends Map.Entry<Point2D.Double, Point2D.Double>> pointsPairList) {
		super(new AffineTransformerTestImpl(), pointsPairList);
	}

	public double getDiscrepancyThreshold(TransformLearnerResult result) {
		return TestUtils.precision;
	}
	
	public Point2D.Double createTemporaryTargetObject() {
		return new Point2D.Double();
	}

	public double getDiscrepancy(Entry<Point2D.Double, Point2D.Double> item) {
		return ((TransformerDataTestImpl) item).discrepancy;
	}

	public double getWeight(Entry<Point2D.Double, Point2D.Double> item) {
		return 1.0;
	}

	public boolean isBad(Entry<Point2D.Double, Point2D.Double> item) {
		return ((TransformerDataTestImpl) item).isBad;
	}

	public void setBad(Entry<Point2D.Double, Point2D.Double> item, boolean bad) {
		((TransformerDataTestImpl) item).isBad = bad;
	}

	public void setDiscrepancy(Entry<Point2D.Double, Point2D.Double> item, double discrepancy) {
		((TransformerDataTestImpl) item).discrepancy = discrepancy;
	}
}
