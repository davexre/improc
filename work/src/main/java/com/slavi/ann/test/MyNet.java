package com.slavi.ann.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.slavi.math.matrix.Matrix;

public class MyNet {
	int sizeInput;
	int sizeOutput;
	protected List<MyLayer> layers = new ArrayList<>();

	public MyNet(Class<? extends MyLayer> layerClass, Integer ... sizes) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Integer sizeInput = sizes[0];
		this.sizeInput = sizeInput;
		Constructor<? extends MyLayer> c = layerClass.getConstructor(int.class, int.class, double.class);
		for (int i = 1; i < sizes.length; i++) {
			layers.add(c.newInstance(sizeInput, sizes[i], 1d/i));
			sizeInput = sizes[i];
		}
		this.sizeOutput = sizeInput;
	}

	public int getSizeInput() {
		return sizeInput;
	}

	public int getSizeOutput() {
		return sizeOutput;
	}

	public void eraseMemory() {
		for (MyLayer l : layers)
			l.eraseMemory();
	}

	public void resetEpoch() {
		for (MyLayer l : layers)
			l.resetEpoch();
	}

	public Matrix feedForward(Matrix input) {
		for (MyLayer l : layers) {
			input = l.feedForward(input);
		}
		return input;
	}

	public Matrix backPropagate(Matrix error) {
		for (int i = layers.size() - 1; i >= 0; i--) {
			MyLayer l = layers.get(i);
			error = l.backPropagate(error);
		}
		return error;
	}

	public void applyTraining() {
		for (MyLayer l : layers)
			l.applyTraining();
	}
}
