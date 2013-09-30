package com.slavi.improc.myadjust;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.myadjust.sphere2.SphereNorm2;
import com.slavi.improc.myadjust.sphere2.SpherePanoTransformLearner2;
import com.slavi.math.MathUtil;
import com.slavi.math.RotationZYZ;
import com.slavi.math.SphericalCoordsLongZen;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.testUtil.TestUtil;

public class SpherePanoTransformer2Test {

	public static double precision = 1.0 / 10000.0;

	@Test
	public void testSpherePanoTransformer() {
		KeyPointList kpl1 = new KeyPointList();
		kpl1.cameraOriginX = 1001;
		kpl1.cameraOriginY = 2002;
		kpl1.cameraScale = 1.0 / (2.0 * Math.max(kpl1.cameraOriginX, kpl1.cameraOriginY));
		kpl1.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;
		kpl1.sphereRZ1 = 10 * MathUtil.deg2rad;
		kpl1.sphereRY = 20 * MathUtil.deg2rad;
		kpl1.sphereRZ2 = 30 * MathUtil.deg2rad;

		KeyPoint p1 = new KeyPoint(kpl1, kpl1.cameraOriginX, kpl1.cameraOriginY + 1000);

		double dest[] = new double[3];
		double dest2[] = new double[3];
		SphereNorm2.transformForeward(p1.getDoubleX(), p1.getDoubleY(), kpl1, dest);
		SphereNorm2.transformBackward(dest[0], dest[1], kpl1, dest2);
		Assert.assertEquals(dest2[0], p1.getDoubleX(), precision);
		Assert.assertEquals(dest2[1], p1.getDoubleY(), precision);
		
		Matrix m = RotationZYZ.instance.makeAngles(kpl1.sphereRZ1, kpl1.sphereRY, kpl1.sphereRZ2);
		double d1[] = new double[3];
		double d2[] = new double[3];

		d1[0] = (p1.getDoubleX() - p1.getKeyPointList().cameraOriginX) * p1.getKeyPointList().cameraScale;
		d1[1] = (p1.getDoubleY() - p1.getKeyPointList().cameraOriginY) * p1.getKeyPointList().cameraScale;
		d1[2] = p1.getKeyPointList().scaleZ;
		RotationZYZ.instance.transformForward(m, d1, d2);
		SphericalCoordsLongZen.cartesianToPolar(d2[0], d2[1], d2[2], d2);
		TestUtil.assertEqualAngle("", dest[0], d2[0]);
		TestUtil.assertEqualAngle("", dest[1], d2[1]);

		SphereNorm2.transformForeward(dest2[0], dest2[1], kpl1, dest2);
		TestUtil.assertEqualAngle("", dest2[0], dest[0]);
		TestUtil.assertEqualAngle("", dest2[1], dest[1]);
	}

	@Test
	public void testSphericalDistance() {
		double delta = 2 * MathUtil.deg2rad;
		double rx1 = 10 * MathUtil.deg2rad;
		double ry1 = 89 * MathUtil.deg2rad;
		double d = SphericalCoordsLongZen.getSphericalDistance(rx1, ry1, rx1, ry1 + delta);
		TestUtil.assertEqualAngle("", d, delta);
		
		rx1 = 0 * MathUtil.deg2rad;
		ry1 = 90 * MathUtil.deg2rad;
		d = SphericalCoordsLongZen.getSphericalDistance(rx1, ry1, rx1, -ry1);
		TestUtil.assertEqualAngle("", d, 180 * MathUtil.deg2rad);
		
		rx1 = 179 * MathUtil.deg2rad;
		ry1 = 90 * MathUtil.deg2rad;
		d = SphericalCoordsLongZen.getSphericalDistance(rx1, ry1, rx1 + delta, ry1);
		TestUtil.assertEqualAngle("", d, delta);
	}

	private static void checkNorm0(KeyPoint kp, double dX, double dY, double dZ, double dF) {
		double dest0[] = new double[3];
		SphereNorm2.transformForeward(kp.getDoubleX(), kp.getDoubleY(), kp.getKeyPointList(), dest0);
		SphereNorm2.PointDerivatives pd = new SphereNorm2.PointDerivatives();
		pd.setKeyPoint(kp);
		
		TestUtil.assertEqualAngle("", dest0[0], pd.tx);
		TestUtil.assertEqualAngle("", dest0[1], pd.ty);
		
		double dest2[] = new double[2];
		dest2[0] = dest0[0] + pd.dTX_dR1 * dX + pd.dTX_dR2 * dY + pd.dTX_dR3 * dZ + pd.dTX_dF * dF;
		dest2[1] = dest0[1] + pd.dTY_dR1 * dX + pd.dTY_dR2 * dY + pd.dTY_dR3 * dZ + pd.dTY_dF * dF;
		
		kp.getKeyPointList().sphereRZ1 += dX;
		kp.getKeyPointList().sphereRY += dY;
		kp.getKeyPointList().sphereRZ2 += dZ;
		kp.getKeyPointList().scaleZ += dF;

		double dest1[] = new double[3];
		SphereNorm2.transformForeward(kp.getDoubleX(), kp.getDoubleY(), kp.getKeyPointList(), dest1);
		
		kp.getKeyPointList().sphereRZ1 -= dX;
		kp.getKeyPointList().sphereRY -= dY;
		kp.getKeyPointList().sphereRZ2 -= dZ;
		kp.getKeyPointList().scaleZ -= dF;
		
		TestUtil.assertEqualAngle("", dest1[0], dest2[0]);
		TestUtil.assertEqualAngle("", dest1[1], dest2[1]);
	}

