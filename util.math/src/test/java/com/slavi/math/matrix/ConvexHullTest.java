package com.slavi.math.matrix;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.junit.Test;

import com.slavi.math.ConvexHull;
import com.slavi.math.ConvexHull.ConvexHullArea;
import com.slavi.util.testUtil.TestUtil;

public class ConvexHullTest {
	
	@Test
	public void testConvexHull() throws Exception {
		ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
		points.add(new Point2D.Double(1, 1));
		points.add(new Point2D.Double(1, 1));
		points.add(new Point2D.Double(1, 1));
		points.add(new Point2D.Double(1, 1));
		points.add(new Point2D.Double(1, 1));
		points.add(new Point2D.Double(1, 3));

		points.add(new Point2D.Double(1, 2));
		points.add(new Point2D.Double(1, 3));

		points.add(new Point2D.Double(2, 1));
		points.add(new Point2D.Double(2, 2));
		points.add(new Point2D.Double(2, 3));

		points.add(new Point2D.Double(3, 1));
		points.add(new Point2D.Double(3, 2));
		points.add(new Point2D.Double(3, 3));
		
		points.add(new Point2D.Double(1, 1));
		points.add(new Point2D.Double(1, 2));
		points.add(new Point2D.Double(1, 3));

		points.add(new Point2D.Double(2, 1));
		points.add(new Point2D.Double(2, 2));
		points.add(new Point2D.Double(2, 3));

		points.add(new Point2D.Double(3, 1));
		points.add(new Point2D.Double(3, 2));
		points.add(new Point2D.Double(3, 3));

		ArrayList<Point2D.Double> result = ConvexHull.makeConvexHull(points);
		TestUtil.assertEqual("Number of points in convex hull polygon", result.size(), 5);
		TestUtil.assertEqual("Area of polygon", ConvexHull.getPolygonArea(result), 4.0);
		TestUtil.assertEqual("Area of convex hull polygon", ConvexHull.getConvexHullArea(result), 4.0);
		TestUtil.assertEqual("Area of convex hull polygon", new ConvexHullArea(points).getConvexHullArea(), 4.0);
	}
}
