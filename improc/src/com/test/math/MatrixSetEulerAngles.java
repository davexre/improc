package com.test.math;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;

public class MatrixSetEulerAngles {

	static Matrix makeAngles(double a, double b, double c, boolean type) {
		double sa = Math.sin(a);
		double ca = Math.cos(a);

		double sb = Math.sin(b);
		double cb = Math.cos(b);

		double sc = Math.sin(c);
		double cc = Math.cos(c);

		Matrix rx = new Matrix(3, 3);
		Matrix ry = new Matrix(3, 3);
		Matrix rz = new Matrix(3, 3);
		// rx
		rx.setItem(0, 0, 1.0);
		rx.setItem(1, 0, 0.0);
		rx.setItem(2, 0, 0.0);

		rx.setItem(0, 1, 0.0);
		rx.setItem(1, 1, ca);
		rx.setItem(2, 1, -sa);

		rx.setItem(0, 2, 0.0);
		rx.setItem(1, 2, sa);
		rx.setItem(2, 2, ca);
		// ry
		ry.setItem(0, 0, cb);
		ry.setItem(1, 0, 0.0);
		ry.setItem(2, 0, sb);

		ry.setItem(0, 1, 0.0);
		ry.setItem(1, 1, 1.0);
		ry.setItem(2, 1, 0.0);

		ry.setItem(0, 2, -sb);
		ry.setItem(1, 2, 0.0);
		ry.setItem(2, 2, cb);
		// rz
		rz.setItem(0, 0, cc);
		rz.setItem(1, 0, -sc);
		rz.setItem(2, 0, 0.0);

		rz.setItem(0, 1, sc);
		rz.setItem(1, 1, cc);
		rz.setItem(2, 1, 0.0);

		rz.setItem(0, 2, 0.0);
		rz.setItem(1, 2, 0.0);
		rz.setItem(2, 2, 1.0);

		Matrix t = new Matrix(3, 3);
		if (type)
			rz.mMul(rx, t);
		else
			rx.mMul(rz, t);
		t.mMul(ry, rx);
		return rx;
	}

	static void test(boolean type) {
		double a = 10 * MathUtil.deg2rad; 
		double b = 20 * MathUtil.deg2rad; 
		double c = 30 * MathUtil.deg2rad;
		
		Matrix m1 = makeAngles(a, b, c, type);
		Matrix m2 = MathUtil.makeAngles(a, b, c, type);
		Matrix t = new Matrix();
		m1.mSub(m2, t);
		if (!t.is0(0)) {
			System.out.println("ERROR");
			System.out.println(m1);
			System.out.println();
			System.out.println(m2);
			System.out.println();
			System.out.println(t);
		}
	}
	
	public static void main(String[] args) {
		test(true);
		test(false);
	}
}
