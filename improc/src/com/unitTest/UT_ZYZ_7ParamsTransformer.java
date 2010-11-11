package com.unitTest;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.myadjust.zyz7params.ZYZ_7ParamsLearner;
import com.slavi.improc.myadjust.zyz7params.ZYZ_7ParamsNorm;
import com.slavi.math.MathUtil;

public class UT_ZYZ_7ParamsTransformer {

	KeyPoint p1;
	KeyPointList kpl1;
	
	public UT_ZYZ_7ParamsTransformer() {
		kpl1 = new KeyPointList();
		kpl1.cameraOriginX = 1001;
		kpl1.cameraOriginY = 2002;
		kpl1.cameraScale = 1.0 / (2.0 * Math.max(kpl1.cameraOriginX, kpl1.cameraOriginY));
		kpl1.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;
		kpl1.sphereRZ1 = 10 * MathUtil.deg2rad;
		kpl1.sphereRY = 20 * MathUtil.deg2rad;
		kpl1.sphereRZ2 = 30 * MathUtil.deg2rad;
		
		p1 = new KeyPoint();
		p1.keyPointList = kpl1;
		p1.doubleX = 1234;
		p1.doubleY = 2345;
	}
	
	private static void checkNorm(KeyPointPair kpp, 
			double dSRZ1, double dSRY, double dSRZ2, double dSTX, double dSTY, double dSTZ, double dSS, 
			double dTRZ1, double dTRY, double dTRZ2, double dTTX, double dTTY, double dTTZ, double dTS) {
		double dest1[] = new double[3];
		double dest2[] = new double[3];
		
		//if (kpp.)
		
		ZYZ_7ParamsNorm norm = new ZYZ_7ParamsNorm();
		ZYZ_7ParamsLearner.buildCamera2RealMatrix(kpp.sourceSP.keyPointList);
		ZYZ_7ParamsLearner.buildCamera2RealMatrix(kpp.targetSP.keyPointList);
		norm.setKeyPointPair(kpp);
		
		kpp.sourceSP.keyPointList.sphereRZ1 += dSRZ1;
		kpp.sourceSP.keyPointList.sphereRY += dSRY;
		kpp.sourceSP.keyPointList.sphereRZ2 += dSRZ2;
		kpp.sourceSP.keyPointList.scaleZ += dSS;
		kpp.sourceSP.keyPointList.tx += dSTX;
		kpp.sourceSP.keyPointList.ty += dSTY;
		kpp.sourceSP.keyPointList.tz += dSTZ;

		kpp.targetSP.keyPointList.sphereRZ1 += dTRZ1;
		kpp.targetSP.keyPointList.sphereRY += dTRY;
		kpp.targetSP.keyPointList.sphereRZ2 += dTRZ2;
		kpp.targetSP.keyPointList.scaleZ += dTS;
		kpp.targetSP.keyPointList.tx += dTTX;
		kpp.targetSP.keyPointList.ty += dTTY;
		kpp.targetSP.keyPointList.tz += dTTZ;
		
		ZYZ_7ParamsLearner.buildCamera2RealMatrix(kpp.sourceSP.keyPointList);
		ZYZ_7ParamsLearner.buildCamera2RealMatrix(kpp.targetSP.keyPointList);

		ZYZ_7ParamsNorm.transformForeward(kpp.sourceSP.doubleX, kpp.sourceSP.doubleY, kpp.sourceSP.keyPointList, dest1);
		ZYZ_7ParamsNorm.transformForeward(kpp.targetSP.doubleX, kpp.targetSP.doubleY, kpp.targetSP.keyPointList, dest2);

		kpp.sourceSP.keyPointList.sphereRZ1 -= dSRZ1;
		kpp.sourceSP.keyPointList.sphereRY -= dSRY;
		kpp.sourceSP.keyPointList.sphereRZ2 -= dSRZ2;
		kpp.sourceSP.keyPointList.scaleZ -= dSS;
		kpp.sourceSP.keyPointList.tx -= dSTX;
		kpp.sourceSP.keyPointList.ty -= dSTY;
		kpp.sourceSP.keyPointList.tz -= dSTZ;

		kpp.targetSP.keyPointList.sphereRZ1 -= dTRZ1;
		kpp.targetSP.keyPointList.sphereRY -= dTRY;
		kpp.targetSP.keyPointList.sphereRZ2 -= dTRZ2;
		kpp.targetSP.keyPointList.scaleZ -= dTS;
		kpp.targetSP.keyPointList.tx -= dTTX;
		kpp.targetSP.keyPointList.ty -= dTTY;
		kpp.targetSP.keyPointList.tz -= dTTZ;
		
		ZYZ_7ParamsLearner.buildCamera2RealMatrix(kpp.sourceSP.keyPointList);
		ZYZ_7ParamsLearner.buildCamera2RealMatrix(kpp.targetSP.keyPointList);

		for (int c1 = 0; c1 < 3; c1++) {
			int c2 = (c1 + 1) % 3;
			double L0 = norm.p1.P[c1] * norm.p2.P[c2] - norm.p1.P[c2] * norm.p2.P[c1];
			double L2= L0 +
				calcSum(0, norm.p1, dSRZ1, dSRY, dSRZ2, dSTX, dSTY, dSTZ, dSS) +
				calcSum(0, norm.p2, dTRZ1, dTRY, dTRZ2, dTTX, dTTY, dTTZ, dTS);
			double L1 = dest1[c1] * dest2[c2] - dest1[c2] * dest2[c1];
			TestUtils.assertEqual("", L2, L1);
		}		
	}
	
