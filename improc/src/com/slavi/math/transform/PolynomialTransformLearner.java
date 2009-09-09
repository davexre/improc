package com.slavi.math.transform;

import java.util.Map;

import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;

public abstract class PolynomialTransformLearner<InputType, OutputType> extends BaseTransformLearner<InputType, OutputType> {

	protected Matrix coefs;			// Temporary matrix, used by computeOne methods
	protected LeastSquaresAdjust lsa;

	public PolynomialTransformLearner(PolynomialTransformer<InputType, OutputType> transformer, 
			Iterable<? extends Map.Entry<InputType, OutputType>> pointsPairList) {
		super(transformer, pointsPairList);
		int numberOfCoefsPerCoordinate = transformer.getNumberOfCoefsPerCoordinate();
		this.coefs = new Matrix(numberOfCoefsPerCoordinate, 1);
		this.lsa = new LeastSquaresAdjust(numberOfCoefsPerCoordinate, transformer.getOutputSize());		
	}
	
	public int getRequiredTrainingPoints() {
		int inputSize = transformer.getInputSize();
		int outputSize = transformer.getOutputSize();
		return (int) Math.ceil(Math.pow(((PolynomialTransformer<InputType, OutputType>)transformer).polynomPower, inputSize) * 
				outputSize / inputSize);
	}
	
	public static int getRequiredTrainingPoints(int polynomPower, int inputSize, int outputSize) {
		return (int) Math.ceil(Math.pow(polynomPower, inputSize) * 
				outputSize / inputSize);
	}
	
	/**
	 * 
	 * @return True if adjusted. False - Try again/more adjustments needed.
	 */
	public TransformLearnerResult calculateOne() {
		TransformLearnerResult result = new TransformLearnerResult();
		
		int inputSize = transformer.getInputSize();
		int outputSize = transformer.getOutputSize();
		result.minGoodRequired = lsa.getRequiredPoints();
		computeWeights(result);

		if (result.oldGoodCount < result.minGoodRequired)
			return result;

		computeScaleAndOrigin();

		PolynomialTransformer<InputType, OutputType> tr = (PolynomialTransformer<InputType, OutputType>)transformer;
		Matrix tmpS = new Matrix(inputSize, 1);
		Matrix tmpT = new Matrix(outputSize, 1);
		
		lsa.clear();
		for (Map.Entry<InputType, OutputType> item : items) {
			if (isBad(item))
				continue;

			InputType source = item.getKey();
			OutputType dest = item.getValue();
			for (int i = inputSize - 1; i >= 0; i--)
				tmpS.setItem(i, 0, transformer.getSourceCoord(source, i) - sourceOrigin.getItem(i, 0));
			for (int i = outputSize - 1; i >= 0; i--)
				tmpT.setItem(i, 0, transformer.getTargetCoord(dest, i) - targetOrigin.getItem(i, 0));
			tmpS.termDiv(sourceScale, tmpS);
			tmpT.termDiv(targetScale, tmpT);
			for (int j = tr.numPoints - 1; j >= 0; j--) {
				double tmp = 1;
				for (int i = inputSize - 1; i >= 0; i--) {
					tmp *= Math.pow(tmpS.getItem(i, 0), tr.polynomPowers.getItem(i, j));
				}
				coefs.setItem(j, 0, tmp);
			}
			double computedWeight = getComputedWeight(item);
			for (int i = outputSize - 1; i >= 0; i--) {
				double L = tmpT.getItem(i, 0);						
				lsa.addMeasurement(coefs, computedWeight, L, i);
			}
		}
		if (!lsa.calculate())
			return result;

		// Build transformer
		Matrix u = lsa.getUnknown();

		for (int i = outputSize - 1; i >= 0; i--) {
			double t = targetScale.getItem(i, 0);
			for (int j = tr.numPoints - 1; j >= 0; j--) {
				u.setItem(i, j, u.getItem(i, j) * t);
			}
		}
		
		for (int j = tr.numPoints - 1; j >= 0; j--) {
			double t = 1;
			for (int i = inputSize - 1; i >= 0; i--) {
				t *= Math.pow(sourceScale.getItem(i, 0), tr.polynomPowers.getItem(i, j));
			}
			for (int i = outputSize - 1; i >= 0; i--) {
				u.setItem(i, j, u.getItem(i, j) / t);
			}
		}

		for (int i = outputSize - 1; i >= 0; i--) {
			u.setItem(i, 0, u.getItem(i, 0) + targetOrigin.getItem(i, 0));
		}
		
		u.copyTo(tr.polynomCoefs);
		sourceOrigin.copyTo(tr.sourceOrigin);
		
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result; 
	}
}
