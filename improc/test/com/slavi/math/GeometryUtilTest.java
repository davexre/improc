package com.slavi.math;

import java.awt.geom.Point2D;
import java.util.ArrayList;

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

	@Test
	public void testSimplifyPolygon1() {
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		ArrayList<Point2D> simplified = GeometryUtil.simplifyPolygon(points, 0.5);
		TestUtils.assertTrue("", simplified.size() < 3);

		points.add(new Point2D.Double(0, 0));
		simplified = GeometryUtil.simplifyPolygon(points, 0.5);
		TestUtils.assertTrue("", simplified.size() < 3);

		points.add(new Point2D.Double(1, 0));
		simplified = GeometryUtil.simplifyPolygon(points, 0.5);
		TestUtils.assertTrue("", simplified.size() < 3);

		points.add(new Point2D.Double(2, 0));
		simplified = GeometryUtil.simplifyPolygon(points, 0.5);
		TestUtils.assertTrue("", simplified.size() < 3);

		points.add(new Point2D.Double(2, 0));
		simplified = GeometryUtil.simplifyPolygon(points, 0.5);
		TestUtils.assertTrue("", simplified.size() < 3);

		points.add(new Point2D.Double(2, 2));
		points.add(new Point2D.Double(-2, 2));
		points.add(new Point2D.Double(-2, 0));
		points.add(new Point2D.Double(-1, 0));
		simplified = GeometryUtil.simplifyPolygon(points, 0.5);
		TestUtils.assertEqual("", simplified.size(), 4);
	}
	
	@Test
	public void testSimplifyPolygon2() {
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		points.add(new Point2D.Double(1, 0));
		points.add(new Point2D.Double(4, 0));
		points.add(new Point2D.Double(0, 0));
		points.add(new Point2D.Double(2, 0));
		ArrayList<Point2D> simplified = GeometryUtil.simplifyPolygon(points, 0.5);
		TestUtils.assertTrue("", simplified.size() < 3);
	}
	
	@Test
	public void testSimplifyPolygon3() {
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		points.add(new Point2D.Double(0, 0));
		points.add(new Point2D.Double(1, 0));
		points.add(new Point2D.Double(0, 1));
		points.add(new Point2D.Double(1, 1));
		ArrayList<Point2D> simplified = GeometryUtil.simplifyPolygon(points, 1);
		TestUtils.assertEqual("", simplified.size(), 4);

		simplified = GeometryUtil.simplifyPolygon(points, 1.1);
		TestUtils.assertTrue("", simplified.size() < 3);
	}
		
	@Test
	public void testSimplifyPolygon333() {
		ArrayList<Point2D> points = new ArrayList<Point2D>();
//		points.add(new Point2D.Double(0, 0));
//		points.add(new Point2D.Double(1, 0));
//		points.add(new Point2D.Double(0, 1));
//		points.add(new Point2D.Double(1, 1));
		double d = 1.1;
		for (int i = 0; i < points.size(); i++)
			System.out.println(i + " " + points.get(i));
		System.out.println();
		ArrayList<Point2D> simplified = GeometryUtil.simplifyPolygon(points, d);
		System.out.println(simplified);
	}
	
	public static void main(String[] args) {
		new GeometryUtilTest().testSimplifyPolygon333();
	}
}
