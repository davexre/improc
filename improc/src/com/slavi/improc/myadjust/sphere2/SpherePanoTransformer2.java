package com.slavi.improc.myadjust.sphere2;

import com.slavi.improc.KeyPointList;
import com.slavi.math.MathUtil;

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

	/**
	 * sx -> longitude
	 * sy -> zenith angle (90 - latitude) 
	 */
	public static void rotateForeward(double sx, double sy, double IZ1, double IY, double IZ2, double dest[]) {
		sx += IZ1;
		double sinDX = Math.sin(sx);
		double cosDX = Math.cos(sx);
		double sinIY = Math.sin(IY);
		double cosIY = Math.cos(IY);
		double sinSY = Math.sin(sy);
		double cosSY = Math.cos(sy);

		dest[0] = MathUtil.fixAngleMPI_PI(Math.atan2(sinDX * sinSY, cosDX * cosIY * sinSY - sinIY * cosSY) + IZ2);
		dest[1] = Math.acos(cosSY * cosIY + sinSY * sinIY * cosDX);
	}

	public static void transformBackward(double rx, double ry, KeyPointList srcImage, double dest[]) {
		rotateBackward(rx, ry, srcImage.sphereRZ1, srcImage.sphereRY, srcImage.sphereRZ2, dest);
		// x => longitude, y => zenith
		double r = srcImage.scaleZ * Math.tan(dest[1]);
		dest[1] = srcImage.cameraOriginY + r * Math.sin(dest[0]) / srcImage.cameraScale;
		dest[0] = srcImage.cameraOriginX + r * Math.cos(dest[0]) / srcImage.cameraScale;
	}

	/**
	 * rx -> longitude
	 * ry -> zenith angle (90 - latitude) 
	 */
	public static void rotateBackward(double rx, double ry, double IZ1, double IY, double IZ2, double dest[]) {
		rx -= IZ2;
		double sinIY = Math.sin(IY);
		double cosIY = Math.cos(IY);
		double sinRY = Math.sin(ry);
		double cosRY = Math.cos(ry);
		double sinRX = Math.sin(rx);
		double cosRX = Math.cos(rx);
		
		dest[0] = MathUtil.fixAngleMPI_PI(Math.atan2(sinRX * sinRY, sinIY * cosRY + cosRX * cosIY * sinRY) - IZ1);
		dest[1] = Math.acos(cosRY * cosIY - sinRY * sinIY * cosRX);
	}
}
