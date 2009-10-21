package com.unitTest;

import java.util.ArrayList;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.myadjust.SphereNorm;
import com.slavi.improc.myadjust.SpherePanoTransformLearner;
import com.slavi.improc.myadjust.SpherePanoTransformer;
import com.slavi.math.MathUtil;

public class UT_SpherePanoTransformer {

	KeyPoint p1;
	KeyPointList kpl1;
	
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
	
	static final double precision = 10000;
	public static void asserEqualAngle(double a, double b) {
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
		dest1[0] = 200 * MathUtil.deg2rad;
		dest1[1] = 50 * MathUtil.deg2rad;
		SpherePanoTransformer.rotateForeward(dest1[0], dest1[1], rot[0], rot[1], rot[2], dest2);
		SpherePanoTransformer.rotateBackward(dest2[0], dest2[1], rot[0], rot[1], rot[2], dest3);
		asserEqualAngle(dest1[0], dest3[0]);
		asserEqualAngle(dest1[1], dest3[1]);

		SpherePanoTransformer.rotateForeward(dest1[0], dest1[1], 0, 90 * MathUtil.deg2rad, 0, dest2);
		asserEqualAngle(dest1[0], dest2[0]);
		asserEqualAngle(dest1[1], dest2[1]);

		SpherePanoTransformer.rotateBackward(dest1[0], dest1[1], 0, 90 * MathUtil.deg2rad, 0, dest2);
		asserEqualAngle(dest1[0], dest2[0]);
		asserEqualAngle(dest1[1], dest2[1]);

		SpherePanoTransformer.rotateForeward(dest1[0], dest1[1], 
				45 * MathUtil.deg2rad, 
				90 * MathUtil.deg2rad, 
				45 * MathUtil.deg2rad, 
				dest2);
		asserEqualAngle(dest1[0], dest2[0]);
		asserEqualAngle(dest1[1], dest2[1]);
	}
	
	void testSpherePanoTransformer() {
		double dest[] = new double[2];
		double dest2[] = new double[2];
		SpherePanoTransformer.transformForeward(p1.doubleX, p1.doubleY, kpl1, dest);
		SpherePanoTransformer.transformBackward(dest[0], dest[1], kpl1, dest2);
		asserEqualAngle(dest2[0], p1.doubleX);
		asserEqualAngle(dest2[1], p1.doubleY);
		SpherePanoTransformer.transformForeward(dest2[0], dest2[1], kpl1, dest2);
		asserEqualAngle(dest2[0], dest[0]);
		asserEqualAngle(dest2[1], dest[1]);
	}

	void testSphericalDistance() {
		double delta = 2 * MathUtil.deg2rad;
		double rx1 = 180 * MathUtil.deg2rad;
		double ry1 = 89 * MathUtil.deg2rad;
		double d = SpherePanoTransformer.getSphericalDistance(rx1, ry1, rx1, ry1 + delta);
		assertEqual(d, delta);
		
		rx1 = 0 * MathUtil.deg2rad;
		ry1 = 90 * MathUtil.deg2rad;
		d = SpherePanoTransformer.getSphericalDistance(rx1, ry1, rx1, -ry1);
		assertEqual(d, 180 * MathUtil.deg2rad);
		
		rx1 = 179 * MathUtil.deg2rad;
		ry1 = 90 * MathUtil.deg2rad;
		d = SpherePanoTransformer.getSphericalDistance(rx1, ry1, rx1 + delta, ry1);
		assertEqual(d, delta);
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
		SpherePanoTransformer.transformForeward(p1.doubleX, p1.doubleY, kpl1, dest1);
		SpherePanoTransformer.transformBackward(dest1[0], dest1[1], kpl2, dest2);

		KeyPoint p2 = new KeyPoint();
		p2.keyPointList = kpl2;
		p2.doubleX = dest2[0];
		p2.doubleY = dest2[1];

		double delta = 0.001 * MathUtil.deg2rad;
//		System.out.println(MathUtil.rad2degStr(delta));
		kpl2.rx += delta;
		kpl2.ry += delta;
		kpl2.rz += delta;
		
		SpherePanoTransformer.transformForeward(p2.doubleX, p2.doubleY, kpl2, dest2);
		double dist = SpherePanoTransformer.getSphericalDistance(dest1[0], dest1[1], dest2[0], dest2[1]);
//		System.out.println("DIST=" + MathUtil.rad2degStr(dist));
		kpl1.rx += delta;
		kpl1.ry += delta;
		kpl1.rz += delta;

		kpl2.rx += delta;
		kpl2.ry += delta;
		kpl2.rz += delta;

		KeyPointPair kpp = new KeyPointPair();
		kpp.sourceSP = p1;
		kpp.targetSP = p2;
		
		SphereNorm sn = new SphereNorm();
		sn.setKeyPointPair(kpp);
		
		SpherePanoTransformer.transformForeward(p1.doubleX, p1.doubleY, kpl1, dest1);
		SpherePanoTransformer.transformForeward(p2.doubleX, p2.doubleY, kpl2, dest2);
		double dist0 = SpherePanoTransformer.getSphericalDistance(dest1[0], dest1[1], dest2[0], dest2[1]);
		dist0 += sn.dDist_dIX1 * delta;
		dist0 += sn.dDist_dIY1 * delta;
		dist0 += sn.dDist_dIZ1 * delta;
		dist0 += sn.dDist_dIF1 * delta;
		
		dist0 += sn.dDist_dIX2 * delta;
		dist0 += sn.dDist_dIY2 * delta;
		dist0 += sn.dDist_dIZ2 * delta;
		dist0 += sn.dDist_dIF2 * delta;
		
//		System.out.println(MathUtil.rad2degStr(dist));
//		System.out.println(MathUtil.rad2degStr(dist0));
		assertEqual(dist, dist0);
	}

