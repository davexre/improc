package com.slavi.math.matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import com.slavi.math.MathUtil;

/**
 * Rectangular matrix.
 *
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
public class Matrix <T extends Matrix<T>> extends Vector<T> implements IMatrix<T> {

	/**
	 * Number of columns
	 */
	private int sizeX;

	/**
	 * Number of rows
	 */
	private int sizeY;

	boolean transposed = false;

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
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || !(obj instanceof Matrix))
			return false;
		Matrix a = (Matrix) obj;
		if ((a.getSizeX() != getSizeX()) || (a.getSizeY() != getSizeY()))
			return false;
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--)
				if (getItem(i, j) != a.getItem(i, j))
					return false;
		return true;
	}

	public boolean equals(Matrix a, double tolerance) {
		if (a == null)
			return false;
		if (a == this)
			return true;
		if ((getSizeX() != a.getSizeX()) || (getSizeY() != a.getSizeY()))
			return false;
		tolerance = Math.abs(tolerance);
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--)
				if (Math.abs(getItem(i, j) - a.getItem(i, j)) > tolerance)
					return false;
		return true;
	}

	public String toMatlabString(String variableName) {
		StringBuilder result = new StringBuilder();
		result.append(variableName);
		result.append("=[");
		for (int j = 0; j < getSizeY(); j++) {
			if (j != 0)
				result.append(";\n");
			for (int i = 0; i < getSizeX(); i++) {
				if (i != 0)
					result.append(" ");
				//result.append(String.format(Locale.US, "%1$27.19f",new Object[] { new Double(.getItem(i, j]) } ));
				result.append(getItem(i, j));
			}
		}
		result.append("];");
		return result.toString();
	}

	public String toOneLineString() {
		StringBuilder result = new StringBuilder();
		for (int j = 0; j < getSizeY(); j++) {
			if (j != 0)
				result.append(";");
			for (int i = 0; i < getSizeX(); i++) {
				if (i != 0)
					result.append(" ");
				result.append(String.format(Locale.US, "%1$10.4f",
						new Object[] { new Double(getItem(i, j)) } ));
			}
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
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int j = 0; j < getSizeY(); j++) {
			for (int i = 0; i < getSizeX(); i++) {
				if (i != 0)
					result.append(" ");
				result.append(MathUtil.d4(getItem(i, j)));
			}
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 * Loads the matrix from a text stream.
	 * @return this
	 */
	public Matrix load(BufferedReader fin) throws IOException {
		for (int j = 0; j < getSizeY(); j++) {
			StringTokenizer st = new StringTokenizer(fin.readLine());
			for (int i = 0; i < getSizeX(); i++)
				setItem(i, j, st.hasMoreTokens() ? Double.parseDouble(st.nextToken()) : 0.0);
		}
		return this;
	}

	/**
	 * Saves the matrix to a text stream.
	 * @return this
	 */
	public Matrix save(PrintStream fou) {
		for (int j = 0; j < getSizeY(); j++) {
			for (int i = 0; i < getSizeX(); i++) {
				if (i != 0)
					fou.print("\t");
				fou.print(getItem(i, j));
			}
			fou.print("\n");
		}
		return this;
	}

	public static Matrix fromArray(double d[][]) {
		int sizeX = d.length;
		int sizeY = sizeX == 0 ? 0 : d[0].length;
		Matrix r = new Matrix(sizeX, sizeY);
		for (int i = r.getSizeX() - 1; i >= 0; i--) {
			for (int j = r.getSizeY() - 1; j >= 0; j--) {
				r.setItem(i, j, d[i][j]);
			}
		}
		return r;
	}

	/**
	 * Resizes the matrix if the new size differs from the current matrix size.
	 * @return this
	 */
	public Matrix resize(int newSizeX, int newSizeY) {
		if ((newSizeX < 0) || (newSizeY < 0)) {
			throw new Error("Invalid matrix size");
		}
		int newSize = newSizeX * newSizeY;
		if (m == null || m.length != newSize)
			m = newSize == 0 ? null : new double[newSize];
		if (transposed) {
			sizeY = newSizeX;
			sizeX = newSizeY;
		} else {
			sizeX = newSizeX;
			sizeY = newSizeY;
		}
		return this;
	}

	/**
	 * Returns the number of columns in the matrix.
	 */
	@Override
	public int getSizeX() {
		return transposed ? sizeY : sizeX;
	}

	/**
	 * Returns the number of rows in the matrix.
	 */
	@Override
	public int getSizeY() {
		return transposed ? sizeX : sizeY;
	}

	private int getItemIndex(int atX, int atY) {
		if (atX < 0 || atX >= getSizeX() ||
			atY < 0 || atY >= getSizeY())
			throw new IndexOutOfBoundsException("Indexes (" + atX + "," + atY + ") not in range (" + getSizeX() + "," + getSizeY() + ")");
		return transposed ? atY + atX * sizeX : atX + atY * sizeX;
	}

	/**
	 * Returns the value of the matrix atX column and atY row. The top-left
	 * element is atX=0, atY=0.
	 */
	@Override
	public double getItem(int atX, int atY) {
		return m[getItemIndex(atX, atY)];
	}

	/**
	 * Sets the value of the matrix atX column and atY row. The top-left element
	 * is atX=0, atY=0.
	 */
	@Override
	public void setItem(int atX, int atY, double aValue) {
		m[getItemIndex(atX, atY)] = aValue;
	}

	/**
	 * Returns the new value.
	 */
	@Override
	public double itemAdd(int atX, int atY, double aValue) {
		return m[getItemIndex(atX, atY)] += aValue;
	}

	/**
	 * Returns the new value.
	 */
	@Override
	public double itemMul(int atX, int atY, double aValue) {
		return m[getItemIndex(atX, atY)] *= aValue;
	}

	/**
	 * Multiplies two matrices and stores the result in dest matrix. If the dest
	 * matrix is of incorrect size it will be resized. The formula is:<br>
	 * <tt>dest = this * second<br>
	 * </tt>
	 * Requires: this != second != dest
	 * @param dest	If null a new Matrix will be created to store the result
	 * @return dest
	 */
	public Matrix mMul(Matrix second, Matrix dest) {
		if (this == dest || second == dest) {
			throw new Error("Invalid argument");
		}
		if (getSizeX() != second.getSizeY()) {
			throw new Error("Invalid argument this(" +
						getSizeX() + "," + getSizeY() + ") * second(" +
						second.getSizeX() + "," + second.getSizeY() + ")");
		}
		if (dest == null)
			dest = new Matrix(second.getSizeX(), getSizeY());
		else
			dest.resize(second.getSizeX(), getSizeY());
		double D;
		for (int i = second.getSizeX() - 1; i >= 0; i--) {
			for (int j = getSizeY() - 1; j >= 0; j--) {
				D = 0;
				for (int k = getSizeX() - 1; k >= 0; k--)
					D += getItem(k, j) * second.getItem(i, k);
				dest.setItem(i, j, D);
			}
		}
		return dest;
	}

	/**
	 * Performs an element by element sum of two matrices of equal size and
	 * stores the result in dest matrix. If the dest matrix is of incorrect size
	 * it will be resized to the same size as the source matrix. The formula is:<br>
	 * <tt>dest[i, j] = this[i, j] + second[i, j]<br>
	 * </tt>
	 * Allows: this == second == dest
	 * @param dest	If null a new Matrix will be created to store the result
	 * @return dest
	 */
	public Matrix mSum(Matrix second, Matrix dest) {
		assertSameSize(second);
		if (dest == null)
			dest = new Matrix(getSizeX(), getSizeY());
		else
			dest.resize(getSizeX(), getSizeY());
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--)
				dest.setItem(i, j, getItem(i, j) + second.getItem(i, j));
		return dest;
	}

	/**
	 * Performs an element by element subtraction of two matrices of equal size
	 * and stores the result in dest matrix. If the dest matrix is of incorrect
	 * size it will be resized to the same size as the source matrix. The
	 * formula is:<br>
	 * <tt>dest[i, j] = this[i, j] - second[i, j]<br>
	 * </tt>
	 * Allows: this == second == dest
	 * @param dest	If null a new Matrix will be created to store the result
	 * @return dest
	 */
	public Matrix mSub(Matrix second, Matrix dest) {
		assertSameSize(second);
		if (dest == null)
			dest = new Matrix(getSizeX(), getSizeY());
		else
			dest.resize(getSizeX(), getSizeY());
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--)
				dest.setItem(i, j, getItem(i, j) - second.getItem(i, j));
		return dest;
	}

	/**
	 * Returns the dot product of the matrix. The formula is:<br>
	 * <tt>Result = Sum( this[i, j] * second[i, j] )<br>
	 * </tt>
	 */
	public double dotProduct(Matrix second) {
		assertSameSize(second);
		double sum = 0;
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--)
				sum += getItem(i, j) * second.getItem(i, j);
		return sum;
	}

	/**
	 * Performs an element by element abs and
	 * stores the result in dest matrix. If the dest matrix is of incorrect size
	 * it will be resized to the same size as the source matrix. The formula is:<br>
	 * <tt>dest[i, j] = abs(this[i, j])<br>
	 * </tt>
	 * Allows: this == dest
	 * @param dest	If null a new Matrix will be created to store the result
	 * @return dest
	 */
	public Matrix termAbs(Matrix dest) {
		if (dest == null)
			dest = new Matrix(getSizeX(), getSizeY());
		else
			dest.resize(getSizeX(), getSizeY());
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--)
				dest.setItem(i, j, Math.abs(getItem(i, j)));
		return dest;
	}

	/**
	 * Performs an element by element multiplication of two matrices of equal
	 * size and stores the result in dest matrix. If the dest matrix is of
	 * incorrect size it will be resized to the same size as the source matrix.
	 * The formula is:<br>
	 * <tt>dest[i, j] = this[i, j] * second[i, j]<br>
	 * </tt>
	 * Allows: this == dest
	 * @param dest	If null a new Matrix will be created to store the result
	 * @return dest
	 */
	public Matrix termMul(Matrix second, Matrix dest) {
		assertSameSize(second);
		if (dest == null)
			dest = new Matrix(getSizeX(), getSizeY());
		else
			dest.resize(getSizeX(), getSizeY());
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--)
				dest.setItem(i, j, getItem(i, j) * second.getItem(i, j));
		return dest;
	}

	/**
	 * Performs an element by element division of two matrices of equal size and
	 * stores the result in dest matrix. If the dest matrix is of incorrect size
	 * it will be resized to the same size as the source matrix. The formula is:<br>
	 * <tt>dest[i, j] = this[i, j] / second[i, j]<br>
	 * </tt> <b>Warning:</b><i>If there is an element that is zero, an
	 * exception will rise <code>java.lang.ArithmeticException</code>.</i>
	 * Allows: this == second == dest
	 * @param dest	If null a new Matrix will be created to store the result
	 * @return dest
	 */
	public Matrix termDiv(Matrix second, Matrix dest) {
		assertSameSize(second);
		if (dest == null)
			dest = new Matrix(getSizeX(), getSizeY());
		else
			dest.resize(getSizeX(), getSizeY());
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--)
				dest.setItem(i, j, getItem(i, j) / second.getItem(i, j));
		return dest;
	}

	/**
	 * Sets the elements of dest matrix to the maximum corresponding elements of
	 * this and second matrix. If the dest matrix is of incorrect size it will
	 * be resized. The formula is:<br>
	 * <tt>dest[i, j] = max(abs(this[i, j]), abs(second[i, j]))<br>
	 * </tt>
	 * Allows: this == second == dest
	 * @param dest	If null a new Matrix will be created to store the result
	 * @return dest
	 */
	public Matrix mMaxAbs(Matrix second, Matrix dest) {
		assertSameSize(second);
		if (dest == null)
			dest = new Matrix(getSizeX(), getSizeY());
		else
			dest.resize(getSizeX(), getSizeY());
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--) {
				double a = Math.abs(getItem(i, j));
				double b = Math.abs(second.getItem(i, j));
				dest.setItem(i, j, a > b ? a : b);
			}
		return dest;
	}

	/**
	 * Sets the elements of dest matrix to the maximum corresponding elements of
	 * this and second matrix. If the dest matrix is of incorrect size it will
	 * be resized. The formula is:<br>
	 * <tt>dest[i, j] = max( this[i, j] , second[i, j] )<br>
	 * </tt>
	 * Allows: this == second == dest
	 * @param dest	If null a new Matrix will be created to store the result
	 * @return dest
	 */
	public Matrix mMax(Matrix second, Matrix dest) {
		assertSameSize(second);
		if (dest == null)
			dest = new Matrix(getSizeX(), getSizeY());
		else
			dest.resize(getSizeX(), getSizeY());
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--) {
				double a = getItem(i, j);
				double b = second.getItem(i, j);
				dest.setItem(i, j, a > b ? a : b);
			}
		return dest;
	}

	/**
	 * Sets the elements of dest matrix to the minimum corresponding elements of
	 * this and second matrix. If the dest matrix is of incorrect size it will
	 * be resized. The formula is:<br>
	 * <tt>dest[i, j] = min( this[i, j] , second[i, j] )<br>
	 * </tt>
	 * Allows: this == second == dest
	 * @param dest	If null a new Matrix will be created to store the result
	 * @return dest
	 */
	public Matrix mMin(Matrix second, Matrix dest) {
		assertSameSize(second);
		if (dest == null)
			dest = new Matrix(getSizeX(), getSizeY());
		else
			dest.resize(getSizeX(), getSizeY());
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--) {
				double a = getItem(i, j);
				double b = second.getItem(i, j);
				dest.setItem(i, j, a < b ? a : b);
			}
		return dest;
	}

	/**
	 * Makes a transposed matirx of this matrix. If the dest matrix is of
	 * incorrect size it will be resized.
	 * Requires: this != dest
	 * @param dest	If null a new Matrix will be created to store the result
	 * @return dest
	 */
	public Matrix transpose(Matrix dest) {
		if (this == dest) {
			throw new Error("Invalid argument");
		}
		if (dest == null)
			dest = new Matrix(getSizeY(), getSizeX());
		else
			dest.resize(getSizeY(), getSizeX());
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--)
				dest.setItem(j, i, getItem(i, j));
		return dest;
	}

	/**
	 * Care should be taken when reading/writing from/to json/xml files a transposed matrix.
	 * If a matrix A is transposed  and save to json and read back to a matrix B then the
	 * A.getItem(i,j) = B.getItem(i,j) and A.getVectorItem(i) != B.getVectorItem(i).
	 * @return this
	 */
	@Override
	public T transpose() {
		transposed = !transposed;
		return (T) this;
	}

	public boolean isTransposed() {
		return transposed;
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
	 * @return this
	 */
	public Matrix normalize2() {
		if ((getSizeX() == 0) || (getSizeY() == 0))
			return this;
		double maxVal = getItem(0, 0);
		double minVal = maxVal;
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--) {
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
			for (int i = getSizeX() - 1; i >= 0; i--)
				for (int j = getSizeY() - 1; j >= 0; j--)
					setItem(i, j, (getItem(i, j) - minVal) / delta);
		}
		return this;
	}

	/**
	 * Returns the sum of the squares of all elements of the matrix.
	 */
	public double sumPow2() {
		double D = 0;
		for (int i = getVectorSize() - 1; i >= 0; i--) {
			double v = getVectorItem(i);
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
		Matrix result = new Matrix(getSizeX(), getSizeY());
		copyTo(result);
		return result;
	}

	/**
	 * Copies this matrix to a destination. The destination is resized if
	 * necessary.
	 * Allows: this == dest
	 * @param dest	If null a new Matrix will be created to store the result
	 * @return dest
	 */
	public Matrix copyTo(Matrix dest) {
		if (dest == null) {
			dest = new Matrix(getSizeX(), getSizeY());
		}
		dest.transposed = transposed;
		dest.resize(getSizeX(), getSizeY());
		System.arraycopy(this.m, 0, dest.m, 0, this.m.length);
		return dest;
	}

	/**
	 * Makes the identity matrix. The formula is:<br>
	 * <tt>Result[i, j] = (i == j) ? 1 : 0<br>
	 * </tt>
	 * @return this
	 */
	public Matrix makeE() {
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--)
				setItem(i, j, (i == j) ? 1.0 : 0.0);
		return this;
	}

	public double getSquaredDeviationFromE() {
		double result = 0.0;
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--) {
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
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--) {
				double d = i == j ? getItem(i, j) - 1.0 : getItem(i, j);
				if (Math.abs(d) > tolerance)
					return false;
			}
		return true;
	}

	/**
	 * Multiplies all elements at column atX with D.
	 * @return this
	 */
	public Matrix mulX(int atX, double D) {
		for (int j = getSizeY() - 1; j >= 0; j--)
			itemMul(atX, j, D);
		return this;
	}

	/**
	 * Multiplies all elements at row atY with D.
	 * @return this
	 */
	public Matrix mulY(int atY, double D) {
		for (int i = getSizeX() - 1; i >= 0; i--)
			itemMul(i, atY, D);
		return this;
	}

	/**
	 * Sums the elements at column atX1 with the elements at column atX2 and
	 * stores the result in column atX1. The formula is:<br>
	 * <tt>this[atX1, j] = this[atX1, j] + this[atX2, j]<br>
	 * </tt>
	 * @return this
	 */
	public Matrix sumX(int atX1, int atX2) {
		for (int j = getSizeY() - 1; j >= 0; j--)
			itemAdd(atX1, j, getItem(atX2, j));
		return this;
	}

	/**
	 * Sums the elements at row atY1 with the elements at row atY2 and stores
	 * the result in row atY1. The formula is:<br>
	 * <tt>this[i, atX1] = this[i, atX1] + this[i, atX2]<br>
	 * </tt>
	 * @return this
	 */
	public Matrix sumY(int atY1, int atY2) {
		for (int i = getSizeX() - 1; i >= 0; i--)
			itemAdd(i, atY1, getItem(i, atY2));
		return this;
	}

	/**
	 * Exchanges the column atX1 with column atX2.
	 * @return this
	 */
	public Matrix exchangeX(int atX1, int atX2) {
		for (int j = getSizeY() - 1; j >= 0; j--) {
			double D = getItem(atX1, j);
			setItem(atX1, j, getItem(atX2, j));
			setItem(atX2, j, D);
		}
		return this;
	}

	/**
	 * Exchanges the row atY1 with row atY2.
	 * @return this
	 */
	public Matrix exchangeY(int atY1, int atY2) {
		for (int i = getSizeX() - 1; i >= 0; i--) {
			double D = getItem(i, atY1);
			setItem(i, atY1, getItem(i, atY2));
			setItem(i, atY2, D);
		}
		return this;
	}

	/**
	 * Private class used by Matrix.inverse()
	 */
	public static class XchgRec {

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
	public boolean inverse_OLD() {
		if (getSizeX() != getSizeY()) {
			throw new Error("Invalid argument");
		}
		double tol = calcEpsTolerance();
		ArrayList<XchgRec> xchg = new ArrayList<XchgRec>();

		for (int i = 0; i < getSizeX(); i++) {
			double A = Math.abs(getItem(i, i));
			int pivotIndex = i;
			for (int j = i + 1; j < getSizeX(); j++) {
				double tmp = Math.abs(getItem(j, i));
				if (tmp > A) {
					A = tmp;
					pivotIndex = j;
				}
			}

			if (A < tol) {
				make0();
				return false;
			}
			if (pivotIndex != i) {
				exchangeX(i, pivotIndex);
				xchg.add(new XchgRec(i, pivotIndex));
			}
			A = getItem(i, i);

			A = 1.0 / A;
			for (int j = 0; j < getSizeX(); j++)
				if (i != j) {
					double B = getItem(j, i) * A;
					for (int k = 0; k < getSizeX(); k++)
						if (k != i) {
							if ((k < i) && (j < i))
								itemAdd(j, k,  B * getItem(i, k));
							else
								itemAdd(j, k, -B * getItem(i, k));
						}
				}

			for (int j = 0; j < getSizeX(); j++)
				if (i != j) {
					if (i > j) {
						itemMul(i, j, -A);
						itemMul(j, i, -A);
					} else {
						itemMul(i, j,  A);
						itemMul(j, i,  A);
					}
				}
			setItem(i, i, A);
		}

		for (int i = xchg.size() - 1; i >= 0; i--) {
			XchgRec x = xchg.get(i);
			exchangeY(x.a, x.b);
		}
		return true;
	}

	/**
	 * Calculates the inverse matrix of this matrix. The algorithm calculates
	 * the inverse matrix "in place" and does NOT create any intermediate
	 * matrices.
	 * <p>Algorythm is described <a href="https://file.scirp.org/pdf/AM_2013100413422038.pdf">here</a>.
	 * <p>A copy also available: <a href="doc-files/Matrix-In-place-inverse.pdf">Matrix In-place inverse.pdf</a>
	 *
	 * @return Returns true if the inverse matrix is computable. If the inverse
	 *         matrix can not be computed this.make0 is called and the returned
	 *         value is false.
	 */
	public boolean inverse() {
		if (getSizeX() != getSizeY()) {
			throw new Error("Invalid argument");
		}
		ArrayList<XchgRec> xchg = new ArrayList<XchgRec>();
		BitSet bs = new BitSet(getSizeY());
		double tol = calcEpsTolerance();

		for (int row = 0; row < getSizeY(); row++) {
			// Find pivot row
			int prow = 0;
			int pcol = 0;
			double tmp = 0;
			for (int j = 0; j < getSizeY(); j++) {
				if (bs.get(j))
					continue;
				for (int i = 0; i < getSizeX(); i++) {
					if (bs.get(i))
						continue;
					double t = Math.abs(getItem(i, j));
					if (tmp < t) {
						tmp = t;
						prow = j;
						pcol = i;
					}
				}
			}

			if (tmp < tol) {
				make0();
				return false;
			}
			if (pcol != prow) {
				exchangeY(prow, pcol);
				xchg.add(new XchgRec(prow, pcol));
				prow = pcol;
			}

			bs.set(prow);
			tmp = 1.0 / getItem(prow, prow);
			setItem(prow, prow, 1.0);
			for (int j = 0; j < getSizeX(); j++)
				itemMul(j, prow, tmp);

			for (int i = 0; i < getSizeY(); i++) {
				if (i == prow)
					continue;
				tmp = -getItem(prow, i);
				for (int j = 0; j < getSizeX(); j++)
					if (j != prow)
						itemAdd(j, i, tmp * getItem(j, prow));
					else
						setItem(j, i, tmp * getItem(j, prow));
			}
		}

		for (int i = xchg.size() - 1; i >= 0; i--) {
			XchgRec x = xchg.get(i);
			exchangeX(x.a, x.b);
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
		if (getSizeX() != getSizeY()) {
			throw new Error("Invalid argument");
		}
		double D;
		double result = 1;
		Matrix tmp = makeCopy();

		for (int i = 0; i < getSizeX(); i++) {
			if (tmp.getItem(i, i) == 0) {
				boolean err = true;
				for (int a = getSizeY() - 1; a > i; a--)
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

			for (int a = 0; a < getSizeX(); a++)
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

	public double calcEpsTolerance() {
		return MathUtil.eps * Math.max(getSizeX(), getSizeY()) * getNormInfinity();
	}

	/**
	 * Row Reduced Echalon Form. Translated from Octave rref.m
	 * The return value contains the list of "bound variables",
	 * which are those columns on which elimination has been performed.
	 */
	public List<Integer> rref() {
		double tol = calcEpsTolerance();
		int r = 0;
		ArrayList<Integer> k = new ArrayList();
		for (int c = 0; c < getSizeX(); c++) {
			// Find the pivot row
			int pivot = getSizeY() - 1;
			double m = 0;
			double v = 0;
			for (int j = pivot; j >= r; j--) {
				double v1 = getItem(c, j);
				double m1 = Math.abs(v1);
				if (m1 > m) {
					v = v1;
					m = m1;
					pivot = j;
				}
			}

			if (m < tol) {
				// Skip column c, making sure the approximately zero terms are actually zero.
				for (int j = getSizeY() - 1; j >= r; j--)
					setItem(c, j, 0);
			} else {
				// keep track of bound variables
				k.add(c);

				// Swap current row and pivot row
				exchangeY(pivot, r);
				// Normalize pivot row
				for (int i = getSizeX() - 1; i >= c; i--)
					itemMul(i, r, 1.0 / v);
				// Eliminate the current column
				for (int j = getSizeY() - 1; j >= 0; j--) {
					if (j == r)
						continue;
					double t = -getItem(c, j);
					for (int i = getSizeX() - 1; i >= c; i--)
						itemAdd(i, j, t * getItem(i, r));
				}
				// Check if done
				if (++r >= getSizeY())
					break;
			}
		}
		return k;
	}

	/**
	 * @return this
	 */
	public Matrix printM(String title) {
		System.out.println(title);
//		System.out.print(toString());
		for (int j = 0; j < getSizeY(); j++) {
			for (int i = 0; i < getSizeX(); ) {
				System.out.print(String.format(Locale.US, "%12.8f\t",new Object[] { new Double(getItem(i, j)) } ));
				i++;
//				if (i % 5 == 0)
//					System.out.println();
			}
			System.out.println();
		}
		return this;
	}

	public void lq(Matrix q, Matrix tau) {
		q.resize(getSizeY(), getSizeY());
		lqDecomposition(tau);
		lqDecompositionGetQ(tau, q);
		lqDecompositionGetL(this);
	}

	// //////////
	public void lqDecomposition(Matrix tau) {
		int minXY = Math.min(getSizeX(), getSizeY());
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
				itemMul(i, atIndex, scale);
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
					q.itemMul(i, atIndex, -tmp_tau);
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
		int minXY = Math.min(getSizeX(), getSizeY());

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
				itemMul(atIndex, j, scale);
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
					q.itemMul(atIndex, j, -tmp_tau);
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
				itemAdd(i, j, -tau * sum * getItem(atX, j));
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
				itemAdd(i, j, -tau * sum * getItem(i, atY));
		}
	}
}
