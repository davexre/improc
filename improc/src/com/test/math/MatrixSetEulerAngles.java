package com.test.math;

import java.util.Locale;

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

	/**
	 * <pre>
	 *  mx            my            mz
	 *  1   0   0     cb  0  sb     cc -sc  0
	 *  0  ca -sa      0  1  0      sc  cc  0
	 *  0  sa  ca    -sb  0  cb      0   0  1
	 *  
	 *	mx*mz
	 *  cc		-sc		0
	 *  ca*sc	ca*cc	-sa
	 *  sa*sc	sa*cc	ca
	 *  
	 *  (mx*mz)*my
	 *  cc*cb				-sc			sb*cc
	 *  ca*sc*cb+sa*sb		ca*cc		ca*sc*sb-sa*cb
	 *  sa*sc*cb-ca*sb		sa*cc		sa*sc*sb+ca*cb
	 * </pre>
	 */
	static Matrix makeAngles2(double rx, double ry, double rz, boolean type) {
		/*
		 *  mx            my            mz
		 *  1   0   0     cb  0  sb     cc -sc  0
		 *  0  ca -sa      0  1  0      sc  cc  0
		 *  0  sa  ca    -sb  0  cb      0   0  1
		 */
		double sa = Math.sin(rx);
		double ca = Math.cos(rx);

		double sb = Math.sin(ry);
		double cb = Math.cos(ry);

		double sc = Math.sin(rz);
		double cc = Math.cos(rz);
		
		Matrix r = new Matrix(3, 3);
		if (type) {
			/*
			 *	mz*mx
			 *  cc		-sc*ca		sc*sa
			 *  sc		cc*ca		-cc*sa
			 *  0		sa			ca
			 *  
			 *  (mz*mx)*my
			 *  cc*cb-sb*sc*sa		-sc*ca		sb*cc+cb*sc*sa
			 *  sc*cb+sb*cc*sa		cc*ca		sc*sb-cb*cc*sa
			 *  -ca*sb				sa			ca*cb
			 */
			r.setItem(0, 0, cc*cb-sb*sc*sa);
			r.setItem(1, 0, -sc*ca);
			r.setItem(2, 0, sb*cc+cb*sc*sa);
			
			r.setItem(0, 1, sc*cb+sb*cc*sa);
			r.setItem(1, 1, cc*ca);
			r.setItem(2, 1, sc*sb-cb*cc*sa);
			
			r.setItem(0, 2, -ca*sb);
			r.setItem(1, 2, sa);
			r.setItem(2, 2, ca*cb);
		} else {
			/*
			 *	mx*mz
			 *  cc		-sc		0
			 *  ca*sc	ca*cc	-sa
			 *  sa*sc	sa*cc	ca
			 *  
			 *  (mx*mz)*my
			 *  cc*cb				-sc			sb*cc
			 *  ca*sc*cb+sa*sb		ca*cc		ca*sc*sb-sa*cb
			 *  sa*sc*cb-ca*sb		sa*cc		sa*sc*sb+ca*cb
			 */
			r.setItem(0, 0, cc*cb);
			r.setItem(1, 0, -sc);
			r.setItem(2, 0, sb*cc);
			
			r.setItem(0, 1, ca*sc*cb+sa*sb);
			r.setItem(1, 1, ca*cc);
			r.setItem(2, 1, ca*sc*sb-sa*cb);
			
			r.setItem(0, 2, sa*sc*cb-ca*sb);
			r.setItem(1, 2, sa*cc);
			r.setItem(2, 2, sa*sc*sb+ca*cb);
		}
		return r;
	}
	
	static final double deg2rad = Math.PI / 180;
	static final double rad2deg = 180 / Math.PI;
	static final double rad2grad = 200 / Math.PI;
	
	
	static void test(boolean type) {
		double a = 10 * deg2rad; 
		double b = 20 * deg2rad; 
		double c = 30 * deg2rad;
		
		Matrix m1 = makeAngles(a, b, c, type);
		Matrix m2 = makeAngles2(a, b, c, type);
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
	
	public static String rad2radStr(double angle) {
		return String.format(Locale.US, "%+9.7f rad", angle);
	}
	
	public static String rad2gradStr(double angle) {
		return String.format(Locale.US, "%+1.5f", angle * rad2grad);
	}
	
	public static String rad2degStr(double angle) {
		angle = angle * rad2deg;
		double gr = (int) angle;
		if ((gr == 0) & (angle < 0))
			gr = -0.0000000000001;
		angle = Math.abs(angle - gr) * 60;
		int min = (int) Math.floor(angle);
		double sec = (angle - min) * 60;
		return String.format(Locale.US, "%+1.0f°% 2d'% 4.1f\"", gr, min, sec);
	}
	
	public static void main(String[] args) {
		double c = -0.001 * deg2rad;
		System.out.println(rad2degStr(c));
		System.out.println(rad2gradStr(c));
		System.out.println(rad2radStr(c));
//		test(true);
//		test(false);
	}
}
