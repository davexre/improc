package com.slavi.ann.test.v2.test;

import java.util.List;

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
	void doIt2() throws Exception {
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
		Utils.randomMatrix(l.weight);
		NetworkBuilder nb = new NetworkBuilder(inputSizeX, inputSizeY)
				.addLayer(l)
				.addConstScaleAndBiasLayer(5, -10)
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
				.addConstScaleAndBiasLayer(5, -10)
				.addSigmoidLayer()
				//.addConstScaleAndBiasLayer(0.01, 0)
				//.addConstScaleAndBiasLayer(1, -0.1)
				//.addLayer(new BinaryOutputLayer())
				.build();
		FullyConnectedLayer l2 = (FullyConnectedLayer) net.get(0);
//		w.copyTo(l2.weight);
//		l2.weight.rMul(2.1);
//		l2.weight.makeR(1);
		Utils.randomMatrix(l2.weight);
		com.slavi.ann.test.Utils.randomMatrix(l2.weight);
		new Trainer() {
			public void epochComplete() {
				System.out.println(w.toMatlabString("W1"));
				System.out.println(l2.weight.toMatlabString("W2"));
			}
		}.train(net, trainset, 15);
	}

	void doIt() throws Exception {
		//List<ConvolutionTestDataPoint> trainset = ConvolutionTestData.readDataSet(getClass().getResourceAsStream("ConvolutionLayerTest2.txt"));
		Matrix kernel = Matrix.fromOneLineString("0.1 0.3 0.35; 0 0.7 0; 0.5 0 0.7");
//		kernel.makeE();
		NetworkBuilder nb = new NetworkBuilder(5, 5);
		ConvolutionLayer l1 = new ConvolutionLayer(kernel.getSizeX(), kernel.getSizeY(), 1);
		kernel.copyTo(l1.kernel);
		nb.addLayer(l1);
		nb.addSigmoidLayer();
		nb.addFullyConnectedLayer(2);
		FullyConnectedLayer l2 = (FullyConnectedLayer) nb.getLastLayer();
		Utils.randomMatrix(l2.weight);
		nb.addSigmoidLayer();

		List<MatrixDataPointPair> trainset = MatrixTestData.generateConvolutionDataSet(nb.build(), nb.inputSize[0], nb.inputSize[1], 500);

		MatrixDataPointPair p0 = trainset.get(0);
		Matrix mi = new Matrix();
		Matrix mo = new Matrix();
		p0.toInputMatrix(mi);
		p0.toOutputMatrix(mo);

		{
			NetworkBuilder nb2 = new NetworkBuilder(mi.getSizeX(), mi.getSizeY());
			Network net = nb2
					.addConvolutionLayer(6).addSigmoidLayer()
					.addFullyConnectedLayer(mo.getVectorSize())
					.addSigmoidLayer()
//					.addReLULayer()
					.build();
			ConvolutionLayer l = (ConvolutionLayer) net.get(0);
//			l.kernel.makeR(1);
//			Utils.randomMatrix(l.kernel);
			System.out.println(nb2.describe());
			new Trainer().train(net, trainset, 10);
			System.out.println(kernel.toMatlabString("K1"));
			System.out.println(l.kernel.toMatlabString("K2"));
		}
/*
		{
			Network net = new NetworkBuilder(m.getSizeX(), m.getSizeY())
					.addFullyConnectedLayer(1)
					.build();
			FullyConnectedLayer l = (FullyConnectedLayer) net.get(0);
			l.weight.makeR(1);
			new Trainer().train(net, trainset, 1);
			System.out.println(kernel.toMatlabString("K1"));
			System.out.println(l.weight.toMatlabString("K2"));
		}*/
	}

	public static void main(String[] args) throws Exception {
		new ConvolutionLayerTest2().doIt2();
		System.out.println("Done.");
	}
}

