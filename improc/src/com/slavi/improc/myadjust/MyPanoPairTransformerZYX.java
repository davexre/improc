package com.slavi.improc.myadjust;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.math.RotationZYX;

public class MyPanoPairTransformerZYX {

	public static final RotationZYX rot = RotationZYX.instance;

	/*
	 * x -> fi (longitude)
	 * y -> psi (latitude) 
	 */
	
	/**
	 * dest[0] = x
	 * dest[1] = y
	 * dest[2] = z
	 */
	public static void polarToCartesian(double fi, double psi, double r, double dest[]) {
		double cosPsi = Math.cos(psi);
		dest[0] = r * Math.cos(fi) * cosPsi;
		dest[1] = r * Math.sin(fi) * cosPsi;
		dest[2] = r * Math.sin(psi);
	}
	
	/**
	 * dest[0] = fi
	 * dest[1] = psi
	 * dest[2] = r 
	 */
	public static void cartesianToPolar(double x, double y, double z, double dest[]) {
		dest[0] = Math.atan2(y, x);
		dest[2] = Math.sqrt(x*x + y*y + z*z);
		dest[1] = dest[2] == 0.0 ? 0.0 : Math.asin(z / dest[2]);
	}
	
	/**
	 * Find the angular (Great circle) distance between the two points on a sphere 
	 */
	public static double getSphericalDistance(double fi1, double psi1, double fi2, double psi2) {
		double cosX1 = Math.cos(fi1);
		double sinX1 = Math.sin(fi1);
		double cosX2 = Math.cos(fi2);
		double sinX2 = Math.sin(fi2);
		psi1 -= psi2;
		double cosDY = Math.cos(psi1);
		double sinDY = Math.sin(psi1);
		
		double tmp1 = cosX2 * sinDY;
		double tmp2 = cosX1 * sinX2 - sinX1 * cosX2 * cosDY;
		
		double dx = Math.sqrt(tmp1 * tmp1 + tmp2 * tmp2);
		double dy = sinX1 * sinX2 + cosX1 * cosX2 * cosDY;
		return Math.atan2(dx, dy);
	}
	
	/**
	 * Transforms from source image coordinate system into world coord.system.
	 * @param sx, sy	Coordinates in pixels of the source image with origin pixel(0,0)
	 * @param dest		The transformed coordinates in radians. Longitude is 
	 * 					returned in dest[0] and is in the range (-pi; pi] and Latitude
	 * 					is returned in dest[1] in the range [-pi/2; pi/2]. dest[2] should be 1.0    
	 */
	public static void transformForeward(double sx, double sy, KeyPointList srcImage, double dest[]) {
		sx = (sx - srcImage.cameraOriginX) * srcImage.cameraScale;
		sy = (sy - srcImage.cameraOriginY) * srcImage.cameraScale;
		double sz = srcImage.scaleZ;
		
		rot.transformForward(srcImage.camera2real, sx, sy, sz, dest);
		cartesianToPolar(dest[2], dest[0], dest[1], dest);
	}

	public static void transformBackward(double rx, double ry, KeyPointList srcImage, double dest[]) {
		polarToCartesian(rx, ry, 1.0, dest);
		rot.transformBackward(srcImage.camera2real, dest[1], dest[2], dest[0], dest);

		if (dest[2] == 0) {
			dest[0] = Double.NaN;
			dest[1] = Double.NaN;
			return;
		}
		dest[0] = srcImage.scaleZ * (dest[0] / dest[2]);
		dest[1] = srcImage.scaleZ * (dest[1] / dest[2]);
		
		dest[0] = (dest[0] / srcImage.cameraScale) + srcImage.cameraOriginX;
		dest[1] = (dest[1] / srcImage.cameraScale) + srcImage.cameraOriginY;
	}
	
	public static void transform3D(KeyPoint source, KeyPointList srcImage, double dest[]) {
		double sx = (source.doubleX - srcImage.cameraOriginX) * srcImage.cameraScale;
		double sy = (source.doubleY - srcImage.cameraOriginY) * srcImage.cameraScale;
		double sz = srcImage.scaleZ;
		
		rot.transformForward(srcImage.camera2real, sx, sy, sz, dest);
	}
}