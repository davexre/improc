package com.slavi.ann.test.v2;

import com.slavi.math.matrix.Matrix;

public class FullyConnectedLayer extends Layer {
	Matrix weight;
	double learningRate;
	double scale;

	public FullyConnectedLayer(int sizeInput, int sizeOutput, double learningRate) {
		this.learningRate = learningRate;
		weight = new Matrix(sizeInput, sizeOutput);
		scale = 1.0;
		fillWeight(weight, 0.3);
	}
	
	@Override
	public LayerWorkspace createWorkspace() {
		return new LayerWorkspace();
	}

	@Override
	public void applyWorkspace(Workspace workspace) {
		LayerWorkspace ws = (LayerWorkspace) workspace;
		ws.dW.mSum(weight, weight);
		ws.resetEpoch();
	}

	protected class LayerWorkspace extends Workspace {
		protected Matrix input;
		protected Matrix inputError;
		protected Matrix output;
		protected Matrix dW;
		protected int dCount;

		protected LayerWorkspace() {
			input = null;
			int sizeInput = weight.getSizeX();
			int sizeOutput = weight.getSizeY();
			inputError = new Matrix(sizeInput, 1);
			output = new Matrix(sizeOutput, 1);
			dW = new Matrix(sizeInput, sizeOutput);
			dCount = 0;
		}

		@Override
		public Matrix feedForward(Matrix input) {
			if (input.getVectorSize() != weight.getSizeX())
				throw new Error("Invalid argument");
			this.input = input;
			for (int j = weight.getSizeY() - 1; j >= 0; j--) {
				double r = 0.0;
				for (int i = weight.getSizeX() - 1; i >= 0; i--) {
					r += input.getVectorItem(i) * weight.getItem(i, j);
				}
				r = 1.0 / (1.0 + Math.exp(-r * scale));
				output.setVectorItem(j, r);
			}
			return output;
		}

		@Override
		public Matrix backPropagate(Matrix error) {
			if (input == null)
				throw new Error("Invalid state");
			if (error.getVectorSize() != weight.getSizeY())
				throw new Error("Invalid argument");
			inputError.make0();
			for (int j = weight.getSizeY() - 1; j >= 0; j--) {
				double r = output.getVectorItem(j);
				r = scale * error.getVectorItem(j) * r * (1 - r);
				for (int i = weight.getSizeX() - 1; i >= 0; i--) {
					double dw = r * input.getVectorItem(i) * learningRate;
					inputError.vectorItemAdd(i, r * weight.getItem(i, j));
					dW.itemAdd(i, j, -dw); // the w-dw means descent, while w+dw means ascent (maximize the error)
				}
			}
			input = null;
			dCount++;
			return inputError;
		}

		@Override
		protected void resetEpoch() {
			dW.make0();
			dCount = 0;
		}
	}
}
