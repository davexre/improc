package a.myDelaunay;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import a.myDelaunay.Triangle.TriangleRotation;

import com.slavi.math.GeometryUtil;

/**
 * http://wwwpi6.fernuni-hagen.de/Geometrie-Labor/VoroGlide/
 * http://en.wikipedia.org/wiki/Delaunay_triangulation
 */
public abstract class MyDelaunay {

	public abstract int getPointId(Point2D p);
	
	public ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
	public ArrayList<Triangle> triangles = new ArrayList<Triangle>();
	public Triangle root;

	boolean allCollinear = true;
	
	public ArrayList<Triangle> getTriangles() {
		return triangles;
	}
	
	public void insertPoint(Point2D.Double p) {
		points.add(p);
/*		if (points.size() == 1) {
			return;
		}
		if (points.size() == 2) {
			Point2D.Double a = points.get(0);
			root = new Triangle(a, p);
			Triangle mirrorT = new Triangle(p, a);
			triangles.add(root);
			triangles.add(mirrorT);
			
			root.setAb(mirrorT);
			root.setBc(mirrorT);
			root.setCa(mirrorT);
			
			mirrorT.setAb(root);
			mirrorT.setBc(root);
			mirrorT.setCa(root);

			return;
		}
*/
		if (points.size() < 3)
			return;
		if (points.size() == 3) {
			Point2D.Double a = points.get(0);
			Point2D.Double b = points.get(1);
			Point2D.Double c = points.get(2);
			Point2D.Double tmp;
			boolean colinear = true;
			
			switch (GeometryUtil.pointToLine(a, b, c)) {
			case GeometryUtil.PointToLinePosition.EqualsTheStartPoint:
			case GeometryUtil.PointToLinePosition.EqualsTheEndPoint:
			case GeometryUtil.PointToLinePosition.InvalidLine:
				throw new RuntimeException("Duplicated point");

			case GeometryUtil.PointToLinePosition.NegativePlane:
				colinear = false;
				break;
			case GeometryUtil.PointToLinePosition.PositivePlane:
				colinear = false;
				// break; // NO break here
			case GeometryUtil.PointToLinePosition.Inside:
				tmp = b;
				b = c;
				c = tmp;
				break;
			case GeometryUtil.PointToLinePosition.BeforeTheStartPoint:
				tmp = a;
				a = c;
				c = b;
				b = tmp;
				break;
			case GeometryUtil.PointToLinePosition.AfterTheEndPoint:
				break;
			}
			
			if (colinear) {
				System.out.println("3 points in a line");
				Triangle left = new Triangle(a, b);
				Triangle mirrorLeft = new Triangle(b, a);
				Triangle right = new Triangle(b, c);
				Triangle mirrorRight = new Triangle(c, b);
				triangles.add(left);
				triangles.add(mirrorLeft);
				triangles.add(right);
				triangles.add(mirrorRight);
				
				left.setAb(mirrorLeft);
				left.setBc(right);
				left.setCa(mirrorLeft);
				
				right.setAb(mirrorRight);
				right.setBc(mirrorRight);
				right.setCa(left);
				
				mirrorLeft.setAb(left);
				mirrorLeft.setBc(left);
				mirrorLeft.setCa(mirrorRight);
				
				mirrorRight.setAb(right);
				mirrorRight.setBc(mirrorLeft);
				mirrorRight.setCa(right);
				
				root = left;
			} else {
				System.out.println("3 points in general possition");
				Triangle t = new Triangle(a, b, c);
				Triangle mirror = new Triangle(b, a);
				Triangle left = new Triangle(a, c);
				Triangle right = new Triangle(c, b);
				
				triangles.add(t);
				triangles.add(mirror);
				triangles.add(left);
				triangles.add(right);
				
				t.setAb(mirror);
				t.setBc(right);
				t.setCa(left);
				
				mirror.setAb(t);
				mirror.setBc(left);
				mirror.setCa(right);
				
				left.setAb(t);
				left.setBc(right);
				left.setCa(mirror);
				
				right.setAb(t);
				right.setBc(mirror);
				right.setCa(left);
				
				root = t;
			}
			return;
		}
		
		Triangle t = doInsert(root, p);
		System.out.println("Inserted triangle id=" + triangles.indexOf(t));
		if (t == null) {
			return;
		}
/*
		Triangle t1 = t;
		if (true) {
			do {
				flip_NEW(t1);
				t1 = t1.getCa();
			} while (t1 != t && t1.c != null);
		} else {
			do {
				flip(t1, 1);
				t1 = t1.getCa();
			} while (t1 != t && t1.c != null);
		}*/
	}
	
	public static boolean isLess(Point2D.Double a, Point2D.Double b) {
		return a.x < b.x || a.x == b.x && a.y < b.y;
	}

