package com.slavi.improc.myadjust.sphere;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.math.MathUtil;
import com.slavi.math.SphericalCoordsLongZen;

public class SphereNorm {

	/**
	 * Transforms from source image coordinate system into world coord.system.
	 * @param sx, sy	Coordinates in pixels of the source image with origin pixel(0,0)
	 * @param dest[3]	OUTPUT: The transformed coordinates in radians. Longitude is 
	 * 					returned in dest[0] and Zenith (pi/2-Latitude) is returned 
	 * 					in dest[1]. dest[2] not used.    
	 */
	public static void transformForeward(double sx, double sy, KeyPointList srcImage, double dest[]) {
		sx = sx - srcImage.cameraOriginX;
		sy = sy - srcImage.cameraOriginY;
		double f = srcImage.scaleZ;
		// srcImage.scaleZ = Math.max(srcImage.imageSizeX, srcImage.imageSizeY) / 
		// 			(2.0 * Math.tan(srcImage.fov / 2.0));
		
		// x => longitude, y => zenith
		SphericalCoordsLongZen.cartesianToPolar(sx, sy, f, dest);
		SphericalCoordsLongZen.rotateForeward(dest[0], dest[1], srcImage.sphereRZ1, srcImage.sphereRY, srcImage.sphereRZ2, dest);
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
	public static void transformBackward(double rx, double ry, KeyPointList srcImage, double dest[]) {
		SphericalCoordsLongZen.rotateBackward(rx, ry, srcImage.sphereRZ1, srcImage.sphereRY, srcImage.sphereRZ2, dest);
		SphericalCoordsLongZen.polarToCartesian(dest[0], dest[1], 1.0, dest);

		if (dest[2] <= 0.0) {
			dest[0] = Double.NaN;
			dest[1] = Double.NaN;
			return;
		}
		dest[0] = srcImage.cameraOriginX + (dest[0] / dest[2]) * srcImage.scaleZ;
		dest[1] = srcImage.cameraOriginY + (dest[1] / dest[2]) * srcImage.scaleZ;
		dest[2] = srcImage.scaleZ;
	}

	PointDerivatives p1 = new PointDerivatives();
	PointDerivatives p2 = new PointDerivatives();

	public double Dist;
	public double dist0;
	
	// Private derivatives 
	public double dDist_dSR1;
	public double dDist_dSR2;
	public double dDist_dSR3;
	public double dDist_dSF;

	public double dDist_dTR1;
	public double dDist_dTR2;
	public double dDist_dTR3;
	public double dDist_dTF;

	public void setKeyPointPair(KeyPointPair kpp) {
		double source[] = new double[3];
		double target[] = new double[3];
		transformForeward(kpp.sourceSP.getDoubleX(), kpp.sourceSP.getDoubleY(), kpp.sourceSP.getKeyPointList(), source);
		transformForeward(kpp.targetSP.getDoubleX(), kpp.targetSP.getDoubleY(), kpp.targetSP.getKeyPointList(), target);
		dist0 = SphericalCoordsLongZen.getSphericalDistance(source[0], source[1], target[0], target[1]);

		p1.setKeyPoint(kpp.sourceSP);
		p2.setKeyPoint(kpp.targetSP);
		
		dDist_dSR1 = calc_dDist_dParam(p1.dTX_dR1, p1.dTY_dR1, 0, 0);
		dDist_dSR2 = calc_dDist_dParam(p1.dTX_dR2, p1.dTY_dR2, 0, 0);
		dDist_dSR3 = calc_dDist_dParam(p1.dTX_dR3, p1.dTY_dR3, 0, 0);
		dDist_dSF = calc_dDist_dParam(p1.dTX_dF, p1.dTY_dF, 0, 0);

		dDist_dTR1 = calc_dDist_dParam(0, 0, p2.dTX_dR1, p2.dTY_dR1);
		dDist_dTR2 = calc_dDist_dParam(0, 0, p2.dTX_dR2, p2.dTY_dR2);
		dDist_dTR3 = calc_dDist_dParam(0, 0, p2.dTX_dR3, p2.dTY_dR3);
		dDist_dTF = calc_dDist_dParam(0, 0, p2.dTX_dF, p2.dTY_dF);
		if (Math.abs(dist0 - Dist) > (MathUtil.epsAngle * 10)) {
			System.out.println("LX1=" + MathUtil.rad2degStr(p1.sx) + "\tLY1=" + MathUtil.rad2degStr(p1.sy));
			System.out.println("LX2=" + MathUtil.rad2degStr(p2.sx) + "\tLY2=" + MathUtil.rad2degStr(p2.sy));
			
			System.out.println(
					"p1.x=" + MathUtil.rad2degStr(source[0]) + 
					"\tp1.x1=" + MathUtil.rad2degStr(p1.tx) + 
					"\tp1.y=" + MathUtil.rad2degStr(source[1]) + 
					"\tp1.y1=" + MathUtil.rad2degStr(p1.ty) + 
					"\tp2.x=" + MathUtil.rad2degStr(target[0]) + 
					"\tp2.x1=" + MathUtil.rad2degStr(p2.tx) + 
					"\tp2.y=" + MathUtil.rad2degStr(target[1]) + 
					"\tp2.y1=" + MathUtil.rad2degStr(p2.ty) 
					);
			
			System.out.println(toString());
		}
	}
	
	public static class PointDerivatives {
		// R1, R2 -> long, zenith of North pole of the target spherical coord 
		// 		system in the local (image) spherical coords system.
		// R1 = kp.keyPointList.sphereRZ1
		// R2 = kp.keyPointList.sphereRY
		// R3 = kp.keyPointList.sphereRZ2 (rotation of local coord system around its pole axis)
		// F  = kp.keyPointList.scaleZ (focal distance of the image)
		public KeyPoint kp;
		
		// tx, ty -> long, zenith of the point in target spherical coords
		public double tx, ty;
		public double sinTX, cosTX;
		public double sinTY, cosTY;
		// Private derivatives 
		public double dTX_dR1;
		public double dTX_dR2;
		public double dTX_dR3;
		public double dTX_dF;
		
		public double dTY_dR1;
		public double dTY_dR2;
		public double dTY_dR3;
		public double dTY_dF;

		// sx, sy -> long, zenith of the point in source/local spherical coords
		private double sx, sy;
		// Private derivatives
		private double dSX_dF;
		private double dSY_dF;

		public void setKeyPoint(KeyPoint kp) {
			this.kp = kp;
			calcLocalSphericalCoords();
			calcTargetSphericalCoords();
		}
		
		private void calcTargetSphericalCoords() {
			double sinDSX = Math.sin(sx - kp.getKeyPointList().sphereRZ1);
			double cosDSX = Math.cos(sx - kp.getKeyPointList().sphereRZ1);
			double sinSY = Math.sin(sy);
			double cosSY = Math.cos(sy);
			double sinR2 = Math.sin(kp.getKeyPointList().sphereRY);
			double cosR2 = Math.cos(kp.getKeyPointList().sphereRY);

			// dTY
			double I = sinSY * sinR2 * cosDSX;
			double dI_dR1 = sinSY * sinR2 * sinDSX;
			double dI_dR2 = sinSY * cosR2 * cosDSX;
			double dI_dF = cosSY * sinR2 * cosDSX * dSY_dF - sinSY * sinR2 * sinDSX * dSX_dF;
			
			double H = cosSY * cosR2;
			double dH_dR1 = 0;
			double dH_dR2 = - cosSY * sinR2;
			double dH_dF = - sinSY * cosR2 * dSY_dF;
			
			double G = H + I;
			double dG_dR1 = dH_dR1 + dI_dR1;
			double dG_dR2 = dH_dR2 + dI_dR2;
			double dG_dF = dH_dF + dI_dF;
			
			ty = Math.acos(G);
			double tmp = - Math.sqrt(1 - G * G);
			dTY_dR1 = dG_dR1 / tmp;
			dTY_dR2 = dG_dR2 / tmp;
			dTY_dF = dG_dF / tmp;
			dTY_dR3 = 0;

			// dTX	
			double E = sinR2 * cosSY;
			double dE_dR1 = 0;
			double dE_dR2 = cosR2 * cosSY;
			double dE_dF = - sinR2 * sinSY * dSY_dF;
			
			double D = cosDSX * cosR2 * sinSY;
			double dD_dR1 = sinDSX * cosR2 * sinSY;
			double dD_dR2 = - cosDSX * sinR2 * sinSY;
			double dD_dF = - sinDSX * cosR2 * sinSY * dSX_dF + cosDSX * cosR2 * cosSY * dSY_dF;
			
			double C = D - E;
			double dC_dR1 = dD_dR1 - dE_dR1; 
			double dC_dR2 = dD_dR2 - dE_dR2;
			double dC_dF = dD_dF - dE_dF;
			
			double B = sinDSX * sinSY;
			double dB_dR1 = - cosDSX * sinSY;
			double dB_dR2 = 0;
			double dB_dF = cosDSX * sinSY * dSX_dF + sinDSX * cosSY * dSY_dF;
			
			double A = B / C;
			double dA_dR1 = (dB_dR1 * C - B * dC_dR1) / (C*C);
			double dA_dR2 = (dB_dR2 * C - B * dC_dR2) / (C*C);
			double dA_dF = (dB_dF * C - B * dC_dF) / (C*C);
			
			tx = Math.atan2(B, C) - kp.getKeyPointList().sphereRZ2;
			tmp = 1 + A * A;
			dTX_dR1 = dA_dR1 / tmp;
			dTX_dR2 = dA_dR2 / tmp;
			dTX_dF = dA_dF / tmp;
			dTX_dR3 = -1;
			
			// calc commons
			sinTX = Math.sin(tx);
			cosTX = Math.cos(tx);
			sinTY = Math.sin(ty);
			cosTY = Math.cos(ty);
		}
		
		private void calcLocalSphericalCoords() {
			// kp.keyPointList.scaleZ = Math.max(kp.keyPointList.imageSizeX, kp.keyPointList.imageSizeY) / 
			// 			(2.0 * Math.tan(kp.keyPointList.fov / 2.0));
			// sy = Math.acos(kp.keyPointList.scaleZ / Math.sqrt(tmpx*tmpx + tmpy*tmpy + kp.keyPointList.scaleZ*kp.keyPointList.scaleZ));
			double tmpx = kp.getDoubleX() - kp.getKeyPointList().cameraOriginX;
			double tmpy = kp.getDoubleY() - kp.getKeyPointList().cameraOriginY;
			
			double E = kp.getKeyPointList().fov / 2.0;
			double dEdF = 0.5;
			double D = Math.tan(E);
			double tmp = Math.cos(E);
			double dDdF = dEdF / (tmp * tmp);
			double B = 0.5 * Math.max(kp.getKeyPointList().imageSizeX, kp.getKeyPointList().imageSizeY) / D;
			double dBdF = - dDdF * B / D;
			
			double G = tmpx * tmpx + tmpy * tmpy + B * B;
			double dGdF = 2.0 * B * dBdF;
			double C = Math.sqrt(G);
			double dCdF = dGdF / C;
			double A = B / C;
			double dAdF = (dBdF * C - B * dCdF) / (C * C);
			
			sy = Math.acos(A);
			dSY_dF = - dAdF / Math.sqrt(1 - A * A);
			
			sx = Math.atan2(tmpy, tmpx);
			dSX_dF = 0.0;
		}
	}
	
	private double calc_dDist_dParam(double dRX1_dP, double dRY1_dP, double dRX2_dP, double dRY2_dP) {
		double H = p2.tx - p1.tx;
		double dHdP = dRX2_dP - dRX1_dP;
		double cosH = Math.cos(H);
		double sinH = Math.sin(H);

		double L = p1.sinTY * p2.sinTY * cosH;
		double dLdP =
			p1.cosTY * p2.sinTY * cosH * dRY1_dP
			+ p1.sinTY * p2.cosTY * cosH * dRY2_dP
			- p1.sinTY * p2.sinTY * sinH * dHdP;
		
		double K = p1.cosTY * p2.cosTY;
		double dKdP = 
			- p1.sinTY * p2.cosTY * dRY1_dP
			- p1.cosTY * p2.sinTY * dRY2_dP;
		
		double J = p1.cosTY * p2.sinTY * cosH;
		double dJdP = 
			- p1.sinTY * p2.sinTY * cosH * dRY1_dP
			+ p1.cosTY * p2.cosTY * cosH * dRY2_dP
			- p1.cosTY * p2.sinTY * sinH * dHdP;
		
		double I = p1.sinTY * p2.cosTY;
		double dIdP = p1.cosTY * p2.cosTY * dRY1_dP - p1.sinTY * p2.sinTY * dRY2_dP;
		
		double G = I - J;
		double dGdP = dIdP - dJdP;
				
		double F = p2.sinTY * sinH;
		double dFdP = p2.cosTY * sinH * dRY2_dP + p2.sinTY * cosH * dHdP;
		
		double E = F * F + G * G;
		double dEdP = 2.0 * F * dFdP + 2.0 * G * dGdP;
		
		double C = K + L;
		double dCdP = K * dKdP + L * dLdP;
		
		double B = Math.sqrt(E);
		double dBdP = dEdP / (2.0 * B);
		
		double A = B / C;
		double dAdP = (dBdP * C - B * dCdP) / (C * C);
		
		Dist = Math.atan2(B, C);
		double dDistdP = dAdP / (1 + A*A);
		return dDistdP;
	}

	public String toString() {
		return 
		"Dist=" + MathUtil.rad2degStr(Dist) +
		"\td=" + MathUtil.rad2degStr(dist0) + 
		"\tiSX=" + p1.kp.getDoubleX() + 
		"\tiSY=" + p1.kp.getDoubleY() + 
//		"\tSCamX=" + p1.kp.keyPointList.cameraOriginX + 
//		"\tSCamY=" + p1.kp.keyPointList.cameraOriginY + 
		"\tiTX=" + p2.kp.getDoubleX() + 
		"\tiTY=" + p2.kp.getDoubleY() + 
//		"\tTCamX=" + p2.kp.keyPointList.cameraOriginX + 
//		"\tTCamY=" + p2.kp.keyPointList.cameraOriginY + 
		"\tSR1=" + MathUtil.rad2degStr(p1.kp.getKeyPointList().sphereRZ1) + 
		"\tSR2=" + MathUtil.rad2degStr(p1.kp.getKeyPointList().sphereRY) + 
		"\tSR3=" + MathUtil.rad2degStr(p1.kp.getKeyPointList().sphereRZ2) + 
		"\tTR1=" + MathUtil.rad2degStr(p2.kp.getKeyPointList().sphereRZ1) + 
		"\tTR2=" + MathUtil.rad2degStr(p2.kp.getKeyPointList().sphereRY) + 
		"\tTR3=" + MathUtil.rad2degStr(p2.kp.getKeyPointList().sphereRZ2);
	}
}
