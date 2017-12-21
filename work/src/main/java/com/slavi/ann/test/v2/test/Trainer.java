package com.slavi.ann.test.v2.test;

import java.util.ArrayList;
import java.util.Collections;

import com.slavi.ann.test.DatapointPair;
import com.slavi.ann.test.v2.Layer;
import com.slavi.ann.test.v2.Layer.Workspace;
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
		MatrixStatistics ms = new MatrixStatistics();
		Workspace ws = l.createWorkspace();
		ArrayList<Workspace> wslist = new ArrayList<>();
		wslist.add(ws);
		Matrix lastAvgE = new Matrix();
		double lastAvgError = 0;

		ArrayList<DatapointTrainResult> errors = new ArrayList<>();
		for (int epoch = 0; epoch < maxEpochs; epoch++) {
			System.out.println("--------------------- EPOCH "  + epoch);
			ms.start();
			int index = 0;
			errors.clear();
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
				ms.addValue(absError);
				errors.add(new DatapointTrainResult(index, absError.sumAll() / absError.getVectorSize()));
				Matrix inputError = ws.backPropagate(error);
				if (print) {
					System.out.println("E(5):" + error.toString());
				}
				index++;
			}
			ms.stop();
			System.out.println(ms.toString(Statistics.CStatStdDev | Statistics.CStatMinMax));
			Matrix avg = ms.getAvgValue();
			double avgError = avg.sumAll() / avg.getVectorSize();
			double dE = lastAvgError - avgError;
			lastAvgError = avgError;
			double maxE = ms.getAbsMaxX().max();
			if (epoch > 0) {
				lastAvgE.mSub(ms.getAvgValue(), lastAvgE);
				System.out.println("dAvgE=" + lastAvgE);
			}
			
			Collections.sort(errors);
			for (int i = 0; i < 5 && i < errors.size(); i++) {
				DatapointTrainResult e = errors.get(i);
				System.out.println("INDEX " + e.index + " ERROR:" + e.error);
			}
			if (maxE < 0.2) { // || (dE > 0 && dE < 0.001)) {
				System.out.println("Threshold reached at epoch " + epoch + " maxE=" + maxE + " dAvgE=" + dE);
				break;
			}
			if (epoch > 0 && dE < 0) {
				System.out.println("AVERAGE ERROR HAS INCREASED.");
			}
			ms.getAvgValue().copyTo(lastAvgE);
			l.applyWorkspaces(wslist);
		}
	}
	
}