	public static boolean isGreater(Point2D.Double a, Point2D.Double b) {
		return a.x > b.x || a.x == b.x && a.y > b.y;
	}

	
	private Triangle doInsert(Triangle t, Point2D.Double p) {
		while (t != null) {
			int abp = GeometryUtil.pointToLine(t.a, t.b, p);
			switch (abp) {
			case GeometryUtil.PointToLinePosition.EqualsTheStartPoint:
			case GeometryUtil.PointToLinePosition.EqualsTheEndPoint:
			case GeometryUtil.PointToLinePosition.InvalidLine:
				// TODO: Do nothing. Point should be ignored.
				return null;
			case GeometryUtil.PointToLinePosition.AfterTheEndPoint:
				if (t.c == null)
					break;
				t = t.getBc();
				continue;
			case GeometryUtil.PointToLinePosition.PositivePlane:
				t = t.getAb();
				continue;
			case GeometryUtil.PointToLinePosition.BeforeTheStartPoint:
				if (t.c == null)
					break;
				t = t.getCa();
				continue;

			case GeometryUtil.PointToLinePosition.Inside:
				return splitOnEdgeAB(t, p);
			case GeometryUtil.PointToLinePosition.NegativePlane:
			default:
				break;
			}
			
			if (t.c == null) {
				// Half plane
				return extendOutside(t, p);
			}

			int bcp = GeometryUtil.pointToLine(t.b, t.c, p);
			switch (bcp) {
			case GeometryUtil.PointToLinePosition.EqualsTheStartPoint:
			case GeometryUtil.PointToLinePosition.EqualsTheEndPoint:
			case GeometryUtil.PointToLinePosition.InvalidLine:
				// TODO: Do nothing. Point should be ignored.
				return null;
			case GeometryUtil.PointToLinePosition.AfterTheEndPoint:
				t = t.getCa();
				continue;
			case GeometryUtil.PointToLinePosition.PositivePlane:
				t = t.getBc();
				continue;
			case GeometryUtil.PointToLinePosition.BeforeTheStartPoint:
				t = t.getAb();
				continue;

			case GeometryUtil.PointToLinePosition.Inside:
				t.rotateCounterClockWise();
				splitOnEdgeAB(t, p);
				break;
				
			case GeometryUtil.PointToLinePosition.NegativePlane:
			default:
				break;
			}
			
			int cap = GeometryUtil.pointToLine(t.c, t.a, p);
			switch (cap) {
			case GeometryUtil.PointToLinePosition.EqualsTheStartPoint:
			case GeometryUtil.PointToLinePosition.EqualsTheEndPoint:
			case GeometryUtil.PointToLinePosition.InvalidLine:
				// TODO: Do nothing. Point should be ignored.
				return null;
			case GeometryUtil.PointToLinePosition.AfterTheEndPoint:
				t = t.getAb();
				continue;
			case GeometryUtil.PointToLinePosition.PositivePlane:
				t = t.getCa();
				continue;
			case GeometryUtil.PointToLinePosition.BeforeTheStartPoint:
				t = t.getBc();
				continue;

			case GeometryUtil.PointToLinePosition.Inside:
				t.rotateClockWise();
				splitOnEdgeAB(t, p);
				break;
				
			case GeometryUtil.PointToLinePosition.NegativePlane:
			default:
				break;
			}
			
			// Point is inside this triangle
			Triangle left = new Triangle(t.c, t.a, p);
			Triangle right = new Triangle(t.b, t.c, p);
			triangles.add(left);
			triangles.add(right);
			t.c = p;
			left.setAb(t.getCa());
			left.setBc(t);
			left.setCa(right);
			right.setAb(t.getBc());
			right.setBc(left);
			right.setCa(t);
			left.getAb().switchneighbors(t, left);
			right.getAb().switchneighbors(t, right);
			t.setBc(right);
			t.setCa(left);
			return t;
		}
		return null;
	}
	
	private Triangle splitOnEdgeAB(Triangle left, Point2D.Double p) {
		if ((left.c != null) && 
			(GeometryUtil.pointToLine(left.a, p, left.c) >= GeometryUtil.PointToLinePosition.PositivePlane))
			throw new RuntimeException("WTF?");

		Triangle mirrorRight = left.getAb();
		TriangleRotation mirrorRightRot = mirrorRight.rotateAndMatchA(left.b);
		
		Triangle tmp;
		Triangle right = new Triangle(p, left.b);
		Triangle mirrorLeft = new Triangle(p, left.a);
		triangles.add(right);
		triangles.add(mirrorLeft);

		right.c = left.c;
		right.setAb(mirrorRight);
		right.setBc(tmp = left.getBc());
		tmp.switchneighbors(left, right);
		right.setCa(left);

		mirrorLeft.c = mirrorRight.c;
		mirrorLeft.setAb(left);
		mirrorLeft.setBc(tmp = mirrorRight.getBc());
		tmp.switchneighbors(mirrorRight, mirrorLeft);
		mirrorLeft.setCa(mirrorRight);

		mirrorRight.b = p;
		mirrorRight.setAb(right);
		mirrorRight.setBc(mirrorLeft);
		mirrorRight.unrotate(mirrorRightRot);
		
		left.b = p;
		left.setAb(mirrorLeft);
		left.setBc(right);

		return right;
	}
	
