package com.slavi.math.transform;

import java.util.ArrayList;

import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;

public class PolynomialTransformLearner extends BaseTransformLearner {

	protected Matrix coefs;			// Temporary matrix, used by computeOne methods
	protected LeastSquaresAdjust lsa;

	public PolynomialTransformLearner(int polynomPower, int inputSize, int outputSize) {
		this(polynomPower, inputSize, outputSize, new ArrayList<PointsPair>());
	}
	
	public PolynomialTransformLearner(int polynomPower, int inputSize, int outputSize, 
			ArrayList<? extends PointsPair> pointsPairList) {
		super(
			new PolynomialTransformer(polynomPower, inputSize, outputSize), 
			pointsPairList);

		int numberOfCoefsPerCoordinate = transformer.getNumberOfCoefsPerCoordinate();
		this.coefs = new Matrix(numberOfCoefsPerCoordinate, 1);
		this.lsa = new LeastSquaresAdjust(numberOfCoefsPerCoordinate, transformer.outputSize);		
	}
	
	public int getRequiredTrainingPoints() {
		return (int) Math.ceil(Math.pow(((PolynomialTransformer)transformer).polynomPower, transformer.inputSize) * 
				transformer.outputSize / transformer.inputSize);
	}
	
	public static int getRequiredTrainingPoints(int polynomPower, int inputSize, int outputSize) {
		return (int) Math.ceil(Math.pow(polynomPower, inputSize) * 
				outputSize / inputSize);
	}
	
	/**
	 * 
	 * @return True if adjusted. False - Try again/more adjustments needed.
	 */
	public boolean calculateOne() {
		int goodCount = computeWeights();
		
		if (goodCount < lsa.getRequiredPoints())
			throw new IllegalArgumentException("Not enough good points");

		computeScaleAndOrigin();

		PolynomialTransformer tr = (PolynomialTransformer)transformer;
		Matrix tmpS = new Matrix(tr.inputSize, 1);
		Matrix tmpT = new Matrix(tr.outputSize, 1);
		
		lsa.clear();
		for (PointsPair item : items) {
			if (item.isBad())
				continue;

			for (int i = tr.inputSize - 1; i >= 0; i--)
				tmpS.setItem(i, 0, item.getSourceCoord(i) - sourceOrigin.getItem(i, 0));
			for (int i = tr.outputSize - 1; i >= 0; i--)
				tmpT.setItem(i, 0, item.getTargetCoord(i) - targetOrigin.getItem(i, 0));
			tmpS.termDiv(sourceScale, tmpS);
			tmpT.termDiv(targetScale, tmpT);
			for (int j = tr.numPoints - 1; j >= 0; j--) {
				double tmp = 1;
				for (int i = tr.inputSize - 1; i >= 0; i--) {
					tmp *= Math.pow(tmpS.getItem(i, 0), tr.polynomPowers.getItem(i, j));
				}
				coefs.setItem(j, 0, tmp);
			}
			double computedWeight = getComputedWeight(item);
			for (int i = tr.outputSize - 1; i >= 0; i--) {
				double L = tmpT.getItem(i, 0);						
				lsa.addMeasurement(coefs, computedWeight, L, i);
			}
		}
		lsa.calculate();

		// Build transformer
		Matrix u = lsa.getUnknown();

		for (int i = tr.outputSize - 1; i >= 0; i--) {
			double t = targetScale.getItem(i, 0);
			for (int j = tr.numPoints - 1; j >= 0; j--) {
				u.setItem(i, j, u.getItem(i, j) * t);
			}
		}
		
		for (int j = tr.numPoints - 1; j >= 0; j--) {
			double t = 1;
			for (int i = tr.inputSize - 1; i >= 0; i--) {
				t *= Math.pow(sourceScale.getItem(i, 0), tr.polynomPowers.getItem(i, j));
			}
			for (int i = tr.outputSize - 1; i >= 0; i--) {
				u.setItem(i, j, u.getItem(i, j) / t);
			}
		}

		for (int i = tr.outputSize - 1; i >= 0; i--) {
			u.setItem(i, 0, u.getItem(i, 0) + targetOrigin.getItem(i, 0));
		}
		
		u.copyTo(tr.polynomCoefs);
		sourceOrigin.copyTo(tr.sourceOrigin);
		
		return isAdjusted();
	}
}
