package com.slavi.math.matrix;

import com.slavi.math.adjust.Statistics;

public class Vector <T extends Vector<T>> {

	protected double m[];

	public Vector() {};

	public Vector(double m[]) {
		this.m = m;
	};

	public int getVectorSize() {
		return m == null ? 0 : m.length;
	}

	public double getVectorItem(int aIndex) {
		return m[aIndex];
	}

	public void setVectorItem(int aIndex, double aValue) {
		m[aIndex] = aValue;
	}

	/**
	 * Returns the new value.
	 */
	public double vectorItemAdd(int aIndex, double aValue) {
		return m[aIndex] += aValue;
	}

	/**
	 * Returns the new value.
	 */
	public double vectorItemMul(int aIndex, double aValue) {
		return m[aIndex] *= aValue;
	}

	public T loadFromVector(double vector[]) {
		if (vector.length != getVectorSize())
			throw new Error("Invalid argument. VectorSize of this(" + getVectorSize() + ") do not match array size (" + vector.length + ")");
		System.arraycopy(vector, 0, m, 0, vector.length);
		return (T) this;
	}

	public T loadFromVector(int vector[]) {
		if (vector.length != getVectorSize())
			throw new Error("Invalid argument. VectorSize of this(" + getVectorSize() + ") do not match array size (" + vector.length + ")");
		for (int i = 0; i < vector.length; i++)
			m[i] = vector[i];
		return (T) this;
	}

	public double[] getVector() {
		double vector[] = new double[getVectorSize()];
		System.arraycopy(m, 0, vector, 0, vector.length);
		return vector;
	}

	public Statistics calcStatistics() {
		return calcStatistics(null);
	}

	public Statistics calcStatistics(Statistics dest) {
		if (dest == null)
			dest = new Statistics();
		dest.start();
		for (int i = getVectorSize() - 1; i >= 0; i--)
			dest.addValue(getVectorItem(i), 1.0);
		dest.stop();
		return dest;
	}

	/**
	 * Sets all elements of to aValue.
	 * @return this
	 */
	public T makeR(double aValue) {
		for (int i = getVectorSize() - 1; i >= 0; i--)
			setVectorItem(i, aValue);
		return (T) this;
	}

	/**
	 * Returns true if all elements are 0.
	 */
	public boolean is0(double tolerance) {
		tolerance = Math.abs(tolerance);
		for (int i = getVectorSize() - 1; i >= 0; i--)
			if (Math.abs(getVectorItem(i)) > tolerance)
				return false;
		return true;
	}

	public double getSquaredDeviationFrom0() {
		double result = 0.0;
		for (int i = getVectorSize() - 1; i >= 0; i--) {
			double d = getVectorItem(i);
			result += d*d;
		}
		return result;
	}

	/**
	 * Makes a zero matrix. All elements are set to 0.
	 * @return this
	 */
	public T make0() {
		for (int i = getVectorSize() - 1; i >= 0; i--)
			setVectorItem(i, 0.0);
		return (T) this;
	}

	/**
	 * Returns the minimum value of all elements.
	 */
	public double min() {
		int size = getVectorSize();
		if (size == 0)
			return 0;
		double d = getVectorItem(0);
		for (int i = size - 1; i > 0; i--)
			if (d > getVectorItem(i))
				d = getVectorItem(i);
		return d;
	}

	/**
	 * Returns the min(abs()) of all elements.
	 */
	public double minAbs() {
		int size = getVectorSize();
		if (size == 0)
			return 0;
		double d = getVectorItem(0);
		for (int i = size - 1; i > 0; i--) {
			double tmp = Math.abs(getVectorItem(i));
			if (d < tmp)
				d = tmp;
		}
		return d;
	}

	/**
	 * Returns the max(abs()) of all elements.
	 * <p>
	 * <b>LAPACK:</b> DOUBLE PRECISION FUNCTION DLANGE( NORM, M, N, A, LDA, WORK )
	 * <p>
	 * DLANGE returns the value of the one norm, or the Frobenius norm, or
	 * the infinity norm, or the element of largest absolute value of a
	 * real matrix A.
	 * <p>
	 * NORM = 'M' or 'm'
	 */
	public double maxAbs() {
		double d = 0;
		for (int i = getVectorSize() - 1; i >= 0; i--) {
			double tmp = Math.abs(getVectorItem(i));
			if (d < tmp)
				d = tmp;

		}
		return d;
	}

	/**
	 * Returns the maximum value of all elements.
	 */
	public double max() {
		int size = getVectorSize();
		if (size == 0)
			return 0;
		double D = getVectorItem(0);
		for (int i = size - 1; i > 0; i--)
			if (D < getVectorItem(i))
				D = getVectorItem(i);
		return D;
	}

	/**
	 * Returns the average value of all elements.
	 */
	public double avg() {
		int size = getVectorSize();
		if (size == 0)
			return 0;
		double D = 0.0;
		for (int i = size - 1; i >= 0; i--)
			D += getVectorItem(i);
		return D / size;
	}

	/**
	 * Returns sum the of all elements.
	 */
	public double sumAll() {
		double D = 0;
		for (int i = getVectorSize() - 1; i >= 0; i--)
			D += getVectorItem(i);
		return D;
	}

	/**
	 * Returns the sum the of the absolute values.
	 */
	public double sumAbs() {
		double D = 0;
		for (int i = getVectorSize() - 1; i >= 0; i--)
			D += Math.abs(getVectorItem(i));
		return D;
	}

	/**
	 * Multiplies all elements of the matrix with aValue. The formula is:<br>
	 * <tt>this[i, j] = aValue * this[i, j]<br>
	 * </tt>
	 * @return this
	 */
	public T rMul(double aValue) {
		for (int i = getVectorSize() - 1; i >= 0; i--)
			vectorItemMul(i, aValue);
		return (T) this;
	}
}
