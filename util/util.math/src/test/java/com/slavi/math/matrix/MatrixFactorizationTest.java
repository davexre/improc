package com.slavi.math.matrix;

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

	void runQrTest(Matrix A) {
		//A.transpose();
		Matrix Q = new Matrix();
		Matrix R = new Matrix();
		MatrixFactorization.qr(A, Q, R);
		A.printM("A");
		Q.printM("Q");
		R.printM("R");

		Matrix t1 = new Matrix();
		Matrix t2 = new Matrix();

		double e;
		Q.copyTo(t1);
		t1.printM("T1");
		e = t1.transpose().mMul(Q, t2).getSquaredDeviationFromE();
		Assert.assertTrue(e < MathUtil.eps);

		e = Q.mMul(R, t1).mSub(A, t2).getSquaredDeviationFrom0();
		Assert.assertTrue(e < MathUtil.eps);
	}

	void runLqTest(Matrix A) {
		Matrix Q = new Matrix();
		Matrix L = new Matrix();
		MatrixFactorization.lq(A, Q, L);

		Matrix t1 = new Matrix();
		Matrix t2 = new Matrix();

		System.out.println(A.toMatlabString("A"));
		System.out.println(L.toMatlabString("L"));


		double e;
		Q.copyTo(t1);
		e = t1.transpose().mMul(Q, t2).getSquaredDeviationFromE();
		Assert.assertTrue(e < MathUtil.eps);

		Q.transpose();
		e = L.mMul(Q, t1).mSub(A, t2).getSquaredDeviationFrom0();
		Q.transpose();
		Assert.assertTrue(e < MathUtil.eps);
	}

	void runOrthoNormalTest(Matrix A) {
		Matrix m = A.makeCopy();
		MatrixFactorization.makeOrthoNormal(m);

		Matrix mt = m.makeCopy();
		mt.transpose();
		Matrix e = m.makeCopy();
		if (m.getSizeX() < m.getSizeY())
			mt.mMul(m, e);
		else
			m.mMul(mt, e);
		Assert.assertTrue("Q' * Q = I", e.getSquaredDeviationFromE() < MathUtil.eps);

		Matrix r = new Matrix();
		mt.mMul(A, r);
		m.mMul(r, e);
		e.mSub(A, e);
		Assert.assertTrue("Q'*A=R, Q*R=A", e.getSquaredDeviationFrom0() < MathUtil.eps);
	}

	Matrix testMatrices[] = new Matrix[] {
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
		for (Matrix i : testMatrices)
			runSvdTest(i.makeCopy());
	}

	public void doIt() throws Exception {
//		Matrix A = Matrix.fromOneLineString("1 2 3; 1 2 3; 4 5 6; 7 8 8");
//		Matrix A = Matrix.fromOneLineString("1 2 3; 1 2 3; 1 2 7; 1 2 8");
		Matrix A = Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 8");
//		A.transpose();
		A.printM("A");
		runSvdTest(A);
		//runOrthoNormalTest(A);
	}

	public static void main(String[] args) throws Exception {
		new MatrixFactorizationTest().doIt();
		System.out.println("Done.");
	}
}
