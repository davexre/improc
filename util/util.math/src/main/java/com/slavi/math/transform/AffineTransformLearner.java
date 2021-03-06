package com.slavi.math.transform;

import java.util.Map;

import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;

public abstract class AffineTransformLearner<InputType, OutputType> extends BaseTransformLearner<InputType, OutputType> {

	protected Matrix coefs;			// Temporary matrix, used by computeOne methods
	protected LeastSquaresAdjust lsa;
	
	public AffineTransformLearner(AffineTransformer<InputType, OutputType> transformer, 
			Iterable<? extends Map.Entry<InputType, OutputType>> pointsPairList) {
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
	public TransformLearnerResult calculateOne() {
		TransformLearnerResult result = new TransformLearnerResult();
		result.minGoodRequired = lsa.getRequiredPoints();
		startNewIteration(result);

		if (result.oldGoodCount < result.minGoodRequired)
			return result;

		// Calculate the affine transform parameters 
		lsa.clear();
		for (Map.Entry<InputType, OutputType> item : items) {
			if (isBad(item))
				continue;

			InputType source = item.getKey();
			OutputType dest = item.getValue();
			for (int i = inputSize - 1; i >= 0; i--) {
				coefs.setItem(i, 0, transformer.getSourceCoord(source, i));
			}
			coefs.setItem(inputSize, 0, 1.0);
			double weight = getWeight(item);
			for (int i = outputSize - 1; i >= 0; i--) {
				double L = transformer.getTargetCoord(dest, i);
				lsa.addMeasurement(coefs, weight, L, i);
			}
		}
		if (!lsa.calculate()) 
			return result;

		// Build transformer
		AffineTransformer<InputType, OutputType> tr = (AffineTransformer<InputType, OutputType>)transformer;
		Matrix u = lsa.getUnknown(); 

		for (int i = outputSize - 1; i >= 0; i--) {
			for (int j = inputSize - 1; j >= 0; j--) {
				tr.affineCoefs.setItem(i, j, 
					u.getItem(i, j));
			}
			tr.origin.setItem(i, 0, 
				u.getItem(i, inputSize)); 
		}

		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result; 
	}
}
