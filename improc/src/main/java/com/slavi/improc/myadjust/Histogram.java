package com.slavi.improc.myadjust;

import java.util.Arrays;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;

/**
 * https://www.math.uci.edu/icamp/courses/math77c/demos/hist_eq.pdf
 */
public class Histogram {

	double minValue;
	double maxValue;
	double delta;
	double scale;
	int histogram[];
	int cdf[];
	boolean safeMode;

	public Histogram(int bins, double minValue, double maxValue, boolean safeMode) {
		this.safeMode = safeMode;
		this.minValue = Math.min(maxValue, minValue);
		this.maxValue = Math.max(maxValue, minValue);
		delta = this.maxValue - this.minValue;
		scale = bins / delta;
		histogram = new int[bins];
		cdf = new int[bins];
	}

	private int calcIndex(double value) {
		if (safeMode)
			value = MathUtil.clipValue(value, minValue, maxValue);
		else if (!MathUtil.isInRange(value, minValue, maxValue))
			throw new Error("Invalid argument");
		return MathUtil.fixIndex((int) ((value - minValue) * scale), histogram.length);
	}

	public void addValue(double value) {
		int index = calcIndex(value);
		histogram[index]++;
		for (int i = cdf.length - 1; i >= index; i--)
			cdf[i]++;
	}

	public void reset() {
		Arrays.fill(histogram, 0);
		Arrays.fill(cdf, 0);
	}

	public int[] getHistogram() {
		return histogram;
	}

	public int[] getCDF() {
		return cdf;
	}

	public double calcHistogramEqualization(double value) {
		int index = calcIndex(value);
		int lastCdf = cdf[cdf.length - 1];
		if (lastCdf == 0) {
			if (safeMode)
				return value;
			else
				throw new Error("Invalid histogram");
		}
		double d = (double) cdf[index] / (double) lastCdf;
		d = minValue + d * delta;
		return MathUtil.clipValue(d, minValue, maxValue);
	}

	/**
	 * CDF - Cumulative Distribution Function
	 */
	public Matrix calcNoramlizedCDF() {
		Matrix r = new Matrix(cdf.length, 1);
		int lastCdf = cdf[cdf.length - 1];
		if (lastCdf == 0) {
			if (safeMode) {
				return r;
			} else
				throw new Error("Invalid histogram");
		}
		r.loadFromVector(cdf);
		r.rMul(1.0 / lastCdf);
		return r;
	}

	public void calcFromArray(double values[]) {
		reset();
		for (double value : values)
			addValue(value);
	}

	public void calcFromArray(int values[]) {
		reset();
		for (int value : values)
			addValue(value);
	}

	public void calcFromMatrix(Matrix m) {
		reset();
		for (int i = m.getVectorSize() - 1; i >= 0; i--)
			addValue(m.getVectorItem(i));
	}
}
