package com.slavi.ann.test.v2.test;

import com.slavi.ann.test.DatapointPair;
import com.slavi.ann.test.v2.Layer;
import com.slavi.ann.test.v2.Layer.Workspace;
import com.slavi.math.matrix.Matrix;

public class Trainer {

	public void train(Layer l, Iterable<DatapointPair> trainset, int maxEpochs) {
		Matrix input = new Matrix();
		Matrix target = new Matrix();
		Workspace ws = l.createWorkspace();
		for (int epoch = 0; epoch < maxEpochs; epoch++) {
			for (DatapointPair i : trainset) {
				i.toInputMatrix(input);
				i.toOutputMatrix(target);
				Matrix output = ws.feedForward(input);
				
			}
		}
	}
	
}
