package com.slavi.ann.test.v2.connection;

import java.util.ArrayList;

import com.slavi.ann.test.v2.Layer;
import com.slavi.math.matrix.Matrix;

public class PolynomialLayer extends Layer {
	static class Term {
		double coef[];
		double power[];

		public Term(int sizeOutput, int numPowers) {
			coef = new double[sizeOutput];
			power = new double[numPowers];
		}
	}

	ArrayList<Term> terms = new ArrayList<>();
	Term cur;

	int sizeInput;
	int sizeOutput;

	public PolynomialLayer(int sizeInput, int sizeOutput) {
		this.sizeInput = sizeInput;
		this.sizeOutput = sizeOutput;
	}

	@Override
	public int getNumAdjustableParams() {
		return terms.size() * sizeOutput + sizeInput;
	}

	@Override
	public int[] getOutputSize(int[] inputSize) {
		return new int[] { sizeOutput, 1 };
	}

	@Override
	public LayerWorkspace createWorkspace() {
		return new Workspace();
	}

	public class Workspace extends LayerWorkspace {
		public Matrix input;
		public Matrix inputError;
		public Matrix output;
		public Matrix outputError;	// Used by com.slavi.ann.test.v2.Utils.DrawFullyConnectedLayer
		public Matrix dW;

		protected Workspace() {
			input = null;
			inputError = new Matrix(sizeInput, 1);
			output = new Matrix(sizeOutput, 1);
			outputError = new Matrix(sizeOutput, 1);
			dW = new Matrix(sizeInput, sizeOutput);
		}

		@Override
		public Matrix feedForward(Matrix input) {
			if (input.getVectorSize() != sizeInput)
				throw new Error("Invalid argument");
			this.input = input;

			for (int j = 0; j < sizeOutput; j++) {
				double r = 0.0;
				for (Term term : terms) {
					double t = term.coef[j];
					for (int i = 0; i < sizeInput; i++) {
						double p = term.power[i];
						t *= Math.pow(input.getVectorItem(i), p);
					}
					r += t;
				}
				output.setItem(j, 0, r);
			}
			return output;
		}

		@Override
		public Matrix backPropagate(Matrix coefs, int startingIndex, Matrix error) {
			if (input == null)
				throw new Error("Invalid state");
			if (error.getVectorSize() != sizeOutput)
				throw new Error("Invalid argument");

			return null;
		}
	}
}
