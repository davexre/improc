package com.slavi.ann.test.v2;

import com.slavi.ann.test.BellCurveDistribution;
import com.slavi.math.matrix.Matrix;

public class FullyConnectedLayer extends Layer {
	public Matrix weight;
	public double learningRate;
	public double scale;
	public double bias;

	public FullyConnectedLayer(int sizeInput, int sizeOutput, double learningRate) {
		this.learningRate = learningRate;
		weight = new Matrix(sizeInput, sizeOutput);
		BellCurveDistribution.fillWeight(weight, 0.3);
//		bias = 0.5;
		scale = 1;
		scale = 5.0 / sizeOutput;
//		weight.rMul(scale);
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

	public class LayerWorkspace extends Workspace {
		public Matrix input;
		public Matrix inputError;
		public Matrix output;
		public Matrix output0;
		public Matrix dW;
		public Matrix dW0;
		public int dCount;

		protected LayerWorkspace() {
			input = null;
			int sizeInput = weight.getSizeX();
			int sizeOutput = weight.getSizeY();
			inputError = new Matrix(sizeInput, 1);
			output = new Matrix(sizeOutput, 1);
			output0 = new Matrix(sizeOutput, 1);
			dW = new Matrix(sizeInput, sizeOutput);
			dW0 = new Matrix(sizeInput, sizeOutput);
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
				output0.setVectorItem(j, bias - r * scale);
				r = 1.0 / (1.0 + Math.exp(bias - r * scale));
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
			dW0.make0();
			for (int j = weight.getSizeY() - 1; j >= 0; j--) {
				double r = output.getVectorItem(j);
				r =  scale * error.getVectorItem(j) * r * (1 - r);
				for (int i = weight.getSizeX() - 1; i >= 0; i--) {
					double dw = r * input.getVectorItem(i) * learningRate;
					inputError.vectorItemAdd(i, r * weight.getItem(i, j));
					dW.itemAdd(i, j, -dw); // the w-dw means descent, while w+dw means ascent (maximize the error)
					dW0.itemAdd(i, j, -dw);
				}
			}
//			input = null;
			dCount++;
			return inputError;
		}

		@Override
		protected void resetEpoch() {
			dW.make0();
			dCount = 0;
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
