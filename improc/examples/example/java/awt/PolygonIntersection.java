package example.java.awt;

import java.awt.BasicStroke;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.slavi.math.GeometryUtil;
import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;

public class PolygonIntersection {
	
/*
Idea borrowed from http://answers.yahoo.com/question/index?qid=20080717224651AAo2C3j

Line is specified as: x1, y1, x2, y2

(x-x1)/(x2-x1) = (y-y1)/(y2-y1)
A = y2-y1
B = x1-x2
(x-x1)/(-B) = (y-y1)/A
(x-x1)*A = (y-y1)*(-B)
(x-x1)*A = (y1-y)*B
A*x - A*x1 = B*y1 - B*y
A*x + B*y = A*x1 + B*y1
C = A*x1 + B*y1
A*x + B*y = C --> (Almost) General form (A,B,C should be integers and A>=0 to have a *true* general form)

*/
	
	public static boolean isBetween(double value, double intervalA, double intervalB) {
		return intervalA < intervalB ?
				(intervalA <= value + epsilon) && (value - epsilon <= intervalB) :
				(intervalB <= value + epsilon) && (value - epsilon <= intervalA);
	}

	static final double epsilon = 0.0000001;
	private static void calcIntersections(double rayA, double rayB, double rayC,
			double x1, double y1, double x2, double y2,
			ArrayList<Point2D> points) {
		double lineA = y2 - y1;
		double lineB = x1 - x2;
		double lineC = lineA*x1 + lineB*y1;
		/* 
		 * Now solve the matrix equation
		 * [A] * [X] = [B]
		 * where
		 * [A] = | rayA  rayB  |
		 *       | lineA lineB |
		 *       
		 * [X] = | intersectionX |
		 *       | intersectionY | 
		 *       
		 * [B] = | rayC  |
		 *       | lineC |
		 */
		double det = rayA * lineB - rayB * lineA;
		if (Math.abs(det) > epsilon) {
			double x = (rayC * lineB - lineC * rayB) / det;
			double y = (lineC * rayA - rayC * lineA) / det;
			// Check if the point is inside the line segment
			if (isBetween(x, x1, x2) && isBetween(y, y1, y2)) {
				System.out.println("between");
				points.add(new Point2D.Double(x, y));
			} else {
				System.out.println("not between");
			}
			System.out.println("x=" + x);
			System.out.println("y=" + y);
		} else {
			// The ray and the line are parallel.
			double tmpC = rayA * x1 + rayB * y1;
			if (Math.abs(tmpC - rayC) < epsilon) {
				// The ray overlaps the line. Add both end points of the line.
				System.out.println("coincide");
				points.add(new Point2D.Double(x1, y1));
				points.add(new Point2D.Double(x2, y2));
			} else {
				System.out.println("just parallel");
			}
			System.out.println("tmpC=" + tmpC);
			System.out.println("rayC=" + rayC);
		}
		System.out.println("x1=" + x1);
		System.out.println("y1=" + y1);
		System.out.println("x2=" + x2);
		System.out.println("y2=" + y2);
	}
	
	public static ArrayList<Point2D> calcRayToPolygonIntersections(double rayOffset, double rayAngle, PathIterator iter) {
		ArrayList<Point2D> result = new ArrayList<Point2D>();
		double coords[] = new double[6];
		double rayA = Math.sin(rayAngle);
		double rayB = -Math.cos(rayAngle);
		double curX1 = 0;
		double curY1 = 0;
		double startX1 = 0;
		double startY1 = 0;
		boolean started = false;
		while (!iter.isDone()) {
			int seg = iter.currentSegment(coords);
			switch (seg) {
			case PathIterator.SEG_MOVETO:
				curX1 = coords[0];
				curY1 = coords[1];
				if (!started) {
					startX1 = curX1;
					startY1 = curY1;
					started = true;
				}
				break;
			case PathIterator.SEG_LINETO: 
				if (!started) {
					startX1 = curX1;
					startY1 = curY1;
					started = true;
				}
				calcIntersections(rayA, rayB, rayOffset, curX1, curY1, coords[0], coords[1], result);
				curX1 = coords[0];
				curY1 = coords[1];
				break;
			case PathIterator.SEG_CLOSE:
				if (started) {
					calcIntersections(rayA, rayB, rayOffset, curX1, curY1, startX1, startY1, result);
				}
				started = false;
				break;
			case PathIterator.SEG_QUADTO:
			case PathIterator.SEG_CUBICTO:
			default:
				throw new IllegalArgumentException("Path iterator contains unsupported path segment types");
			}
			iter.next();
		}
		final boolean inverseOrder = rayA > 0 ? false : rayA < 0 ? true : rayB > 0 ? false : true;
		Collections.sort(result, new Comparator<Point2D>() {
			public int compare(Point2D o1, Point2D o2) {
				int result;
				if (o1.getX() < o2.getX())
					result = 1;
				else if (o1.getX() > o2.getX())
					result = -1;
				else if (o1.getY() < o2.getY())
					result = 1;
				else if (o1.getY() > o2.getY())
					result = -1;
				else
					result = 0;
				return inverseOrder ? -result : result;
			}
		});
		return result;
	}

	public static Path2D makePath(ArrayList<Point2D> points) {
		Path2D result = new Path2D.Double();
		for (int i = 0; i < points.size(); i++) {
			Point2D p = points.get(i);
			if (i % 2 == 0)
				result.moveTo(p.getX(), p.getY());
			else
				result.lineTo(p.getX(), p.getY());
		}
		return result;
	}
	
	public static void main(String[] args) {
		Polygon p = new Polygon();
		p.addPoint(10, 10);
		p.addPoint(20, 10);
		p.addPoint(20, 20);
		p.addPoint(10, 20);
		System.out.println(GeometryUtil.pathIteratorToString(p.getPathIterator(null)));
		Path2D intersect = makePath(calcRayToPolygonIntersections(0, 45 * MathUtil.deg2rad, p.getPathIterator(null)));
		System.out.println("-- intersection --");
		System.out.println(GeometryUtil.pathIteratorToString(intersect.getPathIterator(null)));
	}
	
	
	public static void main2(String[] args) {
		Path2D.Double p1 = new Path2D.Double();
		p1.moveTo(0, 0);
		p1.lineTo(1, 0);
		p1.lineTo(0, 1);
		p1.closePath();
		
		Polygon p2 = new Polygon();
		p2.addPoint(0, 0);
		p2.addPoint(1, 0);
		p2.addPoint(1, 1);
		
//		p2.translate(10, 10);
		Area a1 = new Area(p1);
		Area a2 = new Area(p2);
		Area intersect = new Area();
		intersect.add(a1);
		intersect.add(a2);

		System.out.println(GeometryUtil.pathIteratorToString(intersect.getPathIterator(null)));
		System.out.println("-------");
		Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
		Area l;
//		l = new Area(stroke.createStrokedShape(new Line2D.Double(0, -0.5, 2, 2)));
		l = new Area(stroke.createStrokedShape(new Rectangle2D.Double(1, 2, 3, 4)));
//		l = new Area(new Ellipse2D.Double(0, 0, 10, 20));
		intersect.reset();
		intersect.add(a1);
		intersect.intersect(l);
		System.out.println(GeometryUtil.pathIteratorToString(l.getPathIterator(null)));
		
		System.out.println("Done.");
	}
}
