package com.slavi.math;

import org.junit.Test;

import com.slavi.util.testUtil.TestUtil;

public class SphericalCoordsLongLatTest {
	@Test
	public void testPolar() throws Exception {
		double point[] = { 5, 15, 25 };
		double tmp1[] = new double[3];
		double tmp2[] = new double[3];
		SphericalCoordsLongLat.cartesianToPolar(point[0], point[1], point[2], tmp1);
		SphericalCoordsLongLat.polarToCartesian(tmp1[0], tmp1[1], tmp1[2], tmp2);
		TestUtil.assertEqual("", point, tmp2);

		SphericalCoordsLongZen.cartesianToPolar(point[0], point[1], point[2], tmp1);
		SphericalCoordsLongZen.polarToCartesian(tmp1[0], tmp1[1], tmp1[2], tmp2);
		TestUtil.assertEqual("", point, tmp2);
	}
}
