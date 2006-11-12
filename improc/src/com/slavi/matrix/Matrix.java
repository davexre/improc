package com.slavi.matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jdom.Element;
import org.jdom.JDOMException;

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
		StreamTokenizer t = new StreamTokenizer(fin);
		for (int j = 0; j < sizeY; j++)
			for (int i = 0; i < sizeX; i++) {
				if (t.nextToken() != StreamTokenizer.TT_NUMBER)
					throw new IOException("Malformed input file");
				m[i][j] = t.nval;
			}
		// Scip the EOL that remains AFTER the last token is read
		fin.readLine();
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

	/**
	 * Returns TAU 
	 * DLARFG: Generates a real elementary reflector H of order n, such that
	 *      H * ( alpha ) = ( beta ),   H' * H = I.
	 *          (   x   )   (   0  )
	 * where alpha and beta are scalars, and x is an (n-1)-element real
	 * vector. H is represented in the form
	 *       H = I - tau * ( 1 ) * ( 1 v' ) ,
	 *                     ( v )
	 * where tau is a real scalar and v is a real (n-1)-element vector.
	 * If the elements of x are all zero, then tau = 0 and H is taken to be the unit matrix.
	 * Otherwise  1 <= tau <= 2.
	 */
	private double DLARFG(int atIndex, boolean useRows) {
		return 0;
//		if (useRows) {
//			// DNRM2: Returns the euclidean norm of a vector DNRM2 := sqrt( x'*x )
//			double ssq = 1.0;
//			double scale = 0.0;
//			for (int i = atIndex + 1; i < getSizeX(); i++) {
//				double absM = Math.abs(getItem(i, atIndex));
//				if (absM == 0.0) 
//					continue;
//				if (scale < absM) {
//					ssq = 1.0 + ssq * Math.pow(scale / absM, 2);
//					scale = absM;
//				} else
//					ssq += Math.pow(absM / scale, 2);
//			}
//			double xnorm = scale / Math.sqrt(ssq);
//			// End DNRM2
//			// TODO: DLARFG:102 XNORM, BETA may be inaccurate; scale X and recompute them
//			double beta = -SIGN(alpha, Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2)));
//			double tau = (beta - alpha) / beta;
//			scale = 1.0 / (alpha - beta);
//			for (int i = atIndex + 1; i < getSizeX(); i++) 
//				setItem(i, atIndex, scale * getItem(i, atIndex));
//			// setItem(atIndex, atIndex, beta);  // How to return BETA ??
//			return tau;
//		}
//		// DNRM2: Returns the euclidean norm of a vector DNRM2 := sqrt( x'*x )
//		double ssq = 1.0;
//		double scale = 0.0;
//		for (int j = atIndex + 1; j < getSizeY(); j++) {
//			double absM = Math.abs(getItem(atIndex, j));
//			if (absM == 0.0) 
//				continue;
//			if (scale < absM) {
//				ssq = 1.0 + ssq * Math.pow(scale / absM, 2);
//				scale = absM;
//			} else
//				ssq += Math.pow(absM / scale, 2);
//		}
//		double xnorm = scale / Math.sqrt(ssq);
//		// End DNRM2
//		// TODO: DLARFG:102 XNORM, BETA may be inaccurate; scale X and recompute them
//		double beta = -SIGN(getItem(atIndex, atIndex),
//				Math.sqrt(Math.pow(getItem(atIndex, atIndex), 2) + Math.pow(xnorm, 2)));
//		double tau = (beta - getItem(atIndex, atIndex)) / beta;
//		scale = 1.0 / (getItem(atIndex, atIndex)- beta);
//		for (int j = atIndex + 1; j < getSizeY(); j++) 
//			setItem(atIndex, j, scale * getItem(atIndex, j));
//		setItem(atIndex, atIndex, beta);
//		return tau;
	}
	
	private void DLARF(int atX, double tau, boolean useRows) {
		if (useRows) {
			for (int j = atX; j < getSizeY(); j++) {
				double sum = 0.0;
				for (int i = atX + 1; i < getSizeX(); i++) {
					sum += getItem(i, j) * getItem(i - 1, atX);
				}
				for (int i = atX + 1; i < getSizeX(); i++) {
					setItem(i, j, getItem(i, j) - tau * sum * getItem(i - 1, atX));
				}				
			}
		} else {
			for (int i = atX; i < getSizeX(); i++) {
				double sum = 0.0;
				for (int j = atX + 1; j < getSizeY(); j++) {
					sum += getItem(i, j) * getItem(atX, j - 1);
				}
				for (int j = atX + 1; j < getSizeY(); j++) {
					setItem(i, j, getItem(i, j) - tau * sum * getItem(atX, j - 1));
				}				
			}
		}
	}
	
	// MY SVD translation from LAPACK's DGESVD
	// MY SVD translation from LAPACK's DGESVD
	public void mysvd(Matrix U, Matrix V, Matrix s) {
		U.resize(getSizeY(), getSizeY());
		s.resize(getSizeX(), getSizeY());
		V.resize(getSizeX(), getSizeX());

		int minXY = Math.min(getSizeX(), getSizeY());
		double work[] = new double[minXY];
		double tau[] = new double[minXY];
		double tauP[] = new double[minXY];

		double anrm = maxAbs();
		// if (anrm < some_small_number) || (anrm > some_big_num) this.scale();
		
		// DGEQR2 QR factorization
		for (int i = 0; i < minXY; i++) {
			// Generate elementary reflector H(i) to annihilate A(i+1:m,i)
			tau[i] = DLARFG(i, true);
			// DGEQR2:109 Apply H(i) to A(i:m,i+1:n) from the left
			double AII = getItem(i, i); 
			setItem(i, i, 1.0);
			DLARF(i, tau[i], true);
			setItem(i, i, AII);			
		}
		// End DGEQR2
		
		// DGESVD:1750 DLACPY
		for (int i = 0; i < getSizeX(); i++)
			for (int j = i; j < getSizeY(); j++)
				U.setItem(i, j, getItem(i, j));

		// DGESVD:1752 Generate Q in U

		// DORGQR
		// DORG2R:98 Initialise columns k+1:n to columns of the unit matrix
		for (int j = getSizeX(); j < getSizeY(); j++) {
			for (int i = 0; i < getSizeY(); i++)
				U.setItem(i, j, 0.0);
			U.setItem(j, j, 1.0);
		}
		// DORG2R:109 Apply H(i) to A(i:m,i:n) from the left
		for (int i = getSizeX() - 1; i >= 0; i--) {
			if (i + 1 < getSizeY()) {
				U.setItem(i, i, 1.0);
				U.DLARF(i, tau[i], true);
				// DORG2R:117 DSCAL
				for (int ii = i + 1; ii < U.getSizeX(); ii++)
					U.setItem(ii, i, -tau[i] * U.getItem(ii, i));
			}
			U.setItem(i, i, 1.0 - tau[i]);
			// DORG2R:120 Set U(1:i-1,i) to zero
			for (int ii = 0; ii < i; ii++)
				U.setItem(i, ii, 0.0);
		}
		// End DORGQR

		// DGESVD:1758 Copy R to WORK(IU), zeroing out below it
		for (int i = 0; i < getSizeX(); i++)
			for (int j = 0; j < getSizeX(); j++)
				if ((i > j) && (j < getSizeY()))  // set(i, i, 0)
					V.setItem(i, j, getItem(i, j)); // Actually WORK[i] is used in the original code
				else
					V.setItem(i, j, 0.0);

		// DGESVD:1769 Bidiagonalize R in WORK(IU), copying result to VT
		// DGEBRD
		double E[] = new double[V.getSizeX()];
		
		for (int i = 0; i < V.getSizeX(); i++) {
			// DGEBD2:163 Generate elementary reflector H(i) to annihilate A(i+1:m,i)
			tau[i] = V.DLARFG(i, true);
			// DGEBD2:167
			s.setItem(i, i, V.getItem(i, i));
			V.setItem(i, i, 1.0);
			// DGEBD2:170 Apply H(i) to A(i:m,i+1:n) from the left
			V.DLARF(i, tau[i], true);
			V.setItem(i, i, s.getItem(i, i));
			if (i + 1 < V.getSizeX()) {
				// DGEBD2:178 Generate elementary reflector G(i) to annihilate A(i,i+2:n)
				tauP[i] = V.DLARFG(i, false); 
				E[i] = V.getItem(i, i + 1);
				V.setItem(i, i + 1, 1.0);
				// DGEBD2:186 Apply G(i) to A(i+1:m,i+1:n) from the right
				//V.DLARF(
			}
		}
		// End DGEBRD
		
		// DGESVD:1776 DLACPY
		Matrix VT = V.makeCopy();
		
		// DGESVD:1779 Generate left bidiagonalizing vectors in WORK(IU)
		
		// DGESVD:1782 DORGBR
		// DORG2R:98 Initialise columns k+1:n to columns of the unit matrix
		// DORG2R:109 Apply H(i) to A(i:m,i:n) from the left
		for (int i = V.getSizeX() - 1; i >= 0; i--) {
			if (i + 1 < V.getSizeY()) {
				V.setItem(i, i, 1.0);
				V.DLARF(i, tau[i], true);
				// DORG2R:117 DSCAL
				for (int ii = i + 1; ii < V.getSizeX(); ii++)
					V.setItem(ii, i, -tau[i] * V.getItem(ii, i));
			}
			V.setItem(i, i, 1.0 - tau[i]);
			// DORG2R:120 Set V(1:i-1,i) to zero
			for (int ii = 0; ii < i; ii++)
				V.setItem(i, ii, 0.0);
		}
		// End DORGQR

		// DGESVD:1786 Generate right bidiagonalizing vectors in VT
		for (int i = V.getSizeX() - 1; i >= 0; i--) {
			if (i + 1 < V.getSizeY()) {
				V.setItem(i, i, 1.0);
				V.DLARF(i, tau[i], true);
				// DORG2R:117 DSCAL
				for (int ii = i + 1; ii < V.getSizeX(); ii++)
					V.setItem(ii, i, -tau[i] * V.getItem(ii, i));
			}
			V.setItem(i, i, 1.0 - tau[i]);
			// DORG2R:120 Set V(1:i-1,i) to zero
			for (int ii = 0; ii < i; ii++)
				V.setItem(i, ii, 0.0);
		}
		// End DORGQR
		// End DORGBR
		
		// DGESVD:1794 Perform bidiagonal QR iteration, computing left 
		// singular vectors of R in WORK(IU) and computing right singular vectors of R in VT

		// DBDSQR
		// DBDSQR:258 Compute approximate maximum, minimum singular values
		double smax = 0.0;
		for (int i = 0; i < s.getSizeX(); i++) {
			double absS = Math.abs(s.getItem(i, i));
			if (smax < absS)
				smax = absS;
		}
		//for (int i = 0; i < 
		
		
		// End DBDSQR
	}
	
	public void svd2(Matrix U, Matrix V, Matrix s) {
		int maxit = 30;
		
		int sizeS = Math.min(sizeX, sizeY);
		s.resize(sizeS, 1);
		U.resize(sizeY, sizeY);
		V.resize(sizeX, sizeX);
		double e[] = new double[sizeX];
		double work[] = new double[sizeY];

		for (int l = 0; l < sizeS; l++) {
			//if (l < nct) {
			{
				// compute the transformation for the l-th column and
				// place the l-th diagonal in s(l).
				
				// start snrm2
				double scale = 0.0;
				double ssq = 1.0;
				for (int jj = l; jj < sizeY; jj++) {
					double absX = Math.abs(m[l][jj]);
					if (scale < absX) {
						ssq = 1.0 + ssq * Math.pow(scale / absX, 2);
						scale = absX;
					} else {
						ssq += Math.pow(absX / scale, 2);
					}
				}
				s.m[l][0] = scale * Math.sqrt(ssq);
				// end of snrm2
				if (s.m[l][0] != 0.0) {
					if (m[l][l] != 0.0) {
						s.m[l][0] = m[l][l] >= 0 ? Math.abs(s.m[l][0]) : -Math.abs(s.m[l][0]);
					}
					double tmp = 1.0 / s.m[l][0];
					// start sscal
					for (int jj = l; jj < sizeY; jj++) {
						m[l][jj] *= tmp;
					}
					// end sscal
					m[l][l] += 1.0;
				}
				s.m[l][0] = -s.m[l][0];
			}
			
			for (int j = l+1; j < sizeX; j++) {
				if (s.m[l][0] != 0.0) {
					// apply the transformation.
					// start sdot
					double t = 0;
					for (int jj = l; jj < sizeY; jj++) {
						t += m[l][jj] * m[j][jj];
					}
					// end sdot
					t = -t / m[l][l];
					// start saxpy
					for (int jj = l; jj < sizeY; jj++) {
						m[j][jj] += t * m[l][jj]; 
					}
					// end saxpy
				}
				// place the l-th row of x into  e for the
				// subsequent calculation of the row transformation.
				e[j] = m[j][l];
			}

			// place the transformation in u for subsequent back
			// multiplication.
			for (int i = l; i < sizeY; i++)
				U.m[l][i] = m[l][i];

			// compute the l-th row transformation and place the
			// l-th super-diagonal in e(l).
				
			// start snrm2
			double scale = 0.0;
			double ssq = 1.0;
			for (int jj = l+1; jj < sizeX; jj++) {
				double absX = Math.abs(e[jj]);
				if (scale < absX) {
					ssq = 1.0 + ssq * Math.pow(scale / absX, 2);
					scale = absX;
				} else {
					ssq += Math.pow(absX / scale, 2);
				}
			}
			e[l] = scale * Math.sqrt(ssq);
			// end of snrm2
			if ((l+1 < sizeX) && (e[l] != 0.0)) {
				if (e[l+1] != 0.0) {
					e[l] = e[l+1] >= 0 ? Math.abs(e[l]) : -Math.abs(e[l]);
				}
				double tmp = 1.0 / e[l];
				// start sscal
				for (int jj = l+1; jj < sizeX; jj++) {
					e[jj] *= tmp;
				}
				// end sscal
				e[l+1] += 1.0;
			}
			e[l] = -e[l];
			if ((l+1 < sizeY) && (e[l] != 0.0)) {
				// apply the transformation.
				for (int i = l+1; i < sizeY; i++) {
					work[i] = 0.0;
				}
				for (int j = l+1; j < sizeX; j++) {
					// start saxpy
					for (int jj = l+1; jj < sizeY; jj++) {
						work[jj] += e[j] * m[j][jj]; 
					}
					// end saxpy
				}
				for (int j = l+1; j < sizeX; j++) {
					// start saxpy
					double t = -e[j] / e[l+1];
					for (int jj = l+1; jj < sizeY; jj++) {
						m[j][jj] += t * work[jj]; 
					}
					// end saxpy
				}
			}
			
			// place the transformation in v for subsequent
			// back multiplication.
			for (int i = l+1; i < sizeX; i++) {
				V.m[l][i] = e[i];
			}
		}
		
		// set up the final bidiagonal matrix or order m.
		if (sizeS < sizeX) // ????
			s.m[sizeS-1][0] = m[sizeS-1][sizeS-1];
		if (sizeS > sizeY) 
			s.m[sizeS-1][0] = 0.0; // ????
		if (sizeS < sizeY)
			e[sizeS-1] = m[sizeS-1][sizeX-1]; // ????
		e[sizeS-1] = 0.0; // ???
		
		int m1 = sizeS - 1; // Math.min(sizeX, sizeY + 1); // ????!!!!????
		
		// generate u.
		for (int j = sizeS; j < sizeY; j++) {
			for (int i = 0; i < sizeY; i++) {
				U.m[j][i] = 0.0;
			}
			U.m[j][j] = 1.0;
		}
			
		for (int l = sizeS - 1; l >= 0; l--) {
			if (s.m[l][0] != 0.0) {
				for (int j = l+1; j < sizeY; j++) {
					// start sdot
					double t = 0;
					for (int jj = l; jj < sizeY; jj++) {
						t += U.m[l][jj] * U.m[j][jj];
					}
					// end sdot
					t = -t / U.m[l][l];
					// start saxpy
					for (int jj = l; jj < sizeY; jj++) {
						U.m[j][jj] += t * U.m[l][jj]; 
					}
					// end saxpy
				}
				// start sscal
				for (int jj = l; jj < sizeY; jj++) {
					U.m[l][jj] = -U.m[l][jj]; 
				}
				// end sscal
				U.m[l][l] += 1.0;
				for (int i = 0; i < l; i++) {
					U.m[l][i] = 0.0;
				}
			} else {
				for (int i = 0; i < sizeY; i++) {
					U.m[l][i] = 0.0;
				}
				U.m[l][l] = 1.0;					
			}
		}

		// generate v.
		for (int l = sizeX - 1; l >= 0; l--) {
			if (e[l] != 0.0) {
				for (int j = l+1; j < sizeX; j++) {
					// start sdot
					double t = 0;
					for (int jj = l+1; jj < sizeX; jj++) {
						t += V.m[l][jj] * V.m[j][jj];
					}
					// end sdot
					t = -t / V.m[l][l+1];
					// start saxpy
					for (int jj = l+1; jj < sizeX; jj++) {
						V.m[j][jj] += t * V.m[l][jj]; 
					}
					// end saxpy
				}
			}
			for (int i = 0; i < sizeX; i++) {
				V.m[l][i] = 0.0;
			}
			V.m[l][l] = 1.0;
		}
		
		// main iteration loop for the singular values.
		int mm = m1;
		int iter = 0;
		while (true) {
			// quit if all the singular values have been found.
			if (m1 == 0)
				break;
			// if too many iterations have been performed, set
			// flag and return.
			if (iter >= maxit) {
				throw new Error("Maximum iterations reached!");
				// break; // TODO: set flag!
			}
			/*
			 * this section of the program inspects for
			 * negligible elements in the s and e arrays.  on
			 * completion the variables kase and l are set as follows.
			 *
			 * kase = 1		if s(m) and e(l-1) are negligible and l.lt.m
			 * kase = 2		if s(l) is negligible and l.lt.m
			 * kase = 3		if e(l-1) is negligible, l.lt.m, and
			 * 				s(l), ..., s(m) are not negligible (qr step).
			 * kase = 4		if e(m-1) is negligible (convergence).
			 */
			int l = 0;
			for (l = m1 - 2; l >= 0; l--) {
				double test = Math.abs(s.m[l][0]) + Math.abs(s.m[l+1][0]);
				double ztest = test + Math.abs(e[l]);
				if (ztest == test) {
					e[l] = 0.0;
					l--;
					break;
				}				
			}
			l++;
			int kase = 0;
			if (l == m1 - 1 - 1) {
				kase = 4;
			} else {
				kase = 1;
				int freezeL = l;
				for (int ls = m1; ls >= freezeL; ls--) {
					if (ls == freezeL) {
						kase = 3;
						break;
					}
					double test = 0.0;
					if (ls != m1) // ???
						test += Math.abs(e[ls]);
					if (ls != freezeL + 1)
						test += Math.abs(e[ls-1]);
					double ztest = test + Math.abs(s.m[ls][0]);
					if (ztest == test) {
						s.m[ls][0] = 0.0;
						break;
					}
					kase = 2;
					l = ls;
				}
			}
			l++;
			// perform the task indicated by kase.
			kase = 4;
			switch (kase) {
			case 1: {
				// deflate negligible s(m).
				double f = e[m1 - 1];
				e[m1 - 1] = 0.0;
				for (int k = m1 - 1; k > l; k--) {
					double t1 = s.m[k][0];
					// start srotg
					double sn = 0.0;
					double cs = 0.0;
					// t1,f,cs,sn
					// sa,sb,c,s
					double roe = Math.abs(t1) > Math.abs(f) ? t1 : f;
					double scale = Math.abs(t1) + Math.abs(f);
					if (scale == 0.0) {
						cs = 1.0;
						sn = 0.0;
						t1 = 0.0;
						f = 0.0;
					} else {
						double r = scale * Math.sqrt(Math.pow(t1 / scale, 2) + Math.pow(f / scale, 2));
						r = roe >= 0.0 ? r : -r;
						cs = t1 / r;
						sn = f / r;
						double z = Math.abs(t1) > Math.abs(f) ? sn : 1.0;
						if ((Math.abs(f) >= Math.abs(t1)) && (cs != 0.0))
							z = 1.0 / cs;
						t1 = r;
						f = z;
					}
					// end srotg
					s.m[k][0] = t1;
					if (k != l) {
						f = -sn * e[k - 1];
						e[k - 1] = cs * e[k - 1];
					}
					// start srot
					for (int jj = 0; jj < sizeX; jj++) {
						double tmp = cs * V.m[k][jj] + sn * V.m[m1][jj];
						V.m[m1][jj] = cs * V.m[m1][jj] - sn * V.m[k][jj];
						V.m[k][jj] = tmp;						
					}
					// end srot
				}
				break;
			}
			case 2: {
				// split at negligible s(l).
				double f = e[l - 1];
				e[l - 1] = 0.0;
				for (int k = l; k < m1; k++) {
					double t1 = s.m[k][0];
					// start srotg
					double sn = 0.0;
					double cs = 0.0;
					// t1,f,cs,sn
					// sa,sb,c,s
					double roe = Math.abs(t1) > Math.abs(f) ? t1 : f;
					double scale = Math.abs(t1) + Math.abs(f);
					if (scale == 0.0) {
						cs = 1.0;
						sn = 0.0;
						t1 = 0.0;
						f = 0.0;
					} else {
						double r = scale * Math.sqrt(Math.pow(t1 / scale, 2) + Math.pow(f / scale, 2));
						r = roe >= 0.0 ? r : -r;
						cs = t1 / r;
						sn = f / r;
						double z = Math.abs(t1) > Math.abs(f) ? sn : 1.0;
						if ((Math.abs(f) >= Math.abs(t1)) && (cs != 0.0))
							z = 1.0 / cs;
						t1 = r;
						f = z;
					}
					// end srotg
					s.m[k][0] = t1;
					f = -sn * e[k];
					e[k] = cs * e[k];
					// start srot
					for (int jj = 0; jj < sizeY; jj++) {
						double tmp = cs * U.m[k][jj] + sn * U.m[l-1][jj];
						U.m[l-1][jj] = cs * U.m[l-1][jj] - sn * U.m[k][jj];
						U.m[k][jj] = tmp;						
					}
					// end srot
				}
				break;
			}
			case 3: {
				// perform one qr step.
				// calculate the shift.
				double sscale = Math.max(Math.abs(s.m[m1][0]), Math.abs(s.m[m1-1][0]));
				sscale = Math.max(Math.abs(e[m1 - 1]), sscale);
				sscale = Math.max(Math.abs(s.m[l][0]), sscale);
				sscale = Math.max(Math.abs(e[l]), sscale);
				double sm = s.m[m1][0] / sscale;
				double smm1 = s.m[m1-1][0] /sscale;
				double emm1 = e[m1-1] /sscale;
				double sl = s.m[l][0] / sscale;
				double el = e[l] / sscale;
				double b = ((smm1 + sm)*(smm1 - sm) + Math.pow(emm1, 2)) / 2.0;
				double c = Math.pow(sm*emm1, 2);
				double shift = 0.0;
				if ((b != 0.0) || (c != 0.0)) {
					shift = Math.sqrt(b * b + c);
					if (b < 0.0)
						shift = -shift;
					shift = c/(b + shift);
				}
				double f = (sl + sm)*(sl - sm) + shift;
				double g = sl*el;
				// chase zeros.
				for (int k = l; k < m1-1; k++) {
					// start srotg
					double sn = 0.0;
					double cs = 0.0;
					// f,g,cs,sn
					// sa,sb,c,s
					double roe = Math.abs(f) > Math.abs(g) ? f : g;
					double scale = Math.abs(f) + Math.abs(g);
					if (scale == 0.0) {
						cs = 1.0;
						sn = 0.0;
						f = 0.0;
						g = 0.0;
					} else {
						double r = scale * Math.sqrt(Math.pow(f / scale, 2) + Math.pow(g / scale, 2));
						r = roe >= 0.0 ? r : -r;
						cs = f / r;
						sn = g / r;
						double z = Math.abs(f) > Math.abs(g) ? sn : 1.0;
						if ((Math.abs(g) >= Math.abs(f)) && (cs != 0.0))
							z = 1.0 / cs;
						f = r;
						g = z;
					}
					// end srotg
					if (k != l)
						e[k-1] = f;
					f = cs*s.m[k][0] + sn*e[k];
					e[k] = cs*e[k] - sn*s.m[k][0];
					g = sn*s.m[k+1][0];
					s.m[k+1][0] *= cs;
					// start srot
					for (int jj = 0; jj < sizeX; jj++) {
						double tmp = cs * V.m[k][jj] + sn * V.m[k+1][jj];
						V.m[k+1][jj] = cs * V.m[k+1][jj] - sn * V.m[k][jj];
						V.m[k][jj] = tmp;						
					}
					// end srot
					// start srotg
					sn = 0.0;
					cs = 0.0;
					// f,g,cs,sn
					// sa,sb,c,s
					roe = Math.abs(f) > Math.abs(g) ? f : g;
					scale = Math.abs(f) + Math.abs(g);
					if (scale == 0.0) {
						cs = 1.0;
						sn = 0.0;
						f = 0.0;
						g = 0.0;
					} else {
						double r = scale * Math.sqrt(Math.pow(f / scale, 2) + Math.pow(g / scale, 2));
						r = roe >= 0.0 ? r : -r;
						cs = f / r;
						sn = g / r;
						double z = Math.abs(f) > Math.abs(g) ? sn : 1.0;
						if ((Math.abs(g) >= Math.abs(f)) && (cs != 0.0))
							z = 1.0 / cs;
						f = r;
						g = z;
					}
					// end srotg
					s.m[k][0] = f;
					f = cs*e[k] + sn*s.m[k+1][0];
					s.m[k+1][0] = -sn*e[k] + cs*s.m[k+1][0];
					g = sn*e[k+1];
					e[k+1] *= cs;
					if (k < sizeY - 1) {
						// start srot
						for (int jj = 0; jj < sizeY; jj++) {
							double tmp = cs * U.m[k][jj] + sn * U.m[k+1][jj];
							U.m[k+1][jj] = cs * U.m[k+1][jj] - sn * U.m[k][jj];
							U.m[k][jj] = tmp;						
						}
						// end srot
					}					
				}
				e[m1-1] = f;
				iter++;
				break;
			}
			case 4:
				// convergence.
				// make the singular value  positive.
				if (s.m[l][0] < 0.0) {
					s.m[l][0] = -s.m[l][0];
					// start sscal
					for (int jj = 0; jj < sizeX; jj++) 
						V.m[l][jj] = -V.m[l][jj];
					// end sscal
				}
				// order the singular value.
				while (true) {
					if (l == mm) 
						break;
					if (s.m[l][0] >= s.m[l+1][0])
						break;
					double t = s.m[l][0];
					s.m[l][0] = s.m[l+1][0];
					s.m[l+1][0] = t;
					if (l < sizeX) {
						// start sswap
						for (int jj = 0; jj < sizeX; jj++) {
							double tmp = V.m[l][jj];
							V.m[l][jj] = V.m[l+1][jj];
							V.m[l+1][jj] = tmp;
						}
						// end sswap
					}
					if (l < sizeY) {
						// start sswap
						for (int jj = 0; jj < sizeY; jj++) {
							double tmp = U.m[l][jj];
							U.m[l][jj] = U.m[l+1][jj];
							U.m[l+1][jj] = tmp;
						}
						// end sswap
					}
					l++;
				}
				iter = 0;
				m1--;
				break;
			}
		}
	}
	
	// Derived from LINPACK code.
	// http://www.netlib.org/lapack/lug/lapack_lug.html
	// http://www.netlib.org/lapack/lug/node53.html
	public void svd3(Matrix U, Matrix V, Matrix s) {
		/* Apparently the failing cases are only a proper subset of (m<n), 
		 so let's not throw error.  Correct fix to come later?
		 if (m<n) {
		 throw new IllegalArgumentException("Jama SVD only works for m >= n"); }
		 */
		s.resize(Math.min(sizeX, sizeY + 1), 1);
		U.resize(Math.min(sizeX, sizeY), sizeY);
		V.resize(sizeX, sizeX);
		double[] e = new double[sizeX];
		double[] work = new double[sizeY];
		boolean wantu = true;
		boolean wantv = true;

		// Reduce A to bidiagonal form, storing the diagonal elements
		// in s and the super-diagonal elements in e.

		int nct = Math.min(sizeY - 1, sizeX);
		int nrt = Math.max(0, Math.min(sizeX - 2, sizeY));
		for (int k = 0; k < Math.max(nct, nrt); k++) {
			if (k < nct) {

				// Compute the transformation for the k-th column and
				// place the k-th diagonal in s[k].
				// Compute 2-norm of k-th column without under/overflow.
				s.m[k][0] = 0.0;
				for (int i = k; i < sizeY; i++) 
					s.m[k][0] = hypot(s.m[k][0], this.m[k][i]);
				
				if (s.m[k][0] != 0.0) {
					if (this.m[k][k] < 0.0)
						s.m[k][0] = -s.m[k][0];
					for (int i = k; i < sizeY; i++) 
						this.m[k][i] /= s.m[k][0];
					this.m[k][k] += 1.0;
				}
				s.m[k][0] = -s.m[k][0];
			}
			for (int j = k + 1; j < sizeX; j++) {
				if ((k < nct) & (s.m[k][0] != 0.0)) {
					// Apply the transformation.
					double t = 0;
					for (int i = k; i < sizeY; i++) 
						t += this.m[k][i] * this.m[j][i];
					t = -t / this.m[k][k];
					for (int i = k; i < sizeY; i++) {
						this.m[j][i] += t * this.m[k][i];
					}
				}
				// Place the k-th row of A into e for the
				// subsequent calculation of the row transformation.
				e[j] = this.m[j][k];
			}
			if (wantu & (k < nct)) {
				// Place the transformation in U for subsequent back
				// multiplication.
				for (int i = k; i < sizeY; i++) 
					U.m[k][i] = this.m[k][i];
			}
			if (k < nrt) {
				// Compute the k-th row transformation and place the
				// k-th super-diagonal in e[k].
				// Compute 2-norm without under/overflow.
				e[k] = 0;
				for (int i = k + 1; i < sizeX; i++) 
					e[k] = hypot(e[k], e[i]);
				if (e[k] != 0.0) {
					if (e[k + 1] < 0.0) 
						e[k] = -e[k];
					for (int i = k + 1; i < sizeX; i++) 
						e[i] /= e[k];
					e[k + 1] += 1.0;
				}
				e[k] = -e[k];
				if ((k + 1 < sizeY) & (e[k] != 0.0)) {
					// Apply the transformation.
					for (int i = k + 1; i < sizeY; i++) 
						work[i] = 0.0;
					for (int j = k + 1; j < sizeX; j++) 
						for (int i = k + 1; i < sizeY; i++) 
							work[i] += e[j] * this.m[j][i];
					for (int j = k + 1; j < sizeX; j++) {
						double t = -e[j] / e[k + 1];
						for (int i = k + 1; i < sizeY; i++) 
							this.m[j][i] += t * work[i];
					}
				}
				if (wantv) {
					// Place the transformation in V for subsequent
					// back multiplication.
					for (int i = k + 1; i < sizeX; i++)
						V.m[k][i] = e[i];
				}
			}
		}

		// Set up the final bidiagonal matrix or order p.
		int p = Math.min(sizeX, sizeY + 1);
		if (nct < sizeX) 
			s.m[nct][0] = this.m[nct][nct];
		if (sizeY < p) 
			s.m[p - 1][0] = 0.0;
		if (nrt + 1 < p) 
			e[nrt] = this.m[p - 1][nrt];
		e[p - 1] = 0.0;

		// If required, generate U.

		if (wantu) {
			for (int j = nct; j < U.sizeX; j++) {
				for (int i = 0; i < sizeY; i++) 
					U.m[j][i] = 0.0;
				U.m[j][j] = 1.0;
			}
			for (int k = nct - 1; k >= 0; k--) 
				if (s.m[k][0] != 0.0) {
					for (int j = k + 1; j < U.sizeX; j++) {
						double t = 0;
						for (int i = k; i < sizeY; i++) 
							t += U.m[k][i] * U.m[j][i];
						t = -t / U.m[k][k];
						for (int i = k; i < sizeY; i++) 
							U.m[j][i] += t * U.m[k][i];
					}
					for (int i = k; i < sizeY; i++) 
						U.m[k][i] = -U.m[k][i];
					U.m[k][k] = 1.0 + U.m[k][k];
					for (int i = 0; i < k - 1; i++) 
						U.m[k][i] = 0.0;
				} else {
					for (int i = 0; i < sizeY; i++) 
						U.m[k][i] = 0.0;
					U.m[k][k] = 1.0;
				}
		}

		// If required, generate V.
		if (wantv) {
			for (int k = sizeX - 1; k >= 0; k--) {
				if ((k < nrt) & (e[k] != 0.0)) {
					for (int j = k + 1; j < U.sizeX; j++) {
						double t = 0.0;
						for (int i = k + 1; i < sizeX; i++) 
							t += V.m[k][i] * V.m[j][i];
						t = -t / V.m[k][k + 1];
						for (int i = k + 1; i < sizeX; i++) 
							V.m[j][i] += t * V.m[k][i];
					}
				}
				for (int i = 0; i < sizeX; i++) 
					V.m[k][i] = 0.0;
				V.m[k][k] = 1.0;
			}
		}

		// Main iteration loop for the singular values.
		int pp = p - 1;
		int iter = 0;
		double eps = Math.pow(2.0, -52.0);
		double tiny = Math.pow(2.0, -966.0);
		while (p > 0) {
			int k, kase;
			// Here is where a test for too many iterations would go.

			// This section of the program inspects for
			// negligible elements in the s and e arrays.  On
			// completion the variables kase and k are set as follows.

			// kase = 1     if s(p) and e[k-1] are negligible and k<p
			// kase = 2     if s(k) is negligible and k<p
			// kase = 3     if e[k-1] is negligible, k<p, and
			//              s(k), ..., s(p) are not negligible (qr step).
			// kase = 4     if e(p-1) is negligible (convergence).

			for (k = p - 2; k >= -1; k--) {
				if (k == -1) 
					break;
				if (Math.abs(e[k]) <= tiny + eps * (Math.abs(s.m[k][0]) + Math.abs(s.m[k + 1][0]))) {
					e[k] = 0.0;
					break;
				}
			}
			if (k == p - 2) {
				kase = 4;
			} else {
				int ks;
				for (ks = p - 1; ks >= k; ks--) {
					if (ks == k) 
						break;
					double t = (ks != p ? Math.abs(e[ks]) : 0.) + (ks != k + 1 ? Math.abs(e[ks - 1]) : 0.);
					if (Math.abs(s.m[ks][0]) <= tiny + eps * t) {
						s.m[ks][0] = 0.0;
						break;
					}
				}
				if (ks == k) {
					kase = 3;
				} else if (ks == p - 1) {
					kase = 1;
				} else {
					kase = 2;
					k = ks;
				}
			}
			k++;

			// Perform the task indicated by kase.
			switch (kase) {

			// Deflate negligible s(p).
			case 1: {
				double f = e[p - 2];
				e[p - 2] = 0.0;
				for (int j = p - 2; j >= k; j--) {
					double t = hypot(s.m[j][0], f);
					double cs = s.m[j][0] / t;
					double sn = f / t;
					s.m[j][0] = t;
					if (j != k) {
						f = -sn * e[j - 1];
						e[j - 1] = cs * e[j - 1];
					}
					if (wantv) {
						for (int i = 0; i < sizeX; i++) {
							t = cs * V.m[j][i] + sn * V.m[p - 1][i];
							V.m[p - 1][i] = -sn * V.m[j][i] + cs * V.m[p - 1][i];
							V.m[j][i] = t;
						}
					}
				}
				break;
			}

			// Split at negligible s(k).
			case 2: { 
				double f = e[k - 1];
				e[k - 1] = 0.0;
				for (int j = k; j < p; j++) {
					double t = hypot(s.m[j][0], f);
					double cs = s.m[j][0] / t;
					double sn = f / t;
					s.m[j][0] = t;
					f = -sn * e[j];
					e[j] = cs * e[j];
					if (wantu) {
						for (int i = 0; i < sizeY; i++) {
							t = cs * U.m[j][i] + sn * U.m[k - 1][i];
							U.m[k - 1][i] = -sn * U.m[j][i] + cs * U.m[k - 1][i];
							U.m[j][i] = t;
						}
					}
				}
				break;
			}

			// Perform one qr step.
			case 3: {
				// Calculate the shift.
				double scale = Math.max(Math.max(Math.max(Math.max(Math.abs(s.m[p - 1][0]), Math.abs(s.m[p - 2][0])), 
					Math.abs(e[p - 2])), Math.abs(s.m[k][0])), Math.abs(e[k]));
				double sp = s.m[p - 1][0] / scale;
				double spm1 = s.m[p - 2][0] / scale;
				double epm1 = e[p - 2] / scale;
				double sk = s.m[k][0] / scale;
				double ek = e[k] / scale;
				double b = ((spm1 + sp) * (spm1 - sp) + epm1 * epm1) / 2.0;
				double c = (sp * epm1) * (sp * epm1);
				double shift = 0.0;
				if ((b != 0.0) | (c != 0.0)) {
					shift = Math.sqrt(b * b + c);
					if (b < 0.0) {
						shift = -shift;
					}
					shift = c / (b + shift);
				}
				double f = (sk + sp) * (sk - sp) + shift;
				double g = sk * ek;

				// Chase zeros.
				for (int j = k; j < p - 1; j++) {
					double t = hypot(f, g);
					double cs = f / t;
					double sn = g / t;
					if (j != k) {
						e[j - 1] = t;
					}
					f = cs * s.m[j][0] + sn * e[j];
					e[j] = cs * e[j] - sn * s.m[j][0];
					g = sn * s.m[j + 1][0];
					s.m[j + 1][0] = cs * s.m[j + 1][0];
					if (wantv) {
						for (int i = 0; i < sizeX; i++) {
							t = cs * V.m[j][i] + sn * V.m[j + 1][i];
							V.m[j + 1][i] = -sn * V.m[j][i] + cs * V.m[j + 1][i];
							V.m[j][i] = t;
						}
					}
					t = hypot(f, g);
					cs = f / t;
					sn = g / t;
					s.m[j][0] = t;
					f = cs * e[j] + sn * s.m[j + 1][0];
					s.m[j + 1][0] = -sn * e[j] + cs * s.m[j + 1][0];
					g = sn * e[j + 1];
					e[j + 1] = cs * e[j + 1];
					if (wantu && (j < sizeY - 1)) {
						for (int i = 0; i < sizeY; i++) {
							t = cs * U.m[j][i] + sn * U.m[j + 1][i];
							U.m[j + 1][i] = -sn * U.m[j][i] + cs * U.m[j + 1][i];
							U.m[j][i] = t;
						}
					}
				}
				e[p - 2] = f;
				iter = iter + 1;
				break;
			}

			// Convergence.
			case 4: {
				// Make the singular values positive.
				if (s.m[k][0] <= 0.0) {
					s.m[k][0] = (s.m[k][0] < 0.0 ? -s.m[k][0] : 0.0);
					if (wantv) {
						for (int i = 0; i <= pp; i++) {
							V.m[k][i] = -V.m[k][i];
						}
					}
				}
				// Order the singular values.
				while (k < pp) {
					if (s.m[k][0] >= s.m[k + 1][0]) {
						break;
					}
					double t = s.m[k][0];
					s.m[k][0] = s.m[k + 1][0];
					s.m[k + 1][0] = t;
					if (wantv && (k < sizeX - 1)) {
						for (int i = 0; i < sizeX; i++) {
							t = V.m[k + 1][i];
							V.m[k + 1][i] = V.m[k][i];
							V.m[k][i] = t;
						}
					}
					if (wantu && (k < sizeY - 1)) {
						for (int i = 0; i < sizeY; i++) {
							t = U.m[k + 1][i];
							U.m[k + 1][i] = U.m[k][i];
							U.m[k][i] = t;
						}
					}
					k++;
				}
				iter = 0;
				p--;
				break;
			}
			}
		}
	}

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
		return ((b) >= 0. ? Math.abs(a) : -Math.abs(a));
	}
	
	/** 
	 * sqrt(a^2 + b^2) without under/overflow. 
	 */
	public static double hypot(double a, double b) {
		double r;
		if (Math.abs(a) > Math.abs(b)) {
			r = b / a;
			r = Math.abs(a) * Math.sqrt(1 + r * r);
		} else if (b != 0) {
			r = a / b;
			r = Math.abs(b) * Math.sqrt(1 + r * r);
		} else {
			r = 0.0;
		}
		return r;
	}
	
	public void QRDecomposition(Matrix RDiag) {
		RDiag.resize(sizeX, 1);

		// Main loop.
		for (int k = 0; k < sizeX; k++) {
			// Compute 2-norm of k-th column without under/overflow.
			double nrm = 0;
			for (int i = k; i < sizeY; i++) {
				nrm = hypot(nrm, m[k][i]);
			}

			if (nrm != 0.0) {
				// Form k-th Householder vector.
				if (m[k][k] < 0) {
					nrm = -nrm;
				}
				for (int i = k; i < sizeY; i++) {
					m[k][i] /= nrm;
				}
				m[k][k] += 1.0;

				// Apply transformation to remaining columns.
				for (int j = k + 1; j < sizeX; j++) {
					double s = 0.0;
					for (int i = k; i < sizeY; i++) {
						s += m[k][i] * m[i][j];
					}
					s = -s / m[k][k];
					for (int i = k; i < sizeY; i++) {
						m[j][i] += s * m[k][i];
					}
				}
			}
			RDiag.m[k][0] = -nrm;
		}
	}
	
	public void getH(Matrix destH) {
		destH.resize(sizeX, sizeY);
		for (int i = 0; i < sizeX; i++) {
			for (int j = 0; j < sizeY; j++) {
				if (i >= j) {
					destH.m[i][j] = m[i][j];
				} else {
					destH.m[i][j] = 0.0;
				}
			}
		}
	}

	public void getQ(Matrix destQ) {
		destQ.resize(sizeX, sizeY);
		for (int k = sizeX - 1; k >= 0; k--) {
			for (int i = 0; i < sizeY; i++) {
				destQ.m[k][i] = 0.0;
			}
			destQ.m[k][k] = 1.0;
			for (int j = k; j < sizeX; j++) {
				if (m[k][k] != 0) {
					double s = 0.0;
					for (int i = k; i < sizeY; i++) {
						s += m[k][i] * destQ.m[j][i];
					}
					s = -s / m[k][k];
					for (int i = k; i < sizeY; i++) {
						destQ.m[j][i] += s * m[k][i];
					}
				}
			}
		}
	}
}
