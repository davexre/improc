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
		return r;
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
		return r;
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
		return r;
	}
	
	public static void dumpTestForRotationMatrix(Matrix m) {
		Matrix sumX = new Matrix(3, 1);
		for (int i = 0; i<3; i++)
			sumX.setItem(i, 0, 
				Math.pow(m.getItem(0, i), 2) + 
				Math.pow(m.getItem(1, i), 2) + 
				Math.pow(m.getItem(2, i), 2));
		sumX.printM("sum(X*X)=1");

		Matrix sumY = new Matrix(1, 3);
		for (int i = 0; i<3; i++)
			sumY.setItem(0, i, 
				Math.pow(m.getItem(i, 0), 2) + 
				Math.pow(m.getItem(i, 1), 2) + 
				Math.pow(m.getItem(i, 2), 2));
		sumY.printM("sum(Y*Y)=1");

		Matrix sum = new Matrix(3, 1);
		for (int i = 0; i < 3; i++) {
			int i1 = (i + 1) % 3;
			sum.setItem(i, 0,   
				m.getItem(0, i) * m.getItem(0, i1) +
				m.getItem(1, i) * m.getItem(1, i1) +
				m.getItem(2, i) * m.getItem(2, i1));
		}
		sum.printM("sum(X1*X2)=0");

		Matrix sum2 = new Matrix(1, 3);
		for (int i = 0; i < 3; i++) {
			int i1 = (i + 1) % 3;
			sum2.setItem(0, i,   
				m.getItem(i, 0) * m.getItem(i1, 0) +
				m.getItem(i, 1) * m.getItem(i1, 1) +
				m.getItem(i, 2) * m.getItem(i1, 2));
		}
		sum2.printM("sum(Y1*Y2)=0");
	}
	
	public static void main(String[] args) {
		Matrix m = makeAngles(10 * MathUtil.deg2rad, 20 * MathUtil.deg2rad, 30 * MathUtil.deg2rad);
		dumpTestForRotationMatrix(m);
	}
/*	
	public static void main(String[] args) {
		double rx = 1;
		double ry = 2;
		double rz = 3;
		
		double sa = Math.sin(rx);
		double ca = Math.cos(rx);
		
		double sb = Math.sin(ry);
		double cb = Math.cos(ry);

		double sc = Math.sin(rz);
		double cc = Math.cos(rz);

		Matrix m = make_dF_dZ(rx, ry, rz);
		Matrix x = new Matrix(3, 3);
		Matrix y = new Matrix(3, 3);
		Matrix z = new Matrix(3, 3);
		Matrix tmp1 = new Matrix(3, 3);
		Matrix tmp2 = new Matrix(3, 3);
		
		x.setItem(0, 0, 1);
		x.setItem(1, 0, 0);
		x.setItem(2, 0, 0);
		x.setItem(0, 1, 0);
		x.setItem(1, 1, ca);
		x.setItem(2, 1, -sa);
		x.setItem(0, 2, 0);
		x.setItem(1, 2, sa);
		x.setItem(2, 2, ca);
		
		y.setItem(0, 0, cb);
		y.setItem(1, 0, 0);
		y.setItem(2, 0, -sb);
		y.setItem(0, 1, 0);
		y.setItem(1, 1, 1);
		y.setItem(2, 1, 0);
		y.setItem(0, 2, sb);
		y.setItem(1, 2, 0);
		y.setItem(2, 2, cb);

		// make_dF_dY
//		y.setItem(0, 0, -sb);
//		y.setItem(1, 0, 0);
//		y.setItem(2, 0, -cb);
//		y.setItem(0, 1, 0);
//		y.setItem(1, 1, 0);
//		y.setItem(2, 1, 0);
//		y.setItem(0, 2, cb);
//		y.setItem(1, 2, 0);
//		y.setItem(2, 2, -sb);
		
//		z.setItem(0, 0, cc);
//		z.setItem(1, 0, -sc);
//		z.setItem(2, 0, 0);
//		z.setItem(0, 1, sc);
//		z.setItem(1, 1, cc);
//		z.setItem(2, 1, 0);
//		z.setItem(0, 2, 0);
//		z.setItem(1, 2, 0);
//		z.setItem(2, 2, 1);
		
		// make_dF_dZ
		z.setItem(0, 0, -sc);
		z.setItem(1, 0, -cc);
		z.setItem(2, 0, 0);
		z.setItem(0, 1, cc);
		z.setItem(1, 1, -sc);
		z.setItem(2, 1, 0);
		z.setItem(0, 2, 0);
		z.setItem(1, 2, 0);
		z.setItem(2, 2, 0);
		
		x.mMul(y, tmp1);
		tmp1.mMul(z, tmp2);
		tmp2.mSub(m, tmp1);
		tmp1.printM("tmp1");
	}*/
}
