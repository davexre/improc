package com.slavi.ann.test;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.ann.NNet;
import com.slavi.ann.test.MnistData.MnistPattern;
import com.slavi.math.MathUtil;
import com.slavi.util.Marker;

public class MnistDataTest {

	void doIt() throws Exception {
		List<MnistPattern> pats = MnistData.readMnistSet(false);

		ObjectMapper mapper = Utils.jsonMapper();
		int insize = 28*28;
		NNet nnet = new NNet(NNSimpleLayer3.class,
				insize,
				10, 10);
		nnet.setLearningRate(1);
		nnet.setMomentum(1);
		nnet.eraseMemory();

		int maxPattern = pats.size();
		int maxPatternTrain = maxPattern / 2;

		Marker.mark("Total");
		Marker.mark("Train");
		double input[] = new double[insize];
		double target[] = new double[10];
		double error[] = new double[10];
		//for (int epoch = 0; epoch < 1; epoch++)
			for (int index = 0;
					index < maxPatternTrain; //pats.size()
					index++) {
				MnistPattern pat = pats.get(index);
				for (int i = 0; i < insize; i++)
					input[i] = MathUtil.mapValue(pat.image[i], 0, 255, 0, 1);
				for (int i = 0; i < 10; i++)
					target[i] = pat.label == i ? 1 : 0;
				nnet.feedForward(input);
				double output[] = nnet.getOutput();
				for (int i = error.length - 1; i >= 0; i--)
					error[i] = output[i] - target[i];
				nnet.backPropagate(error);
			}
		Marker.releaseAndMark("Recall");
		double max[] = new double[10];
		Arrays.fill(max, 0);

		for (int index = 0;
				index < maxPattern; //pats.size()
				index++) {
			MnistPattern pat = pats.get(index);
			for (int i = 0; i < insize; i++)
				input[i] = MathUtil.mapValue(input[i], 0, 255, 0, 1);
			for (int i = 0; i < 10; i++)
				target[i] = pat.label == i ? 1 : 0;
			nnet.feedForward(input);
			double output[] = nnet.getOutput();
			for (int i = error.length - 1; i >= 0; i--)
				error[i] = Math.abs(output[i] - target[i]);
			for (int i = error.length - 1; i >= 0; i--)
				max[i] = Math.max(max[i], error[i]);
		}

		System.out.print("[");
		for (int i = 0; i < max.length; i++)
			System.out.print(String.format("%8.5f ", max[i]));
		System.out.println("]");
		//System.out.println(mapper.writeValueAsString(nnet));
		Marker.release();

	}

	public static void main(String[] args) throws Exception {
		new MnistDataTest().doIt();
//		System.out.println("Done.");
	}
}
