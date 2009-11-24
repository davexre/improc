package com.slavi.improc.myadjust.zyx;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.math.RotationZYX;
import com.slavi.math.SphericalCoordsLongLat;

public class MyPanoPairTransformerZYX {

	public static final RotationZYX rot = RotationZYX.instance;

	/*
	 * x -> fi (longitude)
	 * y -> psi (latitude) 
	 */
	
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
		SphericalCoordsLongLat.cartesianToPolar(dest[2], dest[0], dest[1], dest);
	}

	public static void transformBackward(double rx, double ry, KeyPointList srcImage, double dest[]) {
		SphericalCoordsLongLat.polarToCartesian(rx, ry, 1.0, dest);
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
