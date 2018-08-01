package com.slavi.ann.test.v2;

import java.util.List;

import com.slavi.ann.test.BellCurveDistribution;
import com.slavi.improc.parallel.PGaussianFilter;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.MatrixUtil;

public abstract class Layer {

	public abstract int[] getOutputSize(int inputSize[]);

	public int getNumAdjustableParams() {
		return 0;
	};

	public abstract LayerWorkspace createWorkspace();

	public void applyWorkspaces(List<LayerWorkspace> workspaces) {
		for (LayerWorkspace workspace : workspaces) {
			applyWorkspace(workspace);
		}
	}

	public void resetEpoch(List<LayerWorkspace> workspaces) {
		for (LayerWorkspace workspace : workspaces) {
			workspace.resetEpoch();
		}
	}

	@Override
	public String toString() {
		return "";
	}

	/**
	 * After calling this method the caller will invoke the resetEpoch method of each Workspace.
	 */
	protected void applyWorkspace(LayerWorkspace workspaces) {};

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

	public static void fillKernelMatrix(Matrix k, double sigma) {
		double scaleOutput = 10;
		double[] tmpX = new double[k.getSizeX()];
		double[] tmpY = new double[k.getSizeY()];
		if (true) {
			BellCurveDistribution.fillArray(tmpX, 0.3, (tmpX.length - 1) / 2);
			BellCurveDistribution.fillArray(tmpY, 0.3, (tmpY.length - 1) / 2);
		} else {
			PGaussianFilter.fillArray(tmpX, sigma);
			PGaussianFilter.fillArray(tmpY, sigma);
		}
		for (int i = k.getSizeX() - 1; i >= 0; i--)
			for (int j = k.getSizeY() - 1; j >= 0; j--)
				k.setItem(i, j, (tmpX[i] + tmpY[j]) * 0.5);
		/*
		if (true) {
			double scale = k.sumAll();
			if (scale != 0.0)
				scale = scaleOutput / scale;
			k.rMul(scale);
		}
//		k.normalize();
*/
//		k.rMul(50.0 / k.sumAll());
		System.out.println("K ==========");
		System.out.println(MatrixUtil.calcStatistics(k));
		System.out.println("[K] = " + k.sumAll());
	}

	public abstract class LayerWorkspace {
		public Layer getLayer() {
			return Layer.this;
		}
		/**
		 * input may be stored internally for use later in backProppagate. It should treated readonly.
		 * The result may be always the "same instance". It should treated readonly.
		 */
		public abstract Matrix feedForward(Matrix input);

		/**
		 * error may be the result of the "upper" backPropagate. It should treated readonly.
		 * The result may be always the "same instance". It should treated readonly.
		 */
		public abstract Matrix backPropagate(Matrix coefs, int startingIndex, Matrix error);

		/**
		 * Clears internal counters "after" applyWorkspace.
		 */
		protected void resetEpoch() {};
	}
}
