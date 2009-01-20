package com.slavi.math.transform2;

import java.util.Map;

import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public abstract class BaseTransformLearner<InputType, OutputType> {

	public abstract boolean isBad(Map.Entry<InputType, OutputType> item);
	
	public abstract void setBad(Map.Entry<InputType, OutputType> item, boolean bad);

	public abstract double getWeight(Map.Entry<InputType, OutputType> item);

	/**
	 * The distance between target and sourceTransformed. The formula is:
	 * discrepancy = sqrt(sum(pow(target.getItem(i,0) -
	 * soruceTransformed.getItem(i,0), 2)))
	 */
	public abstract double getDiscrepancy(Map.Entry<InputType, OutputType> item);

	public abstract void setDiscrepancy(Map.Entry<InputType, OutputType> item, double discrepancy);

	public abstract OutputType createTemporaryTargetObject();
	
	///////////////////////////
	
	public final int inputSize;
	
	public final int outputSize;
	
	
	public BaseTransformer<InputType, OutputType> transformer;
	
	public Iterable<Map.Entry<InputType, OutputType>> items;
	
	protected Matrix sourceOrigin;	
	protected Matrix sourceScale; 	
	protected Matrix sourceMin; 	
	protected Matrix sourceMax;
	
	protected Matrix targetOrigin;	
	protected Matrix targetScale;	
	protected Matrix targetMin;	
	protected Matrix targetMax;	
	
	protected BaseTransformLearner(BaseTransformer<InputType, OutputType> transformer, 
			Iterable<Map.Entry<InputType, OutputType>> pointsPairList) {
		this.transformer = transformer;
		inputSize = transformer.getInputSize();
		outputSize = transformer.getOutputSize();
		this.items = pointsPairList;
		
		this.sourceOrigin = new Matrix(inputSize, 1);
		this.sourceScale = new Matrix(inputSize, 1); 
		this.sourceMin = new Matrix(inputSize, 1); 
		this.sourceMax = new Matrix(inputSize, 1);
		
		this.targetOrigin = new Matrix(outputSize, 1);
		this.targetScale = new Matrix(outputSize, 1);
		this.targetMin = new Matrix(outputSize, 1);
		this.targetMax = new Matrix(outputSize, 1);
	}
	
	public abstract boolean calculateOne();
	
	public abstract int getRequiredTrainingPoints();

	public boolean canCompute() {
		int required = getRequiredTrainingPoints();
		int goodCount = 0;
		for (Map.Entry<InputType, OutputType> item : items)
			if (!isBad(item))
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
		for (Map.Entry<InputType, OutputType> item : items) {
			if (isBad(item))
				continue;
			double weight = getWeight(item); 
			if (weight < 0)
				throw new IllegalArgumentException("Negative weight received.");
			goodCount++;
			sumWeight += weight;
		}
		if (sumWeight == 0.0) {
			oneOverSumWeights = 1.0 / goodCount;
		} else {
			oneOverSumWeights = 1.0 / sumWeight;
		}
		return goodCount;
	}
	
	public double getComputedWeight(Map.Entry<InputType, OutputType> item) {
		return isBad(item) ? 0.0 : getWeight(item) * oneOverSumWeights; 
	}
	
	protected void computeScaleAndOrigin() {
		// Find source and target points' extents and compute all - 
		// scaleSource, scaleTarget, sourceOrigin, originTarget
		boolean isFirst = true;
		sourceOrigin.make0();
		targetOrigin.make0();
		for (Map.Entry<InputType, OutputType> item : items) {
			if (isBad(item))
				continue;
			InputType source = item.getKey();
			OutputType dest = item.getValue();
			if (isFirst) {
				for (int i = inputSize - 1; i >= 0; i--) {
					double v = transformer.getSourceCoord(source, i);
					sourceMin.setItem(i, 0, v);
					sourceMax.setItem(i, 0, v);
				}
				for (int i = outputSize - 1; i >= 0; i--) {
					double v = transformer.getTargetCoord(dest, i);
					targetMin.setItem(i, 0, v);
					targetMax.setItem(i, 0, v);
				}
			} else {
				for (int i = inputSize - 1; i >= 0; i--) {
					double v = transformer.getSourceCoord(source, i);
					if (v < sourceMin.getItem(i, 0))
						sourceMin.setItem(i, 0, v);
					if (v > sourceMax.getItem(i, 0))
						sourceMax.setItem(i, 0, v);
				}
				for (int i = outputSize - 1; i >= 0; i--) {
					double v = transformer.getTargetCoord(dest, i);
					if (v < targetMin.getItem(i, 0))
						targetMin.setItem(i, 0, v);
					if (v > targetMax.getItem(i, 0))
						targetMax.setItem(i, 0, v);
				}
			}
			double computedWeight = getComputedWeight(item);
			for (int i = inputSize - 1; i >= 0; i--)
				sourceOrigin.setItem(i, 0, sourceOrigin.getItem(i, 0) + transformer.getSourceCoord(source, i) * computedWeight);
			for (int i = outputSize - 1; i >= 0; i--)
				targetOrigin.setItem(i, 0, targetOrigin.getItem(i, 0) + transformer.getTargetCoord(dest, i) * computedWeight);
			isFirst = false;
		}
		
		double t;
		for (int i = inputSize - 1; i >= 0; i--) {
			t = sourceMax.getItem(i, 0) - sourceMin.getItem(i, 0);
			sourceScale.setItem(i, 0, t == 0.0 ? 1.0 : t);
		}
		for (int i = outputSize - 1; i >= 0; i--) {
			t = targetMax.getItem(i, 0) - targetMin.getItem(i, 0);
			targetScale.setItem(i, 0, t == 0.0 ? 1.0 : t);
		}
	}
	
	protected boolean isAdjusted() {
		Statistics stat = new Statistics();

		OutputType sourceTransformed = createTemporaryTargetObject();
		// Determine correctness of source data (items array)
		for (Map.Entry<InputType, OutputType> item : items) {
			// Compute for all points, so no item.isBad check
			InputType source = item.getKey();
			transformer.transform(source, sourceTransformed);
			// Compute distance between target and sourceTransformed
			double sum2 = 0;
			for (int i = outputSize - 1; i >= 0; i--) {
				double d = transformer.getTargetCoord(item.getValue(), i) - transformer.getTargetCoord(sourceTransformed, i);
				sum2 += d * d;
			}
			setDiscrepancy(item, Math.sqrt(sum2));
		}
		
		boolean iterationHasBad = false;
		for (int k = 0; k < 3; k++) {
			stat.start();
			for (Map.Entry<InputType, OutputType> item : items) {
				if (!isBad(item)) {
					stat.addValue(getDiscrepancy(item), getWeight(item));
				}
			}
			stat.stop();
			iterationHasBad = false;
			for (Map.Entry<InputType, OutputType> item : items) {
				if ((!isBad(item)) && stat.isBad(getDiscrepancy(item))) {
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
		for (Map.Entry<InputType, OutputType> item : items) {
			boolean oldIsBad = isBad(item);
			boolean curIsBad = stat.isBad(getDiscrepancy(item));
			if (oldIsBad == curIsBad) {
				setBad(item, curIsBad);
				adjusted = false;
			}
		}
		return adjusted;
	}

//	private static final double MAX_WEIGHT = 100.0;
//	private static final double MAX_WEIGHT_INVERTED = 1.0 / MAX_WEIGHT;
//	/**
//	 * Re-computes the weight of <b>all</b> points using the inverted distance
//	 * (discrepancy) between transformed source and target points. 
//	 * The formula is:
//	 * MAX_WEIGHT = 100
//	 * discrepancy = Sqrt(Sum(Pow(target[i] - tramsformer.transform(source[i]))))
//	 * weight = discrepancy >= (1/MAX_WEIGHT) ? MAX_WEIGHT : 1/discrepancy 
//	 */
//	public void recomputeWeights() {
//		for (PointsPair item : items) {
//			item.setWeight(item.getDiscrepancy() >= MAX_WEIGHT_INVERTED ? MAX_WEIGHT : 1.0 / item.getDiscrepancy());
//		}
//	}
	
	/**
	 * Computes the maximum absolute difference between each 
	 * target point and the transformed source point.
	 * The formula is:
	 * result[i,0] = Max(Abs(items(k).target[i,0] - transformer.transform(items(k).source)[i,0])) 
	 */
	public Matrix computeTransformedTargetDelta(boolean ignoreBad) {
		Matrix result = new Matrix(outputSize, 1);
		OutputType sourceTransformed = createTemporaryTargetObject();
		result.make0();
		for (Map.Entry<InputType, OutputType> item : items) {
			if (isBad(item) && ignoreBad)
				continue;
			InputType source = item.getKey();
			transformer.transform(source, sourceTransformed);
			for (int i = outputSize - 1; i >= 0; i--) {
				double d = transformer.getTargetCoord(item.getValue(), i) - transformer.getTargetCoord(sourceTransformed, i);
				if (d > result.getItem(i, 0))
					result.setItem(i, 0, d);
			}
		}
		return result;
	}	
}