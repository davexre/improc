package com.slavi.statistics;

import java.util.ArrayList;

import com.slavi.matrix.Matrix;

public class AffineTransformLearner extends BaseTransformLearner {

	protected Matrix coefs;			// Temporary matrix, used by computeOne methods
	protected LeastSquaresAdjust lsa;
	
	public AffineTransformLearner(int inputSize, int outputSize) {
		this(inputSize, outputSize, new ArrayList());
	}
	
	public AffineTransformLearner(int inputSize, int outputSize, ArrayList pointsPairList) {
		super(new AffineTransformer(inputSize, outputSize), pointsPairList);

		int numberOfCoefsPerCoordinate = transformer.getNumberOfCoefsPerCoordinate();
		this.coefs = new Matrix(numberOfCoefsPerCoordinate, 1);
		this.lsa = new LeastSquaresAdjust(numberOfCoefsPerCoordinate, transformer.outputSize);		
	}
	
	public int getRequiredTrainingPoints() {
		return transformer.inputSize + 1;
	}	

	public static int getRequiredTrainingPoints(int inputSize) {
		return inputSize + 1;
	}	

	/**
	 * 
	 * @return True if adjusted. False - Try again/more adjustments needed.
	 */
	public boolean calculateOne() {
		int goodCount = computeWeights();

		if (goodCount < lsa.getRequiredPoints())
			throw new Error("Not enough good points");

		computeScaleAndOrigin();

		// Calculate the affine transform parameters 
		lsa.clear();
		for (int p = items.size() - 1; p >= 0; p--) {
			PointsPair item = (PointsPair) items.get(p);
			if (item.bad)
				continue;

			for (int i = transformer.inputSize - 1; i >= 0; i--) {
				coefs.setItem(i, 0, (item.source.getItem(i, 0) - sourceOrigin.getItem(i, 0)) /
					sourceScale.getItem(i, 0));
			}
			coefs.setItem(transformer.inputSize, 0, 1.0);
			for (int i = transformer.outputSize - 1; i >= 0; i--) {
				double L = (item.target.getItem(i, 0) - targetOrigin.getItem(i, 0)) / 
					targetScale.getItem(i, 0);
				lsa.addMeasurement(coefs, item.computedWeight, L, i);
			}
		}
		lsa.calculate();

		// Build transformer
		AffineTransformer tr = (AffineTransformer)transformer;
		Matrix u = lsa.getUnknown(); 

		for (int i = transformer.outputSize - 1; i >= 0; i--) {
			for (int j = transformer.inputSize - 1; j >= 0; j--) {
				tr.affineCoefs.setItem(i, j, 
					u.getItem(i, j) * targetScale.getItem(i, 0) / sourceScale.getItem(j, 0));
			}
			double t = 0;
			for (int j = transformer.inputSize - 1; j >= 0; j--) { 
				t += tr.affineCoefs.getItem(i, j) * sourceOrigin.getItem(j, 0);
			}
			tr.origin.setItem(i, 0, targetOrigin.getItem(i, 0) - t +
				u.getItem(i, transformer.inputSize) * targetScale.getItem(i, 0)); 
		}

		return isAdjusted();
	}
}
