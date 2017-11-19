package com.slavi.ann.test;

import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public class MyConvolutionLayer implements Layer {

	Matrix kernel;
	Matrix output;
	Matrix input;
	Matrix inputError;
	double learningRate;

	Matrix dKernel;
	Matrix dKernel1;
	MatrixStatistics ms = new MatrixStatistics();
	
	public MyConvolutionLayer(int kernelSizeX, int kernelSizeY, double learningRate) {
		kernel = new Matrix(kernelSizeX, kernelSizeY);
		dKernel = new Matrix(kernelSizeX, kernelSizeY);
		dKernel1 = new Matrix(kernelSizeX, kernelSizeY);
		output = new Matrix();
		input = null;
		inputError = new Matrix();
		this.learningRate = learningRate;
	}
	
	public Matrix feedForward(Matrix input) {
		this.input = input;
		output.resize(input.getSizeX() - kernel.getSizeX() + 1, input.getSizeY() - kernel.getSizeY() + 1);
		for (int oy = output.getSizeY() - 1; oy >= 0; oy--) {
			for (int ox = output.getSizeY() - 1; ox >= 0; ox--) {
				double r = 0.0;
				for (int ky = kernel.getSizeY() - 1; ky >= 0; ky--) {
					for (int kx = kernel.getSizeX() - 1; kx >= 0; kx--) {
						r += input.getItem(ox + kx, oy + ky) * kernel.getItem(kx, ky);
					}
				}
				r = 1.0 / (1.0 + Math.exp(-r));
				output.setItem(ox, oy, r);
			}
		}
		return output;
	}
	
	public Matrix backPropagate(Matrix error) {
		if (input == null)
			throw new Error("Invalid state");
		if ((output.getSizeX() != error.getSizeX()) ||
			(output.getSizeY() != error.getSizeY()))
			throw new Error("Invalid argument");

		inputError.resize(input.getSizeX(), input.getSizeY());
		inputError.make0();
		dKernel1.make0();
		for (int oy = output.getSizeY() - 1; oy >= 0; oy--) {
			for (int ox = output.getSizeY() - 1; ox >= 0; ox--) {
				double r = output.getItem(ox, oy);
				r = error.getItem(ox, oy) * r * (1 - r);
				for (int ky = kernel.getSizeY() - 1; ky >= 0; ky--) {
					for (int kx = kernel.getSizeX() - 1; kx >= 0; kx--) {
						double dw = r * input.getItem(ox + kx, oy + ky) * learningRate;
						inputError.itemAdd(ox + kx, oy + ky, r * kernel.getItem(kx, ky));
						dKernel1.itemAdd(kx, ky, -dw); // the w-dw mean descent, while w+dw means ascent (maximize the error)
					}
				}

			}
		}
		dKernel.mSum(dKernel1, dKernel);
//		dKernel1.termAbs(dKernel1);
		Matrix m = new Matrix();
		error.termAbs(m);
		ms.addValue(m);
		return inputError;
	}

	public void eraseMemory() {
	}

	public void resetEpoch() {
		dKernel.make0();
		ms.start();
	}

	public void applyTraining() {
		ms.stop();
		System.out.println(ms.toString(Statistics.CStatStdDev | Statistics.CStatMinMax));
		dKernel.mSum(kernel, kernel);
		resetEpoch();
	}
}