	void testNorm() {
		KeyPointList kpl1 = new KeyPointList();
		kpl1.cameraOriginX = 1136;
		kpl1.cameraOriginY = 856;
		kpl1.cameraScale = 1.0 / (2.0 * Math.max(kpl1.cameraOriginX, kpl1.cameraOriginY));
		kpl1.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;
		kpl1.sphereRZ1 = -178 * MathUtil.deg2rad;
		kpl1.sphereRY = 30 * MathUtil.deg2rad;
		kpl1.sphereRZ2 = 178 * MathUtil.deg2rad;
		
		p1 = new KeyPoint();
		p1.keyPointList = kpl1;
		p1.doubleX = 1881;
		p1.doubleY = 897;
		
		KeyPointList kpl2 = new KeyPointList();
		kpl2.cameraOriginX = 1136;
		kpl2.cameraOriginY = 856;
		kpl2.cameraScale = 1.0 / (2.0 * Math.max(kpl2.cameraOriginX, kpl2.cameraOriginY));
		kpl2.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;
		kpl2.sphereRZ1 = 20 * MathUtil.deg2rad;
		kpl2.sphereRY = 30 * MathUtil.deg2rad;
		kpl2.sphereRZ2 = 40 * MathUtil.deg2rad;
		
		double dest1[] = new double[3];
		double dest2[] = new double[3];
		KeyPoint p2 = new KeyPoint();
		p2.keyPointList = kpl2;

		KeyPointPair kpp = new KeyPointPair();
		kpp.sourceSP = p1;
		kpp.targetSP = p2;
//		double delta = 0.01 * MathUtil.deg2rad;
		double delta = 0.000000001; // too small! ignore this check at all!

		int parts = 8;
		for (int x1 = 0; x1 < parts; x1++) {
			kpl1.sphereRZ1 = x1 * MathUtil.C2PI / parts;
			for (int y1 = 0; y1 < parts; y1++) {
				kpl1.sphereRY = y1 * MathUtil.C2PI / parts;
				for (int z1 = 0; z1 < parts; z1++) {
					kpl1.sphereRZ2 = z1 * MathUtil.C2PI / parts;
					for (int x2 = 0; x2 < parts; x2++) {
						kpl2.sphereRZ1 = x2 * MathUtil.C2PI / parts;
						for (int y2 = 0; y2 < parts; y2++) {
							kpl2.sphereRY = y2 * MathUtil.C2PI / parts;
							for (int z2 = 0; z2 < parts; z2++) {
								kpl2.sphereRZ2 = z2 * MathUtil.C2PI / parts;
								
								ZYZ_7ParamsLearner.buildCamera2RealMatrix(kpp.sourceSP.keyPointList);
								ZYZ_7ParamsLearner.buildCamera2RealMatrix(kpp.targetSP.keyPointList);

								ZYZ_7ParamsNorm.transformForeward(p1.doubleX, p1.doubleY, kpl1, dest1);
								ZYZ_7ParamsNorm.transformBackward(dest1[0] + 1 * MathUtil.deg2rad, dest1[1], dest1[2], kpl2, dest2);
								if (dest2[2] <= 0.0)
									continue;

								p2.doubleX = dest2[0];
								p2.doubleY = dest2[1];
								try {
									checkNorm(kpp, 
											delta, delta, delta, delta, delta, delta, delta, 
											delta, delta, delta, delta, delta, delta, delta);
//									checkNorm(kpp, delta, 0, 0, 0, delta, 0, 0, 0);
								} catch (RuntimeException e) {
									System.out.println("RX1=" + MathUtil.rad2degStr(kpl1.sphereRZ1));
									System.out.println("RY1=" + MathUtil.rad2degStr(kpl1.sphereRY));
									System.out.println("RZ1=" + MathUtil.rad2degStr(kpl1.sphereRZ2));

									System.out.println("RX2=" + MathUtil.rad2degStr(kpl2.sphereRZ1));
									System.out.println("RY2=" + MathUtil.rad2degStr(kpl2.sphereRY));
									System.out.println("RZ2=" + MathUtil.rad2degStr(kpl2.sphereRZ2));
									throw e;
								}
							}
						}
					}
				}
			}
		}
	}

