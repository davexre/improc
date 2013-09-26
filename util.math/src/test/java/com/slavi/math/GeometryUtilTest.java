package com.slavi.math;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.junit.Test;

import com.slavi.util.testUtil.TestUtil;

public class GeometryUtilTest {

	@Test
	public void testDistanceFromPointToRay() {
		TestUtil.assertEqual("", 0.707, Math.sqrt(GeometryUtil.distanceFromPointToRaySquared(0, 0, 1, 0, 0.707, 0.707)));
		TestUtil.assertEqual("", 0.707, Math.sqrt(GeometryUtil.distanceFromPointToRaySquared(0, 0, 1, 0, -0.707, 0.707)));
		TestUtil.assertEqual("", 0, Math.sqrt(GeometryUtil.distanceFromPointToRaySquared(0, 1, 0, 2, 0, 3)));
	}
	
	@Test
	public void testDistanceFromPointToLine() {
		TestUtil.assertEqual("", 0.707, Math.sqrt(GeometryUtil.distanceFromPointToLineSquared(0, 0, 1, 0, 0.707, 0.707)));
		TestUtil.assertEqual("", 0.9998, Math.sqrt(GeometryUtil.distanceFromPointToLineSquared(0, 0, 1, 0, -0.707, 0.707)));
		TestUtil.assertEqual("", 216.3746, Math.sqrt(GeometryUtil.distanceFromPointToLineSquared(0, 0, 100, 100, 200, -106)));
		TestUtil.assertEqual("", 1, Math.sqrt(GeometryUtil.distanceFromPointToLineSquared(0, 1, 0, 2, 0, 3)));
	}

	@Test
	public void testSimplifyPolygon1() {
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		ArrayList<Point2D> simplified = GeometryUtil.simplifyPolygon(points, 0.5);
		TestUtil.assertTrue("", simplified.size() < 3);

		points.add(new Point2D.Double(0, 0));
		simplified = GeometryUtil.simplifyPolygon(points, 0.5);
		TestUtil.assertTrue("", simplified.size() < 3);

		points.add(new Point2D.Double(1, 0));
		simplified = GeometryUtil.simplifyPolygon(points, 0.5);
		TestUtil.assertTrue("", simplified.size() < 3);

		points.add(new Point2D.Double(2, 0));
		simplified = GeometryUtil.simplifyPolygon(points, 0.5);
		TestUtil.assertTrue("", simplified.size() < 3);

		points.add(new Point2D.Double(2, 0));
		simplified = GeometryUtil.simplifyPolygon(points, 0.5);
		TestUtil.assertTrue("", simplified.size() < 3);

		points.add(new Point2D.Double(2, 2));
		points.add(new Point2D.Double(-2, 2));
		points.add(new Point2D.Double(-2, 0));
		points.add(new Point2D.Double(-1, 0));
		simplified = GeometryUtil.simplifyPolygon(points, 0.5);
		TestUtil.assertEqual("", simplified.size(), 4);
	}
	
	@Test
	public void testSimplifyPolygon2() {
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		points.add(new Point2D.Double(1, 0));
		points.add(new Point2D.Double(4, 0));
		points.add(new Point2D.Double(0, 0));
		points.add(new Point2D.Double(2, 0));
		ArrayList<Point2D> simplified = GeometryUtil.simplifyPolygon(points, 0.5);
		TestUtil.assertTrue("", simplified.size() < 3);
	}
	
	@Test
	public void testSimplifyPolygon3() {
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		points.add(new Point2D.Double(0, 0));
		points.add(new Point2D.Double(1, 0));
		points.add(new Point2D.Double(0, 1));
		points.add(new Point2D.Double(1, 1));
		ArrayList<Point2D> simplified = GeometryUtil.simplifyPolygon(points, 1);
		TestUtil.assertEqual("", simplified.size(), 4);

		simplified = GeometryUtil.simplifyPolygon(points, 1.1);
		TestUtil.assertTrue("", simplified.size() < 3);
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
	
	@Test
	public void testPointToLine() {
		Point2D.Double a = new Point2D.Double(1, 1);
		Point2D.Double b = new Point2D.Double(2, 1);
		Point2D.Double c = new Point2D.Double(2, 2);
		Point2D.Double mid = GeometryUtil.midPoint(a, b);
		
		TestUtil.assertTrue("", GeometryUtil.pointToLine(a, b, a) == GeometryUtil.PointToLinePosition.EqualsTheStartPoint);
		TestUtil.assertTrue("", GeometryUtil.pointToLine(a, b, b) == GeometryUtil.PointToLinePosition.EqualsTheEndPoint);
		TestUtil.assertTrue("", GeometryUtil.pointToLine(a, b, mid) == GeometryUtil.PointToLinePosition.Inside);
		TestUtil.assertTrue("", GeometryUtil.pointToLine(a, b, c) == GeometryUtil.PointToLinePosition.NegativePlane);
		TestUtil.assertTrue("", GeometryUtil.pointToLine(a, c, b) == GeometryUtil.PointToLinePosition.PositivePlane);
		TestUtil.assertTrue("", GeometryUtil.pointToLine(mid, b, a) == GeometryUtil.PointToLinePosition.BeforeTheStartPoint);
		TestUtil.assertTrue("", GeometryUtil.pointToLine(a, mid, b) == GeometryUtil.PointToLinePosition.AfterTheEndPoint);
		TestUtil.assertTrue("", GeometryUtil.pointToLine(a, a, b) == GeometryUtil.PointToLinePosition.InvalidLine);
	}
	
	public static void main(String[] args) {
		new GeometryUtilTest().testPointToLine();
		System.out.println("Done.");
	}
}
