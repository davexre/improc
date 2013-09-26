package com.slavi.math;

import com.slavi.math.matrix.Matrix;

public class RotationXYZ implements Rotation3D {
	public static final RotationXYZ instance = new RotationXYZ();
	
	/**
	 * Return a rotation matrix R=R1*R2*R3 
	 */
	public Matrix makeAngles(double r1, double r2, double r3) {
		/*
		 *  R1            R2            R3
		 *  1   0   0     c2  0 -s2     c3 -s3  0
		 *  0  c1 -s1      0  1  0      s3  c3  0
		 *  0  s1  c1     s2  0  c2      0   0  1
		 */
		double s1 = Math.sin(r1);
		double c1 = Math.cos(r1);
		
		double s2 = Math.sin(r2);
		double c2 = Math.cos(r2);

		double s3 = Math.sin(r3);
		double c3 = Math.cos(r3);

		/*
		 * R1*R2
		 * c2				0				-s2
		 * -s1*s2			c1				-s1*c2
		 * c1*s2			s1				c1*c2
		 * 
		 * (R1*R2)*R3
		 * c3*c2			-c2*s3			-s2
		 * c1*s3-s1*s2*c3	c1*c3+s1*s2*s3	-s1*c2
		 * s1*s3+c1*s2*c3	s1*c3-c1*s2*s3	c1*c2
		 */
		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, c3*c2);
		r.setItem(1, 0, -c2*s3);
		r.setItem(2, 0, -s2);
		r.setItem(0, 1, c1*s3-s1*s2*c3);
		r.setItem(1, 1, c1*c3+s1*s2*s3);
		r.setItem(2, 1, -s1*c2);
		r.setItem(0, 2, s1*s3+c1*s2*c3);
		r.setItem(1, 2, s1*c3-c1*s2*s3);
		r.setItem(2, 2, c1*c2);
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
		 * (R1*R2)*R3
		 * c3*c2			-c2*s3			-s2
		 * c1*s3-s1*s2*c3	c1*c3+s1*s2*s3	-s1*c2
		 * s1*s3+c1*s2*c3	s1*c3-c1*s2*s3	c1*c2
		 */
		angles[0] = Math.atan2(-m.getItem(2, 1), m.getItem(2, 2));
		angles[1] = Math.asin(-m.getItem(2, 0));
		angles[2] = Math.atan2(-m.getItem(1, 0), m.getItem(0, 0));
	}	

	/**
	 * Extracts the rotation angles that constructed the REVERSE rotation matrix M.
	 * The angles are returned in the angles array as 
	 * angles[0] = r1,
	 * angles[1] = r2,
	 * angles[2] = r3
	 */
	public void getRotationAnglesBackword(Matrix m, double[] angles) {
		/*
		 * M_foreward = (R1*R2)*R3
		 * c3*c2			-c2*s3			-s2
		 * c1*s3-s1*s2*c3	c1*c3+s1*s2*s3	-s1*c2
		 * s1*s3+c1*s2*c3	s1*c3-c1*s2*s3	c1*c2
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
	 * angles[0] = r1,
	 * angles[1] = r2,
	 * angles[2] = r3
	 */
	public void getRotationAnglesBackword(double r1, double r2, double r3, double[] angles) {
		/*
		 * M_foreward = (R1*R2)*R3
		 * c3*c2			-c2*s3			-s2
		 * c1*s3-s1*s2*c3	c1*c3+s1*s2*s3	-s1*c2
		 * s1*s3+c1*s2*c3	s1*c3-c1*s2*s3	c1*c2
		 * 
		 * M_backword = Transpose(M_foreward)
		 */
		double s1 = Math.sin(r1);
		double c1 = Math.cos(r1);
		
		double s2 = Math.sin(r2);
		double c2 = Math.cos(r2);

		double s3 = Math.sin(r3);
		double c3 = Math.cos(r3);

		angles[0] = Math.atan2(c1*s2*s3-s1*c3, c1*c2);
		angles[1] = Math.asin(-(s1*s3+c1*s2*c3));
		angles[2] = Math.atan2(s1*s2*c3-c1*s3, c3*c2);
	}	

	/*
	 * (sinX)' = cosX
	 * (cosX)' = -sinX
	 */

	public Matrix make_dF_dR3(double r1, double r2, double r3) {
		double s1 = Math.sin(r1);
		double c1 = Math.cos(r1);
		
		double s2 = Math.sin(r2);
		double c2 = Math.cos(r2);

		double s3 = Math.sin(r3);
		double c3 = Math.cos(r3);

		/*
		 * (R1*R2)*R3
		 * c3*c2			-c2*s3			-s2
		 * c1*s3-s1*s2*c3	c1*c3+s1*s2*s3	-s1*c2
		 * s1*s3+c1*s2*c3	s1*c3-c1*s2*s3	c1*c2
		 *
		 * dF/d(r3)
		 * -s3*c2			-c2*c3			0
		 * c1*c3+s1*s2*s3	-c1*s3+s1*s2*c3	0
		 * s1*c3-c1*s2*s3	-s1*s3-c1*s2*c3	0
		 */
		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, -s3*c2);
		r.setItem(1, 0, -c2*c3);
		r.setItem(2, 0, 0);
		r.setItem(0, 1, c1*c3+s1*s2*s3);
		r.setItem(1, 1, -c1*s3+s1*s2*c3);
		r.setItem(2, 1, 0);
		r.setItem(0, 2, s1*c3-c1*s2*s3);
		r.setItem(1, 2, -s1*s3-c1*s2*c3);
		r.setItem(2, 2, 0);
		return r;
	}
	
	public Matrix make_dF_dR2(double r1, double r2, double r3) {
		double s1 = Math.sin(r1);
		double c1 = Math.cos(r1);
		
		double s2 = Math.sin(r2);
		double c2 = Math.cos(r2);

		double s3 = Math.sin(r3);
		double c3 = Math.cos(r3);

		/*
		 * (R1*R2)*R3
		 * c3*c2			-c2*s3			-s2
		 * c1*s3-s1*s2*c3	c1*c3+s1*s2*s3	-s1*c2
		 * s1*s3+c1*s2*c3	s1*c3-c1*s2*s3	c1*c2
		 *
		 * dF/d(r2)
		 * -c3*s2			s2*s3			-c2
		 * -s1*c2*c3		s1*c2*s3		s1*s2
		 * c1*c2*c3			-c1*c2*s3		-c1*s2
		 */
		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, -c3*s2);
		r.setItem(1, 0, s2*s3);
		r.setItem(2, 0, -c2);
		r.setItem(0, 1, -s1*c2*c3);
		r.setItem(1, 1, s1*c2*s3);
		r.setItem(2, 1, s1*s2);
		r.setItem(0, 2, c1*c2*c3);
		r.setItem(1, 2, -c1*c2*s3);
		r.setItem(2, 2, -c1*s2);
		return r;
	}
	
	public Matrix make_dF_dR1(double r1, double r2, double r3) {
		double s1 = Math.sin(r1);
		double c1 = Math.cos(r1);
		
		double s2 = Math.sin(r2);
		double c2 = Math.cos(r2);

		double s3 = Math.sin(r3);
		double c3 = Math.cos(r3);

		/*
		 * (R1*R2)*R3
		 * c3*c2			-c2*s3			-s2
		 * c1*s3-s1*s2*c3	c1*c3+s1*s2*s3	-s1*c2
		 * s1*s3+c1*s2*c3	s1*c3-c1*s2*s3	c1*c2
		 *
		 * dF/d(r1)
		 * 0				0				0
		 * -s1*s3-c1*s2*c3	-s1*c3+c1*s2*s3	-c1*c2
		 * c1*s3-s1*s2*c3	c1*c3+s1*s2*s3	-s1*c2
		 */
		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, 0);
		r.setItem(1, 0, 0);
		r.setItem(2, 0, 0);
		r.setItem(0, 1, -s1*s3-c1*s2*c3);
		r.setItem(1, 1, -s1*c3+c1*s2*s3);
		r.setItem(2, 1, -c1*c2);
		r.setItem(0, 2, c1*s3-s1*s2*c3);
		r.setItem(1, 2, c1*c3+s1*s2*s3);
		r.setItem(2, 2, -s1*c2);
		return r;
	}
}
