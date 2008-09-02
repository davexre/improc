package com.slavi.math.transform;

import java.util.ArrayList;

import com.slavi.math.matrix.Matrix;
import com.slavi.math.statistics.StatisticsLT;

public abstract class BaseTransformLearner {

	public BaseTransformer transformer;
	
	public Iterable<? extends PointsPair> items;
	
	protected Matrix sourceOrigin;	
	protected Matrix sourceScale; 	
	protected Matrix sourceMin; 	
	protected Matrix sourceMax;
	
	protected Matrix targetOrigin;	
	protected Matrix targetScale;	
	protected Matrix targetMin;	
	protected Matrix targetMax;	
	
	protected BaseTransformLearner(BaseTransformer transformer, ArrayList<? extends PointsPair> pointsPairList) {
		this.transformer = transformer;
		this.items = pointsPairList;
		
		this.sourceOrigin = new Matrix(transformer.inputSize, 1);
		this.sourceScale = new Matrix(transformer.inputSize, 1); 
		this.sourceMin = new Matrix(transformer.inputSize, 1); 
		this.sourceMax = new Matrix(transformer.inputSize, 1);
		
		this.targetOrigin = new Matrix(transformer.outputSize, 1);
		this.targetScale = new Matrix(transformer.outputSize, 1);
		this.targetMin = new Matrix(transformer.outputSize, 1);
		this.targetMax = new Matrix(transformer.outputSize, 1);
	}
	
	public abstract boolean calculateOne();
	
	public abstract int getRequiredTrainingPoints();

	public boolean canCompute() {
		int required = getRequiredTrainingPoints();
		int goodCount = 0;
		for (PointsPair item : items)
			if (!item.isBad())
				if (++goodCount >= required)
					return true;
		return false;
	}
	
	private double oneOverSumWeights = 1.0;
	/**
	 * 
	 * @return Number of point pairs NOT marked as bad.
	 */
	protected int computeWeights() {
		int goodCount = 0;
		double sumWeight = 0;
		for (PointsPair item : items) {
			if (item.isBad())
				continue;
			if (item.getWeight() < 0)
				throw new IllegalArgumentException("Negative weight received.");
			goodCount++;
			sumWeight += item.getWeight();
		}
		if (sumWeight == 0.0) {
			oneOverSumWeights = 1.0 / goodCount;
		} else {
			oneOverSumWeights = 1.0 / sumWeight;
		}
		return goodCount;
	}
	
	public double getComputedWeight(PointsPair item) {
		return item.isBad() ? 0.0 : item.getWeight() * oneOverSumWeights; 
	}
	
	protected void computeScaleAndOrigin() {
		// Find source and target points' extents and compute all - 
		// scaleSource, scaleTarget, sourceOrigin, originTarget
		boolean isFirst = true;
		sourceOrigin.make0();
		targetOrigin.make0();
		for (PointsPair item : items) {
			if (item.isBad())
				continue;
			if (isFirst) {
				item.source.copyTo(sourceMin);
				item.source.copyTo(sourceMax);
				item.target.copyTo(targetMin);
				item.target.copyTo(targetMax);
			} else {
				item.source.mMin(sourceMin, sourceMin);
				item.source.mMax(sourceMax, sourceMax);
				item.target.mMin(targetMin, targetMin);
				item.target.mMax(targetMax, targetMax);
			}
			double computedWeight = getComputedWeight(item);
			for (int i = transformer.inputSize - 1; i >= 0; i--)
				sourceOrigin.setItem(i, 0, sourceOrigin.getItem(i, 0) + item.source.getItem(i, 0) * computedWeight);
			for (int i = transformer.outputSize - 1; i >= 0; i--)
				targetOrigin.setItem(i, 0, targetOrigin.getItem(i, 0) + item.target.getItem(i, 0) * computedWeight);
			isFirst = false;
		}
		
		double t;
		for (int i = transformer.inputSize - 1; i >= 0; i--) {
			t = sourceMax.getItem(i, 0) - sourceMin.getItem(i, 0);
			sourceScale.setItem(i, 0, t == 0.0 ? 1.0 : t);
		}
		for (int i = transformer.outputSize - 1; i >= 0; i--) {
			t = targetMax.getItem(i, 0) - targetMin.getItem(i, 0);
			targetScale.setItem(i, 0, t == 0.0 ? 1.0 : t);
		}
	}
	
