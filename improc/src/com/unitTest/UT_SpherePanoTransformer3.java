package com.unitTest;

import java.util.ArrayList;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.myadjust.sphere3.SphereNorm3;
import com.slavi.improc.myadjust.sphere3.SpherePanoTransformLearner3;
import com.slavi.improc.myadjust.sphere3.SpherePanoTransformer3;
import com.slavi.math.MathUtil;
import com.slavi.math.RotationZXZ;
import com.slavi.math.SphericalCoordsLongZen;
import com.slavi.math.matrix.Matrix;

public class UT_SpherePanoTransformer3 {

	KeyPoint p1;
	KeyPointList kpl1;
	
	public UT_SpherePanoTransformer3() {
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
	
	void testSpherePanoTransformerRotate() {
		double rot[] = new double[3];
		double dest1[] = new double[2];
		double dest2[] = new double[2];
		double dest3[] = new double[2];
		rot[0] = 10 * MathUtil.deg2rad;
		rot[1] = 20 * MathUtil.deg2rad;
		rot[2] = 30 * MathUtil.deg2rad;
		dest1[0] = 20 * MathUtil.deg2rad;
		dest1[1] = 50 * MathUtil.deg2rad;
		SpherePanoTransformer3.rotateForeward(dest1[0], dest1[1], rot[0], rot[1], rot[2], dest2);
		SpherePanoTransformer3.rotateBackward(dest2[0], dest2[1], rot[0], rot[1], rot[2], dest3);
		TestUtils.assertEqualAngle("", dest1[0], dest3[0]);
		TestUtils.assertEqualAngle("", dest1[1], dest3[1]);

		SpherePanoTransformer3.rotateForeward(dest1[0], dest1[1], 0, 0 * MathUtil.deg2rad, 0 * MathUtil.deg2rad, dest2);
		TestUtils.assertEqualAngle("", dest1[0], dest2[0]);
		TestUtils.assertEqualAngle("", dest1[1], dest2[1]);

		SpherePanoTransformer3.rotateBackward(dest1[0], dest1[1], 0, 0 * MathUtil.deg2rad, 0 * MathUtil.deg2rad, dest2);
		TestUtils.assertEqualAngle("", dest1[0], dest2[0]);
		TestUtils.assertEqualAngle("", dest1[1], dest2[1]);

		SpherePanoTransformer3.rotateForeward(dest1[0], dest1[1], 
				45 * MathUtil.deg2rad, 
				0 * MathUtil.deg2rad, 
				-45 * MathUtil.deg2rad, 
				dest2);
		TestUtils.assertEqualAngle("", dest1[0], dest2[0]);
		TestUtils.assertEqualAngle("", dest1[1], dest2[1]);
	}
	
	void testSpherePanoTransformer() {
		KeyPointList kpl1 = new KeyPointList();
		kpl1.cameraOriginX = 1001;
		kpl1.cameraOriginY = 2002;
		kpl1.cameraScale = 1.0 / (2.0 * Math.max(kpl1.cameraOriginX, kpl1.cameraOriginY));
		kpl1.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;
		kpl1.sphereRZ1 = 10 * MathUtil.deg2rad;
		kpl1.sphereRY = 20 * MathUtil.deg2rad;
		kpl1.sphereRZ2 = 30 * MathUtil.deg2rad;

		KeyPoint p1 = new KeyPoint();
		p1.keyPointList = kpl1;
		p1.doubleX = p1.keyPointList.cameraOriginX;
		p1.doubleY = p1.keyPointList.cameraOriginY;

		double dest[] = new double[2];
		double dest2[] = new double[2];
		SpherePanoTransformer3.transformForeward(p1.doubleX, p1.doubleY, kpl1, dest);
		SpherePanoTransformer3.transformBackward(dest[0], dest[1], kpl1, dest2);
		TestUtils.dumpAngles("dest", dest);
		TestUtils.assertEqual("", dest2[0], p1.doubleX);
		TestUtils.assertEqual("", dest2[1], p1.doubleY);
		
		Matrix m = RotationZXZ.instance.makeAngles(kpl1.sphereRZ1, kpl1.sphereRY, kpl1.sphereRZ2);
		double d1[] = new double[3];
		double d2[] = new double[3];

		d1[0] = (p1.doubleX - p1.keyPointList.cameraOriginX) * p1.keyPointList.cameraScale;
		d1[1] = (p1.doubleY - p1.keyPointList.cameraOriginY) * p1.keyPointList.cameraScale;
		d1[2] = p1.keyPointList.scaleZ;
		RotationZXZ.instance.transformForward(m, d1, d2);
		SphericalCoordsLongZen.cartesianToPolar(d2[0], d2[1], d2[2], d2);
		TestUtils.assertEqualAngle("", dest[0], d2[0]);
		TestUtils.assertEqualAngle("", dest[1], d2[1]);

		SpherePanoTransformer3.transformForeward(dest2[0], dest2[1], kpl1, dest2);
		TestUtils.assertEqualAngle("", dest2[0], dest[0]);
		TestUtils.assertEqualAngle("", dest2[1], dest[1]);
	}

	void testSphericalDistance() {
		double delta = 2 * MathUtil.deg2rad;
		double rx1 = 10 * MathUtil.deg2rad;
		double ry1 = 89 * MathUtil.deg2rad;
		double d = SphericalCoordsLongZen.getSphericalDistance(rx1, ry1, rx1, ry1 + delta);
		TestUtils.assertEqualAngle("", d, delta);
		
		rx1 = 0 * MathUtil.deg2rad;
		ry1 = 90 * MathUtil.deg2rad;
		d = SphericalCoordsLongZen.getSphericalDistance(rx1, ry1, rx1, -ry1);
		TestUtils.assertEqualAngle("", d, 180 * MathUtil.deg2rad);
		
		rx1 = 179 * MathUtil.deg2rad;
		ry1 = 90 * MathUtil.deg2rad;
		d = SphericalCoordsLongZen.getSphericalDistance(rx1, ry1, rx1 + delta, ry1);
		TestUtils.assertEqualAngle("", d, delta);
	}
	
	private static void checkNorm(KeyPointPair kpp, 
			double dX1, double dY1, double dZ1, double dF1, 
			double dX2, double dY2, double dZ2, double dF2) {
		double dest1[] = new double[2];
		double dest2[] = new double[2];
		SphereNorm3 sn = new SphereNorm3();
		sn.setKeyPointPair(kpp);
		
		SpherePanoTransformer3.transformForeward(kpp.sourceSP.doubleX, kpp.sourceSP.doubleY, kpp.sourceSP.keyPointList, dest1);
		SpherePanoTransformer3.transformForeward(kpp.targetSP.doubleX, kpp.targetSP.doubleY, kpp.targetSP.keyPointList, dest2);
		double dist0 = SphericalCoordsLongZen.getSphericalDistance(dest1[0], dest1[1], dest2[0], dest2[1]);
		double dist2 = dist0;
		dist2 += sn.dDist_dSR1 * dX1;
		dist2 += sn.dDist_dSR2 * dY1;
		dist2 += sn.dDist_dSR3 * dZ1;
		dist2 += sn.dDist_dSF * dF1;
		
		dist2 += sn.dDist_dTR1 * dX2;
		dist2 += sn.dDist_dTR2 * dY2;
		dist2 += sn.dDist_dTR3 * dZ2;
		dist2 += sn.dDist_dTF * dF2;

		kpp.sourceSP.keyPointList.sphereRZ1 += dX1;
		kpp.sourceSP.keyPointList.sphereRY += dY1;
		kpp.sourceSP.keyPointList.sphereRZ2 += dZ1;
		kpp.sourceSP.keyPointList.scaleZ += dF1;

		kpp.targetSP.keyPointList.sphereRZ1 += dX2;
		kpp.targetSP.keyPointList.sphereRY += dY2;
		kpp.targetSP.keyPointList.sphereRZ2 += dZ2;
		kpp.targetSP.keyPointList.scaleZ += dF2;
		
		SpherePanoTransformer3.transformForeward(kpp.sourceSP.doubleX, kpp.sourceSP.doubleY, kpp.sourceSP.keyPointList, dest1);
		SpherePanoTransformer3.transformForeward(kpp.targetSP.doubleX, kpp.targetSP.doubleY, kpp.targetSP.keyPointList, dest2);
		double dist1 = SphericalCoordsLongZen.getSphericalDistance(dest1[0], dest1[1], dest2[0], dest2[1]);

		kpp.sourceSP.keyPointList.sphereRZ1 -= dX1;
		kpp.sourceSP.keyPointList.sphereRY -= dY1;
		kpp.sourceSP.keyPointList.sphereRZ2 -= dZ1;
		kpp.sourceSP.keyPointList.scaleZ -= dF1;

		kpp.targetSP.keyPointList.sphereRZ1 -= dX2;
		kpp.targetSP.keyPointList.sphereRY -= dY2;
		kpp.targetSP.keyPointList.sphereRZ2 -= dZ2;
		kpp.targetSP.keyPointList.scaleZ -= dF2;
		
		TestUtils.assertEqualAngle("", dist2, dist1);
	}
	
	void testSphereNorm() {
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
		
		double dest1[] = new double[2];
		double dest2[] = new double[2];
		KeyPoint p2 = new KeyPoint();
		p2.keyPointList = kpl2;

		KeyPointPair kpp = new KeyPointPair();
		kpp.sourceSP = p1;
		kpp.targetSP = p2;
		double delta = 0.01 * MathUtil.deg2rad;

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
								
								SpherePanoTransformer3.transformForeward(p1.doubleX, p1.doubleY, kpl1, dest1);
								SpherePanoTransformer3.transformBackward(dest1[0] + 1 * MathUtil.deg2rad, dest1[1], kpl2, dest2);
								
								p2.doubleX = dest2[0];
								p2.doubleY = dest2[1];
								try {
									checkNorm(kpp, delta, delta, delta, delta, delta, delta, delta, delta);
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

	void testCalcPrims() {
		KeyPointList origin = new KeyPointList();
		origin.cameraOriginX = 1100;
		origin.cameraOriginY = 2200;
		origin.imageSizeX = (int) (origin.cameraOriginX * 2);
		origin.imageSizeY = (int) (origin.cameraOriginY * 2);
		origin.cameraScale = 1.0 / (2.0 * Math.max(origin.cameraOriginX, origin.cameraOriginY));
		origin.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;
		origin.sphereRZ1 = 0 * MathUtil.deg2rad;
		origin.sphereRY = 0 * MathUtil.deg2rad;
		origin.sphereRZ2 = 0 * MathUtil.deg2rad;
		
		KeyPointList kpl = new KeyPointList();
		kpl.cameraOriginX = origin.cameraOriginX;
		kpl.cameraOriginY = origin.cameraOriginY;
		kpl.cameraScale = 1.0 / (2.0 * Math.max(kpl.cameraOriginX, kpl.cameraOriginY));
		kpl.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;
		kpl.imageSizeX = (int) (kpl.cameraOriginX * 2);
		kpl.imageSizeY = (int) (kpl.cameraOriginY * 2);
		kpl.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;

		KeyPointPairList kppl = new KeyPointPairList();
		boolean sourceOrigin = true;
		if (sourceOrigin) {
			kppl.source = origin;
			kppl.target = kpl;
		} else {
			kppl.target = origin;
			kppl.source = kpl;
		}
		kppl.sphereRZ1 = 40 * MathUtil.deg2rad;
		kppl.sphereRY = 50 * MathUtil.deg2rad;
		kppl.sphereRZ2 = 60 * MathUtil.deg2rad;
		kppl.scale = kpl.scaleZ;
		
		ArrayList<KeyPointPairList> chain = new ArrayList<KeyPointPairList>();
		chain.add(kppl);		
		ArrayList<KeyPointList> images = new ArrayList<KeyPointList>();
		images.add(kpl);
		
		SpherePanoTransformLearner3.calculatePrims(origin, images, chain);

//		System.out.println(MathUtil.rad2degStr(kpl.sphereRZ1));
//		System.out.println(MathUtil.rad2degStr(kpl.sphereRY));
//		System.out.println(MathUtil.rad2degStr(kpl.sphereRZ2));
//		System.out.println(kpl.scaleZ);

		double pOrigin[] = new double[2];
		double pWorld1[] = new double[2];
		double pWorld2[] = new double[2];
		double pKPL[] = new double[2];
		
		pOrigin[0] = 20 * MathUtil.deg2rad;
		pOrigin[1] = 60 * MathUtil.deg2rad;
		
		double angles[] = new double[3];
		if (sourceOrigin) {
			angles[0] = kppl.sphereRZ1;
			angles[1] = kppl.sphereRY;
			angles[2] = kppl.sphereRZ2;
		} else {
			RotationZXZ.instance.getRotationAnglesBackword(kppl.sphereRZ1, kppl.sphereRY, kppl.sphereRZ2, angles);
		}
		SpherePanoTransformer3.rotateForeward(pOrigin[0], pOrigin[1], origin.sphereRZ1, origin.sphereRY, origin.sphereRZ2, pWorld1);
		SpherePanoTransformer3.rotateForeward(pOrigin[0], pOrigin[1], angles[0], angles[1], angles[2], pKPL);
		SpherePanoTransformer3.rotateForeward(pKPL[0], pKPL[1], kpl.sphereRZ1, kpl.sphereRY, kpl.sphereRZ2, pWorld2);
/*		System.out.println("-------");
		dump("origin", pOrigin);
		dump("world1", pWorld1);
		dump("pKPL", pKPL);
		dump("world2", pWorld2);
		pKPL[0] = pWorld2[0] - pWorld1[0];
		pKPL[1] = pWorld2[1] - pWorld1[1];
		dump("delta", pKPL);*/
		TestUtils.assertEqualAngle("", pWorld1[0], pWorld2[0]);
		TestUtils.assertEqualAngle("", pWorld1[1], pWorld2[1]);
	}
	
	private static void checkNorm0(KeyPoint kp, double dR1, double dR2, double dR3, double dF) {
		double dest0[] = new double[2];
		SpherePanoTransformer3.transformForeward(kp.doubleX, kp.doubleY, kp.keyPointList, dest0);
		SphereNorm3.PointDerivatives pd = new SphereNorm3.PointDerivatives();
		pd.setKeyPoint(kp);
		
		TestUtils.assertEqualAngle("", dest0[0], pd.tx);
		TestUtils.assertEqualAngle("", dest0[1], pd.ty);
		
		double dest2[] = new double[2];
		dest2[0] = dest0[0] + pd.dTX_dR1 * dR1 + pd.dTX_dR2 * dR2 + pd.dTX_dR3 * dR3 + pd.dTX_dF * dF;
		dest2[1] = dest0[1] + pd.dTY_dR1 * dR1 + pd.dTY_dR2 * dR2 + pd.dTY_dR3 * dR3 + pd.dTY_dF * dF;
		
		kp.keyPointList.sphereRZ1 += dR1;
		kp.keyPointList.sphereRY += dR2;
		kp.keyPointList.sphereRZ2 += dR3;
		kp.keyPointList.scaleZ += dF;

		double dest1[] = new double[2];
		SpherePanoTransformer3.transformForeward(kp.doubleX, kp.doubleY, kp.keyPointList, dest1);
		
		kp.keyPointList.sphereRZ1 -= dR1;
		kp.keyPointList.sphereRY -= dR2;
		kp.keyPointList.sphereRZ2 -= dR3;
		kp.keyPointList.scaleZ -= dF;
		
		TestUtils.assertEqualAngle("", dest1[0], dest2[0]);
		TestUtils.assertEqualAngle("", dest1[1], dest2[1]);
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

		double delta = 10 * MathUtil.deg2rad;
		int parts = 16;
		for (int i1 = 0; i1 < parts; i1++) {
			double R1 = i1 * MathUtil.C2PI / parts;
			for (int i2 = 0; i2 < parts; i2++) {
				double R2 = i2 * MathUtil.C2PI / parts;
				for (int i3 = 0; i3 < parts; i3++) {
					double R3 = i3 * MathUtil.C2PI / parts;
					p1.keyPointList.sphereRZ1 = R1;
					p1.keyPointList.sphereRY = R2;
					p1.keyPointList.sphereRZ2 = R3;
					try {
						checkNorm0(p1, delta, 0, 0, 0);
//						checkNorm0(p1, delta, delta, delta, delta);
					} catch (RuntimeException e) {
						System.out.println("R1=" + MathUtil.rad2degStr(R1));
						System.out.println("R2=" + MathUtil.rad2degStr(R2));
						System.out.println("R3=" + MathUtil.rad2degStr(R3));
						throw e;
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		UT_SpherePanoTransformer3 test = new UT_SpherePanoTransformer3();
		test.testSpherePanoTransformerRotate();
		test.testSpherePanoTransformer();
		test.testSphericalDistance();
		test.testNorm0();
//		test.testSphereNorm();
//		test.testCalcPrims();
		System.out.println("Done");
	}
}
