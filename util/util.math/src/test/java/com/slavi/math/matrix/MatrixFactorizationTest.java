package com.slavi.math.matrix;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.slavi.math.MathUtil;

public class MatrixFactorizationTest {

	void runSvdTest(Matrix A) {
		Matrix u = new Matrix();
		DiagonalMatrix q = new DiagonalMatrix();
		Matrix v = new Matrix();
		MatrixFactorization.svd(A, u, q, v, true);

		double e;
		Matrix tmp1 = new Matrix();
		Matrix tmp2 = new Matrix();

		u.transpose(tmp1);
		u.mMul(tmp1, tmp2);
		e = tmp2.getSquaredDeviationFromE();
		if (e > MathUtil.eps)
			throw new Error("U * UT != I => " + e);

		u.transpose(tmp1);
		tmp1.mMul(u, tmp2);
		e = tmp2.getSquaredDeviationFromE();
		if (e > MathUtil.eps)
			throw new Error("UT * U != I => " + e);

		v.transpose(tmp1);
		v.mMul(tmp1, tmp2);
		e = tmp2.getSquaredDeviationFromE();
		if (e > MathUtil.eps)
			throw new Error("V * VT != I => " + e);

		tmp1.mMul(v, tmp2);
		e = tmp2.getSquaredDeviationFromE();
		if (e > MathUtil.eps)
			throw new Error("VT * V != I => " + e);

		Matrix s = q.toMatrix();
		v.transpose();
		u.mMul(s, tmp1);
		tmp1.mMul(v, tmp2);
		tmp2.mSub(A, tmp2);
		e = tmp2.getSquaredDeviationFrom0();
		if (e > MathUtil.eps)
			throw new Error("A - U*S*VT != 0 => " + e);
		v.transpose();

		Matrix pinv = MatrixFactorization.pinv(u, q, v, null);
		A.mMul(pinv, tmp1);
		tmp1.mMul(A, tmp2);
		tmp2.mSub(A, tmp2);
		e = tmp2.getSquaredDeviationFrom0();
		if (e > MathUtil.eps)
			throw new Error("A * P+ * A - A != 0 => " + e);
	}

	void runOrthoNormalTest(Matrix A) {
		Matrix m = A.makeCopy();
		List<Integer> nullspace = MatrixFactorization.makeOrthoNormal(m);

		Matrix mt = m.makeCopy();
		mt.transpose();
		Matrix e = m.makeCopy();
		mt.mMul(m, e);

		double err = 0;
		for (int i = e.getSizeX() - 1; i >= 0; i--)
			for (int j = e.getSizeY() - 1; j >= 0; j--) {
				double d = e.getItem(i, j);
				if (i == j && nullspace.indexOf(i) < 0) {
					d -= 1;
				}
				err += d*d;
			}
		Assert.assertTrue("Q' * Q = I", err < MathUtil.eps);

		Matrix r = new Matrix();
		mt.mMul(A, r);
		m.mMul(r, e);
		e.mSub(A, e);
		Assert.assertTrue("Q'*A=R, Q*R=A", e.getSquaredDeviationFrom0() < MathUtil.eps);
	}

	void runQrTest(Matrix A) {
		Matrix Q = new Matrix();
		Matrix R = new Matrix();
		MatrixFactorization.qr(A, Q, R);

//		System.out.println(A.toMatlabString("A"));
//		System.out.println(Q.toMatlabString("Q"));
//		System.out.println(R.toMatlabString("R"));
//		System.out.println();

		Matrix t1 = new Matrix();
		double e = Q.mMul(R, t1).getSquaredDifference(A);
		Assert.assertEquals(0, e, MathUtil.eps);
	}

	void runLqTest(Matrix A) {
		Matrix Q = new Matrix();
		Matrix L = new Matrix();
		MatrixFactorization.lq(A, Q, L);

		System.out.println(A.toMatlabString("A"));
		System.out.println(L.toMatlabString("L"));
		System.out.println(Q.toMatlabString("Q"));
		System.out.println();

		Matrix t1 = new Matrix();
		double e = L.mMul(Q, t1).getSquaredDifference(A);
		Assert.assertEquals(0, e, MathUtil.eps);
	}

	Matrix testMatrices[] = new Matrix[] {
			Matrix.fromOneLineString("0"),
			Matrix.fromOneLineString("1"),
			Matrix.fromOneLineString("0 0 0"),
			Matrix.fromOneLineString("1 1 1"),

			Matrix.fromOneLineString("0 1; 0 0"),
			Matrix.fromOneLineString("0 0; 0 0"),
			Matrix.fromOneLineString("1 0; 0 1"),
			Matrix.fromOneLineString("0 1; 0 1"),
			Matrix.fromOneLineString("0 0; 1 1"),

			Matrix.fromOneLineString("1 0 0; 0 1 1"),
			Matrix.fromOneLineString("1 0; 0 1; 0 1"),

			Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 8"),
			Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 9"),

			Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 8; 1 2 3"),
			Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 9; 9 2 3"),
			Matrix.fromOneLineString("1 2 3; 1 2 3; 1 2 7; 1 2 8"),
			Matrix.fromOneLineString("1 2 3 1; 4 5 6 1; 7 8 8 1"),
			Matrix.fromOneLineString("1 2 3 1; 4 5 6 1; 7 8 9 1"),
		};

	@Test
	public void testSvd() {
		for (Matrix i : testMatrices) {
			runSvdTest(i.makeCopy());
			runSvdTest(i.makeCopy().transpose());
		}
	}

	@Test
	public void testOrthoNormal() {
		for (Matrix i : testMatrices) {
			runOrthoNormalTest(i.makeCopy());
			runOrthoNormalTest(i.makeCopy().transpose());
		}
	}

	@Test
	public void testQR() {
		for (Matrix i : testMatrices) {
			runQrTest(i.makeCopy());
			runQrTest(i.makeCopy().transpose());
		}
	}

	@Test
	public void testLQ() {
		for (Matrix i : testMatrices) {
			runLqTest(i.makeCopy());
			runLqTest(i.makeCopy().transpose());
		}
	}

	public void doIt() throws Exception {
//		Matrix A = Matrix.fromOneLineString("1 2 3; 1 2 3; 4 5 6; 7 8 8");
//		Matrix A = Matrix.fromOneLineString("1 2 3; 1 2 3; 1 2 7; 1 2 8");
//		Matrix A = Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 8");
//		Matrix A = Matrix.fromOneLineString("0 1; 0 0");
		Matrix A = Matrix.fromOneLineString("1 2 3; 1 2 3; 1 2 7; 1 2 8; 1 2 3; 1 2 3; 1 2 7; 1 2 8");
//		Matrix A = Matrix.fromOneLineString("1 1 1");
//		A.transpose();
		A.printM("A");
		runLqTest(A);
//		runQrTest(A);
	}

	public static void main(String[] args) throws Exception {
		new MatrixFactorizationTest().doIt();
		System.out.println("Done.");
	}
}