	/**
	 * Extends Outside, i.e. increase the convex hull of all the points.
	 */
	Triangle extendOutside(Triangle t, Point2D.Double p) {
		// Triangle t is a "border" tirangle, i.e. t.c == null 
		if (GeometryUtil.pointToLine(t.a, t.b, p) == GeometryUtil.PointToLinePosition.Inside) {
			// already handled
			throw new RuntimeException("WTF?");
		} else {
			Triangle leftT = new Triangle();
			Triangle rightT = new Triangle();
			triangles.add(leftT);
			triangles.add(rightT);

			Triangle curT = t;
			while (true) {
				// Go Left (counter clock wise)
				curT.c = p;
				Triangle nextT = curT.getCa();
				if (GeometryUtil.pointToLine(nextT.a, nextT.b, p) >= GeometryUtil.PointToLinePosition.PositivePlane) {
					leftT.a = curT.a;
					leftT.b = p;
					leftT.setAb(curT);
					leftT.setBc(rightT);
					leftT.setCa(nextT);

					nextT.setBc(leftT);
					curT.setCa(leftT);
					break;
				}
				curT = nextT;
			}

			curT = t;
			while (true) {
				// Go Right (clock wise)
				curT.c = p;
				Triangle nextT = curT.getBc();
				if (GeometryUtil.pointToLine(nextT.a, nextT.b, p) >= GeometryUtil.PointToLinePosition.PositivePlane) {
					rightT.a = p;
					rightT.b = curT.b;
					rightT.setAb(curT);
					rightT.setBc(nextT);
					rightT.setCa(leftT);

					nextT.setCa(rightT);
					curT.setBc(rightT);
					break;
				}
				curT = nextT;
			}
			return rightT;
		}
	}

	void flipNew2(Triangle t) {
		Triangle ab = t.getAb();
		Triangle bc = t.getBc();
		Triangle ca = t.getCa();
		
		if ((t.a == null) ||
			(t.b == null) ||
			(t.c == null))
			return;
		if (t.getAb().getCircumCircle().isPointInside(t.c)) {
			flipAB(t);
		}
		if (t.getBc().getCircumCircle().isPointInside(t.a)) {
			
		}
		if (t.getCa().getCircumCircle().isPointInside(t.b)) {
			
		}
	}
	
	void flipAB(Triangle t) {
		Triangle mirror = t.getAb();
		if ((t.a == null) ||
			(t.b == null) ||
			(t.c == null) ||
			(mirror.a == null) ||
			(mirror.b == null) ||
			(mirror.c == null) ||
			(!mirror.getCircumCircle().isPointInside(t.c)))
			return;
		mirror.rotateAndMatchA(t.b);
		Triangle tmp;
		t.b = mirror.c;
		mirror.a = t.c;

		t.setAb(tmp = mirror.getBc());
		tmp.switchneighbors(mirror, t);
		
		mirror.setAb(tmp = t.getBc());
		tmp.switchneighbors(tmp, mirror);
		
		t.setBc(mirror);
		mirror.setBc(t);
	}
	
	void flip_NEW(Triangle t) {
		Triangle t1 = t.getAb();
		if ((t1.c == null) || !t1.getCircumCircle().isPointInside(t.c))
			return;

//		System.out.println("Before");
//		dumpTriangle(t, "T ");
//		dumpTriangle(t1, "T1");

		if (t.a == t1.a) {
			t.a = t1.b;
			t1.c = t.c;
			t.setAb(t1.getBc());
			t1.setCa(t.getCa());
			t.setCa(t1);
			t1.setBc(t);
		} else if (t.a == t1.b) {
			t.a = t1.c;
			t1.a = t.c;
			t.setAb(t1.getCa());
			t1.setAb(t.getCa());
			t.setCa(t1);
			t1.setCa(t);
		} else if (t.a == t1.c) {
			t.a = t1.a;
			t1.b = t.c;
			t.setAb(t1.getAb());
			t1.setBc(t.getCa());
			t.setCa(t1);
			t1.setAb(t);
		} else {
			throw new Error("Error in flip");
//			System.out.println("Error in flip.");
//			return;
		}

//		System.out.println("After");
//		dumpTriangle(t, "T ");
//		dumpTriangle(t1, "T1");
//		System.out.println();

		flip_NEW(t);
		flip_NEW(t1);
	}

