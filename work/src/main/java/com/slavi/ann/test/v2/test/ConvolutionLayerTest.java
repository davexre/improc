package com.slavi.ann.test.v2.test;

import java.util.ArrayList;

import com.slavi.ann.test.v2.ConvolutionLayer;
import com.slavi.ann.test.v2.ConvolutionSameSizeLayer;
import com.slavi.ann.test.v2.Layer.Workspace;
import com.slavi.ann.test.v2.Network;
import com.slavi.ann.test.v2.Network.NetWorkSpace;
import com.slavi.math.adjust.MatrixStatistics;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public class ConvolutionLayerTest {

	void doIt() throws Exception {
		Network net = new Network(
				new ConvolutionSameSizeLayer(2, 2, 1)
				,new ConvolutionLayer(2, 2, 1)
				);
		
		Matrix input = new Matrix(4, 4);
		Matrix target = new Matrix(2, 2);

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
				boolean print = index == 1; // || epoch == 19;
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
						ConvolutionLayer.LayerWorkspace w = (ConvolutionLayer.LayerWorkspace) ws.workspaces.get(i);
						ConvolutionLayer ll = (ConvolutionLayer) net.get(i);
						w.inputError.printM("IER " + i);
						w.output.printM("OUT " + i);
						ll.kernel.printM("Kernel " + i);
					}*/
					System.out.println();
				}
				error.termAbs(tmpErr);
				ms.addValue(tmpErr);
			}
			net.applyWorkspaces(wslist);
			ms.stop();
			System.out.println(ms.toString(Statistics.CStatStdDev | Statistics.CStatMinMax));
		}
	}

	public static void main(String[] args) throws Exception {
		new ConvolutionLayerTest().doIt();
		System.out.println("Done.");
	}
}