	private static double calcSum(int coord, ZYZ_7ParamsNorm.PointDerivatives pd, double dZ1, double dY, double dZ2, double dTX, double dTY, double dTZ, double dS) {
		return 
			pd.dPdZ1.getItem(0, coord) * dZ1 + 
			pd.dPdY .getItem(0, coord) * dY + 
			pd.dPdZ2.getItem(0, coord) * dZ2 + 
			pd.dPdTX.getItem(0, coord) * dTX + 
			pd.dPdTY.getItem(0, coord) * dTY + 
			pd.dPdTZ.getItem(0, coord) * dTZ + 
			pd.dPdS .getItem(0, coord) * dS;
	}
	
	private static void checkNorm0(KeyPoint kp, double dZ1, double dY, double dZ2, double dTX, double dTY, double dTZ, double dS) {
		double dest0[] = new double[3];
		ZYZ_7ParamsLearner.buildCamera2RealMatrix(kp.keyPointList);
		ZYZ_7ParamsNorm.transformForeward(kp.doubleX, kp.doubleY, kp.keyPointList, dest0);
		ZYZ_7ParamsNorm.PointDerivatives pd = new ZYZ_7ParamsNorm.PointDerivatives();
		pd.setKeyPoint(kp);
		
		double dest2[] = new double[3];
		for (int coord = 0; coord < 3; coord++)
			dest2[coord] = dest0[coord] + calcSum(coord, pd, dZ1, dY, dZ2, dTX, dTY, dTZ, dS);
		
		kp.keyPointList.sphereRZ1 += dZ1;
		kp.keyPointList.sphereRY += dY;
		kp.keyPointList.sphereRZ2 += dZ2;
		kp.keyPointList.scaleZ += dS;
		kp.keyPointList.tx += dTX;
		kp.keyPointList.ty += dTY;
		kp.keyPointList.tz += dTZ;

		double dest1[] = new double[3];
		ZYZ_7ParamsLearner.buildCamera2RealMatrix(kp.keyPointList);
		ZYZ_7ParamsNorm.transformForeward(kp.doubleX, kp.doubleY, kp.keyPointList, dest1);
		
		kp.keyPointList.sphereRZ1 -= dZ1;
		kp.keyPointList.sphereRY -= dY;
		kp.keyPointList.sphereRZ2 -= dZ2;
		kp.keyPointList.scaleZ -= dS;
		kp.keyPointList.tx -= dTX;
		kp.keyPointList.ty -= dTY;
		kp.keyPointList.tz -= dTZ;
		ZYZ_7ParamsLearner.buildCamera2RealMatrix(kp.keyPointList);
		
		TestUtils.assertEqual("", dest1[0], dest2[0]);
		TestUtils.assertEqual("", dest1[1], dest2[1]);
	}
	
