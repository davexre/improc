package com.slavi.ann.test;

import com.slavi.math.matrix.Matrix;

public class MyLayerTest {

	void printM(Matrix m, String desc) {
		//System.out.println(m.toMatlabString(desc));
		m.printM(desc);
	}

	void doIt() throws Exception {
		int sizeInput = 16;
		int sizeOutput = 1;
		//MyLayer l = new MyLayer(sizeInput, sizeOutput, 1);
		MyNet net = MyNet.makeNet(MyLayer.class, sizeInput, 9, sizeOutput);
		Matrix input = new Matrix(sizeInput, 1);
		Matrix error = new Matrix(sizeOutput, 1);
		Matrix target = new Matrix(sizeOutput, 1);

		net.eraseMemory();
		for (int epoch = 0; epoch < 50; epoch++) {
			System.out.println("---------------------\nEPOCH "  + epoch);
			MatrixStatistics ms = new MatrixStatistics();
			for (int index = 0; index < sizeInput; index++) {
				boolean print = index == 1;
				if (print)
					for (int i = 0; i < net.layers.size(); i++) {
						MyLayer ll = (MyLayer) net.layers.get(i);
						printM(ll.inputError, "IER " + i);
						printM(ll.output, "OUT " + i);
						printM(ll.weight, "W");
					}

				if (print)
					System.out.println("At index " + index);
				for (int i = 0; i < input.getVectorSize(); i++) {
					input.setVectorItem(i, index == i ? 0.95 : 0.05);
				}
				for (int i = 0; i < target.getVectorSize(); i++)
					target.setVectorItem(i, (index & (1 << i)) == 0 ? 0.05 : 0.95);
				Matrix output = net.feedForward(input);
				if (print) {
					printM(input, "Input");
					printM(target, "Target");
					printM(output, "Output");
				}
				output.mSub(target, error);
				if (print)
					printM(error, "E1");
				//Matrix inputError =
				
				error.termAbs(output);
				ms.addValue(output);
				net.backPropagate(error);
				//printM(inputError, "IE");
				output = net.feedForward(input);
				output.mSub(target, error);
				if (print)
					printM(error, "E2");
			}
			//System.out.println(l.weight.toMatlabString("W"));
			ms.stop();
			System.out.println(ms.toString());
			net.applyTraining();
		}
	}

	public static void main(String[] args) throws Exception {
		new MyLayerTest().doIt();
		System.out.println("Done.");
	}
}
