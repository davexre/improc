package com.slavi.ann.test.v2.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slavi.ann.test.DatapointPair;
import com.slavi.ann.test.v2.Layer;
import com.slavi.ann.test.v2.Layer.LayerWorkspace;
import com.slavi.math.MathUtil;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.adjust.MatrixStatistics;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public class Trainer {

	static class DatapointTrainResult implements Comparable<DatapointTrainResult> {
		int index;
		DatapointPair pair;
		double error;

		public DatapointTrainResult(int index, DatapointPair pair, double error) {
			this.index = index;
			this.pair = pair;
			this.error = error;
		}

		public int compareTo(DatapointTrainResult o) {
			return Double.compare(o.error, error); // Descending order
		}
	}

	public static Matrix calcScales(Layer l, Iterable<? extends DatapointPair> trainset) throws IOException {
		Matrix input = new Matrix();
		Matrix target = new Matrix();
		double imin = Double.MAX_VALUE;
		double imax = Double.MIN_VALUE;
		double tmin = Double.MAX_VALUE;
		double tmax = Double.MIN_VALUE;
		for (DatapointPair pair : trainset) {
			pair.toInputMatrix(input);
			pair.toOutputMatrix(target);
			for (int i = input.getVectorSize() - 1; i >= 0; i--) {
				double v = input.getVectorItem(i);
				if (v < imin) imin = v;
				if (v > imax) imax = v;
			}
			for (int i = target.getVectorSize() - 1; i >= 0; i--) {
				double v = target.getVectorItem(i);
				if (v < tmin) tmin = v;
				if (v > tmax) tmax = v;
			}
		}
		Matrix r = new Matrix(2, 2);
		r.setItem(0, 0, imin);
		r.setItem(1, 0, imax);
		r.setItem(0, 1, tmin);
		r.setItem(1, 1, tmax);
		return r;
	}

	public void epochComplete() {
	}

	Logger log_measurements = LoggerFactory.getLogger(LeastSquaresAdjust.class.getName() + ".measurements");

	public void train(Layer l, List<? extends DatapointPair> trainset, int maxEpochs) throws IOException {
//		Logger LOG = LoggerFactory.getLogger("LOG");
		Matrix input = new Matrix();
		Matrix target = new Matrix();
		Matrix error = new Matrix();
		Matrix absError = new Matrix();
		MatrixStatistics stAbsError = new MatrixStatistics();
		MatrixStatistics stInputError = new MatrixStatistics();
		MatrixStatistics stTarget = new MatrixStatistics();
		MatrixStatistics stOutput = new MatrixStatistics();
		MatrixStatistics stOutputError = new MatrixStatistics();
		Statistics st = new Statistics();
		LayerWorkspace ws = l.createWorkspace();
		ArrayList<LayerWorkspace> wslist = new ArrayList<>();
		wslist.add(ws);
		double lastAvgError = 0;

		int numAdjustableParams = l.getNumAdjustableParams();
		LeastSquaresAdjust lsa = new LeastSquaresAdjust(numAdjustableParams);
		Matrix coefs = new Matrix(numAdjustableParams, 1);
		Matrix params = new Matrix(numAdjustableParams, 1);
		l.extractParams(params, 0);
		log_measurements.trace(params.toMatlabString("P"));
		for (int epoch = 0; epoch < maxEpochs; epoch++) {
			System.out.println("\n--------------------- EPOCH "  + epoch);
			lsa.clear();

			stAbsError.start();
			stInputError.start();
			st.start();
			stTarget.start();
			stOutput.start();
			stOutputError.start();
			int index = 0;
			int patternsLearend = 0;
			for (DatapointPair pair : trainset) {
				pair.toInputMatrix(input);
				pair.toOutputMatrix(target);
				Matrix output = ws.feedForward(input);
				if (target.getVectorSize() != output.getVectorSize())
					throw new Error("Dimensions mismatch");
				error.resize(output.getSizeX(), output.getSizeY());
				absError.resize(output.getSizeX(), output.getSizeY());
				double R = 0;
				for (int i = target.getVectorSize() - 1; i >= 0; i--) {
					double e = output.getVectorItem(i) - target.getVectorItem(i);
					R += e*e;
					error.setVectorItem(i, e);
					e = Math.abs(e);
					absError.setVectorItem(i, e);
				}
				R *= 0.5;
				if (absError.max() < 0.15)
					patternsLearend++;
				st.addValue(R);
				stAbsError.addValue(absError);
				coefs.make0();
				Matrix inputError = ws.backPropagate(coefs, 0, error);
				lsa.addMeasurement(coefs, 1, R, 0);
				inputError.termAbs(inputError);
				stInputError.addValue(inputError);
				stTarget.addValue(target);
				stOutput.addValue(output);
				stOutputError.addValue(absError);
				index++;
			}
			if (!lsa.calculateSvd())
				throw new Error("LSA failed");
			Matrix x = lsa.getUnknown();

			stAbsError.stop();
			stInputError.stop();
			st.stop();
			stTarget.stop();
			stOutput.stop();
			stOutputError.stop();

			Matrix avg = stAbsError.getAvgValue();
			double avgError = avg.avg(); // avg.sumAll(); // / avg.getVectorSize();
			double learnProgress = lastAvgError - avgError;
			double maxErr = stAbsError.getAbsMaxX().max();

			double maxStdInputErr = stInputError.getStdDeviation().max();
			double patternsLearendPercent = (double) patternsLearend / index;
			System.out.println("maxStdInputErr:   " + MathUtil.d4(maxStdInputErr));
			System.out.println("maxAbsInputErr:   " + MathUtil.d4(stInputError.getAbsMaxX().maxAbs()));
			System.out.println("maxErr:           " + MathUtil.d4(maxErr));
			System.out.println("avg R:            " + MathUtil.d4(st.getAvgValue()));
			System.out.println("std R:            " + MathUtil.d4(st.getStdDeviation()));
			System.out.println("avg Avg Error**:  " + MathUtil.d4(avgError));
			System.out.println("avg Max Error:    " + MathUtil.d4(avg.max()));
			System.out.println("std Max Error:    " + MathUtil.d4(stAbsError.getStdDeviation().max()));
			if (epoch > 0) {
				System.out.println("LearnProgress**:  " + MathUtil.d4(learnProgress * 100));
				System.out.println("lastAvgAvgError:  " + MathUtil.d4(lastAvgError));
			}
			System.out.println("patternsLearend%: " + MathUtil.d4(patternsLearendPercent * 100));
			System.out.println("patternsLearend:  " + patternsLearend + " / " + index);
			System.out.println("params.max.abs:   " + MathUtil.d4(params.maxAbs()));
			System.out.println("dX.max.abs:       " + MathUtil.d4(x.maxAbs()));
			System.out.println(params.toMatlabString("params"));
			System.out.println(x.transpose(null).toMatlabString("dX"));
			System.out.println("Target stat: " + stTarget.toString(Statistics.CStatAvg | Statistics.CStatStdDev));
			System.out.println("Output stat: " + stOutput.toString(Statistics.CStatAvg | Statistics.CStatStdDev));
			System.out.println("Output error stat: " + stOutputError.toString(Statistics.CStatAvg | Statistics.CStatStdDev));
			lastAvgError = avgError;

//			if (maxStdInputErr < 0.0001) {
//				System.out.println("Threashold 'Input error std dev' reached at epoch " + epoch + " maxErr=" + MathUtil.d4(maxErr) + " learnProgress=" + MathUtil.d4(learnProgress));
//				break;
//			}
			if (patternsLearendPercent > 0.8 && maxErr < 0.3) {
				System.out.println("Threshold 'patternsLearendPercent' reached at epoch " + epoch + " maxErr=" + MathUtil.d4(maxErr) + " learnProgress=" + MathUtil.d4(learnProgress));
				break;
			}
			if (maxErr < 0.2) { // || (dE > 0 && dE < 0.001)) {
				System.out.println("Threshold 'maxE' reached at epoch " + epoch + " maxErr=" + MathUtil.d4(maxErr) + " learnProgress=" + MathUtil.d4(learnProgress));
				break;
			}
			if (epoch > 0 && learnProgress < 0) {
				System.out.println("AVERAGE ERROR HAS INCREASED.");
			}
			//l.applyWorkspaces(wslist);
/*			int tmpInputSize[] = new int[] { input.getSizeX(), input.getSizeY() };
			BufferedImage bi = Utils.draw(ws, tmpInputSize);
			ImageIO.write(bi, "png", new File(outDir, String.format("tmp%03d.png", epoch)));*/

			for (int i = numAdjustableParams - 1; i >= 0; i--)
				params.itemAdd(i, 0, -x.getItem(0, i));
			l.loadParams(params, 0);
			epochComplete();
			l.resetEpoch(wslist);
		}
	}
}
