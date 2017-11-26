package com.slavi.ann.test;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;

public class BellCurveDistribution {

	private static final double sqrt2pi = Math.sqrt(2 * Math.PI);

	public double getY(double X, double stdDev, double mean) {
		double d = X - mean;
		return Math.exp(- d*d / (2*stdDev*stdDev)) / (stdDev*sqrt2pi);
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

	public static void fillWeight(Matrix w, double stdDev) {
		double scale = 1.0 / (2.0 * stdDev * stdDev);
		double w2 = w.getSizeX() / 2.0;
		double sum = 0.0;
		for (int j = w.getSizeY() - 1; j >= 0; j--) {
			double tr = w2 - j * w.getSizeX() / w.getSizeY();
			for (int i = w.getSizeX() - 1; i >= 0; i--) {
				double d = (i + tr) % w.getSizeX();
				if (d < 0)
					d += w.getSizeX();
				d -= w2;
				double v = Math.exp(-d*d*scale);
				w.setItem(i, j, v);
				sum += v;
			}
		}
		w.rMul(w.getSizeY() / sum);
	}

	public static void fillWeight_MY(Matrix w, double stdDev) {
		double scale = 1.0 / (stdDev * w.getSizeX());
		double w2 = w.getSizeX() / 2.0;
		for (int j = w.getSizeY() - 1; j >= 0; j--) {
			double tr = w2 - j * w.getSizeX() / w.getSizeY();
			for (int i = w.getSizeX() - 1; i >= 0; i--) {
				double d = (i + tr) % w.getSizeX();
				if (d < 0)
					d += w.getSizeX();
				d -= w2;
				w.setItem(i, j, Math.exp(-d*d*scale));
			}
		}
	}

	public static void main(String[] args) {
		double stdDev = 0.3;
/*
		double w[] = new double[33];
		int meanAtIndex = 60;
		fillArray(w, stdDev, meanAtIndex);
		for (int i = 0; i < w.length; i++)
			System.out.println(w[i]);
*/
		Matrix w = new Matrix(800, 1);
		fillWeight_MY(w, stdDev);
//		w.rMul(15/4.0);
		for (int i = 0; i < w.getSizeX(); i++)
			System.out.println(MathUtil.d20(w.getItem(i, 0)));
		
		System.out.println(w.sumAll());
	}
}
