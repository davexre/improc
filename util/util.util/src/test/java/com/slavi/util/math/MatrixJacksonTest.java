package com.slavi.util.math;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.math.matrix.DiagonalMatrix;
import com.slavi.math.matrix.IMatrix;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.matrix.SymmetricMatrix;
import com.slavi.math.matrix.TriangularMatrix;
import com.slavi.math.matrix.Vector;
import com.slavi.util.Util;

public class MatrixJacksonTest {

	void testIMatrix(IMatrix A) throws Exception {
		testIMatrix(A, Util.jsonMapper());
		testIMatrix(A, Util.xmlMapper());
	}

	void testIMatrix(IMatrix A, ObjectMapper m) throws Exception {
		String v = m.writeValueAsString(A);
		IMatrix B = m.readValue(v, A.getClass());
		double dist = A.getSquaredDifference(B);
		if (dist != 0) {
			A.toMatrix().printM("A");
			B.toMatrix().printM("B");
			System.out.println("Difference " + dist);
			Assert.fail();
		}
	}

	void fillWithData(Vector A) {
		double dd[] = A.getVector();
		for (int i = 0; i < dd.length; i++) {
			dd[i] = i;
		}
		A.loadFromVector(dd);
	}

	@Test
	public void testMatrix() throws Exception {
		Matrix A = new Matrix(3, 4);
		fillWithData(A);
		testIMatrix(A);
		A.transpose();
		testIMatrix(A);
	}

	@Test
	public void testTriangularMatrix() throws Exception {
		TriangularMatrix A = new TriangularMatrix(3, 4, false);
		fillWithData(A);
		testIMatrix(A);
		A.transpose();
		testIMatrix(A);
	}

	@Test
	public void testTriangularMatrixUpper() throws Exception {
		TriangularMatrix A = new TriangularMatrix(3, 4, true);
		fillWithData(A);
		testIMatrix(A);
		A.transpose();
		testIMatrix(A);
	}

	@Test
	public void testDiagonalMatrix() throws Exception {
		DiagonalMatrix A = new DiagonalMatrix(3, 4);
		fillWithData(A);
		testIMatrix(A);
		A.transpose();
		testIMatrix(A);
	}

	@Test
	public void testSymmetricMatrix() throws Exception {
		SymmetricMatrix A = new SymmetricMatrix(4);
		fillWithData(A);
		testIMatrix(A);
		A.transpose();
		testIMatrix(A);
	}

	public static void main(String[] args) throws Exception {
		new MatrixJacksonTest().testTriangularMatrix();
		System.out.println("Done.");
	}
}
