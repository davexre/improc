package com.unitTest;

import java.util.ArrayList;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.myadjust.sphere.SphereNorm;
import com.slavi.improc.myadjust.sphere.SpherePanoTransformLearner;
import com.slavi.math.MathUtil;
import com.slavi.math.SphericalCoordsLongLat;
import com.slavi.math.SphericalCoordsLongZen;

public class UT_SpherePanoTransformer {

	KeyPoint p1;
	KeyPointList kpl1;
	static SpherePanoTransformLearner panoTransform = new SpherePanoTransformLearner();
	
	public UT_SpherePanoTransformer() {
		kpl1 = new KeyPointList();
		kpl1.cameraOriginX = 1001;
		kpl1.cameraOriginY = 2002;
		kpl1.scaleZ = 3003;
		kpl1.rx = 10 * MathUtil.deg2rad;
		kpl1.ry = 20 * MathUtil.deg2rad;
		kpl1.rz = 30 * MathUtil.deg2rad;
		
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
		SphericalCoordsLongZen.rotateForeward(dest1[0], dest1[1], rot[0], rot[1], rot[2], dest2);
		SphericalCoordsLongZen.rotateBackward(dest2[0], dest2[1], rot[0], rot[1], rot[2], dest3);
		TestUtils.assertEqualAngle("", dest1[0], dest3[0]);
		TestUtils.assertEqualAngle("", dest1[1], dest3[1]);

		SphericalCoordsLongZen.rotateForeward(dest1[0], dest1[1], 0, 90 * MathUtil.deg2rad, 180 * MathUtil.deg2rad, dest2);
		TestUtils.assertEqualAngle("", dest1[0], dest2[0]);
		TestUtils.assertEqualAngle("", dest1[1], dest2[1]);

		SphericalCoordsLongZen.rotateBackward(dest1[0], dest1[1], 0, 90 * MathUtil.deg2rad, 180 * MathUtil.deg2rad, dest2);
		TestUtils.assertEqualAngle("", dest1[0], dest2[0]);
		TestUtils.assertEqualAngle("", dest1[1], dest2[1]);

		SphericalCoordsLongZen.rotateForeward(dest1[0], dest1[1], 
				45 * MathUtil.deg2rad, 
				90 * MathUtil.deg2rad, 
				(180 + 45) * MathUtil.deg2rad, 
				dest2);
		TestUtils.assertEqualAngle("", dest1[0], dest2[0]);
		TestUtils.assertEqualAngle("", dest1[1], dest2[1]);
	}
	
	void testSpherePanoTransformer() {
		double dest[] = new double[2];
		double dest2[] = new double[2];
		panoTransform.transformForeward(p1.doubleX, p1.doubleY, kpl1, dest);
		panoTransform.transformBackward(dest[0], dest[1], kpl1, dest2);
		TestUtils.assertEqualAngle("", dest2[0], p1.doubleX);
		TestUtils.assertEqualAngle("", dest2[1], p1.doubleY);
		panoTransform.transformForeward(dest2[0], dest2[1], kpl1, dest2);
		TestUtils.assertEqualAngle("", dest2[0], dest[0]);
		TestUtils.assertEqualAngle("", dest2[1], dest[1]);
	}

	void testSphericalDistance() {
		double delta = 2 * MathUtil.deg2rad;
		double rx1 = 180 * MathUtil.deg2rad;
		double ry1 = 89 * MathUtil.deg2rad;
		double d = SphericalCoordsLongLat.getSphericalDistance(rx1, ry1, rx1, ry1 + delta);
		TestUtils.assertEqual("", d, delta);
		
		rx1 = 0 * MathUtil.deg2rad;
		ry1 = 90 * MathUtil.deg2rad;
		d = SphericalCoordsLongLat.getSphericalDistance(rx1, ry1, rx1, -ry1);
		TestUtils.assertEqual("", d, 180 * MathUtil.deg2rad);
		
		rx1 = 179 * MathUtil.deg2rad;
		ry1 = 0 * MathUtil.deg2rad;
		d = SphericalCoordsLongLat.getSphericalDistance(rx1, ry1, rx1 + delta, ry1);
		TestUtils.assertEqual("", d, delta);
	}
	
