package com.slavi.ann.test.v2.test;

import java.util.ArrayList;
import java.util.List;

import com.slavi.ann.test.MnistData;
import com.slavi.ann.test.MnistData.MnistPattern;
import com.slavi.ann.test.v2.ConvolutionSameSizeLayer;
import com.slavi.ann.test.v2.ConvolutionWithStrideLayer;
import com.slavi.ann.test.v2.FullyConnectedLayer;
import com.slavi.ann.test.v2.Layer.Workspace;
import com.slavi.ann.test.v2.Network;
import com.slavi.ann.test.v2.Network.NetWorkSpace;
import com.slavi.math.MathUtil;
import com.slavi.math.adjust.MatrixStatistics;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Marker;

public class MyMnistDataTest {
	int insize = MnistPattern.size; // => 784

	void doIt() throws Exception {
		Marker.mark("Read");
		List<MnistPattern> pats = MnistData.readMnistSet(false);
		Marker.release();

		int outsize = 10;
		Network net = new Network(
//				new ConvolutionWithStrideLayer(4, 4, 1, 1, 1),
/*				new ConvolutionWithStrideLayer(9, 9, 2, 2, 1),
				new ConvolutionWithStrideLayer(5, 5, 2, 2, 1),
				new FullyConnectedLayer(49, 10, 1)
	*/			
//				new FullyConnectedLayer(insize, 50, 1),
//				new FullyConnectedLayer(50, 10, 1)
				new FullyConnectedLayer(insize, 10, 1)
/*				new FullyConnectedLayer(784, 700, 1),
				new FullyConnectedLayer(700, 500, 1),
				new FullyConnectedLayer(500, 100, 1),
				new FullyConnectedLayer(100, 10, 1)*/
				);

		int maxPattern = pats.size();
		int startPattern = 0;
		int maxPatternTrain = 10; //maxPattern / 2;

		Marker.mark("Total");
		Marker.mark("Train");
		Matrix input = new Matrix(28, 28);
		Matrix target = new Matrix(outsize, 1);

		Matrix error = new Matrix();
		Matrix tmpErr = new Matrix();

//		MatrixStatistics ms = new MatrixStatistics();
		MatrixStatistics msErr = new MatrixStatistics();
		NetWorkSpace ws = net.createWorkspace();
		ArrayList<Workspace> wslist = new ArrayList<>();
		wslist.add(ws);
		for (int epoch = 0; epoch < 100; epoch++) {
			System.out.println("--------------------- EPOCH "  + epoch);
//			ms.start();
			msErr.start();
			for (int index = 0;
					index < maxPatternTrain; //pats.size()
					index++) {
				MnistPattern pat = pats.get(startPattern + index);
				pat.toInputMatrix(input);
				pat.toOutputMatrix(target);
				Matrix output = ws.feedForward(input);
				output.mSub(target, error);
				Matrix inputError = ws.backPropagate(error);

//				FullyConnectedLayer.LayerWorkspace tmpws = (FullyConnectedLayer.LayerWorkspace) ws.workspaces.get(0);
//				FullyConnectedLayer tmpl = (FullyConnectedLayer) net.get(0);
				
				error.termAbs(tmpErr);
				msErr.addValue(tmpErr);
				if (index == 5) {
//					System.out.println("x0= " + tmpws.output0.toString());
//					System.out.println(tmpws.output.maxAbs());
//					System.out.println(tmpl.weight.toMatlabString("w"));
				}
//				ms.addValue(tmpws.output0);
			}
//			FullyConnectedLayer tmpl = (FullyConnectedLayer) net.get(0);
//			System.out.println("sumW: " + MathUtil.d4(tmpl.weight.sumAbs() ));
			net.applyWorkspaces(wslist);
//			ms.stop();
			msErr.stop();
//			System.out.println(ms.toString(Statistics.CStatStdDev | Statistics.CStatMinMax));
//			System.out.println("sumW: " + MathUtil.d4(tmpl.weight.sumAbs()) + "/" + MathUtil.d4(tmpl.weight.sumAll()));
//			System.out.println("max abs ms : " + MathUtil.d4(ms.getAbsMaxX().max()));
			System.out.println("max abs err: " + MathUtil.d4(msErr.getAbsMaxX().max()));
//			System.out.println(tmpl.weight.toMatlabString("w"));
			
/*
			FullyConnectedLayer.LayerWorkspace tmpws = (FullyConnectedLayer.LayerWorkspace) ws.workspaces.get(0);
			System.out.println("LAYER 0");
			System.out.println(tmpws);
			tmpws = (FullyConnectedLayer.LayerWorkspace) ws.workspaces.get(1);
			System.out.println("LAYER 1");
			System.out.println(tmpws);*/
/*			if (ms.getAbsMaxX().max() < 0.2) {
				System.out.println("Threshold reached at epoch " + epoch);
				break;
			}*/
		}
//		Marker.release();
	}

	public static void main(String[] args) throws Exception {
		new MyMnistDataTest().doIt();
		System.out.println("Done.");
	}
}
