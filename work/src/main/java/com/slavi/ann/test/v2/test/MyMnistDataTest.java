package com.slavi.ann.test.v2.test;

import java.util.ArrayList;
import java.util.List;

import com.slavi.ann.test.MnistData;
import com.slavi.ann.test.MnistData.MnistPattern;
import com.slavi.ann.test.v2.Network;
import com.slavi.ann.test.v2.NetworkBuilder;
import com.slavi.ann.test.v2.activation.DebugLayer;
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
				.addConvolutionLayer(5)
				.addConstScaleAndBiasLayer(10.0 / 25, -5)
				.addSigmoidLayer()
//				.addReLULayer()

//				.addFullyConnectedLayer(50).addSigmoidLayer()
//				.addFullyConnectedLayer(30).addSigmoidLayer()

				.addFullyConnectedLayer(10)
				.addConstScaleAndBiasLayer()
				.addSigmoidLayer()
				.addLayer(new DebugLayer("fully connected", 0, DebugLayer.defaultStyle))

				.build();

		Trainer.train(net, trainset, 1000);
	}

	public static void main(String[] args) throws Exception {
		new MyMnistDataTest().doIt();
		System.out.println("Done.");
	}
}
