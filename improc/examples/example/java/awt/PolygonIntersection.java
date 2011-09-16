package example.java.awt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.imageio.ImageIO;

import com.slavi.math.GeometryUtil;
import com.slavi.math.MathUtil;
import com.slavi.util.Const;

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
A*x + B*y - A*x1 - B*y1 = 0
C = - A*x1 - B*y1
A*x + B*y + C = 0 --> (Almost) General form (A>=0 to have a *true* general form)

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
		double lineC = -lineA*x1 - lineB*y1;
		/* 
		 * Now solve the matrix equation
		 * [A] * [X] + [B] = 0
		 * where
		 * [A] = | rayA  rayB  |
		 *       | lineA lineB |
		 *       
		 * [X] = | intersectionX |
		 *       | intersectionY | 
		 *       
		 * [B] = | rayC  |
		 *       | lineC |
		 *       
		 * [X] = -1 * inverse([A]) * [B]
		 * determinant([A]) = rayA * lineB - lineA * rayB
		 * inverse([A]) = (1/determinant([A])) * | lineB   -rayB |
		 *                                       | -lineA  rayA  |
		 */
		double det = rayA * lineB - rayB * lineA;
		if (Math.abs(det) > epsilon) {
			double x = (lineC * rayB - rayC * lineB) / det;
			double y = (rayC * lineA - lineC * rayA) / det;
			// Check if the point is inside the line segment
			if (isBetween(x, x1, x2) && isBetween(y, y1, y2)) {
				points.add(new Point2D.Double(x, y));
			}
		} else {
			// The ray and the line are parallel.
			double tmpC = rayA * x1 + rayB * y1;
			if (Math.abs(tmpC - rayC) < epsilon) {
				// The ray overlaps the line. Add both end points of the line.
				points.add(new Point2D.Double(x1, y1));
				points.add(new Point2D.Double(x2, y2));
			}
		}
	}
	
	public static ArrayList<Point2D> calcRayToPathIntersectionPoints(double rayOffset, double rayAngle, PathIterator iter) {
		double rayA = Math.sin(rayAngle);
		double rayB = -Math.cos(rayAngle); 
		return calcRayToPathIntersectionPoints(rayA, rayB, rayOffset, iter);
	}
	
	public static ArrayList<Point2D> calcRayToPathIntersectionPoints(double rayA, double rayB, double rayC, PathIterator iter) {
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		double coords[] = new double[6];
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
				calcIntersections(rayA, rayB, rayC, curX1, curY1, coords[0], coords[1], points);
				curX1 = coords[0];
				curY1 = coords[1];
				break;
			case PathIterator.SEG_CLOSE:
				if (started) {
					calcIntersections(rayA, rayB, rayC, curX1, curY1, startX1, startY1, points);
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
		
		final boolean inverseOrder = Math.abs(rayA) < epsilon ? 
				(rayB > 0 ? false : true) : 
				(rayA > 0 ? false : true);
		Collections.sort(points, new Comparator<Point2D>() {
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
		
		// Remove duplicate points that occur when the ray passes through a path vertex
		if (points.size() > 0) {
			Point2D p1 = points.get(points.size() - 1);
			for (int i = points.size() - 2; i >= 0; i--) {
				Point2D p2 = points.get(i);
				if ((Math.abs(p1.getX() - p2.getX()) < epsilon) && 
					(Math.abs(p1.getY() - p2.getY()) < epsilon))
					points.remove(i);
				else
					p1 = p2;
			}
		}
		return points;
	}

	private static Path2D makePath(ArrayList<Point2D> points, Area area) {
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

	public static Path2D hatchArea(double startFromX, double startFromY, double rayOffset, double rayAngle, Area area) {
		Path2D result = new Path2D.Double();
		double rayA = Math.sin(rayAngle);
		double rayB = -Math.cos(rayAngle); 
		double rayC = - (rayA * startFromX + rayB * startFromY);
		
		double curOffset = rayC;
		while (true) {
			ArrayList<Point2D> points = calcRayToPathIntersectionPoints(rayA, rayB, curOffset, area.getPathIterator(null));
			Path2D path = makePath2(points, area);
			if (path.getPathIterator(null).isDone())
				break;
			result.append(path.getPathIterator(null), false);
			curOffset += rayOffset;
		}
		curOffset = rayC - rayOffset;
		while (true) {
			ArrayList<Point2D> points = calcRayToPathIntersectionPoints(rayA, rayB, curOffset, area.getPathIterator(null));
			Path2D path = makePath2(points, area);
			if (path.getPathIterator(null).isDone())
				break;
			result.append(path.getPathIterator(null), false);
			curOffset -= rayOffset;
		}
		
		return result;
	}
	
	public static Path2D makePath2(ArrayList<Point2D> points, Area area) {
		Path2D result = new Path2D.Double();
		if (points.size() < 2)
			return result;
		int curPoint = 0;
		Point2D p1 = null;
		Point2D prev = points.get(curPoint++);
		while (curPoint < points.size()) {
			Point2D cur = points.get(curPoint++);
			boolean curLineIsInside = area.contains(
					0.5 * (prev.getX() + cur.getX()),
					0.5 * (prev.getY() + cur.getY()));
			if (curLineIsInside) {
				if (p1 == null)
					p1 = prev;
			} else {
				if (p1 != null) {
					result.moveTo(p1.getX(), p1.getY());
					result.lineTo(prev.getX(), prev.getY());
				}
				p1 = null;
			}
			prev = cur;
		}
		if (p1 != null) {
			result.moveTo(p1.getX(), p1.getY());
			result.lineTo(prev.getX(), prev.getY());
		}
		return result;
	}
	
	//////////////////////////////
	
	BufferedImage bi;
	Graphics2D g;
	static final int cellOffset = 100; 
	int cumulativeCellOffsetX;
	
	Area a1;
	Area a2;
	Area a3;
	Area area;
	
	public PolygonIntersection() {
		bi = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D)bi.getGraphics();
		cumulativeCellOffsetX = 0;

		Polygon p1 = new Polygon();
		p1.addPoint(10, 10);
		p1.addPoint(90, 10);
		p1.addPoint(90, 90);
		p1.addPoint(10, 90);
		a1 = new Area(p1);
		
		Polygon p2 = new Polygon();
		p2.addPoint(20, 20);
		p2.addPoint(80, 20);
		p2.addPoint(80, 80);
		p2.addPoint(20, 80);
		a2 = new Area(p2);
		
		Polygon p3 = new Polygon();
		p3.addPoint(50, 20);
		p3.addPoint(80, 50);
		p3.addPoint(50, 80);
		p3.addPoint(20, 50);
		Area a3 = new Area(p3);
		
		area = new Area();
		area.add(a1);
		area.exclusiveOr(a2);
		area.exclusiveOr(a3);
	}

	private void nextCell() {
		g.translate(cellOffset, 0);
		cumulativeCellOffsetX += cellOffset;
		if ((cumulativeCellOffsetX + cellOffset) > bi.getWidth())
			nextRow();
	}
	
	private void nextRow() {
		g.translate(-cumulativeCellOffsetX, cellOffset);
		cumulativeCellOffsetX = 0;
	}
	
	public void save() throws IOException {
		File fou = new File(Const.tempDir, "hatch.png");
		ImageIO.write(bi, "png", fou);
		System.out.println(fou);
	}

	public void drawHatchedArea(Area a, Shape hatch) {
		g.setColor(Color.white);
		g.draw(a);
		g.fill(a);
		g.setColor(Color.red);
		g.draw(hatch);
		nextCell();
	}
	
	public void doIt() throws IOException {
		ArrayList<Point2D> pointsOfIntersection = calcRayToPathIntersectionPoints(20, 0 * MathUtil.deg2rad, area.getPathIterator(null));
		Path2D path = makePath(pointsOfIntersection, area);
		drawHatchedArea(area, path);
		
		path = makePath2(pointsOfIntersection, area);
		drawHatchedArea(area, path);

		for (int i = 0; i < 16; i++) {
			path = hatchArea(10, 10, 5, i * 22.5 * MathUtil.deg2rad, area);
			drawHatchedArea(area, path);
		}
		
		save();
	}

	public static void main(String[] args) throws Exception {
		new PolygonIntersection().doIt();
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
