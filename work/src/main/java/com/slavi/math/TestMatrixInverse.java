package com.slavi.math;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import com.slavi.math.matrix.Matrix;
import com.slavi.math.matrix.SymmetricMatrix;
import com.slavi.util.MatrixUtil;

public class TestMatrixInverse {
	SymmetricMatrix testSymmetricMatrices[] = {
			SymmetricMatrix.fromOneLineString("0; 1 0"),
			SymmetricMatrix.fromOneLineString("1"),
			SymmetricMatrix.fromOneLineString("0; 1 1"),
			SymmetricMatrix.fromOneLineString("2; 0 3; 1 0 4"),
			SymmetricMatrix.fromOneLineString("6; 5 6;7 8 1"),
			SymmetricMatrix.fromOneLineString("1; 2 3; 4 5 6"),
		};
	// Singular: SymmetricMatrix.fromOneLineString("737.630401973854; 641.6617208364153 779.8773070691678; 591.1690147703112 579.1128184642857 701.8483323687534; -244.14942110480422 -142.53225692506578 -121.94797613215586 140.3474861834374; -142.53225692506578 -113.35389464089128 -55.025364708805895 59.2187692033351 47.636483134243385; -121.94797613215586 -55.025364708805895 -86.1664120427953 50.908790880328056 21.791431606077214 38.9351225002107; -310.45430442475697 -212.77557101054325 -192.51077701825335 149.87470242562543 68.74329131574528 60.382650116920395 168.012103595918; -212.77557101054325 -210.4822888488422 -135.69931871641975 68.74329131574528 54.85640572134056 25.985835619638816 87.32406154905608 75.72868096542233; -192.51077701825332 -135.69931871641975 -182.82763997874906 60.382650116920395 25.985835619638816 45.205139283043074 79.3118946377687 42.15872628514715 65.92777100160944"),


	void testApacheInverse(Matrix a) {
		BlockRealMatrix aa = MatrixUtil.toApacheMatrix(a);
		RealMatrix im = new LUDecomposition(aa).getSolver().getInverse();
		Matrix ia = MatrixUtil.fromApacheMatrix(im, null);
		double e = ia.mMul(a, null).getSquaredDeviationFromE();
		System.out.println(e);
	}

	void doIt() throws Exception {
		//SymmetricMatrix A = testSymmetricMatrices[0];
		for (SymmetricMatrix A : testSymmetricMatrices) {
			Matrix m = A.toMatrix();
			testApacheInverse(m);
		}
	}

	public static void main(String[] args) throws Exception {
		new TestMatrixInverse().doIt();
		System.out.println("Done.");
	}
}
