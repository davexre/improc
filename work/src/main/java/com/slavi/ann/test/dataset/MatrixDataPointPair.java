package com.slavi.ann.test.dataset;

import com.slavi.ann.test.DatapointPair;
import com.slavi.math.matrix.Matrix;

public class MatrixDataPointPair implements DatapointPair {
	public String name;
	public Matrix input;
	public Matrix output;

	@Override
	public void toInputMatrix(Matrix dest) {
		input.copyTo(dest);
	}

	@Override
	public void toOutputMatrix(Matrix dest) {
		output.copyTo(dest);
	}

	@Override
	public String getName() {
		return name;
	}
}
