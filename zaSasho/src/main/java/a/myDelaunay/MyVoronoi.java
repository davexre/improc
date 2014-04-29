package a.myDelaunay;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import com.slavi.math.GeometryUtil;
import com.slavi.math.MathUtil;
import com.slavi.math.GeometryUtil.PointToLinePosition;

public class MyVoronoi {

	public static int pointToLine(
			double lineX1, double lineY1, 
			double lineX2, double lineY2, 
			double pointX, double pointY) {
		double dXBA = lineX2 - lineX1;
		double dYBA = lineY2 - lineY1;
/*		double f2 = dYBA * (pointX - lineX1) - dXBA * (pointY - lineY1);
		if (f2 < 0.0)
			return PointToLinePosition.NegativePlane;
		if (f2 > 0.0)
			return PointToLinePosition.PositivePlane;
*/		
		// Point P lies on the line
		if (Math.abs(dXBA) > Math.abs(dYBA)) {
			if (dXBA > 0.0) {
				if (pointX < lineX1) {
					return PointToLinePosition.BeforeTheStartPoint;
				} else if (pointX == lineX1) {
					return PointToLinePosition.EqualsTheStartPoint;
				} else if (pointX > lineX2) {
					return PointToLinePosition.AfterTheEndPoint;
				} else if (pointX == lineX2) {
					return PointToLinePosition.EqualsTheEndPoint;
				} else {
					// P between A and B
					return PointToLinePosition.Inside;
				}
			}
			
			if (dXBA < 0.0) {
				if (lineX1 < pointX) {
					return PointToLinePosition.BeforeTheStartPoint;
				} else if (lineX1 == pointX) {
					return PointToLinePosition.EqualsTheStartPoint;
				} else if (lineX2 > pointX) {
					return PointToLinePosition.AfterTheEndPoint;
				} else if (lineX2 == pointX) {
					return PointToLinePosition.EqualsTheEndPoint;
				} else {
					return PointToLinePosition.Inside;
				}
			}
		}
		
		if (dYBA > 0.0) {
			if (pointY < lineY1) {
				return PointToLinePosition.BeforeTheStartPoint;
			} else if (pointY == lineY1) {
				return PointToLinePosition.EqualsTheStartPoint;
			} else if (pointY > lineY2) {
				return PointToLinePosition.AfterTheEndPoint;
			} else if (pointY == lineY2) {
				return PointToLinePosition.EqualsTheEndPoint;
			} else {
				return PointToLinePosition.Inside;
			}
		}
		
		if (dYBA < 0.0) {
			if (lineY1 < pointY) {
				return PointToLinePosition.BeforeTheStartPoint;
			} else if (lineY1 == pointY) {
				return PointToLinePosition.EqualsTheStartPoint;
			} else if (lineY2 > pointY) {
				return PointToLinePosition.AfterTheEndPoint;
			} else if (lineY2 == pointY) {
				return PointToLinePosition.EqualsTheEndPoint;
			} else {
				return PointToLinePosition.Inside;
			}
		}
		// The points of the line are equal, i.e. lineA = lineB
		return PointToLinePosition.InvalidLine;
	}
	
	static Triangle getNextTriangle(Triangle t, Point2D p) {
		if (t.a == p) {
			return t.getAb();
		} else if (t.b == p) {
			return t.getBc();
		} else {
			return t.getCa();
		}
	}

	static Point2D intersectWithRect(Point2D a, Point2D b, Point2D lowerLeft, Point2D upperRight, int rectSide) {
		double line2X1, line2Y1;
		double line2X2, line2Y2;
		
		switch (rectSide % 4) {
		case 0:
			line2X1 = lowerLeft.getX();
			line2Y1 = lowerLeft.getY();
			line2X2 = lowerLeft.getX();
			line2Y2 = upperRight.getY();
			break;
		case 1:
			line2X1 = lowerLeft.getX();
			line2Y1 = upperRight.getY();
			line2X2 = upperRight.getX();
			line2Y2 = upperRight.getY();
			break;
		case 2:
			line2X1 = upperRight.getX();
			line2Y1 = upperRight.getY();
			line2X2 = upperRight.getX();
			line2Y2 = lowerLeft.getY();
			break;
		case 3:
		default:
			line2X1 = upperRight.getX();
			line2Y1 = lowerLeft.getY();
			line2X2 = lowerLeft.getX();
			line2Y2 = lowerLeft.getY();
			break;
		}
		Point2D mid = GeometryUtil.midPoint(a, b);
		double bx = b.getX() - a.getX();
		double by = b.getY() - a.getY();
		double angle = Math.atan2(by, bx) + MathUtil.PIover2; // atan2(y, x) + pi/2 = atan2(x, y)
		bx = mid.getX() + 100 * Math.cos(angle);
		by = mid.getY() + 100 * Math.sin(angle);
		
		Point2D r = GeometryUtil.lineIntersectsLine(mid.getX(), mid.getY(), bx, by, line2X1, line2Y1, line2X2, line2Y2);
		if (r == null)
			return null;
		int pos = pointToLine(mid.getX(), mid.getY(), bx, by, r.getX(), r.getY());
		if (pos == GeometryUtil.PointToLinePosition.AfterTheEndPoint || 
			pos == GeometryUtil.PointToLinePosition.Inside ||
			pos == GeometryUtil.PointToLinePosition.EqualsTheEndPoint) {
			pos = pointToLine(line2X1, line2Y1, line2X2, line2Y2, r.getX(), r.getY());
			if (pos == GeometryUtil.PointToLinePosition.Inside ||
				pos == GeometryUtil.PointToLinePosition.EqualsTheStartPoint) {
				// Do not compare to the endpoint pos == GeometryUtil.PointToLinePosition.EqualsTheEndPoint
				return r;
			}
		}
		return null;
	}
	
