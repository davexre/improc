package com.slavi.ann;


public class NNArcTanLayer extends NNClassicLayer {

	protected double activat[];
	
	public NNArcTanLayer(int sizeInput, int sizeOutput) {
		super(sizeInput, sizeOutput);
		activat = new double[sizeOutput];
	}

	public void backPropagate(double[] errorOutput) {
		errorBackProp(errorOutput);
		
		for (int j = output.length - 1; j >= 0; j--) {
			double r = learningRate * errorOutput[j] * Math.PI * (1 + activat[j] * activat[j]);
			bias[j] += r;
			//???	r = r / input.length;
			for (int i = input.length - 1; i >= 0; i--) {
				double r1 = r * input[i] + momentum * delta[i][j];
				delta[i][j] = r1;
				weight[i][j] = weight[i][j] + r1;
			}
		}
	}
	
	public void errorBackProp(double[] errorOutput) {
		ANN.zeroArray(input);
		for (int j = output.length - 1; j >= 0; j--) {
			double r = errorOutput[j] * Math.PI * (1 + activat[j] * activat[j]) / input.length;
			for (int i = input.length - 1; i >= 0; i--) {
				input[i] += r * weight[i][j];
			}
		}
	}
	
	public void feedForward(double[] inputPattern) {
		for (int i = input.length - 1; i >= 0; i--) {
			input[i] = inputPattern[i];
		}
		for (int j = output.length - 1; j >= 0; j--) {
			double r = 0;
			for (int i = input.length - 1; i >= 0; i--) {
				r += input[i] * weight[i][j];
			}
			r = bias[j] + r / input.length;
			output[j] = Math.atan(r) / Math.PI;
		}
	}
}