	protected boolean isAdjusted() {
		StatisticsLT stat = new StatisticsLT();

		Matrix source = new Matrix(transformer.getInputSize(), 1);
		Matrix sourceTransformed = new Matrix(transformer.getOutputSize(), 1);
		// Determine correctness of source data (items array)
		for (PointsPair item : items) {
			// Compute for all points, so no item.isBad check
			for (int i = transformer.getInputSize() - 1; i >= 0; i--)
				source.setItem(i, 0, item.source.getItem(i, 0));
			transformer.transform(source, sourceTransformed);
			// Compute distance between target and sourceTransformed
			double sum2 = 0;
			for (int i = transformer.outputSize - 1; i >= 0; i--) {
				double d = item.target.getItem(i, 0) - sourceTransformed.getItem(i, 0);
				sum2 += d * d;
			}
			item.discrepancy = Math.sqrt(sum2);
		}
		
		boolean iterationHasBad = false;
		for (int k = 0; k < 3; k++) {
			stat.start();
			for (PointsPair item : items) {
				if (!item.isBad()) {
					stat.addValue(item.discrepancy, item.getWeight());
				}
			}
			stat.stop();
			iterationHasBad = false;
			for (PointsPair item : items) {
				if ((!item.isBad()) && stat.isBad(item.discrepancy)) {
					iterationHasBad = true;
					break;
				}
			}
			if (!iterationHasBad)
				break;
		}
		boolean adjusted = true;
		if (iterationHasBad)
			adjusted = false;
		for (PointsPair item : items) {
			boolean oldIsBad = item.isBad();
			boolean curIsBad = stat.isBad(item.discrepancy);
			if (oldIsBad == curIsBad) {
				item.setBad(curIsBad);
				adjusted = false;
			}
		}
		return adjusted;
	}

	private static final double MAX_WEIGHT = 100.0;
	private static final double MAX_WEIGHT_INVERTED = 1.0 / MAX_WEIGHT;
	/**
	 * Re-computes the weight of <b>all</b> points using the inverted distance
	 * (discrepancy) between transformed source and target points. 
	 * The formula is:
	 * MAX_WEIGHT = 100
	 * discrepancy = Sqrt(Sum(Pow(target[i] - tramsformer.transform(source[i]))))
	 * weight = discrepancy >= (1/MAX_WEIGHT) ? MAX_WEIGHT : 1/discrepancy 
	 */
	public void recomputeWeights() {
		for (PointsPair item : items) {
			item.setWeight(item.discrepancy >= MAX_WEIGHT_INVERTED ? MAX_WEIGHT : 1.0 / item.discrepancy);
		}
	}
	
	/**
	 * Computes the maximum absolute difference between each 
	 * target point and the transformed source point.
	 * The formula is:
	 * result[i,0] = Max(Abs(items(k).target[i,0] - transformer.transform(items(k).source)[i,0])) 
	 */
	public Matrix computeTransformedTargetDelta(boolean ignoreBad) {
		Matrix result = new Matrix(transformer.outputSize, 1);
		Matrix source = new Matrix(transformer.getInputSize(), 1);
		Matrix sourceTransformed = new Matrix(transformer.getOutputSize(), 1);
		result.make0();
		for (PointsPair item : items) {
			if (item.isBad() && ignoreBad)
				continue;
			for (int i = transformer.getInputSize() - 1; i >= 0; i--)
				source.setItem(i, 0, item.source.getItem(i, 0));
			transformer.transform(source, sourceTransformed);
			for (int i = transformer.getOutputSize() - 1; i >= 0; i--) {
				double d = Math.abs(item.target.getItem(i, 0) - sourceTransformed.getItem(i, 0));
				if (d > result.getItem(i, 0))
					result.setItem(i, 0, d);
			}
		}
		return result;
	}	
}
