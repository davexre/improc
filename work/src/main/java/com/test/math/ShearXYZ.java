package com.test.math;

import com.slavi.math.matrix.Matrix;

public class ShearXYZ {

	/*
	 * R1 the angle between Y and Z axis (measured starting from Y)
	 * R2 the angle between Z and X axis (measured starting from Z)
	 * R3 the angle between X and Y axis (measured starting from X)
	 * ROT the rotation of the millimeter paper (in XY plane)
	 * 
	 * No skew angles R1=R2=R3=pi/2
	 * 
	 * sin(R1)=s1; sin(R2)=s2; sin(R3)=c3; sin(ROT)=sr
	 * cos(R1)=c1; cos(R2)=c2; cos(R3)=c3; cos(ROT)=cr;
	 * 
	 * R1: (skew around X)	z1=z+y*cos(R1);	y1=y*sin(R1)
	 * R2: (skew around Y)	x2=x+z*cos(R2);	z2=z*sin(R2)
	 * R3: (skew around Z)	x3=x+y*cos(R3);	y3=y*sin(R3)
	 * 
	 * R1           R2           R3
	 * 1   0  0     1  0  c2     1  c3  0
	 * 0  s1  0     0  1   0     0  s3  0
	 * 0  c1  1     0  0  s2     0   0  1
	 * 
	 * R1 * R2
	 * 1	0	c2
	 * 0	s1	0
	 * 0	c1	s2
	 * 
	 * (R1 * R2) * R3
	 * 1	c3		c2
	 * 0	s1*s3	0
	 * 0	c1*s3	s2
	 * 
	 * ROT
	 * cr -sr  0
	 * sr  cr  0
	 *  0   0  1
	 *  
	 * ROT * (R1 * R2 * R3)
	 * cr	cr*c3-sr*s1*s3		cr*c2
	 * sr	sr*c3+cr*s1*s3		sr*c2
	 * 0	c1*s3				s2
	 */
	public Matrix makeAngles(double r1, double r2, double r3, double rot) {
		double c1 = Math.cos(r1);
		double c2 = Math.cos(r2);
		double c3 = Math.cos(r3);
		double cr = Math.cos(rot);
		double s1 = Math.sin(r1);
		double s2 = Math.sin(r2);
		double s3 = Math.sin(r3);
		double sr = Math.sin(rot);

		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, cr);
		r.setItem(1, 0, cr*c3-sr*s1*s3);
		r.setItem(2, 0, cr*c2);
		r.setItem(0, 1, sr);
		r.setItem(1, 1, sr*c3+cr*s1*s3);
		r.setItem(2, 1, sr*c2);
		r.setItem(0, 2, 0);
		r.setItem(1, 2, c1*s3);
		r.setItem(2, 2, s2);
		return r;
	}
	
	public Matrix make_dF_dROT(double r1, double r2, double r3, double rot) {
//		double c1 = Math.cos(r1);
		double c2 = Math.cos(r2);
		double c3 = Math.cos(r3);
		double cr = Math.cos(rot);
		double s1 = Math.sin(r1);
//		double s2 = Math.sin(r2);
		double s3 = Math.sin(r3);
		double sr = Math.sin(rot);

		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, -sr);
		r.setItem(1, 0, -sr*c3-cr*s1*s3);
		r.setItem(2, 0, -sr*c2);
		r.setItem(0, 1, cr);
		r.setItem(1, 1, cr*c3-sr*s1*s3);
		r.setItem(2, 1, cr*c2);
		r.setItem(0, 2, 0);
		r.setItem(1, 2, 0);
		r.setItem(2, 2, 0);
		return r;
	}

	public Matrix make_dF_dR3(double r1, double r2, double r3, double rot) {
		double c1 = Math.cos(r1);
//		double c2 = Math.cos(r2);
		double c3 = Math.cos(r3);
		double cr = Math.cos(rot);
		double s1 = Math.sin(r1);
//		double s2 = Math.sin(r2);
		double s3 = Math.sin(r3);
		double sr = Math.sin(rot);

		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, 0);
		r.setItem(1, 0, -cr*s3-sr*s1*c3);
		r.setItem(2, 0, 0);
		r.setItem(0, 1, 0);
		r.setItem(1, 1, -sr*s3+cr*s1*c3);
		r.setItem(2, 1, 0);
		r.setItem(0, 2, 0);
		r.setItem(1, 2, c1*c3);
		r.setItem(2, 2, 0);
		return r;
	}

	public Matrix make_dF_dR2(double r1, double r2, double r3, double rot) {
//		double c1 = Math.cos(r1);
		double c2 = Math.cos(r2);
//		double c3 = Math.cos(r3);
		double cr = Math.cos(rot);
//		double s1 = Math.sin(r1);
		double s2 = Math.sin(r2);
//		double s3 = Math.sin(r3);
		double sr = Math.sin(rot);

		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, 0);
		r.setItem(1, 0, 0);
		r.setItem(2, 0, -cr*s2);
		r.setItem(0, 1, 0);
		r.setItem(1, 1, 0);
		r.setItem(2, 1, -sr*s2);
		r.setItem(0, 2, 0);
		r.setItem(1, 2, 0);
		r.setItem(2, 2, c2);
		return r;
	}

	public Matrix make_dF_dR1(double r1, double r2, double r3, double rot) {
		double c1 = Math.cos(r1);
//		double c2 = Math.cos(r2);
//		double c3 = Math.cos(r3);
		double cr = Math.cos(rot);
		double s1 = Math.sin(r1);
//		double s2 = Math.sin(r2);
		double s3 = Math.sin(r3);
		double sr = Math.sin(rot);

		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, 0);
		r.setItem(1, 0, -sr*c1*s3);
		r.setItem(2, 0, 0);
		r.setItem(0, 1, 0);
		r.setItem(1, 1, cr*c1*s3);
		r.setItem(2, 1, 0);
		r.setItem(0, 2, 0);
		r.setItem(1, 2, -s1*s3);
		r.setItem(2, 2, 0);
		return r;
	}

	public void transformForward(Matrix rot,
			double x, double y, double z, double dest[]) {
		dest[0] = x * rot.getItem(0, 0) + y * rot.getItem(1, 0) + z * rot.getItem(2, 0);
		dest[1] = x * rot.getItem(0, 1) + y * rot.getItem(1, 1) + z * rot.getItem(2, 1);
		dest[2] = x * rot.getItem(0, 2) + y * rot.getItem(1, 2) + z * rot.getItem(2, 2);
	}

	public void transformForward(Matrix rot, double src[], double dest[]) {
		transformForward(rot, src[0], src[1], src[2], dest);
	}
}
