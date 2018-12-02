package com.slavi.math;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * More info at:
 * http://en.wikipedia.org/wiki/B%C3%A9zier_curve
 *
 * Some code borrowed from:
 * http://www.sunsite.ubc.ca/LivingMathematics/V001N01/UBCExamples/Bezier/bezier.html
 */
public class BezierCubicCurve {

	public Point2D.Double p0;
	public Point2D.Double p1;
	public Point2D.Double p2;
	public Point2D.Double p3;

	public BezierCubicCurve() {
		p0 = new Point2D.Double();
		p1 = new Point2D.Double();
		p2 = new Point2D.Double();
		p3 = new Point2D.Double();
	}

	public BezierCubicCurve(Point2D.Double p0, Point2D.Double p1, Point2D.Double p2, Point2D.Double p3) {
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

	public BezierCubicCurve split(double t) {
		Point2D.Double q0 = GeometryUtil.splitPoint(p0, p1, t);
		Point2D.Double q1 = GeometryUtil.splitPoint(p1, p2, t);
		Point2D.Double q2 = GeometryUtil.splitPoint(p2, p3, t);

		Point2D.Double r0 = GeometryUtil.splitPoint(q0, q1, t);
		Point2D.Double r1 = GeometryUtil.splitPoint(q1, q2, t);

		Point2D.Double b = GeometryUtil.splitPoint(r0, r1, t);

		BezierCubicCurve r = new BezierCubicCurve(b, r1, q2, p3);
		p1 = q0;
		p2 = r0;
		p3 = b;
		return r;
	}

	public BezierCubicCurve bisect() {
		Point2D.Double q0 = GeometryUtil.midPoint(p0, p1);
		Point2D.Double q1 = GeometryUtil.midPoint(p1, p2);
		Point2D.Double q2 = GeometryUtil.midPoint(p2, p3);

		Point2D.Double r0 = GeometryUtil.midPoint(q0, q1);
		Point2D.Double r1 = GeometryUtil.midPoint(q1, q2);

		Point2D.Double b = GeometryUtil.midPoint(r0, r1);

		BezierCubicCurve r = new BezierCubicCurve(b, r1, q2, p3);
		p1 = q0;
		p2 = r0;
		p3 = b;
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

		ArrayList<BezierCubicCurve> todo = new ArrayList<BezierCubicCurve>();
		todo.add(new BezierCubicCurve(p0, p1, p2, p3));
		while (todo.size() > 0) {
			BezierCubicCurve curve = todo.remove(0);
			double breadth = curve.breadth(minLineLength);
			if (breadth > maxBreadth) {
				BezierCubicCurve curve2 = curve.bisect();
				todo.add(0, curve);
				todo.add(1, curve2);
			} else {
				points.add(curve.p3);
			}
		}
	}

	/**
	 * More info on cubic curves:
	 * http://graphics.ucsd.edu/courses/cse167_w06/slides/CSE167_07.ppt
	 * <pre>
	 * 0 &lt;= t &lt;= 1
	 *
	 * SEG_CUBICTO
	 * P(t) = B(3,0)*CP + B(3,1)*P1 + B(3,2)*P2 + B(3,3)*P3
	 * P(t) = CP*(1-t)^3 + P1*3*t*(1-t)^2 + P2*3*t^2*(1-t) + P3*t^3
	 *
	 * B(n,m) = mth coefficient of nth degree Bernstein polynomial
	 * B(n,m) = C(n,m) * t^(m) * (1 - t)^(n-m)
	 *
	 * C(n,m) = Combinations of n things, taken m at a time
	 * C(n,m) = n! / (m! * (n-m)!)
	 * </pre>
	 */
	public Point2D.Double getPointAt(double t) {
		if ((t < 0.0) || (t > 1.0))
			throw new IllegalArgumentException();
		Point2D.Double r = new Point2D.Double();
		double t1 = 1 - t;
		r.x = p0.x * t1*t1*t1 + p1.x * 3*t*t1*t1 + p2.x * 3*t*t*t1 + p3.x * t*t*t;
		r.y = p0.y * t1*t1*t1 + p1.y * 3*t*t1*t1 + p2.y * 3*t*t*t1 + p3.y * t*t*t;
		return r;
	}
}
