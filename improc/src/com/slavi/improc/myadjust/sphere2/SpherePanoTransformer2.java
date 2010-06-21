package com.slavi.improc.myadjust.sphere2;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.math.MathUtil;
import com.slavi.math.RotationZYZ;
import com.slavi.math.SphericalCoordsLongZen;
import com.slavi.math.matrix.Matrix;
import com.unitTest.TestUtils;

public class SpherePanoTransformer2 {

	public static void main(String[] args) {
		{
			double dest[] = new double[2];
			double rot[] = new double[] { 10 * MathUtil.deg2rad, 12 * MathUtil.deg2rad, 0 * MathUtil.deg2rad };
			double p[] = new double[] { 10 * MathUtil.deg2rad, 12 * MathUtil.deg2rad };
			SphericalCoordsLongZen.rotateForeward(p[0], p[1], rot[0], rot[1], rot[2], dest);
			System.out.println(dest[0]);
			System.out.println(dest[1]);
			System.out.println(MathUtil.rad2degStr(dest[0]));
			System.out.println(MathUtil.rad2degStr(dest[1]));
		}
		
		KeyPointList kpl1 = new KeyPointList();
		kpl1.cameraOriginX = 1001;
		kpl1.cameraOriginY = 2002;
		kpl1.cameraScale = 1.0 / (2.0 * Math.max(kpl1.cameraOriginX, kpl1.cameraOriginY));
		kpl1.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;
		kpl1.sphereRZ1 = 10 * MathUtil.deg2rad;
		kpl1.sphereRY = 20 * MathUtil.deg2rad;
		kpl1.sphereRZ2 = 30 * MathUtil.deg2rad;

		KeyPoint p1 = new KeyPoint();
		p1.keyPointList = kpl1;
		p1.doubleX = p1.keyPointList.cameraOriginX;
		p1.doubleY = p1.keyPointList.cameraOriginY + 1000;

		double dest[] = new double[2];
		double dest2[] = new double[2];
		SphereNorm2.transformForeward(p1.doubleX, p1.doubleY, kpl1, dest);
		SphereNorm2.transformBackward(dest[0], dest[1], kpl1, dest2);
		TestUtils.assertEqual("", dest2[0], p1.doubleX);
		TestUtils.assertEqual("", dest2[1], p1.doubleY);
		
		Matrix m = RotationZYZ.instance.makeAngles(kpl1.sphereRZ1, kpl1.sphereRY, kpl1.sphereRZ2);
		double d1[] = new double[3];
		double d2[] = new double[3];

		d1[0] = (p1.doubleY - p1.keyPointList.cameraOriginY) * p1.keyPointList.cameraScale;
		d1[1] = (p1.doubleX - p1.keyPointList.cameraOriginX) * p1.keyPointList.cameraScale;
		d1[2] = p1.keyPointList.scaleZ;
		double r1 = Math.sqrt(d1[0] * d1[0] + d1[1] * d1[1] + d1[2] * d1[2]);
		System.out.println("r1=" + r1);
		RotationZYZ.instance.transformForward(m, d1, d2);
		SphericalCoordsLongZen.cartesianToPolar(d2[0], d2[1], d2[2], d2);
		System.out.println("r =" + d2[2]);
		TestUtils.assertEqualAngle("", dest[0], d2[0]);
		TestUtils.assertEqualAngle("", dest[1], d2[1]);

		SphereNorm2.transformForeward(dest2[0], dest2[1], kpl1, dest2);
		TestUtils.assertEqualAngle("", dest2[0], dest[0]);
		TestUtils.assertEqualAngle("", dest2[1], dest[1]);
	}
}
