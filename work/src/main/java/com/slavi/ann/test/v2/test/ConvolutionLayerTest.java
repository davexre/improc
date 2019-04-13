package com.slavi.ann.test.v2.test;

import java.util.List;

import com.slavi.ann.test.dataset.BinaryDigits;
import com.slavi.ann.test.dataset.BinaryDigits.BinaryDigitsPattern;
import com.slavi.ann.test.v2.Network;
import com.slavi.ann.test.v2.NetworkBuilder;

public class ConvolutionLayerTest {

	void doIt() throws Exception {
		List<BinaryDigitsPattern> trainset = BinaryDigits.dataSet();

		Network net = new NetworkBuilder(4, 4)
				.addConvolutionLayer(3)
//				.addConvolutionSameSizeLayer(3)
//				.addSubsamplingAvgLayer(2)
//				.addFullyConnectedLayer(4)
				.addFullyConnectedLayer(4)
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
		new Trainer().train(net, trainset, 4);
	}

	public static void main(String[] args) throws Exception {
		new ConvolutionLayerTest().doIt();
		System.out.println("Done.");
	}
}

