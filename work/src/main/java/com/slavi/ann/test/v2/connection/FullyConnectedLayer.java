package com.slavi.ann.test.v2.connection;

import org.apache.commons.math3.linear.RealVector;

import com.slavi.ann.test.BellCurveDistribution;
import com.slavi.ann.test.v2.Layer;
import com.slavi.math.adjust.MatrixStatistics;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public class FullyConnectedLayer extends Layer {
	public Matrix weight;
	public double learningRate;

	public FullyConnectedLayer(int sizeInput, int sizeOutput, double learningRate) {
		this.learningRate = learningRate;
		weight = new Matrix(sizeInput, sizeOutput);
		BellCurveDistribution.fillWeight(weight, 0.3);
	}

	@Override
	public int[] getOutputSize(int inputSize[]) {
		if (inputSize[0] * inputSize[1]  != weight.getSizeX())
			throw new Error("Invalid argument");
		return new int[] { weight.getSizeY(), 1 };
	}

	@Override
	public void extractParams(RealVector delta, int startingIndex) {
		for (int j = weight.getSizeY() - 1; j >= 0; j--) {
			int coefIndex = startingIndex + j * weight.getSizeX();
			for (int i = weight.getSizeX() - 1; i >= 0; i--) {
				delta.setEntry(coefIndex + i, weight.getItem(i, j));
			}
		}
	}

	@Override
	public void applyDeltaToParams(RealVector delta, int startingIndex) {
		System.out.println(weight.toMatlabString("W0"));
		for (int j = weight.getSizeY() - 1; j >= 0; j--) {
			int coefIndex = startingIndex + j * weight.getSizeX();
			for (int i = weight.getSizeX() - 1; i >= 0; i--) {
				double r = delta.getEntry(coefIndex + i);
				weight.itemAdd(i, j, r);
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
		public Matrix outputError;
		public Matrix dW;
		public Matrix tmp;
		public MatrixStatistics ms;

		protected Workspace() {
			input = null;
			int sizeInput = weight.getSizeX();
			int sizeOutput = weight.getSizeY();
			inputError = new Matrix(sizeInput, 1);
			output = new Matrix(sizeOutput, 1);
			outputError = new Matrix(sizeOutput, 1);
			dW = new Matrix(sizeInput, sizeOutput);
			tmp = new Matrix(sizeInput, sizeOutput);
			ms = new MatrixStatistics();
			ms.start();
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
				double r = error.getVectorItem(j);
				int coefIndex = startingIndex + j * weight.getSizeX();
				for (int i = weight.getSizeX() - 1; i >= 0; i--) {
					double dw = r * input.getVectorItem(i) * learningRate;
					inputError.vectorItemAdd(i, r * weight.getItem(i, j));
					dW.itemAdd(i, j, -dw); // the w-dw means descent, while w+dw means ascent (maximize the error)
					coefs.setItem(coefIndex + i, 0, -dw);
					tmp.setItem(i, j, -dw);
				}
			}
			ms.addValue(tmp);
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
					.append(ms.statStatToString(Statistics.CStatDetail))
					.toString();
		}
	}
}
