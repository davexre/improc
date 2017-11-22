package com.slavi.ann.test.v1;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.slavi.math.matrix.Matrix;

public class MyNet {
	public final List<Layer> layers = new ArrayList<>();

	public static MyNet makeNet(Class<? extends MyLayer> layerClass, Integer ... sizes) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		MyNet r = new MyNet();
		Integer sizeInput = sizes[0];
		Constructor<? extends MyLayer> c = layerClass.getConstructor(int.class, int.class, double.class);
		for (int i = 1; i < sizes.length; i++) {
			r.layers.add(c.newInstance(sizeInput, sizes[i], 1d));
			sizeInput = sizes[i];
		}
		return r;
	}
	
	public void eraseMemory() {
		for (Layer l : layers)
			l.eraseMemory();
	}

	public void resetEpoch() {
		for (Layer l : layers)
			l.resetEpoch();
	}

	public Matrix feedForward(Matrix input) {
		for (Layer l : layers) {
			input = l.feedForward(input);
		}
		return input;
	}

	public Matrix backPropagate(Matrix error) {
		for (int i = layers.size() - 1; i >= 0; i--) {
			Layer l = layers.get(i);
			error = l.backPropagate(error);
		}
		return error;
	}

	public void applyTraining() {
		for (Layer l : layers)
			l.applyTraining();
	}
}
