package com.slavi.improc.myadjust.sphere;

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

		dest[0] = IZ - Math.atan2(sinDX * cosSY, cosIY * sinSY - cosDX * sinIY * cosSY);
//		dest[0] = IZ + Math.PI - Math.atan2(sinDX * cosSY, cosIY * sinSY - cosDX * sinIY * cosSY);
		dest[1] = Math.asin(sinIY * sinSY + cosIY * cosSY * cosDX);
	}

	public static void transformBackward(double rx, double ry, KeyPointList srcImage, double dest[]) {
		rotateBackward(rx, ry, srcImage.rx, srcImage.ry, srcImage.rz, dest);
		// sx => longitude, sy => latitude
		dest[1] = srcImage.cameraOriginY + srcImage.scaleZ * Math.tan(dest[1]) / Math.cos(dest[0]);
		dest[0] = srcImage.cameraOriginX + srcImage.scaleZ * Math.tan(dest[0]);
	}

	public static void rotateBackward(double rx, double ry, double IX, double IY, double IZ, double dest[]) {
		rx = IZ - rx;
//		rx = IZ + Math.PI - rx;
		
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
	 * Find the angular (Great circle) distance between the two points on a sphere.
	 * http://en.wikipedia.org/wiki/Great-circle_distance
	 * 
	 * x -> geographical longitude
	 * y -> geographical latitude
	 */
	public static double getSphericalDistance(double rx1, double ry1, double rx2, double ry2) {
		double cosY1 = Math.cos(ry1);
		double sinY1 = Math.sin(ry1);
		double cosY2 = Math.cos(ry2);
		double sinY2 = Math.sin(ry2);
		rx2 -= rx1;
		double cosDX = Math.cos(rx2);
		double sinDX = Math.sin(rx2);
		
		double tmp1 = cosY2 * sinDX;
		double tmp2 = cosY1 * sinY2 - sinY1 * cosY2 * cosDX;
		
		double dy = Math.sqrt(tmp1 * tmp1 + tmp2 * tmp2);
		double dx = sinY1 * sinY2 + cosY1 * cosY2 * cosDX;
		return Math.atan2(dy, dx);
	}
}
