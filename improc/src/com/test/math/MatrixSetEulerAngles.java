package com.test.math;

import com.slavi.math.matrix.Matrix;

public class MatrixSetEulerAngles {

	static Matrix makeAngles(double a, double b, double c) {
		double sina = Math.sin(a);
		double cosa = Math.cos(a);

		double sinb = Math.sin(b);
		double cosb = Math.cos(b);

		double sinc = Math.sin(c);
		double cosc = Math.cos(c);

		Matrix rx = new Matrix(3, 3);
		Matrix ry = new Matrix(3, 3);
		Matrix rz = new Matrix(3, 3);
		// rx
		rx.setItem(0, 0, 1.0);
		rx.setItem(1, 0, 0.0);
		rx.setItem(2, 0, 0.0);

		rx.setItem(0, 1, 0.0);
		rx.setItem(1, 1, cosa);
		rx.setItem(2, 1, -sina);

		rx.setItem(0, 2, 0.0);
		rx.setItem(1, 2, sina);
		rx.setItem(2, 2, cosa);
		// ry
		ry.setItem(0, 0, cosb);
		ry.setItem(1, 0, 0.0);
		ry.setItem(2, 0, sinb);

		ry.setItem(0, 1, 0.0);
		ry.setItem(1, 1, 1.0);
		ry.setItem(2, 1, 0.0);

		ry.setItem(0, 2, -sinb);
		ry.setItem(1, 2, 0.0);
		ry.setItem(2, 2, cosb);
		// rz
		rz.setItem(0, 0, cosc);
		rz.setItem(1, 0, -sinc);
		rz.setItem(2, 0, 0.0);

		rz.setItem(0, 1, sinc);
		rz.setItem(1, 1, cosc);
		rz.setItem(2, 1, 0.0);

		rz.setItem(0, 2, 0.0);
		rz.setItem(1, 2, 0.0);
		rz.setItem(2, 2, 1.0);

		Matrix t = new Matrix(3, 3);
		rz.mMul(rx, t);
//		t.mMul(ry, rx);
		return t;
	}

	/*
	 *  rx            ry            rz
	 *  1   0   0     cb  0 -sb     cc  sc  0
	 *  0  ca  sa      0  1  0     -sc  cc  0
	 *  0 -sa  ca     sb  0  cb      0   0  1
	 *  
	 *	rz*rx  
	 *  cc		sc*ca		sc*sa
	 *  -sc		cc*ca		cc*sa
	 *  0		-sa			ca
	 *  
	 *  (rz*rx)*ry
	 *  cc*cb+sb*sc*sa		sc*sa		-sb*cc+cb*sc*sa
	 *  -sc*cb+sb*cc*sa		cc*ca		sc*sb+cb*cc*sa
	 *  ca*sb				-sa			ca*cb
	 */
	
	static Matrix makeAngles2(double a, double b, double c) {
		double sa = Math.sin(a);
		double ca = Math.cos(a);

		double sb = Math.sin(b);
		double cb = Math.cos(b);

		double sc = Math.sin(c);
		double cc = Math.cos(c);
		
		Matrix r = new Matrix(3, 3);
		
		r.setItem(0, 0, cc);
		r.setItem(1, 0, -sc);
		r.setItem(2, 0, 0);
		
		r.setItem(0, 1, sc*sa);
		r.setItem(1, 1, cc*ca);
		r.setItem(2, 1, -sa);
		
		r.setItem(0, 2, sc*sa);
		r.setItem(1, 2, cc*sa);
		r.setItem(2, 2, ca);
/*		
		r.setItem(0, 0, cc*cb+sb*sc*sa);
		r.setItem(1, 0, -sc*cb+sb*cc*sa);
		r.setItem(2, 0, ca*sb);
		
		r.setItem(0, 1, sc*sa);
		r.setItem(1, 1, cc*ca);
		r.setItem(2, 1, -sa);
		
		r.setItem(0, 2, -sb*cc+cb*sc*sa);
		r.setItem(1, 2, sc*sb+cb*cc*sa);
		r.setItem(2, 2, ca*cb);
*/		
		return r;
	}
	
	static final double deg2rad = Math.PI / 180;
	
	public static void main(String[] args) {
		double a = 10 * deg2rad; 
		double b = 20 * deg2rad; 
		double c = 30 * deg2rad;
		
		Matrix m = makeAngles(a, b, c);
		System.out.println(m);
		System.out.println();
		Matrix m2 = makeAngles2(a, b, c);
		System.out.println(m2);
	}
}
