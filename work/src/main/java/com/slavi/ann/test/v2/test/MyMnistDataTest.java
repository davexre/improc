package com.slavi.ann.test.v2.test;

import java.util.ArrayList;
import java.util.List;

import com.slavi.ann.test.MnistData;
import com.slavi.ann.test.MnistData.MnistPattern;
import com.slavi.ann.test.v2.Network;
import com.slavi.ann.test.v2.NetworkBuilder;
import com.slavi.util.Marker;

public class MyMnistDataTest {
	int insize = MnistPattern.size; // => 784

	void doIt() throws Exception {
		Marker.mark("Read");
		List<MnistPattern> pats = MnistData.readMnistSet(false);
		Marker.release();

		ArrayList<MnistPattern> trainset = new ArrayList<>();
		for (int i = 0; i < 30; i++)
			trainset.add(pats.get(i));
		
		Network net = new NetworkBuilder(28, 28)
//				.addConvolutionLayer(5)
				.addConvolutionLayer(5)
				.addSigmoidLayer()
//				.addReLULayer()
//				.addFullyConnectedLayer(50).addSigmoidLayer()
//				.addFullyConnectedLayer(30).addSigmoidLayer()
				.addFullyConnectedLayer(10).addSigmoidLayer()
				.build();
				
//				new Network(
//				new ConvolutionWithStrideLayer(4, 4, 1, 1, 1),
/*				new ConvolutionWithStrideLayer(9, 9, 2, 2, 1),
				new ConvolutionWithStrideLayer(5, 5, 2, 2, 1),
				new FullyConnectedLayer(49, 10, 1), new SigmoidLayer()
	*/			
//				new FullyConnectedLayer(insize, 50, 1), new SigmoidLayer(), 
//				new FullyConnectedLayer(50, 10, 1), new SigmoidLayer()
//				new FullyConnectedLayer(insize, 10, 1), new SigmoidLayer()
/*				new FullyConnectedLayer(784, 700, 1), new SigmoidLayer(),
				new FullyConnectedLayer(700, 500, 1), new SigmoidLayer(),
				new FullyConnectedLayer(500, 100, 1), new SigmoidLayer(),
				new FullyConnectedLayer(100, 10, 1), new SigmoidLayer()*/
//				);
		Trainer.train(net, trainset, 100);
	}

	public static void main(String[] args) throws Exception {
		new MyMnistDataTest().doIt();
		System.out.println("Done.");
	}
}
