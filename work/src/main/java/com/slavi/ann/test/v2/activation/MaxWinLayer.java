package com.slavi.ann.test.v2.activation;

import com.slavi.ann.test.Utils;
import com.slavi.ann.test.v2.Layer;
import com.slavi.math.matrix.Matrix;

public class MaxWinLayer extends Layer {
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
		public double maxVal;

		@Override
		public Matrix feedForward(Matrix input) {
			this.input = input;
			output.resize(input.getSizeX(), input.getSizeY());
			maxVal = Double.MIN_VALUE;
			for (int i = input.getVectorSize() - 1; i >= 0; i--) {
				double r = input.getVectorItem(i);
				if (maxVal < r)
					maxVal = r;
			}
			for (int i = input.getVectorSize() - 1; i >= 0; i--) {
				double r = input.getVectorItem(i);
				output.setVectorItem(i, maxVal > r ? Utils.valueLow : Utils.valueHigh);
			}
			return output;
		}

		@Override
		public Matrix backPropagate(Matrix coefs, int startingIndex, Matrix error) {
			if (input == null)
				throw new Error("Invalid state");
			if (error.getVectorSize() != input.getVectorSize())
				throw new Error("Invalid argument");
			inputError.resize(input.getSizeX(), input.getSizeY());
			for (int i = input.getVectorSize() - 1; i >= 0; i--) {
				double r = input.getVectorItem(i);
				inputError.setVectorItem(i, maxVal > r ? 0 : error.getVectorItem(i));
			}
			return inputError;
		}
	}
}
