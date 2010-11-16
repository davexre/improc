package com.slavi.improc.myadjust.zyx;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.math.RotationZYX;

public class MyPanoPairTransformZYXNorm {

	public static final RotationZYX rot = RotationZYX.instance;

	/**
	 * Transforms from source image coordinate system into world coord.system.
	 * @param sx, sy	Coordinates in pixels of the source image with origin pixel(0,0)
	 * @param dest[3]	OUTPUT: The transformed coordinates.
	 */
	public static void transformForeward(double sx, double sy, KeyPointList srcImage, double dest[]) {
		sx = (sx - srcImage.cameraOriginX) * srcImage.cameraScale;
		sy = (sy - srcImage.cameraOriginY) * srcImage.cameraScale;
		double sz = srcImage.scaleZ;
		
		rot.transformForward(srcImage.camera2real, sx, sy, sz, dest);
	}
	
	/**
	 * Transforms from world coordinate system into source image coord.system. 
	 * dest[0] = x in image coordinates
	 * dest[1] = y in image coordinates
	 * dest[2] = r - radius, i.e. distance from the image pixel
	 * 					to the focal point or origin of the 3D image coordinate system. 
	 * 					If r > 0 coordinates are ok.
	 * 					If r <=0 the specified rx,ry are outside of the source image (on 
	 * 					the opposite side of the sphere)
	 */
	public static void transformBackward(double rx, double ry, double rz, KeyPointList srcImage, double dest[]) {
		rot.transformBackward(srcImage.camera2real, rx, ry, rz, dest);
		if (dest[2] <= 0.0) {
			dest[0] = Double.NaN;
			dest[1] = Double.NaN;
			return;
		}
		dest[0] = srcImage.cameraOriginX + (dest[0] / dest[2]) * srcImage.scaleZ / srcImage.cameraScale;
		dest[1] = srcImage.cameraOriginY + (dest[1] / dest[2]) * srcImage.scaleZ / srcImage.cameraScale;
		dest[2] = dest[2] == 0.0 ? 0.0 : srcImage.scaleZ / dest[2];
	}	

	public PointDerivatives p1 = new PointDerivatives();
	public PointDerivatives p2 = new PointDerivatives();

	public void setKeyPointPair(KeyPointPair kpp) {
		p1.setKeyPoint(kpp.sourceSP);
		p2.setKeyPoint(kpp.targetSP);
	}
	
	public static class PointDerivatives {
		
		public double P[]     = new double[3];
		public double dPdZ1[] = new double[3];
		public double dPdY[]  = new double[3];
		public double dPdZ2[] = new double[3];
		public double dPdS[]  = new double[3];
		
		public void setKeyPoint(KeyPoint kp) {
			double sx = (kp.doubleX - kp.keyPointList.cameraOriginX) * kp.keyPointList.cameraScale;
			double sy = (kp.doubleY - kp.keyPointList.cameraOriginY) * kp.keyPointList.cameraScale;
			double sz = kp.keyPointList.scaleZ;
			
			rot.transformForward(kp.keyPointList.dMdX, sx, sy, sz, dPdZ1);
			rot.transformForward(kp.keyPointList.dMdY, sx, sy, sz, dPdY);
			rot.transformForward(kp.keyPointList.dMdZ, sx, sy, sz, dPdZ2);
			rot.transformForward(kp.keyPointList.camera2real, 0, 0, 1, dPdS);
			rot.transformForward(kp.keyPointList.camera2real, sx, sy, sz, P);
		}
	}
}
