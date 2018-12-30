package com.slavi.math;

import org.junit.Assert;

import com.slavi.math.matrix.JLapack;
import com.slavi.math.matrix.Matrix;

public class TestMyQrFactorization {

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

	void runQrOldTest(Matrix A) {
		Matrix R = A.makeCopy();
		Matrix Q = new Matrix();
		Matrix tau = new Matrix();
		JLapack.qr(R, Q, tau);

		A.printM("A");
		Q.printM("Q");
		R.printM("R");
		tau.printM("TAU");

		Matrix t1 = new Matrix();
		double e = Q.mMul(R, t1).getSquaredDifference(A);
		Assert.assertEquals(0, e, MathUtil.eps);
	}

	void runLqOldTest(Matrix A) {
		A.printM("A");

		Matrix L = A.makeCopy();
		Matrix Q = new Matrix();
		Matrix tau = new Matrix();
		JLapack.lq(L, Q, tau);

		L.printM("L");
		Q.printM("Q");
		tau.printM("TAU");

		Matrix t1 = new Matrix();
		double e = L.mMul(Q, t1).getSquaredDifference(A);
		Assert.assertEquals(0, e, MathUtil.eps);
	}

	void doIt1() throws Exception {
		//Matrix i = testMatrices[3].transpose();
		Matrix i = Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 8; 1 2 3").transpose();
		runQrOldTest(i.makeCopy());
	}

	void doIt() throws Exception {
		for (Matrix i : testMatrices) {
			runQrOldTest(i.makeCopy());
			runQrOldTest(i.makeCopy().transpose());
		}

		for (Matrix i : testMatrices) {
			runLqOldTest(i.makeCopy());
			runLqOldTest(i.makeCopy().transpose());
		}
	}

	public static void main(String[] args) throws Exception {
		new TestMyQrFactorization().doIt();
		System.out.println("Done.");
	}
}
