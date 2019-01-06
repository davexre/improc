package com.slavi.math.matrix;

import org.junit.Assert;
import org.junit.Test;

import com.slavi.math.MathUtil;

public class MatrixInverseTest {

	SymmetricMatrix testSymmetricMatrices[] = {
			SymmetricMatrix.fromOneLineString("0; 1 0"),
			SymmetricMatrix.fromOneLineString("1"),
			SymmetricMatrix.fromOneLineString("0; 1 1"),
			SymmetricMatrix.fromOneLineString("2; 0 3; 1 0 4"),
			SymmetricMatrix.fromOneLineString("6; 5 6;7 8 1"),
			SymmetricMatrix.fromOneLineString("1; 2 3; 4 5 6"),
		};
	// Singular: SymmetricMatrix.fromOneLineString("737.630401973854; 641.6617208364153 779.8773070691678; 591.1690147703112 579.1128184642857 701.8483323687534; -244.14942110480422 -142.53225692506578 -121.94797613215586 140.3474861834374; -142.53225692506578 -113.35389464089128 -55.025364708805895 59.2187692033351 47.636483134243385; -121.94797613215586 -55.025364708805895 -86.1664120427953 50.908790880328056 21.791431606077214 38.9351225002107; -310.45430442475697 -212.77557101054325 -192.51077701825335 149.87470242562543 68.74329131574528 60.382650116920395 168.012103595918; -212.77557101054325 -210.4822888488422 -135.69931871641975 68.74329131574528 54.85640572134056 25.985835619638816 87.32406154905608 75.72868096542233; -192.51077701825332 -135.69931871641975 -182.82763997874906 60.382650116920395 25.985835619638816 45.205139283043074 79.3118946377687 42.15872628514715 65.92777100160944"),

	void doIt() throws Exception {
		SymmetricMatrix SA = testSymmetricMatrices[0];
		Matrix A = SA.toMatrix();
		Matrix a = A.makeCopy();
		Assert.assertTrue(a.inverse());
		a.printM("A");

		SymmetricMatrix sa = SA.makeCopy();
		Assert.assertTrue(sa.inverse());

		SymmetricMatrix sb = new SymmetricMatrix();

		Assert.assertEquals(0, SA.mMul(sa, sb).getSquaredDeviationFromE(), MathUtil.eps);
		Assert.assertEquals(0, sa.mMul(SA, sb).getSquaredDeviationFromE(), MathUtil.eps);
	}

	public static void main(String[] args) throws Exception {
		new MatrixInverseTest().doIt();
		System.out.println("Done.");
	}

	void testSymmetricMatrixInverse(SymmetricMatrix SA) {
		Matrix A = SA.toMatrix();
		Matrix a = A.makeCopy();
		Assert.assertTrue(a.inverse());

		SymmetricMatrix sa = SA.makeCopy();
		Assert.assertTrue(sa.inverse());

		SymmetricMatrix sb = new SymmetricMatrix();

		Assert.assertEquals(0, SA.mMul(sa, sb).getSquaredDeviationFromE(), MathUtil.eps);
		Assert.assertEquals(0, sa.mMul(SA, sb).getSquaredDeviationFromE(), MathUtil.eps);
	}

	@Test
	public void testSymmetricMatrixInverse() {
		for (SymmetricMatrix i : testSymmetricMatrices)
			testSymmetricMatrixInverse(i);
	}

	void testMatrixInverse(Matrix A) {
		Matrix a = A.makeCopy();
		Assert.assertTrue(a.inverse());
		Matrix b = new Matrix();
		Assert.assertEquals(0, A.mMul(a, b).getSquaredDeviationFromE(), MathUtil.eps);
		Assert.assertEquals(0, a.mMul(A, b).getSquaredDeviationFromE(), MathUtil.eps);
	}

	Matrix testMatrices[] = {
			Matrix.fromOneLineString("1 2 3 4; 8 7 6 6; 9 11 10 12; 13 13 13 13"),
			Matrix.fromOneLineString("0 1 2; 3 0 5; 0 9 0"),
			Matrix.fromOneLineString("0 -1; 1 0"),
			Matrix.fromOneLineString("0 3; 2 0"),
			Matrix.fromOneLineString("0 1 0; 0 0 2; 1 0 0"),
			Matrix.fromOneLineString("1 2 3 4; 2 3 3 5; 3 4 5 5; 5 5 6 6"),
			Matrix.fromOneLineString("-1 -1 3; 2 1 2; -2 -2 1"),
			Matrix.fromOneLineString("1 2 3; 0 4 4; 1 8 1"),
			Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 8"),
		};

	@Test
	public void testMatrixInverse() {
		for (Matrix i : testMatrices)
			testMatrixInverse(i);
	}
}
