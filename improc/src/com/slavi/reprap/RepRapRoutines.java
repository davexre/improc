package com.slavi.reprap;

import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.Shape3D;
import javax.vecmath.Point3d;

public class RepRapRoutines {
	public static final double epsilon = 0.0000001;
	
	/**
	 * Returns:
	 * 0	Math.abs(left - right) < epsilon
	 * -1	left < right
	 * 1	left > right
	 */
	public static int compareWithEpsilon(double left, double right) {
		return Math.abs(left - right) < epsilon ? 0 : (left < right ? -1 : 1);
	}
	
	public static int comparePointsWithEpsilon(Point2D p1, Point2D p2) {
		int result;
		switch (compareWithEpsilon(p1.getX(), p2.getX())) {
		case -1:
			result = 1;
			break;
		case 1:
			result = -1;
			break;
		case 0:
		default:
			result = compareWithEpsilon(p1.getY(), p2.getY());
			break;
		}
		return result;
	}
	
	/**
	 * Returns true is the value is between intervalA and intervalB.
	 */
	public static boolean isBetweenWithEpsilon(double value, double intervalA, double intervalB) {
		switch (compareWithEpsilon(intervalA, intervalB)) {
		case -1:
			// intervalA < intervalB
			return 
				(compareWithEpsilon(intervalA, value) <= 0) &&
				(compareWithEpsilon(value, intervalB) <= 0);
		case 1:
			// intervalB < intervalA
			return 
				(compareWithEpsilon(intervalB, value) <= 0) &&
				(compareWithEpsilon(value, intervalA) <= 0);
		case 0:
		default:
			// intervalA = intervalB
			return 
			(compareWithEpsilon(intervalB, value) == 0) &&
			(compareWithEpsilon(value, intervalA) == 0);
		}
	}

