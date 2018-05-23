package com.slavi.ann.test.v2.test;

import java.util.List;

import com.slavi.ann.test.DatapointPair;
import com.slavi.ann.test.dataset.MnistData;
import com.slavi.ann.test.dataset.MnistData.MnistPattern;
import com.slavi.ann.test.v2.Network;
import com.slavi.ann.test.v2.NetworkBuilder;
import com.slavi.ann.test.v2.activation.DebugLayer;
import com.slavi.math.adjust.MatrixStatistics;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Marker;

public class MyMnistDataTest {
	void doIt2() throws Exception {
		List<? extends DatapointPair> trainset = MnistData.readMnistSet(false); //.subList(0, 30);
		MatrixStatistics stIn = new MatrixStatistics();
		MatrixStatistics stOut = new MatrixStatistics();
		stIn.start();
		stOut.start();
		Matrix input = new Matrix();
		Matrix output = new Matrix();

		for (DatapointPair pair : trainset) {
			MnistPattern mp = (MnistPattern) pair;
			if (mp.label == 8)
				System.out.println(mp.patternNumber);
			//System.out.println(pair);
			pair.toInputMatrix(input);
			pair.toOutputMatrix(output);
			stIn.addValue(input);
			stOut.addValue(output);
		}

		stIn.stop();
		stOut.stop();

//		System.out.println(stIn.toString());
//		System.out.println("------------");
//		System.out.println(stOut.toString());
	}

	void doIt() throws Exception {
		Marker.mark("Read");
		List<? extends DatapointPair> trainset = MnistData.readMnistSet(false).subList(0, 30); // Number 8 is missing until index 61,84, 110
		//List<? extends DatapointPair> trainset = TwoSpiralsData.dataSet(100);
		Marker.release();

		DatapointPair pair0 = trainset.get(0);
		Matrix input0 = new Matrix();
		Matrix output0 = new Matrix();
		pair0.toInputMatrix(input0);
		pair0.toOutputMatrix(output0);
		NetworkBuilder nb = new NetworkBuilder(input0.getSizeX(), input0.getSizeY())
				// MNIST data
				.addConvolutionLayer(5)
				.addConstScaleAndBiasLayer(10.0 / 25, -5)
				.addSigmoidLayer()
				.addFullyConnectedLayer(10).addSigmoidLayer()
				.addFullyConnectedLayer(output0.getVectorSize()).addSigmoidLayer()
				.addDebugLayer("last", DebugLayer.off)

/*
//				.addConstScaleAndBiasLayer(2, -1)
				.addConvolutionLayer(5)
//				.addDebugLayer("After convolution", DebugLayer.defaultStyle, DebugLayer.off)
//				.addConstScaleAndBiasLayer(10.0 / 25, -5)
				.addSigmoidLayer()
//				.addReLULayer()

				.addFullyConnectedLayer(30).addSigmoidLayer()
				.addFullyConnectedLayer(output0.getVectorSize()).addSigmoidLayer()
*/
/*
				// TwoSpiralsData
				.addFullyConnectedLayer(4).addSigmoidLayer()
				.addFullyConnectedLayer(output0.getVectorSize()).addSigmoidLayer()
				.addDebugLayer("last", DebugLayer.off)*/
				;
		System.out.println(nb.describe());
		Network net = nb.build();
//		System.out.println(((ConvolutionLayer) net.get(0)).kernel.normalize());
		Trainer.train(net, trainset, 1000);
	}

	public static void main(String[] args) throws Exception {
		new MyMnistDataTest().doIt();
		System.out.println("Done.");
	}
}