	private static void checkNorm(KeyPointPair kpp, 
			double dX1, double dY1, double dZ1, double dF1, 
			double dX2, double dY2, double dZ2, double dF2) {
		double dest1[] = new double[2];
		double dest2[] = new double[2];
		SphereNorm sn = new SphereNorm();
		sn.setKeyPointPair(kpp);
		
		panoTransform.transformForeward(kpp.sourceSP.doubleX, kpp.sourceSP.doubleY, kpp.sourceSP.keyPointList, dest1);
		panoTransform.transformForeward(kpp.targetSP.doubleX, kpp.targetSP.doubleY, kpp.targetSP.keyPointList, dest2);
		double dist0 = SphericalCoordsLongLat.getSphericalDistance(dest1[0], dest1[1], dest2[0], dest2[1]);
		dist0 -= sn.dDist_dSR1 * dX1;
		dist0 -= sn.dDist_dSR2 * dY1;
		dist0 -= sn.dDist_dSR3 * dZ1;
		dist0 -= sn.dDist_dSF * dF1;
		
		dist0 -= sn.dDist_dTR1 * dX2;
		dist0 -= sn.dDist_dTR2 * dY2;
		dist0 -= sn.dDist_dTR3 * dZ2;
		dist0 -= sn.dDist_dTF * dF2;

		kpp.sourceSP.keyPointList.sphereRZ1 += dX1;
		kpp.sourceSP.keyPointList.sphereRY += dY1;
		kpp.sourceSP.keyPointList.sphereRZ2 += dZ1;
		kpp.sourceSP.keyPointList.scaleZ += dF1;

		kpp.targetSP.keyPointList.sphereRZ1 += dX2;
		kpp.targetSP.keyPointList.sphereRY += dY2;
		kpp.targetSP.keyPointList.sphereRZ2 += dZ2;
		kpp.targetSP.keyPointList.scaleZ += dF2;
		
		panoTransform.transformForeward(kpp.sourceSP.doubleX, kpp.sourceSP.doubleY, kpp.sourceSP.keyPointList, dest1);
		panoTransform.transformForeward(kpp.targetSP.doubleX, kpp.targetSP.doubleY, kpp.targetSP.keyPointList, dest2);
		double dist1 = SphericalCoordsLongLat.getSphericalDistance(dest1[0], dest1[1], dest2[0], dest2[1]);

		kpp.sourceSP.keyPointList.sphereRZ1 -= dX1;
		kpp.sourceSP.keyPointList.sphereRY -= dY1;
		kpp.sourceSP.keyPointList.sphereRZ2 -= dZ1;
		kpp.sourceSP.keyPointList.scaleZ -= dF1;

		kpp.targetSP.keyPointList.sphereRZ1 -= dX2;
		kpp.targetSP.keyPointList.sphereRY -= dY2;
		kpp.targetSP.keyPointList.sphereRZ2 -= dZ2;
		kpp.targetSP.keyPointList.scaleZ -= dF2;
		
		TestUtils.assertEqualAngle("", dist0, dist1);
		System.out.println(MathUtil.rad2degStr(dist0));
		System.out.println(MathUtil.rad2degStr(dist1));
		System.out.println(MathUtil.d20(dist0));
		System.out.println(MathUtil.d20(dist1));
	}
	
	void testSphereNorm() {
		KeyPointList kpl2 = new KeyPointList();
		kpl2.cameraOriginX = 1100;
		kpl2.cameraOriginY = 2200;
		kpl2.scaleZ = 3300;
		kpl2.rx = 20 * MathUtil.deg2rad;
		kpl2.ry = 30 * MathUtil.deg2rad;
		kpl2.rz = 40 * MathUtil.deg2rad;

		double dest1[] = new double[2];
		double dest2[] = new double[2];
		panoTransform.transformForeward(p1.doubleX, p1.doubleY, kpl1, dest1);
		panoTransform.transformBackward(dest1[0] + 10 * MathUtil.deg2rad, dest1[1], kpl2, dest2);

		KeyPoint p2 = new KeyPoint();
		p2.keyPointList = kpl2;
		p2.doubleX = dest2[0];
		p2.doubleY = dest2[1];

		KeyPointPair kpp = new KeyPointPair();
		kpp.sourceSP = p1;
		kpp.targetSP = p2;
		
		double delta = 0.1 * MathUtil.deg2rad;
		checkNorm(kpp, delta, delta, delta, delta, delta, delta, delta, delta);
		
		panoTransform.transformForeward(p2.doubleX, p2.doubleY, kpl2, dest2);
		double dist0 = SphericalCoordsLongLat.getSphericalDistance(dest1[0], dest1[1], dest2[0], dest2[1]);
		kpl1.rx += delta;
		kpl1.ry += delta;
		kpl1.rz += delta;

		kpl2.rx += delta;
		kpl2.ry += delta;
		kpl2.rz += delta;

		SphereNorm sn = new SphereNorm();
		sn.setKeyPointPair(kpp);
		
		panoTransform.transformForeward(p1.doubleX, p1.doubleY, kpl1, dest1);
		panoTransform.transformForeward(p2.doubleX, p2.doubleY, kpl2, dest2);
		double dist1 = SphericalCoordsLongLat.getSphericalDistance(dest1[0], dest1[1], dest2[0], dest2[1]);
		dist1 -= sn.dDist_dSR1 * delta;
		dist1 -= sn.dDist_dSR2 * delta;
		dist1 -= sn.dDist_dSR3 * delta;
		dist1 -= sn.dDist_dSF * delta;
		
		dist1 -= sn.dDist_dTR1 * delta;
		dist1 -= sn.dDist_dTR2 * delta;
		dist1 -= sn.dDist_dTR3 * delta;
		dist1 -= sn.dDist_dTF * delta;
		
//		System.out.println(MathUtil.rad2degStr(dist));
//		System.out.println(MathUtil.rad2degStr(dist0));
		TestUtils.assertEqualAngle("", dist0, dist1);
	}