	/**
	 * Computes the intersection between a ray and a line segment and adds the 
	 * result to shapes list. The intersection might be a point (a Point2D), 
	 * a line (if the ray overlaps the line segment - Line2D) or there might 
	 * be no intersection at all (nothing is added to the shapes list). 
	 *  
	 * Idea borrowed from http://answers.yahoo.com/question/index?qid=20080717224651AAo2C3j 
	 *
	 * Line is specified as: x1, y1, x2, y2
     * 
	 * (x-x1)/(x2-x1) = (y-y1)/(y2-y1)
	 * A = y2-y1
	 * B = x1-x2
	 * (x-x1)/(-B) = (y-y1)/A
	 * (x-x1)*A = (y-y1)*(-B)
	 * (x-x1)*A = (y1-y)*B
	 * A*x - A*x1 = B*y1 - B*y
	 * A*x + B*y - A*x1 - B*y1 = 0
	 * C = - A*x1 - B*y1
	 * A*x + B*y + C = 0 --> (Almost) General form (A>=0 to have a *true* general form)
	 */
	private static void calcIntersections(double rayA, double rayB, double rayC,
			double x1, double y1, double x2, double y2,
			ArrayList shapes) {
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
			if (isBetweenWithEpsilon(x, x1, x2) && isBetweenWithEpsilon(y, y1, y2)) {
				shapes.add(new Point2D.Double(x, y));
			}
		} else {
			// The ray and the line are parallel.
			double tmpC = -rayA * x1 - rayB * y1;
			if (compareWithEpsilon(tmpC, rayC) == 0) {
				// The ray overlaps the line. Add both end points of the line.
				shapes.add(new Line2D.Double(x1, y1, x2, y2));
			}
		}
	}

	private static boolean getPointSortOrder(double rayA, double rayB) {
		switch (compareWithEpsilon(rayA, 0)) {
		case -1:
			return true;
		case 1:
			return false;
		case 0:
		default:
			return compareWithEpsilon(0, rayB) > 0;
		}
	}

	private static ArrayList calcRayToPathIntersectionPoints(double rayA, double rayB, double rayC, PathIterator iter) {
		ArrayList shapes = new ArrayList();
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
				calcIntersections(rayA, rayB, rayC, curX1, curY1, coords[0], coords[1], shapes);
				curX1 = coords[0];
				curY1 = coords[1];
				break;
			case PathIterator.SEG_CLOSE:
				if (started) {
					calcIntersections(rayA, rayB, rayC, curX1, curY1, startX1, startY1, shapes);
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
		
		final boolean inverseOrder = getPointSortOrder(rayA, rayB);
		// Reverse lines if necessary
		for (int i = 0; i < shapes.size(); i++) {
			Object o = shapes.get(i);
			if (o instanceof Line2D) {
				Line2D l = (Line2D) o;
				int c = comparePointsWithEpsilon(l.getP1(), l.getP2());
				boolean swap = false;
				if (inverseOrder) {
					if (c < 0)
						swap = true;
				} else {
					if (c > 0)
						swap = true;
				}
				if (swap)
					l.setLine(l.getX2(), l.getY2(), l.getX1(), l.getY1());
			}
		}
		
		Collections.sort(shapes, new Comparator() {
			public int compare(Object o1, Object o2) {
				int result = 0;
				if (o1 instanceof Line2D) {
					if (o2 instanceof Line2D) {
						result = comparePointsWithEpsilon(
								((Line2D) o1).getP1(), 
								((Line2D) o2).getP1()); 
						return inverseOrder ? -result : result;
					} else { //if (o2 instanceof Point2D) {
						result = comparePointsWithEpsilon(
								((Line2D) o1).getP1(), 
								(Point2D) o2);
						if (result == 0)
							return 1;
						else
							return inverseOrder ? -result : result;
					}
				} else { //if (o1 instanceof Point2D) {
					if (o2 instanceof Line2D) {
						result = comparePointsWithEpsilon(
								(Point2D) o1, 
								((Line2D) o2).getP1()); 
						if (result == 0)
							return -1;
						else
							return inverseOrder ? -result : result;
					} else { //if (o2 instanceof Point2D) {
						result = comparePointsWithEpsilon(
								(Point2D) o1, 
								(Point2D) o2); 
						return inverseOrder ? -result : result;
					}
				}
			}
		});
		return shapes;
	}

	private static Path2D makePath(ArrayList shapes, Area area) {
		Path2D result = new Path2D.Double();
		if (shapes.size() < 2)
			return result;
		int curPoint = 0;
		Point2D p1;
		Point2D prev;
		Object o = shapes.get(curPoint++);
		if (o instanceof Point2D) {
			p1 = null;
			prev = (Point2D) o;
		} else {
			Line2D l = (Line2D) o;
			p1 = l.getP1();
			prev = l.getP2();
		}
		
		while (curPoint < shapes.size()) {
			o = shapes.get(curPoint++);
			if (o instanceof Point2D) {
				Point2D cur = (Point2D) o;
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
			} else {
				Line2D l = (Line2D) o;
				boolean curLineIsInside = area.contains(
						0.5 * (prev.getX() + l.getX1()),
						0.5 * (prev.getY() + l.getY1()));
				if (curLineIsInside || (comparePointsWithEpsilon(prev, l.getP1()) == 0)) {
					if (p1 == null)
						p1 = prev;
					prev = l.getP2();
				} else {
					if (p1 != null) {
						result.moveTo(p1.getX(), p1.getY());
						result.lineTo(prev.getX(), prev.getY());
					}
					p1 = l.getP1();
					prev = l.getP2();
				}
			}
		}
		if (p1 != null) {
			result.moveTo(p1.getX(), p1.getY());
			result.lineTo(prev.getX(), prev.getY());
		}
		return result;
	}

	private static double getC(double rayA, double rayB, double x, double y) {
		return -rayA*x - rayB*y;
	}
	
	public static Path2D hatchArea(double startFromX, double startFromY, double rayOffset, double rayAngle, Area area) {
		Path2D result = new Path2D.Double();
		double rayA = Math.sin(rayAngle);
		double rayB = -Math.cos(rayAngle);
		double startC = - rayA * startFromX - rayB * startFromY;
		
		Rectangle2D bounds = area.getBounds2D();
		double minC, maxC, tmpC;
		
		tmpC = getC(rayA, rayB, bounds.getX(), bounds.getY());
		minC = maxC = tmpC;
		
		tmpC = getC(rayA, rayB, bounds.getX() + bounds.getWidth(), bounds.getY());
		minC = Math.min(minC, tmpC);
		maxC = Math.max(maxC, tmpC);

		tmpC = getC(rayA, rayB, bounds.getX() + bounds.getWidth(), bounds.getY() + bounds.getHeight());
		minC = Math.min(minC, tmpC);
		maxC = Math.max(maxC, tmpC);

		tmpC = getC(rayA, rayB, bounds.getX(), bounds.getY() + bounds.getHeight());
		minC = Math.min(minC, tmpC);
		maxC = Math.max(maxC, tmpC);
		
		double curC = minC + (startC - minC) % rayOffset;
		while (curC <= maxC) {
			ArrayList shapes = calcRayToPathIntersectionPoints(rayA, rayB, curC, area.getPathIterator(null));
			Path2D path = makePath(shapes, area);
			result.append(path.getPathIterator(null), false);
			curC += rayOffset;
		}
		
		return result;
	}
	
	public static Area areaExpandWithBrushWidth(Stroke stroke, Area area) {
		Area result = new Area();
		result.add(area);
		result.add(new Area(stroke.createStrokedShape(area)));
		return result;
	}
	
	public static Area areaShrinkWithBrushWidth(Stroke stroke, Area area) {
		Area result = new Area();
		result.add(area);
		result.subtract(new Area(stroke.createStrokedShape(area)));
		return result;
	}

	private static void addEdges(ArrayList<Line2D> edges, double z, Point3d p, Point3d q, Point3d r) {
		Point3d odd = null, even1 = null, even2 = null;
		int pat = 0;

		if(p.z < z)
			pat = pat | 1;
		if(q.z < z)
			pat = pat | 2;
		if(r.z < z)
			pat = pat | 4;
		
		switch(pat) {
		case 6:		// q, r below, p above	
		case 1:		// p below, q, r above
			odd = p;
			even1 = q;
			even2 = r;
			break;
			
		case 5:		// p, r below, q above	
		case 2:		// q below, p, r above	
			odd = q;
			even1 = r;
			even2 = p;
			break;

		case 3:		// p, q below, r above	
		case 4:		// r below, p, q above	
			odd = r;
			even1 = p;
			even2 = q;
			break;
			
		case 0:		// All above
		case 7:		// All below
		default:
			return;
		}
		
		// Work out the intersection line segment (e1 -> e2) between the z plane and the triangle
		even1.sub(odd);
		even2.sub(odd);
		double t = (z - odd.z)/even1.z;	

		double x1 = odd.x + t*even1.x;
		double y1 = odd.y + t*even1.y;
		t = (z - odd.z)/even2.z;
		double x2 = odd.x + t*even2.x;
		double y2 = odd.y + t*even2.y;
		edges.add(new Line2D.Double(x1, y1, x2, y2));
	}

	public static void calcShape3DtoPlaneZIntersectionEdges(ArrayList<Line2D> edges, double planeZ, Object object) {
		if (object instanceof Shape3D) {
			Shape3D shape = (Shape3D) object;
			GeometryArray g = (GeometryArray)shape.getGeometry();
			int count = g.getVertexCount();
			if (count % 3 != 0)
				throw new IllegalArgumentException("Vertices not a multiple of 3");
			Point3d p1 = new Point3d();
			Point3d p2 = new Point3d();
			Point3d p3 = new Point3d();
			int i = 0;
			while (i < count) {
	            g.getCoordinate(i++, p1);
	            g.getCoordinate(i++, p2);
	            g.getCoordinate(i++, p3);
	            addEdges(edges, planeZ, p1, p2, p3);
			}
		} else if (object instanceof Group) {
			Group group = (Group) object;
			Enumeration children = group.getAllChildren();
			while (children.hasMoreElements())
				calcShape3DtoPlaneZIntersectionEdges(edges, planeZ, children.nextElement());
		}
	}
	
	public static void calcShapeBounds(Object object, Bounds3d bounds) {
		if (object instanceof Shape3D) {
			Shape3D shape = (Shape3D) object;
			GeometryArray g = (GeometryArray)shape.getGeometry();
			int count = g.getVertexCount();
			Point3d p = new Point3d();
			int i = 0;
			while (i < count) {
	            g.getCoordinate(i++, p);
	            bounds.minX = Math.min(bounds.minX, p.x);
	            bounds.minY = Math.min(bounds.minY, p.y);
	            bounds.minZ = Math.min(bounds.minZ, p.z);
	            
	            bounds.maxX = Math.max(bounds.maxX, p.x);
	            bounds.maxY = Math.max(bounds.maxY, p.y);
	            bounds.maxZ = Math.max(bounds.maxZ, p.z);
			}
		} else if (object instanceof Group) {
			Group group = (Group) object;
			Enumeration children = group.getAllChildren();
			while (children.hasMoreElements())
				calcShapeBounds(children.nextElement(), bounds);
		}
	}
}
