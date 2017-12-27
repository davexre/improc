package com.slavi.ann.test.v2.connection;

import com.slavi.ann.test.BellCurveDistribution;
import com.slavi.ann.test.v2.Layer;
import com.slavi.math.matrix.Matrix;

public class FullyConnectedLayer extends Layer {
	public Matrix weight;
	public double learningRate;

	public FullyConnectedLayer(int sizeInput, int sizeOutput, double learningRate) {
		this.learningRate = learningRate;
		weight = new Matrix(sizeInput, sizeOutput);
		BellCurveDistribution.fillWeight(weight, 0.3);
	}

	public int[] getOutputSize(int inputSize[]) {
		if (inputSize[0] * inputSize[1]  != weight.getSizeX())
			throw new Error("Invalid argument");
		return new int[] { weight.getSizeY(), 1 };
	}
	
	@Override
	public Workspace createWorkspace() {
		return new Workspace();
	}

	@Override
	public void applyWorkspace(LayerWorkspace workspace) {
		Workspace ws = (Workspace) workspace;
		ws.dW.mSum(weight, weight);
		ws.resetEpoch();
	}

	public class Workspace extends LayerWorkspace {
		public Matrix input;
		public Matrix inputError;
		public Matrix output;
		public Matrix dW;

		protected Workspace() {
			input = null;
			int sizeInput = weight.getSizeX();
			int sizeOutput = weight.getSizeY();
			inputError = new Matrix(sizeInput, 1);
			output = new Matrix(sizeOutput, 1);
			dW = new Matrix(sizeInput, sizeOutput);
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
			inputError.resize(input.getSizeX(), input.getSizeY());
			inputError.make0();
			
			for (int j = weight.getSizeY() - 1; j >= 0; j--) {
				double r = error.getVectorItem(j);
				for (int i = weight.getSizeX() - 1; i >= 0; i--) {
					double dw = r * input.getVectorItem(i) * learningRate;
					inputError.vectorItemAdd(i, r * weight.getItem(i, j));
					dW.itemAdd(i, j, -dw); // the w-dw means descent, while w+dw means ascent (maximize the error)
				}
			}
			return inputError;
		}

		@Override
		protected void resetEpoch() {
			dW.make0();
		}

		public String toString() {
			return new StringBuilder()
					.append("weight\n").append(weight)
					.append("dWeight\n").append(dW)
					.append("output\n").append(output)
					.toString();
		}
	}
}
