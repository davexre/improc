package com.slavi.math;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class GeometryUtil {
	/**
	 * Returns the distance^2 from point to a ray.
	 * 
	 * Code borrowed from: http://www.codeguru.com/forum/printthread.php?t=194400
	 * 
	 * Suppose you have points A(xa, ya), B(xb, yb) and C(xc,yc). The distance 
	 * between point C and line segment AB equals the area of parallelgram ABCC' 
	 * divided by the length of AB.
	 * 
	 * distance = |AB X AC| / sqrt(AB * AB)
	 * Here X mean cross product of vectors, and * mean dot product 
	 * of vectors. This applied in both 2 dimentional and three dimentioanl 
	 * space.
	 * 
	 * In 2-D it becomes:
	 * sqrt(((yb-ya)*(xc-xa)+(xb-xa)*(yc-ya))^2/((xb-xa)^2 + (yb-ya)^2))
	 */
	public static double distanceFromPointToRaySquared(
			double lineX1, double lineY1, 
			double lineX2, double lineY2, 
			double pointX, double pointY) {
		double vx = lineX1 - pointX; 
		double vy = lineY1 - pointY;
		double ux = lineX2 - lineX1;
		double uy = lineY2 - lineY1;
		double length = ux * ux + uy * uy;

		double det = ux * vy - uy * vx;
		return (det * det) / length;
	}

	public static double distanceFromPointToLineSquared(
			double lineX1, double lineY1, 
			double lineX2, double lineY2, 
			double pointX, double pointY) {
		double vx = lineX1 - pointX; 
		double vy = lineY1 - pointY;
		double ux = lineX2 - lineX1;
		double uy = lineY2 - lineY1;
		double length = ux * ux + uy * uy;

		double det = (-vx * ux) + (-vy * uy); //if this is < 0 or > length then its outside the line segment
		if(det < 0 || det > length) {
			ux = lineX2 - pointX;
			uy = lineY2 - pointY;
			return Math.min(vx * vx + vy * vy, ux * ux + uy * uy);
		}
		det = ux * vy - uy * vx;
		return (det * det) / length;
    }
	
	public static double distanceSquared(double x1, double y1, double x2, double y2) {
		x1 -= x2;
		y1 -= y2;
		return x1*x1 + y1*y2;
	}
	
	/**
	 * @return the vertex%points.size() at which the polygon deviates from a (nearly) straight line from v1
	 */
	private static int findAngleStart(ArrayList<Point2D> points, int startAt, double d2) {
		int len = points.size();
		Point2D curPoint = points.get(startAt % len);
		int nextPointIndex = startAt;
		for (int i = len - 1; i >= 0; i--) {
			nextPointIndex++;
			Point2D nextPoint = points.get(nextPointIndex % len);
			for (int j = startAt + 1; j < nextPointIndex; j++) {
				Point2D tmpPoint = points.get(j % len);
				double tmpD2 = distanceFromPointToRaySquared(
						curPoint.getX(), curPoint.getY(), 
						nextPoint.getX(), nextPoint.getY(), 
						tmpPoint.getX(), tmpPoint.getY());
				if (tmpD2 >= d2) {
					return nextPointIndex - 1;
				}
			}
		}
		return -1;
	}

	/**
	 * Returns a new simplified polygon that resembles the input polygon and having removed the
	 * points that are lieing on (almost) stright lines with in a distance d. The method MIGHT
	 * return an invalid polygon, i.e. polygon containing less that 3 points.    
	 */
	public static ArrayList<Point2D> simplifyPolygon(ArrayList<Point2D> polygon, double d) {
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		int startAt = -1;
		for (int i = polygon.size() - 1; i >= 0; i--) {
			Point2D p = polygon.get(i);
			if (p.getX() > minX)
				continue;
			if ((p.getX() == minX) && (p.getY() > minY))
				continue;
			startAt = i;
			minX = p.getX();
			minY = p.getY();
		}
		ArrayList<Point2D> res = new ArrayList<Point2D>();
		if (startAt < 0) {
			return res;
		}
		double d2 = d*d;
		startAt = findAngleStart(polygon, startAt, d2);
		if (startAt < 0) {
			return res;
		}
		int curPoint = startAt; 
		while ((curPoint >= 0) && (curPoint - startAt < polygon.size())) {
			res.add(polygon.get(curPoint % polygon.size()));
			curPoint = findAngleStart(polygon, curPoint, d2);
		}
		return res;
	}
	
	/**
	 * Extracts a polygon from a list of lines. The lines that are used in the
	 * extracted polygon are removed from the supplied list.
	 */
	public static ArrayList<Point2D> extractPolygon(ArrayList<Line2D> lines) {
		if (lines.size() < 1)
			return null;
		ArrayList<Point2D> result = new ArrayList<Point2D>();
		Line2D line = lines.remove(0);
		Point2D start = line.getP1();
		Point2D end = line.getP2();
		result.add(start);
		result.add(end);
		
		boolean first = true;
		while(lines.size() > 0) {
			double d2 = distanceSquared(start.getX(), start.getY(), end.getX(), end.getY());
			if(first)
				d2 = Math.max(d2, 1);

			// Find nearest segment to start OR end point
			boolean aEnd = false;
			int index = -1;
			for(int i = 0; i < lines.size(); i++) {
				line = lines.get(i);
				double dd = distanceSquared(end.getX(), end.getY(), line.getX1(), line.getY1());
				if(dd < d2) {
					d2 = dd;
					aEnd = true;
					index = i;
				}
				dd = distanceSquared(end.getX(), end.getY(), line.getX2(), line.getY2());
				if(dd < d2) {
					d2 = dd;
					aEnd = false;
					index = i;
				}
			}

			if(index >= 0) {
				line = lines.get(index);
				lines.remove(index);
				Point2D p1;
				Point2D p2;
				if(aEnd) {
					p1 = line.getP1();
					p2 = line.getP2();
				} else {
					p2 = line.getP1();
					p1 = line.getP2();
				}
				end.setLocation(
						0.5 * (end.getX() + p1.getX()), 
						0.5 * (end.getY() + p1.getY()));
				end = p2;
				result.add(end);
			} else {
				break;
			}
		}
		return result;
	}
}
