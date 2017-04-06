package com.slavi.ann;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.FIELD)
public class NNLayerBase implements NNLayer {

	protected double learningRate;

	protected double momentum;

	@XmlTransient
	protected double input[];

	@XmlTransient
	protected double output[];

	public double[] getInput() {
		return input;
	}

	public void setInput(double[] input) {
		this.input = input;
	}

	public double getLearningRate() {
		return learningRate;
	}

	public void setLearningRate(double learningRate) {
		this.learningRate = learningRate;
	}

	public double getMomentum() {
		return momentum;
	}

	public void setMomentum(double momentum) {
		this.momentum = momentum;
	}

	public double[] getOutput() {
		return output;
	}

	public void setOutput(double[] output) {
		this.output = output;
	}

	public NNLayerBase(int sizeInput, int sizeOutput) {
		learningRate = 1;
		momentum = 0.8;
		input = new double[sizeInput];
		output = new double[sizeOutput];
	}

	public void eraseMemory() {
	}

	public void feedForward(double[] inputPattern) {
		if (inputPattern.length != input.length) {
			throw new IllegalArgumentException("FeedForward invalid input data size");
		}
	}

	public void errorBackProp(double[] errorOutput) {
		if (errorOutput.length != output.length) {
			throw new IllegalArgumentException("ErrorBackProp invalid ErrorOut size");
		}
	}

	public void backPropagate(double[] errorOutput) {
		if (errorOutput.length != output.length) {
			throw new IllegalArgumentException("BackPropagate invalid ErrorOut size");
		}
	}
}
