package com.slavi.improc.myadjust.sphere3;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointPair;
import com.slavi.math.MathUtil;
import com.slavi.math.SphericalCoordsLongZen;

public class SphereNorm3 {

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
		double source[] = new double[2];
		double target[] = new double[2];
		SpherePanoTransformer3.transformForeward(kpp.sourceSP.doubleX, kpp.sourceSP.doubleY, kpp.sourceSP.keyPointList, source);
		SpherePanoTransformer3.transformForeward(kpp.targetSP.doubleX, kpp.targetSP.doubleY, kpp.targetSP.keyPointList, target);
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
			double sinDSX = Math.sin(sx + kp.keyPointList.sphereRZ1);
			double cosDSX = Math.cos(sx + kp.keyPointList.sphereRZ1);
			double sinSY = Math.sin(sy);
			double cosSY = Math.cos(sy);
			double sinR2 = Math.sin(kp.keyPointList.sphereRY);
			double cosR2 = Math.cos(kp.keyPointList.sphereRY);

			// dTY
			double I = sinSY * sinR2 * cosDSX;
			double dI_dR1 = - sinSY * sinR2 * sinDSX;
			double dI_dR2 = sinSY * cosR2 * cosDSX;
			double dI_dF = cosSY * sinR2 * cosDSX * dSY_dF - sinSY * sinR2 * sinDSX * dSX_dF;
			
			double H = cosSY * cosR2;
			double dH_dR1 = 0;
			double dH_dR2 = - cosSY * sinR2;
			double dH_dR3 = - sinSY * cosR2 * dSY_dF;
			
			double G = H + I;
			double dG_dR1 = dH_dR1 + dI_dR1;
			double dG_dR2 = dH_dR2 + dI_dR2;
			double dG_dR3 = dH_dR3 + dI_dF;
			
			ty = Math.acos(G);
			double tmp = - Math.sqrt(1 - G * G);
			dTY_dR1 = dG_dR1 / tmp;
			dTY_dR2 = dG_dR2 / tmp;
			dTY_dF = dG_dR3 / tmp;
			dTY_dR3 = 0;

			// dTX
			double E = sinR2 * cosSY;
			double dE_dR1 = 0;
			double dE_dR2 = cosR2 * cosSY;
			double dE_dF = - sinR2 * sinSY * dSY_dF;
			
			double D = cosDSX * cosR2 * sinSY;
			double dD_dR1 = - sinDSX * cosR2 * sinSY;
			double dD_dR2 = - cosDSX * sinR2 * sinSY;
			double dD_dF = - sinDSX * cosR2 * sinSY * dSX_dF + cosDSX * cosR2 * cosSY * dSY_dF;
			
			double C = D - E;
			double dC_dR1 = dD_dR1 - dE_dR1; 
			double dC_dR2 = dD_dR2 - dE_dR2;
			double dC_dF = dD_dF - dE_dF;
			
			double B = sinDSX * sinSY;
			double dB_dR1 = cosDSX * sinSY;
			double dB_dR2 = 0;
			double dB_dF = cosDSX * sinSY * dSX_dF + sinDSX * cosSY * dSY_dF;
			
			double A = B / C;
			double dA_dR1 = (dB_dR1 * C - B * dC_dR1) / (C*C);
			double dA_dR2 = (dB_dR2 * C - B * dC_dR2) / (C*C);
			double dA_dF = (dB_dF * C - B * dC_dF) / (C*C);
			
			tx = Math.atan2(B, C) + kp.keyPointList.sphereRZ2;
			tmp = 1 + A * A;
			dTX_dR1 = dA_dR1 / tmp;
			dTX_dR2 = dA_dR2 / tmp;
			dTX_dF = dA_dF / tmp;
			dTX_dR3 = 1;
			
			// calc commons
			sinTX = Math.sin(tx);
			cosTX = Math.cos(tx);
			sinTY = Math.sin(ty);
			cosTY = Math.cos(ty);
		}
		
		private void calcLocalSphericalCoords() {
			double tmpx = (kp.doubleX - kp.keyPointList.cameraOriginX) * kp.keyPointList.cameraScale;
			double tmpy = (kp.doubleY - kp.keyPointList.cameraOriginY) * kp.keyPointList.cameraScale;
			
			double B = Math.sqrt(tmpx * tmpx + tmpy * tmpy);
			double A = B / kp.keyPointList.scaleZ;
			double dAdF = - B / (kp.keyPointList.scaleZ * kp.keyPointList.scaleZ) ;
			
			sx = Math.atan2(B, kp.keyPointList.scaleZ);
			dSX_dF = dAdF / (1.0 + A * A);
			
			sy = Math.atan2(tmpx, tmpy);
			dSY_dF = 0.0;
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
//		"\td=" + MathUtil.rad2degStr(dist0) + 
		"\tiSX=" + p1.kp.doubleX + 
		"\tiSY=" + p1.kp.doubleY + 
//		"\tSCamX=" + p1.kp.keyPointList.cameraOriginX + 
//		"\tSCamY=" + p1.kp.keyPointList.cameraOriginY + 
		"\tiTX=" + p2.kp.doubleX + 
		"\tiTY=" + p2.kp.doubleY + 
//		"\tTCamX=" + p2.kp.keyPointList.cameraOriginX + 
//		"\tTCamY=" + p2.kp.keyPointList.cameraOriginY + 
		"\tSR1=" + MathUtil.rad2degStr(p1.kp.keyPointList.sphereRZ1) + 
		"\tSR2=" + MathUtil.rad2degStr(p1.kp.keyPointList.sphereRY) + 
		"\tSR3=" + MathUtil.rad2degStr(p1.kp.keyPointList.sphereRZ2) + 
		"\tTR1=" + MathUtil.rad2degStr(p2.kp.keyPointList.sphereRZ1) + 
		"\tTR2=" + MathUtil.rad2degStr(p2.kp.keyPointList.sphereRY) + 
		"\tTR3=" + MathUtil.rad2degStr(p2.kp.keyPointList.sphereRZ2);
	}
}
