package com.slavi.math.matrix;

public interface IMatrix <T extends IMatrix<T>> {

	/**
	 * Returns the number of columns in the matrix.
	 */
	int getSizeX();

	/**
	 * Returns the number of rows in the matrix.
	 */
	int getSizeY();

	/**
	 * Returns the value of the matrix atX column and atY row. The top-left
	 * element is atX=0, atY=0.
	 */
	double getItem(int atX, int atY);

	/**
	 * Sets the value of the matrix atX column and atY row. The top-left element
	 * is atX=0, atY=0.
	 */
	void setItem(int atX, int atY, double aValue);

	T transpose();

	default void assertSameSize(IMatrix second) {
		if ((getSizeX() != second.getSizeX()) || (getSizeY() != second.getSizeY()))
			throw new Error("Invalid argument. Sizes of this(" + getSizeX() + "," + getSizeY() + ") do not match second(" + second.getSizeX() + "," + second.getSizeY() + ")");
	}

	/**
	 * Calculates sum( (this(i,j) - second(i,j)) ^ 2 )
	 */
	default double getSquaredDifference(IMatrix second) {
		assertSameSize(second);
		double result = 0.0;
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--) {
				double d = getItem(i, j) - second.getItem(i, j);
				result += d*d;
			}
		return result;
	}

	/**
	 * Returns the new value.
	 */
	default double itemAdd(int atX, int atY, double aValue) {
		double d = getItem(atX, atY) + aValue;
		setItem(atX, atY, d);
		return d;
	}

	/**
	 * Returns the new value.
	 */
	default double itemMul(int atX, int atY, double aValue) {
		double d = getItem(atX, atY) * aValue;
		setItem(atX, atY, d);
		return d;
	}

	/**
	 * 1-norm, the largest column sum of the absolute values of A.
	 * https://octave.sourceforge.io/octave/function/norm.html
	 *
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
	default double getOneNorm() {
		double result = 0.0;
		for (int i = getSizeX() - 1; i >= 0; i--) {
			double d = 0.0;
			for (int j = getSizeY() - 1; j >= 0; j--)
				d += Math.abs(getItem(i, j));
			if (d > result)
				result = d;
		}
		return result;
	}

	/**
	 * Infinity norm, the largest row sum of the absolute values of A.
	 * https://octave.sourceforge.io/octave/function/norm.html
	 *
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
	default double getNormInfinity() {
		double result = 0.0;
		for (int j = getSizeY() - 1; j >= 0; j--) {
			double d = 0.0;
			for (int i = getSizeX() - 1; i >= 0; i--)
				d += Math.abs(getItem(i, j));
			if (d > result)
				result = d;
		}
		return result;
	}

	/**
	 * Frobenius norm of A, sqrt (sum (diag (A' * A))).
	 * https://octave.sourceforge.io/octave/function/norm.html
	 *
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
	default double getForbeniusNorm() {
		double scale = 0.0;
		double sum = 1.0;
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--) {
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

	default double[][] toArray() {
		double [][] r = new double[getSizeX()][getSizeY()];
		for (int i = getSizeX() - 1; i >= 0; i--) {
			for (int j = getSizeY() - 1; j >= 0; j--) {
				r[i][j] = getItem(i, j);
			}
		}
		return r;
	}

	default Matrix toMatrix() {
		return toMatrix(null);
	}

	/**
	 * Copies this IMatrix to the dest Matrix.
	 * If the dest matrix is of incorrect size it will be resized.
	 */
	default Matrix toMatrix(Matrix dest) {
		if (dest == null)
			dest = new Matrix(getSizeX(), getSizeY());
		else
			dest.resize(getSizeX(), getSizeY());
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--)
				dest.setItem(i, j, getItem(i, j));
		return dest;
	}

	/**
	 * Compares this matrix to the second and returns the correlation between
	 * them.
	 */
	default MatrixCompareResult compareTo(IMatrix second) {
		if ((getSizeX() != second.getSizeX()) || (getSizeY() != second.getSizeY())) {
			throw new IllegalArgumentException("Comparing matrices of different size");
		}
		MatrixCompareResult res = new MatrixCompareResult();
		// *** Изчисляване на корелацията (Pearson's r) между данните. ***

		double sumAll1 = 0;
		double sumAll2 = 0;
		for (int i = getSizeX() - 1; i >= 0; i--) {
			for (int j = getSizeY() - 1; j >= 0; j--) {
				sumAll1 += getItem(i, j);
				sumAll2 += second.getItem(i, j);
			}
		}

		// Средно аритметично.
		double S = getSizeX() * getSizeY();

		res.AvgA = sumAll1 / S;
		res.AvgB = sumAll2 / S;
		res.SAA = 0;
		res.SAB = 0;
		res.SBB = 0;
		// Коефициенти на корелация.
		double dA, dB;
		for (int i = getSizeX() - 1; i >= 0; i--)
			for (int j = getSizeY() - 1; j >= 0; j--) {
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
}
