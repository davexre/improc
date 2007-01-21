package com.slavi.matrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

	/**
	 * Returns a multiline string containing all elements of the matrix.
	 */
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int j = 0; j < sizeY; j++) {
			for (int i = 0; i < sizeX; i++) {
				if (i != 0)
					result.append(" ");
				result.append(String.format(Locale.US, "%1$5.10f",new Object[] { new Double(m[i][j]) } ));
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
			throw new Error("Comparing matrices of different size");
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
				res.SAA += Math.pow(dA, 2);
				res.SBB += Math.pow(dB, 2);
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
		int i, its, j, jj, k, l = 0, nm = 0;
		boolean flag;
		double c, f, h, s, x, y, z;
		double anorm = 0., g = 0., scale = 0.;
//		if (sizeX < sizeY)
//			throw new Error("m < n");
		// zliberror._assert(m>=n) ;
		double[] rv1 = new double[sizeY];

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
					throw new Error("no svd convergence in 50 iterations");
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

	static final double SIGN(double a, double b) {
		return ((b) >= 0.0 ? Math.abs(a) : -Math.abs(a));
	}
	
	/** 
	 * sqrt(a^2 + b^2) without under/overflow. 
	 */
	private static double hypot(double a, double b) {
		double r;
		double absA = Math.abs(a);
		double absB = Math.abs(b);
		if (absA > absB) {
			r = b / a;
			r = absA * Math.sqrt(1 + Math.pow(r, 2.0));
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
			double beta = Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2));
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
			// DLARF Right
			for (int j = getSizeY() - 1; j > atIndex; j--) {
				double sum = 0.0;
				for (int i = getSizeX() - 1; i >= atIndex; i--) 
					sum += getItem(i, j) * getItem(i, atIndex);
				for (int i = getSizeX() - 1; i >= atIndex; i--) 
					setItem(i, j, getItem(i, j) - tmp_tau * sum * getItem(i, atIndex));
			}
			// End DLARF Right
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
			throw new Error("Invalid parameter");
		
		q.resize(getSizeX(), getSizeX());
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeX() - 1; j >= 0; j--)
				q.setItem(i, j, (i >= j) && (j < getSizeY()) ? getItem(i, j) : i == j ? 1.0 : 0.0);

		for (int atIndex = getSizeY() - 1; atIndex >= 0; atIndex--) {
			if (atIndex < q.getSizeX()) {
				double tmp_tau = tau.getItem(atIndex, 0);
				q.setItem(atIndex, atIndex, 1.0);
				// DLARF Right
				for (int j = q.getSizeY() - 1; j > atIndex; j--) {
					double sum = 0.0;
					for (int i = q.getSizeX() - 1; i >= atIndex; i--) 
						sum += q.getItem(i, j) * q.getItem(i, atIndex);
					for (int i = q.getSizeX() - 1; i >= atIndex; i--) 
						q.setItem(i, j, q.getItem(i, j) - tmp_tau * sum * q.getItem(i, atIndex));
				}
				// End DLARF Right

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
			double beta = Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2));
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
			// DLARF Left
			for (int i = getSizeX() - 1; i > atIndex; i--) {
				double sum = 0.0;
				for (int j = getSizeY() - 1; j >= atIndex; j--) 
					sum += getItem(i, j) * getItem(atIndex, j);
				for (int j = getSizeY() - 1; j >= atIndex; j--) 
					setItem(i, j, getItem(i, j) - tmp_tau * sum * getItem(atIndex, j));
			}
			// End DLARF Left
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
		if ((tau.getSizeX() != getSizeX()) || (tau.getSizeY() != 1))
			throw new Error("Invalid parameter");
		
		q.resize(getSizeY(), getSizeY());
		for (int i = getSizeY() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--)
				q.setItem(i, j, (i <= j) && (i < getSizeX()) ? getItem(i, j) : i == j ? 1.0 : 0.0);

		for (int atIndex = getSizeX() - 1; atIndex >= 0; atIndex--) {
			if (atIndex < q.getSizeY()) {
				double tmp_tau = tau.getItem(atIndex, 0);
				q.setItem(atIndex, atIndex, 1.0);
				// DLARF Left
				for (int i = q.getSizeX() - 1; i > atIndex; i--) {
					double sum = 0.0;
					for (int j = q.getSizeY() - 1; j >= atIndex; j--) 
						sum += q.getItem(i, j) * q.getItem(atIndex, j);
					for (int j = q.getSizeY() - 1; j >= atIndex; j--) 
						q.setItem(i, j, q.getItem(i, j) - tmp_tau * sum * q.getItem(atIndex, j));
				}
				// End DLARF Left

				for (int j = q.getSizeY() - 1; j > atIndex; j--)
					q.setItem(atIndex, j, q.getItem(atIndex, j) * (-tmp_tau));
				q.setItem(atIndex, atIndex, 1.0 - tmp_tau);
				for (int j = atIndex - 1; j >= 0; j--)
					q.setItem(atIndex, j, 0.0);				
			}
		}
	}
	
	public static void svdDBDSQR(Matrix U, Matrix s, Matrix V, Matrix E, Matrix work) {
		// DBDSQR
		// Perform bidiagonal QR iteration, computing left singular vectors 
		// of R in WORK(IU) and computing right singular vectors of R in VT
		
		// WORK(IU) = Q 
		// WORK(IE) = E 
		// WORK(IWORK) = temporary
		// S, V
		
		// DBDSQR:258 Compute approximate maximum, minimum singular values
//		double smax = 0.0;
//		for (int i = s.getSizeY() - 1; i >= 0; i--)
//			smax = Math.max(smax, Math.abs(s.getItem(i, i)));
//		for (int i = s.getSizeX() - 2; i >= 0; i--)
//			smax = Math.max(smax, Math.abs(E.getItem(i, 0)));
		
		int atIndex = Math.min(U.getSizeX(), V.getSizeY()) - 1;
		int maxit = 6 * atIndex * atIndex;
		int iter = maxit;
		double TOL = 1.0958067E-014; // ?????
		double threshold = 2.59371421E-015;
		double EPS = 1.11022302E-016;
		
		int old_atIndex = -1;
		int old_ll = -1;
		double SMINL = 0.0;
		
		work.resize(V.getSizeX(), 4);
		int idir = 0;
		
		while ((atIndex >= 0) && (iter >= 0)) {
 			System.out.println("Iteration = " + (maxit - iter) + " atIndex = " + atIndex);
			s.printM("S=");
			E.printM("E=");
			V.printM("VT=");
			U.printM("U=");
			
			// DBDSQR:316 Find diagonal block of matrix to work on
			double smax = Math.abs(s.getItem(atIndex, atIndex));
			double smin = smax;
			int ll = atIndex - 1;
			boolean found_it = false; 
			while (ll >= 0) {
				double abss = Math.abs(s.getItem(ll, ll));
				double abse = Math.abs(E.getItem(ll, 0));
				if (abse <= threshold) {
					E.setItem(ll, 0, 0.0);
					// Matrix splits since E(LL) = 0
					if (ll == atIndex - 1) {
						// Convergence of bottom singular value, return to top of loop
						atIndex--;
						found_it = true;
						break;
					}
				}
				smin = Math.min(smin, abss);
				smax = Math.max(smax, Math.max(abss, abse));
				ll--;
			}
			if (found_it)
				continue;
			ll++;
			// E(LL) through E(M-1) are nonzero, E(LL-1) is zero
			if (ll == atIndex - 1) {
				// DBDSQR:354 2 by 2 block, handle separately
				double SINR, COSR, SINL, COSL;
				
				// DLASV2
				double F = s.getItem(atIndex - 1, atIndex - 1);
				double G = E.getItem(atIndex - 1, 0);
				double H = s.getItem(atIndex, atIndex);
				double FT = F;
				double GT = G;
				double HT = H;
				
				System.out.println("F=" + FT + " G=" + GT + " H=" + HT);
				
				double FA = Math.abs(FT);
				double HA = Math.abs(HT);
				double GA = Math.abs(GT);
				
				int pmax = 1;
				boolean swap = HA > FA;
				
				if (swap) {
					pmax = 3;
					double tmp = FT;
					FT = HT;
					HT = tmp;
					tmp = FA;
					FA = HA;
					HA = tmp;
				}
				double CLT = 0, CRT = 0, SLT = 0, SRT = 0;
				if (GA == 0.0) {
					// DLASV:132 Diagonal matrix
					s.setItem(atIndex, atIndex, HA);			// SSMIN
					s.setItem(atIndex - 1, atIndex - 1, FA);	// SSMAX
					CRT = CLT = 1.0;
					SRT = SLT = 0.0;
				} else {
					boolean gasmal = true;
					if (GA > FA) {
						pmax = 2;
						if ((FA / GA) < EPS) {  
							// DLASV2:146 Case of very large GA
							gasmal = false;
							s.setItem(atIndex, atIndex, 
									HA > 1.0 ? FA / (GA / HA) : (FA / GA) * HA);	// SSMIN
							s.setItem(atIndex - 1, atIndex - 1, GA);				// SSMAX
							SRT = CLT = 1.0;
							SLT = HT / GT;
							CRT = FT / GT;								
						}
					}
					if (gasmal) {
						// DLASV2:163 Normal case
						double tmpD = FA - HA;
						double L;
						if (tmpD == FA)
							// DLASV2:168 Copes with infinite F or H
							L = 1.0;
						else
							L = tmpD / FA;  
						double M = GT / FT;
						double T = 2.0 - L;
						double MM = M * M;
						double TT = T * T;
						double _S = Math.sqrt(TT + MM);
						double _R = L == 0.0 ? Math.abs(M) : Math.sqrt(L * L + MM);
						double A = (_S + _R) * 0.5;
						s.setItem(atIndex, atIndex, HA / A);			// SSMIN
						s.setItem(atIndex - 1, atIndex - 1, FA * A);	// SSMAX
						if (MM == 0.0) {
							// DLASV:207 Note that M is very tiny
							if (L == 0.0)
								T = SIGN(2.0, FT) * SIGN(1.0, GT);
							else
								T = GT / SIGN(tmpD, FT) + M / T;
						} else
							T = (M / (_S + T) + M / (_R + L)) * (1.0 + A);
						L = Math.sqrt(T * T + 4.0);
						CRT = 2.0 / L;
						SRT = T / L;
						CLT = (CRT + SRT * M) / A;
						SLT = (HT / FT ) * SRT / A;
					}
				}
				if (swap) {
					COSL = SRT;
					SINL = CRT;
					COSR = SLT;
					SINR = CLT;
				} else {
					COSL = CLT;
					SINL = SLT;
					COSR = CRT;
					SINR = SRT;
				};
				// DLASV2:236 Correct signs of SSMAX and SSMIN
				double tsign = 0;
				if (pmax == 1)
					tsign = SIGN(1.0, COSR) * SIGN(1.0, COSL) * SIGN(1.0, F);
				if (pmax == 2)
					tsign = SIGN(1.0, SINR) * SIGN(1.0, COSL) * SIGN(1.0, G);
				if (pmax == 3)
					tsign = SIGN(1.0, SINR) * SIGN(1.0, SINL) * SIGN(1.0, H);
				s.setItem(atIndex - 1, atIndex - 1, SIGN(s.getItem(atIndex - 1, atIndex - 1), tsign));	// SSMAX
				s.setItem(atIndex, atIndex, SIGN(s.getItem(atIndex, atIndex), 
						tsign * SIGN(1.0, F) * SIGN(1.0, H)));	// SSMIN
				// End DLASV2
				E.setItem(atIndex - 1, 0, 0.0);
				s.printM("S after DLASV2=");
				
				// DBDSQR:362 Compute singular vectors
				for (int i = V.getSizeX() - 1; i >= 0; i--) {
					double tmp =  COSR * V.getItem(i, atIndex - 1) + SINR * V.getItem(i, atIndex);
					V.setItem(i, atIndex, COSR * V.getItem(i, atIndex) - SINR * V.getItem(i, atIndex - 1));
					V.setItem(i, atIndex - 1, tmp);
				}
				for (int j = U.getSizeY() - 1; j >= 0; j--) {
					double tmp =  COSL * U.getItem(atIndex - 1, j) + SINL * U.getItem(atIndex, j);
					U.setItem(atIndex, j, COSL * U.getItem(atIndex, j) - SINL * U.getItem(atIndex - 1, j));
					U.setItem(atIndex - 1, j, tmp);
				}
				atIndex -= 2;
				continue;
			}
			
			// DBDSQR:376 If working on new submatrix, choose shift direction (from larger end diagonal element towards smaller)
			if ((ll > old_atIndex) || (atIndex < old_ll)) {
				if (Math.abs(s.getItem(ll, ll)) >= Math.abs(s.getItem(atIndex, atIndex)))
					// DBDSQR:382 Chase bulge from top (big end) to bottom (small end)
					idir = 1;
				else
					// DBDSQR:387 Chase bulge from bottom (big end) to top (small end)
					idir = 2;					
			}
			System.out.println("IDIR=" + idir);
			// DBDSQR:393 Apply convergence tests
			if (idir == 1) {
				// DBDSQR:397 Run convergence test in forward direction. First apply standard test to bottom of matrix
				if ((Math.abs(E.getItem(atIndex - 1, 0)) <= Math.abs(TOL * s.getItem(atIndex, atIndex))) ||
					((TOL < 0.0) && (Math.abs(E.getItem(atIndex - 1, 0)) < threshold ))) {
					E.setItem(atIndex - 1, 0, 0.0);
					continue;
				}
				if (TOL >= 0.0) {
					// DBDSQR:408 If relative accuracy desired, apply convergence criterion forward
					double MU = Math.abs(s.getItem(ll, ll));
					SMINL = MU;
					boolean found = false;
					for (int lll = ll; lll < atIndex; lll++) {
						if (Math.abs(E.getItem(lll, 0)) <= (TOL * MU)) {
							E.setItem(lll, 0, 0.0);
							found = true;
							break;
						}
						MU = Math.abs(s.getItem(lll + 1, lll + 1)) * (MU / (MU + Math.abs(E.getItem(lll, 0))));
						SMINL = Math.min(SMINL, MU);
					}
					if (found)
						continue;					
				}
			} else {
				// DBDSQR:426 Run convergence test in backward direction First apply standard test to top of matrix
				if ((Math.abs(E.getItem(ll, 0)) <= Math.abs(TOL * s.getItem(ll, ll))) || 
					((TOL < 0.0) && (Math.abs(E.getItem(ll, 0)) <= threshold))) {   
					E.setItem(ll, ll, 0.0);
					continue;
				}
				if (TOL > 0.0) {
					// DBDSQR:437 If relative accuracy desired, apply convergence criterion backward
					double MU = Math.abs(s.getItem(atIndex, atIndex));
					SMINL = MU;
					boolean found = false;
					for (int lll = atIndex - 1; lll >= ll; lll--) {
						if (Math.abs(E.getItem(lll, 0)) <= (TOL * MU)) {
							E.setItem(lll, 0, 0.0);
							found = true;
							break;
						}
						MU = Math.abs(s.getItem(lll, lll)) * (MU / (MU + Math.abs(E.getItem(lll, 0))));
						SMINL = Math.min(SMINL, MU);
					}
					if (found)
						continue;					
				}
			}
			old_ll = ll;
			old_atIndex = atIndex;
			
			double shift = 0.0;
			s.printM("mid S=");
			System.out.println("SMINL=" + SMINL);
			System.out.println("N    =" + V.getSizeX());
			System.out.println("SAMX =" + smax);
			System.out.println("Form1=" + (V.getSizeX() * TOL * (SMINL / smax)));
			System.out.println("Form2=" + (Math.max(EPS, TOL * 0.01)));
			
			// DBDQR:456 Compute shift.  First, test if shifting would ruin relative accuracy, and if so set the shift to zero.
			if ((TOL >= 0.0) && (V.getSizeX() * TOL * (SMINL / smax) <= Math.max(EPS, TOL * 0.01))) { 
				// DBDSQR:462 Use a zero shift to avoid loss of relative accuracy
				shift = 0.0;
			} else {
				// DBDSQR:467 Compute the shift from 2-by-2 block at end of matrix
				double sll, FA, GA, HA;				
				if (idir == 1) {
					sll = Math.abs(s.getItem(ll, ll));
					FA = Math.abs(s.getItem(atIndex - 1, atIndex - 1));
					GA = Math.abs(E.getItem(atIndex - 1, 0));
					HA = Math.abs(s.getItem(atIndex, atIndex));
				} else {
					sll = Math.abs(s.getItem(atIndex, atIndex));
					FA = Math.abs(s.getItem(ll, ll));
					GA = Math.abs(E.getItem(ll, 0));
					HA = Math.abs(s.getItem(ll + 1, ll + 1));
				}
				
				// DLAS2:15 computes the singular values of the 2-by-2 matrix
				// [  F   G  ]
				// [  0   H  ]
				if (FA < HA) {
					// FA => FHMX; HA => FHMN
					double tmp = FA;
					FA = HA;
					HA = tmp;
				}
				if (HA == 0.0) {
					shift = 0.0;
				} else {
					if (GA < FA) {
						double AS = 1.0 + HA / FA;
						double AT = (FA - HA) / FA;
						double AU = Math.pow((GA / FA), 2.0);
						double C = 2.0 / (Math.sqrt(AS * AS + AU) + Math.sqrt(AT * AT + AU));
						shift = HA * C;
					} else {
						double AU = FA / GA;
						if (AU == 0.0) {
							// DLAS2:101 Avoid possible harmful underflow if exponent range
							// asymmetric (true SSMIN may not underflow even if AU underflows)
							shift = (FA + HA) / GA;
						} else {
							double AS = 1.0 + HA / FA;
							double AT = (FA - HA) / FA;
							double C = 1.0 / (Math.sqrt(1.0 + Math.pow(AS * AU, 2.0)) + Math.sqrt(1.0 + Math.pow(AT * AU, 2.0)));
							shift = 2.0 * (HA * C * AU);
						}
					}
				}
				// End of DLAS2
				
				// DBDSQR:477 Test if shift negligible, and if so set to zero
				if ((sll > 0.0) && (Math.pow(shift / sll, 2.0) < EPS))
					shift = 0.0;
			}
			
			// DBDSQR:485 Increment iteration count
			iter -= atIndex - ll;
			// DBDSQR:489 If SHIFT = 0, do simplified QR iteration
			System.out.println("SHIFT=" + shift);
			
			if (shift == 0.0) {
				if (idir == 1) {
					// DBDSQR:570 Use nonzero shift
					if (idir == 1) {
						System.out.println("use path 01");
						// DBDSQR:494 Chase bulge from top to bottom
						// Save cosines and sines for later singular vector updates
						double CS = 1.0;
						double oldCS = 1.0;
						double oldSN = 0.0;
						double SN;

						for (int lll = ll; lll < atIndex; lll++) {
							// DLARTG
							double F = s.getItem(lll, lll) * CS;
							double G = E.getItem(lll, 0);
							double r = Math.sqrt(Math.pow(F, 2.0) + Math.pow(G, 2.0)); // hypot(F, G);
							CS = F / r;
							SN = G / r;
							if ((CS < 0.0) && (Math.abs(F) > Math.abs(G))) {
								CS = -CS;
								SN = -SN;
								r = -r;
							}
							//End DLARTG
							if (lll > ll)
								E.setItem(lll - 1, 0, oldSN * r);
							
							F = oldCS * r;
							G = s.getItem(lll + 1, lll + 1) * SN;
							// DLARTG
							r = hypot(F, G);
							oldCS = F / r;
							oldSN = G / r;
							if ((oldCS < 0.0) && (Math.abs(F) > Math.abs(G))) {
								oldCS = -oldCS;
								oldSN = -oldSN;
								r = -r;
							}
							s.setItem(lll, lll, r);
							// End DLARTG

							work.setItem(lll, 0, CS);
							work.setItem(lll, 1, SN);
							work.setItem(lll, 2, oldCS);
							work.setItem(lll, 3, oldSN);
						}
						double H = s.getItem(atIndex, atIndex) * CS;
						s.setItem(atIndex, atIndex, H * oldCS);
						E.setItem(atIndex - 1, 0, H * oldSN);
						// DBDSQR:513 Update singular vectors
						for (int lll = ll; lll < atIndex; lll++) {
							CS = work.getItem(lll, 0);
							SN = work.getItem(lll, 1);
							oldCS = work.getItem(lll, 2);
							oldSN = work.getItem(lll, 3);
							
							if ((CS != 1.0) || (SN != 0.0)) 
								for (int indx = 0; indx < V.getSizeX(); indx++) {
									double tmp1 = V.getItem(indx, lll + 1);
									double tmp2 = V.getItem(indx, lll);
									V.setItem(indx, lll + 1, CS * tmp1 - SN * tmp2);
									V.setItem(indx, lll,     SN * tmp1 + CS * tmp2);
								}
							if ((oldCS != 1.0) || (oldSN != 0.0)) 
								for (int indy = 0; indy < U.getSizeY(); indy++) {
									double tmp1 = U.getItem(lll + 1, indy);
									double tmp2 = U.getItem(lll, indy);
									U.setItem(lll + 1, indy, oldCS * tmp1 - oldSN * tmp2);
									U.setItem(lll, indy,     oldSN * tmp1 + oldCS * tmp2);
								}
						}
						// DBDSQR:525 Test convergence
						if (Math.abs(E.getItem(atIndex - 1, 0)) < threshold)
							E.setItem(atIndex - 1, 0, 0.0);
					}
				} else {
					System.out.println("use path 02");
					// DBDSQR:532 Chase bulge from bottom to top
					// Save cosines and sines for later singular vector updates
					double CS = 1.0;
					double oldCS = 1.0;
					double oldSN = 0.0;
					double SN;

					for (int lll = atIndex; lll > ll; lll--) {
						double F = s.getItem(lll, lll) * CS;
						double G = E.getItem(lll - 1, 0);
						// DLARTG
						double r = Math.sqrt(Math.pow(F, 2.0) + Math.pow(G, 2.0)); // TODO: hypot(F, G);
						CS = F / r;
						SN = G / r;
						if ((CS < 0.0) && (Math.abs(F) > Math.abs(G))) {
							CS = -CS;
							SN = -SN;
							r = -r;
						}
						//End DLARTG
						if (lll < atIndex)
							E.setItem(lll, 0, oldSN * r);
						
						F = oldCS * r;
						G = s.getItem(lll - 1, lll - 1) * SN;
						// DLARTG
						r = Math.sqrt(Math.pow(F, 2.0) + Math.pow(G, 2.0)); // TODO: hypot(F, G);
						oldCS = F / r;
						oldSN = G / r;
						if ((oldCS < 0.0) && (Math.abs(F) > Math.abs(G))) {
							oldCS = -oldCS;
							oldSN = -oldSN;
							r = -r;
						}
						// End DLARTG
						s.setItem(lll, lll, r);

						work.setItem(lll, 0, CS);
						work.setItem(lll, 1, -SN);
						work.setItem(lll, 2, oldCS);
						work.setItem(lll, 3, -oldSN);
					}
					double H = s.getItem(ll, ll) * CS;
					s.setItem(ll, ll, H * oldCS);
					E.setItem(ll, 0, H * oldSN);
					// DBDSQR:551 Update singular vectors
					for (int lll = ll; lll < atIndex; lll++) {
						CS = work.getItem(lll, 0);
						SN = work.getItem(lll, 1);
						oldCS = work.getItem(lll, 2);
						oldSN = work.getItem(lll, 3);
						
						if ((CS != 1.0) || (SN != 0.0)) 
							for (int indx = V.getSizeX() - 1; indx >= 0; indx--) {
								double tmp1 = V.getItem(indx, lll + 1);
								double tmp2 = V.getItem(indx, lll);
								V.setItem(indx, lll + 1, CS * tmp1 - SN * tmp2);
								V.setItem(indx, lll,     SN * tmp1 + CS * tmp2);
							}
						if ((oldCS != 1.0) || (oldSN != 0.0)) 
							for (int indy = U.getSizeY() - 1; indy >= 0; indy--) {
								double tmp1 = U.getItem(lll + 1, indy);
								double tmp2 = U.getItem(lll, indy);
								U.setItem(lll + 1, indy, oldCS * tmp1 - oldSN * tmp2);
								U.setItem(lll, indy,     oldSN * tmp1 + oldCS * tmp2);
							}
					}
					// DBDSQR:563 Test convergence
					if (Math.abs(E.getItem(ll, 0)) < threshold)
						E.setItem(ll, 0, 0.0);
				}
			} else {
				// DBDSQR:570 Use nonzero shift
				if (idir == 1) {
					System.out.println("use path 11");
					// DBDSQR:574 Chase bulge from top to bottom
					// Save cosines and sines for later singular vector updates
					double F = (Math.abs(s.getItem(ll, ll)) - shift) *
						(SIGN(1.0, s.getItem(ll, ll)) + shift / s.getItem(ll, ll));
					double G = E.getItem(ll, 0);
					
					double CS = 1.0;
					double oldCS = 1.0;
					double oldSN = 0.0;
					double SN;

					for (int lll = ll; lll < atIndex; lll++) {
						// DLARTG
						double r = Math.sqrt(Math.pow(F, 2.0) + Math.pow(G, 2.0)); // hypot(F, G);
						CS = F / r;
						SN = G / r;
						if ((CS < 0.0) && (Math.abs(F) > Math.abs(G))) {
							CS = -CS;
							SN = -SN;
							r = -r;
						}
						//End DLARTG
						if (lll > ll)
							E.setItem(lll - 1, 0, r);
						
						F = CS * s.getItem(lll, lll) + SN * E.getItem(lll, 0);
						E.setItem(lll, 0, CS * E.getItem(lll, 0) - SN * s.getItem(lll, lll));
						G = s.getItem(lll + 1, lll + 1) * SN;
						s.setItem(lll + 1, lll + 1, CS * s.getItem(lll + 1, lll + 1));
						// DLARTG
						r = hypot(F, G);
						oldCS = F / r;
						oldSN = G / r;
						if ((oldCS < 0.0) && (Math.abs(F) > Math.abs(G))) {
							oldCS = -oldCS;
							oldSN = -oldSN;
							r = -r;
						}
						s.setItem(lll, lll, r);
						// End DLARTG

						F = oldCS * E.getItem(lll, 0) + oldSN * s.getItem(lll + 1, lll + 1);
						s.setItem(lll + 1, lll + 1, oldCS * s.getItem(lll + 1, lll + 1) - oldSN * E.getItem(lll, 0));
						if (lll < atIndex - 1) {
							G = oldSN * E.getItem(lll + 1, 0); 
							E.setItem(lll + 1, 0, oldCS * E.getItem(lll + 1, 0));
						}
						work.setItem(lll, 0, CS);
						work.setItem(lll, 1, SN);
						work.setItem(lll, 2, oldCS);
						work.setItem(lll, 3, oldSN);
					}
					E.setItem(atIndex - 1, 0, F);
					// DBDSQR:603 Update singular vectors
					for (int lll = ll; lll < atIndex; lll++) {
						CS = work.getItem(lll, 0);
						SN = work.getItem(lll, 1);
						oldCS = work.getItem(lll, 2);
						oldSN = work.getItem(lll, 3);
						
						if ((CS != 1.0) || (SN != 0.0)) 
							for (int indx = 0; indx < V.getSizeX(); indx++) {
								double tmp1 = V.getItem(indx, lll + 1);
								double tmp2 = V.getItem(indx, lll);
								V.setItem(indx, lll + 1, CS * tmp1 - SN * tmp2);
								V.setItem(indx, lll,     SN * tmp1 + CS * tmp2);
							}
						if ((oldCS != 1.0) || (oldSN != 0.0)) 
							for (int indy = 0; indy < U.getSizeY(); indy++) {
								double tmp1 = U.getItem(lll + 1, indy);
								double tmp2 = U.getItem(lll, indy);
								U.setItem(lll + 1, indy, oldCS * tmp1 - oldSN * tmp2);
								U.setItem(lll, indy,     oldSN * tmp1 + oldCS * tmp2);
							}
					}
					// DBDSQR:615 Test convergence
					if (Math.abs(E.getItem(atIndex - 1, 0)) < threshold)
						E.setItem(atIndex - 1, 0, 0.0);
				} else {
					System.out.println("use path 12");
					// DBDSQR:622 Chase bulge from bottom to top
					// Save cosines and sines for later singular vector updates
					double F = (Math.abs(s.getItem(atIndex, atIndex)) - shift) *
						(SIGN(1.0, s.getItem(atIndex, atIndex)) + shift / s.getItem(atIndex, atIndex));
					double G = E.getItem(atIndex - 1, 0);
					
					double CS = 1.0;
					double oldCS = 1.0;
					double oldSN = 0.0;
					double SN;

					for (int lll = atIndex; lll > ll; lll--) {
						// DLARTG
						double r = Math.sqrt(Math.pow(F, 2.0) + Math.pow(G, 2.0)); // TODO: hypot(F, G);
						CS = F / r;
						SN = G / r;
						if ((CS < 0.0) && (Math.abs(F) > Math.abs(G))) {
							CS = -CS;
							SN = -SN;
							r = -r;
						}
						//End DLARTG
						if (lll > ll)
							E.setItem(lll, 0, r);
						
						F = CS * s.getItem(lll, lll) + SN * E.getItem(lll - 1, 0);
						E.setItem(lll - 1, 0, CS * E.getItem(lll - 1, 0) - SN * s.getItem(lll, lll));
						G = s.getItem(lll - 1, lll - 1) * SN;
						s.setItem(lll - 1, lll - 1, CS * s.getItem(lll - 1, lll - 1));
						// DLARTG
						r = Math.sqrt(Math.pow(F, 2.0) + Math.pow(G, 2.0)); // TODO: hypot(F, G);
						oldCS = F / r;
						oldSN = G / r;
						if ((oldCS < 0.0) && (Math.abs(F) > Math.abs(G))) {
							oldCS = -oldCS;
							oldSN = -oldSN;
							r = -r;
						}
						// End DLARTG
						s.setItem(lll, lll, r);

						F = oldCS * E.getItem(lll - 1, 0) + oldSN * s.getItem(lll - 1, lll - 1);
						s.setItem(lll - 1, lll - 1, oldCS * s.getItem(lll - 1, lll - 1) - oldSN * E.getItem(lll - 1, 0));
						if (lll > ll + 1) {
							G = oldSN * E.getItem(lll - 2, 0); 
							E.setItem(lll - 2, 0, oldCS * E.getItem(lll - 2, 0));
						}
						work.setItem(lll, 0, CS);
						work.setItem(lll, 1, -SN);
						work.setItem(lll, 2, oldCS);
						work.setItem(lll, 3, -oldSN);
					}
					E.setItem(atIndex - 1, 0, F);
					// DBDSQR:656 Update singular vectors
					for (int lll = ll; lll < atIndex; lll++) {
						CS = work.getItem(lll, 0);
						SN = work.getItem(lll, 1);
						oldCS = work.getItem(lll, 2);
						oldSN = work.getItem(lll, 3);
						
						if ((CS != 1.0) || (SN != 0.0)) 
							for (int indx = V.getSizeX() - 1; indx >= 0; indx--) {
								double tmp1 = V.getItem(indx, lll + 1);
								double tmp2 = V.getItem(indx, lll);
								V.setItem(indx, lll + 1, CS * tmp1 - SN * tmp2);
								V.setItem(indx, lll,     SN * tmp1 + CS * tmp2);
							}
						if ((oldCS != 1.0) || (oldSN != 0.0)) 
							for (int indy = U.getSizeY() - 1; indy >= 0; indy--) {
								double tmp1 = U.getItem(lll + 1, indy);
								double tmp2 = U.getItem(lll, indy);
								U.setItem(lll + 1, indy, oldCS * tmp1 - oldSN * tmp2);
								U.setItem(lll, indy,     oldSN * tmp1 + oldCS * tmp2);
							}
					}
					// DBDSQR:615 Test convergence
					if (Math.abs(E.getItem(ll - 1, 0)) < threshold)
						E.setItem(ll - 1, 0, 0.0);
				}
			}
			// DBDSQR:670 QR iteration finished, go back and check convergence
		}

		if ((atIndex < 0) && (iter >= 0)) 
			svdSortResult(U, s, V);
		else 
			// DBDSQR:720 Maximum number of iterations exceeded, failure to converge
			throw new Error("Maximum number of iterations exceeded, failure to converge");
	}
	
	public static void svdSortResult(Matrix U, Matrix s, Matrix V) {
		// Check data
//		if ((U.getSizeX() != U.getSizeY()) ||		// TODO: Enable this check
//			(V.getSizeX() != V.getSizeY()) ||
//			(V.getSizeX() != s.getSizeX()) ||
//			(U.getSizeY() != s.getSizeY()) )
//			throw new Error("Invalid arguments");
		int minXY = Math.min(s.getSizeX(), s.getSizeY());
		// Make all singular values positive
		for (int j = 0; j < minXY; j++) {
			if (s.getItem(j, j) < 0.0) {
				s.setItem(j, j, -s.getItem(j, j));
				for (int i = V.getSizeX() - 1; i >= 0; i--)
					V.setItem(i, j, -V.getItem(i, j));
			}
		}
		// Sort the singular values into decreasing order (insertion sort on
		// singular values, but only one transposition per singular vector)
		for (int j = 0; j < minXY; j++) {
			// Scan for smallest D(I)
			int minJindex = j;
			double minJvalue = s.getItem(j, j);
			for (int i = j + 1; i < minXY; i++) {
				if (s.getItem(i, i) > minJvalue) {
					minJindex = i;
					minJvalue = s.getItem(i, i);						
				}				
			}
			if (minJindex != j) {
				// Swap singular values and vectors
				double tmp = s.getItem(j, j);
				s.setItem(j, j, s.getItem(minJindex, minJindex));
				s.setItem(minJindex, minJindex, tmp);
				for (int i = V.getSizeX() - 1; i >= 0; i--) {
					tmp = V.getItem(i, j);
					V.setItem(i, j, V.getItem(i, minJindex));
					V.setItem(i, minJindex, tmp);
				}
				for (int i = U.getSizeX() - 1; i >= 0; i--) {   
					tmp = U.getItem(j, i);
					U.setItem(j, i, U.getItem(minJindex, i));
					U.setItem(minJindex, i, tmp);
				}
			}
		}
	}
	
	public void mysvd(Matrix U, Matrix V, Matrix s, Matrix debug) throws Exception {
		U.resize(getSizeY(), getSizeY());
		s.resize(getSizeX(), getSizeY());
		V.resize(getSizeX(), getSizeX());
		s.make0();
		int minXY = Math.min(getSizeX(), getSizeY());

		Matrix tau = new Matrix();
		
		qrDecomposition(tau);
		// Generate Q in U
		qrDecomositionGetQ(tau, U);
		U.printM("U=");
		Matrix UBackup = U.makeCopy();
		Matrix R = new Matrix(getSizeX(), getSizeX());
		//qrDecomositionGetR(R);
		for (int i = R.getSizeX() - 1; i >= 0; i--)
			for (int j = R.getSizeY() - 1; j >= 0; j--)
				R.setItem(i, j, (i >= j) && (j < getSizeY()) ? getItem(i, j) : 0.0);
				
		// DGESVD:1769 Bidiagonalize R in WORK(IU), copying result to VT
		// DGEBRD
		Matrix tauP = new Matrix(R.getSizeX(), 1);
		Matrix tauQ = new Matrix(R.getSizeX(), 1);
		Matrix E = new Matrix(R.getSizeX() - 1, 1);
		
		if ((tau.getSizeX() < minXY) || (tau.getSizeY() < 1))
			tauQ.resize(minXY, 1);
		for (int atIndex = 0; atIndex < R.getSizeX(); atIndex++) {
			// Generate elementary reflector H(i) to annihilate A(i+1:m,i)
			// DLARFG
			double xnorm = 0.0;
			for (int j = R.getSizeY() - 1; j > atIndex; j--)					
				xnorm = hypot(xnorm, R.getItem(atIndex, j));
			
			double beta;
			double tmp_tau;
			if (xnorm == 0.0) { 
				tmp_tau = 0.0;
				beta = R.getItem(atIndex, atIndex);
			} else {
				double alpha = R.getItem(atIndex, atIndex);
				//double beta = -SIGN(Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2)), alpha);
				beta = Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2));
				if (beta == 0.0) {
					tauQ.setItem(atIndex, 0, 0.0);
					continue;
				}
				if (alpha >= 0.0)
					beta = -beta;
				tmp_tau = (beta - alpha) / beta;
				double scale = 1.0 / (alpha - beta);
				for (int j = R.getSizeY() - 1; j > atIndex; j--) 
					R.setItem(atIndex, j, scale * R.getItem(atIndex, j));
			}
			tauQ.setItem(atIndex, 0, tmp_tau);
			// End DLARFG

			// DGEQR2:109 Apply H(i) to A(i:m,i+1:n) from the left
			R.setItem(atIndex, atIndex, 1.0);
			// DLARF Left
			for (int i = R.getSizeX() - 1; i > atIndex; i--) {
				double sum = 0.0;
				for (int j = R.getSizeY() - 1; j >= atIndex; j--) 
					sum += R.getItem(i, j) * R.getItem(atIndex, j);
				for (int j = R.getSizeY() - 1; j >= atIndex; j--) 
					R.setItem(i, j, R.getItem(i, j) - tmp_tau * sum * R.getItem(atIndex, j));
			}
			// End DLARF Left
			R.setItem(atIndex, atIndex, beta);
			s.setItem(atIndex, atIndex, beta);

			// =============================================
			// DGEBD2:176
			if (atIndex >= R.getSizeY() - 1) {
				tauP.setItem(atIndex, 0, 0.0); // DGEBD2:192
				continue;
			}
			// DGEBD2:178 Generate elementary reflector G(i) to annihilate A(i,i+2:n)
			// DLARFG - now working with rows!
			
			int atIndex1 = atIndex + 1;
			//int atIndex2 = Math.min(atIndex + 2, R.getSizeX() - 1);
			xnorm = 0.0;
			for (int i = R.getSizeX() - 1; i > atIndex1; i--)					
				xnorm = hypot(xnorm, R.getItem(i, atIndex));

			if (xnorm == 0.0) {
				tmp_tau = 0.0;
				beta = R.getItem(atIndex1, atIndex);
			} else {
				double alpha = R.getItem(atIndex1, atIndex);
				//double beta = -SIGN(Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2)), alpha);
				beta = Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2));
				if (beta == 0.0) {
					tauP.setItem(atIndex1, 0, 0.0);
					continue;
				}
				if (alpha >= 0.0)
					beta = -beta;
				
				tmp_tau = (beta - alpha) / beta;
				double scale = 1.0 / (alpha - beta);
				for (int i = R.getSizeX() - 1; i > atIndex1; i--) 
					R.setItem(i, atIndex, scale * R.getItem(i, atIndex));
			}
			tauP.setItem(atIndex, 0, tmp_tau);
			// End DLARFG
			
			// DGEBD2:186 Apply G(i) to A(i+1:m,i+1:n) from the right
			R.setItem(atIndex1, atIndex, 1.0);
			// DLARF Right
			for (int j = R.getSizeY() - 1; j >= atIndex1; j--) {
				double sum = 0.0;
				for (int i = R.getSizeX() - 1; i >= atIndex1; i--) 
					sum += R.getItem(i, j) * R.getItem(i, atIndex);
				for (int i = R.getSizeX() - 1; i >= atIndex1; i--) 
					R.setItem(i, j, R.getItem(i, j) - tmp_tau * sum * R.getItem(i, atIndex));
			}
			// End DLARF Right
			R.setItem(atIndex1, atIndex, beta);
			E.setItem(atIndex, 0, beta);
		}
		
		R.printM("R");
		s.printM("S");
		tauP.printM("tauP");
		tauQ.printM("tauQ");
		E.printM("e");
		
		// DGESVD:1807
		for (int i = V.getSizeX() - 1; i >= 0; i--)
			for (int j = i; j >= 0; j--)
				V.setItem(i, j, R.getItem(i, j));
		
		V.printM("V");
		
		// DGESVD:1817 Generate left bidiagonalizing vectors in WORK(IU)
		Matrix Q = new Matrix();
		R.qrDecomositionGetQ(tauQ, Q);
		Q.printM("Q");
		
		// DGESVD:1829 Generate right bidiagonalizing vectors in VT
		
		// DORGBR:217 Shift the vectors which define the elementary reflectors one
		// row downward, and set the first row and column of P' to those of the unit matrix
		for (int j = V.getSizeY() - 1; j > 0; j--) {
			for (int i = j - 1; i > 0; i--)
				V.setItem(j, i, V.getItem(j, i - 1));
			V.setItem(0, j, 0.0);
			V.setItem(j, 0, 0.0);
		}
		V.setItem(0, 0, 1.0);
		
		// DORGL2
		for (int atIndex = V.getSizeX() - 1; atIndex > 0; atIndex--) {
			double tmp_tau = tauP.getItem(atIndex - 1, 0);
			if (atIndex < V.getSizeX() - 1) {
				// Apply H(i) to A(i:m,i:n) from the right
				V.setItem(atIndex, atIndex, 1.0);
				// DLARF Right
				for (int j = V.getSizeY() - 1; j > atIndex; j--) {
					double sum = 0.0;
					for (int i = V.getSizeX() - 1; i >= atIndex; i--) 
						sum += V.getItem(i, j) * V.getItem(i, atIndex);
					for (int i = V.getSizeX() - 1; i >= atIndex; i--) 
						V.setItem(i, j, V.getItem(i, j) - tmp_tau * sum * V.getItem(i, atIndex));
				// End DLARF Right
				}
				for (int j = V.getSizeY() - 2; j >= atIndex; j--) 
					V.setItem(atIndex+1, j, -tmp_tau * V.getItem(atIndex+1, j));
				// End of DORGL2
			}
			V.setItem(atIndex, atIndex, 1.0 - tmp_tau);
			// DORGL2:124 Set A(i,1:i-1) to zero 
			for (int i = atIndex - 1; i > 0; i--)
				V.setItem(i, atIndex, 0.0);
		}
		// End DORGL2
		V.printM("V");

		System.out.println("-----------------------------");

		Q.copyTo(U);
		Matrix work = new Matrix();
		svdDBDSQR(U, s, V, E, work);
		
		System.out.println("FINISHED!!!!!");
		s.printM("S=");
		E.printM("E=");
		V.printM("VT=");
		U.printM("U=");
	
		// DGESVD:1850 Multiply Q in U by left singular vectors of R in WORK(IU), storing result in A
		UBackup.printM("Q =");
		U.printM("IU=");
		for (int i = U.getSizeX() - 1; i >= 0; i--) {
			for (int j = UBackup.getSizeY() - 1; j >= 0; j--) {
				double sum = 0.0;	
				for (int k = U.getSizeY() - 1; k >= 0; k--) {
					double a = UBackup.getItem(k, j);
					double b = U.getItem(i, k);
					sum += a * b;
				}
				setItem(i, j, sum);
				UBackup.setItem(i, j, sum);
			}
		}
		printM("AAAAAAA");
		UBackup.copyTo(U);
		
		// End DBDSQR
	}	
	
	public static void main(String[] args) throws Exception {
		BufferedReader fin = new BufferedReader(new FileReader(
				MatrixTest.class.getResource(
					"SVD-A.txt").getFile()));
		Matrix a = new Matrix(3, 4);
		a.load(fin);
		fin.close();
		Matrix at = a.makeCopy();
		a.transpose(at);
		
		Matrix u = new Matrix();
		Matrix v = new Matrix();
		Matrix s = new Matrix();
		Matrix r = new Matrix();
		Matrix debug = new Matrix();
		Matrix tau = new Matrix();
		
		Matrix b = a.makeCopy();
		
		b.printM("QR");
		b.qrDecomposition(tau);
		b.qrDecomositionGetR(r);
		r.printM("r");
		
		at.printM("LQ");
		at.lqDecomposition(tau);
		at.lqDecomositionGetL(r);
		r.printM("L");
		

		
		
//		a.printM("A");
//		a.mysvd(u, v, s, debug);
//		a.printM("A=");
//		u.printM("U=");
//		v.printM("VT=");
//		s.printM("S=");
		
		
//		a.qrDecomposition(tau);
//		a.printM("A");
//		tau.printM("TAU");
//		a.qrDecomositionGetQ(tau, u);
//		u.printM("U");
//		a.qrDecomositionGetR(r);
//		r.printM("R");

//		at.QRDecomposition(debug);
//		at.printM("AT");
//		debug.printM("TAU");
		
//		
//		a.QRDecomposition(s);
//		//a.mysvd(u, v, s, debug);
//		a.printM("A");
//		s.printM("R");
	}
}