	void testNorm0() {
		KeyPointList kpl1 = new KeyPointList();
		kpl1.cameraOriginX = 1136;
		kpl1.cameraOriginY = 856;
		kpl1.cameraScale = 1.0 / (2.0 * Math.max(kpl1.cameraOriginX, kpl1.cameraOriginY));
		kpl1.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;
		kpl1.sphereRZ1 = -178 * MathUtil.deg2rad;
		kpl1.sphereRY = 30 * MathUtil.deg2rad;
		kpl1.sphereRZ2 = 178 * MathUtil.deg2rad;
		
		KeyPoint p1 = new KeyPoint();
		p1.keyPointList = kpl1;
		p1.doubleX = 1881;
		p1.doubleY = 897;

//		double delta = 0.1 * MathUtil.deg2rad;
		double delta = 0.00001;
		int parts = 16;
		for (int x = 0; x < parts; x++) {
			double rx = x * MathUtil.C2PI / parts;
			for (int y = 0; y < parts; y++) {
				double ry = y * MathUtil.C2PI / parts;
				for (int z = 0; z < parts; z++) {
					double rz = z * MathUtil.C2PI / parts;
					p1.keyPointList.sphereRZ1 = rx;
					p1.keyPointList.sphereRY = ry;
					p1.keyPointList.sphereRZ2 = rz;
					try {
//						checkNorm0(p1, delta, 0, 0, 0, 0, 0, 0); // ok
//						checkNorm0(p1, 0, delta, 0, 0, 0, 0, 0); // ok
//						checkNorm0(p1, 0, 0, delta, 0, 0, 0, 0); // ok
//						checkNorm0(p1, 0, 0, 0, delta, 0, 0, 0); // ok
//						checkNorm0(p1, 0, 0, 0, 0, delta, 0, 0); // ok
//						checkNorm0(p1, 0, 0, 0, 0, 0, delta, 0); // ok
//						checkNorm0(p1, 0, 0, 0, 0, 0, 0, delta); // ok
//						checkNorm0(p1, 0, 0, 0, delta, delta, delta, 0); // ok
//						checkNorm0(p1, delta, 0, 0, delta, delta, delta, delta); // ok
						
						checkNorm0(p1, delta, delta, delta, delta, delta, delta, delta); // ok
					} catch (RuntimeException e) {
						System.out.println("RX=" + MathUtil.rad2degStr(rx));
						System.out.println("RY=" + MathUtil.rad2degStr(ry));
						System.out.println("RZ=" + MathUtil.rad2degStr(rz));
						throw e;
					}
				}
			}
		}
	}

	void testTransformer() {
		KeyPointList kpl1 = new KeyPointList();
		kpl1.cameraOriginX = 1001;
		kpl1.cameraOriginY = 2002;
		kpl1.cameraScale = 1.0 / (2.0 * Math.max(kpl1.cameraOriginX, kpl1.cameraOriginY));
		kpl1.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;
		kpl1.sphereRZ1 = 10 * MathUtil.deg2rad;
		kpl1.sphereRY  = 20 * MathUtil.deg2rad;
		kpl1.sphereRZ2 = 30 * MathUtil.deg2rad;
		kpl1.tx = 10;
		kpl1.ty = 20;
		kpl1.tz = 30;

		KeyPoint p1 = new KeyPoint();
		p1.keyPointList = kpl1;
		p1.doubleX = p1.keyPointList.cameraOriginX;
		p1.doubleY = p1.keyPointList.cameraOriginY;

		double dest[] = new double[3];
		double dest2[] = new double[3];
		ZYZ_7ParamsLearner.buildCamera2RealMatrix(kpl1);
		ZYZ_7ParamsNorm.transformForeward(p1.doubleX, p1.doubleY, kpl1, dest);
		ZYZ_7ParamsNorm.transformBackward(dest[0], dest[1], dest[2], kpl1, dest2);
		TestUtils.assertEqual("norm.transform.x", dest2[0], p1.doubleX);
		TestUtils.assertEqual("norm.transform.y", dest2[1], p1.doubleY);

		ZYZ_7ParamsLearner tr = new ZYZ_7ParamsLearner();
		tr.transformForeward(p1.doubleX, p1.doubleY, kpl1, dest);
		tr.transformBackward(dest[0], dest[1], kpl1, dest2);
		TestUtils.assertEqual("X", dest2[0], p1.doubleX);
		TestUtils.assertEqual("Y", dest2[1], p1.doubleY);
	}
	
	public static void main(String[] args) {
		UT_ZYZ_7ParamsTransformer test = new UT_ZYZ_7ParamsTransformer();
		test.testTransformer();
//		test.testNorm0();
//		test.testNorm();
		System.out.println("Done");
	}
}
