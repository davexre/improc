package com.slavi.ann.test;

import java.util.List;

import com.slavi.ann.test.MnistData.MnistPattern;
import com.slavi.math.MathUtil;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Marker;
/*

To check this: https://github.com/deepmind/sonnet
more info: http://yann.lecun.com/exdb/publis/index.html#lecun-98

https://github.com/ivan-vasilev/neuralnetworks

*/
public class MyMnistDataTest {

	int insize = 28*28;
	void patToInput(MnistPattern pat, Matrix dest) {
		dest.resize(insize, 1);
		for (int i = 0; i < insize; i++)
			dest.setVectorItem(i, MathUtil.mapValue(pat.image[i], 0, 255, 0, 1));
	}

	void patToOutput(MnistPattern pat, Matrix dest) {
		dest.resize(10, 1);
		for (int i = 0; i < 10; i++)
			dest.setVectorItem(i, pat.label == i ? 1 : 0);
	}

	void doIt() throws Exception {
		Marker.mark("Read");
		List<MnistPattern> pats = MnistData.readMnistSet(false);
		Marker.release();

		MyNet nnet = new MyNet(MyLayer.class,
				insize,
				//700, 600, 500, 400, 300, 200, 100,
				50, 10);
		nnet.eraseMemory();

		int maxPattern = pats.size();
		int maxPatternTrain = 10; //maxPattern / 2;

		Marker.mark("Total");
		Marker.mark("Train");
		Matrix input = new Matrix(nnet.getSizeInput(), 1);
		Matrix target = new Matrix(nnet.getSizeOutput(), 1);
		Matrix error = new Matrix(nnet.getSizeOutput(), 1);

		for (int epoch = 0; epoch < 1; epoch++) {
			nnet.resetEpoch();
			for (int index = 0;
					index < maxPatternTrain; //pats.size()
					index++) {
				MnistPattern pat = pats.get(index);
				patToInput(pat, input);
				patToOutput(pat, target);
				Matrix output = nnet.feedForward(input);

				System.out.println(input.toMatlabString("I"));
				System.out.println(target.toMatlabString("T"));
				System.out.println(output.toMatlabString("O"));
				output.mSub(target, error);
				System.out.println(error.toMatlabString("E1"));
				Matrix inputError = nnet.backPropagate(error);
				System.out.println(inputError.toMatlabString("IE"));
				output = nnet.feedForward(input);
				output.mSub(target, error);
				System.out.println(error.toMatlabString("E2"));
			}
		}
		Marker.releaseAndMark("Recall");

		//nnet.layers.get(nnet.layers.size() - 1).tmpW.printM("last tmpW");
		//nnet.layers.get(nnet.layers.size() - 1).sumDW.printM("last sumDW");
		nnet.applyTraining();

		Matrix max = new Matrix(nnet.getSizeOutput(), 1);
		max.make0();

		Statistics st = new Statistics();
		st.start();
		Statistics st2 = new Statistics();
		st2.start();
		Statistics st3 = new Statistics();
		st3.start();
		for (int index = 0;
				index < maxPattern; //pats.size()
				index++) {
			MnistPattern pat = pats.get(index);
			patToInput(pat, input);
			patToOutput(pat, target);
			Matrix output = nnet.feedForward(input);
			for (int i = 0; i < target.getVectorSize(); i++) {
				double e = output.getVectorItem(i);
				st3.addValue(e);
			}
			output.mSub(target, error);
			error.termAbs(error);

			for (int i = 0; i < error.getVectorSize(); i++) {
				double e = error.getVectorItem(i);
				if (e >= 0.5)
					st.addValue(e);
				else
					st2.addValue(e);
			}
			//st.addValue(op.max());
			//st2.addValue(op.min());
			max.mMax(error, max);
		}
		st.stop();
		st2.stop();
		st3.stop();

		max.printM("MAX");
		System.out.println(st.toString());
		System.out.println("MIN");
		System.out.println(st2.toString());
		System.out.println("Vals");
		System.out.println(st3.toString());
		Marker.release();

		for (int index = 0; index < nnet.layers.size(); index++) {
			MyLayer l = nnet.layers.get(index);
			System.out.println("\nWeight " + index);
			System.out.println(l.weight.calcItemStatistics());
			l.maxInputError.printM("Max input Error");
			l.tmpOutput.printM("tmpOutput");
		}
	}

	void printM(Matrix m, String desc) {
		System.out.println(m.toMatlabString(desc));
	}
	
	void doIt2() throws Exception {
		int sizeInput = 15;
		int sizeOutput = 4;
		//MyLayer l = new MyLayer(sizeInput, sizeOutput, 1);
		MyNet l = new MyNet(MyLayer.class, sizeInput, 10, sizeOutput);
		Matrix input = new Matrix(sizeInput, 1);
		Matrix error = new Matrix(sizeOutput, 1);
		Matrix target = new Matrix(sizeOutput, 1);

		l.eraseMemory();
		for (int epoch = 0; epoch < 4; epoch++) {
			System.out.println("---------------------\nEPOCH "  + epoch);
			for (int index = 0;
					index < sizeInput;
					index++) {
				boolean print = index == 0;
				if (print)
					for (int i = 0; i < l.layers.size(); i++) {
						MyLayer ll = l.layers.get(i);
						printM(ll.inputError, ("IER " + i));
						System.out.println(ll.output.toMatlabString("OUT " + i));
						//System.out.println(ll.weight.toMatlabString("W"));
					}

				if (print)
					System.out.println("At index " + index);
				for (int i = 0; i < input.getVectorSize(); i++) {
					input.setVectorItem(i, index == i ? 0.95 : 0.05);
				}
				for (int i = 0; i < target.getVectorSize(); i++)
					target.setVectorItem(i, ((index + 1) & (1 << i)) == 0 ? 0.05 : 0.95);
				Matrix output = l.feedForward(input);
				if (print) {
					System.out.println(input.toMatlabString("Input"));
					System.out.println(target.toMatlabString("Target"));
					System.out.println(output.toMatlabString("Output"));
				}
				output.mSub(target, error);
				if (print)
					System.out.println(error.toMatlabString("E1"));
				//Matrix inputError =
				l.backPropagate(error);
				//System.out.println(inputError.toMatlabString("IE"));
				output = l.feedForward(input);
				output.mSub(target, error);
				if (print)
					System.out.println(error.toMatlabString("E2"));


			}
			//System.out.println(l.weight.toMatlabString("W"));
		}

	}

	public static void main(String[] args) throws Exception {
		new MyMnistDataTest().doIt2();
//		System.out.println("Done.");
	}
}