	void testCalcPrims() {
		KeyPointList origin = new KeyPointList();
		origin.cameraOriginX = 1100;
		origin.cameraOriginY = 2200;
		origin.imageSizeX = (int) (origin.cameraOriginX * 2);
		origin.imageSizeY = (int) (origin.cameraOriginY * 2);
		origin.scaleZ = 0.5 * Math.max(origin.imageSizeX, origin.imageSizeY) * 
			Math.tan(0.5 * KeyPointList.defaultCameraFieldOfView);
		origin.rx = 0 * MathUtil.deg2rad;
		origin.ry = 90 * MathUtil.deg2rad;
		origin.rz = 10 * MathUtil.deg2rad;
		
		KeyPointList kpl = new KeyPointList();
		kpl.cameraOriginX = origin.cameraOriginX;
		kpl.cameraOriginY = origin.cameraOriginY;
		kpl.imageSizeX = (int) (kpl.cameraOriginX * 2);
		kpl.imageSizeY = (int) (kpl.cameraOriginY * 2);
		kpl.scaleZ = origin.scaleZ;
		kpl.rx = 0 * MathUtil.deg2rad;
		kpl.ry = 0 * MathUtil.deg2rad;
		kpl.rz = 0 * MathUtil.deg2rad;

//		double dest1[] = new double[2];
//		double dest2[] = new double[2];
//		
//		SpherePanoTransformer.transformForeward(p1.doubleX, p1.doubleY, origin, dest1);
//		SpherePanoTransformer.transformBackward(dest1[0], dest1[1], kpl, dest2);
		
		KeyPointPairList kppl = new KeyPointPairList();
		kppl.source = origin;
		kppl.target = kpl;
		kppl.rx = 0 * MathUtil.deg2rad;
		kppl.ry = origin.ry;
		kppl.rz = origin.rz;
//		kppl.rz = 0 * MathUtil.deg2rad;
		kppl.scale = kpl.scaleZ;
		
		ArrayList<KeyPointPairList> chain = new ArrayList<KeyPointPairList>();
		chain.add(kppl);		
		ArrayList<KeyPointList> images = new ArrayList<KeyPointList>();
		images.add(kpl);
		
		SpherePanoTransformLearner.calculatePrims(origin, images, chain);
		System.out.println(MathUtil.rad2degStr(kpl.rx));
		System.out.println(MathUtil.rad2degStr(kpl.ry));
		System.out.println(MathUtil.rad2degStr(kpl.rz));
		System.out.println(kpl.scaleZ);
	}
	
	public static void main(String[] args) {
		UT_SpherePanoTransformer test = new UT_SpherePanoTransformer();
		test.testSpherePanoTransformerRotate();
		test.testSpherePanoTransformer();
		test.testSphericalDistance();
		test.testSphereNorm();
		test.testCalcPrims();
		System.out.println("Done");
	}
}
