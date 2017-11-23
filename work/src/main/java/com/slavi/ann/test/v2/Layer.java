package com.slavi.ann.test.v2;

import java.util.List;

import com.slavi.math.matrix.Matrix;

public abstract class Layer {

	public abstract Workspace createWorkspace();
	
	/**
	 * After calling this method the caller will invoke the resetEpoch method of each Workspace.
	 */
	public void applyWorkspaces(List<Workspace> workspaces) {
		for (Workspace workspace : workspaces) {
			applyWorkspace(workspace);
		}
	}
	
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
