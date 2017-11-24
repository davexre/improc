package com.slavi.ann.test.v2;

import java.util.List;

import com.slavi.ann.test.BellCurveDistribution;
import com.slavi.math.matrix.Matrix;

public abstract class Layer {

	public abstract Workspace createWorkspace();
	
	public void applyWorkspaces(List<Workspace> workspaces) {
		for (Workspace workspace : workspaces) {
			applyWorkspace(workspace);
			workspace.resetEpoch();
		}
	}

	/**
	 * After calling this method the caller will invoke the resetEpoch method of each Workspace.
	 */
	protected void applyWorkspace(Workspace workspaces) {};

	public static double sqrt2pi = Math.sqrt(2.0 * Math.PI);
	
	public static void fillWeight(Matrix w, double stdDev) {
		double scale = 1.0 / (2 * stdDev * stdDev); // * w.getSizeX());
		double scale2 = 1.0 / (stdDev * sqrt2pi);
		double w2 = w.getSizeX() / 2.0;
		for (int j = w.getSizeY() - 1; j >= 0; j--) {
			double tr = w2 - j * w.getSizeX() / w.getSizeY();
			for (int i = w.getSizeX() - 1; i >= 0; i--) {
				double d = (i + tr) % w.getSizeX();
				if (d < 0)
					d += w.getSizeX();
				d -= w2;
				w.setItem(i, j, Math.exp(-d*d*scale) * scale2);
			}
		}
	}

	public static void fillArray(double w[], double stdDev, double meanAtIndex) {
		double scale = 1.0 / (stdDev * w.length);
		double w2 = w.length / 2.0;
		double tr = w2 - meanAtIndex;
		for (int i = 0; i < w.length; i++) {
			double d = (i + tr) % w.length;
			if (d < 0)
				d += w.length;
			d -= w2;
			w[i] = Math.exp(-d*d*scale);
		}
	}

	public static void fillKernelMatrix(Matrix k, double stdDev) {
		double[] tmpX = new double[k.getSizeX()];
		double[] tmpY = new double[k.getSizeY()];
		BellCurveDistribution.fillArray(tmpX, 0.3, (tmpX.length - 1) / 2);
		BellCurveDistribution.fillArray(tmpY, 0.3, (tmpY.length - 1) / 2);
		for (int i = k.getSizeX() - 1; i >= 0; i--)
			for (int j = k.getSizeY() - 1; j >= 0; j--)
				k.setItem(i, j, (tmpX[i] + tmpY[j]) * 0.5);
	}
	
	public abstract class Workspace {
		/**
		 * input may be stored internally for use later in backProppagate. It should treated readonly.
		 * The result may be always the "same instance". It should treated readonly.
		 */
		public abstract Matrix feedForward(Matrix input);
		
		/**
		 * error may be the result of the "upper" backPropagate. It should treated readonly.
		 * The result may be always the "same instance". It should treated readonly.
		 */
		public abstract Matrix backPropagate(Matrix error);

		/**
		 * Clears internal counters "after" applyWorkspace.
		 */
		protected abstract void resetEpoch();
	}
}
