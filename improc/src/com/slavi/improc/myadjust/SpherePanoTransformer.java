package com.slavi.improc.myadjust;

import com.slavi.improc.KeyPointList;

public class SpherePanoTransformer {
	/**
	 * Transforms from source image coordinate system into world coord.system.
	 * @param sx, sy	Coordinates in pixels of the source image with origin pixel(0,0)
	 * @param dest		The transformed coordinates in radians. Longitude is 
	 * 					returned in dest[0] and is in the range (-pi; pi] and Latitude
	 * 					is returned in dest[1] in the range [-pi/2; pi/2].   
	 */
	public static void transformForeward(double sx, double sy, KeyPointList srcImage, double dest[]) {
		sx -= srcImage.cameraOriginX;
		sy -= srcImage.cameraOriginY;
		// sx => longitude, sy => latitude
		sy = Math.asin(sy / Math.sqrt(sx * sx + sy * sy + srcImage.scaleZ * srcImage.scaleZ));
		sx = Math.atan2(sx, srcImage.scaleZ);
		rotateForeward(sx, sy, srcImage.rx, srcImage.ry, srcImage.rz, dest);
	}
	
	public static void rotateForeward(double sx, double sy, double IX, double IY, double IZ, double dest[]) {
		double sinDX = Math.sin(sx - IX);
		double cosDX = Math.cos(sx - IX);
		double sinIY = Math.sin(IY);
		double cosIY = Math.cos(IY);
		double sinSY = Math.sin(sy);
		double cosSY = Math.cos(sy);

		dest[0] = IZ + Math.atan2(sinDX * cosSY, cosIY * sinSY - cosDX * sinIY * cosSY);
		dest[1] = Math.asin(sinIY * sinSY + cosIY * cosSY * cosDX);
	}

	public static void transformBackward(double rx, double ry, KeyPointList srcImage, double dest[]) {
		rotateBackward(rx, ry, srcImage.rx, srcImage.ry, srcImage.rz, dest);
		// sx => longitude, sy => latitude
		dest[1] = srcImage.cameraOriginY + srcImage.scaleZ * Math.tan(dest[1]) / Math.cos(dest[0]);
		dest[0] = srcImage.cameraOriginX + srcImage.scaleZ * Math.tan(dest[0]);
	}

	public static void rotateBackward(double rx, double ry, double IX, double IY, double IZ, double dest[]) {
		rx -= IZ;
		
		double sinIY = Math.sin(IY);
		double cosIY = Math.cos(IY);
		double sinRY = Math.sin(ry);
		double cosRY = Math.cos(ry);
		double sinRX = Math.sin(rx);
		double cosRX = Math.cos(rx);
		
		dest[0] = IX + Math.atan2(sinRX * cosRY, cosIY * sinRY - cosRX * sinIY * cosRY);
		dest[1] = Math.asin(sinIY * sinRY + cosIY * cosRY * cosRX);
	}
	
	/**
	 * Find the angular (Great circle) distance between the two points on a sphere 
	 */
	public static double getSphericalDistance(double rx1, double ry1, double rx2, double ry2) {
		double cosX1 = Math.cos(rx1);
		double sinX1 = Math.sin(rx1);
		double cosX2 = Math.cos(rx2);
		double sinX2 = Math.sin(rx2);
		ry2 -= ry1;
		double cosDY = Math.cos(ry2);
		double sinDY = Math.sin(ry2);
		
		double tmp1 = cosX2 * sinDY;
		double tmp2 = cosX1 * sinX2 - sinX1 * cosX2 * cosDY;
		
		double dx = Math.sqrt(tmp1 * tmp1 + tmp2 * tmp2);
		double dy = sinX1 * sinX2 + cosX1 * cosX2 * cosDY;
		return Math.atan2(dx, dy);
	}
}
