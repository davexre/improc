package com.slavi.improc.myadjust.zyz;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.math.RotationZYZ;

public class MyPanoPairTransformerZYZ {

	public static final RotationZYZ rot = RotationZYZ.instance;

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
