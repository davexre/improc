package com.slavi.math;

import com.slavi.math.matrix.Matrix;

public class RotationXYZ {
	/**
	 * Return a rotation matrix R=mx*my*mz 
	 */
	public static Matrix makeAngles(double rx, double ry, double rz) {
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

		/*
		 * mx*my
		 * cb				0				-sb
		 * -sa*sb			ca				-sa*cb
		 * ca*sb			sa				ca*cb
		 * 
		 * (mx*my)*mz
		 * cc*cb			-cb*sc			-sb
		 * ca*sc-sa*sb*cc	ca*cc+sa*sb*sc	-sa*cb
		 * sa*sc+ca*sb*cc	sa*cc-ca*sb*sc	ca*cb
		 */
		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, cc*cb);
		r.setItem(1, 0, -cb*sc);
		r.setItem(2, 0, -sb);
		r.setItem(0, 1, ca*sc-sa*sb*cc);
		r.setItem(1, 1, ca*cc+sa*sb*sc);
		r.setItem(2, 1, -sa*cb);
		r.setItem(0, 2, sa*sc+ca*sb*cc);
		r.setItem(1, 2, sa*cc-ca*sb*sc);
		r.setItem(2, 2, ca*cb);
		return r;
	}

	/*
	 * (sinX)' = cosX
	 * (cosX)' = -sinX
	 */

	public static Matrix make_dF_dZ(double rx, double ry, double rz) {
		double sa = Math.sin(rx);
		double ca = Math.cos(rx);
		
		double sb = Math.sin(ry);
		double cb = Math.cos(ry);

		double sc = Math.sin(rz);
		double cc = Math.cos(rz);

		/*
		 * (mx*my)*mz
		 * cc*cb			-cb*sc			-sb
		 * ca*sc-sa*sb*cc	ca*cc+sa*sb*sc	-sa*cb
		 * sa*sc+ca*sb*cc	sa*cc-ca*sb*sc	ca*cb
		 *
		 * dF/d(rz)
		 * -sc*cb			-cb*cc			0
		 * ca*cc+sa*sb*sc	-ca*sc+sa*sb*cc	0
		 * sa*cc-ca*sb*sc	-sa*sc-ca*sb*cc	0
		 */
		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, -sc*cb);
		r.setItem(1, 0, -cb*cc);
		r.setItem(2, 0, 0);
		r.setItem(0, 1, ca*cc+sa*sb*sc);
		r.setItem(1, 1, -ca*sc+sa*sb*cc);
		r.setItem(2, 1, 0);
		r.setItem(0, 2, sa*cc-ca*sb*sc);
		r.setItem(1, 2, -sa*sc-ca*sb*cc);
		r.setItem(2, 2, 0);
		return null;
	}
	
	public static Matrix make_dF_dY(double rx, double ry, double rz) {
		double sa = Math.sin(rx);
		double ca = Math.cos(rx);
		
		double sb = Math.sin(ry);
		double cb = Math.cos(ry);

		double sc = Math.sin(rz);
		double cc = Math.cos(rz);

		/*
		 * (mx*my)*mz
		 * cc*cb			-cb*sc			-sb
		 * ca*sc-sa*sb*cc	ca*cc+sa*sb*sc	-sa*cb
		 * sa*sc+ca*sb*cc	sa*cc-ca*sb*sc	ca*cb
		 *
		 * dF/d(ry)
		 * -cc*sb			sb*sc			-cb
		 * -sa*cb*cc		sa*cb*sc		sa*sb
		 * ca*cb*cc			-ca*cb*sc		-ca*sb
		 */
		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, -cc*sb);
		r.setItem(1, 0, sb*sc);
		r.setItem(2, 0, -cb);
		r.setItem(0, 1, -sa*cb*cc);
		r.setItem(1, 1, sa*cb*sc);
		r.setItem(2, 1, sa*sb);
		r.setItem(0, 2, ca*cb*cc);
		r.setItem(1, 2, -ca*cb*sc);
		r.setItem(2, 2, -ca*sb);
		return null;
	}
	
	public static Matrix make_dF_dX(double rx, double ry, double rz) {
		double sa = Math.sin(rx);
		double ca = Math.cos(rx);
		
		double sb = Math.sin(ry);
		double cb = Math.cos(ry);

		double sc = Math.sin(rz);
		double cc = Math.cos(rz);

		/*
		 * (mx*my)*mz
		 * cc*cb			-cb*sc			-sb
		 * ca*sc-sa*sb*cc	ca*cc+sa*sb*sc	-sa*cb
		 * sa*sc+ca*sb*cc	sa*cc-ca*sb*sc	ca*cb
		 *
		 * dF/d(rx)
		 * 0				0				0
		 * -sa*sc-ca*sb*cc	-sa*cc+ca*sb*sc	-ca*cb
		 * ca*sc-sa*sb*cc	ca*cc+sa*sb*sc	-sa*cb
		 */
		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, 0);
		r.setItem(1, 0, 0);
		r.setItem(2, 0, 0);
		r.setItem(0, 1, -sa*sc-ca*sb*cc);
		r.setItem(1, 1, -sa*cc+ca*sb*sc);
		r.setItem(2, 1, -ca*cb);
		r.setItem(0, 2, ca*sc-sa*sb*cc);
		r.setItem(1, 2, ca*cc+sa*sb*sc);
		r.setItem(2, 2, -sa*cb);
		return null;
	}
}
