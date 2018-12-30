package com.slavi.math.matrix;

import java.util.ArrayList;
import java.util.List;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;

public class MatrixFactorization {
	/**
	 * Calculates the SVD decomposition of A using the Golub & Reinsch method.
	 * Does not modify A. See
	 * <a href="doc-files/SVD-Golub+Reinsch-NM-1970.pdf">SVD-Golub+Reinsch-NM-1970.pdf</a>.
	 * <pre>
	 * Properties:
	 * A = U * diag(S) * V'
	 * U * U' = U' * U = I
	 * V * V' = V' * V = I
	 *
	 * (pseudo inverse)
	 * S+ = diag( [1/s(0), 1/s(1), ... 1/s(n) ] )
	 * A+ = V * S+ * U'
	 * </pre>
	 * @param A		Input: ANY (m,n) matrix (m=A.getSizeY(), n=A.getSizeX())
	 * @param U		Output: (m,m)
	 * @param S		Output: (n,1)
	 * @param V		Output: (n,n)
	 */
	public static void svd(Matrix A, Matrix U, DiagonalMatrix S, Matrix V, boolean sortS) {
		boolean transposed = false;
		if (A.getSizeX() > A.getSizeY()) {
			transposed = true;
			A.transpose();
			Matrix tmp = V;
			V = U;
			U = tmp;
		}

		int m = A.getSizeY();
		int n = A.getSizeX();

		V.resize(n, n);
		S.resize(n, m);
		U.resize(m, m);
		U.make0();
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				U.setItem(j, i, A.getItem(j, i));
		double tol = MathUtil.eps;

		// Householder's reduction to bidiagonal form
		double g = 0;
		double maxX = 0;
		Matrix e = new Matrix(n, 1);
		for (int i = 0; i < n; i++) {
			e.setVectorItem(i, g);
			double s = 0;
			for (int j = i; j < m; j++) {
				double tmp = U.getItem(i, j);
				s += tmp * tmp;
			}

			if (s < tol) {
				g = 0;
			} else {
				double f = U.getItem(i, i);
				g = f < 0 ? Math.sqrt(s) : -Math.sqrt(s);
				double h = f * g - s;
				U.setItem(i, i, f - g);
				for (int j = i + 1; j < n; j++) {
					double ss = 0;
					for (int k = i; k < m; k++) {
						ss += U.getItem(i, k)* U.getItem(j, k);
					}
					f = ss / h;
					for (int k = i; k < m; k++) {
						U.itemAdd(j, k, f * U.getItem(i, k));
					}
				}
			}

			S.setVectorItem(i, g);
			s = 0;
			for (int j = i + 1; j < n; j++) { // Looks like a bug. Seems better m
				double tmp = U.getItem(j, i);
				s += tmp * tmp;
			}
			if (s < tol) {
				g = 0;
			} else {
				double f = U.getItem(i + 1, i);
				g = f < 0 ? Math.sqrt(s) : -Math.sqrt(s);
				double h = f * g - s;
				U.setItem(i + 1, i, f - g);
				for (int j = i + 1; j < n; j++) {
					e.setVectorItem(j, U.getItem(j, i) / h);
				}
				for (int j = i + 1; j < m; j++) {
					double ss = 0;
					for (int k = i + 1; k < n; k++) {
						ss += U.getItem(k, j) * U.getItem(k, i);
					}
					for (int k = i + 1; k < n; k++) {
						U.itemAdd(k, j, ss * e.getVectorItem(k));
					}
				}
			}
			double y = Math.abs(S.getVectorItem(i)) + Math.abs(e.getVectorItem(i));
			if (y > maxX)
				maxX = y;
		}

		// accumulation of right-hand transformations
		for (int i = n - 1; i >= 0; i--) {
			if (g != 0.0) {
				double h = g * U.getItem(i + 1, i); // TODO: i+1 out of range
				for (int j = i + 1; j < n; j++) {
					V.setItem(i, j, U.getItem(j, i) / h);
				}
				for (int j = i + 1; j < n; j++) {
					double ss = 0;
					for (int k = i + 1; k < n; k++) {
						ss += U.getItem(k, i) * V.getItem(j, k);
					}
					for (int k = i + 1; k < n; k++) {
						V.itemAdd(j, k, ss * V.getItem(i, k));
					}
				}
			}
			for (int j = i + 1; j < n; j++) {
				V.setItem(i, j, 0);
				V.setItem(j, i, 0);
			}
			V.setItem(i, i, 1);
			g = e.getVectorItem(i);
		}

		// accumulation of left-hand transformations
		// See 5. Organizational and Notational Details (i)
		for (int i = n; i < m; i++) {
			for (int j = n; j < m; j++) {
				U.setItem(j, i, 0);
			}
			U.setItem(i, i, 1);
		}

		for (int i = n - 1; i >= 0; i--) {
			for (int j = i + 1; j < m; j++) { // See 5.(i)
				U.setItem(j, i, 0);
			}
			double gg = S.getVectorItem(i);
			if (gg != 0.0) {
				double h = gg * U.getItem(i, i);
				for (int j = i + 1; j < m; j++) { // See 5.(i)
					double ss = 0;
					for (int k = i + 1; k < m; k++) {
						ss += U.getItem(i, k) * U.getItem(j, k);
					}
					double ff = ss/h;
					for (int k = i; k < m; k++) {
						U.itemAdd(j, k, ff * U.getItem(i, k));
					}
				}
				for (int j = i; j < m; j++) {
					U.itemMul(i, j, 1/gg);
				}
			} else {
				for (int j = i; j < m; j++) {
					U.setItem(i, j, 0);
				}
			}
			U.itemAdd(i, i, 1);
		}

		// diagonalization of tile bidiagonal form
		double eps = MathUtil.eps * maxX;
		for (int k = n - 1; k >= 0; k--) {
			int its = 0;
			loopK: while(true) {
				boolean cancellation = false;
				int l = k;
				for (; l >= 0; l--) {
					if (Math.abs(e.getVectorItem(l)) <= eps) {
						break;
					}
					if (Math.abs(S.getVectorItem(l - 1)) <= eps) { // TODO: l-1 out of range
						cancellation = true;
						break;
					}
				}
				if (cancellation) {
					// cancellation of e[l] if l > 1
					double c = 0;
					double s = 1;
					for (int i = l; i <= k; i++) {
						double f = s * e.getVectorItem(i);
						e.vectorItemMul(i, c);
						if (Math.abs(f) <= eps)
							break;
						double gg = S.getVectorItem(i);
						double h = Math.sqrt(f*f + gg*gg);
						S.setVectorItem(i, h);
						c = gg/h;
						s = -f/h;
						// Different in Minfit
						for (int j = 0; j < m; j++) {
							// Different in Minfit: indexes in reverse!
							double yy = U.getItem(l - 1, j);
							double zz = U.getItem(i, j);
							U.setItem(l - 1, j, zz*s + yy*c);
							U.setItem(i, j, zz*c - yy*s);
						}
					}
				}

				// test / convergence
				double z = S.getVectorItem(k);
				if (l == k) {
					// convergence
					if (z < 0) {
						// q[k] is made non-negative
						S.setVectorItem(k, -z);
						for (int j = 0; j < n; j++) {
							V.itemMul(k, j, -1);
						}
					}
					break loopK; // break l loop, next k
				}

				// shift from bottom 2X2 minor;
				double x = S.getVectorItem(l);
				double y = S.getVectorItem(k - 1);
				double gg = e.getVectorItem(k - 1);
				double h = e.getVectorItem(k);
				double f = ((y-z)*(y+z) + (gg-h)*(gg+h)) / (2*h*y);
				gg = Math.sqrt(f*f + 1);
				double tmp = f < 0 ? f - gg: f + gg;
				f = ((x-z)*(x+z) + h*(y/tmp - h)) / x;

				// next QR transformation
				double c = 1;
				double s = 1;
				for (int i = l + 1; i <= k; i++) {	// checkme
					gg = e.getVectorItem(i);
					y = S.getVectorItem(i);
					h = s*gg;
					gg = c*gg;
					z = Math.sqrt(f*f + h*h);
					e.setVectorItem(i - 1, z);
					c = f/z;
					s = h/z;
					f = gg*s + x*c;
					gg = gg*c - x*s;
					h = y*s;
					y = y*c;
					for (int j = 0; j < n; j++) {
						double xx = V.getItem(i - 1, j);
						double zz = V.getItem(i, j);
						V.setItem(i - 1, j, zz*s + xx*c);
						V.setItem(i, j, zz*c - xx*s);
					}
					z = Math.sqrt(f*f + h*h);
					S.setVectorItem(i - 1, z);
					c = f/z;
					s = h/z;
					f = s*y + c*gg;
					x = c*y - s*gg;
					// Different in Minfit
					for (int j = 0; j < m; j++) {
						// Different in Minfit: indexes in reverse!
						double yy = U.getItem(i - 1, j);
						double zz = U.getItem(i, j);
						U.setItem(i - 1, j, zz*s + yy*c);
						U.setItem(i, j, zz*c - yy*s);
					}
				}

				e.setVectorItem(l, 0);
				e.setVectorItem(k, f);
				S.setVectorItem(k, x);
				l = k;
				its++;
				if (its >= 50)
					throw new ArithmeticException("no svd convergence in 50 iterations");
			}
		}

		if (sortS) {
			int ss = S.getVectorSize();
			for (int i = 0; i < ss; i++) {
				int maxI = i;
				double maxV = S.getVectorItem(i);
				for (int j = i + 1; j < ss; j++) {
					double v = S.getVectorItem(j);
					if (maxV < v) {
						maxI = j;
						maxV = v;
					}
				}
				if (maxI != i) {
					V.exchangeX(i, maxI);
					U.exchangeX(i, maxI);
					S.exchangeX(i, maxI);
				}
			}
		}

		if (transposed) {
			A.transpose();
			S.transpose();
			Matrix tmp = V;
			V = U;
			U = tmp;
		}
	}

