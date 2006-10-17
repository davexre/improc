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
	private double m[][];

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
		if (sizeX != sizeY) {
			throw new Error("Invalid argument");
		}
		dest.resize(sizeX, sizeY);
		double D;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				D = m[i][j];
				m[i][j] = dest.m[j][i];
				dest.m[j][i] = D;
			}
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
}
