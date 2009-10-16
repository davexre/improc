package com.slavi.improc.myadjust;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointPair;

public class SphereNorm {

	PointDerivatives p1 = new PointDerivatives();
	PointDerivatives p2 = new PointDerivatives();

	public double Dist;
	
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
	}
	
	public static class PointDerivatives {
		// IX, IY -> long, lat of North pole of the world spherical coord 
		// 		system in the local (image) spherical coords system.
		// IX = kp.keyPointList.rx
		// IY = kp.keyPointList.ry
		// IZ = kp.keyPointList.rz (rotation of local coord system around its pole axis)
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
			double sinDSX = Math.sin(sx - kp.keyPointList.rx);
			double cosDSX = Math.cos(sx - kp.keyPointList.rx);
			double sinSY = Math.sin(sy);
			double cosSY = Math.cos(sy);
			
			double sinIY = Math.sin(kp.keyPointList.ry);
			double cosIY = Math.cos(kp.keyPointList.ry);

			// dRY
			double H = cosIY * cosSY * cosDSX;
			double dH_dIX = cosIY * cosSY * sinDSX;
			double dH_dIY = - sinIY * cosSY * cosDSX;
			double dH_dIF = - cosIY * (cosSY * sinDSX * dSX_dIF + sinSY * cosDSX * dSY_dIF);
			
			double G = sinIY * sinSY;
			//double dG_dIX = 0;
			double dG_dIY = cosIY * sinSY;
			double dG_dIF = sinIY * cosSY * dSY_dIF;
			
			double F = G + H;
			double dF_dIX = dH_dIX; // + dG_dIX;
			double dF_dIY = dH_dIY + dG_dIY;
			double dF_dIF = dH_dIF + dG_dIF;
			
			ry = Math.asin(F);
			double tmp = Math.sqrt(1 - F * F);
			dRY_dIX = dF_dIX / tmp;
			dRY_dIY = dF_dIY / tmp;
			dRY_dIF = dF_dIF / tmp;
			dRY_dIZ = 0;

			// dRX			
			double E = cosDSX * sinIY * cosSY;
			double dE_dIX = sinDSX * sinIY * cosSY;
			double dE_dIY = cosDSX * cosIY * cosSY;
			double dE_dIF = - sinIY * (sinDSX * cosSY * dSX_dIF + sinSY * cosDSX * dSY_dIF);
			
			double D = cosIY * sinSY;
			//double dD_dIX = 0;
			double dD_dIY = - sinIY * sinSY;
			double dD_dIF = cosIY * cosSY * dSY_dIF;
			
			double C = D - E;
			double dC_dIX = - dE_dIX; // + dD_dIX;
			double dC_dIY = dD_dIY - dE_dIY;
			double dC_dIF = dD_dIF - dE_dIF;
			
			double B = sinDSX * cosSY;
			double dB_dIX = - cosDSX * cosSY;
			double dB_dIY = - sinDSX * sinSY;
			double dB_dIF = cosDSX * cosSY * dSX_dIF - sinDSX * sinSY * dSY_dIF;
			
			double A = B / C;
			double dA_dIX = (dB_dIX * C - B * dC_dIX) / (C*C);
			double dA_dIY = (dB_dIY * C - B * dC_dIY) / (C*C);
			double dA_dIF = (dB_dIF * C - B * dC_dIF) / (C*C);
			
			rx = kp.keyPointList.rz + Math.atan(A);
			dRX_dIX = dA_dIX / (1 + A * A);
			dRX_dIY = dA_dIY / (1 + A * A);
			dRX_dIF = dA_dIF / (1 + A * A);
			dRX_dIZ = 1;
			
			// calc commons
			sinRX = Math.sin(rx);
			cosRX = Math.cos(rx);
			sinRY = Math.sin(ry);
			cosRY = Math.cos(ry);
		}
		
		private void calcLocalSphericalCoords() {
			double x1 = kp.doubleX - kp.keyPointList.cameraOriginX;
			double y1 = kp.doubleY - kp.keyPointList.cameraOriginY;
			
			double C = x1 * x1 + y1 * y1 + kp.keyPointList.scaleZ * kp.keyPointList.scaleZ;
			double dCdIF = 2.0 * kp.keyPointList.scaleZ;
			double B = y1 / Math.sqrt(C);
			double dBdIF = -0.5 * y1 * Math.pow(C, -3.0/2.0) * dCdIF;
			sy = Math.asin(B);
			dSY_dIF = dBdIF / Math.sqrt(1.0 - B * B);
			
			double A = x1 / kp.keyPointList.scaleZ;
			double dAdIF = - x1 / (kp.keyPointList.scaleZ * kp.keyPointList.scaleZ);
			sx = Math.atan(A);
			dSX_dIF = dAdIF / (1.0 + A * A);
		}
	}
	
	private double calc_dDist_dParam(double dRX1_dP, double dRY1_dP, double dRX2_dP, double dRY2_dP) {
		double H = p1.rx - p2.rx;
		double sinH = Math.sin(H);
		double cosH = Math.cos(H);
		double dHdP = dRX1_dP - dRX2_dP;
		
		double L = p1.cosRY * p2.cosRY * cosH;
		double dLdP = -p1.sinRY * p2.cosRY * cosH * dRY1_dP
				-p1.cosRY * p2.sinRY * cosH * dRY2_dP
				-p1.cosRY * p2.cosRY * sinH * dHdP;
		double K = p1.sinRY * p2.sinRY;
		double dKdP = p1.cosRY * p2.sinRY * dRY1_dP + p1.sinRY * p2.cosRY * dRY2_dP;
		
		double J = p1.sinRY * p2.cosRY * cosH;
		double dJdP = p1.cosRY * p2.cosRY * cosH * dRY1_dP 
				-p1.sinRY * p2.sinRY * cosH * dRY2_dP 
				-p1.sinRY * p2.cosRY * sinH * dHdP;
		double I = p1.cosRY * p2.sinRY;
		double dIdP = -p1.sinRY * p2.sinRY * dRY1_dP + p1.cosRY * p2.cosRY * dRY2_dP;
		
		double G = I - J;
		double dGdP = dIdP - dJdP;
		
		double F = p2.cosRY * sinH;
		double dFdP = -p2.sinRY * sinH * dRY2_dP + p2.cosRY * cosH * dHdP;
		double E = F * F + G * G;
		double dEdP = 2 * F * dFdP + 2 * G * dGdP;
		
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
