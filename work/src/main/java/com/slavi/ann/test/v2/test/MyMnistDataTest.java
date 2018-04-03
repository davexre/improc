package com.slavi.ann.test.v2.test;

import java.util.ArrayList;
import java.util.List;

import com.slavi.ann.test.DatapointPair;
import com.slavi.ann.test.MnistData;
import com.slavi.ann.test.TwoSpiralsData;
import com.slavi.ann.test.v2.Network;
import com.slavi.ann.test.v2.NetworkBuilder;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Marker;

public class MyMnistDataTest {
	void doIt() throws Exception {
		Marker.mark("Read");
		/*List<? extends DatapointPair> pats = MnistData.readMnistSet(false);
		List<DatapointPair> trainset = new ArrayList<>();
		for (int i = 0; i < 30; i++)
			trainset.add(pats.get(i));*/
		List<? extends DatapointPair> trainset = TwoSpiralsData.dataSet(100);
		Marker.release();
		
		DatapointPair pair0 = trainset.get(0);
		Matrix m = new Matrix();
		pair0.toInputMatrix(m);
		NetworkBuilder nb = new NetworkBuilder(m.getSizeX(), m.getSizeY())
//				.addConstScaleAndBiasLayer(2, -1)
//				.addConvolutionLayer(5)
//				.addDebugLayer("After convolution", DebugLayer.defaultStyle, DebugLayer.off)
//				.addConstScaleAndBiasLayer(10.0 / 25, -5)
//				.addSigmoidLayer()
//				.addReLULayer()

				.addFullyConnectedLayer(8).addSigmoidLayer()
//				.addFullyConnectedLayer(30).addSigmoidLayer()

				.addFullyConnectedLayer(8)
//				.addConstScaleAndBiasLayer()
				.addSigmoidLayer()

//				.addLayer(new DebugLayer("fully connected", DebugLayer.defaultStyle))
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
