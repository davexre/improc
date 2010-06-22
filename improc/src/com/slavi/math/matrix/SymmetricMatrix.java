package com.slavi.math.matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Locale;

public class SymmetricMatrix {

	/**
	 * The elements of the matrix.
	 */
	private double m[];

	/**
	 * Size of the matrix
	 */
	private int sizeM;

	/**
	 * Creates a matrix with sizeX = sizeY = 0.
	 */
	public SymmetricMatrix() {
		resize(0);
	}

	/**
	 * Creates a matrix with aSizeX columns and aSizeY rows and sets all
	 * elements to 0.
	 */
	public SymmetricMatrix(int aSizeM) {
		resize(aSizeM);
	}

	/**
	 * Compares two matrices element by element.
	 * 
	 * @return Returns true if all the ements of the matrices are equal.
	 */
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof SymmetricMatrix))
			return false;
		if (obj == this)
			return true;
		SymmetricMatrix a = (SymmetricMatrix) obj;
		if (a.sizeM != sizeM)
			return false;
		for (int i = m.length - 1; i >= 0; i--)
			if (m[i] != a.m[i])
				return false;
		return true;
	}

	/**
	 * Returns a multiline string containing all elements of the matrix.
	 */
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int j = 0; j < sizeM; j++) {
			for (int i = 0; i <= j; i++) {
				if (i != 0)
					result.append(" ");
				result.append(String.format(Locale.US, "%1$5.10f", new Object[] { new Double(getItem(i, j)) } ));
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
		for (int j = 0; j < sizeM; j++)
			for (int i = 0; i <= j; i++) {
				if (t.nextToken() != StreamTokenizer.TT_NUMBER)
					throw new IOException("Malformed input file");
				setItem(i, j, t.nval);
			}
		// Scip the EOL that remains AFTER the last token is read
		fin.readLine();
	}

	/**
	 * Saves the matrix to a text stream
	 */
	public void save(PrintStream fou) {
		fou.print(this.toString());
	}

	/**
	 * Resizes the matrix if the new size differs from the current matrix size.
	 */
	public void resize(int aSizeM) {
		if (aSizeM < 0) {
			throw new IllegalArgumentException("Invalid matrix size");
		}
		if ((aSizeM == sizeM) && (m != null)) {
			return;
		}
		sizeM = aSizeM;
		m = new double[getVectorSize()];
	}

	/**
	 * Returns the size of the matrix.
	 */
	public int getSizeM() {
		return sizeM;
	}

	/**
	 * Returns the value of the matrix atX column and atY row. The top-left
	 * element is atX=0, atY=0. Also have in mind that this.get(i,j) is
	 * ABSOLUTELY the same as this.get(j,i), i.e. it is the same object.
	 */
	public double getItem(int atX, int atY) {
		if (atX < atY) {
			int tmp = atX;
			atX = atY;
			atY = tmp;
		}
		return m[((atX * (atX + 1)) >> 1) + atY];
	}

	/**
	 * Sets the value of the matrix atX column and atY row. The top-left element
	 * is atX=0, atY=0. Also have in mind that this.set(i,j) is ABSOLUTELY the
	 * same as this.set(j,i), i.e. it is the same object.
	 */
	public void setItem(int atX, int atY, double aValue) {
		if (atX < atY) {
			int tmp = atX;
			atX = atY;
			atY = tmp;
		}
		m[((atX * (atX + 1)) >> 1) + atY] = aValue;
	}

	/**
	 * Returns the size of the SymmetricMatrix as a vector
	 * 
	 * @see SymmetricMatrix#getVectorItem(int)
	 */
	public int getVectorSize() {
		return ((sizeM + 1) * sizeM) >> 1;
	}

	/**
	 * The matrix can be interpreted as a "vector" where the elements are
	 * aligned by rows, i.e.<br>
	 * <br>
	 * <tt>
	 *       SymmetricMatrix [4]<br>
	 *        a b d g<br>
	 *        b c e h<br>
	 *        d e f i<br>
	 *        g h i j<br>
	 *       or with internal (in-memory) representation<br> 
	 *        a      <br>
	 *        b c    <br>
	 *        d e f  <br>
	 *        g h i j<br>
	 *       <br> 
	 *       The same matrix as vector [10] (size+1)*size/2 = (4+1)*4/2=10<br>
	 *        a b c d e f j h i j<br>
	 * </tt>
	 * 
	 * @param aIndex
	 * 
	 * @return The value
	 */
	public double getVectorItem(int aIndex) {
		return m[aIndex];
	}

	/**
	 * @see SymmetricMatrix#getVectorItem(int)
	 */
	public void setVectorItem(int aIndex, double aValue) {
		m[aIndex] = aValue;
	}

	/**
	 * Multiplies two matrices and stores the result in dest matrix. If the dest
	 * matrix is of incorrect size it will be resized. The formula is:<br>
	 * <tt>Result = dest = this * second<br>
	 * </tt>
	 */
	public void mMul(SymmetricMatrix second, SymmetricMatrix dest) {
		if (sizeM != second.sizeM) {
			throw new IllegalArgumentException("Invalid argument");
		}
		dest.resize(sizeM);
		double D;
		for (int i = sizeM - 1; i >= 0; i--) {
			for (int j = sizeM - 1; j >= i; j--) {
				D = 0;
				for (int k = sizeM - 1; k >= 0; k--)
					D += getItem(k, i) * second.getItem(j, k);
				dest.setItem(j, i, D);
			}
		}
	}

	/**
	 * Multiplies two matrices and stores the result in dest matrix. If the dest
	 * matrix is of incorrect size it will be resized. The formula is:<br>
	 * <tt>dest = this * second<br>
	 * </tt>
	 */
	public void mMul(Matrix second, Matrix dest) {
		if (sizeM != second.getSizeY()) {
			throw new IllegalArgumentException("Invalid argument");
		}
		dest.resize(second.getSizeX(), sizeM);
		double D;
		for (int i = sizeM - 1; i >= 0; i--) {
			for (int j = second.getSizeX() - 1; j >= 0; j--) {
				D = 0;
				for (int k = sizeM - 1; k >= 0; k--)
					D += getItem(k, i) * second.getItem(j, k);
				dest.setItem(j, i, D);
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
	public void mSum(SymmetricMatrix second, SymmetricMatrix dest) {
		if (sizeM != second.sizeM) {
			throw new IllegalArgumentException("Invalid argument");
		}
		dest.resize(sizeM);
		for (int i = m.length - 1; i >= 0; i--)
			dest.m[i] = m[i] + second.m[i];
	}

	/**
	 * Performs an element by element subtraction of two matrices of equal size
	 * and stores the result in dest matrix. If the dest matrix is of incorrect
	 * size it will be resized to the same size as the source matrix. The
	 * formula is:<br>
	 * <tt>dest[i][j] = this[i][j] - second[i][j]<br>
	 * </tt>
	 */
	public void mSub(SymmetricMatrix second, SymmetricMatrix dest) {
		if (sizeM != second.sizeM) {
			throw new IllegalArgumentException("Invalid argument");
		}
		dest.resize(sizeM);
		for (int i = m.length - 1; i >= 0; i--)
			dest.m[i] = m[i] - second.m[i];
	}

	/**
	 * Returns the dot product of the matrix. The formula is:<br>
	 * <tt>Result = Sum( m[i][j] )<br>
	 * </tt>
	 */
	public double dotProduct(SymmetricMatrix second) {
		if (sizeM != second.sizeM) {
			throw new IllegalArgumentException("Invalid argument");
		}
		double sum = 0;
		for (int i = sizeM - 1; i >= 0; i--)
			for (int j = sizeM - 1; j >= 0; j--)
				sum += getItem(i, j) * second.getItem(i, j);
		return sum;
	}

	/**
	 * Returns the dot product of the matrix. The formula is:<br>
	 * <tt>Result = Sum( m[i][j] )<br>
	 * </tt>
	 */
	public double dotProduct(Matrix second) {
		if ((sizeM != second.getSizeX()) || (sizeM != second.getSizeY())) {
			throw new IllegalArgumentException("Invalid argument");
		}
		double sum = 0;
		for (int i = sizeM - 1; i >= 0; i--)
			for (int j = sizeM - 1; j >= 0; j--)
				sum += getItem(i, j) * second.getItem(i, j);
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
	public void termMul(SymmetricMatrix second, SymmetricMatrix dest) {
		if (sizeM != second.sizeM) {
			throw new IllegalArgumentException("Invalid argument");
		}
		dest.resize(sizeM);
		for (int i = m.length - 1; i >= 0; i--)
			dest.m[i] = m[i] * second.m[i];
	}

	/**
	 * Performs an element by element division of two matrices of equal size and
	 * stores the result in dest matrix. If the dest matrix is of incorrect size
	 * it will be resized to the same size as the source matrix. The formula is:<br>
	 * <tt>dest[i][j] = this[i][j] / second[i][j]<br>
	 * </tt> <b>Warning:</b><i>If there is an element that is zero, an
	 * exception will rise <code>java.lang.ArithmeticException</code>.</i>
	 */
	public void termDiv(SymmetricMatrix second, SymmetricMatrix dest) {
		if (sizeM != second.sizeM) {
			throw new IllegalArgumentException("Invalid argument");
		}
		dest.resize(sizeM);
		for (int i = m.length - 1; i >= 0; i--)
			dest.m[i] = m[i] / second.m[i];
	}

	/**
	 * Multiplies all elements of the matrix with aValue. The formula is:<br>
	 * <tt>dest[i][j] = aValue * this[i][j]<br>
	 * </tt>
	 */
	public void rMul(double aValue) {
		for (int i = m.length - 1; i >= 0; i--)
			m[i] *= aValue;
	}

	/**
	 * Returns the sum of all elements in the matrix <b>AS IF the matrix is
	 * <i>Matrix</i> not <i>SymmetricMatrix</i></b>
	 */
	public double sumAll() {
		double D = 0;
		for (int i = sizeM - 1; i >= 0; i--)
			for (int j = sizeM - 1; j >= 0; j--)
				D += getItem(i, j);
		return D;
	}

	/**
	 * Retuns the maximum value of all elements of the matrix.
	 */
	public double max() {
		if (sizeM == 0)
			return 0;
		double D = m[0];
		for (int i = m.length - 1; i >= 0; i--)
			if (D < m[i])
				D = m[i];
		return D;
	}

	/**
	 * Retuns the minimum value of all elements of the matrix.
	 */
	public double min() {
		if (sizeM == 0)
			return 0;
		double D = m[0];
		for (int i = m.length - 1; i >= 0; i--)
			if (D > m[i])
				D = m[i];
		return D;
	}

	/**
	 * Sets the elements of dest matrix to the maximum corresponding elements of
	 * this and second matrix. If the dest matrix is of incorrect size it will
	 * be resized. The formula is:<br>
	 * <tt>dest[i][j] = max( this[i][j] , second[i][j] )<br>
	 * </tt>
	 */
	public void mMax(SymmetricMatrix second, SymmetricMatrix dest) {
		if (sizeM != second.sizeM) {
			throw new IllegalArgumentException("Invalid argument");
		}
		dest.resize(sizeM);
		for (int i = m.length - 1; i >= 0; i--)
			dest.m[i] = (m[i] > second.m[i] ? m[i] : second.m[i]);
	}

	/**
	 * Sets the elements of dest matrix to the minimum corresponding elements of
	 * this and second matrix. If the dest matrix is of incorrect size it will
	 * be resized. The formula is:<br>
	 * <tt>dest[i][j] = min( this[i][j] , second[i][j] )<br>
	 * </tt>
	 */
	public void mMin(SymmetricMatrix second, SymmetricMatrix dest) {
		if (sizeM != second.sizeM) {
			throw new IllegalArgumentException("Invalid argument");
		}
		dest.resize(sizeM);
		for (int i = m.length - 1; i >= 0; i--)
			dest.m[i] = (m[i] < second.m[i] ? m[i] : second.m[i]);
	}

	/**
	 * Normalizes the matrix <b>AS IF the matrix is <i>Matrix</i> not
	 * <i>SymmetricMatrix</i></b>. If normalization is not possible, i.e.
	 * sumAll() returns 0, the SymmetricMatrix.make0() is called. The formula
	 * is:<br>
	 * <tt>this[i][j] = this[i][j] / sumAll()<br>
	 * </tt>
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
	 * normalization is not possible, i.e. max()-min()=0, the
	 * SymmetricMatrix.make0() is called. The formula is:<br>
	 * <tt>this[i][j] = (this[i][j] - min()) / (max() - min())<br>
	 * </tt>
	 */
	public void normalize2() {
		if (sizeM == 0)
			return;
		double maxVal = m[0];
		double minVal = maxVal;
		for (int i = m.length - 1; i >= 0; i--) {
			double value = m[i]; 
			if (maxVal < value)
				maxVal = value;
			if (minVal > value)
				minVal = value;
		}
		double delta = maxVal - minVal;
		if (delta == 0) {
			make0();
		} else {
			for (int i = m.length - 1; i >= 0; i--)
				m[i] = (m[i] - minVal) / delta;
		}
	}

	/**
	 * Returns a new symetrical matrix of class Matrix (not SymmetricMatrix).
	 * 
	 * @return Returns the new Matrix matrix.
	 */
	public Matrix makeSquareMatrix() {
		Matrix result = new Matrix(sizeM, sizeM);
		copyToSquareMatrix(result);
		return result;
	}

	/**
	 * Copies this SymmetricMatrix to the dest Matrix. The dest becomes a symetrical matrix.
	 * If the dest matrix is of incorrect size it will be resized.
	 */
	public void copyToSquareMatrix(Matrix dest) {
		dest.resize(sizeM, sizeM);
		for (int i = sizeM - 1; i >= 0; i--)
			for (int j = sizeM - 1; j >= 0; j--)
				dest.setItem(i, j, getItem(i, j));
	}

	/**
	 * Makes a copy of the matrix.
	 * 
	 * @return Returns the new matrix.
	 */
	public SymmetricMatrix makeCopy() {
		SymmetricMatrix result = new SymmetricMatrix(sizeM);
		copyTo(result);
		return result;
	}

	/**
	 * Copies this matrix to a destination. The destination is resized if
	 * necessary.
	 */
	public void copyTo(SymmetricMatrix dest) {
		dest.resize(sizeM);
		for (int i = m.length - 1; i >= 0; i--)
			dest.m[i] = m[i];
	}

	/**
	 * Makes the identity matrix. The formula is:<br>
	 * <tt>Result[i][j] = (i == j) ? 1 : 0<br>
	 * </tt>
	 */
	public void makeE() {
		for (int i = sizeM - 1; i >= 0; i--)
			for (int j = i; j >= 0; j--)
				if (i == j)
					setItem(i, j, 1);
				else
					setItem(i, j, 0);
	}

	/**
	 * Makes a zero matrix. All elements are set to 0.
	 */
	public void make0() {
		for (int i = m.length - 1; i >= 0; i--)
			m[i] = 0;
	}

	/**
	 * Sets all elements of this matrix to aValue.
	 */
	public void makeR(double aValue) {
		for (int i = m.length - 1; i >= 0; i--)
			m[i] = aValue;
	}

	/**
	 * Exchanges the column atX1 with column atX2.<br>
	 * <b>Warning:</b>Do mind that the matrix is SymmetricMatrix and
	 * this.get(i,j) returns the <i>SAME OBJECT</i> as this.get(j,i), i.e.
	 * exchanging two columns also exchanges the corresponding rows.
	 */
	public void exchangeX(int atX1, int atX2) {
		if (atX1 == atX2)
			return;

		int j;
		double tmpA[][] = new double[2][sizeM];
		for (int i = sizeM - 1; i >= 0; i--) {
			tmpA[0][i] = getItem(atX1, i);
			tmpA[1][i] = getItem(atX2, i);
		}

		j = 0;
		for (int i = 0; i < sizeM; i++)
			if (i == atX1) {
				setItem(atX1, i, tmpA[1][atX2]);
			} else {
				if (j == atX2)
					j++;
				setItem(atX1, i, tmpA[1][j++]);
			}

		j = 0;
		for (int i = 0; i < sizeM; i++)
			if (i == atX2) {
				setItem(atX2, i, tmpA[0][atX1]);
			} else {
				if (j == atX1)
					j++;
				setItem(atX2, i, tmpA[0][j++]);
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
	public double debugMinAbsDiag = 0; 
	
	public boolean inverse() {
		ArrayList<XchgRec> xchg = new ArrayList<XchgRec>();
		debugMinAbsDiag = Double.MAX_VALUE;
		
		for (int i = 0; i < sizeM; i++) {
			double A = getItem(i, i);
			double abs = Math.abs(A);
			if (abs != 0.0) {
				debugMinAbsDiag = Math.min(debugMinAbsDiag, abs);
			}			
			if (A == 0) {
				int indexI = 0;
				for (int j = i + 1; j < sizeM; j++) {
					if (getItem(i, j) != 0) {
						indexI = j;
						exchangeX(i, j);
						xchg.add(new XchgRec(i, j));
						break;
					}
				}
				if (indexI == 0) {
					make0();
					return false;
				}
				A = getItem(i, i);
			}

			for (int j = 0; j < sizeM; j++)
				if (i != j) {
					double B = getItem(j, i) / A;
					for (int k = j; k < sizeM; k++)
						if (k != i) {
							if (k < i)
								setItem(j, k, getItem(j, k) + B * getItem(i, k));
							else
								setItem(j, k, getItem(j, k) - B * getItem(i, k));
						}
				}

			for (int j = 0; j < sizeM; j++)
				if (i != j) {
					if (i > j)
						setItem(i, j, -getItem(i, j) / A);
					else
						setItem(i, j, getItem(i, j) / A);
				}
			setItem(i, i, 1 / A);
		}

		for (int i = xchg.size() - 1; i >= 0; i--) {
			XchgRec x = xchg.get(i);
			exchangeX(x.a, x.b);
		}
		return true;
	}

	/**
	 * Compares this matrix to the second and returns the correlation between
	 * them.
	 */
	public MatrixCompareResult compareTo(SymmetricMatrix second) {
		if (sizeM != second.sizeM) {
			throw new IllegalArgumentException("Comparing matrices of different size");
		}
		MatrixCompareResult res = new MatrixCompareResult();
		// *** Изчисляване на корелацията (Pearson's r) между данните. ***

		// Средно аритметично.
		double S = 2.0 / (sizeM * (sizeM + 1));
		res.AvgA = 0;
		res.AvgB = 0;
		for (int i = m.length - 1; i >= 0; i--) {
			res.AvgA += m[i];
			res.AvgB += second.m[i];
		}
		res.AvgA /= S;
		res.AvgB /= S;
		res.SAA = 0;
		res.SAB = 0;
		res.SBB = 0;
		// Коефициенти на корелация.
		double dA, dB;
		for (int i = m.length - 1; i >= 0; i--) {
			dA = m[i] - res.AvgA;
			dB = second.m[i] - res.AvgB;
			res.SAA += dA * dA;
			res.SBB += dB * dB;
			res.SAB += dA * dB;
		}

		// Коефициент на корелация на Pearson
		res.PearsonR = res.SAB / Math.sqrt(res.SAA * res.SBB);

		return res;
	}
}
