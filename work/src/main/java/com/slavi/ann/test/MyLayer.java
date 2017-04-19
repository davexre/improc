package com.slavi.ann.test;

import com.slavi.math.matrix.Matrix;

public class MyLayer {
	Matrix input;
	Matrix inputError;
	Matrix output;
	Matrix weight;

	Matrix maxInputError;
	Matrix tmpOutput;
	Matrix tmpW;
	Matrix sumDW;
	int countDW;
	double scale;
	double learningRate;

	public MyLayer(int sizeInput, int sizeOutput, double learningRate) {
		input = null;
		inputError = new Matrix(sizeInput, 1);
		maxInputError = new Matrix(sizeInput, 1);

		output = new Matrix(sizeOutput, 1);
		tmpOutput = new Matrix(sizeOutput, 1);

		weight = new Matrix(sizeInput, sizeOutput);
		tmpW = new Matrix(sizeInput, sizeOutput);
		sumDW = new Matrix(sizeInput, sizeOutput);

		scale = 1; //5.0 / sizeInput;
		this.learningRate = learningRate;
		eraseMemory();
	}

	public int getSizeInput() {
		return weight.getSizeX();
	}

	public int getSizeOutput() {
		return weight.getSizeY();
	}

	public void eraseMemory() {
		weight.makeR(0.5);
		//weight.make0();
		inputError.make0();
		maxInputError.make0();
		output.make0();
		tmpOutput.make0();
		tmpW.make0();
		sumDW.make0();
	}

	public void resetEpoch() {
		inputError.make0();
		maxInputError.make0();
		output.make0();
		tmpOutput.make0();
		tmpW.make0();
		sumDW.make0();
	}

	/**
	 * inputPattern = new Matirx(sizeInput, 1);
	 */
	public Matrix feedForward(Matrix input) {
		if (input.getVectorSize() != getSizeInput())
			throw new Error("Invalid argument");
		this.input = input;
		for (int j = getSizeOutput() -1; j >= 0; j--) {
			double r = 0.0;
			for (int i = getSizeInput() -1; i >= 0; i--) {
				r += input.getVectorItem(i) * weight.getItem(i, j);
			}
			r = 1.0 / (1.0 + Math.exp(-r * scale));
			output.setVectorItem(j, r);
		}
		tmpOutput.mMaxAbs(output, tmpOutput);
		return output;
	}

	public Matrix backPropagate(Matrix error) {
		if (input == null)
			throw new Error("Invalid state");
		if (error.getVectorSize() != getSizeOutput())
			throw new Error("Invalid argument");
		inputError.make0();
		for (int j = getSizeOutput() -1; j >= 0; j--) {
			double r = output.getVectorItem(j);
			r = scale * error.getVectorItem(j) * r * (1 - r);
			for (int i = getSizeInput() -1; i >= 0; i--) {
				double dw = r * input.getVectorItem(i) * learningRate;
				tmpW.setItem(i, j, tmpW.getItem(i, j) + Math.abs(dw));
				sumDW.setItem(i, j, sumDW.getItem(i, j) + dw);
				inputError.setVectorItem(i, inputError.getVectorItem(i) + r * weight.getItem(i, j));
				weight.setItem(i, j, weight.getItem(i, j) - dw); // the w-dw mean descent, while w+dw means ascent (maximize the error)
			}
		}
		maxInputError.mMaxAbs(inputError, maxInputError);
		input = null;
		countDW++;
		return inputError;
	}

	public String toString() {
		StringBuilder r = new StringBuilder();
		r.append("Input:     ");	r.append(input);
		r.append("Output:    ");	r.append(output);
		r.append("Input err: ");	r.append(inputError);
		return r.toString();
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
