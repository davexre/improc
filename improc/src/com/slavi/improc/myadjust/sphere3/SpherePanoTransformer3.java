package com.slavi.improc.myadjust.sphere3;

import com.slavi.improc.KeyPointList;
import com.slavi.math.MathUtil;

public class SpherePanoTransformer3 {

	/**
	 * Transforms from source image coordinate system into world coord.system.
	 * @param sx, sy	Coordinates in pixels of the source image with origin pixel(0,0)
	 * @param dest		The transformed coordinates in radians. Longitude is 
	 * 					returned in dest[0] and is in the range (-pi; pi] and zenith
	 * 					is returned in dest[1] in the range [0; pi].   
	 */
	public static void transformForeward(double sx, double sy, KeyPointList srcImage, double dest[]) {
		sx = (sx - srcImage.cameraOriginX) * srcImage.cameraScale;
		sy = (sy - srcImage.cameraOriginY) * srcImage.cameraScale;
		double f = srcImage.scaleZ;
		// x => longitude, y => zenith
		double y = Math.atan2(sx, sy); /// TODO: CHECK ME!!!!
		double r1 = Math.sqrt(sx*sx + sy*sy);
		double x = Math.atan2(r1, f);
		rotateForeward(x, y, srcImage.sphereRZ1, srcImage.sphereRY, srcImage.sphereRZ2, dest);
	}

	/**
	 * sx -> (source) longitude
	 * sy -> (source) zenith angle (90 - latitude) 
	 * 
	 * R1 = Rot(Z1)
	 * R2 = Rot(X)
	 * R3 = Rot(Z2)
	 * 
	 * dest[0] = tx -> (target) longitude
	 * dest[1] = ty -> (target) zenith
	 */
	public static void rotateForeward(double sx, double sy, double R1, double R2, double R3, double dest[]) {
		sx += R1;
		double sinDSX = Math.sin(sx);
		double cosDSX = Math.cos(sx);
		double sinR2 = Math.sin(R2);
		double cosR2 = Math.cos(R2);
		double sinSY = Math.sin(sy);
		double cosSY = Math.cos(sy);

		dest[0] = MathUtil.fixAngleMPI_PI(Math.atan2(sinDSX * sinSY, cosDSX * cosR2 * sinSY - sinR2 * cosSY) + R3);
		dest[1] = Math.acos(cosSY * cosR2 + sinSY * sinR2 * cosDSX);
	}

	public static void transformBackward(double rx, double ry, KeyPointList srcImage, double dest[]) {
		rotateBackward(rx, ry, srcImage.sphereRZ1, srcImage.sphereRY, srcImage.sphereRZ2, dest);
		// x => longitude, y => zenith
		double r1 = srcImage.scaleZ * Math.tan(dest[1]);
		dest[1] = srcImage.cameraOriginY + r1 * Math.cos(dest[0]) / srcImage.cameraScale;
		dest[0] = srcImage.cameraOriginX + r1 * Math.sin(dest[0]) / srcImage.cameraScale;
	}

	/**
	 * tx -> longitude
	 * ty -> zenith angle (90 - latitude) 
	 * 
	 * R1 = Rot(Z1)
	 * R2 = Rot(X)
	 * R3 = Rot(Z2)
	 * 
	 * dest[0] = sx -> (source) longitude
	 * dest[1] = sy -> (source) zenith
	 */
	public static void rotateBackward(double tx, double ty, double R1, double R2, double R3, double dest[]) {
		tx -= R3;
		double sinR2 = Math.sin(R2);
		double cosR2 = Math.cos(R2);
		double sinTY = Math.sin(ty);
		double cosTY = Math.cos(ty);
		double sinTX = Math.sin(tx);
		double cosTX = Math.cos(tx);
		
		dest[0] = MathUtil.fixAngleMPI_PI(Math.atan2(sinTX * sinTY, sinR2 * cosTY + cosTX * cosR2 * sinTY) - R1);
		dest[1] = Math.acos(cosTY * cosR2 - sinTY * sinR2 * cosTX);
	}
}
