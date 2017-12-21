package com.slavi.ann.test;

import com.slavi.math.matrix.Matrix;

public interface DatapointPair {
	public void toInputMatrix(Matrix dest);
	
	public void toOutputMatrix(Matrix dest);
}
