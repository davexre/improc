package com.slavi.math;

import java.awt.Point;
import java.util.ArrayList;

public class ConvexHull {

	/**
	 * See
	 * http://en.wikipedia.org/wiki/Polygon_area 
	 */
	public static double getPolygonArea(ArrayList<Point.Double> polygon) {
		if (polygon == null || polygon.size() <= 1)
			return 0.0;
		double result = 0.0;
		Point.Double curPoint = polygon.get(0);
		for (int i = 1; i < polygon.size(); i++) {
			Point.Double nextPoint = polygon.get(i);
			result += curPoint.x * nextPoint.y - nextPoint.x * curPoint.y;
			curPoint = nextPoint;
		}
		Point.Double nextPoint = polygon.get(0);
		result += curPoint.x * nextPoint.y - nextPoint.x * curPoint.y;
		return result * 0.5;
	}
	
	/**
	 * Ideas borrowed from
	 * http://www.dr-mikes-maths.com/DotPlacer.html 
	 */
	public static ArrayList<Point.Double> makeConvexHull(ArrayList<Point.Double> points) {
		ArrayList<Point.Double> result = new ArrayList<Point.Double>();
		
		// Find the left-most point, i.e. min X
		Point.Double startPoint = null;
		double minX = Double.MAX_VALUE;
		for (Point.Double point : points) {
			if (point.x < minX) {
				minX = point.x;
				startPoint = point;
			}
		}
		if (startPoint == null)
			return result;
		result.add(startPoint);
		
		Point.Double currentPoint = startPoint;
		double localX = 1.0;
		double localY = 0.0;
		while (true) {
			Point.Double nextPoint = null;
			double angle = -2.0;
			double length = -1;
			for (Point.Double point : points) {
				double dX = point.x - currentPoint.x;
				double dY = point.y - currentPoint.y;
				double hypot = MathUtil.hypot(dX, dY);
				if (hypot != 0) {
					double cur = (localX * dX + localY * dY) / hypot;
					if ((cur > angle) || ((cur == angle) && (hypot > length))) {
						nextPoint = point;
						length = hypot;
						angle = cur;
					}
				}
			}
			if (nextPoint == null)
				return result;
			result.add(nextPoint);
			double dX = nextPoint.x - currentPoint.x;
			double dY = nextPoint.y - currentPoint.y;
			double hypot = MathUtil.hypot(dX, dY);
			if (hypot != 0) {
				localX = dX / hypot;
				localY = dY / hypot;
			}
			currentPoint = nextPoint;

			dX = nextPoint.x - startPoint.x;
			dY = nextPoint.y - startPoint.y;
			if ((dX == 0.0) && (dY == 0.0))
				break;
		}
		return result;
	}

	/**
	 * Ideas borrowed from
	 * http://www.dr-mikes-maths.com/DotPlacer.html 
	 */
	public static double getConvexHullArea(ArrayList<Point.Double> points) {
		double result = 0.0;
		
		// Find the left-most point, i.e. min X
		Point.Double startPoint = null;
		double minX = Double.MAX_VALUE;
		for (Point.Double point : points) {
			if (point.x < minX) {
				minX = point.x;
				startPoint = point;
			}
		}
		if (startPoint == null)
			return 0.0;
		Point.Double currentPoint = startPoint;
		double localX = 1.0;
		double localY = 0.0;
		while (true) {
			Point.Double nextPoint = null;
			double angle = -2.0;
			double length = -1;
			for (Point.Double point : points) {
				double dX = point.x - currentPoint.x;
				double dY = point.y - currentPoint.y;
				double hypot = MathUtil.hypot(dX, dY);
				if (hypot != 0) {
					double cur = (localX * dX + localY * dY) / hypot;
					if ((cur > angle) || ((cur == angle) && (hypot > length))) {
						nextPoint = point;
						length = hypot;
						angle = cur;
					}
				}
			}
			if (nextPoint == null)
				return 0.0;
			result += currentPoint.x * nextPoint.y - nextPoint.x * currentPoint.y;
			double dX = nextPoint.x - currentPoint.x;
			double dY = nextPoint.y - currentPoint.y;
			double hypot = MathUtil.hypot(dX, dY);
			if (hypot != 0) {
				localX = dX / hypot;
				localY = dY / hypot;
			}
			currentPoint = nextPoint;

			dX = nextPoint.x - startPoint.x;
			dY = nextPoint.y - startPoint.y;
			if ((dX == 0.0) && (dY == 0.0))
				break;
		}
		return result * 0.5;
	}

	public static class ConvexHullArea extends AbstractConvexHullArea {
		ArrayList<Point.Double> points;
		int curPoint;
		
		public ConvexHullArea(ArrayList<Point.Double> points) {
			this.points = points;
			curPoint = -1;
		}
		
		public void resetPointIterator() {
			curPoint = -1;
		}

		public boolean nextPoint() {
			return (++curPoint) < points.size();
		}

		public double getX() {
			return points.get(curPoint).x;
		}

		public double getY() {
			return points.get(curPoint).y;
		}
	}
	
	public static void main(String[] args) {
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

		ArrayList<Point.Double> result = makeConvexHull(points);
		System.out.println(result.size());
		for (int i = 0; i < result.size(); i++) {
			System.out.println(result.get(i));
		}
		System.out.println(getPolygonArea(result));
		System.out.println(getConvexHullArea(points));
		System.out.println(new ConvexHullArea(points).getConvexHullArea());
	}
}
