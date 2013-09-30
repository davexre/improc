package com.slavi.ann;


public class NNMyDeltaLayer extends NNLayerBase {

	protected double[][] weight;

	protected double[][] delta;

	protected double[] bias;

	public double[] getBias() {
		return bias;
	}

	public void setBias(double[] bias) {
		this.bias = bias;
	}

	public double[][] getDelta() {
		return delta;
	}

	public void setDelta(double[][] delta) {
		this.delta = delta;
	}

	public double[][] getWeight() {
		return weight;
	}

	public void setWeight(double[][] weight) {
		this.weight = weight;
	}

	public NNMyDeltaLayer(int sizeInput, int sizeOutput) {
		super(sizeInput, sizeOutput);
		weight = new double[sizeInput][sizeOutput];
		delta = new double[sizeInput][sizeOutput];
		bias = new double[sizeOutput];
	}

	public void backPropagate(double[] errorOutput) {
		super.backPropagate(errorOutput);
		errorBackProp(errorOutput);
		for (int j = output.length - 1; j >= 0; j--) {
			double r = learningRate * errorOutput[j] * output[j] * (1 - output[j]);
			bias[j] += r;
			r /= input.length; // WHY THIS HAS TO BE HERE !!!???
			for (int i = input.length - 1; i >= 0; i--) {
				double r1 = r * input[i] + momentum * delta[i][j];
				delta[i][j] = r1;
				weight[i][j] += r1;
			}
		}
	}

	public void eraseMemory() {
		ANN.randomizeMatrix(weight);
		ANN.zeroMatrix(delta);
		ANN.zeroArray(bias);
	}

	public void errorBackProp(double[] errorOutput) {
		super.errorBackProp(errorOutput);
		ANN.zeroArray(input);
		for (int j = output.length - 1; j >= 0; j--) {
			double r = errorOutput[j] * output[j] * (1 - output[j]) / input.length;
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
			r = bias[j] + r / input.length;
			output[j] = 1 / (1 + Math.exp(-r));
		}
	}
}
