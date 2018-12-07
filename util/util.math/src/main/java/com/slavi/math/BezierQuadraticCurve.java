package com.slavi.math;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * More info at:
 * http://en.wikipedia.org/wiki/B%C3%A9zier_curve
 */
public class BezierQuadraticCurve {

	public Point2D.Double p0;
	public Point2D.Double p1;
	public Point2D.Double p2;

	public BezierQuadraticCurve() {
		p0 = new Point2D.Double();
		p1 = new Point2D.Double();
		p2 = new Point2D.Double();
	}

	public BezierQuadraticCurve(Point2D.Double p0, Point2D.Double p1, Point2D.Double p2) {
		this.p0 = p0;
		this.p1 = p1;
		this.p2 = p2;
	}
/*
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
*/

	public BezierQuadraticCurve split(double t) {
		Point2D.Double q0 = GeometryUtil.splitPoint(p0, p1, t);
		Point2D.Double q1 = GeometryUtil.splitPoint(p1, p2, t);

		Point2D.Double b = GeometryUtil.splitPoint(q0, q1, t);

		BezierQuadraticCurve r = new BezierQuadraticCurve(b, q1, p2);
		p1 = q0;
		p2 = b;
		return r;
	}

	public static Point2D.Double midPoint(Point2D.Double a, Point2D.Double b) {
		return new Point2D.Double(
				0.5 * (a.x + b.x),
				0.5 * (a.y + b.y));
	}

	public BezierQuadraticCurve bisect() {
		Point2D.Double q0 = GeometryUtil.midPoint(p0, p1);
		Point2D.Double q1 = GeometryUtil.midPoint(p1, p2);

		Point2D.Double b = GeometryUtil.midPoint(q0, q1);

		BezierQuadraticCurve r = new BezierQuadraticCurve(b, q1, p2);
		p1 = q0;
		p2 = b;
		return r;
	}

	public void appendToPath(Path2D.Double path) {
		path.moveTo(p0.x, p0.y);
		path.quadTo(p1.x, p1.y, p2.x, p2.y);
	}

	public void appendToPointsList(List<Point2D.Double> points, double maxBreadth, double minLineLength) {
		if (maxBreadth <= 0)
			maxBreadth = 0.1;
		points.add(p0);

		ArrayList<BezierQuadraticCurve> todo = new ArrayList<BezierQuadraticCurve>();
		todo.add(new BezierQuadraticCurve(p0, p1, p2));
		while (todo.size() > 0) {
			BezierQuadraticCurve curve = todo.remove(0);
			double breadth = 0; // TODO: curve.breadth(minLineLength);
			if (breadth > maxBreadth) {
				BezierQuadraticCurve curve2 = curve.bisect();
				todo.add(0, curve);
				todo.add(1, curve2);
			} else {
				points.add(curve.p2);
			}
		}
	}

	/**
	 * <pre>
	 * 0 &lt;= t &lt;= 1
	 *
	 * B(n,m) = mth coefficient of nth degree Bernstein polynomial
	 * B(n,m) = C(n,m) * t^(m) * (1 - t)^(n-m)
	 *
	 * C(n,m) = Combinations of n things, taken m at a time
	 * C(n,m) = n! / (m! * (n-m)!)
	 *
	 * C(2,0) = 2! / (0! * 2!) = 1
	 * C(2,1) = 2! / (1! * 1!) = 2
	 * C(2,2) = 2! / (2! * 0!) = 1
	 *
	 * C(3,0) = 3! / (0! * 3!) = 1
	 * C(3,1) = 3! / (1! * 2!) = 3
	 * C(3,2) = 3! / (2! * 1!) = 3
	 * C(3,3) = 3! / (3! * 0!) = 1
	 *
	 * B(2,0) = (1-t)^2
	 * B(2,1) = 2 * t * (1-t)
	 * B(2,2) = t^2
	 *
	 * B(3,0) = (1-t)^3
	 * B(3,1) = 3 * t * (1-t)^2
	 * B(3,2) = 3 * t^2 * (1-t)
	 * B(3,3) = t^3
	 *
	 * SEG_QUADTO
	 * P(t) = B(2,0)*CP + B(2,1)*P1 + B(2,2)*P2
	 * P(t) = CP*(1-t)^2 + P1*2*t*(1-t) + P2*t^2
	 * </pre>
	 */
	public Point2D.Double getPointAt(double t) {
		if ((t < 0.0) || (t > 1.0))
			throw new IllegalArgumentException();
		Point2D.Double r = new Point2D.Double();
		double t1 = 1 - t;
		r.x = p0.x * t1*t1 + p1.x * 2*t*t1 + p2.x * t*t;
		r.y = p0.y * t1*t1 + p1.y * 2*t*t1 + p2.y * t*t;
		return r;
	}
}
