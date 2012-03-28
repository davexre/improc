package com.slavi.math;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Borrowed from:
 * http://www.sunsite.ubc.ca/LivingMathematics/V001N01/UBCExamples/Bezier/bezier.html
 */
public class BezierCurve {
	
	public Point2D.Double p0;
	public Point2D.Double p1;
	public Point2D.Double p2;
	public Point2D.Double p3;
	
	public BezierCurve() {
		p0 = new Point2D.Double();
		p1 = new Point2D.Double();
		p2 = new Point2D.Double();
		p3 = new Point2D.Double();
	}
	
	public BezierCurve(Point2D.Double p0, Point2D.Double p1, Point2D.Double p2, Point2D.Double p3) {
		this.p0 = p0;
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
	}
	
	public double breadth(double minLineLength) {
		if (minLineLength <= 0)
			minLineLength = 0.01;
		double dx = p3.x - p0.x;
		double dy = p3.y - p0.y;
		double dist = Math.sqrt(dy * dy + dx * dx);

		if (dist < minLineLength) {
			return 0;
//			double a1 = Math.abs(p1.x - p0.x) + Math.abs(p1.y - p0.y);
//			double a2 = Math.abs(p2.x - p0.x) + Math.abs(p2.y - p0.y);
//			return Math.max(a1, a2);
		}
		double d2 = p3.x * p0.y - p0.x * p3.y;
		double a1 = Math.abs((dx * p1.y - dy * p1.x) - d2) / dist;
		double a2 = Math.abs((dx * p2.y - dy * p2.x) - d2) / dist;
		return Math.max(a1, a2);
	}
	
	public static Point2D.Double midPoint(Point2D.Double a, Point2D.Double b) {
		return new Point2D.Double(
				0.5 * (a.x + b.x),
				0.5 * (a.y + b.y));
	}

	public BezierCurve bisect() {
		Point2D.Double m01 = midPoint(p0, p1);
		Point2D.Double m12 = midPoint(p1, p2);
		Point2D.Double m23 = midPoint(p2, p3);
		
		Point2D.Double n0 = midPoint(m01, m12);
		Point2D.Double n1 = midPoint(m12, m23);

		Point2D.Double o = midPoint(n0, n1);

		BezierCurve r = new BezierCurve(o, n1, m23, p3);
		p1 = m01;
		p2 = n0;
		p3 = o;
		return r;
	}
	
	public void appendToPath(Path2D.Double path) {
		path.moveTo(p0.x, p0.y);
		path.curveTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);
	}
	
	public void appendToPointsList(List<Point2D.Double> points, double maxBreadth, double minLineLength) {
		if (maxBreadth <= 0)
			maxBreadth = 0.1;
		points.add(p0);

		ArrayList<BezierCurve> todo = new ArrayList<BezierCurve>();
		todo.add(new BezierCurve(p0, p1, p2, p3));
		while (todo.size() > 0) {
			BezierCurve curve = todo.remove(0);
			if (curve.breadth(minLineLength) > maxBreadth) {
				BezierCurve curve2 = curve.bisect();
				todo.add(0, curve);
				todo.add(1, curve2);
			} else {
				points.add(curve.p3);
			}
		}
	}
}
