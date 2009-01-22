package com.slavi.math.transform;

import java.util.Map;

import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;

public abstract class AffineTransformLearner<InputType, OutputType> extends BaseTransformLearner<InputType, OutputType> {

	protected Matrix coefs;			// Temporary matrix, used by computeOne methods
	protected LeastSquaresAdjust lsa;
	
	public AffineTransformLearner(AffineTransformer<InputType, OutputType> transformer, 
			Iterable<Map.Entry<InputType, OutputType>> pointsPairList) {
		super(transformer, pointsPairList);

		int numberOfCoefsPerCoordinate = transformer.getNumberOfCoefsPerCoordinate();
		this.coefs = new Matrix(numberOfCoefsPerCoordinate, 1);
		this.lsa = new LeastSquaresAdjust(numberOfCoefsPerCoordinate, transformer.getOutputSize());		
	}
	
	public int getRequiredTrainingPoints() {
		return transformer.getInputSize() + 1;
	}	

	public static int getRequiredTrainingPoints(int inputSize) {
		return inputSize + 1;
	}	

	/**
	 * 
	 * @return True if adjusted. False - Try again/more adjustments needed.
	 */
	public boolean calculateOne() {
		int inputSize = transformer.getInputSize();
		int outputSize = transformer.getOutputSize();
		int goodCount = computeWeights();

		if (goodCount < lsa.getRequiredPoints())
			return false;

		computeScaleAndOrigin();

		// Calculate the affine transform parameters 
		lsa.clear();
		for (Map.Entry<InputType, OutputType> item : items) {
			if (isBad(item))
				continue;

			InputType source = item.getKey();
			OutputType dest = item.getValue();
			for (int i = inputSize - 1; i >= 0; i--) {
				coefs.setItem(i, 0, (transformer.getSourceCoord(source, i) - sourceOrigin.getItem(i, 0)) /
					sourceScale.getItem(i, 0));
			}
			coefs.setItem(inputSize, 0, 1.0);
			double computedWeight = getComputedWeight(item);
			for (int i = outputSize - 1; i >= 0; i--) {
				double L = (transformer.getTargetCoord(dest, i) - targetOrigin.getItem(i, 0)) / 
					targetScale.getItem(i, 0);
				lsa.addMeasurement(coefs, computedWeight, L, i);
			}
		}
		if (!lsa.calculate()) 
			return false;

		// Build transformer
		AffineTransformer tr = (AffineTransformer)transformer;
		Matrix u = lsa.getUnknown(); 

		for (int i = outputSize - 1; i >= 0; i--) {
			for (int j = inputSize - 1; j >= 0; j--) {
				tr.affineCoefs.setItem(i, j, 
					u.getItem(i, j) * targetScale.getItem(i, 0) / sourceScale.getItem(j, 0));
			}
			double t = 0;
			for (int j = inputSize - 1; j >= 0; j--) { 
				t += tr.affineCoefs.getItem(i, j) * sourceOrigin.getItem(j, 0);
			}
			tr.origin.setItem(i, 0, targetOrigin.getItem(i, 0) - t +
				u.getItem(i, inputSize) * targetScale.getItem(i, 0)); 
		}

		return isAdjusted();
	}
}
