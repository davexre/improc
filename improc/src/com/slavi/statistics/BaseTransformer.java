package com.slavi.statistics;

import com.slavi.matrix.Matrix;

public abstract class BaseTransformer {
	protected int inputSize;
	
	protected int outputSize;

	public abstract void transform(Matrix source, Matrix dest);

	public int getInputSize() {
		return inputSize;
	}

	public int getOutputSize() {
		return outputSize;
	}
	
	public abstract int getNumberOfCoefsPerCoordinate();
}
