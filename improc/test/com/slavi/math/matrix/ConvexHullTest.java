package com.slavi.math.matrix;

import java.awt.Point;
import java.util.ArrayList;

import org.junit.Test;

import com.slavi.TestUtils;
import com.slavi.math.ConvexHull;
import com.slavi.math.ConvexHull.ConvexHullArea;

public class ConvexHullTest {
	
	@Test
	public void testConvexHull() throws Exception {
		ArrayList<Point.Double> points = new ArrayList<Point.Double>();
		points.add(new Point.Double(1, 1));
		points.add(new Point.Double(1, 1));
		points.add(new Point.Double(1, 1));
		points.add(new Point.Double(1, 1));
		points.add(new Point.Double(1, 1));
		points.add(new Point.Double(1, 3));

		points.add(new Point.Double(1, 2));
		points.add(new Point.Double(1, 3));

		points.add(new Point.Double(2, 1));
		points.add(new Point.Double(2, 2));
		points.add(new Point.Double(2, 3));

		points.add(new Point.Double(3, 1));
		points.add(new Point.Double(3, 2));
		points.add(new Point.Double(3, 3));
		
		points.add(new Point.Double(1, 1));
		points.add(new Point.Double(1, 2));
		points.add(new Point.Double(1, 3));

		points.add(new Point.Double(2, 1));
		points.add(new Point.Double(2, 2));
		points.add(new Point.Double(2, 3));

		points.add(new Point.Double(3, 1));
		points.add(new Point.Double(3, 2));
		points.add(new Point.Double(3, 3));

		ArrayList<Point.Double> result = ConvexHull.makeConvexHull(points);
		TestUtils.assertEqual("Number of points in convex hull polygon", result.size(), 5);
		TestUtils.assertEqual("Area of polygon", ConvexHull.getPolygonArea(result), 4.0);
		TestUtils.assertEqual("Area of convex hull polygon", ConvexHull.getConvexHullArea(result), 4.0);
		TestUtils.assertEqual("Area of convex hull polygon", new ConvexHullArea(points).getConvexHullArea(), 4.0);
	}
}
