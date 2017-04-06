package com.slavi.ann;

import java.io.Serializable;

public interface NNLayer extends Serializable {

	public double[] getInput();

	public void setInput(double[] input);

	public double getLearningRate();

	public void setLearningRate(double learningRate);

	public double getMomentum();

	public void setMomentum(double momentum);

	public double[] getOutput();

	public void setOutput(double[] output);

	public void eraseMemory();

	public void feedForward(double[] inputPattern);

	public void errorBackProp(double[] errorOutput);

	public void backPropagate(double[] errorOutput);
}
