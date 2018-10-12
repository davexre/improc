package com.slavi.ann.test.v2.test;

import java.util.List;
import java.util.Random;

import org.openimaj.math.geometry.transforms.MatrixTransformProvider;

import com.slavi.ann.test.dataset.MatrixDataPointPair;
import com.slavi.ann.test.dataset.MatrixTestData;
import com.slavi.ann.test.v2.Network;
import com.slavi.ann.test.v2.NetworkBuilder;
import com.slavi.ann.test.v2.activation.BinaryOutputLayer;
import com.slavi.ann.test.v2.activation.SigmoidLayer;
import com.slavi.ann.test.Utils;
import com.slavi.ann.test.v2.connection.ConvolutionLayer;
import com.slavi.ann.test.v2.connection.FullyConnectedLayer;
import com.slavi.math.matrix.Matrix;

public class ConvolutionLayerTest2 {
	public static void randomMatrix(Matrix dest) {
		Random random = new Random();
		for (int i = dest.getVectorSize() - 1; i >= 0; i--)
			dest.setVectorItem(i, random.nextDouble() * 2.0 - 1.0);
	}

	void doIt() throws Exception {
		int inputSizeX = 3;
		int inputSizeY = 3;
		int outputSize = 3;
		//List<ConvolutionTestDataPoint> trainset = ConvolutionTestData.readDataSet(getClass().getResourceAsStream("ConvolutionLayerTest2.txt"));
		Matrix w = Matrix.fromOneLineString("0.1 0.3 0.35; 1 0.7 1; 0.5 1 0.7");
		FullyConnectedLayer.normalizeWeights(w);
		FullyConnectedLayer l = new FullyConnectedLayer(inputSizeX * inputSizeY, outputSize, 1);
//		w.copyTo(l.weight);
//		w.makeE();
//		w.makeR(0.5);
		randomMatrix(l.weight);
		NetworkBuilder nb = new NetworkBuilder(inputSizeX, inputSizeY)
				.addLayer(l)
//				.addConstScaleAndBiasLayer(2, 0)
				.addSigmoidLayer()
				//.addReLULayer()
				//.addConstScaleAndBiasLayer(1, -0.1)
				//.addLayer(new BinaryOutputLayer())
				;
		List<MatrixDataPointPair> trainset = MatrixTestData.generateDataSet(nb.build(), inputSizeX, inputSizeY, 500);
		MatrixTestData.checkDataSet(trainset);

//		if (true)
//			return;
		MatrixDataPointPair p0 = trainset.get(0);
		Matrix m = new Matrix();
		p0.toInputMatrix(m);

		Network net = new NetworkBuilder(inputSizeX, inputSizeY)
				.addFullyConnectedLayer(outputSize)
//				.addConstScaleAndBiasLayer(3, 0)
				.addSigmoidLayer()
				//.addReLULayer()
				//.addConstScaleAndBiasLayer(0.01, 0)
				//.addConstScaleAndBiasLayer(1, -0.1)
				//.addLayer(new BinaryOutputLayer())
				.build();
		FullyConnectedLayer l2 = (FullyConnectedLayer) net.get(0);
//		w.copyTo(l2.weight);
//		l2.weight.rMul(2.1);
//		l2.weight.makeR(1);
		Utils.randomMatrix(l2.weight);
		new Trainer() {
			public void epochComplete() {
//				System.out.println(w.toMatlabString("W1"));
//				System.out.println(l2.weight.toMatlabString("W2"));
			}
		}.train(net, trainset, 15);
	}

	public static void main(String[] args) throws Exception {
		new ConvolutionLayerTest2().doIt();
		System.out.println("Done.");
	}
}

