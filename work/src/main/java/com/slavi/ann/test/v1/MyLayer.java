package com.slavi.ann.test.v1;

import java.util.Random;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;

public class MyLayer implements Layer {
	Matrix input;
	Matrix inputError;
	Matrix output;
	Matrix weight;
	Matrix dW;

	int dCount;
	double scale;
	double learningRate;

	public MyLayer(int sizeInput, int sizeOutput, double learningRate) {
		input = null;
		inputError = new Matrix(sizeInput, 1);
		output = new Matrix(sizeOutput, 1);
		weight = new Matrix(sizeInput, sizeOutput);
		dW = new Matrix(sizeInput, sizeOutput);

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

	boolean isBetween(int value, int A, int B) {
		return A < B ?
			A <= value && value <= B :
			B <= value && value <= A;
	}

	public void eraseWeights_0() {
		Random r = new Random();
		for (int i = weight.getVectorSize() - 1; i >= 0; i--) {
			weight.setVectorItem(i, r.nextDouble());
		}
	}
	
	public void eraseWeights_1() {
		if (weight.getSizeX() < weight.getSizeY()) {
			throw new Error();
		}
		int w = (2 * weight.getSizeX()) / 3;
		int off = weight.getSizeX() / weight.getSizeY();
		for (int j = 0; j < weight.getSizeY(); j++) {
			int start = MathUtil.fixIndexLooped(j * off, weight.getSizeX());
			int end = MathUtil.fixIndexLooped(start + w, weight.getSizeX());
			for (int i = 0; i < weight.getSizeX(); i++) {
				boolean isInRange = isBetween(i, start, end);
 				weight.setItem(i, j, isInRange ? 0.95 : 0.05);
			}
		}
	}

	public void eraseWeights_2() {
		fillWeight(weight, 0.3);
	}

	static double sqrt2pi = Math.sqrt(2.0 * Math.PI);
	
	public static void fillWeight(Matrix w, double stdDev) {
		double scale = 1.0 / (2 * stdDev * stdDev); // * w.getSizeX());
		double scale2 = 1.0 / (stdDev * sqrt2pi);
		double w2 = w.getSizeX() / 2.0;
		for (int j = w.getSizeY() - 1; j >= 0; j--) {
			double tr = w2 - j * w.getSizeX() / w.getSizeY();
			for (int i = w.getSizeX() - 1; i >= 0; i--) {
				double d = (i + tr) % w.getSizeX();
				if (d < 0)
					d += w.getSizeX();
				d -= w2;
				w.setItem(i, j, Math.exp(-d*d*scale) * scale2);
			}
		}
	}

	public static void fillWeight_quick(Matrix w, double stdDev) {
		double scale = 1.0 / (stdDev * w.getSizeX());
		double w2 = w.getSizeX() / 2.0;
		for (int j = w.getSizeY() - 1; j >= 0; j--) {
			double tr = w2 - j * w.getSizeX() / w.getSizeY();
			for (int i = w.getSizeX() - 1; i >= 0; i--) {
				double d = (i + tr) % w.getSizeX();
				if (d < 0)
					d += w.getSizeX();
				d -= w2;
				w.setItem(i, j, Math.exp(-d*d*scale));
			}
		}
	}

	public void eraseMemory() {
		//weight.makeR(0.5);
		//weight.make0();
		eraseWeights_2();
		dW.make0();
		inputError.make0();
		output.make0();
	}

	public void resetEpoch() {
//		inputError.make0();
//		output.make0();
		dW.make0();
		dCount = 0;
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
				inputError.vectorItemAdd(i, r * weight.getItem(i, j));
				dW.itemAdd(i, j, -dw); // the w-dw mean descent, while w+dw means ascent (maximize the error)
			}
		}
		input = null;
		dCount++;
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
		if (dCount == 0)
			return;
//		dW.rMul(1.0 / dCount);
		dW.mSum(weight, weight);
		dCount = 0;
		resetEpoch();
	}
}
