package com.slavi.ann.test.v2.test;

import java.util.List;

import com.slavi.ann.test.DatapointPair;
import com.slavi.ann.test.dataset.MnistData;
import com.slavi.ann.test.v2.Layer.LayerWorkspace;
import com.slavi.ann.test.v2.activation.ScaleAndBiasLayer;
import com.slavi.math.adjust.MatrixStatistics;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public class CalcScales {

	public static void makeScales(MatrixStatistics ms) {

	}

	static final double valueLow = 0.05;
	static final double valueHigh = 0.95;

	public void doIt(String[] args) throws Exception {
		List<? extends DatapointPair> trainset = MnistData.readDataSet(false); //.subList(0, 30);
		Matrix input = new Matrix();
		Matrix output = new Matrix();

		MatrixStatistics msin = new MatrixStatistics();
		MatrixStatistics msout = new MatrixStatistics();
		msin.start();
		msout.start();

		for (DatapointPair i : trainset) {
			i.toInputMatrix(input);
			i.toOutputMatrix(output);
			msin.addValue(input);
			msout.addValue(output);
		}

		msin.stop();
		msout.stop();

		Matrix avg = msout.getAvgValue();
		Matrix scale = new Matrix(avg.getSizeX(), avg.getSizeY());
		Matrix bias = new Matrix(avg.getSizeX(), avg.getSizeY());

		for (int i = bias.getVectorSize() - 1; i >= 0; i--) {
/*			double s = 1 / (2 * Math.max(
					msout.getMaxX().getVectorItem(i) - avg.getVectorItem(i),
					avg.getVectorItem(i) - msout.getMinX().getVectorItem(i)));
			double b = 0.5 - avg.getVectorItem(i) * s;*/

			double s = msout.getMaxX().getVectorItem(i) - msout.getMinX().getVectorItem(i);
			s = Math.abs(s) > 0.001 ? (valueHigh - valueLow) / s : 1.0;
			double b = valueLow - msout.getMinX().getVectorItem(i) * s;
			bias.setVectorItem(i, b);
			scale.setVectorItem(i, s);
		}

		scale.printM("-- computed scale:");
		bias.printM("-- computed bias:");

		System.out.println("------------");
		//System.out.println(msin.toString(Statistics.CStatAll));
		System.out.println("------------");
		System.out.println(msout.toString(Statistics.CStatAll));

		msin.start();
		msout.start();

		ScaleAndBiasLayer l = new ScaleAndBiasLayer(scale, bias);
		LayerWorkspace ws = l.createWorkspace();
		for (DatapointPair i : trainset) {
			i.toInputMatrix(input);
			i.toOutputMatrix(output);
			Matrix m = ws.feedForward(output);
			msin.addValue(input);
			msout.addValue(m);
		}

		msin.stop();
		msout.stop();

		System.out.println("------------");
		//System.out.println(msin.toString(Statistics.CStatAll));
		System.out.println("------------");
		System.out.println(msout.toString(Statistics.CStatAll));
	}

	public static void main(String[] args) throws Exception {
		new CalcScales().doIt(args);
		System.out.println("Done.");
	}
}
