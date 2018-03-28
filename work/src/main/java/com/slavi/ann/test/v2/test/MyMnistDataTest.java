package com.slavi.ann.test.v2.test;

import java.util.ArrayList;
import java.util.List;

import com.slavi.ann.test.MnistData;
import com.slavi.ann.test.MnistData.MnistPattern;
import com.slavi.ann.test.v2.Network;
import com.slavi.ann.test.v2.NetworkBuilder;
import com.slavi.ann.test.v2.activation.DebugLayer;
import com.slavi.ann.test.v2.connection.ConvolutionLayer;
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
		
		NetworkBuilder nb = new NetworkBuilder(28, 28)
				.addConvolutionLayer(5)
//				.addDebugLayer("After convolution", DebugLayer.defaultStyle, DebugLayer.off)
				.addConstScaleAndBiasLayer(10.0 / 25, -5)
				.addSigmoidLayer()
//				.addReLULayer()

//				.addFullyConnectedLayer(50).addSigmoidLayer()
//				.addFullyConnectedLayer(30).addSigmoidLayer()

				.addFullyConnectedLayer(10)
//				.addConstScaleAndBiasLayer()
				.addSigmoidLayer()

//				.addLayer(new DebugLayer("fully connected", DebugLayer.defaultStyle))
				;
		System.out.println(nb.describe());
		Network net = nb.build();
		System.out.println(((ConvolutionLayer) net.get(0)).kernel.normalize());
		Trainer.train(net, trainset, 1000);
	}

	public static void main(String[] args) throws Exception {
		new MyMnistDataTest().doIt();
		System.out.println("Done.");
	}
}
