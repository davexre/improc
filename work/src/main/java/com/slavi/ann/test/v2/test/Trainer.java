package com.slavi.ann.test.v2.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
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
import com.slavi.util.Marker;
import com.slavi.util.MatrixUtil;

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

	public static double assertValue(double d) {
		if (Double.isInfinite(d) || Double.isNaN(d))
			throw new RuntimeException("Invalid value");
		return d;
	}

	public static void train(Layer l, List<? extends DatapointPair> trainset, int maxEpochs) throws IOException {
		Logger LOG = LoggerFactory.getLogger("LOG");
		Matrix input = new Matrix();
		Matrix target = new Matrix();
		Matrix error = new Matrix();
		Matrix absError = new Matrix();
		MatrixStatistics stAbsError = new MatrixStatistics();
		MatrixStatistics stInputError = new MatrixStatistics();
		Statistics st = new Statistics();
		int numAdjustableParams = l.getNumAdjustableParams();
		Matrix coefs = new Matrix(numAdjustableParams, 1);
		LayerWorkspace ws = l.createWorkspace();
		ArrayList<LayerWorkspace> wslist = new ArrayList<>();
		wslist.add(ws);
		double lastAvgError = 0;
/*		File outDir = new File("./target/tmp");
		FileUtils.deleteQuietly(outDir);
		outDir.mkdirs();*/

		LeastSquaresAdjust lsa = new LeastSquaresAdjust(numAdjustableParams);
		RealMatrix jacobian = new Array2DRowRealMatrix(trainset.size(), numAdjustableParams);
		RealVector residuals = new ArrayRealVector(trainset.size());
		RealVector params = new ArrayRealVector(numAdjustableParams);
		ArrayList<DatapointTrainResult> errors = new ArrayList<>();
		for (int epoch = 0; epoch < maxEpochs; epoch++) {
			System.out.println("--------------------- EPOCH "  + epoch);
			stAbsError.start();
			stInputError.start();
			st.start();
			int index = 0;
			errors.clear();
			int patternsLearend = 0;
			for (DatapointPair pair : trainset) {
				boolean print = (index == 5);
				print = false;
				pair.toInputMatrix(input);
				pair.toOutputMatrix(target);
				Matrix output = ws.feedForward(input);
				if (target.getVectorSize() != output.getVectorSize())
					throw new Error("Dimensions mismatch");
				error.resize(output.getSizeX(), output.getSizeY());
				absError.resize(output.getSizeX(), output.getSizeY());
				double L = 0;
				double R = 0;
				for (int i = target.getVectorSize() - 1; i >= 0; i--) {
					double e = output.getVectorItem(i) - target.getVectorItem(i);
					L += e;
					R += e*e;
					error.setVectorItem(i, e);
					e = Math.abs(e);
					absError.setVectorItem(i, e);
				}

				if (absError.max() < 0.15)
					patternsLearend++;
				st.addValue(absError.sumAll());
				stAbsError.addValue(absError);
				//errors.add(new DatapointTrainResult(index, pair, absError.sumAll() / absError.getVectorSize()));
				errors.add(new DatapointTrainResult(index, pair, absError.max()));
				coefs.make0();
				Matrix inputError = ws.backPropagate(coefs, 0, error);
				//lsa.addMeasurement(coefs, 1, L, 0);
				for (int i = 0; i < numAdjustableParams; i++)
					jacobian.setEntry(index, i, coefs.getItem(i, 0));
				residuals.setEntry(index, R/2);
				inputError.termAbs(inputError);
				stInputError.addValue(inputError);
				if (print) {
					System.out.println("E(5):" + error.toString());
				}
				index++;
			}
			//System.out.println("\n\nJ=" + MatrixUtils.OCTAVE_FORMAT.format(jacobian));
			//System.out.println("\n\n" + MatrixUtil.fromApacheMatrix(jacobian, null).toMatlabString("J"));
			l.extractParams(params, 0);
			LOG.debug(MatrixUtil.fromApacheMatrix(jacobian, null).toMatlabString("J"));
			LOG.debug(MatrixUtil.fromApacheVector(params, null).toMatlabString("P"));
			LOG.debug(MatrixUtil.fromApacheVector(residuals, null).toMatlabString("R"));
			SingularValueDecomposition svd = new SingularValueDecomposition(jacobian);
			RealVector x = svd.getSolver().solve(residuals);
			LOG.debug(MatrixUtil.fromApacheVector(x, null).toMatlabString("X"));
			/*if (!lsa.calculate())
				throw new Error("LSA failed");*/

			stAbsError.stop();
			stInputError.stop();
			st.stop();
//			System.out.println("MS - AbsError");
//			System.out.println(stAbsError.toString(Statistics.CStatStdDev | Statistics.CStatMinMax));
//			System.out.println(stInputError.toString(Statistics.CStatStdDev | Statistics.CStatMinMax));
			System.out.println("MaxAbsInputErr=" + MathUtil.d4(stInputError.getAbsMaxX().maxAbs()));
//			System.out.println("Max(Error) stats\n" + st.toString(Statistics.CStatStdDev | Statistics.CStatMinMax| Statistics.CStatAbs));
			Matrix avg = stAbsError.getAvgValue();
			double avgError = avg.avg(); // avg.sumAll(); // / avg.getVectorSize();
			double learnProgress = lastAvgError - avgError;
			double maxErr = stAbsError.getAbsMaxX().max();

			Collections.sort(errors);

			for (int i = 0; i < 5 && i < errors.size(); i++) {
				DatapointTrainResult e = errors.get(i);
				System.out.println("INDEX " + e.index + " ERROR:" + e.error + ", " + e.pair.getName());
			}
			double maxStdInputErr = stInputError.getStdDeviation().max();
			double patternsLearendPercent = (double) patternsLearend / index;
			System.out.println("maxStdInputErr:   " + MathUtil.d4(maxStdInputErr));
			System.out.println("maxErr:           " + MathUtil.d4(maxErr));
			System.out.println("maxErrPair:       " + errors.get(0).pair.getName());
			System.out.println("avgSumError:      " + MathUtil.d4(st.getAvgValue()));
			System.out.println("stdSumError:      " + MathUtil.d4(st.getStdDeviation()));
			System.out.println("sumAvgError:      " + MathUtil.d4(avgError));
			System.out.println("avg Max Error:    " + MathUtil.d4(avg.max()));
			System.out.println("std Max Error:    " + MathUtil.d4(stAbsError.getStdDeviation().max()));
			System.out.println("LearnProgress:    " + MathUtil.d4(learnProgress * 100));
			System.out.println("lastSumAvgError:  " + MathUtil.d4(lastAvgError));
			System.out.println("patternsLearend%: " + MathUtil.d4(patternsLearendPercent * 100));
			System.out.println("patternsLearend:  " + patternsLearend + " / " + index);
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
			l.applyDeltaToParams(x, 0);
			l.resetEpoch(wslist);
		}
	}
}
