package com.slavi.math.matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import com.slavi.math.MathUtil;

public class Matrix {

	/**
	 * The elements of the matrix.
	 */
	private double m[];

	/**
	 * Number of columns
	 */
	private int sizeX;

	/**
	 * Number of rows
	 */
	private int sizeY;

	/**
	 * Creates a matrix with sizeX = sizeY = 0.
	 */
	public Matrix() {
		resize(0, 0);
	}

	/**
	 * Creates a matrix with aSizeX columns and aSizeY rows and sets all
	 * elements to 0.
	 */
	public Matrix(int aSizeX, int aSizeY) {
		resize(aSizeX, aSizeY);
	}

	/**
	 * Compares two matrices element by element.
	 *
	 * @return Returns true if all the elements of the matrices are equal.
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || !(obj instanceof Matrix))
			return false;
		Matrix a = (Matrix) obj;
		if ((a.sizeX != sizeX) || (a.sizeY != sizeY))
			return false;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				if (getItem(i, j) != a.getItem(i, j))
					return false;
		return true;
	}

	public boolean equals(Matrix a, double tolerance) {
		if (a == null)
			return false;
		if (a == this)
			return true;
		if ((sizeX != a.sizeX) || (sizeY != a.sizeY))
			return false;
		tolerance = Math.abs(tolerance);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				if (Math.abs(getItem(i, j) - a.getItem(i, j)) > tolerance)
					return false;
		return true;
	}

	public String toMatlabString(String variableName) {
		StringBuilder result = new StringBuilder();
		result.append(variableName);
		result.append("=[");
		for (int j = 0; j < sizeY; j++) {
			for (int i = 0; i < sizeX; i++) {
				if (i != 0)
					result.append(" ");
				//result.append(String.format(Locale.US, "%1$27.19f",new Object[] { new Double(.getItem(i, j]) } ));
				result.append(getItem(i, j));
			}
			result.append(";\n");
		}
		result.append("];");
		return result.toString();
	}

	public String toOneLineString() {
		StringBuilder result = new StringBuilder();
		for (int j = 0; j < sizeY; j++) {
			for (int i = 0; i < sizeX; i++) {
				if (i != 0)
					result.append(" ");
				result.append(String.format(Locale.US, "%1$10.4f",
						new Object[] { new Double(getItem(i, j)) } ));
			}
			result.append("");
		}
		return result.toString();
	}

	public static Matrix fromOneLineString(String str) {
		StringTokenizer st = new StringTokenizer(str, ";");
		int sizeY = st.countTokens();
		int sizeX = 0;
		for (int j = 0; j < sizeY; j++) {
			StringTokenizer st2 = new StringTokenizer(st.nextToken());
			sizeX = Math.max(sizeX, st2.countTokens());
		}
		st = new StringTokenizer(str, ";");
		Matrix result = new Matrix(sizeX, sizeY);
		for (int j = 0; j < sizeY; j++) {
			StringTokenizer st2 = new StringTokenizer(st.nextToken());
			int i = 0;
			while (st2.hasMoreTokens()) {
				result.setItem(i++, j, Double.parseDouble(st2.nextToken()));
			}
		}
		return result;
	}

	/**
	 * Returns a multiline string containing all elements of the matrix.
	 */
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int j = 0; j < sizeY; j++) {
			for (int i = 0; i < sizeX; i++) {
				if (i != 0)
					result.append(" ");
				result.append(MathUtil.d4(getItem(i, j)));
			}
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 * Loads the matrix from a text stream
	 */
	public void load(BufferedReader fin) throws IOException {
		for (int j = 0; j < sizeY; j++) {
			StringTokenizer st = new StringTokenizer(fin.readLine());
			for (int i = 0; i < sizeX; i++)
				setItem(i, j, st.hasMoreTokens() ? Double.parseDouble(st.nextToken()) : 0.0);
		}
	}

	/**
	 * Saves the matrix to a text stream
	 */
	public void save(PrintStream fou) {
		for (int j = 0; j < sizeY; j++) {
			for (int i = 0; i < sizeX; i++) {
				if (i != 0)
					fou.print("\t");
				fou.print(getItem(i, j));
			}
			fou.print("\n");
		}
	}

	public double[][] toArray() {
		double [][] r = new double[getSizeX()][getSizeY()];
		for (int i = getSizeX() - 1; i >= 0; i--) {
			for (int j = getSizeY() - 1; j >= 0; j--) {
				r[i][j] = getItem(i, j);
			}
		}
		return r;
	}

	/**
	 * Resizes the matrix if the new size differs from the current matrix size.
	 */
	public void resize(int newSizeX, int newSizeY) {
		if ((newSizeX < 0) || (newSizeY < 0)) {
			throw new Error("Invalid matrix size");
		}
		if ((newSizeX == sizeX) && (newSizeY == sizeY) && (m != null)) {
			return;
		}
		sizeX = newSizeX;
		sizeY = newSizeY;
		int newSize = newSizeX * newSizeY;
		if (m == null || m.length != newSize)
			m = new double[newSize];
	}

	/**
	 * Returns the number of columns in the matrix.
	 */
	public int getSizeX() {
		return sizeX;
	}

	/**
	 * Returns the number of rows in the matrix.
	 */
	public int getSizeY() {
		return sizeY;
	}

	/**
	 * Returns the value of the matrix atX column and atY row. The top-left
	 * element is atX=0, atY=0.
	 */
	public double getItem(int atX, int atY) {
		if (atX < 0 || atX >= sizeX ||
			atY < 0 || atY >= sizeY)
			throw new IndexOutOfBoundsException();
		return m[atX + atY * sizeX];
	}

	/**
	 * Sets the value of the matrix atX column and atY row. The top-left element
	 * is atX=0, atY=0.
	 */
	public void setItem(int atX, int atY, double aValue) {
		if (atX < 0 || atX >= sizeX ||
				atY < 0 || atY >= sizeY)
				throw new IndexOutOfBoundsException();
		m[atX + atY * sizeX] = aValue;
	}

	/**
	 * Returns the size of the matrix as a vector.
	 * @see Matrix#getVectorItem(int)
	 */
	public int getVectorSize() {
		return m == null ? 0 : m.length; // sizeX * sizeY;
	}

	/**
	 * The matrix can be interpreted as a "vector" where the elements are
	 * aligned by rows, i.e.<br>
	 * <br>
	 * <tt>
	 *       Matrix [4, 3]<br>
	 *        a b c d<br>
	 *        e f j h<br>
	 *        i j k l<br>
	 *       <br>
	 *       The same matrix as vector [12] sizeX*sizeY = 4*3 = 12<br>
	 *  &nbsp;a b c d e f j h i j k l     <br>
	 *       | Y = 1 | Y = 2 | Y = 3 |    <br>
	 * </tt>
	 *
	 * @return The value at the specified position.
	 */
	public double getVectorItem(int aIndex) {
		return m[aIndex]; // getItem(aIndex % sizeX, aIndex / sizeX);
	}

	/**
	 * @see Matrix#getVectorItem(int)
	 */
	public void setVectorItem(int aIndex, double aValue) {
		m[aIndex] = aValue; // setItem(aIndex % sizeX, aIndex / sizeX, aValue);
	}

	/**
	 * @see Matrix#getVectorItem(int)
	 */
	public void loadFromVector(double vector[]) {
		if (vector.length != getVectorSize())
			throw new Error("Invalid argument");
		for (int i = vector.length - 1; i >= 0 ; i--)
			setVectorItem(i, vector[i]);
	}

	/**
	 * @see Matrix#getVectorItem(int)
	 */
	public void loadFromVector(int vector[]) {
		if (vector.length != getVectorSize())
			throw new Error("Invalid argument");
		for (int i = vector.length - 1; i >= 0 ; i--)
			setVectorItem(i, vector[i]);
	}

	/**
	 * @see Matrix#getVectorItem(int)
	 */
	public double[] getVector() {
		double vector[] = new double[getVectorSize()];
		for (int i = vector.length - 1; i >= 0 ; i--)
			vector[i] = getVectorItem(i);
		return vector;
	}

	/**
	 * Multiplies two matrices and stores the result in dest matrix. If the dest
	 * matrix is of incorrect size it will be resized. The formula is:<br>
	 * <tt>dest = this * second<br>
	 * </tt>
	 * Requires: this != second != dest
	 */
	public void mMul(Matrix second, Matrix dest) {
		if (sizeX != second.sizeY || this == second || this == dest || second == dest) {
			throw new Error("Invalid argument");
		}
		dest.resize(second.sizeX, sizeY);
		double D;
		for (int i = sizeY - 1; i >= 0; i--) {
			for (int j = second.sizeX - 1; j >= 0; j--) {
				D = 0;
				for (int k = sizeX - 1; k >= 0; k--)
					D += getItem(k, i) * second.getItem(j, k);
				dest.setItem(j, i, D);
			}
		}
	}

	/**
	 * Performs an element by element sum of two matrices of equal size and
	 * stores the result in dest matrix. If the dest matrix is of incorrect size
	 * it will be resized to the same size as the source matrix. The formula is:<br>
	 * <tt>dest[i, j] = this[i, j] + second[i, j]<br>
	 * </tt>
	 * Allows: this == second == dest
	 */
	public void mSum(Matrix second, Matrix dest) {
		if ((sizeX != second.sizeX) || (sizeY != second.sizeY)) {
			throw new Error("Invalid argument");
		}
		dest.resize(sizeX, sizeY);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				dest.setItem(i, j, getItem(i, j) + second.getItem(i, j));
	}

	/**
	 * Performs an element by element subtraction of two matrices of equal size
	 * and stores the result in dest matrix. If the dest matrix is of incorrect
	 * size it will be resized to the same size as the source matrix. The
	 * formula is:<br>
	 * <tt>dest[i, j] = this[i, j] - second[i, j]<br>
	 * </tt>
	 * Allows: this == second == dest
	 */
	public void mSub(Matrix second, Matrix dest) {
		if ((sizeX != second.sizeX) || (sizeY != second.sizeY)) {
			throw new Error("Invalid argument");
		}
		dest.resize(sizeX, sizeY);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				dest.setItem(i, j, getItem(i, j) - second.getItem(i, j));
	}

	/**
	 * Returns the dot product of the matrix. The formula is:<br>
	 * <tt>Result = Sum( .getItem(i, j] )<br>
	 * </tt>
	 */
	public double dotProduct(Matrix second) {
		if ((sizeX != second.sizeX) || (sizeY != second.sizeY)) {
			throw new Error("Invalid argument");
		}
		double sum = 0;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				sum += getItem(i, j);
		return sum;
	}

	/**
	 * Performs an abs to the elements of the matrix and stores the result in dest matrix.
	 * Allows: this == dest
	 */
	public void termAbs(Matrix dest) {
		dest.resize(sizeX, sizeY);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				dest.setItem(i, j, Math.abs(getItem(i, j)));
	}

	/**
	 * Performs an element by element multiplication of two matrices of equal
	 * size and stores the result in dest matrix. If the dest matrix is of
	 * incorrect size it will be resized to the same size as the source matrix.
	 * The formula is:<br>
	 * <tt>dest[i, j] = this[i, j] * second[i, j]<br>
	 * </tt>
	 * Allows: this == dest
	 */
	public void termMul(Matrix second, Matrix dest) {
		if ((sizeX != second.sizeX) || (sizeY != second.sizeY)) {
			throw new Error("Invalid argument");
		}
		dest.resize(sizeX, sizeY);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				dest.setItem(i, j, getItem(i, j) * second.getItem(i, j));
	}

	/**
	 * Performs an element by element division of two matrices of equal size and
	 * stores the result in dest matrix. If the dest matrix is of incorrect size
	 * it will be resized to the same size as the source matrix. The formula is:<br>
	 * <tt>dest[i, j] = this[i, j] / second[i, j]<br>
	 * </tt> <b>Warning:</b><i>If there is an element that is zero, an
	 * exception will rise <code>java.lang.ArithmeticException</code>.</i>
	 * Allows: this == second == dest
	 */
	public void termDiv(Matrix second, Matrix dest) {
		if ((sizeX != second.sizeX) || (sizeY != second.sizeY)) {
			throw new Error("Invalid argument");
		}
		dest.resize(sizeX, sizeY);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				dest.setItem(i, j, getItem(i, j) / second.getItem(i, j));
	}

	/**
	 * Multiplies all elements of the matrix with aValue. The formula is:<br>
	 * <tt>this[i, j] = aValue * this[i, j]<br>
	 * </tt>
	 */
	public void rMul(double aValue) {
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				setItem(i, j, getItem(i, j) * aValue);
	}

	/**
	 * Returns sum the of all elements of the matrix.
	 */
	public double sumAll() {
		double D = 0;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				D += getItem(i, j);
		return D;
	}

	/**
	 * Returns the sum the of the absolute values of the matrix.
	 */
	public double sumAbs() {
		double D = 0;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				D += Math.abs(getItem(i, j));
		return D;
	}

	/**
	 * Returns the maximum value of all elements of the matrix.
	 */
	public double max() {
		if ((sizeX == 0) || (sizeY == 0))
			return 0;
		double D = getItem(0, 0);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				if (D < getItem(i, j))
					D = getItem(i, j);
		return D;
	}

	/**
	 * Returns the max(abs()) of all elements of the matrix.
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
		double D = 0;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				double tmp = Math.abs(getItem(i, j));
				if (D < tmp)
					D = tmp;
			}
		return D;
	}

	/**
	 * Returns the minimum value of all elements of the matrix.
	 */
	public double min() {
		if ((sizeX == 0) || (sizeY == 0))
			return 0;
		double D = getItem(0, 0);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				if (D > getItem(i, j))
					D = getItem(i, j);
		return D;
	}

	/**
	 * Return the matrix one norm value that is equal to the maximum of the
	 * column sums of the absolute values of the array elements.
	 * <p>
	 * <b>LAPACK:</b> DOUBLE PRECISION FUNCTION DLANGE( NORM, M, N, A, LDA, WORK )
	 * <p>
	 * DLANGE returns the value of the one norm, or the Frobenius norm, or
	 * the infinity norm, or the element of largest absolute value of a
	 * real matrix A.
	 * <p>
	 * NORM = '1', 'O' or 'o'
	 */
	public double getOneNorm() {
		double result = 0.0;
		for (int i = sizeX - 1; i >= 0; i--) {
			double d = 0.0;
			for (int j = sizeY - 1; j >= 0; j--)
				d += Math.abs(getItem(i, j));
			if (d > result)
				result = d;
		}
		return result;
	}

	/**
	 * Return the infinity norm of the matrix that is equal to the
	 * maximum of the row sums of the absolute values of the array elements.
	 * <p>
	 * <b>LAPACK:</b> DOUBLE PRECISION FUNCTION DLANGE( NORM, M, N, A, LDA, WORK )
	 * <p>
	 * DLANGE returns the value of the one norm, or the Frobenius norm, or
	 * the infinity norm, or the element of largest absolute value of a
	 * real matrix A.
	 * <p>
	 * NORM = 'I' or 'i'
	 */
	public double getNormInfinity() {
		double result = 0.0;
		for (int j = sizeY - 1; j >= 0; j--) {
			double d = 0.0;
			for (int i = sizeX - 1; i >= 0; i--)
				d += Math.abs(getItem(i, j));
			if (d > result)
				result = d;
		}
		return result;
	}

	/**
	 * Return the Frobenius norm of the matrix.
	 * <p>
	 * <b>LAPACK:</b> DOUBLE PRECISION FUNCTION DLANGE( NORM, M, N, A, LDA, WORK )
	 * <p>
	 * DLANGE returns the value of the one norm, or the Frobenius norm, or
	 * the infinity norm, or the element of largest absolute value of a
	 * real matrix A.
	 * <p>
	 * NORM = 'F', 'f', 'E' or 'e'
	 */
	public double getForbeniusNorm() {
		double scale = 0.0;
		double sum = 1.0;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				double d = Math.abs(getItem(i, j));
				if (d != 0.0) {
					if (scale < d) {
						double d1 = scale / d;
						sum = 1.0 + sum * d1 * d1;
						scale = d;
					} else {
						double d1 = d / scale;
						sum += d1 * d1;
					}
				}
			}
		return scale * Math.sqrt(sum);
	}

	/**
	 * Sets the elements of dest matrix to the maximum corresponding elements of
	 * this and second matrix. If the dest matrix is of incorrect size it will
	 * be resized. The formula is:<br>
	 * <tt>dest[i, j] = max( this[i, j] , second[i, j] )<br>
	 * </tt>
	 * Allows: this == second == dest
	 */
	public void mMax(Matrix second, Matrix dest) {
		if ((sizeX != second.sizeX) || (sizeY != second.sizeY)) {
			throw new Error("Invalid argument");
		}
		dest.resize(sizeX, sizeY);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				double a = getItem(i, j);
				double b = second.getItem(i, j);
				dest.setItem(i, j, a > b ? a : b);
			}
	}

	/**
	 * Sets the elements of dest matrix to the minimum corresponding elements of
	 * this and second matrix. If the dest matrix is of incorrect size it will
	 * be resized. The formula is:<br>
	 * <tt>dest[i, j] = min( this[i, j] , second[i, j] )<br>
	 * </tt>
	 * Allows: this == second == dest
	 */
	public void mMin(Matrix second, Matrix dest) {
		if ((sizeX != second.sizeX) || (sizeY != second.sizeY)) {
			throw new Error("Invalid argument");
		}
		dest.resize(sizeX, sizeY);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				double a = getItem(i, j);
				double b = second.getItem(i, j);
				dest.setItem(i, j, a < b ? a : b);
			}
	}

	/**
	 * Makes a transposed matirx of this matrix. If the dest matrix is of
	 * incorrect size it will be resized.
	 * Requires: this != dest
	 */
	public void transpose(Matrix dest) {
		if (this == dest) {
			throw new Error("Invalid argument");
		}
		dest.resize(sizeY, sizeX);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				dest.setItem(j, i, getItem(i, j));
	}

	/**
	 * Normalizes the matrix so that sumAll() returns 1. If normalization is not
	 * possible, i.e. sumAll() returns 0, the Matrix.make0() is called. The
	 * formula is:<br>
	 * <tt>this[i, j] = this[i, j] / sum ( this[i, j] )<br>
	 * </tt>
	 *
	 * @return Returns true on success.
	 */
	public boolean normalize() {
		double D = sumAll();
		if (D == 0) {
			make0();
			return false;
		}
		rMul(1 / D);
		return true;
	}

	/**
	 * Normalizes the matrix so that all elements are in the range [0..1]. If
	 * normalization is not possible, i.e. max()-min()=0, the Matrix.make0() is
	 * called. The formula is:<br>
	 * <tt>this[i, j] = (this[i, j] - min()) / (max() - min())<br>
	 * </tt>
	 */
	public void normalize2() {
		if ((sizeX == 0) || (sizeY == 0))
			return;
		double maxVal = getItem(0, 0);
		double minVal = maxVal;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				double value = getItem(i, j);
				if (maxVal < value)
					maxVal = value;
				if (minVal > value)
					minVal = value;
			}
		double delta = maxVal - minVal;
		if (delta == 0) {
			make0();
		} else {
			for (int i = sizeX - 1; i >= 0; i--)
				for (int j = sizeY - 1; j >= 0; j--)
					setItem(i, j, (getItem(i, j) - minVal) / delta);
		}
	}

	/**
	 * Returns the sum of the squares of all elements of the matrix.
	 */
	public double sumPow2() {
		double D = 0;
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--) {
				double v = getItem(i, j);
				D += v*v;
			}
		return D;
	}

	/**
	 * Normalizes the matrix so that the sum of the squares of all element is 1.
	 */
	public boolean normalizePow2() {
		double D = sumPow2();
		if (D == 0) {
			make0();
			return false;
		}
		D = 1 / Math.sqrt(D);
		rMul(D);
		return true;
	}

	/**
	 * Makes a copy of the matrix.
	 *
	 * @return Returns the new matrix.
	 */
	public Matrix makeCopy() {
		Matrix result = new Matrix(sizeX, sizeY);
		copyTo(result);
		return result;
	}

	/**
	 * Copies this matrix to a destination. The destination is resized if
	 * necessary.
	 * Allows: this == dest
	 */
	public void copyTo(Matrix dest) {
		dest.resize(sizeX, sizeY);
		System.arraycopy(this.m, 0, dest.m, 0, this.m.length);
/*		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				dest.setItem(i, j, getItem(i, j));*/
	}

	/**
	 * Makes the identity matrix. The formula is:<br>
	 * <tt>Result[i, j] = (i == j) ? 1 : 0<br>
	 * </tt>
	 */
	public void makeE() {
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				setItem(i, j, (i == j) ? 1.0 : 0.0);
	}

	public double getSquaredDeviationFromE() {
		double result = 0.0;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				double d = i == j ? getItem(i, j) - 1.0 : getItem(i, j);
				result += d*d;
			}
		return result;
	}

	/**
	 * Returns true if this matrix is the identity matrix
	 */
	public boolean isE(double tolerance) {
		tolerance = Math.abs(tolerance);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				double d = i == j ? getItem(i, j) - 1.0 : getItem(i, j);
				if (Math.abs(d) > tolerance)
					return false;
			}
		return true;
	}

	public double getSquaredDeviationFrom0() {
		double result = 0.0;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				double d = getItem(i, j);
				result += d*d;
			}
		return result;
	}

	/**
	 * Makes a zero matrix. All elements are set to 0.
	 */
	public void make0() {
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				setItem(i, j, 0.0);
	}

	/**
	 * Returns true if all elements are 0.
	 */
	public boolean is0(double tolerance) {
		tolerance = Math.abs(tolerance);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				if (Math.abs(getItem(i, j)) > tolerance)
					return false;
		return true;
	}

	/**
	 * Sets all elements of this matrix to aValue.
	 */
	public void makeR(double aValue) {
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				setItem(i, j, aValue);
	}

	/**
	 * Multiplies all elements at column atX with D.
	 */
	public void mulX(int atX, double D) {
		for (int j = sizeY - 1; j >= 0; j--)
			setItem(atX, j, getItem(atX, j) * D);
	}

	/**
	 * Multiplies all elements at row atY with D.
	 */
	public void mulY(int atY, double D) {
		for (int i = sizeX - 1; i >= 0; i--)
			setItem(i, atY, getItem(i, atY) * D);
	}

	/**
	 * Sums the elements at column atX1 with the elements at column atX2 and
	 * stores the result in column atX1. The formula is:<br>
	 * <tt>this[atX1, j] = this[atX1, j] + this[atX2, j]<br>
	 * </tt>
	 */
	public void sumX(int atX1, int atX2) {
		for (int j = sizeY - 1; j >= 0; j--)
			setItem(atX1, j, getItem(atX1, j) + getItem(atX2, j));
	}

	/**
	 * Sums the elements at row atY1 with the elements at row atY2 and stores
	 * the result in row atY1. The formula is:<br>
	 * <tt>this[i, atX1] = this[i, atX1] + this[i, atX2]<br>
	 * </tt>
	 */
	public void sumY(int atY1, int atY2) {
		for (int i = sizeX - 1; i >= 0; i--)
			setItem(i, atY1, getItem(i, atY1) + getItem(i, atY2));
	}

	/**
	 * Exchanges the column atX1 with column atX2.
	 */
	public void exchangeX(int atX1, int atX2) {
		for (int j = sizeY - 1; j >= 0; j--) {
			double D = getItem(atX1, j);
			setItem(atX1, j, getItem(atX2, j));
			setItem(atX2, j, D);
		}
	}

	/**
	 * Exchanges the row atY1 with row atY2.
	 */
	public void exchangeY(int atY1, int atY2) {
		for (int i = sizeX - 1; i >= 0; i--) {
			double D = getItem(i, atY1);
			setItem(i, atY1, getItem(i, atY2));
			setItem(i, atY2, D);
		}
	}

	/**
	 * Private class used by Matrix.inverse()
	 */
	private static class XchgRec {

		public int a;

		public int b;

		public XchgRec(int A, int B) {
			a = A;
			b = B;
		}
	}

	/**
	 * Calculates the inverse matrix of this matrix. The algorithm calculates
	 * the inverse matrix "in place" and does NOT create any intermediate
	 * matrices.
	 *
	 * @return Returns true if the inverse matrix is computable. If the inverse
	 *         matrix can not be computed this.make0 is called and the returned
	 *         value is false.
	 */
	public boolean inverse() {
		if (sizeX != sizeY) {
			throw new Error("Invalid argument");
		}
		ArrayList<XchgRec> xchg = new ArrayList<XchgRec>();

		for (int i = 0; i < sizeX; i++) {
			double A = getItem(i, i);
			if (A == 0) {
				int indexI = 0;
				for (int j = i + 1; j < sizeX; j++)
					if (getItem(i, j) != 0) {
						indexI = j;
						exchangeX(i, j);
						xchg.add(new XchgRec(i, j));
						break;
					}
				if (indexI == 0) {
					make0();
					return false;
				}
				A = getItem(i, i);
			}

			for (int j = 0; j < sizeX; j++)
				if (i != j) {
					double B = getItem(j, i) / A;
					for (int k = 0; k < sizeX; k++)
						if (k != i) {
							if ((k < i) && (j < i))
								setItem(j, k, getItem(j, k) + B * getItem(i, k));
							else
								setItem(j, k, getItem(j, k) - B * getItem(i, k));
						}
				}

			for (int j = 0; j < sizeX; j++)
				if (i != j) {
					if (i > j) {
						setItem(i, j, -getItem(i, j) / A);
						setItem(j, i, -getItem(j, i) / A);
					} else {
						setItem(i, j, getItem(i, j) / A);
						setItem(j, i, getItem(j, i) / A);
					}
				}
			setItem(i, i, 1 / A);
		}

		for (int i = xchg.size() - 1; i >= 0; i--) {
			XchgRec x = xchg.get(i);
			exchangeY(x.a, x.b);
		}
		return true;
	}

	/**
	 * Computes the determinant of this matrix. While computing the determinant
	 * creates internally a matrix of the same size as this.
	 *
	 * @return Returns the determinant of the matrix or 0 if the determinant is
	 *         incomputable.
	 */
	public double det() {
		if (sizeX != sizeY) {
			throw new Error("Invalid argument");
		}
		double D;
		double result = 1;
		Matrix tmp = makeCopy();

		for (int i = 0; i < sizeX; i++) {
			if (tmp.getItem(i, i) == 0) {
				boolean err = true;
				for (int a = sizeY - 1; a > i; a--)
					if (tmp.getItem(i, a) != 0) {
						tmp.sumY(i, a);
						err = false;
						break;
					}
				if (err)
					return 0; // 0 -> the matrix has NO determinant
			}

			D = tmp.getItem(i, i);
			if (D != 1) {
				tmp.mulY(i, 1 / D);
				result /= D;
			}

			for (int a = 0; a < sizeX; a++)
				if (a != i) {
					D = getItem(a, i);
					if (D != 0) {
						tmp.mulY(a, -1 / D);
						result /= -D;
						tmp.sumY(a, i);
					}
				}
		}
		return result;
	}

	/**
	 * Compares this matrix to the second and returns the correlation between
	 * them.
	 */
	public MatrixCompareResult compareTo(Matrix second) {
		if ((sizeX != second.sizeX) || (sizeY != second.sizeY)) {
			throw new IllegalArgumentException("Comparing matrices of different size");
		}
		MatrixCompareResult res = new MatrixCompareResult();
		// *** Изчисляване на корелацията (Pearson's r) между данните. ***

		// Средно аритметично.
		double S = sizeX * sizeY;
		res.AvgA = sumAll() / S;
		res.AvgB = second.sumAll() / S;
		res.SAA = 0;
		res.SAB = 0;
		res.SBB = 0;
		// Коефициенти на корелация.
		double dA, dB;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				dA = getItem(i, j) - res.AvgA;
				dB = second.getItem(i, j) - res.AvgB;
				res.SAA += dA * dA;
				res.SBB += dB * dB;
				res.SAB += dA * dB;
			}

		// Коефициент на корелация на Pearson
		res.PearsonR = res.SAB / Math.sqrt(res.SAA * res.SBB);
		return res;
	}

	public void printM(String title) {
		System.out.println(title);
//		System.out.print(toString());
		for (int j = 0; j < sizeY; j++) {
			for (int i = 0; i < sizeX; ) {
				System.out.print(String.format(Locale.US, "%12.8f\t",new Object[] { new Double(getItem(i, j)) } ));
				i++;
//				if (i % 5 == 0)
//					System.out.println();
			}
			System.out.println();
		}
	}

	public void lq(Matrix q, Matrix tau) {
		q.resize(getSizeY(), getSizeY());
		lqDecomposition(tau);
		lqDecompositionGetQ(tau, q);
		lqDecompositionGetL(this);
	}

	// //////////
	public void lqDecomposition(Matrix tau) {
		int minXY = Math.min(sizeX, sizeY);
		if ((tau.getSizeX() < minXY) || (tau.getSizeY() < 1))
			tau.resize(minXY, 1);
		for (int atIndex = 0; atIndex < minXY; atIndex++) {
			if (atIndex >= getSizeX() - 1) {
				tau.setItem(atIndex, 0, 0.0);
				continue;
			}
			// Generate elementary reflector H(i) to annihilate A(i,i+1:n)
			// DLARFG
			double xnorm = 0.0;
			for (int i = getSizeX() - 1; i > atIndex; i--)
				xnorm = MathUtil.hypot(xnorm, getItem(i, atIndex));
			if (xnorm == 0.0) {
				tau.setItem(atIndex, 0, 0.0);
				continue;
			}

			double alpha = getItem(atIndex, atIndex);
			//double beta = -SIGN(Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2)), alpha);
			//double beta = Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2));
			double beta = MathUtil.hypot(alpha, xnorm);
			if (beta == 0.0) {
				tau.setItem(atIndex, 0, 0.0);
				continue;
			}
			if (alpha >= 0.0)
				beta = -beta;
			double tmp_tau = (beta - alpha) / beta;
			tau.setItem(atIndex, 0, tmp_tau);
			double scale = 1.0 / (alpha - beta);
			for (int i = getSizeX() - 1; i > atIndex; i--)
				setItem(i, atIndex, scale * getItem(i, atIndex));
			// End DLARFG

			// DGELQ2:109 Apply H(i) to A(i+1:m,i:n) from the right
			setItem(atIndex, atIndex, 1.0);
			svdDLARF_Y(atIndex, tmp_tau);
			setItem(atIndex, atIndex, beta);
		}
	}

	public void lqDecompositionGetL(Matrix l) {
		l.resize(getSizeX(), getSizeY());
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--)
				l.setItem(i, j, i > j ? 0.0 : getItem(i, j));
	}

	public void lqDecompositionGetQ(Matrix tau, Matrix q) {
		if ((tau.getSizeX() < getSizeY()) || (tau.getSizeY() != 1))
			throw new IllegalArgumentException("Invalid parameter");

		q.resize(getSizeX(), getSizeX());
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeX() - 1; j >= 0; j--)
				q.setItem(i, j, (i >= j) && (j < getSizeY()) ? getItem(i, j) : i == j ? 1.0 : 0.0);

		for (int atIndex = getSizeY() - 1; atIndex >= 0; atIndex--) {
			if (atIndex < q.getSizeX()) {
				double tmp_tau = tau.getItem(atIndex, 0);
				q.setItem(atIndex, atIndex, 1.0);
				q.svdDLARF_Y(atIndex, tmp_tau);

				for (int i = q.getSizeX() - 1; i > atIndex; i--)
					q.setItem(i, atIndex, q.getItem(i, atIndex) * (-tmp_tau));
				q.setItem(atIndex, atIndex, 1.0 - tmp_tau);
				for (int i = atIndex - 1; i >= 0; i--)
					q.setItem(i, atIndex, 0.0);
			}
		}
	}

	public void qr(Matrix q, Matrix tau) {
		q.resize(getSizeX(), getSizeX());
		tau.resize(getSizeX(), 1);
		qrDecomposition(tau);
		qrDecomositionGetQ(tau, q);
		qrDecomositionGetR(this);
	}

	// private static final double SAFMIN = 1.0E-30; // 2.00416836E-292;
	public void qrDecomposition(Matrix tau) {
		int minXY = Math.min(sizeX, sizeY);

		if ((tau.getSizeX() < minXY) || (tau.getSizeY() < 1))
			tau.resize(minXY, 1);
		for (int atIndex = 0; atIndex < minXY; atIndex++) {
			if (atIndex >= getSizeY() - 1) {
				tau.setItem(atIndex, 0, 0.0);
				continue;
			}
			// Generate elementary reflector H(i) to annihilate A(i+1:m,i)
			// DLARFG
			double xnorm = 0.0;
			for (int j = getSizeY() - 1; j > atIndex; j--)
				xnorm = MathUtil.hypot(xnorm, getItem(atIndex, j));
			if (xnorm == 0.0) {
				tau.setItem(atIndex, 0, 0.0);
				continue;
			}

			double alpha = getItem(atIndex, atIndex);
			//double beta = -SIGN(Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2)), alpha);
			//double beta = Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2));
			double beta = MathUtil.hypot(alpha, xnorm);
			if (beta == 0.0) {
				tau.setItem(atIndex, 0, 0.0);
				continue;
			}
			if (alpha >= 0.0)
				beta = -beta;
			double tmp_tau = (beta - alpha) / beta;
			tau.setItem(atIndex, 0, tmp_tau);
			double scale = 1.0 / (alpha - beta);
			for (int j = getSizeY() - 1; j > atIndex; j--)
				setItem(atIndex, j, scale * getItem(atIndex, j));
			// End DLARFG

			// DGEQR2:109 Apply H(i) to A(i:m,i+1:n) from the left
			setItem(atIndex, atIndex, 1.0);
			svdDLARF_X(atIndex, tmp_tau);
			setItem(atIndex, atIndex, beta);
		}
	}

	public void qrDecomositionGetR(Matrix r) {
		r.resize(getSizeX(), getSizeY());
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--)
				r.setItem(i, j, i < j ? 0.0 : getItem(i, j));
	}

	public void qrDecomositionGetQ(Matrix tau, Matrix q) {
		if ((tau.getSizeX() < getSizeX()) || (tau.getSizeY() < 1))
			throw new IllegalArgumentException("Invalid parameter");
		q.resize(getSizeY(), getSizeY());
		for (int i = getSizeY() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--)
				q.setItem(i, j, (i <= j) && (i < getSizeX()) ? getItem(i, j) : i == j ? 1.0 : 0.0);

		for (int atIndex = getSizeX() - 1; atIndex >= 0; atIndex--) {
			if (atIndex < q.getSizeY()) {
				double tmp_tau = tau.getItem(atIndex, 0);
				q.setItem(atIndex, atIndex, 1.0);
				q.svdDLARF_X(atIndex, tmp_tau);

				for (int j = q.getSizeY() - 1; j > atIndex; j--)
					q.setItem(atIndex, j, q.getItem(atIndex, j) * (-tmp_tau));
				q.setItem(atIndex, atIndex, 1.0 - tmp_tau);
				for (int j = atIndex - 1; j >= 0; j--)
					q.setItem(atIndex, j, 0.0);
			}
		}
	}

	/**
	 * DLARF applies a real elementary reflector H to a real m by n matrix
	 * C, from either the left or the right. H is represented in the form
	 *       H = I - tau * v * v'
	 * where tau is a real scalar and v is a real vector.
	 * If tau = 0, then H is taken to be the unit matrix.
	 * (DLARF Left)
	 */
	private void svdDLARF_X(int atX, double tau) {

		for (int i = getSizeX() - 1; i > atX; i--) {
			double sum = 0.0;
			for (int j = getSizeY() - 1; j >= atX; j--)
				sum += getItem(i, j) * getItem(atX, j);
			for (int j = getSizeY() - 1; j >= atX; j--)
				setItem(i, j, getItem(i, j) - tau * sum * getItem(atX, j));
		}
	}

	/**
	 * DLARF applies a real elementary reflector H to a real m by n matrix
	 * C, from either the left or the right. H is represented in the form
	 *       H = I - tau * v * v'
	 * where tau is a real scalar and v is a real vector.
	 * If tau = 0, then H is taken to be the unit matrix.
	 * (DLARF Right)
	 */
	private void svdDLARF_Y(int atY, double tau) {
		for (int j = getSizeY() - 1; j > atY; j--) {
			double sum = 0.0;
			for (int i = getSizeX() - 1; i >= atY; i--)
				sum += getItem(i, j) * getItem(i, atY);
			for (int i = getSizeX() - 1; i >= atY; i--)
				setItem(i, j, getItem(i, j) - tau * sum * getItem(i, atY));
		}
	}

/*	public void luDecomposition() {
		boolean found;
		Matrix permMatr = new Matrix(getSizeX(), getSizeY());
		permMatr.makeE();

		for (int k = 0; k < getSizeX(); k++)
			for (int i = k + 1; k < getSizeX(); i++) {
				if (getItem(k, k) == 0.0) {
					found = false;
					for (int p = k + 1; p < getSizeX(); p++) {
						if (getItem(0, k) != 0) {
							exchangeX(k, p);
							permMatr.exchangeX(k, p);
							found = true;
							break;
						}
					}
					if (!found)
						throw new ArithmeticException("LU decomposition failed. Matrix is singular");
				}

				throw new RuntimeException("not implemented");
				// ????
			}
	}*/
}
