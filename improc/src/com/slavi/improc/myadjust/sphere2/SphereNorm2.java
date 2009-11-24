package com.slavi.improc.myadjust.sphere2;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointPair;
import com.slavi.math.MathUtil;
import com.slavi.math.SphericalCoordsLongZen;

public class SphereNorm2 {

	PointDerivatives p1 = new PointDerivatives();
	PointDerivatives p2 = new PointDerivatives();

	public double Dist;
	public double dist0;
	
	// Private derivatives 
	public double dDist_dIX1;
	public double dDist_dIY1;
	public double dDist_dIZ1;
	public double dDist_dIF1;

	public double dDist_dIX2;
	public double dDist_dIY2;
	public double dDist_dIZ2;
	public double dDist_dIF2;

	public void setKeyPointPair(KeyPointPair kpp) {
		double source[] = new double[2];
		double target[] = new double[2];
		SpherePanoTransformer2.transformForeward(kpp.sourceSP.doubleX, kpp.sourceSP.doubleY, kpp.sourceSP.keyPointList, source);
		SpherePanoTransformer2.transformForeward(kpp.targetSP.doubleX, kpp.targetSP.doubleY, kpp.targetSP.keyPointList, target);
		dist0 = SphericalCoordsLongZen.getSphericalDistance(source[0], source[1], target[0], target[1]);

		p1.setKeyPoint(kpp.sourceSP);
		p2.setKeyPoint(kpp.targetSP);
		
		dDist_dIX1 = calc_dDist_dParam(p1.dRX_dIX, p1.dRY_dIX, 0, 0);
		dDist_dIY1 = calc_dDist_dParam(p1.dRX_dIY, p1.dRY_dIY, 0, 0);
		dDist_dIZ1 = calc_dDist_dParam(p1.dRX_dIZ, p1.dRY_dIZ, 0, 0);
		dDist_dIF1 = calc_dDist_dParam(p1.dRX_dIF, p1.dRY_dIF, 0, 0);

		dDist_dIX2 = calc_dDist_dParam(0, 0, p2.dRX_dIX, p2.dRY_dIX);
		dDist_dIY2 = calc_dDist_dParam(0, 0, p2.dRX_dIY, p2.dRY_dIY);
		dDist_dIZ2 = calc_dDist_dParam(0, 0, p2.dRX_dIZ, p2.dRY_dIZ);
		dDist_dIF2 = calc_dDist_dParam(0, 0, p2.dRX_dIF, p2.dRY_dIF);
		if (Math.abs(dist0 - Dist) > (MathUtil.epsAngle * 10)) {
			System.out.println("LX1=" + MathUtil.rad2degStr(p1.sx) + "\tLY1=" + MathUtil.rad2degStr(p1.sy));
			System.out.println("LX2=" + MathUtil.rad2degStr(p2.sx) + "\tLY2=" + MathUtil.rad2degStr(p2.sy));
			
			System.out.println(
					"p1.x=" + MathUtil.rad2degStr(source[0]) + 
					"\tp1.x1=" + MathUtil.rad2degStr(p1.rx) + 
					"\tp1.y=" + MathUtil.rad2degStr(source[1]) + 
					"\tp1.y1=" + MathUtil.rad2degStr(p1.ry) + 
					"\tp2.x=" + MathUtil.rad2degStr(target[0]) + 
					"\tp2.x1=" + MathUtil.rad2degStr(p2.rx) + 
					"\tp2.y=" + MathUtil.rad2degStr(target[1]) + 
					"\tp2.y1=" + MathUtil.rad2degStr(p2.ry) 
					);
			
			System.out.println(toString());
		}
	}
	
	public static class PointDerivatives {
		// IX, IY -> long, lat of North pole of the world spherical coord 
		// 		system in the local (image) spherical coords system.
		// IX = kp.keyPointList.sphereRZ1
		// IY = kp.keyPointList.sphereRY
		// IZ = kp.keyPointList.sphereRZ2 (rotation of local coord system around its pole axis)
		// IF = kp.keyPointList.scaleZ (focal distance of the image)
		public KeyPoint kp;
		
