package com.slavi.math.transform;

import java.util.Map;

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
	
	public Iterable<? extends Map.Entry<InputType, OutputType>> items;
	
	protected int iteration = 0; 
	
	protected BaseTransformLearner(BaseTransformer<InputType, OutputType> transformer, 
			Iterable<? extends Map.Entry<InputType, OutputType>> pointsPairList) {
		this.transformer = transformer;
		inputSize = transformer.getInputSize();
		outputSize = transformer.getOutputSize();
		this.items = pointsPairList;
	}
	
	public abstract TransformLearnerResult calculateOne();
	
	public abstract int getRequiredTrainingPoints();

	protected void startNewIteration(TransformLearnerResult result) {
		result.iteration = ++iteration;
		result.dataCount = 0;
		result.oldBadCount = 0;
		result.oldGoodCount = 0;
		
		for (Map.Entry<InputType, OutputType> item : items) {
			result.dataCount++;
			if (isBad(item)) {
				result.oldBadCount++;
			} else {
				result.oldGoodCount++;
			}
		}
	}
	
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
				if (Math.abs(d) > Math.abs(result.getItem(i, 0)))
					result.setItem(i, 0, d);
			}
		}
		return result;
	}	

	public void computeDiscrepancies(TransformLearnerResult result) {
		OutputType sourceTransformed = createTemporaryTargetObject();
		result.discrepancyStatistics.start();
		// Determine correctness of source data (items array)
		for (Map.Entry<InputType, OutputType> item : items) {
			// Compute for all points, so no item.isBad check
			InputType source = item.getKey();
			OutputType target = item.getValue();
			transformer.transform(source, sourceTransformed);
			// Compute distance between target and sourceTransformed
			double sum2 = 0;
			for (int i = outputSize - 1; i >= 0; i--) {
				double d = transformer.getTargetCoord(target, i) - transformer.getTargetCoord(sourceTransformed, i);
				sum2 += d * d;
			}
			setDiscrepancy(item, Math.sqrt(sum2));
			if (!isBad(item)) {
				result.discrepancyStatistics.addValue(getDiscrepancy(item), 1);
//				result.discrepancyStatistics.addValue(getDiscrepancy(item), getWeight(item));
			}
		}
		result.discrepancyStatistics.stop();
	}
	
	public double getDiscrepancyThreshold(TransformLearnerResult result) {
		return 0;
	}
	
	public double getRecoverDiscrepancy(TransformLearnerResult result) {
		return result.discrepancyStatistics.getAvgValue();
	}
	
	public double getMaxAllowedDiscrepancy(TransformLearnerResult result) {
		return Math.min(result.discrepancyStatistics.getJ_End(),
				(result.discrepancyStatistics.getAvgValue() + 
				result.discrepancyStatistics.getAvgValue() +
				result.discrepancyStatistics.getMaxX()) / 3.0);
	}
	
	protected void computeBad(TransformLearnerResult result) {
		result.newBadCount = 0;
		result.newGoodCount = 0;
		result.oldGoodNowBad = 0;
		result.oldBadNowGood = 0;
		result.discrepancyThreshold = getDiscrepancyThreshold(result);
		result.maxAllowedDiscrepancy = Math.max(getMaxAllowedDiscrepancy(result), result.discrepancyThreshold);
		result.recoverDiscrepancy = Math.min(getRecoverDiscrepancy(result), result.maxAllowedDiscrepancy);
		
		for (Map.Entry<InputType, OutputType> item : items) {
			boolean oldIsBad = isBad(item);
			double discrepancy = getDiscrepancy(item);
			boolean curIsBad = discrepancy > result.maxAllowedDiscrepancy;
			if (oldIsBad != curIsBad) {
				if (curIsBad) {
					setBad(item, curIsBad);
					result.oldGoodNowBad++;
				} else {
					if (discrepancy < result.recoverDiscrepancy) {
						setBad(item, curIsBad);
						result.oldBadNowGood++;
					} else {
						curIsBad = true; // rejected to be recovered
					}
				}
			}
			if (curIsBad) {
				result.newBadCount++;
			} else {
				result.newGoodCount++;
			}
		}
	}
}