	void dump(String str, double dest[]) {
		double x = dest[0]; 
		double y = dest[1];
		x = MathUtil.fixAngle2PI(x);
		y = MathUtil.fixAngle2PI(y);
		System.out.println(str + "\t" + MathUtil.rad2degStr(x) + "\t" + MathUtil.rad2degStr(y));
	}
	
	void testCalcPrims() {
		KeyPointList origin = new KeyPointList();
		origin.cameraOriginX = 1100;
		origin.cameraOriginY = 2200;
		origin.imageSizeX = (int) (origin.cameraOriginX * 2);
		origin.imageSizeY = (int) (origin.cameraOriginY * 2);
		origin.scaleZ = 0.5 * Math.max(origin.imageSizeX, origin.imageSizeY) * 
			Math.tan(0.5 * KeyPointList.defaultCameraFieldOfView);
//		origin.rx = 0 * MathUtil.deg2rad;
//		origin.ry = 0 * MathUtil.deg2rad;
//		origin.rz = 0 * MathUtil.deg2rad;
		
		KeyPointList kpl = new KeyPointList();
		kpl.cameraOriginX = origin.cameraOriginX;
		kpl.cameraOriginY = origin.cameraOriginY;
		kpl.imageSizeX = (int) (kpl.cameraOriginX * 2);
		kpl.imageSizeY = (int) (kpl.cameraOriginY * 2);
		kpl.scaleZ = 0.5 * Math.max(origin.imageSizeX, origin.imageSizeY) * 
			Math.tan(0.5 * KeyPointList.defaultCameraFieldOfView);

		KeyPointPairList kppl = new KeyPointPairList();
		kppl.source = origin;
		kppl.target = kpl;
		kppl.rx = 80 * MathUtil.deg2rad;
		kppl.ry = 30 * MathUtil.deg2rad;
		kppl.rz = 0 * MathUtil.deg2rad;
		kppl.scale = kpl.scaleZ;
		
		ArrayList<KeyPointPairList> chain = new ArrayList<KeyPointPairList>();
		chain.add(kppl);		
		ArrayList<KeyPointList> images = new ArrayList<KeyPointList>();
		images.add(kpl);
		
//		double dest[] = new double[2];
//		double dest1[] = new double[2];
//		SpherePanoTransformer.rotateForeward(
//				origin.rx, origin.ry, 
//				kppl.rx, kppl.ry, kppl.rz, dest);
//		kpl.rx = MathUtil.fixAngleMPI_PI(dest[0]);
//		kpl.ry = MathUtil.fixAngleMPI_PI(dest[1]);
//		kpl.rz = MathUtil.fixAngleMPI_PI(origin.rz);
//		kpl.scaleZ = kppl.source.scaleZ / kppl.scale; 
		SpherePanoTransformLearner.calculatePrims(origin, images, chain);
//		dump("world2", dest);
//		SpherePanoTransformer.rotateBackward(0 * MathUtil.deg2rad, 90 * MathUtil.deg2rad, kpl.rx, kpl.ry, kpl.rz, dest1);
//		dump(dest1);
//		SpherePanoTransformer.rotateForeward(dest[0], dest[1], kpl.rx, kpl.ry, kpl.rz, dest1);
//		dump(dest1);

		System.out.println("------");
		System.out.println(MathUtil.rad2degStr(kpl.rx));
		System.out.println(MathUtil.rad2degStr(kpl.ry));
		System.out.println(MathUtil.rad2degStr(kpl.rz));
		System.out.println(kpl.scaleZ);

		double pOrigin[] = new double[2];
		double pWorld1[] = new double[2];
		double pWorld2[] = new double[2];
		double pKPL[] = new double[2];
		
		pOrigin[0] = 0 * MathUtil.deg2rad;
		pOrigin[1] = 0 * MathUtil.deg2rad;
		
		SphericalCoordsLongZen.rotateForeward(pOrigin[0], pOrigin[1], origin.rx, origin.ry + 90 * MathUtil.deg2rad, origin.rz + 0 * MathUtil.deg2rad, pWorld1);
		SphericalCoordsLongZen.rotateForeward(pOrigin[0], pOrigin[1], kppl.rx, kppl.ry, kppl.rz + 180 * MathUtil.deg2rad, pKPL);
		SphericalCoordsLongZen.rotateForeward(pKPL[0], pKPL[1], kpl.rx, kpl.ry + 90 * MathUtil.deg2rad, kpl.rz + 0 * MathUtil.deg2rad, pWorld2);
		System.out.println("-------");
//		dump(pOrigin);
		dump("1", pWorld1);
		dump("pKPL", pKPL);
		dump("2", pWorld2);
		pKPL[0] = pWorld2[0] - pWorld1[0];
		pKPL[1] = pWorld2[1] - pWorld1[1];
		dump("delta", pKPL);
	}
	
	public static void main(String[] args) {
		UT_SpherePanoTransformer test = new UT_SpherePanoTransformer();
		test.testSpherePanoTransformerRotate();
		test.testSpherePanoTransformer();
		test.testSphericalDistance();
		test.testSphereNorm();
//		test.testCalcPrims();
		System.out.println("Done");
	}
}
