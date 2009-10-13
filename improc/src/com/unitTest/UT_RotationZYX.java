package com.unitTest;

import com.slavi.improc.myadjust.MyPanoPairTransformerZYX;
import com.slavi.math.MathUtil;
import com.slavi.math.RotationXYZ;
import com.slavi.math.RotationZYX;
import com.slavi.math.matrix.Matrix;

public class UT_RotationZYX {

	RotationXYZ rot1 = RotationXYZ.instance;
	RotationZYX rot = RotationZYX.instance;
	
	double angles[] = { 10 * MathUtil.deg2rad, 20 * MathUtil.deg2rad, 30 * MathUtil.deg2rad };
	double point[] = { 5, 15, 25 };

	double tmp1[] = new double[3];
	double tmp2[] = new double[3];
	double tmp3[] = new double[3];

	double tmp4[] = new double[3];
	double tmp5[] = new double[3];
	double tmp6[] = new double[3];
	
	public static void dumpPoint3D(double p[]) {
		System.out.println(MathUtil.d4(p[0]) + "\t" + MathUtil.d4(p[1]) + "\t" + MathUtil.d4(p[2]));
	}

	static final double precision = 10000;
	public static boolean equal(double a, double b) {
		return (int)(a * precision) == (int)(b * precision);
	}
	
	public static void assertEqualPoint3D(double p1[], double p2[]) {
		if (equal(p1[0], p2[0]) &&
			equal(p1[1], p2[1]) &&
			equal(p1[2], p2[2]))
			return;
		dumpPoint3D(p1);
		dumpPoint3D(p2);
		throw new RuntimeException("Failed");
	}
	
	void testTransform() {
		Matrix r = rot.makeAngles(angles);
		rot.transformForward(r, point[0], point[1], point[2], tmp1);
		rot.transformBackward(r, tmp1[0], tmp1[1], tmp1[2], tmp2);
		assertEqualPoint3D(point, tmp2);
	}
	
	void testGetRotationAngles() {
		Matrix r = rot.makeAngles(angles);
		rot.getRotationAngles(r, tmp1);
		assertEqualPoint3D(angles, tmp1);
	}
	
	void testGetRotationAnglesBackword() {
		Matrix r = rot.makeAngles(angles);
		rot.transformForward(r, point[0], point[1], point[2], tmp1);
		rot.getRotationAnglesBackword(r, tmp2);
		Matrix back = rot.makeAngles(tmp2);
		rot.transformForward(back, tmp1[0], tmp1[1], tmp1[2], tmp3);
		assertEqualPoint3D(point, tmp3);
		
		rot.getRotationAnglesBackword(angles[0], angles[1], angles[2], tmp3);
		assertEqualPoint3D(tmp2, tmp3);
		
		// check identity
		Matrix tmpM = new Matrix();
		r.mMul(back, tmpM);
		if (!tmpM.isE(1E-15))
			throw new RuntimeException("Failed");
	}
	
	void test_dF() {
		Matrix r = rot.makeAngles(angles);
		rot.transformForward(r, point, tmp1);
		double delta = 0.000001 * MathUtil.deg2rad;

		Matrix dF[] = {
				rot.make_dF_dX(angles[0], angles[1], angles[2]), 
				rot.make_dF_dY(angles[0], angles[1], angles[2]), 
				rot.make_dF_dZ(angles[0], angles[1], angles[2])
		};
		
		for (int dindex = 0; dindex < 3; dindex++) {
			double d[] = { 0, 0, 0 };
			d[dindex] = delta;
			rot.transformForward(dF[dindex], point, tmp2);
			for (int i = 0; i < 3; i++) {
				tmp2[i] = tmp1[i] + tmp2[i] * delta;
			}
			double d2[] = point.clone();
			d2[dindex] += delta;
			rot.transformForward(r, d2, tmp3);
			assertEqualPoint3D(tmp3, tmp2);
		}
	}

	void testPolar() {
		MyPanoPairTransformerZYX.cartesianToPolar(point[0], point[1], point[2], tmp1);
		MyPanoPairTransformerZYX.polarToCartesian(tmp1[0], tmp1[1], tmp1[2], tmp2);
		assertEqualPoint3D(point, tmp2);
	}
	
	public static void main(String[] args) {
		UT_RotationZYX test = new UT_RotationZYX();
		test.testTransform();
		test.testGetRotationAngles();
		test.testGetRotationAnglesBackword();
		test.test_dF();
		test.testPolar();
		System.out.println("Done.");
	}
}
