package com.slavi.ann.test.v2.test;

import java.util.List;

import com.slavi.ann.test.DatapointPair;
import com.slavi.ann.test.Utils;
import com.slavi.ann.test.dataset.MatrixDataPointPair;
import com.slavi.ann.test.dataset.MatrixTestData;
import com.slavi.ann.test.v2.Layer.LayerWorkspace;
import com.slavi.ann.test.v2.activation.ScaleAndBiasLayer;
import com.slavi.ann.test.v2.connection.FullyConnectedLayer;
import com.slavi.math.adjust.MatrixStatistics;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public class CalcScales {
	MatrixStatistics inputStat = new MatrixStatistics();
	MatrixStatistics outputStat = new MatrixStatistics();

	Matrix inputBias = new Matrix();
	Matrix inputScale = new Matrix();
	Matrix outputBias = new Matrix();
	Matrix outputScale = new Matrix();

	public static CalcScales calcStatistics(List<? extends DatapointPair> pairs) {
		CalcScales r = new CalcScales();

		Matrix input = new Matrix();
		Matrix output = new Matrix();
		r.inputStat.start();
		r.outputStat.start();
		for (DatapointPair pair : pairs) {
			pair.toInputMatrix(input);
			pair.toOutputMatrix(output);
			r.inputStat.addValue(input);
			r.outputStat.addValue(output);
		}
		r.inputStat.stop();
		r.outputStat.stop();

		calcScaleAndBias(r.inputStat, r.inputScale, r.inputBias);
		calcScaleAndBias(r.outputStat, r.outputScale, r.outputBias);

		return r;
	}

	public static void calcScaleAndBias(MatrixStatistics stat, Matrix scale, Matrix bias) {
		Matrix avg = stat.getAvgValue();
		bias.resize(avg.getSizeX(), avg.getSizeY());
		scale.resize(avg.getSizeX(), avg.getSizeY());

		for (int i = bias.getVectorSize() - 1; i >= 0; i--) {
/*			double s = 1 / (2 * Math.max(
					msout.getMaxX().getVectorItem(i) - avg.getVectorItem(i),
					avg.getVectorItem(i) - msout.getMinX().getVectorItem(i)));
			double b = 0.5 - avg.getVectorItem(i) * s;*/

			double s = stat.getMaxX().getVectorItem(i) - stat.getMinX().getVectorItem(i);
			s = Math.abs(s) > 0.001 ? (Utils.valueHigh - Utils.valueLow) / s : 1.0;
			double b = Utils.valueLow - stat.getMinX().getVectorItem(i) * s;
			bias.setVectorItem(i, b);
			scale.setVectorItem(i, s);
		}
	}

	public static void checkUnused(MatrixStatistics stat) {
		// stat.getStdDeviation().min();
	}

	public static void main(String[] args) throws Exception {
		//List<? extends DatapointPair> trainset = MnistData.readDataSet(false); //.subList(0, 30);

		Matrix w = Matrix.fromOneLineString("0.1 0.3 0.35; 1 0.7 1; 0.5 1 0.7");
		FullyConnectedLayer.normalizeWeights(w);
		List<MatrixDataPointPair> trainset = MatrixTestData.generateFullyConnectedDataSet(w, 20);

		CalcScales calcScales = CalcScales.calcStatistics(trainset);

		calcScales.outputScale.printM("-- computed scale:");
		calcScales.outputBias.printM("-- computed bias:");

		System.out.println("------------");
		//System.out.println(calcScales.inputStat.toString(Statistics.CStatAll));
		System.out.println("------------ output stat");
		System.out.println(calcScales.outputStat.toString(Statistics.CStatAll));

		MatrixStatistics inputStat = new MatrixStatistics();
		MatrixStatistics outputStat = new MatrixStatistics();
		inputStat.start();
		outputStat.start();

		Matrix input = new Matrix();
		Matrix output = new Matrix();

		ScaleAndBiasLayer l = new ScaleAndBiasLayer(calcScales.outputScale, calcScales.outputBias);
		LayerWorkspace ws = l.createWorkspace();
		for (DatapointPair i : trainset) {
			i.toInputMatrix(input);
			i.toOutputMatrix(output);
			Matrix m = ws.feedForward(output);
			inputStat.addValue(input);
			outputStat.addValue(m);
		}

		inputStat.stop();
		outputStat.stop();

		System.out.println("------------");
		//System.out.println(inputStat.toString(Statistics.CStatAll));
		System.out.println("------------ output stat");
		System.out.println(outputStat.toString(Statistics.CStatAll));

		System.out.println("Done.");
	}
}
