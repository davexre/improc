package com.unitTest;

import java.util.ArrayList;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.myadjust.sphere2.SphereNorm2;
import com.slavi.improc.myadjust.sphere2.SpherePanoTransformLearner2;
import com.slavi.improc.myadjust.sphere2.SpherePanoTransformer2;
import com.slavi.math.MathUtil;
import com.slavi.math.RotationZYZ;

public class UT_SpherePanoTransformer2 {

	KeyPoint p1;
	KeyPointList kpl1;
	
	public UT_SpherePanoTransformer2() {
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
	
	static final double precision = 10000;
	public static void assertEqualAngle(double a, double b) {
		a = MathUtil.fixAngle2PI(a);
		b = MathUtil.fixAngle2PI(b);
		if ((int)(a * precision) == (int)(b * precision))
			return;
		System.out.println(MathUtil.rad2degStr(a));
		System.out.println(MathUtil.rad2degStr(b));
		System.out.flush();
		throw new RuntimeException("Failed");
	}
	
	public static void assertEqual(double a, double b) {
		if ((int)(a * precision) == (int)(b * precision))
			return;
		System.out.println(a);
		System.out.println(b);
		System.out.flush();
		throw new RuntimeException("Failed");
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
		SpherePanoTransformer2.rotateForeward(dest1[0], dest1[1], rot[0], rot[1], rot[2], dest2);
		SpherePanoTransformer2.rotateBackward(dest2[0], dest2[1], rot[0], rot[1], rot[2], dest3);
		assertEqualAngle(dest1[0], dest3[0]);
		assertEqualAngle(dest1[1], dest3[1]);

		SpherePanoTransformer2.rotateForeward(dest1[0], dest1[1], 0, 0 * MathUtil.deg2rad, 0 * MathUtil.deg2rad, dest2);
		assertEqualAngle(dest1[0], dest2[0]);
		assertEqualAngle(dest1[1], dest2[1]);

		SpherePanoTransformer2.rotateBackward(dest1[0], dest1[1], 0, 0 * MathUtil.deg2rad, 0 * MathUtil.deg2rad, dest2);
		assertEqualAngle(dest1[0], dest2[0]);
		assertEqualAngle(dest1[1], dest2[1]);

		SpherePanoTransformer2.rotateForeward(dest1[0], dest1[1], 
				45 * MathUtil.deg2rad, 
				0 * MathUtil.deg2rad, 
				-45 * MathUtil.deg2rad, 
				dest2);
		assertEqualAngle(dest1[0], dest2[0]);
		assertEqualAngle(dest1[1], dest2[1]);
	}
	
	void testSpherePanoTransformer() {
		double dest[] = new double[2];
		double dest2[] = new double[2];
		SpherePanoTransformer2.transformForeward(p1.doubleX, p1.doubleY, kpl1, dest);
		SpherePanoTransformer2.transformBackward(dest[0], dest[1], kpl1, dest2);
		assertEqualAngle(dest2[0], p1.doubleX);
		assertEqualAngle(dest2[1], p1.doubleY);
		SpherePanoTransformer2.transformForeward(dest2[0], dest2[1], kpl1, dest2);
		assertEqualAngle(dest2[0], dest[0]);
		assertEqualAngle(dest2[1], dest[1]);
	}

	void testSphericalDistance() {
		double delta = 2 * MathUtil.deg2rad;
		double rx1 = 10 * MathUtil.deg2rad;
		double ry1 = 89 * MathUtil.deg2rad;
		double d = SpherePanoTransformer2.getSphericalDistance(rx1, ry1, rx1, ry1 + delta);
		assertEqual(d, delta);
		
		rx1 = 0 * MathUtil.deg2rad;
		ry1 = 90 * MathUtil.deg2rad;
		d = SpherePanoTransformer2.getSphericalDistance(rx1, ry1, rx1, -ry1);
		assertEqual(d, 180 * MathUtil.deg2rad);
		
		rx1 = 179 * MathUtil.deg2rad;
		ry1 = 90 * MathUtil.deg2rad;
		d = SpherePanoTransformer2.getSphericalDistance(rx1, ry1, rx1 + delta, ry1);
		assertEqual(d, delta);
	}
	
	private static void checkNorm(KeyPointPair kpp, 
			double dX1, double dY1, double dZ1, double dF1, 
			double dX2, double dY2, double dZ2, double dF2) {
		double dest1[] = new double[2];
		double dest2[] = new double[2];
		SphereNorm2 sn = new SphereNorm2();
		sn.setKeyPointPair(kpp);
		
		SpherePanoTransformer2.transformForeward(kpp.sourceSP.doubleX, kpp.sourceSP.doubleY, kpp.sourceSP.keyPointList, dest1);
		SpherePanoTransformer2.transformForeward(kpp.targetSP.doubleX, kpp.targetSP.doubleY, kpp.targetSP.keyPointList, dest2);
		double dist0 = SpherePanoTransformer2.getSphericalDistance(dest1[0], dest1[1], dest2[0], dest2[1]);
		dist0 += sn.dDist_dIX1 * dX1;
		dist0 += sn.dDist_dIY1 * dY1;
		dist0 += sn.dDist_dIZ1 * dZ1;
		dist0 += sn.dDist_dIF1 * dF1;
		
		dist0 += sn.dDist_dIX2 * dX2;
		dist0 += sn.dDist_dIY2 * dY2;
		dist0 += sn.dDist_dIZ2 * dZ2;
		dist0 += sn.dDist_dIF2 * dF2;

		kpp.sourceSP.keyPointList.sphereRZ1 += dX1;
		kpp.sourceSP.keyPointList.sphereRY += dY1;
		kpp.sourceSP.keyPointList.sphereRZ2 += dZ1;
		kpp.sourceSP.keyPointList.scaleZ += dF1;

		kpp.targetSP.keyPointList.sphereRZ1 += dX2;
		kpp.targetSP.keyPointList.sphereRY += dY2;
		kpp.targetSP.keyPointList.sphereRZ2 += dZ2;
		kpp.targetSP.keyPointList.scaleZ += dF2;
		
		SpherePanoTransformer2.transformForeward(kpp.sourceSP.doubleX, kpp.sourceSP.doubleY, kpp.sourceSP.keyPointList, dest1);
		SpherePanoTransformer2.transformForeward(kpp.targetSP.doubleX, kpp.targetSP.doubleY, kpp.targetSP.keyPointList, dest2);
		double dist1 = SpherePanoTransformer2.getSphericalDistance(dest1[0], dest1[1], dest2[0], dest2[1]);

		kpp.sourceSP.keyPointList.sphereRZ1 -= dX1;
		kpp.sourceSP.keyPointList.sphereRY -= dY1;
		kpp.sourceSP.keyPointList.sphereRZ2 -= dZ1;
		kpp.sourceSP.keyPointList.scaleZ -= dF1;

		kpp.targetSP.keyPointList.sphereRZ1 -= dX2;
		kpp.targetSP.keyPointList.sphereRY -= dY2;
		kpp.targetSP.keyPointList.sphereRZ2 -= dZ2;
		kpp.targetSP.keyPointList.scaleZ -= dF2;
		
		assertEqualAngle(dist0, dist1);
		System.out.println(MathUtil.rad2degStr(dist0));
		System.out.println(MathUtil.rad2degStr(dist1));
		System.out.println(MathUtil.d20(dist0));
		System.out.println(MathUtil.d20(dist1));
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
		SpherePanoTransformer2.transformForeward(p1.doubleX, p1.doubleY, kpl1, dest1);
		SpherePanoTransformer2.transformBackward(dest1[0] + 18 * MathUtil.deg2rad, dest1[1], kpl2, dest2);

		KeyPoint p2 = new KeyPoint();
		p2.keyPointList = kpl2;
		p2.doubleX = dest2[0];
		p2.doubleY = dest2[1];

		KeyPointPair kpp = new KeyPointPair();
		kpp.sourceSP = p1;
		kpp.targetSP = p2;
		
		double delta = 0.01 * MathUtil.deg2rad;
//		checkNorm(kpp, 0, 0, 0, delta, 0, 0, 0, 0);
		checkNorm(kpp, delta, delta, delta, delta, delta, delta, delta, delta);
		
/*		
		SpherePanoTransformer2.transformForeward(p2.doubleX, p2.doubleY, kpl2, dest2);
		double dist0 = SpherePanoTransformer2.getSphericalDistance(dest1[0], dest1[1], dest2[0], dest2[1]);
		kpl1.sphereRZ1 += delta;
		kpl1.sphereRY += delta;
		kpl1.sphereRZ2 += delta;
		kpl1.scaleZ += delta;

		kpl2.sphereRZ1 += delta;
		kpl2.sphereRY += delta;
		kpl2.sphereRZ2 += delta;
		kpl2.scaleZ += delta;

		SphereNorm2 sn = new SphereNorm2();
		sn.setKeyPointPair(kpp);
		
		SpherePanoTransformer2.transformForeward(p1.doubleX, p1.doubleY, kpl1, dest1);
		SpherePanoTransformer2.transformForeward(p2.doubleX, p2.doubleY, kpl2, dest2);
		double dist1 = SpherePanoTransformer2.getSphericalDistance(dest1[0], dest1[1], dest2[0], dest2[1]);
		dist1 += sn.dDist_dIX1 * delta;
		dist1 += sn.dDist_dIY1 * delta;
		dist1 += sn.dDist_dIZ1 * delta;
		dist1 += sn.dDist_dIF1 * delta;
		
		dist1 += sn.dDist_dIX2 * delta;
		dist1 += sn.dDist_dIY2 * delta;
		dist1 += sn.dDist_dIZ2 * delta;
		dist1 += sn.dDist_dIF2 * delta;
		
		System.out.println(MathUtil.rad2degStr(dist0));
		System.out.println(MathUtil.rad2degStr(dist1));
		System.out.println("-------");
		System.out.println(MathUtil.d20(dist0));
		System.out.println(MathUtil.d20(dist1));
		assertEqualAngle(dist0, dist1);*/
	}

	public static void dump(String str, double x, double y) {
		x = MathUtil.fixAngle2PI(x);
		y = MathUtil.fixAngle2PI(y);
		System.out.println(str + "\t" + MathUtil.rad2degStr(x) + "\t" + MathUtil.rad2degStr(y));
	}
	
	public static void dump(String str, double dest[]) {
		dump(str, dest[0], dest[1]);
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
		boolean sourceOrigin = false;
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
		SpherePanoTransformer2.rotateForeward(pOrigin[0], pOrigin[1], origin.sphereRZ1, origin.sphereRY, origin.sphereRZ2, pWorld1);
		SpherePanoTransformer2.rotateForeward(pOrigin[0], pOrigin[1], angles[0], angles[1], angles[2], pKPL);
		SpherePanoTransformer2.rotateForeward(pKPL[0], pKPL[1], kpl.sphereRZ1, kpl.sphereRY, kpl.sphereRZ2, pWorld2);
/*		System.out.println("-------");
		dump("origin", pOrigin);
		dump("world1", pWorld1);
		dump("pKPL", pKPL);
		dump("world2", pWorld2);
		pKPL[0] = pWorld2[0] - pWorld1[0];
		pKPL[1] = pWorld2[1] - pWorld1[1];
		dump("delta", pKPL);*/
		assertEqualAngle(pWorld1[0], pWorld2[0]);
		assertEqualAngle(pWorld1[1], pWorld2[1]);
	}
	
	public static void main(String[] args) {
		UT_SpherePanoTransformer2 test = new UT_SpherePanoTransformer2();
		test.testSpherePanoTransformerRotate();
		test.testSpherePanoTransformer();
		test.testSphericalDistance();
		test.testSphereNorm();
		test.testCalcPrims();
		System.out.println("Done");
	}
}
