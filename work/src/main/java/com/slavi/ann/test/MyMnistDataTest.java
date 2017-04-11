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
				700, 600, 500, 400, 300, 200, 100,
				50, 10);
		nnet.eraseMemory();

		int maxPattern = 100; //pats.size();
		int maxPatternTrain = maxPattern; // / 2;

		Marker.mark("Total");
		Marker.mark("Train");
		Matrix input = new Matrix(nnet.getSizeInput(), 1);
		Matrix op = new Matrix(nnet.getSizeOutput(), 1);
		for (int epoch = 0; epoch < 1; epoch++)
			for (int index = 0;
					index < maxPatternTrain; //pats.size()
					index++) {
				MnistPattern pat = pats.get(index);
				patToInput(pat, input);
				patToOutput(pat, op);
				Matrix t = nnet.feedForward(input);
				op.mSub(t, op);
				nnet.backPropagate(op);
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
		for (int index = 0;
				index < maxPattern; //pats.size()
				index++) {
			MnistPattern pat = pats.get(index);
			patToInput(pat, input);
			patToOutput(pat, op);
			Matrix t = nnet.feedForward(input);
			op.mSub(t, op);
			op.termAbs(op);

			for (int i = 0; i < op.getVectorSize(); i++) {
				double e = op.getVectorItem(i);
				if (e >= 0.5)
					st.addValue(e);
				else
					st2.addValue(e);
			}
			//st.addValue(op.max());
			//st2.addValue(op.min());
			max.mMax(op, max);
		}
		st.stop();
		st2.stop();

		max.printM("MAX");
		System.out.println(st.toString());
		System.out.println("MIN");
		System.out.println(st2.toString());
		Marker.release();
		//nnet.layers.get(nnet.layers.size() - 1).weight.printM("last W");

	}

	public static void main(String[] args) throws Exception {
		new MyMnistDataTest().doIt();
//		System.out.println("Done.");
	}
}
