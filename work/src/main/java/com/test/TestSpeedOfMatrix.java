package com.test;

import com.slavi.math.matrix.Matrix;
import com.slavi.util.Marker;

public class TestSpeedOfMatrix {

	void doSomethingToArray(double m[][], double d) {
		for (int i = m.length - 1; i >= 0; i--) {
			double mi[] = m[i];
			for (int j = mi.length - 1; j >= 0; j--) {
				mi[j] += d;
			}
		}
	}
	
	void doSomethingToArray2(double m[][], double d) {
		for (int i = m.length - 1; i >= 0; i--) {
			double mi[] = m[i];
			for (int j = mi.length - 1; j >= 0; j--) {
				m[i][j] += d;
			}
		}
	}
	
	void doSomethingToMatrix(Matrix m, double d) {
		//d += m.getItem(0, 0);
		for (int j = 0; j < m.getSizeY(); j++)
			for (int i = 0; i < m.getSizeX(); i++) {
				//d += m.getItem(i, j);
				m.itemAdd(i, j, d);
				//m.setItem(i, j, m.getItem(i, j) + d);
			}
		if (d < 0)
			System.out.println(d);
	}
	
	void doSomethingToMatrix2(Matrix m, double d) {
		//d += m.getItem(0, 0);
		for (int j = m.getSizeY() - 1; j >= 0; j--)
			for (int i = m.getSizeX() - 1; i >= 0; i--) {
				//d += m.getItem(i, j);
				//m.itemAdd(i, j, d);
				m.setItem(i, j, m.getItem(i, j) + d);
			}
		if (d < 0)
			System.out.println(d);
	}
	
	void doIt() throws Exception {
		// 19.158 seconds -> separate get/set
		// 18.734 seconds -> itemAdd
		// 17.192 seconds -> itemAdd without range check
		Matrix m = new Matrix(10, 10);
		Marker.mark("matrix");
		for (int i = 0; i < 200_000_000; i++)
				doSomethingToMatrix(m, 0.001);
		Marker.release();

		
/*		// 10.675 seconds
		double a[][] = new double[10][10];
		Marker.mark("array");
		for (int i = 0; i < 200_000_000; i++)
				doSomethingToArray(a, 0.001);
		Marker.release();
*/
/*		// 23.427 seconds
		double a[][] = new double[10][10];
		Marker.mark("array");
		for (int i = 0; i < 200_000_000; i++)
				doSomethingToArray2(a, 0.001);
		Marker.release();
*/
	}

	public static void main(String[] args) throws Exception {
		new TestSpeedOfMatrix().doIt();
		System.out.println("Done.");
	}
}