	@Test
	public void testNorm0() {
		KeyPointList kpl1 = new KeyPointList();
		kpl1.cameraOriginX = 1136;
		kpl1.cameraOriginY = 856;
		kpl1.cameraScale = 1.0 / (2.0 * Math.max(kpl1.cameraOriginX, kpl1.cameraOriginY));
		kpl1.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;
		kpl1.sphereRZ1 = -178 * MathUtil.deg2rad;
		kpl1.sphereRY = 30 * MathUtil.deg2rad;
		kpl1.sphereRZ2 = 178 * MathUtil.deg2rad;
		
		KeyPoint p1 = new KeyPoint(kpl1, 1881, 897);

		double delta = 0.1 * MathUtil.deg2rad;
		int parts = 16;
		for (int x = 0; x < parts; x++) {
			double rx = x * MathUtil.C2PI / parts;
			for (int y = 0; y < parts; y++) {
				double ry = y * MathUtil.C2PI / parts;
				for (int z = 0; z < parts; z++) {
					double rz = z * MathUtil.C2PI / parts;
					p1.getKeyPointList().sphereRZ1 = rx;
					p1.getKeyPointList().sphereRY = ry;
					p1.getKeyPointList().sphereRZ2 = rz;
					try {
						checkNorm0(p1, delta, delta, delta, delta);
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
/*
	private static void checkNorm(KeyPointPair kpp, 
			double dX1, double dY1, double dZ1, double dF1, 
			double dX2, double dY2, double dZ2, double dF2) {
		double dest1[] = new double[3];
		double dest2[] = new double[3];
		SphereNorm2 sn = new SphereNorm2();
		sn.setKeyPointPair(kpp);
		
		SphereNorm2.transformForeward(kpp.sourceSP.doubleX, kpp.sourceSP.doubleY, kpp.sourceSP.keyPointList, dest1);
		SphereNorm2.transformForeward(kpp.targetSP.doubleX, kpp.targetSP.doubleY, kpp.targetSP.keyPointList, dest2);
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
		
		SphereNorm2.transformForeward(kpp.sourceSP.doubleX, kpp.sourceSP.doubleY, kpp.sourceSP.keyPointList, dest1);
		SphereNorm2.transformForeward(kpp.targetSP.doubleX, kpp.targetSP.doubleY, kpp.targetSP.keyPointList, dest2);
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

	@Test
	public void testSphereNorm() {
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
								
								SphereNorm2.transformForeward(p1.doubleX, p1.doubleY, kpl1, dest1);
								SphereNorm2.transformBackward(dest1[0] + 1 * MathUtil.deg2rad, dest1[1], kpl2, dest2);
								
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
*/
	@Test
	public void testCalcPrims() {
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
		
		SpherePanoTransformLearner2.calculatePrims(origin, images, chain);

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
			RotationZYZ.instance.getRotationAnglesBackword(kppl.sphereRZ1, kppl.sphereRY, kppl.sphereRZ2, angles);
		}
		SphericalCoordsLongZen.rotateForeward(pOrigin[0], pOrigin[1], origin.sphereRZ1, origin.sphereRY, origin.sphereRZ2, pWorld1);
		SphericalCoordsLongZen.rotateForeward(pOrigin[0], pOrigin[1], angles[0], angles[1], angles[2], pKPL);
		SphericalCoordsLongZen.rotateForeward(pKPL[0], pKPL[1], kpl.sphereRZ1, kpl.sphereRY, kpl.sphereRZ2, pWorld2);
/*		System.out.println("-------");
		dump("origin", pOrigin);
		dump("world1", pWorld1);
		dump("pKPL", pKPL);
		dump("world2", pWorld2);
		pKPL[0] = pWorld2[0] - pWorld1[0];
		pKPL[1] = pWorld2[1] - pWorld1[1];
		dump("delta", pKPL);*/
		TestUtil.assertEqualAngle("", pWorld1[0], pWorld2[0]);
		TestUtil.assertEqualAngle("", pWorld1[1], pWorld2[1]);
	}
}
