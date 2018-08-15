package com.slavi.ann.test.v2.test;

import java.util.List;

import com.slavi.ann.test.dataset.MatrixDataPointPair;
import com.slavi.ann.test.dataset.MatrixTestData;
import com.slavi.ann.test.v2.Network;
import com.slavi.ann.test.v2.NetworkBuilder;
import com.slavi.ann.test.v2.connection.ConvolutionLayer;
import com.slavi.ann.test.v2.connection.FullyConnectedLayer;
import com.slavi.math.matrix.Matrix;

public class ConvolutionLayerTest2 {
	void doIt2() throws Exception {
		//List<ConvolutionTestDataPoint> trainset = ConvolutionTestData.readDataSet(getClass().getResourceAsStream("ConvolutionLayerTest2.txt"));
		Matrix w = Matrix.fromOneLineString("0.1 0.3 0.35 1 0.7 1 0.5 1 0.7");
//		w.makeE();
		List<MatrixDataPointPair> trainset = MatrixTestData.generateFullyConnectedDataSet(w, 20);

		MatrixDataPointPair p0 = trainset.get(0);
		Matrix m = new Matrix();
		p0.toInputMatrix(m);

		Network net = new NetworkBuilder(m.getSizeX(), m.getSizeY())
				.addFullyConnectedLayer(1)
				.build();
		FullyConnectedLayer l = (FullyConnectedLayer) net.get(0);
		w.copyTo(l.weight);
		l.weight.rMul(1.1);
//		l.weight.makeR(1);
		new Trainer() {
			public void epochComplete() {
				System.out.println(w.toMatlabString("W1"));
				System.out.println(l.weight.toMatlabString("W2"));
			}
		}.train(net, trainset, 20);
	}

	void doIt() throws Exception {
		//List<ConvolutionTestDataPoint> trainset = ConvolutionTestData.readDataSet(getClass().getResourceAsStream("ConvolutionLayerTest2.txt"));
		Matrix kernel = Matrix.fromOneLineString("0.1 0.3 0.35; 0 0.7 0; 0.5 0 0.7");
//		kernel.makeE();
		List<MatrixDataPointPair> trainset = MatrixTestData.generateConvolutionDataSet(kernel, 3, 3, 20);

		MatrixDataPointPair p0 = trainset.get(0);
		Matrix m = new Matrix();
		p0.toInputMatrix(m);

		{
			Network net = new NetworkBuilder(m.getSizeX(), m.getSizeY())
					.addConvolutionLayer(3)
					.build();
			ConvolutionLayer l = (ConvolutionLayer) net.get(0);
			l.kernel.makeR(1);
			new Trainer().train(net, trainset, 5);
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
		new ConvolutionLayerTest2().doIt();
		System.out.println("Done.");
	}
}

