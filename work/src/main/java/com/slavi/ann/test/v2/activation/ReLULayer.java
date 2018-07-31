package com.slavi.ann.test.v2.activation;

import com.slavi.ann.test.v2.Layer;
import com.slavi.math.matrix.Matrix;

/*
Rectifier function derivation
=============================
https://en.wikipedia.org/wiki/Rectifier_(neural_networks)
f(x) = max(0, x)
df/dx = x > 0 ? 1 : 0

A smooth approximation (Softplus)
s(x) = log(1 + exp(x))
ds/dx = exp(x) / (1 + exp(x)) = 1 / (1 + exp(-x))
ds/dx -> logistic function -> https://en.wikipedia.org/wiki/Logistic_function

*/
public class ReLULayer extends Layer {
	@Override
	public LayerParameters getLayerParams(LayerParameters inputLayerParameters) {
		return inputLayerParameters;
	}

	@Override
	public LayerWorkspace createWorkspace() {
		return new Workspace();
	}

	public class Workspace extends LayerWorkspace {
		public Matrix input;
		public Matrix inputError = new Matrix();
		public Matrix output = new Matrix();

		@Override
		public Matrix feedForward(Matrix input) {
			this.input = input;
			output.resize(input.getSizeX(), input.getSizeY());
			for (int i = input.getVectorSize() - 1; i >= 0; i--) {
				double r = input.getVectorItem(i);
				output.setVectorItem(i, r > 0 ? r : 0);
			}
			return output;
		}

		@Override
		public Matrix backPropagate(Matrix error) {
			if (input == null)
				throw new Error("Invalid state");
			if (error.getVectorSize() != input.getVectorSize())
				throw new Error("Invalid argument");
			inputError.resize(input.getSizeX(), input.getSizeY());
			for (int i = input.getVectorSize() - 1; i >= 0; i--) {
				double r = input.getVectorItem(i);
				inputError.setVectorItem(i, r > 0 ? error.getVectorItem(i) : 0);
			}
			return inputError;
		}
	}
}
