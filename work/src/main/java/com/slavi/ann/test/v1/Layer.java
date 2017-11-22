package com.slavi.ann.test.v1;

import com.slavi.math.matrix.Matrix;

public interface Layer {
	void eraseMemory();
	void resetEpoch();
	Matrix feedForward(Matrix input);
	Matrix backPropagate(Matrix error);
	void applyTraining();
}
