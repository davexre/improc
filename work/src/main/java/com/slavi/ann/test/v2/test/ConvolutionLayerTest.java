package com.slavi.ann.test.v2.test;

import java.util.ArrayList;

import com.slavi.ann.test.v2.ConvolutionSameSizeLayer;
import com.slavi.ann.test.v2.ConvolutionWithStrideLayer;
import com.slavi.ann.test.v2.FullyConnectedLayer;
import com.slavi.ann.test.v2.Layer.Workspace;
import com.slavi.ann.test.v2.Network;
import com.slavi.ann.test.v2.Network.NetWorkSpace;
import com.slavi.math.adjust.MatrixStatistics;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public class ConvolutionLayerTest {

	void doIt() throws Exception {
		Network net = new Network(
//				new ConvolutionSameSizeLayer(4, 4, 1),
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
		
		Matrix input = new Matrix(4, 4);
		Matrix target = new Matrix(4, 1);

		int sizeInput = input.getVectorSize();
		MatrixStatistics ms = new MatrixStatistics();
		Matrix error = new Matrix();
		Matrix tmpErr = new Matrix();
		NetWorkSpace ws = net.createWorkspace();
		ArrayList<Workspace> wslist = new ArrayList<>();
		wslist.add(ws);
		for (int epoch = 0; epoch < 100; epoch++) {
			System.out.println("---------------------\nEPOCH "  + epoch);
			ms.start();
			for (int index = 0; index < sizeInput; index++) {
				boolean print = index == 5 || epoch==-10 || epoch==-99; // || epoch == 19;
				print = false;
				if (print) System.out.println("Index = " + index);

				for (int i = 0; i < input.getVectorSize(); i++)
					input.setVectorItem(i, index == i ? 0.95 : 0.05);
				for (int i = 0; i < target.getVectorSize(); i++)
					target.setVectorItem(i, (index & (1 << i)) == 0 ? 0.05 : 0.95);

				Matrix output = ws.feedForward(input);
				output.mSub(target, error);
				Matrix inputError = ws.backPropagate(error);

				if (print) {
					input.printM("input");
					inputError.printM("inputError");
					target.printM("target");
					output.printM("output");
					error.printM("error");
/*
					for (int i = 0; i < net.size(); i++) {
 */
					{
						int i = 0;
						System.out.println(ws.workspaces.get(i));
					}
					System.out.println();
				}
				error.termAbs(tmpErr);
				ms.addValue(tmpErr);
			}
			net.applyWorkspaces(wslist);
			ms.stop();
			System.out.println(ms.toString(Statistics.CStatStdDev | Statistics.CStatMinMax));
			if (ms.getAbsMaxX().max() < 0.2) {
				System.out.println("Threshold reached at epoch " + epoch);
				break;
			}
		}
	}

	public static void main(String[] args) throws Exception {
		new ConvolutionLayerTest().doIt();
		System.out.println("Done.");
	}
}

