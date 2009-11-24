package com.unitTest;

import com.slavi.math.MathUtil;
import com.slavi.math.RotationXYZ;
import com.slavi.math.RotationZXZ;
import com.slavi.math.RotationZYX;
import com.slavi.math.RotationZYZ;
import com.slavi.math.SphericalCoordsLongLat;
import com.slavi.math.matrix.Matrix;

public class UT_RotationZYX {

	RotationXYZ rot1 = RotationXYZ.instance;
	RotationZYX rot2 = RotationZYX.instance;
	RotationZYZ rot = RotationZYZ.instance;
	RotationZXZ rot4 = RotationZXZ.instance;
	
	double angles[] = { 10 * MathUtil.deg2rad, 20 * MathUtil.deg2rad, 30 * MathUtil.deg2rad };
	double point[] = { 5, 15, 25 };

	double tmp1[] = new double[3];
	double tmp2[] = new double[3];
	double tmp3[] = new double[3];

	double tmp4[] = new double[3];
	double tmp5[] = new double[3];
	double tmp6[] = new double[3];
	
	void testTransform() {
		Matrix r = rot.makeAngles(angles);
		rot.transformForward(r, point[0], point[1], point[2], tmp1);
		rot.transformBackward(r, tmp1[0], tmp1[1], tmp1[2], tmp2);
		TestUtils.assertEqual("", point, tmp2);
	}
	
	void testGetRotationAngles() {
		Matrix r = rot.makeAngles(angles);
		rot.getRotationAngles(r, tmp1);
		TestUtils.assertEqual("", angles, tmp1);
	}
	
	void testGetRotationAnglesBackword() {
		Matrix r = rot.makeAngles(angles);
		rot.transformForward(r, point[0], point[1], point[2], tmp1);
		rot.getRotationAnglesBackword(r, tmp2);
		Matrix back = rot.makeAngles(tmp2);
		rot.transformForward(back, tmp1[0], tmp1[1], tmp1[2], tmp3);
		TestUtils.assertEqual("", point, tmp3);
		
		rot.getRotationAnglesBackword(angles[0], angles[1], angles[2], tmp3);
		TestUtils.assertEqual("", tmp2, tmp3);
		
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
				rot.make_dF_dR1(angles[0], angles[1], angles[2]), 
				rot.make_dF_dR2(angles[0], angles[1], angles[2]), 
				rot.make_dF_dR3(angles[0], angles[1], angles[2])
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
			TestUtils.assertEqual("", tmp3, tmp2);
		}
	}

	void testPolar() {
		SphericalCoordsLongLat.cartesianToPolar(point[0], point[1], point[2], tmp1);
		SphericalCoordsLongLat.polarToCartesian(tmp1[0], tmp1[1], tmp1[2], tmp2);
		TestUtils.assertEqual("", point, tmp2);
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
