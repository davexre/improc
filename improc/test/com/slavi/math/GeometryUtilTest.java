package com.slavi.math;

import org.junit.Test;

import com.slavi.TestUtils;

public class GeometryUtilTest {

	@Test
	public void testDistanceFromPointToRay() {
		TestUtils.assertEqual("", 0.707, Math.sqrt(GeometryUtil.distanceFromPointToRaySquared(0, 0, 1, 0, 0.707, 0.707)));
		TestUtils.assertEqual("", 0.707, Math.sqrt(GeometryUtil.distanceFromPointToRaySquared(0, 0, 1, 0, -0.707, 0.707)));
		TestUtils.assertEqual("", 0, Math.sqrt(GeometryUtil.distanceFromPointToRaySquared(0, 1, 0, 2, 0, 3)));
	}
	
	@Test
	public void testDistanceFromPointToLine() {
		TestUtils.assertEqual("", 0.707, Math.sqrt(GeometryUtil.distanceFromPointToLineSquared(0, 0, 1, 0, 0.707, 0.707)));
		TestUtils.assertEqual("", 0.9998, Math.sqrt(GeometryUtil.distanceFromPointToLineSquared(0, 0, 1, 0, -0.707, 0.707)));
		TestUtils.assertEqual("", 216.3746, Math.sqrt(GeometryUtil.distanceFromPointToLineSquared(0, 0, 100, 100, 200, -106)));
		TestUtils.assertEqual("", 1, Math.sqrt(GeometryUtil.distanceFromPointToLineSquared(0, 1, 0, 2, 0, 3)));
	}
	
}
