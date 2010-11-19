package com.slavi.improc.myadjust.zyz7params;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.math.MathUtil;
import com.slavi.math.RotationZYZ;

public class Stereo_7ParamsNorm {

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

	public double F;
	public double F0;
	// Private derivatives 
	public double dF_dSZ1;
	public double dF_dSY;
	public double dF_dSZ2;
	public double dF_dSF;
	public double dF_dSTX;
	public double dF_dSTY;
	public double dF_dSTZ;

	public double dF_dTZ1;
	public double dF_dTY;
	public double dF_dTZ2;
	public double dF_dTF;
	public double dF_dTTX;
	public double dF_dTTY;
	public double dF_dTTZ;
	
	double Zero[] = new double[3];
	
	private double calcCrossAndDot(int i) {
		int j = (i + 1) % 3;
		int k = (i + 2) % 3;
		return p2.vectP[i] * (p1.vectP[j] * (p2.S[k] - p1.S[k]) - p1.vectP[k] * (p2.S[j] - p1.S[j]));
	}
	
	private double calc_dF_dParam_Internal(int i, double dP1_dParam[], double dS1_dParam[], double dP2_dParam[], double dS2_dParam[]) {
		int j = (i + 1) % 3;
		int k = (i + 2) % 3;
		
		double D = p2.S[k] - p1.S[k];
		double dD_dP = dS2_dParam[k] - dS1_dParam[k];

		double E = p2.S[j] - p1.S[j];
		double dE_dP = dS2_dParam[j] - dS1_dParam[j];
		
		double B = p1.vectP[j] * D;
		double dB_dP = dP1_dParam[j] * D + p1.vectP[j] * dD_dP; 
		
		double C = p1.vectP[k] * E;
		double dC_dP = dP1_dParam[k] * E + p1.vectP[k] * dE_dP; 
		
		double A = B - C;
		double dA_dP= dB_dP - dC_dP;
		
		F0 += p2.vectP[i] * A;
		double dFI_dP = dP2_dParam[i] * A + p2.vectP[i] * dA_dP;
		return dFI_dP;
	}

	private double calc_dF_dParam(double dP1_dParam[], double dS1_dParam[], double dP2_dParam[], double dS2_dParam[]) {
		F0 = 0;
		double result = 
			calc_dF_dParam_Internal(0, dP1_dParam, dS1_dParam, dP2_dParam, dS2_dParam) +
			calc_dF_dParam_Internal(1, dP1_dParam, dS1_dParam, dP2_dParam, dS2_dParam) +
			calc_dF_dParam_Internal(2, dP1_dParam, dS1_dParam, dP2_dParam, dS2_dParam);
		if (Math.abs(F0 - F) > (MathUtil.epsAngle * 10)) {
			// F == F0
			throw new Error("Failed to calculate the primary derivates");
		}
		return result;
	}
	
	public double calcF_Only(KeyPointPair kpp) {
		p1.calcF_Only(kpp.sourceSP);
		p2.calcF_Only(kpp.targetSP);
		F = calcCrossAndDot(0) + calcCrossAndDot(1) + calcCrossAndDot(2);
		return F;
	}
	
	public void setKeyPointPair(KeyPointPair kpp) {
		p1.setKeyPoint(kpp.sourceSP);
		p2.setKeyPoint(kpp.targetSP);
		F = calcCrossAndDot(0) + calcCrossAndDot(1) + calcCrossAndDot(2);
		
		Zero[0] = Zero[1] = Zero[2] = 0;
		
		dF_dSZ1 = calc_dF_dParam(p1.dPdZ1, Zero, Zero, Zero);
		dF_dSY  = calc_dF_dParam(p1.dPdY,  Zero, Zero, Zero);
		dF_dSZ2 = calc_dF_dParam(p1.dPdZ2, Zero, Zero, Zero);
		dF_dSF  = calc_dF_dParam(p1.dPdF,  Zero, Zero, Zero);
		dF_dSTX = calc_dF_dParam(p1.dPdTX, p1.dPdTX, Zero, Zero);
		dF_dSTY = calc_dF_dParam(p1.dPdTY, p1.dPdTY, Zero, Zero);
		dF_dSTZ = calc_dF_dParam(p1.dPdTZ, p1.dPdTZ, Zero, Zero);

		dF_dTZ1 = calc_dF_dParam(Zero, Zero, p2.dPdZ1, Zero);
		dF_dTY  = calc_dF_dParam(Zero, Zero, p2.dPdY,  Zero);
		dF_dTZ2 = calc_dF_dParam(Zero, Zero, p2.dPdZ2, Zero);
		dF_dTF  = calc_dF_dParam(Zero, Zero, p2.dPdF,  Zero);
		dF_dTTX = calc_dF_dParam(Zero, Zero, p2.dPdTX, p2.dPdTX);
		dF_dTTY = calc_dF_dParam(Zero, Zero, p2.dPdTY, p2.dPdTY);
		dF_dTTZ = calc_dF_dParam(Zero, Zero, p2.dPdTZ, p2.dPdTZ);
	}
	
	public static class PointDerivatives {
		
		public double vectP[] = new double[3];
		public double P[]     = new double[3];
		public double S[]     = new double[3];
		public double dPdZ1[] = new double[3];
		public double dPdY[]  = new double[3];
		public double dPdZ2[] = new double[3];
		public double dPdF[]  = new double[3];
		public double dPdTX[] = new double[3];
		public double dPdTY[] = new double[3];
		public double dPdTZ[] = new double[3];
		
		public void calcF_Only(KeyPoint kp) {
			double sx = (kp.doubleX - kp.keyPointList.cameraOriginX) * kp.keyPointList.cameraScale;
			double sy = (kp.doubleY - kp.keyPointList.cameraOriginY) * kp.keyPointList.cameraScale;
			double sz = kp.keyPointList.scaleZ;
			rot.transformForward(kp.keyPointList.camera2real, sx, sy, sz, vectP);
			P[0] = vectP[0] + kp.keyPointList.tx;
			P[1] = vectP[1] + kp.keyPointList.ty;
			P[2] = vectP[2] + kp.keyPointList.tz;
		}
		
		public void setKeyPoint(KeyPoint kp) {
			double sx = (kp.doubleX - kp.keyPointList.cameraOriginX) * kp.keyPointList.cameraScale;
			double sy = (kp.doubleY - kp.keyPointList.cameraOriginY) * kp.keyPointList.cameraScale;
			double sz = kp.keyPointList.scaleZ;
			
			rot.transformForward(kp.keyPointList.dMdX, sx, sy, sz, dPdZ1);
			rot.transformForward(kp.keyPointList.dMdY, sx, sy, sz, dPdY);
			rot.transformForward(kp.keyPointList.dMdZ, sx, sy, sz, dPdZ2);
			rot.transformForward(kp.keyPointList.camera2real, 0, 0, 1, dPdF);
			rot.transformForward(kp.keyPointList.camera2real, sx, sy, sz, vectP);
			P[0] = vectP[0] + kp.keyPointList.tx;
			P[1] = vectP[1] + kp.keyPointList.ty;
			P[2] = vectP[2] + kp.keyPointList.tz;

			S[0] = kp.keyPointList.tx;
			S[1] = kp.keyPointList.ty;
			S[2] = kp.keyPointList.tz;
			
			dPdTX[0] = 1;
			dPdTX[1] = 0;
			dPdTX[2] = 0;
			
			dPdTY[0] = 0;
			dPdTY[1] = 1;
			dPdTY[2] = 0;
			
			dPdTZ[0] = 0;
			dPdTZ[1] = 0;
			dPdTZ[2] = 1;
		}
	}
}
