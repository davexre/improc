package com.slavi.ann.test.v2.test;

import java.util.List;

import com.slavi.ann.test.dataset.ConvolutionTestData;
import com.slavi.ann.test.dataset.ConvolutionTestData.ConvolutionTestDataPoint;
import com.slavi.ann.test.v2.Network;
import com.slavi.ann.test.v2.NetworkBuilder;
import com.slavi.math.matrix.Matrix;

public class ConvolutionLayerTest2 {
	void doIt() throws Exception {
		List<ConvolutionTestDataPoint> trainset = ConvolutionTestData.readDataSet(getClass().getResourceAsStream("ConvolutionLayerTest2.txt"));
		ConvolutionTestDataPoint p0 = trainset.get(0);
		Matrix m = new Matrix();
		p0.toInputMatrix(m);

		Network net = new NetworkBuilder(m.getSizeX(), m.getSizeY())
//				.addConvolutionLayer(3)
				.addConvolutionSameSizeLayer(3)
//				.addSubsamplingAvgLayer(2)
//				.addFullyConnectedLayer(4)
//				.addFullyConnectedLayer(4)
				.build();
/*		Network net = new Network(
				new ConvolutionSameSizeLayer(4, 4, 1),
//				new ConvolutionSameSizeLayer(4, 4, 1),
//				new ConvolutionLayer(2, 2, 1)
//				new ConvolutionWithStrideLayer(2, 2, 2, 2, 1),
//				new SubsamplingAvgLayer(1, 1),
//				new SubsamplingMaxLayer(1, 1),
				new FullyConnectedLayer(16, 4, 1)

//				new FullyConnectedLayer(16, 16, 1),
//				new FullyConnectedLayer(16, 16, 1),
//				new FullyConnectedLayer(16, 16, 1),
//				new FullyConnectedLayer(16, 4, 1)
//				new FullyConnectedLayer(12, 8, 1),
//				new FullyConnectedLayer(10, 4, 1)
				);
*/
		Trainer.train(net, trainset, 10);
	}

	public static void main(String[] args) throws Exception {
		new ConvolutionLayerTest2().doIt();
		System.out.println("Done.");
	}
}

