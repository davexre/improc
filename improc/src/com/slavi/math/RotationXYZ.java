package com.slavi.math;

import com.slavi.math.matrix.Matrix;

public class RotationXYZ {
	public static final RotationXYZ instance = new RotationXYZ();
	
	/**
	 * Return a rotation matrix R=mx*my*mz 
	 */
	public Matrix makeAngles(double rx, double ry, double rz) {
		/*
		 *  mx            my            mz
		 *  1   0   0     cb  0 -sb     cc -sc  0
		 *  0  ca -sa      0  1  0      sc  cc  0
		 *  0  sa  ca     sb  0  cb      0   0  1
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

	public Matrix makeAngles(double angles[]) {
		return makeAngles(angles[0], angles[1], angles[2]);
	}
	
	/**
	 * Transforms coordinates in source coordinate system into target coord system.
	 * To obtain a rotation matrix use {@link #makeAngles(double, double, double)}.
	 * The transformation is done as:
	 * <code>
	 * P = [x; y; z]
	 * DEST = ROT * P
	 * </code>
	 */
	public void transformForward(Matrix rot,
			double x, double y, double z, double dest[]) {
		dest[0] = x * rot.getItem(0, 0) + y * rot.getItem(1, 0) + z * rot.getItem(2, 0);
		dest[1] = x * rot.getItem(0, 1) + y * rot.getItem(1, 1) + z * rot.getItem(2, 1);
		dest[2] = x * rot.getItem(0, 2) + y * rot.getItem(1, 2) + z * rot.getItem(2, 2);
	}

	public void transformForward(Matrix rot, double src[], double dest[]) {
		transformForward(rot, src[0], src[1], src[2], dest);
	}
	
	/**
	 * Transforms coordinates in target coordinate system into source coord system.
	 * To obtain a rotation matrix use {@link #makeAngles(double, double, double)}.
	 * The transformation is done as:
	 * <code>
	 * Since:
	 * Inverse(ROT) = Transpose(ROT)
	 * P1 = [x; y; z]
	 * DEST = Transpose(ROT) * P1
	 * </code>
	 */
	public void transformBackward(Matrix rot,
			double x, double y, double z, double dest[]) {
		dest[0] = x * rot.getItem(0, 0) + y * rot.getItem(0, 1) + z * rot.getItem(0, 2);
		dest[1] = x * rot.getItem(1, 0) + y * rot.getItem(1, 1) + z * rot.getItem(1, 2);
		dest[2] = x * rot.getItem(2, 0) + y * rot.getItem(2, 1) + z * rot.getItem(2, 2);
	}
	
	public void transformBackward(Matrix rot, double src[], double dest[]) {
		transformBackward(rot, src[0], src[1], src[2], dest);
	}
	
	/**
	 * Extracts the rotation angles that constructed the rotation matrix M.
	 * The angles are returned in the angles array as 
	 * angles[0] = rx,
	 * angles[1] = ry,
	 * angles[2] = rz
	 */
	public void getRotationAngles(Matrix m, double[] angles) {
		/*
		 * (mx*my)*mz
		 * cc*cb			-cb*sc			-sb
		 * ca*sc-sa*sb*cc	ca*cc+sa*sb*sc	-sa*cb
		 * sa*sc+ca*sb*cc	sa*cc-ca*sb*sc	ca*cb
		 */
		angles[0] = Math.atan2(-m.getItem(2, 1), m.getItem(2, 2));
		angles[1] = Math.asin(-m.getItem(2, 0));
		angles[2] = Math.atan2(-m.getItem(1, 0), m.getItem(0, 0));
	}	

	/**
	 * Extracts the rotation angles that constructed the REVERSE rotation matrix M.
	 * The angles are returned in the angles array as 
	 * angles[0] = rx,
	 * angles[1] = ry,
	 * angles[2] = rz
	 */
	public void getRotationAnglesBackword(Matrix m, double[] angles) {
		/*
		 * M_foreward = (mx*my)*mz
		 * cc*cb			-cb*sc			-sb
		 * ca*sc-sa*sb*cc	ca*cc+sa*sb*sc	-sa*cb
		 * sa*sc+ca*sb*cc	sa*cc-ca*sb*sc	ca*cb
		 * 
		 * M_backword = Transpose(M_foreward)
		 */
		angles[0] = Math.atan2(-m.getItem(1, 2), m.getItem(2, 2));
		angles[1] = Math.asin(-m.getItem(0, 2));
		angles[2] = Math.atan2(-m.getItem(0, 1), m.getItem(0, 0));
	}	
	
	/**
	 * Computes the REVERSE rotation angles that constructed the REVERSE rotation matrix M.
	 * The angles are returned in the angles array as 
	 * angles[0] = rx,
	 * angles[1] = ry,
	 * angles[2] = rz
	 */
	public void getRotationAnglesBackword(double rx, double ry, double rz, double[] angles) {
		/*
		 * M_foreward = (mx*my)*mz
		 * cc*cb			-cb*sc			-sb
		 * ca*sc-sa*sb*cc	ca*cc+sa*sb*sc	-sa*cb
		 * sa*sc+ca*sb*cc	sa*cc-ca*sb*sc	ca*cb
		 * 
		 * M_backword = Transpose(M_foreward)
		 */
		double sa = Math.sin(rx);
		double ca = Math.cos(rx);
		
		double sb = Math.sin(ry);
		double cb = Math.cos(ry);

		double sc = Math.sin(rz);
		double cc = Math.cos(rz);

		angles[0] = Math.atan2(ca*sb*sc-sa*cc, ca*cb);
		angles[1] = Math.asin(-(sa*sc+ca*sb*cc));
		angles[2] = Math.atan2(sa*sb*cc-ca*sc, cc*cb);
	}	

	/*
	 * (sinX)' = cosX
	 * (cosX)' = -sinX
	 */

	public Matrix make_dF_dZ(double rx, double ry, double rz) {
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
	
	public Matrix make_dF_dY(double rx, double ry, double rz) {
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
	
	public Matrix make_dF_dX(double rx, double ry, double rz) {
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
}
