package com.slavi.math.matrix;

public class TriangularMatrixTest {

	void doIt() throws Exception {
		TriangularMatrix tm = new TriangularMatrix(3, 2, false);
		tm.resize(3, 2);
//		tm.transpose();
//		tm.resize(2, 3);
		if (tm.isUpper()) {
			int c = 1;
			for (int j = 0 ; j < tm.getSizeY(); j++) {
				for (int i = j; i < tm.getSizeX(); i++)
					tm.setItem(i, j, c++);
			}
		} else {
			int c = 1;
			for (int j = 0 ; j < tm.getSizeY(); j++) {
				for (int i = 0; i < Math.min(j+1, tm.getSizeX()); i++)
					tm.setItem(i, j, c++);
			}
		}

//		System.out.println(Arrays.toString(tm.m));

		Matrix<?> m = tm.toMatrix();
		System.out.println(tm.isUpper());
		m.printM("TM");
//		m.loadFromVector((double[]) null).printM("m");
	}

	public static void main(String[] args) throws Exception {
		new TriangularMatrixTest().doIt();
		System.out.println("Done.");
	}
}
