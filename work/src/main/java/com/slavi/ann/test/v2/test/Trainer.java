package com.slavi.ann.test.v2.test;

import java.util.ArrayList;

import com.slavi.ann.test.DatapointPair;
import com.slavi.ann.test.v2.Layer;
import com.slavi.ann.test.v2.Layer.LayerWorkspace;
import com.slavi.math.MathUtil;
import com.slavi.math.adjust.MatrixStatistics;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public class Trainer {

	static class DatapointTrainResult implements Comparable<DatapointTrainResult> {
		int index;
		double error;
		
		public DatapointTrainResult(int index, double error) {
			this.index = index;
			this.error = error;
		}

		public int compareTo(DatapointTrainResult o) {
			return Double.compare(o.error, error); // Descending order
		}
	}
	
	public static void train(Layer l, Iterable<? extends DatapointPair> trainset, int maxEpochs) {
		Matrix input = new Matrix();
		Matrix target = new Matrix();
		Matrix error = new Matrix();
		Matrix absError = new Matrix();
		MatrixStatistics stAbsError = new MatrixStatistics();
		MatrixStatistics stInputError = new MatrixStatistics();
		Statistics st = new Statistics();
		LayerWorkspace ws = l.createWorkspace();
		ArrayList<LayerWorkspace> wslist = new ArrayList<>();
		wslist.add(ws);
		double lastAvgError = 0;

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
				error.resize(output.getSizeX(), output.getSizeY());
				absError.resize(output.getSizeX(), output.getSizeY());
				for (int i = target.getVectorSize() - 1; i >= 0; i--) {
					double e = output.getVectorItem(i) - target.getVectorItem(i);
					error.setVectorItem(i, e);
					e = Math.abs(e);
					absError.setVectorItem(i, e);
				}
				if (absError.max() < 0.15)
					patternsLearend++;
				stAbsError.addValue(absError);
				errors.add(new DatapointTrainResult(index, absError.sumAll() / absError.getVectorSize()));
				st.addValue(error.maxAbs());
				Matrix inputError = ws.backPropagate(error);
				inputError.termAbs(inputError);
				stInputError.addValue(inputError);
				if (print) {
					System.out.println("E(5):" + error.toString());
				}
				index++;
			}
			stAbsError.stop();
			stInputError.stop();
			st.stop();
//			System.out.println("MS - AbsError");
//			System.out.println(stAbsError.toString(Statistics.CStatStdDev | Statistics.CStatMinMax));
//			System.out.println(stInputError.toString(Statistics.CStatStdDev | Statistics.CStatMinMax));
			System.out.println("MaxAbsInputErr=" + MathUtil.d4(stInputError.getAbsMaxX().maxAbs()));
//			System.out.println("Max(Error) stats\n" + st.toString(Statistics.CStatStdDev | Statistics.CStatMinMax| Statistics.CStatAbs));
			Matrix avg = stAbsError.getAvgValue();
			double avgError = avg.sumAll(); // / avg.getVectorSize();
			double learnProgress = lastAvgError - avgError;
			lastAvgError = avgError;
			double maxErr = stAbsError.getAbsMaxX().max();
			
/*			Collections.sort(errors);
			for (int i = 0; i < 5 && i < errors.size(); i++) {
				DatapointTrainResult e = errors.get(i);
				System.out.println("INDEX " + e.index + " ERROR:" + e.error);
			}*/
			double maxStdInputErr = stInputError.getStdDeviation().max();
			double patternsLearendPercent = (double) patternsLearend / index;
			System.out.println("maxStdInputErr:   " + MathUtil.d4(maxStdInputErr));
			System.out.println("maxErr:           " + MathUtil.d4(maxErr));
			System.out.println("avgAvgError:      " + MathUtil.d4(avgError));
			System.out.println("avg Max Error:    " + MathUtil.d4(avg.max()));
			System.out.println("std Max Error:    " + MathUtil.d4(stAbsError.getStdDeviation().max()));
			System.out.println("LearnProgress:    " + MathUtil.d4(learnProgress * 100));
			System.out.println("lastAvgError:     " + MathUtil.d4(lastAvgError * 100));
			System.out.println("patternsLearend%: " + MathUtil.d4(patternsLearendPercent * 100));
			System.out.println("patternsLearend:  " + patternsLearend + " / " + index);
			
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
			l.applyWorkspaces(wslist);
		}
	}
	
}
