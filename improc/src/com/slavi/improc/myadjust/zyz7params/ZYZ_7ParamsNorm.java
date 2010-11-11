package com.slavi.improc.myadjust.zyz7params;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.math.RotationZYZ;
import com.slavi.math.matrix.Matrix;

public class ZYZ_7ParamsNorm {

	public static final RotationZYZ rot = RotationZYZ.instance;

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
		dest[0] += srcImage.tx;
		dest[1] += srcImage.ty;
		dest[2] += srcImage.tz;
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
		dest[0] = rx - srcImage.tx;
		dest[1] = ry - srcImage.ty;
		dest[2] = rz - srcImage.tz;
		rot.transformBackward(srcImage.camera2real, dest[0], dest[1], dest[2], dest);
		// Find the point where the vector intersects the image
		// The image is parallel to the XY plane and located at Z=focal distance 
		// http://en.wikipedia.org/wiki/Line-plane_intersection
		double d = dest[2] - srcImage.worldOrigin[2];
		if (d == 0.0) {
			// vector parallel to image
			dest[0] = Double.NaN;
			dest[1] = Double.NaN;
			dest[2] = 0.0;
			return;
		}
		double f = srcImage.scaleZ;
		d = (f - srcImage.worldOrigin[2]) / d;
		dest[0] = srcImage.cameraOriginX + (srcImage.worldOrigin[0] + (dest[0] - srcImage.worldOrigin[0]) * d) / srcImage.cameraScale;
		dest[1] = srcImage.cameraOriginY + (srcImage.worldOrigin[1] + (dest[1] - srcImage.worldOrigin[1]) * d) / srcImage.cameraScale;
		dest[2] = f / srcImage.cameraScale;
	}	

	public PointDerivatives p1 = new PointDerivatives();
	public PointDerivatives p2 = new PointDerivatives();

	public void setKeyPointPair(KeyPointPair kpp) {
		p1.setKeyPoint(kpp.sourceSP);
		p2.setKeyPoint(kpp.targetSP);
	}
	
	public static class PointDerivatives {
		
		public double P[] = new double[3];
		public Matrix dPdZ1 = new Matrix(1, 3);
		public Matrix dPdY  = new Matrix(1, 3);
		public Matrix dPdZ2 = new Matrix(1, 3);
		public Matrix dPdS  = new Matrix(1, 3);
		public Matrix dPdTX = new Matrix(1, 3);
		public Matrix dPdTY = new Matrix(1, 3);
		public Matrix dPdTZ = new Matrix(1, 3);
		public Matrix tmp   = new Matrix(1, 3);
		
		public void setKeyPoint(KeyPoint kp) {
			double sx = (kp.doubleX - kp.keyPointList.cameraOriginX) * kp.keyPointList.cameraScale;
			double sy = (kp.doubleY - kp.keyPointList.cameraOriginY) * kp.keyPointList.cameraScale;
			double sz = kp.keyPointList.scaleZ;
			rot.transformForward(kp.keyPointList.camera2real, sx, sy, sz, P);
			P[0] += kp.keyPointList.tx;
			P[1] += kp.keyPointList.ty;
			P[2] += kp.keyPointList.tz;
			
			tmp.setItem(0, 0, sx);
			tmp.setItem(0, 1, sy);
			tmp.setItem(0, 2, sz);
			
			kp.keyPointList.dMdX.mMul(tmp, dPdZ1);
			kp.keyPointList.dMdY.mMul(tmp, dPdY);
			kp.keyPointList.dMdZ.mMul(tmp, dPdZ2);
			
			tmp.setItem(0, 0, 0.0);
			tmp.setItem(0, 1, 0.0);
			tmp.setItem(0, 2, 1.0);
			kp.keyPointList.camera2real.mMul(tmp, dPdS);
			
			dPdTX.setItem(0, 0, 1.0);
			dPdTX.setItem(0, 1, 0.0);
			dPdTX.setItem(0, 2, 0.0);
			
			dPdTY.setItem(0, 0, 0.0);
			dPdTY.setItem(0, 1, 1.0);
			dPdTY.setItem(0, 2, 0.0);
			
			dPdTZ.setItem(0, 0, 0.0);
			dPdTZ.setItem(0, 1, 0.0);
			dPdTZ.setItem(0, 2, 1.0);
		}
	}
}
