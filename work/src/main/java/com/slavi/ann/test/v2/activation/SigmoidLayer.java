package com.slavi.ann.test.v2.activation;

import com.slavi.ann.test.v2.Layer;
import com.slavi.math.matrix.Matrix;

/*
Sigmoid function derivation
===========================
https://math.stackexchange.com/questions/78575/derivative-of-sigmoid-function-sigma-x-frac11e-x
https://en.wikipedia.org/wiki/Gradient_descent -> guaranteed convergence to local minimum
s(x) = 1 / (1+e^(-x))
ds/dx = ?

Let
f(x) = 1/s = 1+e^(-x)
df/dx = d(1/s)/dx = -(1/(s^2))*ds/dx
df/dx = d(1+e^(-x))/dx = -e^(-x) = 1-f = 1-1/s = (s-1)/s
-(1/(s^2))*ds/dx = (s-1)/s
ds/dx = s(1-s)
*/
public class SigmoidLayer extends Layer {
	@Override
	public int[] getOutputSize(int[] inputSize) {
		return inputSize;
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
				output.setVectorItem(i, 1 / (1 + Math.exp(-input.getVectorItem(i))));
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
				double r = output.getVectorItem(i);
				inputError.setVectorItem(i, error.getVectorItem(i) * r * (1 - r));
			}
			return inputError;
		}
	}
}
