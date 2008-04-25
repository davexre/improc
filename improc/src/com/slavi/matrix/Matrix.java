package com.slavi.matrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.testpackage.MatrixTest;
import com.slavi.utils.XMLHelper;

public class Matrix {

	/**
	 * The elements of the matrix.
	 */
	public double m[][];

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
	 * @return Returns true if all the ements of the matrices are equal.
	 */
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Matrix))
			return false;
		if (obj == this)
			return true;
		Matrix a = (Matrix) obj;
		if ((a.sizeX != sizeX) || (a.sizeY != sizeY))
			return false;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				if (m[i][j] != a.m[i][j])
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
				//result.append(String.format(Locale.US, "%1$27.19f",new Object[] { new Double(m[i][j]) } ));
				result.append(m[i][j]);
			}
			result.append(";");
		}
		result.append("];");
		return result.toString();
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
				//result.append(String.format(Locale.US, "%1$27.19f",new Object[] { new Double(m[i][j]) } ));
				result.append(m[i][j]);
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
				m[i][j] = st.hasMoreTokens() ? Double.parseDouble(st.nextToken()) : 0.0;
		}
	}

	/**
	 * Saves the matrix to a text stream
	 */
	public void save(PrintStream fou) throws IOException {
		fou.printf(this.toString(), (Object[])null);
	}

	/**
	 * Resizes the matrix if the new size differs from the current matrix size.
	 */
	public void resize(int newSizeX, int newSizeY) {
		if ((newSizeX < 0) || (newSizeY < 0)) {
			throw new Error("Invalid matrix size");
		}
		if ((newSizeX == sizeX) && (newSizeY == sizeY)) {
			return;
		}
		sizeX = newSizeX;
		sizeY = newSizeY;
		m = new double[newSizeX][newSizeY];
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
		return m[atX][atY];
	}

	/**
	 * Sets the value of the matrix atX column and atY row. The top-left element
	 * is atX=0, atY=0.
	 */
	public void setItem(int atX, int atY, double aValue) {
		m[atX][atY] = aValue;
	}

	/**
	 * Returns the size of the matrix as a vector.
	 * @see Matrix#getVectorItem(int)
	 */
	public int getVectorSize() {
		return sizeX * sizeY;
	}

	/**
	 * The matrix can be interpreted as a "vector" where the elements are
	 * aligned by rows, i.e.<br>
	 * <br>
	 * <tt>
	 *       Matrix [4][3]<br>
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
		return m[aIndex % sizeX][aIndex / sizeX];
	}

	/**
	 * @see Matrix#getVectorItem(int)
	 */
	public void setVectorItem(int aIndex, double aValue) {
		m[aIndex % sizeX][aIndex / sizeX] = aValue;
	}

	/**
	 * Multiplies two matrices and stores the result in dest matrix. If the dest
	 * matrix is of incorrect size it will be resized. The formula is:<br>
	 * <tt>dest = this * second<br>
	 * </tt>
	 */
	public void mMul(Matrix second, Matrix dest) {
		if (sizeX != second.sizeY) {
			throw new Error("Invalid argument");
		}
		dest.resize(second.sizeX, sizeY);
		double D;
		for (int i = sizeY - 1; i >= 0; i--) {
			for (int j = second.sizeX - 1; j >= 0; j--) {
				D = 0;
				for (int k = sizeX - 1; k >= 0; k--)
					D += m[k][i] * second.m[j][k];
				dest.m[j][i] = D;
			}
		}
	}

	/**
	 * Performs an element by element sum of two matrices of equal size and
	 * stores the result in dest matrix. If the dest matrix is of incorrect size
	 * it will be resized to the same size as the source matrix. The formula is:<br>
	 * <tt>dest[i][j] = this[i][j] + second[i][j]<br>
	 * </tt>
	 */
	public void mSum(Matrix second, Matrix dest) {
		if ((sizeX != second.sizeX) || (sizeY != second.sizeY)) {
			throw new Error("Invalid argument");
		}
		dest.resize(sizeX, sizeY);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				dest.m[i][j] = m[i][j] + second.m[i][j];
	}

	/**
	 * Performs an element by element subtraction of two matrices of equal size
	 * and stores the result in dest matrix. If the dest matrix is of incorrect
	 * size it will be resized to the same size as the source matrix. The
	 * formula is:<br>
	 * <tt>dest[i][j] = this[i][j] - second[i][j]<br>
	 * </tt>
	 */
	public void mSub(Matrix second, Matrix dest) {
		if ((sizeX != second.sizeX) || (sizeY != second.sizeY)) {
			throw new Error("Invalid argument");
		}
		dest.resize(sizeX, sizeY);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				dest.m[i][j] = m[i][j] - second.m[i][j];
	}

	/**
	 * Returns the dot product of the matrix. The formula is:<br>
	 * <tt>Result = Sum( m[i][j] )<br>
	 * </tt>
	 */
	public double dotProduct(Matrix second) {
		if ((sizeX != second.sizeX) || (sizeY != second.sizeY)) {
			throw new Error("Invalid argument");
		}
		double sum = 0;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				sum += m[i][j];
		return sum;
	}

	/**
	 * Performs an element by element multiplication of two matrices of equal
	 * size and stores the result in dest matrix. If the dest matrix is of
	 * incorrect size it will be resized to the same size as the source matrix.
	 * The formula is:<br>
	 * <tt>dest[i][j] = this[i][j] * second[i][j]<br>
	 * </tt>
	 */
	public void termMul(Matrix second, Matrix dest) {
		if ((sizeX != second.sizeX) || (sizeY != second.sizeY)) {
			throw new Error("Invalid argument");
		}
		dest.resize(sizeX, sizeY);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				dest.m[i][j] = m[i][j] * second.m[i][j];
	}

	/**
	 * Performs an element by element division of two matrices of equal size and
	 * stores the result in dest matrix. If the dest matrix is of incorrect size
	 * it will be resized to the same size as the source matrix. The formula is:<br>
	 * <tt>dest[i][j] = this[i][j] / second[i][j]<br>
	 * </tt> <b>Warning:</b><i>If there is an element that is zero, an
	 * exception will rise <code>java.lang.ArithmeticException</code>.</i>
	 */
	public void termDiv(Matrix second, Matrix dest) {
		if ((sizeX != second.sizeX) || (sizeY != second.sizeY)) {
			throw new Error("Invalid argument");
		}
		dest.resize(sizeX, sizeY);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				dest.m[i][j] = m[i][j] / second.m[i][j];
	}

	/**
	 * Multiplies all elements of the matrix with aValue. The formula is:<br>
	 * <tt>this[i][j] = aValue * this[i][j]<br>
	 * </tt>
	 */
	public void rMul(double aValue) {
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				m[i][j] *= aValue;
	}

	/**
	 * Retuns sum the of all elements of the matrix.
	 */
	public double sumAll() {
		double D = 0;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				D += m[i][j];
		return D;
	}

	/**
	 * Retuns the maximum value of all elements of the matrix.
	 */
	public double max() {
		if ((sizeX == 0) || (sizeY == 0))
			return 0;
		double D = m[0][0];
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				if (D < m[i][j])
					D = m[i][j];
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
				double tmp = Math.abs(m[i][j]);
				if (D < tmp)
					D = tmp;
			}
		return D;
	}
	
	/**
	 * Retuns the minimum value of all elements of the matrix.
	 */
	public double min() {
		if ((sizeX == 0) || (sizeY == 0))
			return 0;
		double D = m[0][0];
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				if (D > m[i][j])
					D = m[i][j];
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
	 * <tt>dest[i][j] = max( this[i][j] , second[i][j] )<br>
	 * </tt>
	 */
	public void mMax(Matrix second, Matrix dest) {
		if ((sizeX != second.sizeX) || (sizeY != second.sizeY)) {
			throw new Error("Invalid argument");
		}
		dest.resize(sizeX, sizeY);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				dest.m[i][j] = (m[i][j] > second.m[i][j] ? m[i][j]
						: second.m[i][j]);
	}

	/**
	 * Sets the elements of dest matrix to the minimum corresponding elements of
	 * this and second matrix. If the dest matrix is of incorrect size it will
	 * be resized. The formula is:<br>
	 * <tt>dest[i][j] = min( this[i][j] , second[i][j] )<br>
	 * </tt>
	 */
	public void mMin(Matrix second, Matrix dest) {
		if ((sizeX != second.sizeX) || (sizeY != second.sizeY)) {
			throw new Error("Invalid argument");
		}
		dest.resize(sizeX, sizeY);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				dest.m[i][j] = (m[i][j] < second.m[i][j] ? m[i][j]
						: second.m[i][j]);
	}

	/**
	 * Makes a transposed matirx of this matrix. If the dest matrix is of
	 * incorrect size it will be resized.
	 */
	public void transpose(Matrix dest) {
		dest.resize(sizeY, sizeX);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				dest.m[j][i] = m[i][j];
	}

	/**
	 * Normalizes the matrix so that sumAll() returns 1. If normalization is not
	 * possible, i.e. sumAll() returns 0, the Matrix.make0() is called. The
	 * formula is:<br>
	 * <tt>this[i][j] = this[i][j] / sum ( this[i][j] )<br>
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
	 * <tt>this[i][j] = (this[i][j] - min()) / (max() - min())<br>
	 * </tt>
	 */
	public void normalize2() {
		if ((sizeX == 0) || (sizeY == 0))
			return;
		double maxVal = m[0][0];
		double minVal = maxVal;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				double value = m[i][j]; 
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
					m[i][j] = (m[i][j] - minVal) / delta;
		}
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
	 */
	public void copyTo(Matrix dest) {
		dest.resize(sizeX, sizeY);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				dest.m[i][j] = m[i][j];
	}

	/**
	 * Makes the identity matrix. The formula is:<br>
	 * <tt>Result[i][j] = (i == j) ? 1 : 0<br>
	 * </tt>
	 */
	public void makeE() {
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				m[i][j] = (i == j) ? 1 : 0;
	}

	/**
	 * Makes a zero matrix. All elements are set to 0.
	 */
	public void make0() {
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				m[i][j] = 0;
	}

	/**
	 * Sets all elements of this matrix to aValue.
	 */
	public void makeR(double aValue) {
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				m[i][j] = aValue;
	}

	/**
	 * Multiplies all elements at column atX with D.
	 */
	public void mulX(int atX, double D) {
		for (int j = sizeY - 1; j >= 0; j--)
			m[atX][j] *= D;
	}

	/**
	 * Multiplies all elements at row atY with D.
	 */
	public void mulY(int atY, double D) {
		for (int i = sizeX - 1; i >= 0; i--)
			m[i][atY] *= D;
	}

	/**
	 * Sums the elements at column atX1 with the elements at column atX2 and
	 * stores the result in column atX1. The formula is:<br>
	 * <tt>this[atX1][j] = this[atX1][j] + this[atX2][j]<br>
	 * </tt>
	 */
	public void sumX(int atX1, int atX2) {
		for (int j = sizeY - 1; j >= 0; j--)
			m[atX1][j] += m[atX2][j];
	}

	/**
	 * Sums the elements at row atY1 with the elements at row atY2 and stores
	 * the result in row atY1. The formula is:<br>
	 * <tt>this[i][atX1] = this[i][atX1] + this[i][atX2]<br>
	 * </tt>
	 */
	public void sumY(int atY1, int atY2) {
		for (int i = sizeX - 1; i >= 0; i--)
			m[i][atY1] += m[i][atY2];
	}

	/**
	 * Exchanges the column atX1 with column atX2.
	 */
	public void exchangeX(int atX1, int atX2) {
		for (int j = sizeY - 1; j >= 0; j--) {
			double D = m[atX1][j];
			m[atX1][j] = m[atX2][j];
			m[atX2][j] = D;
		}
	}

	/**
	 * Exchanges the row atY1 with row atY2.
	 */
	public void exchangeY(int atY1, int atY2) {
		for (int i = sizeX - 1; i >= 0; i--) {
			double D = m[i][atY1];
			m[i][atY1] = m[i][atY2];
			m[i][atY2] = D;
		}
	}

	/**
	 * Private class used by Matrix.inverse()
	 */
	private class XchgRec {

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
		ArrayList xchg = new ArrayList();

		for (int i = 0; i < sizeX; i++) {
			double A = m[i][i];
			if (A == 0) {
				int indexI = 0;
				for (int j = i + 1; j < sizeX; j++)
					if (m[i][j] != 0) {
						indexI = j;
						exchangeX(i, j);
						xchg.add(new XchgRec(i, j));
						break;
					}
				if (indexI == 0) {
					make0();
					return false;
				}
				A = m[i][i];
			}

			for (int j = 0; j < sizeX; j++)
				if (i != j) {
					double B = m[j][i] / A;
					for (int k = 0; k < sizeX; k++)
						if (k != i) {
							if ((k < i) && (j < i))
								m[j][k] += B * m[i][k];
							else
								m[j][k] -= B * m[i][k];
						}
				}

			for (int j = 0; j < sizeX; j++)
				if (i != j) {
					if (i > j) {
						m[i][j] /= -A;
						m[j][i] /= -A;
					} else {
						m[i][j] /= A;
						m[j][i] /= A;
					}
				}
			m[i][i] = 1 / A;
		}

		for (int i = xchg.size() - 1; i >= 0; i--) {
			XchgRec x = (XchgRec)xchg.get(i);
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
		Matrix cpy = makeCopy();

		for (int i = 0; i < sizeX; i++) {
			if (cpy.m[i][i] == 0) {
				boolean err = true;
				for (int a = sizeY - 1; a > i; a--)
					if (cpy.m[i][a] != 0) {
						cpy.sumY(i, a);
						err = false;
					}
				if (err)
					return 0; // 0 -> the matrix has NO determinant
			}

			D = cpy.m[i][i];
			if (D != 1) {
				cpy.mulY(i, 1 / D);
				result /= D;
			}

			for (int a = 0; a < sizeX; a++)
				if (a != i) {
					D = m[a][i];
					if (D != 0) {
						cpy.mulY(a, -1 / D);
						result /= -D;
						cpy.sumY(a, i);
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
		res.A = this;
		res.B = second;
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
				dA = m[i][j] - res.AvgA;
				dB = second.m[i][j] - res.AvgB;
				res.SAA += dA * dA;
				res.SBB += dB * dB;
				res.SAB += dA * dB;
			}

		// Коефициент на корелация на Pearson
		res.PearsonR = res.SAB / Math.sqrt(res.SAA * res.SBB);
		return res;
	}

	public void toXML(Element dest) {
		for (int j = 0; j < sizeY; j++) {
			Element row = new Element("row");
			for (int i = 0; i < sizeX; i++)
				row.addContent(XMLHelper.makeAttrEl("item", Double.toString(getItem(i, j))));
			dest.addContent(row);
		}
	}
	
	public static Matrix fromXML(Element source) throws JDOMException {
		// Determine matrix size;
		List rows = source.getChildren("row");
		int cols = 0;
		for (int i = 0; i < rows.size(); i++)
			cols = Math.max(cols, ((Element)rows.get(i)).getChildren("item").size());
		if ((rows.size() <= 0) || (cols <= 0))
			throw new JDOMException("Invalid matrix");
			
		Matrix r = new Matrix(cols, rows.size());
		r.make0();
		for (int j = rows.size() - 1; j >= 0; j--) {
			List items = ((Element)rows.get(j)).getChildren("item");
			for (int i = items.size() - 1; i >= 0; i--) {
				Element item = (Element)items.get(i);
				String v = item.getAttributeValue("v");
				if ((v != null) && (!v.equals("")))
					try {
						r.setItem(i, j, Double.parseDouble(v));
					} catch (Exception e) {
						throw new JDOMException("Invalid matrix value ar row " + j + ", column " + i);
					}
			}
		}
		return r;
	}
	
	
	/**
	 * sqrt(a^2 + b^2) without under/overflow.
	 */
//	public static double hypot(double a, double b) {
//		double r;
//		if (Math.abs(a) > Math.abs(b)) {
//			r = b / a;
//			r = Math.abs(a) * Math.sqrt(1 + r * r);
//		} else if (b != 0) {
//			r = a / b;
//			r = Math.abs(b) * Math.sqrt(1 + r * r);
//		} else {
//			r = 0.0;
//		}
//		return r;
//	}

	// MY SVD translation from LAPACK's DGESVD
	
	public void svd(Matrix w, Matrix v) {
		@SuppressWarnings("unused")
		int i, its, j, jj, k, l = 0, nm = 0;
		@SuppressWarnings("unused")
		boolean flag;
		@SuppressWarnings("unused")
		double c, f, h, s, x, y, z;
		double anorm = 0., g = 0., scale = 0.;
//		if (sizeX < sizeY)
//			throw new IllegalArgumentException("m < n");
		// zliberror._assert(m>=n) ;
		double[] rv1 = new double[sizeY];
		w.resize(Math.min(getSizeX(), getSizeY()), 1);
		v.resize(getSizeY(), getSizeY());
		
		System.out.println("SVD beware results may not be sorted!");

		for (i = 0; i < sizeY; i++) {
			l = i + 1;
			rv1[i] = scale * g;
			g = s = scale = 0.;
			if (i < sizeX) {
				for (k = i; k < sizeX; k++)
					scale += Math.abs(this.m[k][i]);
				if (scale != 0.0) {
					for (k = i; k < sizeX; k++) {
						this.m[k][i] /= scale;
						s += this.m[k][i] * this.m[k][i];
					}
					f = this.m[i][i];
					g = -SIGN(Math.sqrt(s), f);
					h = f * g - s;
					this.m[i][i] = f - g;
					// if (i!=(n-1)) { // CHECK
					for (j = l; j < sizeY; j++) {
						for (s = 0, k = i; k < sizeX; k++)
							s += this.m[k][i] * this.m[k][j];
						f = s / h;
						for (k = i; k < sizeX; k++)
							this.m[k][j] += f * this.m[k][i];
					}
					// }
					for (k = i; k < sizeX; k++)
						this.m[k][i] *= scale;
				}
			}
			w.m[i][0] = scale * g;
			g = s = scale = 0.0;
			if (i < sizeX && i != sizeY - 1) { //
				for (k = l; k < sizeY; k++)
					scale += Math.abs(this.m[i][k]);
				if (scale != 0.) {
					for (k = l; k < sizeY; k++) { //
						this.m[i][k] /= scale;
						s += this.m[i][k] * this.m[i][k];
					}
					f = this.m[i][l];
					g = -SIGN(Math.sqrt(s), f);
					h = f * g - s;
					this.m[i][l] = f - g;
					for (k = l; k < sizeY; k++)
						rv1[k] = this.m[i][k] / h;
					if (i != sizeX - 1) { //
						for (j = l; j < sizeX; j++) { //
							for (s = 0, k = l; k < sizeY; k++)
								s += this.m[j][k] * this.m[i][k];
							for (k = l; k < sizeY; k++)
								this.m[j][k] += s * rv1[k];
						}
					}
					for (k = l; k < sizeY; k++)
						this.m[i][k] *= scale;
				}
			} // i<m && i!=n-1
			anorm = Math.max(anorm, (Math.abs(w.m[i][0]) + Math.abs(rv1[i])));
		} // i
	}	
	
	public void svd1(Matrix w, Matrix v) {
		int i, its, j, jj, k, l = 0, nm = 0;
		boolean flag;
		double c, f, h, s, x, y, z;
		double anorm = 0., g = 0., scale = 0.;
		if (sizeX < sizeY)
			throw new IllegalArgumentException("m < n");
		// zliberror._assert(m>=n) ;
		double[] rv1 = new double[sizeY];
		w.resize(Math.min(getSizeX(), getSizeY()), 1);
		v.resize(getSizeY(), getSizeY());

		System.out.println("SVD beware results may not be sorted!");

		for (i = 0; i < sizeY; i++) {
			l = i + 1;
			rv1[i] = scale * g;
			g = s = scale = 0.;
			if (i < sizeX) {
				for (k = i; k < sizeX; k++)
					scale += Math.abs(this.m[k][i]);
				if (scale != 0.0) {
					for (k = i; k < sizeX; k++) {
						this.m[k][i] /= scale;
						s += this.m[k][i] * this.m[k][i];
					}
					f = this.m[i][i];
					g = -SIGN(Math.sqrt(s), f);
					h = f * g - s;
					this.m[i][i] = f - g;
					// if (i!=(n-1)) { // CHECK
					for (j = l; j < sizeY; j++) {
						for (s = 0, k = i; k < sizeX; k++)
							s += this.m[k][i] * this.m[k][j];
						f = s / h;
						for (k = i; k < sizeX; k++)
							this.m[k][j] += f * this.m[k][i];
					}
					// }
					for (k = i; k < sizeX; k++)
						this.m[k][i] *= scale;
				}
			}
			w.m[i][0] = scale * g;
			g = s = scale = 0.0;
			if (i < sizeX && i != sizeY - 1) { //
				for (k = l; k < sizeY; k++)
					scale += Math.abs(this.m[i][k]);
				if (scale != 0.) {
					for (k = l; k < sizeY; k++) { //
						this.m[i][k] /= scale;
						s += this.m[i][k] * this.m[i][k];
					}
					f = this.m[i][l];
					g = -SIGN(Math.sqrt(s), f);
					h = f * g - s;
					this.m[i][l] = f - g;
					for (k = l; k < sizeY; k++)
						rv1[k] = this.m[i][k] / h;
					if (i != sizeX - 1) { //
						for (j = l; j < sizeX; j++) { //
							for (s = 0, k = l; k < sizeY; k++)
								s += this.m[j][k] * this.m[i][k];
							for (k = l; k < sizeY; k++)
								this.m[j][k] += s * rv1[k];
						}
					}
					for (k = l; k < sizeY; k++)
						this.m[i][k] *= scale;
				}
			} // i<m && i!=n-1
			anorm = Math.max(anorm, (Math.abs(w.m[i][0]) + Math.abs(rv1[i])));
		} // i
		
		
		for (i = sizeY - 1; i >= 0; --i) {
			if (i < sizeY - 1) { //
				if (g != 0.) {
					for (j = l; j < sizeY; j++)
						v.m[j][i] = (this.m[i][j] / this.m[i][l]) / g;
					for (j = l; j < sizeY; j++) {
						for (s = 0, k = l; k < sizeY; k++)
							s += this.m[i][k] * v.m[k][j];
						for (k = l; k < sizeY; k++)
							v.m[k][j] += s * v.m[k][i];
					}
				}
				for (j = l; j < sizeY; j++)
					//
					v.m[i][j] = v.m[j][i] = 0.0;
			}
			v.m[i][i] = 1.0;
			g = rv1[i];
			l = i;
		}
		// for (i=IMIN(m,n);i>=1;i--) { // !
		// for (i = n-1; i>=0; --i) {
		for (i = Math.min(sizeX - 1, sizeY - 1); i >= 0; --i) {
			l = i + 1;
			g = w.m[i][0];
			if (i < sizeY - 1) //
				for (j = l; j < sizeY; j++)
					//
					this.m[i][j] = 0.0;
			if (g != 0.) {
				g = 1. / g;
				if (i != sizeY - 1) {
					for (j = l; j < sizeY; j++) {
						for (s = 0, k = l; k < sizeX; k++)
							s += this.m[k][i] * this.m[k][j];
						f = (s / this.m[i][i]) * g;
						for (k = i; k < sizeX; k++)
							this.m[k][j] += f * this.m[k][i];
					}
				}
				for (j = i; j < sizeX; j++)
					this.m[j][i] *= g;
			} else {
				for (j = i; j < sizeX; j++)
					this.m[j][i] = 0.0;
			}
			this.m[i][i] += 1.0;
		}
		for (k = sizeY - 1; k >= 0; --k) {
			for (its = 1; its <= 30; ++its) {
				flag = true;
				for (l = k; l >= 0; --l) {
					nm = l - 1;
					if ((Math.abs(rv1[l]) + anorm) == anorm) {
						flag = false;
						break;
					}
					if ((Math.abs(w.m[nm][0]) + anorm) == anorm)
						break;
				}
				if (flag) {
					c = 0.0;
					s = 1.0;
					for (i = l; i <= k; i++) { //
						f = s * rv1[i];
						rv1[i] = c * rv1[i];
						if ((Math.abs(f) + anorm) == anorm)
							break;
						g = w.m[i][0];
						h = hypot(f, g);
						w.m[i][0] = h;
						h = 1.0 / h;
						c = g * h;
						s = -f * h;
						for (j = 0; j < sizeX; j++) {
							y = this.m[j][nm];
							z = this.m[j][i];
							this.m[j][nm] = y * c + z * s;
							this.m[j][i] = z * c - y * s;
						}
					}
				} // flag
				z = w.m[k][0];
				if (l == k) {
					if (z < 0.) {
						w.m[k][0] = -z;
						for (j = 0; j < sizeY; j++)
							v.m[j][k] = -v.m[j][k];
					}
					break;
				} // l==k
				if (its >= 50)
					throw new ArithmeticException("no svd convergence in 50 iterations");
				// zliberror._assert(its<50, "no svd convergence in 50
				// iterations");
				x = w.m[l][0];
				nm = k - 1;
				y = w.m[nm][0];
				g = rv1[nm];
				h = rv1[k];
				f = ((y - z) * (y + z) + (g - h) * (g + h)) / (2 * h * y);
				g = hypot(f, 1.0);
				f = ((x - z) * (x + z) + h * ((y / (f + SIGN(g, f))) - h)) / x;
				c = s = 1.0;
				for (j = l; j <= nm; j++) {
					i = j + 1;
					g = rv1[i];
					y = w.m[i][0];
					h = s * g;
					g = c * g;
					z = hypot(f, h);
					rv1[j] = z;
					c = f / z;
					s = h / z;
					f = x * c + g * s;
					g = g * c - x * s;
					h = y * s;
					y *= c;
					for (jj = 0; jj < sizeY; jj++) {
						x = v.m[jj][j];
						z = v.m[jj][i];
						v.m[jj][j] = x * c + z * s;
						v.m[jj][i] = z * c - x * s;
					}
					z = hypot(f, h);
					w.m[j][0] = z;
					if (z != 0.0) {
						z = 1.0 / z;
						c = f * z;
						s = h * z;
					}
					f = c * g + s * y;
					x = c * y - s * g;
					for (jj = 0; jj < sizeX; ++jj) {
						y = this.m[jj][j];
						z = this.m[jj][i];
						this.m[jj][j] = y * c + z * s;
						this.m[jj][i] = z * c - y * s;
					}
				} // j<nm
				rv1[l] = 0.0;
				rv1[k] = f;
				w.m[k][0] = x;
			} // its
		} // k
		// free rv1
	} // svd

	public static final double SIGN(double a, double b) {
		return ((b) >= 0.0 ? Math.abs(a) : -Math.abs(a));
	}
	
	/** 
	 * sqrt(a^2 + b^2) without under/overflow. 
	 */
	public static double hypot(double a, double b) {
		double r;
		double absA = Math.abs(a);
		double absB = Math.abs(b);
		if (absA > absB) {
			r = b / a;
			r = absA * Math.sqrt(1 + r * r);
		} else if (b != 0) {
			r = a / b;
			r = absB * Math.sqrt(1 + r * r);
		} else {
			r = 0.0;
		}
		return r;
	}
	
	public void printM(String title) {
		System.out.println(title);
		System.out.println(toString());
	}

	
	public void lq(Matrix q, Matrix tau) {
		q.resize(getSizeY(), getSizeY());
		lqDecomposition(tau);
		lqDecomositionGetQ(tau, q);
		lqDecomositionGetL(this);
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
				xnorm = hypot(xnorm, getItem(i, atIndex));
			if (xnorm == 0.0) { 
				tau.setItem(atIndex, 0, 0.0);
				continue;
			}
				
			double alpha = getItem(atIndex, atIndex);
			//double beta = -SIGN(Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2)), alpha);
			//double beta = Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2));
			double beta = hypot(alpha, xnorm);
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
	
	public void lqDecomositionGetL(Matrix l) {
		l.resize(getSizeX(), getSizeY());
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--)
				l.setItem(i, j, i > j ? 0.0 : getItem(i, j));
	}

	public void lqDecomositionGetQ(Matrix tau, Matrix q) {
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
				xnorm = hypot(xnorm, getItem(atIndex, j));
			if (xnorm == 0.0) { 
				tau.setItem(atIndex, 0, 0.0);
				continue;
			}
				
			double alpha = getItem(atIndex, atIndex);
			//double beta = -SIGN(Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2)), alpha);
			//double beta = Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2));
			double beta = hypot(alpha, xnorm);
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
	
	public void luDecomposition() {
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
				// ????
			}
	}

	public static void main(String[] args) throws Exception {
		BufferedReader fin = new BufferedReader(new FileReader(
				MatrixTest.class.getResource(
					"SVD-A.txt").getFile()));
		StringTokenizer stt = new StringTokenizer(fin.readLine());
		Matrix a = new Matrix(Integer.parseInt(stt.nextToken()), Integer.parseInt(stt.nextToken()));
		a.load(fin);
		fin.close();
		Matrix at = new Matrix();
		
		a.transpose(at);
		Matrix b = a.makeCopy();
//		Matrix bt = at.makeCopy();
		
		Matrix tmp = new Matrix(50,1);

		Matrix u = new Matrix();
		Matrix v = new Matrix(a.getSizeX(), a.getSizeX());
		Matrix s = new Matrix(a.getSizeX(), a.getSizeY());

//		Matrix ut = new Matrix();
//		Matrix vt = new Matrix();
//		Matrix st = new Matrix();

		
		a.lqDecomposition(tmp);
		a.lqDecomositionGetQ(tmp, u);
		a.lqDecomositionGetL(v);
		
//		a.mysvd(u, v, s);
//		at.mysvd(ut, vt, st);

		u.printM("U2");
		v.printM("V2");
		v.mMul(u, s);
		s.printM("S2");
		s.mSub(b, b);
		b.printM("A");
				
		
		
//		u.printM("U !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//		v.printM("V");
//		s.printM("S");
		
//		Matrix checkA = new Matrix();
//		Matrix checkAt = new Matrix();

//		ut.printM("UT !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//		vt.printM("VT");
//		st.printM("ST");
		
//		u.mMul(s, tmp);
//		tmp.mMul(v, checkA);
//		checkA.mSub(b, a);
//		a.printM("Diff A");

//		ut.mMul(st, tmp);
//		tmp.mMul(vt, checkAt);
//		checkAt.mSub(bt, at);
//		at.printM("Diff AT");
		
//		ut.termMul(ut, ut);
//		vt.termMul(vt, vt);
//		u.termMul(u, u);
//		v.termMul(v, v);
//				
//		vt.transpose(tmp);
//		tmp.mSub(u, tmp);
//		tmp.printM("Diff U");
//
//		ut.transpose(tmp);
//		tmp.mSub(v, tmp);
//		tmp.printM("Diff V");
//
//		st.transpose(tmp);
//		tmp.mSub(s, tmp);
//		tmp.printM("Diff S");
	}
}
