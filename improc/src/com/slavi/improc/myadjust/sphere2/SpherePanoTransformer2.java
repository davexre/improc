package com.slavi.improc.myadjust.sphere2;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.math.MathUtil;
import com.slavi.math.RotationZYZ;
import com.slavi.math.SphericalCoordsLongZen;
import com.slavi.math.matrix.Matrix;
import com.unitTest.TestUtils;

public class SpherePanoTransformer2 {

	/**
	 * Transforms from source image coordinate system into world coord.system.
	 * @param sx, sy	Coordinates in pixels of the source image with origin pixel(0,0)
	 * @param dest		The transformed coordinates in radians. Longitude is 
	 * 					returned in dest[0] and is in the range (-pi; pi] and Latitude
	 * 					is returned in dest[1] in the range [-pi/2; pi/2].   
	 */
	public static void transformForeward(double sx, double sy, KeyPointList srcImage, double dest[]) {
		sx = (sx - srcImage.cameraOriginX) * srcImage.cameraScale;
		sy = (sy - srcImage.cameraOriginY) * srcImage.cameraScale;
		double f = srcImage.scaleZ;
		// x => longitude, y => zenith
		double x = Math.atan2(sy, sx);
		double r = Math.sqrt(sx * sx + sy * sy + f * f);
		double y = Math.acos(f / r);
		rotateForeward(x, y, srcImage.sphereRZ1, srcImage.sphereRY, srcImage.sphereRZ2, dest);
	}

	public static void main(String[] args) {
		{
			double dest[] = new double[2];
			double rot[] = new double[] { 10 * MathUtil.deg2rad, 12 * MathUtil.deg2rad, 0 * MathUtil.deg2rad };
			double p[] = new double[] { 10 * MathUtil.deg2rad, 12 * MathUtil.deg2rad };
			rotateForeward(p[0], p[1], rot[0], rot[1], rot[2], dest);
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
		SpherePanoTransformer2.transformForeward(p1.doubleX, p1.doubleY, kpl1, dest);
		SpherePanoTransformer2.transformBackward(dest[0], dest[1], kpl1, dest2);
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
		cartesianToPolar(d2[0], d2[1], d2[2], d2);
		System.out.println("r =" + d2[2]);
		TestUtils.assertEqualAngle("", dest[0], d2[0]);
		TestUtils.assertEqualAngle("", dest[1], d2[1]);

		SpherePanoTransformer2.transformForeward(dest2[0], dest2[1], kpl1, dest2);
		TestUtils.assertEqualAngle("", dest2[0], dest[0]);
		TestUtils.assertEqualAngle("", dest2[1], dest[1]);
	}
	
	/**
	 * sx -> longitude
	 * sy -> zenith angle (90 - latitude)
	 * r -> radius 
	 * dest[0] = x
	 * dest[1] = y
	 * dest[2] = z
	 */
	public static void polarToCartesian(double sx, double sy, double r, double dest[]) {
		double RsinSY = r * Math.sin(sy);
		dest[0] = Math.cos(sx) * RsinSY;
		dest[1] = Math.sin(sx) * RsinSY;
		dest[2] = r * Math.cos(sy);
	}
	
	/**
	 * dest[0] = sx -> longitude
	 * dest[1] = sy -> zenith angle (90 - latitude)
	 * dest[2] = r -> radius 
	 */
	public static void cartesianToPolar(double x, double y, double z, double dest[]) {
		dest[0] = Math.atan2(y, x);
		dest[2] = Math.sqrt(x*x + y*y + z*z);
		dest[1] = dest[2] == 0.0 ? 0.0 : Math.acos(z / dest[2]);
	}
	
	/**
	 * sx -> longitude
	 * sy -> zenith angle (90 - latitude) 
	 */
	public static void rotateForeward(double sx, double sy, double IZ1, double IY, double IZ2, double dest[]) {
		sx -= IZ1;
		double sinDX = Math.sin(sx);
		double cosDX = Math.cos(sx);
		double sinIY = Math.sin(IY);
		double cosIY = Math.cos(IY);
		double sinSY = Math.sin(sy);
		double cosSY = Math.cos(sy);

		dest[0] = MathUtil.fixAngle2PI(Math.atan2(sinDX * sinSY, cosDX * cosIY * sinSY - sinIY * cosSY) - IZ2);
		double d = cosSY * cosIY + sinSY * sinIY * cosDX;
		if (d > 1.0)
			d = 1.0; // this might happen (ex. sx == IZ1 == 12 * MathUtil.deg2rad)
		dest[1] = Math.acos(d);
	}

	/**
	 * dest[0] = x in image coordinates
	 * dest[1] = y in image coordinates
	 * Returns the radius if r>0 coordinates are ok, if r<0 coordinates the on the opposite side of the sphere.  
	 */
	public static double transformBackward(double rx, double ry, KeyPointList srcImage, double dest[]) {
		rotateBackward(rx, ry, srcImage.sphereRZ1, srcImage.sphereRY, srcImage.sphereRZ2, dest);
		// x => longitude, y => zenith
		double r = srcImage.scaleZ * Math.tan(dest[1]);
		dest[1] = srcImage.cameraOriginY + r * Math.sin(dest[0]) / srcImage.cameraScale;
		dest[0] = srcImage.cameraOriginX + r * Math.cos(dest[0]) / srcImage.cameraScale;
		return r;
	}

	/**
	 * rx -> longitude
	 * ry -> zenith angle (90 - latitude) 
	 */
	public static void rotateBackward(double rx, double ry, double IZ1, double IY, double IZ2, double dest[]) {
		rx += IZ2;
		double sinIY = Math.sin(IY);
		double cosIY = Math.cos(IY);
		double sinRY = Math.sin(ry);
		double cosRY = Math.cos(ry);
		double sinRX = Math.sin(rx);
		double cosRX = Math.cos(rx);
		
		dest[0] = MathUtil.fixAngle2PI(Math.atan2(sinRX * sinRY, sinIY * cosRY + cosRX * cosIY * sinRY) + IZ1);
		double d = cosRY * cosIY - sinRY * sinIY * cosRX;
		if (d > 1.0)
			d = 1.0; // this might happen (ex. sx == IZ1 == 12 * MathUtil.deg2rad)
		dest[1] = Math.acos(d);
	}
}
