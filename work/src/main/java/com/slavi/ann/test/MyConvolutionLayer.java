package com.slavi.ann.test;

import com.slavi.math.matrix.Matrix;

public class MyConvolutionLayer {

	Matrix kernel;
	Matrix dKernel;
	Matrix output;
	Matrix input;
	Matrix inputError;
	double learningRate;

	public MyConvolutionLayer(int kernelSizeX, int kernelSizeY, double learningRate) {
		kernel = new Matrix(kernelSizeX, kernelSizeY);
		dKernel = new Matrix(kernelSizeX, kernelSizeY);
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

		dKernel.make0();
		inputError.resize(input.getSizeX(), input.getSizeY());
		inputError.make0();
		for (int oy = output.getSizeY() - 1; oy >= 0; oy--) {
			for (int ox = output.getSizeY() - 1; ox >= 0; ox--) {
				double r = output.getItem(ox, oy);
				r = error.getItem(ox, oy) * r * (1 - r);
				for (int ky = kernel.getSizeY() - 1; ky >= 0; ky--) {
					for (int kx = kernel.getSizeX() - 1; kx >= 0; kx--) {
						r += input.getItem(ox + kx, oy + ky) * kernel.getItem(kx, ky);
						
						double dw = r * input.getItem(ox + kx, oy + ky) * learningRate;
						inputError.itemAdd(ox + kx, oy + ky, r * kernel.getItem(kx, ky));
						dKernel.itemAdd(kx, ky, -dw); // the w-dw mean descent, while w+dw means ascent (maximize the error)
					}
				}

			}
		}
		dKernel.rMul(1.0d / output.getVectorSize());
		kernel.mSub(dKernel, kernel);
		return inputError;
	}
}
