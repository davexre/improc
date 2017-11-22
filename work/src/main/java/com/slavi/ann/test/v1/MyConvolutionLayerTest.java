package com.slavi.ann.test.v1;

import java.util.Random;

import com.slavi.math.matrix.Matrix;

public class MyConvolutionLayerTest {

	void learn(MyConvolutionLayer l, Matrix input, Matrix target) {
		l.kernel.printM("kernel");
		Matrix output = l.feedForward(input);
		output.printM("output");

		Matrix error = new Matrix();
		output.mSub(target, error);
		error.printM("error");
		
		Matrix inputError = l.backPropagate(error);
		inputError.printM("inputError");
		
	}
	
	void doIt() throws Exception {
		MyNet net = new MyNet();
		MyConvolutionLayer l1 = new MyConvolutionLayer(3, 3, 1);
		l1.kernel.makeR(0.3);
		l1.kernel.setItem(1, 1, 1);
		/*
		l1.kernel.setItem(0, 1, 0.5);
		l1.kernel.setItem(1, 0, 0.5);
		l1.kernel.setItem(1, 2, 0.5);
		l1.kernel.setItem(2, 1, 0.5);
		*/
		net.layers.add(l1);

		MyConvolutionLayer l2 = new MyConvolutionLayer(2, 2, 1);
		l2.kernel.makeR(0.3);
		l2.kernel.setItem(1, 1, 1);
		net.layers.add(l2);
		
		Matrix input = new Matrix(4, 4);
		int sizeInput = input.getVectorSize();
		
		Matrix error = new Matrix(1, 1);
		Matrix target = new Matrix(1, 1);

		for (int epoch = 0; epoch < 100; epoch++) {
			System.out.println("---------------------\nEPOCH "  + epoch);
			for (int index = 0; index < sizeInput; index++) {
				boolean print = index == 1; // || epoch == 19;
				if (print) System.out.println("Index = " + index);

				if (print)
					for (int i = 0; i < net.layers.size(); i++) {
						MyConvolutionLayer ll = (MyConvolutionLayer) net.layers.get(i);
						ll.inputError.printM("IER " + i);
						ll.output.printM("OUT " + i);
						ll.kernel.printM("Kernel " + i);
					}

				for (int i = 0; i < input.getVectorSize(); i++)
					input.setVectorItem(i, index == i ? 0.95 : 0.05);
				for (int i = 0; i < target.getVectorSize(); i++)
					target.setVectorItem(i, (index & (1 << i)) == 0 ? 0.05 : 0.95);

				Matrix output = net.feedForward(input);
				output.mSub(target, error);
				Matrix inputError = net.backPropagate(error);
				if (print) input.printM("input");
				if (print) inputError.printM("inputError");
				if (print) target.printM("target");
				if (print) output.printM("output");
				if (print) error.printM("error");
				if (print) System.out.println();
			}
			
			//l1.kernel.printM("kernel 1");
			// l2.kernel.printM("kernel 2");
			net.applyTraining();
		}
/*		
		
		Random rnd = new Random();
		for (int i = 0; i < input.getVectorSize(); i++)
			input.setVectorItem(i, rnd.nextDouble());
//		input.makeR(0.7);
		Matrix target = new Matrix(1, 1);
		target.makeR(0.95);
		
		for (int i = 0; i < 50; i ++) {
			System.out.println("=================\nEpoch " + i);
			learn(l, input, target);
		}
	*/
	}

	public static void main(String[] args) throws Exception {
		new MyConvolutionLayerTest().doIt();
		System.out.println("Done.");
	}
}

