package com.slavi.math;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

public class ConvexHull {

	/**
	 * See
	 * http://en.wikipedia.org/wiki/Polygon_area 
	 */
	public static double getPolygonArea(Iterator<Point2D.Double> polygon) {
		if (polygon == null || (!polygon.hasNext()))
			return 0.0;
		double result = 0.0;
		Point2D.Double firstPoint, curPoint;
		firstPoint = curPoint = polygon.next();
		while (polygon.hasNext()) {
			Point2D.Double nextPoint = polygon.next();
			result += curPoint.x * nextPoint.y - nextPoint.x * curPoint.y;
			curPoint = nextPoint;
		}
		result += curPoint.x * firstPoint.y - firstPoint.x * curPoint.y;
		return result * 0.5;
	}
	
	public static double getPolygonArea(Iterable<Point2D.Double> polygon) {
		return getPolygonArea(polygon.iterator());
	}
	
	/**
	 * Ideas borrowed from
	 * http://www.dr-mikes-maths.com/DotPlacer.html 
	 */
	public static ArrayList<Point2D.Double> makeConvexHull(Iterable<Point2D.Double> points) {
		ArrayList<Point2D.Double> result = new ArrayList<Point2D.Double>();
		
		// Find the left-most point, i.e. min X
		Point2D.Double startPoint = null;
		double minX = Double.MAX_VALUE;
		for (Point2D.Double point : points) {
			if (point.x < minX) {
				minX = point.x;
				startPoint = point;
			}
		}
		if (startPoint == null)
			return result;
		result.add(startPoint);
		
		Point2D.Double currentPoint = startPoint;
		double localX = 1.0;
		double localY = 0.0;
		while (true) {
			Point2D.Double nextPoint = null;
			double angle = -2.0;
			double length = -1;
			for (Point2D.Double point : points) {
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
	public static double getConvexHullArea(Iterable<Point2D.Double> points) {
		double result = 0.0;
		
		// Find the left-most point, i.e. min X
		Point2D.Double startPoint = null;
		double minX = Double.MAX_VALUE;
		for (Point2D.Double point : points) {
			if (point.x < minX) {
				minX = point.x;
				startPoint = point;
			}
		}
		if (startPoint == null)
			return 0.0;
		Point2D.Double currentPoint = startPoint;
		double localX = 1.0;
		double localY = 0.0;
		while (true) {
			Point2D.Double nextPoint = null;
			double angle = -2.0;
			double length = -1;
			for (Point2D.Double point : points) {
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
		ArrayList<Point2D.Double> points;
		int curPoint;
		
		public ConvexHullArea(ArrayList<Point2D.Double> points) {
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
}
