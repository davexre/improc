package com.slavi.improc.myadjust;

import org.junit.Test;

import com.slavi.math.MathUtil;
import com.slavi.math.SphericalCoordsLongZen;
import com.slavi.util.testUtil.TestUtil;

public class SphericalCoordsLongZenTest {

	@Test
	public void testGetSphericalDistance() {
		double d;
		d = SphericalCoordsLongZen.getSphericalDistance(0 * MathUtil.deg2rad, 0 * MathUtil.deg2rad, 0 * MathUtil.deg2rad, 0 * MathUtil.deg2rad);
		TestUtil.assertEqualAngle("", d, 0 * MathUtil.deg2rad);

		d = SphericalCoordsLongZen.getSphericalDistance(180 * MathUtil.deg2rad, 0 * MathUtil.deg2rad, -20 * MathUtil.deg2rad, 0 * MathUtil.deg2rad);
		TestUtil.assertEqualAngle("", d, 0 * MathUtil.deg2rad);

		d = SphericalCoordsLongZen.getSphericalDistance(180 * MathUtil.deg2rad, 0 * MathUtil.deg2rad, -20 * MathUtil.deg2rad, -180 * MathUtil.deg2rad);
		TestUtil.assertEqualAngle("", d, 180 * MathUtil.deg2rad);

		d = SphericalCoordsLongZen.getSphericalDistance(10 * MathUtil.deg2rad, 10 * MathUtil.deg2rad, 370 * MathUtil.deg2rad, 70 * MathUtil.deg2rad);
		TestUtil.assertEqualAngle("", d, 60 * MathUtil.deg2rad);

		d = SphericalCoordsLongZen.getSphericalDistance(-10 * MathUtil.deg2rad, 90 * MathUtil.deg2rad, 170 * MathUtil.deg2rad, 90 * MathUtil.deg2rad);
		TestUtil.assertEqualAngle("", d, 180 * MathUtil.deg2rad);
		
		d = SphericalCoordsLongZen.getSphericalDistance(-10 * MathUtil.deg2rad, 10 * MathUtil.deg2rad, 170 * MathUtil.deg2rad, -10 * MathUtil.deg2rad);
		TestUtil.assertEqualAngle("", d, 0 * MathUtil.deg2rad);
		
		d = SphericalCoordsLongZen.getSphericalDistance(-10 * MathUtil.deg2rad, -10 * MathUtil.deg2rad, 170 * MathUtil.deg2rad, 10 * MathUtil.deg2rad);
		TestUtil.assertEqualAngle("", d, 0 * MathUtil.deg2rad);
		
		d = SphericalCoordsLongZen.getSphericalDistance(-10 * MathUtil.deg2rad, 10 * MathUtil.deg2rad, 170 * MathUtil.deg2rad, 350 * MathUtil.deg2rad);
		TestUtil.assertEqualAngle("", d, 0 * MathUtil.deg2rad);
	}
	
	@Test
	public void testPolarToCartesian() {
		double dest1[] = new double[3];
		double dest2[] = new double[3];
		double dest3[] = new double[3];
		dest1[0] = 40 * MathUtil.deg2rad;
		dest1[1] = 50 * MathUtil.deg2rad;
		dest1[2] = 1;
		
		SphericalCoordsLongZen.polarToCartesian(dest1, dest2);
		SphericalCoordsLongZen.cartesianToPolar(dest2, dest3);
		
		TestUtil.assertEqualAngle("0", dest1[0], dest3[0]);
		TestUtil.assertEqualAngle("1", dest1[1], dest3[1]);
		TestUtil.assertEqualAngle("2", dest1[2], dest3[2]);
	}
	
	@Test
	public void testRotate() {
		double rot[] = new double[3];
		double dest1[] = new double[2];
		double dest2[] = new double[2];
		double dest3[] = new double[2];
		rot[0] = 10 * MathUtil.deg2rad;
		rot[1] = 20 * MathUtil.deg2rad;
		rot[2] = 30 * MathUtil.deg2rad;
		dest1[0] = 40 * MathUtil.deg2rad;
		dest1[1] = 50 * MathUtil.deg2rad;
		SphericalCoordsLongZen.rotateForeward(dest1[0], dest1[1], rot[0], rot[1], rot[2], dest2);
		SphericalCoordsLongZen.rotateBackward(dest2[0], dest2[1], rot[0], rot[1], rot[2], dest3);
		TestUtil.assertEqualAngle("", dest1[0], dest3[0]);
		TestUtil.assertEqualAngle("", dest1[1], dest3[1]);

		SphericalCoordsLongZen.rotateForeward(dest1[0], dest1[1], 0, 0 * MathUtil.deg2rad, 0 * MathUtil.deg2rad, dest2);
		TestUtil.assertEqualAngle("", dest1[0], dest2[0]);
		TestUtil.assertEqualAngle("", dest1[1], dest2[1]);

		SphericalCoordsLongZen.rotateBackward(dest1[0], dest1[1], 0, 0 * MathUtil.deg2rad, 0 * MathUtil.deg2rad, dest2);
		TestUtil.assertEqualAngle("", dest1[0], dest2[0]);
		TestUtil.assertEqualAngle("", dest1[1], dest2[1]);

		SphericalCoordsLongZen.rotateForeward(dest1[0], dest1[1], 
				45 * MathUtil.deg2rad, 
				0 * MathUtil.deg2rad, 
				-45 * MathUtil.deg2rad, 
				dest2);
		TestUtil.assertEqualAngle("", dest1[0], dest2[0]);
		TestUtil.assertEqualAngle("", dest1[1], dest2[1]);
	}
}
