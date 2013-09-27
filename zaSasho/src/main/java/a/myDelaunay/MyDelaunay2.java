package a.myDelaunay;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.slavi.math.GeometryUtil;

/**
 * http://wwwpi6.fernuni-hagen.de/Geometrie-Labor/VoroGlide/
 * http://en.wikipedia.org/wiki/Delaunay_triangulation
 */
public abstract class MyDelaunay2 {

	public abstract int getPointId(Point2D p);
	
	public ArrayList<Point2D> points = new ArrayList<Point2D>();
	public ArrayList<Triangle> triangles = new ArrayList<Triangle>();
	Triangle firstT, lastT;
	public Triangle root;
	boolean allCollinear = true;
	Point2D.Double firstP, lastP;
	int pointsCount = 0;
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Points:    ").append(points.size()).append('\n');
		sb.append("Triangles: ").append(triangles.size()).append('\n');
		for (Triangle t : triangles) {
			sb.append(triangle2String(t));
			sb.append('\n');
		}
		return sb.toString();
	}
	
	public ArrayList<Triangle> getTriangles() {
		return triangles;
	}
	
	public void insertPoint(Point2D.Double p) {
		points.add(p);
		Triangle t = insertPointSimple_NEW(p);
		if (t == null)
			return;
		Triangle t1 = t;
/*		if (true) {
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

	
	static Triangle findTriangle(Triangle t, Point2D.Double p) {
		while (t != null) {
			switch (GeometryUtil.pointToLine(t.a, t.b, p)) {
			case GeometryUtil.PointToLinePosition.AfterTheEndPoint:
				if (t.c == null)
					return t;
				t = t.getBc();
				continue;
			case GeometryUtil.PointToLinePosition.PositivePlane:
				t = t.getAb();
				continue;
			case GeometryUtil.PointToLinePosition.BeforeTheStartPoint:
				if (t.c == null)
					return t;
				t = t.getCa();
				continue;

			case GeometryUtil.PointToLinePosition.Inside:
			case GeometryUtil.PointToLinePosition.NegativePlane:
			case GeometryUtil.PointToLinePosition.InvalidLine:
			default:
				break;
			}
			
			if (t.c == null) {
				// Half plane
				return t;
			}

			switch (GeometryUtil.pointToLine(t.b, t.c, p)) {
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
			case GeometryUtil.PointToLinePosition.NegativePlane:
			case GeometryUtil.PointToLinePosition.InvalidLine:
			default:
				break;
			}
			
			switch (GeometryUtil.pointToLine(t.c, t.a, p)) {
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
			case GeometryUtil.PointToLinePosition.NegativePlane:
			case GeometryUtil.PointToLinePosition.InvalidLine:
			default:
				break;
			}
			
			// Point is inside this triangle
			return t;
		}
		return null;
	}
	
	/**
	 * Extends Outside, i.e. increase the convex hull of all the points.
	 */
	Triangle extendOutside(Triangle t, Point2D.Double p) {
		// Triangle t is a "border" tirangle, i.e. t.c == null 
		if (GeometryUtil.pointToLine(t.a, t.b, p) == GeometryUtil.PointToLinePosition.Inside) {
			Triangle lt = new Triangle(t.a, p);
			lt.setAb(t.getAb());
			lt.setBc(t);
			lt.setCa(t.getCa());
			t.setCa(lt);
			// TODO:
			
			
			
			System.out.println("extend outside on edge " + getPointId(p));
			Triangle left = new Triangle(t.a, p);
			Triangle right = new Triangle(p, t.b);
			triangles.add(left);
			triangles.add(right);
			left.setAb(t);
			left.setBc(right);
			left.setCa(t.getCa());
			right.setAb(t);
			right.setBc(t.getBc());
			right.setCa(left);
			t.c = p;
			t.setBc(right);
			t.setCa(left);
			return t;
/*
			// original way
			Triangle t1 = new Triangle(t.a, t.b, p);
			Triangle right = new Triangle(p, t.b);
			triangles.add(t1);
			triangles.add(right);
			t.b = p;
			t1.setAb(t.getAb());
			t1.getAb().switchneighbors(t, t1);
			t1.setBc(right);
			right.setAb(t1);
			t1.setCa(t);
			t.setAb(t1);
			right.setBc(t.getBc());
			right.getBc().setCa(right);
			right.setCa(t);
			t.setBc(right);
			return t1;
 */
/*			
			// my old way
			Triangle t1 = new Triangle(t.a, t.b, p);
			Triangle right = new Triangle(p, t.b);
			triangles.add(t1);
			triangles.add(right);
			t1.setAb(t.getAb());
			t1.setBc(right);
			t1.setCa(t);
			t1.getAb().switchneighbors(t, t1);

			right.setAb(t1);
			right.setBc(t.getBc());
			right.setCa(t);
			right.getBc().setCa(right);
			
			t.b = p;
			t.setAb(t1);
			t.setBc(right);
			return t1;
 */
			
		} else {
			Triangle left = extendcounterclock(t, p);
			Triangle right = extendclock(t, p);
			left.setBc(right);
			right.setCa(left);
			return right.getAb();
		}
	}

	Triangle extendInside(Triangle t, Point2D.Double p) {
		Triangle t1 = treatDegeneracyInside(t, p);
		if (t1 != null) {
			return t1;
		} else {
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
	}

	Triangle treatDegeneracyInside(Triangle t, Point2D.Double p) {
		if (t.getAb().c == null && GeometryUtil.pointToLine(t.b, t.a, p) == GeometryUtil.PointToLinePosition.Inside)
			return extendOutside(t.getAb(), p);
		if (t.getBc().c == null && GeometryUtil.pointToLine(t.c, t.b, p) == GeometryUtil.PointToLinePosition.Inside)
			return extendOutside(t.getBc(), p);
		if (t.getCa().c == null && GeometryUtil.pointToLine(t.a, t.c, p) == GeometryUtil.PointToLinePosition.Inside)
			return extendOutside(t.getCa(), p);
		else
			return null;
	}

	Triangle extendclock(Triangle t, Point2D.Double p) {
/*		while (true) {
			Triangle oldRightT = t.getBc();
			if (GeometryUtil.pointToLine(oldRightT.a, oldRightT.b, p) >= GeometryUtil.PointToLinePosition.PositivePlane) {
				Triangle newRightT = new Triangle(p, t.b);
				triangles.add(newRightT);
				t.c = p;
				newRightT.setAb(t);
				t.setBc(newRightT);
				newRightT.setBc(oldRightT);
				oldRightT.setCa(newRightT);
				return newRightT;
			}
			t = oldRightT;
			oldRightT = t.getBc();
		}
*/
		Triangle rightT = t.getBc();
		if (GeometryUtil.pointToLine(rightT.a, rightT.b, p) >= GeometryUtil.PointToLinePosition.PositivePlane) {
			dumpTriangle(t, "T");
			System.out.println("befre");
			dumpTriangles();

			t.c = p;
			Triangle newT = new Triangle(p, t.b);
			triangles.add(newT);
			newT.setAb(t);
			t.setBc(newT);
			newT.setBc(rightT);
			rightT.setCa(newT);

			System.out.println("after");
			dumpTriangles();
			return newT;
		} else {
			t.c = p;
			t = extendclock(rightT, p);
			return t;
		}
	}
	
	Triangle extendcounterclock(Triangle t, Point2D.Double p) {
		Triangle leftT = t.getCa();
		if (GeometryUtil.pointToLine(leftT.a, leftT.b, p) >= GeometryUtil.PointToLinePosition.PositivePlane) {
			t.c = p;
			Triangle newT = new Triangle(t.a, p);
			triangles.add(newT);
			newT.setAb(t);
			t.setCa(newT);
			newT.setCa(leftT);
			leftT.setBc(newT);
			return newT;
		} else {
			System.out.println("qweqwe");
			dumpTriangle(t, "T");
			System.out.println("befre 0");
			dumpTriangles();
			t.c = p;
			t = extendcounterclock(leftT, p);
			return t;
		}
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
		String isOk = MyDelaunay.isTriangleOk(t) ? "  " : "* ";
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
				"\tab=" + ab +
				"\tbc=" + bc +
				"\tca=" + ca;
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

	Triangle insertPointSimple_NEW(Point2D.Double p) {
		pointsCount++;
		if (pointsCount == 1) {
			firstP = p;
			return null;
		}
		if (pointsCount == 2) {
			startTriangulation(p);
			dumpIfBadTrianglesExist();
			return null;
		}
		Triangle t = findTriangle(root, p);
		System.out.println("Selected triangle id=" + triangles.indexOf(t));
		if (t.c == null) {
			// Half plane
			extendOutside(t, p);
		} else {
			extendInside(t, p);
		}
		dumpIfBadTrianglesExist();
		return t;
	}
	
	Triangle insertPointSimple(Point2D.Double p) {
		pointsCount++;
		if (!allCollinear) {
			Triangle t = findTriangle(root, p);
			System.out.println("Selected triangle id=" + triangles.indexOf(t));
			if (t.c == null) {
				// Half plane
				extendOutside(t, p);
			} else {
				extendInside(t, p);
			}
			return t;
		}
		if (pointsCount == 1) {
			firstP = p;
			return null;
		}
		if (pointsCount == 2) {
			startTriangulation(p);
			return null;
		}
		switch (GeometryUtil.pointToLine(firstP, lastP, p)) {
		case GeometryUtil.PointToLinePosition.NegativePlane:
			extendOutside(firstT.getAb(), p);
			allCollinear = false;
			break;

		case GeometryUtil.PointToLinePosition.PositivePlane:
			extendOutside(firstT, p);
			allCollinear = false;
			break;

		case GeometryUtil.PointToLinePosition.Inside: {
			Triangle leftT;
			for (leftT = firstT; isGreater(p, leftT.a); leftT = leftT.getCa())
				;
			Triangle newT = new Triangle(p, leftT.b);
			Triangle newMirrorT = new Triangle(leftT.b, p);
			triangles.add(newT);
			triangles.add(newMirrorT);
			Triangle mirrorLeft = leftT.getAb();
			leftT.b = p;
			mirrorLeft.a = p;
			Triangle oldRight = leftT.getBc();
			Triangle oldMirrorLeft = mirrorLeft.getCa();
			
			newT.setAb(newMirrorT);
			newMirrorT.setAb(newT);
			
			newT.setBc(oldRight);
			oldRight.setCa(newT);
			
			newT.setCa(leftT);
			leftT.setBc(newT);
			
			newMirrorT.setCa(oldMirrorLeft);
			oldMirrorLeft.setBc(newMirrorT);
			
			newMirrorT.setBc(mirrorLeft);
			mirrorLeft.setCa(newMirrorT);
			if (firstT == leftT) {
				firstT = newT;
			}
			break;
		}
		case GeometryUtil.PointToLinePosition.BeforeTheStartPoint: {
			Triangle newT = new Triangle(p, firstP);
			Triangle newMirrorT = new Triangle(firstP, p);
			triangles.add(newT);
			triangles.add(newMirrorT);
			
			Triangle firstMirror = firstT.getAb();
			
			newT.setAb(newMirrorT);
			newMirrorT.setAb(newT);
			
			newT.setBc(firstT);
			newMirrorT.setCa(firstMirror);
			
			newT.setCa(newMirrorT);
			newMirrorT.setBc(newT);

			firstT.setCa(newT);
			firstMirror.setBc(newMirrorT);

			firstT = newT;
			firstP = p;
			break;
		}
		case GeometryUtil.PointToLinePosition.AfterTheEndPoint: {
			Triangle newT = new Triangle(lastP, p);
			Triangle newMirror = new Triangle(p, lastP);
			triangles.add(newT);
			triangles.add(newMirror);
			
			Triangle lastMirror = lastT.getAb();
			
			newT.setAb(newMirror);
			newMirror.setAb(newT);
			
			newT.setBc(newMirror);
			newMirror.setCa(newT);
			
			newT.setCa(lastT);
			newMirror.setBc(lastMirror);
			
			lastT.setCa(newT);
			lastMirror.setBc(newMirror);
			
			lastT = newMirror;
			lastP = p;
			break;
		}
		}
		return null;
	}
	
	void startTriangulation(Point2D.Double p) {
		Point2D.Double b;
		Point2D.Double a;
		if (isLess(firstP, p)) {
			a = firstP;
			b = p;
		} else {
			a = p;
			b = firstP;
		}
		root = lastT = firstT = new Triangle(a, b);
		Triangle mirrorT = new Triangle(b, a);
		triangles.add(firstT);
		triangles.add(mirrorT);
		
		firstT.setAb(mirrorT);
		firstT.setBc(mirrorT);
		firstT.setCa(mirrorT);
		
		mirrorT.setAb(firstT);
		mirrorT.setBc(firstT);
		mirrorT.setCa(firstT);
		
		firstP = b;
		lastP = a;
	}
}
