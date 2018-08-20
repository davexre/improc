package com.slavi.ann.test.v2.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import com.slavi.ann.test.DatapointPair;
import com.slavi.ann.test.Utils;
import com.slavi.ann.test.dataset.Cifar10;
import com.slavi.ann.test.dataset.Cifar100;
import com.slavi.ann.test.dataset.IrisData;
import com.slavi.ann.test.dataset.MnistData;
import com.slavi.ann.test.dataset.MnistData.MnistPattern;
import com.slavi.ann.test.v2.Network;
import com.slavi.ann.test.v2.NetworkBuilder;
import com.slavi.ann.test.v2.activation.MaxWinLayer;
import com.slavi.math.adjust.MatrixStatistics;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Marker;
import com.slavi.util.MatrixUtil;

/*
a=A'*A;
[u,s,v]=svd(a);
ap=pinv(a);
dX=(ap*A'*L)';
max(abs(dX))
P1=P+dX;
save octave/A.mat P1

A1=A;
L1=L;


 */
public class MyMnistDataTest {
	void doIt() throws Exception {
		Marker.mark("Read");
		List<? extends DatapointPair> trainset = MnistData.readDataSet(false).subList(0, 30); // Number 8 is missing until index 61,84, 110
		//List<? extends DatapointPair> trainset = TwoSpiralsData.dataSet(100);
//		List<? extends DatapointPair> trainset = IrisData.readDataSet(true);
		Marker.release();
		//Utils.computeDataStatistics(trainset);

		DatapointPair pair0 = trainset.get(0);
		Matrix input0 = new Matrix();
		Matrix output0 = new Matrix();
		pair0.toInputMatrix(input0);
		pair0.toOutputMatrix(output0);
		NetworkBuilder nb = new NetworkBuilder(input0.getSizeX(), input0.getSizeY())
				// MNIST data
				//.addConstScaleAndBiasLayer(2, -1)
				.addConvolutionLayer(5).addSigmoidLayer()
				//.addDebugLayer("A1", Statistics.CStatMinMax, Statistics.CStatMinMax)
				//.addConstScaleAndBiasLayer(10.0 / 25, -5)
				//.addDebugLayer("A2", Statistics.CStatDetail, Statistics.CStatDetail)
				//.addDebugLayer("WTF", Statistics.CStatMinMax, Statistics.CStatMinMax)
				//.addFullyConnectedLayer(10).addSigmoidLayer()
				.addFullyConnectedLayer(output0.getVectorSize()).addSigmoidLayer()
				//.addDebugLayer("last", DebugLayer.off)

/*
//				.addConstScaleAndBiasLayer(2, -1)
				.addConvolutionLayer(5)
//				.addDebugLayer("After convolution", DebugLayer.defaultStyle, DebugLayer.off)
//				.addConstScaleAndBiasLayer(10.0 / 25, -5)
				.addSigmoidLayer()
//				.addReLULayer()

				.addFullyConnectedLayer(30).addSigmoidLayer()
				.addFullyConnectedLayer(output0.getVectorSize()).addSigmoidLayer()
*/
/*
				// TwoSpiralsData
				.addFullyConnectedLayer(4).addSigmoidLayer()
				.addFullyConnectedLayer(output0.getVectorSize()).addSigmoidLayer()
				.addDebugLayer("last", DebugLayer.off)*/
				// Iris data
//				.addFullyConnectedLayer(3).addSigmoidLayer()
				//.addConstScaleAndBiasLayer(10, -5)
/*				.addFullyConnectedLayer(output0.getVectorSize()).addSigmoidLayer()
				.addFullyConnectedLayer(output0.getVectorSize()).addSigmoidLayer()
				.addFullyConnectedLayer(output0.getVectorSize()).addSigmoidLayer()*/
				;
		System.out.println(nb.describe());
		Network net = nb.build();
//		net.loadParams(MatrixUtil.loadOctave(new FileInputStream(new File(System.getProperty("user.home"), "/octave/A.mat"))), 0);

		new Trainer().train2(net, trainset, 2);
		System.out.println(net.get(1));
	}

	public static void main(String[] args) throws Exception {
		new MyMnistDataTest().doIt();
		System.out.println("Done.");
	}
}
