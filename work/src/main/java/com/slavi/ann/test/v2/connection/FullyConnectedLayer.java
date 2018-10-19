package com.slavi.ann.test.v2.connection;

import com.slavi.ann.test.BellCurveDistribution;
import com.slavi.ann.test.v2.Layer;
import com.slavi.math.MathUtil;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public class FullyConnectedLayer extends Layer {
	public Matrix weight;
	public double learningRate;

	protected FullyConnectedLayer() {}

	public FullyConnectedLayer(int sizeInput, int sizeOutput, double learningRate) {
		this.learningRate = learningRate;
		weight = new Matrix(sizeInput, sizeOutput);
		BellCurveDistribution.fillWeight(weight, 0.3);
	}

	public static void normalizeWeights(Matrix weight) {
		for (int j = weight.getSizeY() - 1; j >= 0; j--) {
			double sum = 0;
			for (int i = weight.getSizeX() - 1; i >= 0; i--)
				sum += weight.getItem(i, j);
			if (Math.abs(sum) > MathUtil.eps)
				for (int i = weight.getSizeX() - 1; i >= 0; i--)
					weight.setItem(i, j, weight.getItem(i, j) / sum);
		}
	}

	@Override
	public int[] getOutputSize(int inputSize[]) {
		if (inputSize[0] * inputSize[1]  != weight.getSizeX())
			throw new Error("Invalid argument");
		return new int[] { weight.getSizeY(), 1 };
	}

	@Override
	public void extractParams(Matrix delta, int startingIndex) {
		for (int j = weight.getSizeY() - 1; j >= 0; j--) {
			int coefIndex = startingIndex + j * weight.getSizeX();
			for (int i = weight.getSizeX() - 1; i >= 0; i--) {
				delta.setItem(coefIndex + i, 0, weight.getItem(i, j));
			}
		}
	}

	@Override
	public void loadParams(Matrix delta, int startingIndex) {
		for (int j = weight.getSizeY() - 1; j >= 0; j--) {
			int coefIndex = startingIndex + j * weight.getSizeX();
			for (int i = weight.getSizeX() - 1; i >= 0; i--) {
				double r = delta.getItem(coefIndex + i, 0);
				weight.setItem(i, j, r);
			}
		}
	}

	@Override
	public int getNumAdjustableParams() {
		return weight.getVectorSize();
	};

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

	@Override
	public String toString() {
		return new StringBuilder()
				.append(String.format("learning rate: %.4f\n", learningRate))
				.append("weight\n").append(weight)
				.toString();
	}

	public class Workspace extends LayerWorkspace {
		public Matrix input;
		public Matrix inputError;
		public Matrix output;
		public Matrix outputError;	// Used by com.slavi.ann.test.v2.Utils.DrawFullyConnectedLayer
		public Matrix dW;

		protected Workspace() {
			input = null;
			int sizeInput = weight.getSizeX();
			int sizeOutput = weight.getSizeY();
			inputError = new Matrix(sizeInput, 1);
			output = new Matrix(sizeOutput, 1);
			outputError = new Matrix(sizeOutput, 1);
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
		public Matrix backPropagate(Matrix coefs, int startingIndex, Matrix error) {
			if (input == null)
				throw new Error("Invalid state");
			if (error.getVectorSize() != weight.getSizeY())
				throw new Error("Invalid argument");
			outputError.mMaxAbs(error, outputError);
			inputError.resize(input.getSizeX(), input.getSizeY());
			inputError.make0();
			for (int j = weight.getSizeY() - 1; j >= 0; j--) {
				double r = error.getVectorItem(j) * learningRate;
				int coefIndex = startingIndex + j * weight.getSizeX();
				for (int i = weight.getSizeX() - 1; i >= 0; i--) {
					double dw = r * input.getVectorItem(i);
					inputError.vectorItemAdd(i, r * weight.getItem(i, j));
					dW.itemAdd(i, j, -dw); // the w-dw means descent, while w+dw means ascent (maximize the error)
					coefs.setItem(coefIndex + i, 0, -dw);
				}
			}
			return inputError;
		}

		@Override
		protected void resetEpoch() {
			dW.make0();
			outputError.make0();
		}

		public String toString() {
			return new StringBuilder()
					//.append("weight\n").append(weight)
					//.append("dWeight\n").append(dW)
					.append("output\n").append(output)
					.append("weight statistics\n").append(weight.calcStatistics().toString(Statistics.CStatDetail))
					.append("dW statistics\n").append(dW.calcStatistics().toString(Statistics.CStatDetail))
					.toString();
		}
	}
}