	static boolean containsPoint(Triangle t, Point2D.Double p) {
		return ((t != null) && (p != null) && (
				(t.a == p) ||
				(t.b == p) ||
				(t.c == p)));
	}
	
	public void dumpIfBadTrianglesExist() {
		boolean badExist = false;
		for (Triangle t : triangles) {
			if (!isTriangleOk(t)) {
				badExist = true;
				break;
			}
		}
		if (badExist)
			System.out.println("\nBAD triangles exist");
		else
			System.out.println("\nNo bad triangles");
		dumpTriangles();
	}

	public void dumpTriangles() {
		for (Triangle t : triangles) {
			System.out.println(triangle2String(t));
		}
	}

	public void dumpTriangle(Triangle t, String name) {
		System.out.println(name);
		System.out.println(triangle2String(t));
	}
	
	public String triangle2String(Triangle t) {
		boolean abOk = containsPoint(t.getAb(), t.a) && containsPoint(t.getAb(), t.b);
		boolean bcOk = containsPoint(t.getBc(), t.b) && (t.c == null ? true : containsPoint(t.getBc(), t.c));
		boolean caOk = containsPoint(t.getCa(), t.a) && (t.c == null ? true : containsPoint(t.getCa(), t.c));
		
		String isOk = (abOk && bcOk && caOk) ? "  " : "* ";
		String id = Integer.toString(triangles.indexOf(t));
		String a = Integer.toString(getPointId(t.a));
		String b = Integer.toString(getPointId(t.b));
		String c = t.c == null ? "null" : Integer.toString(getPointId(t.c));
		String ab = Integer.toString(triangles.indexOf(t.getAb()));
		String bc = c == null ? " null" : Integer.toString(triangles.indexOf(t.getBc()));
		String ca = c == null ? " null" : Integer.toString(triangles.indexOf(t.getCa()));
		return isOk +
				"id=" + id + 
				"\ta=" + a + 
				"\tb=" + b + 
				"\tc=" + c +
				"\tab=" + ab + (abOk ? "" : "*") +
				"\tbc=" + bc + (bcOk ? "" : "*") +
				"\tca=" + ca + (caOk ? "" : "*");
	}
	
	public static boolean isTriangleOk(Triangle t) {
		if (
			containsPoint(t.getAb(), t.a) &&
			containsPoint(t.getAb(), t.b) &&
			containsPoint(t.getBc(), t.b) &&
			containsPoint(t.getCa(), t.a)) {
			if (t.c == null) {
				if (t.getBc().c == null && 
					t.getCa().c == null)
					return true;
				return false;
			}
			if (containsPoint(t.getBc(), t.c) &&
				containsPoint(t.getCa(), t.c))
				return true;
		}
		return false;
	}
	
	void flip(Triangle t, int recurseCount) {
		if (recurseCount > 1)
			System.out.println("WTF?!? " + recurseCount);
		Triangle t1 = t.getAb();
		if ((t1.c == null) || !t1.getCircumCircle().contains(t.c))
			return;
		System.out.println("Before");
		dumpTriangle(t, "T ");
		dumpTriangle(t1, "T1");
		Triangle t2;
		if (t.a == t1.a) {
			t2 = new Triangle(t1.b, t.b, t.c);
			triangles.add(t2);
			t2.setAb(t1.getBc());
			t.setAb(t1.getAb());
		} else if (t.a == t1.b) {
			t2 = new Triangle(t1.c, t.b, t.c);
			triangles.add(t2);
			t2.setAb(t1.getCa());
			t.setAb(t1.getBc());
		} else if (t.a == t1.c) {
			t2 = new Triangle(t1.a, t.b, t.c);
			triangles.add(t2);
			t2.setAb(t1.getAb());
			t.setAb(t1.getCa());
		} else {
			throw new Error("Error in flip");
//			System.out.println("Error in flip.");
//			return;
		}
		t2.setBc(t.getBc());
		t2.getAb().switchneighbors(t1, t2);
		t2.getBc().switchneighbors(t, t2);
		t.setBc(t2);
		t2.setCa(t);
		t.b = t2.a;
		t.getAb().switchneighbors(t1, t);

		System.out.println("After");
		dumpTriangle(t, "T ");
		dumpTriangle(t1, "T1");
		dumpTriangle(t2, "T2");
		System.out.println();
		
		flip(t, recurseCount + 1);
		flip(t2, recurseCount + 1);
	}
}
