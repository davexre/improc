package com.slavi.math;

import com.slavi.math.matrix.Matrix;

public class MatrixRref {
	public static void main(String[] args) {
		Matrix A = Matrix.fromOneLineString("0 1 2; 3 4 5; 6 7 8; 9 1 2");
		//Matrix A = Matrix.fromOneLineString("0 2 3; 0 2 3; 4 5 5; 6 6 7");
		System.out.println("k=" + A.rref());
		A.printM("rref A");
	}
}