		// rx, ry -> long, lat of the point in world spherical coords
		public double rx, ry;
		public double sinRX, cosRX;
		public double sinRY, cosRY;
		// Private derivatives 
		public double dRX_dIX;
		public double dRX_dIY;
		public double dRX_dIZ;
		public double dRX_dIF;
		
		public double dRY_dIX;
		public double dRY_dIY;
		public double dRY_dIZ;
		public double dRY_dIF;

		// sx, sy -> long, lat of the point in local spherical coords
		private double sx, sy;
		// Private derivatives
		private double dSX_dIF;
		private double dSY_dIF;

		public void setKeyPoint(KeyPoint kp) {
			this.kp = kp;
			calcLocalSphericalCoords();
			calcWorldSphericalCoords();
		}
		
		private void calcWorldSphericalCoords() {
			double sinDSX = Math.sin(sx + kp.keyPointList.sphereRZ1);
			double cosDSX = Math.cos(sx + kp.keyPointList.sphereRZ1);
			double sinSY = Math.sin(sy);
			double cosSY = Math.cos(sy);
			double sinIY = Math.sin(kp.keyPointList.sphereRY);
			double cosIY = Math.cos(kp.keyPointList.sphereRY);

			// dRY
			double H = sinSY * sinIY * cosDSX;
			double dH_dIX = - sinSY * sinIY * sinDSX;
			double dH_dIY = sinSY * cosIY * cosDSX;
			double dH_dIF = cosSY * sinIY * cosDSX * dSY_dIF - sinSY * sinIY * sinDSX * dSX_dIF;
			
			double G = cosSY * cosIY;
			double dG_dIX = 0;
			double dG_dIY = - cosSY * sinIY;
			double dG_dIF = - sinSY * cosIY * dSY_dIF;
			
			double F = G + H;
			double dF_dIX = dG_dIX + dH_dIX;
			double dF_dIY = dG_dIY + dH_dIY;
			double dF_dIF = dG_dIF + dH_dIF;
			
			ry = Math.acos(F);
			double tmp = - Math.sqrt(1 - F * F);
			dRY_dIX = dF_dIX / tmp;
			dRY_dIY = dF_dIY / tmp;
			dRY_dIF = dF_dIF / tmp;
			dRY_dIZ = 0;

			// dRX			
			double E = sinIY * cosSY;
			double dE_dIX = 0;
			double dE_dIY = cosIY * cosSY;
			double dE_dIF = - sinIY * sinSY * dSY_dIF;
			
			double D = cosDSX * cosIY * sinSY;
			double dD_dIX = - sinDSX * cosIY * sinSY;
			double dD_dIY = - cosDSX * sinIY * sinSY;
			double dD_dIF = - sinDSX * cosIY * sinSY * dSX_dIF + cosDSX * cosIY * cosSY * dSY_dIF;
			
			double C = D - E;
			double dC_dIX = dD_dIX - dE_dIX; 
			double dC_dIY = dD_dIY - dE_dIY;
			double dC_dIF = dD_dIF - dE_dIF;
			
			double B = sinDSX * sinSY;
			double dB_dIX = cosDSX * sinSY;
			double dB_dIY = 0;
			double dB_dIF = cosDSX * sinSY * dSX_dIF + sinDSX * cosSY * dSY_dIF;
			
			double A = B / C;
			double dA_dIX = (dB_dIX * C - B * dC_dIX) / (C*C);
			double dA_dIY = (dB_dIY * C - B * dC_dIY) / (C*C);
			double dA_dIF = (dB_dIF * C - B * dC_dIF) / (C*C);
			
			rx = Math.atan2(B, C) + kp.keyPointList.sphereRZ2;
			tmp = 1 + A * A;
			dRX_dIX = dA_dIX / tmp;
			dRX_dIY = dA_dIY / tmp;
			dRX_dIF = dA_dIF / tmp;
			dRX_dIZ = 1;
			
			// calc commons
			sinRX = Math.sin(rx);
			cosRX = Math.cos(rx);
			sinRY = Math.sin(ry);
			cosRY = Math.cos(ry);
		}
		