	/**
	 * Calculates the (RIGHT) pseudo-inverse A+ of a matrix A that has been factored by svd(A, U, S, V).
	 * @see {@link #svd(Matrix, Matrix, Matrix, Matrix)}
	 * A+ has dimensions A+(A.getSizeY(), A.getSizeX())
	 * <pre>
	 * A * A+ * A = A
	 *
	 * A * A+ need not be the general identity matrix, but it maps all column vectors of A to themselves
	 * A * A+ = I, if A has independent rows
	 *
	 * if A is square and has full rank then the right A+ equals the left A+:
	 * A * A+ = A+ * A = I
	 * </pre>
	 *
	 * @param U,S,V		Corresponding matrices as calculated by svd(A, U, S, V)
	 * @param dest		Matrix to store the pseudo inverse. If null a new matrix will be created.
	 * @return 			dest or a new matrix if dest was null
	 */
	public static Matrix pinv(Matrix U, DiagonalMatrix S, Matrix V, Matrix dest) {
		int min = Math.min(U.getSizeX(), V.getSizeX());
		// Check dimensions
		if (U.getSizeX() != U.getSizeY() ||
			V.getSizeX() != V.getSizeY() ||
			S.getSizeX() != V.getSizeY() ||
			S.getSizeY() != U.getSizeX()) {
			throw new Error("Invalid arguments. Matirx sizes are U(" +
								U.getSizeX() + "," + U.getSizeY() + "), S(" +
								S.getSizeX() + "," + S.getSizeY() + "), V(" +
								V.getSizeX() + "," + V.getSizeY() + ")");
		}
		if (dest == null)
			dest = new Matrix(U.getSizeX(), V.getSizeY());
		else
			dest.resize(U.getSizeX(), V.getSizeY());
		dest.make0();
		for (int i = min - 1; i >= 0; i--) {
			double d = S.getItem(i, i);
			if (Math.abs(d) < MathUtil.eps)
				continue;
			for (int j = dest.getSizeY() - 1; j >= 0; j--) {
				double scale = V.getItem(i, j) / d;
				for (int k = dest.getSizeX() - 1; k >= 0; k--) {
					dest.itemAdd(k, j, scale * U.getItem(i, k));
				}
			}
		}
		return dest;
	}

