package com.slavi.math.transform;

import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Map.Entry;

public class Helmert2DTransformLearnerTestImpl extends Helmert2DTransformLearner<Point2D.Double, Point2D.Double> {
	public Helmert2DTransformLearnerTestImpl(Helmert2DTransformerTestImpl transformer,
			Iterable<? extends Map.Entry<Point2D.Double, Point2D.Double>> pointsPairList) {
		super(transformer, pointsPairList);
	}

	double lastMaxAllowedDiscrepancy = Double.MAX_VALUE;

	double discrepancyThreshold = 1.0;
	public double getMaxAllowedDiscrepancy(TransformLearnerResult result) {
		double max = result.discrepancyStatistics.getMaxX();
		double je = result.discrepancyStatistics.getJ_End();
		double avg = result.discrepancyStatistics.getAvgValue();
		double r = Math.min(je, (avg + max) / 2.0);
		if (r < discrepancyThreshold)
			r = discrepancyThreshold;

/*			if (r < lastMaxAllowedDiscrepancy) {
			lastMaxAllowedDiscrepancy = r;
		} else {
			r = lastMaxAllowedDiscrepancy;
		}*/			
		return r;
	}
	
	public double getComputedWeight(Map.Entry<Point2D.Double, Point2D.Double> item) {
		return getWeight(item); 
	}
	
	public Point2D.Double createTemporaryTargetObject() {
		return new Point2D.Double();
	}

	public double getDiscrepancy(Entry<Point2D.Double, Point2D.Double> item) {
		return ((TransformerDataTestImpl) item).discrepancy;
	}

	public double getWeight(Entry<Point2D.Double, Point2D.Double> item) {
		return 2123.123;
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
