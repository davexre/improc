package com.slavi.ann;

public class NNSimpleLayer extends NNLayerBase {

	protected double[][] weight;

	public double[][] getWeight() {
		return weight;
	}

	public void setWeight(double[][] weight) {
		this.weight = weight;
	}

	public NNSimpleLayer(int sizeInput, int sizeOutput) {
		super(sizeInput, sizeOutput);
		weight = new double[sizeInput][sizeOutput];
	}

	public void backPropagate(double[] errorOutput) {
		super.backPropagate(errorOutput);
		errorBackProp(errorOutput);
		for (int j = output.length - 1; j >= 0; j--) {
			double r = learningRate * errorOutput[j] * output[j] * (1 - output[j]);
			for (int i = input.length - 1; i >= 0; i--) {
				weight[i][j] += r * input[i];
			}
		}
	}

	public void eraseMemory() {
		//ANN.randomizeMatrix(weight);
		for (int j = output.length - 1; j >= 0; j--)
			for (int i = input.length - 1; i >= 0; i--)
				weight[i][j] = 0.5;
	}

	public void errorBackProp(double[] errorOutput) {
		super.errorBackProp(errorOutput);
		ANN.zeroArray(input);
		for (int j = output.length - 1; j >= 0; j--) {
			double r = errorOutput[j] * output[j] * (1 - output[j]);
			for (int i = input.length - 1; i >= 0; i--)
				input[i] += r * weight[i][j];
		}
	}

	public void feedForward(double[] inputPattern) {
		super.feedForward(inputPattern);
		for (int i = input.length - 1; i >= 0; i--)
			input[i] = inputPattern[i];
		for (int j = output.length - 1; j >= 0; j--) {
			double r = 0;
			for (int i = input.length - 1; i >= 0; i--)
				r += input[i] * weight[i][j];
			output[j] = 1 / (1 + Math.exp(-r));
		}
	}
}
