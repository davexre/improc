package com.slavi.ann.test.v2.test;

import java.util.ArrayList;

import com.slavi.ann.test.DatapointPair;
import com.slavi.ann.test.v2.Network;
import com.slavi.ann.test.v2.NetworkBuilder;
import com.slavi.math.matrix.Matrix;

public class ConvolutionLayerTest {

	public static class BinaryDigitsPattern implements DatapointPair {
		final int number;

		public BinaryDigitsPattern(int number) {
			this.number = number;
		}

		public void toInputMatrix(Matrix dest) {
			dest.resize(4, 4);
			for (int i = 0; i < dest.getVectorSize(); i++)
				dest.setVectorItem(i, number == i ? 0.95 : 0.05);
		}

		public void toOutputMatrix(Matrix dest) {
			dest.resize(4, 1);
			for (int i = 0; i < dest.getVectorSize(); i++)
				dest.setVectorItem(i, (number & (1 << i)) == 0 ? 0.05 : 0.95);
		}

		public String getName() {
			return Integer.toString(number);
		}
	}

	void doIt() throws Exception {
		ArrayList<BinaryDigitsPattern> trainset = new ArrayList<>();
		for (int i = 0; i < 16; i++)
			trainset.add(new BinaryDigitsPattern(i));

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
		Trainer.train(net, trainset, 4);
	}

	public static void main(String[] args) throws Exception {
		new ConvolutionLayerTest().doIt();
		System.out.println("Done.");
	}
}