	static Point2D getEndPoint(Point2D lowerLeft, Point2D upperRight, int rectSide) {
		switch (rectSide % 4) {
		case 0:
			return new Point2D.Double(lowerLeft.getX(), upperRight.getY());
		case 1:
			return new Point2D.Double(upperRight.getX(), upperRight.getY());
		case 2:
			return new Point2D.Double(upperRight.getX(), lowerLeft.getY());
		case 3:
		default:
			return new Point2D.Double(lowerLeft.getX(), lowerLeft.getY());
		}
	}
	
	static Path2D pointList2Path(List<? extends Point2D> points) {
		if (points == null || points.isEmpty())
			return null;
		Path2D r = new Path2D.Double();
		Point2D p = points.get(0);
		r.moveTo(p.getX(), p.getY());
		for (int i = 1; i < points.size(); i++) {
			p = points.get(i);
			r.lineTo(p.getX(), p.getY());
		}
		r.closePath();
		return r;
	}
	
	public static ArrayList<Path2D> computeVoroni(MyDelaunay d, Rectangle2D voronoiExtent) {
		ArrayList<Path2D> r = new ArrayList<Path2D>();
		if (d.triangles.size() == 0)
			return r;

		Point2D lowerLeft = new Point2D.Double(voronoiExtent.getX(), voronoiExtent.getY());
		Point2D upperRight = new Point2D.Double(voronoiExtent.getMaxX(), voronoiExtent.getMaxY());
		for (Point2D p : d.points) {
			Triangle t = null;
			// Find first triangle contining the point
			for (Triangle i : d.triangles) {
				if (i.containsPoint(p)) {
					t = i;
					break;
				}
			}
			if (t == null)
				throw new Error();
			
			// Find the first non-edge tirangle that contains the point
			Triangle t1 = t;
			do {
				if (t1.c != null) {
					break;
				}
				t1 = getNextTriangle(t1, p);
			} while (t1 != t);
			if (t1.c == null)
				continue; // all triangles are edge triangles! maybe event return, or throw an exception
			t = t1;
			
			ArrayList<Point2D> pathPoints = new ArrayList<Point2D>();
			while (true) {
				if (t1.c == null) {
					Point2D intersect = null;
					int rectSide = 0;
					for (; rectSide < 4; rectSide++) {
						intersect = intersectWithRect(t1.a, t1.b, lowerLeft, upperRight, rectSide);
						if (intersect != null) {
							break;
						}
					}
					if (intersect == null) {
						throw new Error();
					}
					pathPoints.add(intersect);
					// get next triangle
					t1 = getNextTriangle(t1, p);
					if (t1.c != null)
						throw new Error();
					for (int i = 0; i < 4; i++) {
						int nextRectSide = (rectSide + i) % 4;
						intersect = intersectWithRect(t1.a, t1.b, lowerLeft, upperRight, nextRectSide);
						if (intersect != null) {
							pathPoints.add(intersect);
							break;
						}
						Point2D endp = getEndPoint(lowerLeft, upperRight, nextRectSide);
						pathPoints.add(endp);
					}
				} else {
					Point2D c = t1.getCircumCircle().center;
					pathPoints.add(c);
				}
				
				t1 = getNextTriangle(t1, p);
				if (t1 == t)
					break;
			}
			
			Path2D path = pointList2Path(pathPoints);
			r.add(path);
		}
		return r;
	}
}
