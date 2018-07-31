package com.slavi.ann.test.v2.activation;

import com.slavi.ann.test.v2.Layer;
import com.slavi.math.matrix.Matrix;

public class ScaleAndBiasLayer extends Layer {

	Matrix scale;
	Matrix bias;

	public ScaleAndBiasLayer(Matrix scale, Matrix bias) {
		this.scale = scale;
		this.bias = bias;
	}

	@Override
	public LayerParameters getLayerParams(LayerParameters inputLayerParameters) {
		return inputLayerParameters;
	}

	@Override
	public LayerWorkspace createWorkspace() {
		return new Workspace();
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append("scale\n").append(scale)
				.append("bias\n").append(bias)
				.toString();
	}

	public class Workspace extends LayerWorkspace {
		public Matrix inputError = new Matrix();
		public Matrix output = new Matrix();

		@Override
		public Matrix feedForward(Matrix input) {
			output.resize(input.getSizeX(), input.getSizeY());
			for (int i = input.getVectorSize() - 1; i >= 0; i--) {
				double d = input.getVectorItem(i);
				if (scale != null)
					d *= scale.getVectorItem(i);
				if (bias != null)
					d += bias.getVectorItem(i);
				output.setVectorItem(i, d);
			}
			return output;
		}

		@Override
		public Matrix backPropagate(Matrix error) {
			if (error.getVectorSize() != output.getVectorSize())
				throw new Error("Invalid argument");
			if (scale != null) {
				inputError.resize(output.getSizeX(), output.getSizeY());
				for (int i = output.getVectorSize() - 1; i >= 0; i--) {
					inputError.setVectorItem(i, error.getVectorItem(i) * scale.getVectorItem(i));
				}
				return inputError;
			} else {
				return error;
			}
		}
	}
}
