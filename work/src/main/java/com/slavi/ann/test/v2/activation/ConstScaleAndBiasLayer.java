package com.slavi.ann.test.v2.activation;

import com.slavi.ann.test.v2.Layer;
import com.slavi.math.matrix.Matrix;

public class ConstScaleAndBiasLayer extends Layer {

	double scale;
	double bias;

	protected ConstScaleAndBiasLayer() {}

	public ConstScaleAndBiasLayer(double scale, double bias) {
		this.scale = scale;
		this.bias = bias;
	}

	@Override
	public int[] getOutputSize(int[] inputSize) {
		return inputSize;
	}

	@Override
	public LayerWorkspace createWorkspace() {
		return new Workspace();
	}

	@Override
	public String toString() {
		return String.format("scale: %.4f, bias: %.4f", scale, bias);
	}

	public class Workspace extends LayerWorkspace {
		public Matrix inputError = new Matrix();
		public Matrix output = new Matrix();

		@Override
		public Matrix feedForward(Matrix input) {
			output.resize(input.getSizeX(), input.getSizeY());
			for (int i = input.getVectorSize() - 1; i >= 0; i--) {
				output.setVectorItem(i, input.getVectorItem(i) * scale + bias);
			}
			return output;
		}

		@Override
		public Matrix backPropagate(Matrix coefs, int startingIndex, Matrix error) {
			if (error.getVectorSize() != output.getVectorSize())
				throw new Error("Invalid argument");
			inputError.resize(output.getSizeX(), output.getSizeY());
			for (int i = output.getVectorSize() - 1; i >= 0; i--) {
				inputError.setVectorItem(i, error.getVectorItem(i) * scale);
			}
			return inputError;
		}
	}
}
