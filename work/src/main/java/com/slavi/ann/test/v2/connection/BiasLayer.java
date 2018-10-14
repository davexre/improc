package com.slavi.ann.test.v2.connection;

import com.slavi.ann.test.v2.Layer;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public class BiasLayer extends Layer {
	public Matrix bias;
	public double learningRate;

	public BiasLayer(int sizeInputX, int sizeInputY, double learningRate) {
		this.learningRate = learningRate;
		bias = new Matrix(sizeInputX, sizeInputY);
	}

	@Override
	public int[] getOutputSize(int inputSize[]) {
		return inputSize;
	}

	@Override
	public void extractParams(Matrix delta, int startingIndex) {
		for (int j = bias.getSizeY() - 1; j >= 0; j--) {
			int coefIndex = startingIndex + j * bias.getSizeX();
			for (int i = bias.getSizeX() - 1; i >= 0; i--) {
				delta.setItem(coefIndex + i, 0, bias.getItem(i, j));
			}
		}
	}

	@Override
	public void loadParams(Matrix delta, int startingIndex) {
		for (int j = bias.getSizeY() - 1; j >= 0; j--) {
			int coefIndex = startingIndex + j * bias.getSizeX();
			for (int i = bias.getSizeX() - 1; i >= 0; i--) {
				double r = delta.getItem(coefIndex + i, 0);
				bias.setItem(i, j, r);
			}
		}
	}

	@Override
	public int getNumAdjustableParams() {
		return bias.getVectorSize();
	};

	@Override
	public Workspace createWorkspace() {
		return new Workspace();
	}

	@Override
	public void applyWorkspace(LayerWorkspace workspace) {
		Workspace ws = (Workspace) workspace;
		ws.dB.mSum(bias, bias);
		ws.resetEpoch();
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append(String.format("learning rate: %.4f\n", learningRate))
				.append("bias\n").append(bias)
				.toString();
	}

	public class Workspace extends LayerWorkspace {
		public Matrix inputError;
		public Matrix output;
		public Matrix dB;

		protected Workspace() {
			int sizeInputX = bias.getSizeX();
			int sizeInputY = bias.getSizeY();
			inputError = new Matrix(sizeInputX, sizeInputY);
			output = new Matrix(sizeInputX, sizeInputY);
			dB = new Matrix(sizeInputX, sizeInputY);
		}

		@Override
		public Matrix feedForward(Matrix input) {
			if (input.getSizeX() != bias.getSizeX() ||
				input.getSizeY() != bias.getSizeY())
				throw new Error("Invalid argument");
			bias.mSum(input, output);
			return output;
		}

		@Override
		public Matrix backPropagate(Matrix coefs, int startingIndex, Matrix error) {
			if (error.getSizeX() != bias.getSizeX() ||
				error.getSizeY() != bias.getSizeY())
				throw new Error("Invalid argument");
			for (int j = bias.getSizeY() - 1; j >= 0; j--) {
				int coefIndex = startingIndex + j * bias.getSizeX();
				for (int i = bias.getSizeX() - 1; i >= 0; i--) {
					double r = error.getItem(i, j);
					inputError.setItem(i, j, r);
					r *= learningRate;
					dB.itemAdd(i, j, -r);
					coefs.setItem(coefIndex + i, 0, -r);
				}
			}
			return inputError;
		}

		@Override
		protected void resetEpoch() {
			dB.make0();
		}

		public String toString() {
			return new StringBuilder()
					.append("output\n").append(output)
					.append("bias statistics\n").append(bias.calcStatistics().toString(Statistics.CStatDetail))
					.append("dB statistics\n").append(dB.calcStatistics().toString(Statistics.CStatDetail))
					.toString();
		}
	}
}
