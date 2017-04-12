package com.slavi.ann.test;

import com.slavi.math.matrix.Matrix;

public class MyLayer {
	Matrix lastInput;
	Matrix inputError;
	Matrix output;
	Matrix weight;

	Matrix tmpW;
	Matrix sumDW;
	int countDW;
	double scale;

	public MyLayer(int sizeInput, int sizeOutput) {
		lastInput = null;
		inputError = new Matrix(sizeInput, 1);
		output = new Matrix(sizeOutput, 1);
		weight = new Matrix(sizeInput, sizeOutput);
		tmpW = new Matrix(sizeInput, sizeOutput);
		sumDW = new Matrix(sizeInput, sizeOutput);
		scale = 5.0 / sizeInput;
		eraseMemory();
	}

	public int getSizeInput() {
		return weight.getSizeX();
	}

	public int getSizeOutput() {
		return weight.getSizeY();
	}

	public void eraseMemory() {
		//weight.makeR(0.5);
		weight.make0();
		inputError.make0();
		output.make0();
		tmpW.make0();
		sumDW.make0();
	}

	protected void applyActivation() {
		for (int j = getSizeOutput() -1; j >= 0; j--) {
			double r = output.getVectorItem(j);
			r = 1.0 / (1.0 + Math.exp(-r * scale));
			output.setVectorItem(j, r);
		}
	}

	protected void applyActivationDerivative(Matrix error) {
		for (int j = getSizeOutput() -1; j >= 0; j--) {
			double r = output.getVectorItem(j);
			r = scale * error.getVectorItem(j) * r * (1 - r);
			output.setVectorItem(j, r);
		}
	}

	/**
	 * inputPattern = new Matirx(sizeInput, 1);
	 */
	public Matrix feedForward(Matrix input) {
		if (input.getVectorSize() != getSizeInput())
			throw new Error("Invalid argument");
		lastInput = input;
		for (int j = getSizeOutput() -1; j >= 0; j--) {
			double r = 0.0;
			for (int i = getSizeInput() -1; i >= 0; i--) {
				r += input.getVectorItem(i) * weight.getItem(i, j);
			}
			output.setVectorItem(j, r);
		}
		applyActivation();
		return output;
	}

	public Matrix errorBackProp(Matrix error) {
		if (error.getVectorSize() != getSizeOutput())
			throw new Error("Invalid argument");
		applyActivationDerivative(error);
		inputError.make0();
		for (int j = getSizeOutput() -1; j >= 0; j--) {
			double r = output.getVectorItem(j);
			for (int i = getSizeInput() -1; i >= 0; i--) {
				inputError.setVectorItem(i, inputError.getVectorItem(i) + r * weight.getItem(i, j));
			}
		}
		return inputError;
	}

	public Matrix backPropagate(Matrix error) {
		if (lastInput == null)
			throw new Error("Invalid state");
		if (error.getVectorSize() != getSizeOutput())
			throw new Error("Invalid argument");
		applyActivationDerivative(error);
		inputError.make0();
		for (int j = getSizeOutput() -1; j >= 0; j--) {
			double r = output.getVectorItem(j); // * learningRate;
			for (int i = getSizeInput() -1; i >= 0; i--) {
				double dw = r * lastInput.getVectorItem(i);
				tmpW.setItem(i, j, tmpW.getItem(i, j) + Math.abs(dw));
				sumDW.setItem(i, j, sumDW.getItem(i, j) + dw);
				inputError.setVectorItem(i, inputError.getVectorItem(i) + r * weight.getItem(i, j));
				weight.setItem(i, j, weight.getItem(i, j) + dw);
			}
		}
		lastInput = null;
		countDW++;
		return inputError;
	}

	public void applyTraining() {
		if (countDW == 0)
			return;
		sumDW.rMul(1.0 / countDW);
		//weight.mSum(sumDW, weight);
		tmpW.make0();
		sumDW.make0();
		countDW = 0;
	}
}
