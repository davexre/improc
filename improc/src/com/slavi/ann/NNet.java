package com.slavi.ann;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class NNet extends NNLayerBase {

	private ArrayList<NNLayer> layers = new ArrayList<NNLayer>();

	public void setLearningRate(double learningRate) {
		super.setLearningRate(learningRate);
		for (int i = layers.size() - 1; i >= 0; i--)
			layers.get(i).setLearningRate(learningRate);
	}

	public void setMomentum(double momentum) {
		super.setMomentum(momentum);
		for (int i = layers.size() - 1; i >= 0; i--)
			layers.get(i).setLearningRate(learningRate);
	}

	// TODO: Fix this warning
	@SuppressWarnings("unchecked")
	public NNet(int sizeInput, int sizeOutput) {
		this(sizeInput, new int[] { sizeInput * 2, sizeOutput }, 
			new Class[] { NNClassicLayer.class, NNClassicLayer.class });
	}

	public NNet(int sizeInput, int[] outputSizes, Class<? extends NNLayer>[] layerClasses) {
		super(sizeInput, outputSizes[outputSizes.length - 1]);
		if (outputSizes.length != layerClasses.length)
			throw new Error("NNet: bad argument list");
		int endSize = sizeInput;
		int startSize;
		for (int i = 0; i < outputSizes.length; i++) {
			startSize = endSize;
			endSize = outputSizes[i];
			Constructor<? extends NNLayer> c = null;
			try {
				c = layerClasses[i].getConstructor(int.class, int.class);
				layers.add(c.newInstance(new Integer(startSize), new Integer(endSize)));
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (c == null)
				throw new IllegalArgumentException(
						"NNet: Not supported class passed as parameter");
		}
	}

	public void backPropagate(double[] errorOutput) {
		super.backPropagate(errorOutput);
		double[] dd = errorOutput;
		for (int i = layers.size() - 1; i >= 0; i--) {
			NNLayer l = layers.get(i);
			l.backPropagate(dd);
			dd = l.getInput();
		}
		for (int i = input.length - 1; i >= 0; i--)
			input[i] = dd[i];
	}

	public void eraseMemory() {
		for (int i = layers.size() - 1; i >= 0; i--)
			layers.get(i).eraseMemory();
	}

	public void errorBackProp(double[] errorOutput) {
		super.errorBackProp(errorOutput);
		double[] dd = errorOutput;
		for (int i = layers.size() - 1; i >= 0; i--) {
			NNLayer l = layers.get(i);
			l.errorBackProp(dd);
			dd = l.getInput();
		}
		for (int i = input.length - 1; i >= 0; i--)
			input[i] = dd[i];
	}

	public void feedForward(double[] inputPattern) {
		super.feedForward(inputPattern);
		for (int i = input.length - 1; i >= 0; i--)
			input[i] = inputPattern[i];

		NNLayer l = null;
		double[] dd = inputPattern;
		for (int i = 0; i <= layers.size() - 1; i++) {
			l = layers.get(i);
			l.feedForward(dd);
			dd = l.getOutput();
		}
		for (int i = output.length - 1; i >= 0; i--)
			output[i] = l.getOutput()[i];
	}
}