		private void calcLocalSphericalCoords() {
			double tmpx = (kp.doubleX - kp.keyPointList.cameraOriginX) * kp.keyPointList.cameraScale;
			double tmpy = (kp.doubleY - kp.keyPointList.cameraOriginY) * kp.keyPointList.cameraScale;
			
			double B = Math.sqrt(tmpx * tmpx + tmpy * tmpy);
			double A = B / kp.keyPointList.scaleZ;
			double dAdIF = - B / (kp.keyPointList.scaleZ * kp.keyPointList.scaleZ) ;
			
			sy = Math.atan2(B, kp.keyPointList.scaleZ);
			dSY_dIF = dAdIF / (1.0 + A * A);
			
			sx = Math.atan2(tmpy, tmpx);
			dSX_dIF = 0.0;
		}
	}
	
	private double calc_dDist_dParam(double dRX1_dP, double dRY1_dP, double dRX2_dP, double dRY2_dP) {
		double H = p2.rx - p1.rx;
		double dHdP = dRX2_dP - dRX1_dP;
		double cosH = Math.cos(H);
		double sinH = Math.sin(H);

		double L = p1.sinRY * p2.sinRY * cosH;
		double dLdP =
			p1.cosRY * p2.sinRY * cosH * dRY1_dP
			+ p1.sinRY * p2.cosRY * cosH * dRY2_dP
			- p1.sinRY * p2.sinRY * sinH * dHdP;
		
		double K = p1.cosRY * p2.cosRY;
		double dKdP = 
			- p1.sinRY * p2.cosRY * dRY1_dP
			- p1.cosRY * p2.sinRY * dRY2_dP;
		
		double J = p1.cosRY * p2.sinRY * cosH;
		double dJdP = 
			- p1.sinRY * p2.sinRY * cosH * dRY1_dP
			+ p1.cosRY * p2.cosRY * cosH * dRY2_dP
			- p1.cosRY * p2.sinRY * sinH * dHdP;
		
		double I = p1.sinRY * p2.cosRY;
		double dIdP = p1.cosRY * p2.cosRY * dRY1_dP - p1.sinRY * p2.sinRY * dRY2_dP;
		
		double G = I - J;
		double dGdP = dIdP - dJdP;
				
		double F = p2.sinRY * sinH;
		double dFdP = p2.cosRY * sinH * dRY2_dP + p2.sinRY * cosH * dHdP;
		
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
		"\tsX=" + p1.kp.doubleX + 
		"\tsY=" + p1.kp.doubleY + 
//		"\tsCamX=" + p1.kp.keyPointList.cameraOriginX + 
//		"\tsCamY=" + p1.kp.keyPointList.cameraOriginY + 
		"\ttX=" + p2.kp.doubleX + 
		"\ttY=" + p2.kp.doubleY + 
//		"\ttCamX=" + p2.kp.keyPointList.cameraOriginX + 
//		"\ttCamY=" + p2.kp.keyPointList.cameraOriginY + 
		"\tsZ1=" + MathUtil.rad2degStr(p1.kp.keyPointList.sphereRZ1) + 
		"\tsY=" + MathUtil.rad2degStr(p1.kp.keyPointList.sphereRY) + 
		"\tsZ2=" + MathUtil.rad2degStr(p1.kp.keyPointList.sphereRZ2) + 
		"\ttZ1=" + MathUtil.rad2degStr(p2.kp.keyPointList.sphereRZ1) + 
		"\ttY=" + MathUtil.rad2degStr(p2.kp.keyPointList.sphereRY) + 
		"\ttZ2=" + MathUtil.rad2degStr(p2.kp.keyPointList.sphereRZ2);
	}
}
