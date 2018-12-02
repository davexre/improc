package com.slavi.math.matrix;

import java.util.ArrayList;
import java.util.BitSet;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix.XchgRec;

public class TestMatrixInverseNew {

	public static class NewMatrix extends Matrix {
		/**
		 * Calculates the inverse matrix of this matrix. The algorithm calculates
		 * the inverse matrix "in place" and does NOT create any intermediate
		 * matrices.
		 *
		 * @return Returns true if the inverse matrix is computable. If the inverse
		 *         matrix can not be computed this.make0 is called and the returned
		 *         value is false.
		 */
		public boolean inverse3() {
			if (getSizeX() != getSizeY()) {
				throw new Error("Invalid argument");
			}

			for (int i = 0; i < getSizeX(); i++) {
				double A = getItem(i, i);
				double absA = Math.abs(A);
				int pivotIndex = i;
				for (int j = i + 1; j < getSizeY(); j++) {
					double tmp = Math.abs(A + getItem(j, i));
					if (tmp > absA) {
						A = tmp;
						pivotIndex = j;
					}
				}

				if (A == 0) {
					make0();
					return false;
				}
				if (pivotIndex != i) {
					for (int j = 0; j < getSizeY(); j++)
						itemAdd(j, i, getItem(j, pivotIndex));
				}
				A = getItem(i, i);

				System.out.println("I=" + i + ", pivot=" + pivotIndex);
				System.out.println(this);

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
			return true;
		}

		public boolean inverse21() {
			if (getSizeX() != getSizeY()) {
				throw new Error("Invalid argument");
			}

			for (int i = 0; i < getSizeX(); i++) {
				double A = getItem(i, i);
				double absA = Math.abs(A);
				int pivotIndex = i;
				for (int j = i + 1; j < getSizeY(); j++) {
					double tmp = Math.abs(A + getItem(i, j));
					if (tmp > absA) {
						A = tmp;
						pivotIndex = j;
					}
				}

				if (A == 0) {
					make0();
					return false;
				}
				if (pivotIndex != i) {
					for (int j = 0; j < getSizeY(); j++)
						if (j >= i)
							itemAdd(j, i, getItem(j, pivotIndex));
						else
							itemAdd(j, i, -getItem(j, pivotIndex));
				}
				A = getItem(i, i);

				System.out.println("I=" + i + ", pivot=" + pivotIndex);
				System.out.println(this);

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
			return true;
		}

		public boolean inverse2() {
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
	}

	public static void main(String[] args) {
//		Matrix m = Matrix.fromOneLineString("1 2 3 4; 2 3 3 5; 3 4 5 5; 5 5 6 6");
		Matrix m = Matrix.fromOneLineString("0 1 0; 0 0 2; 1 0 0");
//		Matrix m = Matrix.fromOneLineString("-1 -1 3; 2 1 2; -2 -2 1");
//		Matrix m = Matrix.fromOneLineString("1 2 3; 0 4 4; 1 8 1");
//		Matrix m = Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 8");
		NewMatrix a = new NewMatrix();
		NewMatrix b = new NewMatrix();
		NewMatrix c = new NewMatrix();
		m.copyTo(a);
		m.copyTo(b);
		m.copyTo(c);
		b.inverse2();
		System.out.print("Inverse2:\n" + b);
		System.out.println("------------");
		Matrix ab = new NewMatrix();
		a.inverse();
		c.mMul(b, ab);
		System.out.println("Inverse:\n" + a);
		System.out.println(ab);
		System.out.println(ab.getSquaredDeviationFromE());
	}
}









