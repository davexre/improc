package com.slavi.improc.myadjust.sphere;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointPair;
import com.slavi.math.MathUtil;

public class SphereNorm {

	PointDerivatives p1 = new PointDerivatives();
	PointDerivatives p2 = new PointDerivatives();

	public double Dist;
	
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
	}
	
	public static class PointDerivatives {
		// R1, R2 -> long, lat of North pole of the world spherical coord 
		// 		system in the local (image) spherical coords system.
		// R1 = kp.keyPointList.rx
		// R2 = kp.keyPointList.ry
		// R3 = kp.keyPointList.rz (rotation of local coord system around its pole axis)
		// F  = kp.keyPointList.scaleZ (focal distance of the image)
		public KeyPoint kp;
		
		// tx, ty -> long, lat of the point in target spherical coords
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

		// sx, sy -> long, lat of the point in source/local spherical coords
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
			double sinDSX = Math.sin(sx - kp.keyPointList.rx);
			double cosDSX = Math.cos(sx - kp.keyPointList.rx);
			double sinSY = Math.sin(sy);
			double cosSY = Math.cos(sy);
			double sinR2 = Math.sin(kp.keyPointList.ry);
			double cosR2 = Math.cos(kp.keyPointList.ry);

			// dTY
			double I = cosR2 * cosSY * cosDSX;
			double dI_dR1 = cosR2 * cosSY * sinDSX;
			double dI_dR2 = - sinR2 * cosSY * cosDSX;
			double dI_dF = - cosR2 * (cosSY * sinDSX * dSX_dF + sinSY * cosDSX * dSY_dF);
			
			double H = sinR2 * sinSY;
			//double dH_dR1 = 0;
			double dH_dR2 = cosR2 * sinSY;
			double dH_dF = sinR2 * cosSY * dSY_dF;
			
			double G = H + I;
			double dG_dR1 = dI_dR1; // + dH_dR1;
			double dG_dR2 = dI_dR2 + dH_dR2;
			double dG_dF = dI_dF + dH_dF;
			
			ty = Math.asin(G);
			double tmp = Math.sqrt(1 - G * G);
			dTY_dR1 = dG_dR1 / tmp;
			dTY_dR2 = dG_dR2 / tmp;
			dTY_dF = dG_dF / tmp;
			dTY_dR3 = 0;

			// dTX
			double E = cosDSX * sinR2 * cosSY;
			double dE_dR1 = sinDSX * sinR2 * cosSY;
			double dE_dR2 = cosDSX * cosR2 * cosSY;
			double dE_dR3 = - sinR2 * (sinDSX * cosSY * dSX_dF + cosDSX * sinSY * dSY_dF);
			
			double D = cosR2 * sinSY;
			//double dD_dR1 = 0;
			double dD_dR2 = - sinR2 * sinSY;
			double dD_dF = cosR2 * cosSY * dSY_dF;
			
			double C = D - E;
			double dC_dR1 = - dE_dR1; // + dD_dR1;
			double dC_dR2 = dD_dR2 - dE_dR2;
			double dC_dF = dD_dF - dE_dR3;
			
			double B = sinDSX * cosSY;
			double dB_dR1 = - cosDSX * cosSY;
			double dB_dR2 = - sinDSX * sinSY;
			double dB_dF = cosDSX * cosSY * dSX_dF - sinDSX * sinSY * dSY_dF;
			
			double A = B / C;
			double dA_dR1 = (dB_dR1 * C - B * dC_dR1) / (C*C);
			double dA_dR2 = (dB_dR2 * C - B * dC_dR2) / (C*C);
			double dA_dF = (dB_dF * C - B * dC_dF) / (C*C);
			
			tx = kp.keyPointList.rz + MathUtil.C2PI - Math.atan(A);
			tmp = 1 + A * A;
			dTX_dR1 = -dA_dR1 / tmp;
			dTX_dR2 = -dA_dR2 / tmp;
			dTX_dF = -dA_dF / tmp;
			dTX_dR3 = 1;
			
			// calc commons
			sinTX = Math.sin(tx);
			cosTX = Math.cos(tx);
			sinTY = Math.sin(ty);
			cosTY = Math.cos(ty);
		}
		
		private void calcLocalSphericalCoords() {
			double x1 = kp.doubleX - kp.keyPointList.cameraOriginX;
			double y1 = kp.doubleY - kp.keyPointList.cameraOriginY;
			
			double C = x1 * x1 + y1 * y1 + kp.keyPointList.scaleZ * kp.keyPointList.scaleZ;
			double dCdF = 2.0 * kp.keyPointList.scaleZ;
			double B = y1 / Math.sqrt(C);
			double dBdF = -0.5 * y1 * Math.pow(C, -3.0/2.0) * dCdF;
			sy = Math.asin(B);
			dSY_dF = dBdF / Math.sqrt(1.0 - B * B);
			
			double A = x1 / kp.keyPointList.scaleZ;
			double dAdIF = - x1 / (kp.keyPointList.scaleZ * kp.keyPointList.scaleZ);
			sx = Math.atan(A);
			dSX_dF = dAdIF / (1.0 + A * A);
		}
	}
	
	private double calc_dDist_dParam(double dRX1_dP, double dRY1_dP, double dRX2_dP, double dRY2_dP) {
		double H = p2.tx - p1.tx;
		double sinH = Math.sin(H);
		double cosH = Math.cos(H);
		double dHdP = dRX2_dP - dRX1_dP;
		
		double L = p1.cosTY * p2.cosTY * cosH;
		double dLdP = -p1.sinTY * p2.cosTY * cosH * dRY1_dP
				-p1.cosTY * p2.sinTY * cosH * dRY2_dP
				-p1.cosTY * p2.cosTY * sinH * dHdP;
		double K = p1.sinTY * p2.sinTY;
		double dKdP = p1.cosTY * p2.sinTY * dRY1_dP + p1.sinTY * p2.cosTY * dRY2_dP;
		
		double J = p1.sinTY * p2.cosTY * cosH;
		double dJdP = p1.cosTY * p2.cosTY * cosH * dRY1_dP 
				-p1.sinTY * p2.sinTY * cosH * dRY2_dP 
				-p1.sinTY * p2.cosTY * sinH * dHdP;
		double I = p1.cosTY * p2.sinTY;
		double dIdP = -p1.sinTY * p2.sinTY * dRY1_dP + p1.cosTY * p2.cosTY * dRY2_dP;
		
		double G = I - J;
		double dGdP = dIdP - dJdP;
		
		double F = p2.cosTY * sinH;
		double dFdP = -p2.sinTY * sinH * dRY2_dP + p2.cosTY * cosH * dHdP;
		double E = F * F + G * G;
		double dEdP = 2 * (F * dFdP + G * dGdP);
		
		double C = K + L;
		double dCdP = dKdP + dLdP;
		double B = Math.sqrt(E);
		if (B == 0.0) {
			Dist = 0.0;
			return 0.0;
		}
		
		double dBdP = dEdP / (2 * B);
		
		double A = B / C;
		double dAdP = (dBdP * C - B * dCdP) / (C * C);
		
		Dist = Math.atan(A);
		double dDistdP = dAdP / (1 + A * A);
		
		return dDistdP;
	}
}
