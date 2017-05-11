package com.slavi.math;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ConvexHull {

	/**
	 * See
	 * http://en.wikipedia.org/wiki/Polygon_area 
	 */
	public static <T extends Point2D> double getPolygonArea(Iterator<T> polygon) {
		if (polygon == null || (!polygon.hasNext()))
			return 0.0;
		double result = 0.0;
		T firstPoint, curPoint;
		firstPoint = curPoint = polygon.next();
		while (polygon.hasNext()) {
			T nextPoint = polygon.next();
			result += curPoint.getX() * nextPoint.getY() - nextPoint.getX() * curPoint.getY();
			curPoint = nextPoint;
		}
		result += curPoint.getX() * firstPoint.getY() - firstPoint.getX() * curPoint.getY();
		return result * 0.5;
	}
	
	public static <T extends Point2D> double getPolygonArea(Iterable<T> polygon) {
		return getPolygonArea(polygon.iterator());
	}
	
	private static class ComparePoints<T extends Point2D> implements Comparator<T> {
		public T origin;
		
		// -1 left turn or p2 is closer to origin; 0 equal; 1 right turn 
		public int compare(T p2, T p3) {
			double dx2 = p2.getX() - origin.getX();
			double dy2 = p2.getY() - origin.getY();
			double dx3 = p3.getX() - origin.getX();
			double dy3 = p3.getY() - origin.getY();
			int result = Double.compare(dy2 * dx3, dx2 * dy3);
			if (result == 0) {
				return Double.compare(
						Math.abs(dx2) + Math.abs(dy2), 
						Math.abs(dx3) + Math.abs(dy3));
			}
			return result;
		}
	}
	
	/**
	 * The list of points is reoreded!
	 * https://en.wikipedia.org/wiki/Graham_scan
	 */
	public static <T extends Point2D> ArrayList<T> makeConvexHull2(List<T> points) {
		ArrayList<T> result = new ArrayList<>();
		if (points.size() < 1)
			return result;
		
		ComparePoints<T> comparePoints = new ComparePoints<>();
		comparePoints.origin = points.get(0);
		for (int i = points.size() - 1; i >= 1; i--) {
			T p = points.get(i);
			if ((p.getY() < comparePoints.origin.getY()) ||
				((p.getY() == comparePoints.origin.getY()) && (p.getX() < comparePoints.origin.getX()))) {
				comparePoints.origin = p;
			}
		}
		Collections.sort(points, comparePoints);

		result.add(comparePoints.origin);
		for (T p : points) {
			while (true) {
				int size = result.size();
				T p2 = null;
				if (size > 0) {
					p2 = result.get(size - 1);
					if (p2.getX() == p.getX() && p2.getY() == p.getY())
						break; // equal points
				}
				if (size < 2) {
					result.add(p);
					break;
				}
				comparePoints.origin = result.get(size - 2);
				int r = comparePoints.compare(p2, p);
				if (r < 0) {
					// left turn
					result.add(p);
					break;
				}
				result.remove(size - 1);
			}
		}
		return result;
	}
	
	/**
	 * Ideas borrowed from
	 * http://www.dr-mikes-maths.com/DotPlacer.html 
	 */
	public static <T extends Point2D> ArrayList<T> makeConvexHull(Iterable<T> points) {
		ArrayList<T> result = new ArrayList<>();
		
		// Find the left-most point, i.e. min X
		T startPoint = null;
		double minX = Double.MAX_VALUE;
		for (T point : points) {
			if (point.getX() < minX) {
				minX = point.getX();
				startPoint = point;
			}
		}
		if (startPoint == null)
			return result;
		result.add(startPoint);
		
		T currentPoint = startPoint;
		double localX = 1.0;
		double localY = 0.0;
		while (true) {
			T nextPoint = null;
			double angle = -2.0;
			double length = -1;
			for (T point : points) {
				double dX = point.getX() - currentPoint.getX();
				double dY = point.getY() - currentPoint.getY();
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
			double dX = nextPoint.getX() - currentPoint.getX();
			double dY = nextPoint.getY() - currentPoint.getY();
			double hypot = MathUtil.hypot(dX, dY);
			if (hypot != 0) {
				localX = dX / hypot;
				localY = dY / hypot;
			}
			currentPoint = nextPoint;

			dX = nextPoint.getX() - startPoint.getX();
			dY = nextPoint.getY() - startPoint.getY();
			if ((dX == 0.0) && (dY == 0.0))
				break;
		}
		return result;
	}

	/**
	 * Ideas borrowed from
	 * http://www.dr-mikes-maths.com/DotPlacer.html 
	 */
	public static <T extends Point2D> double getConvexHullArea(Iterable<T> points) {
		double result = 0.0;
		
		// Find the left-most point, i.e. min X
		T startPoint = null;
		double minX = Double.MAX_VALUE;
		for (T point : points) {
			if (point.getX() < minX) {
				minX = point.getX();
				startPoint = point;
			}
		}
		if (startPoint == null)
			return 0.0;
		T currentPoint = startPoint;
		double localX = 1.0;
		double localY = 0.0;
		while (true) {
			T nextPoint = null;
			double angle = -2.0;
			double length = -1;
			for (T point : points) {
				double dX = point.getX() - currentPoint.getX();
				double dY = point.getY() - currentPoint.getY();
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
			result += currentPoint.getX() * nextPoint.getY() - nextPoint.getX() * currentPoint.getY();
			double dX = nextPoint.getX() - currentPoint.getX();
			double dY = nextPoint.getY() - currentPoint.getY();
			double hypot = MathUtil.hypot(dX, dY);
			if (hypot != 0) {
				localX = dX / hypot;
				localY = dY / hypot;
			}
			currentPoint = nextPoint;

			dX = nextPoint.getX() - startPoint.getX();
			dY = nextPoint.getY() - startPoint.getY();
			if ((dX == 0.0) && (dY == 0.0))
				break;
		}
		return result * 0.5;
	}

	public static class ConvexHullArea<T extends Point2D> extends AbstractConvexHullArea {
		ArrayList<T> points;
		int curPoint;
		
		public ConvexHullArea(ArrayList<T> points) {
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
			return points.get(curPoint).getX();
		}

		public double getY() {
			return points.get(curPoint).getY();
		}
	}
}
