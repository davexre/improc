package com.slavi.math;

import com.slavi.math.matrix.Matrix;
import com.slavi.math.matrix.MatrixFactorization;

public class GramSchmidt {

//	String Astr = "1 2 3 4; -1 0 -3 5; 0 -2 3 6";
	String Astr = "1 2 3; -1 0 -3; 0 -2 3; 1 4 2";

	double eps = MathUtil.eps;

	void doIt() throws Exception {
		Matrix m = Matrix.fromOneLineString(Astr);
		Matrix a = m.makeCopy();
		m.printM("M");
		MatrixFactorization.makeOrthoNormal(m);
		m.printM("M");

		// Check
		Matrix mt = m.makeCopy();
		mt.transpose();
		Matrix e = m.makeCopy();
		if (m.getSizeX() < m.getSizeY())
			mt.mMul(m, e);
		else
			m.mMul(mt, e);
		e.printM("E");
		System.out.println("Distance from E: " + e.getSquaredDeviationFromE());

		Matrix r = new Matrix();
		mt.mMul(a, r);
		r.printM("R");
		m.mMul(r, e);
		e.mSub(a, e);
		System.out.println("Distance from 0: " + e.getSquaredDeviationFrom0());
	}

	public static void main(String[] args) throws Exception {
		new GramSchmidt().doIt();
		System.out.println("Done.");
	}
}
