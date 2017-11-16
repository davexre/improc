package com.slavi.ann.test;

import com.slavi.math.matrix.Matrix;

public class MyConvolutionLayerTest {

	void learn(MyConvolutionLayer l, Matrix input, Matrix target) {
		Matrix output = l.feedForward(input);
		output.printM("output");

		Matrix error = new Matrix();
		output.mSub(target, error);
		error.printM("error");
		
		Matrix inputError = l.backPropagate(error);
		inputError.printM("inputError");
	}
	
	void doIt() throws Exception {
		MyConvolutionLayer l = new MyConvolutionLayer(3, 3, 1);
		l.kernel.makeR(0.3);
		l.kernel.setItem(1, 1, 1);
		
		Matrix input = new Matrix(3, 3);
		input.makeR(0.7);
		Matrix target = new Matrix(1, 1);
		target.makeR(0.3);
		
		for (int i = 0; i < 3; i ++) {
			System.out.println("=================\nEpoch " + i);
			learn(l, input, target);
		}
		
	}

	public static void main(String[] args) throws Exception {
		new MyConvolutionLayerTest().doIt();
		System.out.println("Done.");
	}
}