	/**
	 * Calculates the (RIGHT) pseudo-inverse A+ of a matrix A using svd(A, U, S, V).
	 * @see {@link #svd(Matrix, Matrix, Matrix, Matrix)}
	 * @see {@link #pinv(Matrix, Matrix, Matrix, Matrix)}
	 * A+ has dimensions A+(A.getSizeY(), A.getSizeX())
	 *
	 * @param dest		Matrix to store the pseudo inverse. If null a new matrix will be created.
	 * @return 			dest or a new matrix if dest was null
	 */
	public static Matrix pinv(Matrix A, Matrix dest) {
		Matrix U = new Matrix();
		DiagonalMatrix S = new DiagonalMatrix();
		Matrix V = new Matrix();
		svd(A, U, S, V, false);
		return pinv(U, S, V, dest);
	}


	/**
	 * Make the matrix OrthoNormal. Uses Gram-Schmidt method.
	 * See <a href="doc-files/QR-Gram-Schmidt.pdf">QR-Gram-Schmidt.pdf</a>
	 * <pre>
	 * A - any (m rows x n cols) matrix
	 * Q = A.makeOrthoNormal();
	 * if m > n:   Q' * Q = Q^-1 * Q = E (left inverse)
	 * if m < n:   Q * Q' = Q * Q^-1 = E (right inverse)
	 * if m = n:   Q' = Q^-1 (two-sided inverse)
	 * </pre>
	 * A = Q*R
	 * Q = makeOrthoNormal(Q)
	 * R = Q' * A
	 * @return List of column numbers that are in the Nullspace of A
	 */
	public static List<Integer> makeOrthoNormal(Matrix A) {
		List<Integer> nullColumns = new ArrayList<>();
		for (int i = 0; i < A.getSizeX(); i++) {
			double s = 0.0;
			for (int j = A.getSizeY() - 1; j >= 0; j--) {
				double tmp = A.getItem(i, j);
				s += tmp * tmp;
			}
			if (s < MathUtil.eps) {
				// TODO: May be this const shuld be different
				System.out.println("Null vector in column " + i);
				nullColumns.add(i);
				continue;
			}
			s = 1.0 / s;
			for (int i2 = A.getSizeX() - 1; i2 > i; i2--) {
				double ss = 0.0;
				for (int j = A.getSizeY() - 1; j >= 0; j--) {
					ss += A.getItem(i, j) * A.getItem(i2, j);
				}
				ss *= -s;
				for (int j = A.getSizeY() - 1; j >= 0; j--) {
					A.itemAdd(i2, j, ss * A.getItem(i, j));
				}
			}
			s = Math.sqrt(s);
			for (int j = A.getSizeY() - 1; j >= 0; j--) {
				A.itemMul(i, j, s);
			}
		}
		return nullColumns;
	}

	/**
	 * Calculates QR of A.
	 */
	public static void qr(Matrix A, Matrix Q, Matrix R) {
/*		A.copyTo(R);
		List<Integer> nullspace = makeOrthoNormal(R);
		Q.resize(newSizeX, newSizeY)
*/
		A.copyTo(Q);
		makeOrthoNormal(Q);
		Q.transpose();
		Q.mMul(A, R);
		Q.transpose();
	}

	public static void lq(Matrix A, Matrix Q, Matrix L) {
		A.copyTo(Q);
		makeOrthoNormal(Q);
		A.transpose();
		A.mMul(Q, L);
		A.transpose();
	}
}
