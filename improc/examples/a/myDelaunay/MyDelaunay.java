package a.myDelaunay;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;

import com.slavi.math.GeometryUtil;

/**
 * http://wwwpi6.fernuni-hagen.de/Geometrie-Labor/VoroGlide/
 * http://en.wikipedia.org/wiki/Delaunay_triangulation
 */
public abstract class MyDelaunay {

	public abstract int getPointId(Point2D p);
	
	public ArrayList<Triangle> triangles = new ArrayList<Triangle>();
	Triangle firstT, lastT;
	public Triangle root;
	boolean allCollinear = true;
	Point2D.Double firstP, lastP;
	int pointsCount = 0;
	
	void recursiveAddTriangle(Triangle t, HashSet<Triangle> r) {
		if (t == null)
			return;
		if (r.contains(t))
			return;
		r.add(t);
		recursiveAddTriangle(t.getAb(), r);
		recursiveAddTriangle(t.getBc(), r);
		recursiveAddTriangle(t.getCa(), r);
	}
	
	public HashSet<Triangle> getTriangles() {
		HashSet<Triangle> r = new HashSet<Triangle>();
		if (root == null) {
			System.out.println("All colinear");
			recursiveAddTriangle(firstT, r);
		} else {
			recursiveAddTriangle(root, r);
		}
		return r;
	}
	
	public void insertPoint(Point2D.Double p) {
		Triangle t = insertPointSimple(p);
		if (t == null)
			return;
		Triangle t1 = t;
		if (true) {
			flip_NEW(t1);
		} else {
			do {
				flip(t1, 1);
				t1 = t1.getCa();
			} while (t1 != t && t1.c != null);
		}
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
			System.out.println("extend outside on edge " + getPointId(p));
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
/*
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
			
		} else {
			Triangle t2 = extendcounterclock(t, p);
			Triangle t4 = extendclock(t, p);
			t2.setBc(t4);
			t4.setCa(t2);
			return t4.getAb();
		}
	}

	Triangle extendInside(Triangle t, Point2D.Double p) {
		Triangle t1 = treatDegeneracyInside(t, p);
		if (t1 != null) {
			return t1;
		} else {
			Triangle t2 = new Triangle(t.c, t.a, p);
			Triangle t3 = new Triangle(t.b, t.c, p);
			triangles.add(t2);
			triangles.add(t3);
			t.c = p;
			t2.setAb(t.getCa());
			t2.setBc(t);
			t2.setCa(t3);
			t3.setAb(t.getBc());
			t3.setBc(t2);
			t3.setCa(t);
			t2.getAb().switchneighbors(t, t2);
			t3.getAb().switchneighbors(t, t3);
			t.setBc(t3);
			t.setCa(t2);
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
		int oldtc = getPointId(t.c);
		t.c = p;
		Triangle rightT = t.getBc();
		if (GeometryUtil.pointToLine(rightT.a, rightT.b, p) >= GeometryUtil.PointToLinePosition.PositivePlane) {
			Triangle newT = new Triangle(p, t.b);
			triangles.add(newT);
			newT.setAb(t);
			t.setBc(newT);
			newT.setBc(rightT);
			rightT.setCa(newT);
			return newT;
		} else {
			System.out.println("extend clock " + getPointId(p) + " old=" + oldtc);
			return extendclock(rightT, p);
		}
	}
	
	Triangle extendcounterclock(Triangle t, Point2D.Double p) {
		t.c = p;
		Triangle leftT = t.getCa();
		if (GeometryUtil.pointToLine(leftT.a, leftT.b, p) >= GeometryUtil.PointToLinePosition.PositivePlane) {
			Triangle newT = new Triangle(t.a, p);
			triangles.add(newT);
			newT.setAb(t);
			t.setCa(newT);
			newT.setCa(leftT);
			leftT.setBc(newT);
			return newT;
		} else {
			return extendcounterclock(leftT, p);
		}
	}

	void flip_NEW(Triangle t) {
		Triangle t1 = t.getAb();
		if ((t1.c == null) || !t1.getCircumCircle().isPointInside(t.c))
			return;
		System.out.println("Before");
		dumpTriangle(t, "T ");
		dumpTriangle(t1, "T1");
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
			System.out.println("Error in flip.");
			return;
		}

		System.out.println("After");
		dumpTriangle(t, "T ");
		dumpTriangle(t1, "T1");
		System.out.println();

		flip_NEW(t);
		flip_NEW(t1);
	}

	String triangle2String(Triangle t) {
		return	getPointId(t.a) + ":" + 
				getPointId(t.b) + ":" + 
				getPointId(t.c);
	}
	
	public void dumpTriangle(Triangle t, String name) {
		System.out.println(name + "(" + triangle2String(t) + ")" +
				" ab(" + triangle2String(t.getAb()) + ") " +
				" bc(" + triangle2String(t.getBc()) + ") " +
				" ca(" + triangle2String(t.getCa()) + ")");
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
			System.out.println("Error in flip.");
			return;
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

	Triangle insertPointSimple(Point2D.Double p) {
		pointsCount++;
		if (!allCollinear) {
			Triangle t = findTriangle(root, p);
			if (t.c == null) {
				// Half plane
				root = extendOutside(t, p);
			} else {
				root = extendInside(t, p);
			}
			return root;
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
			root = extendOutside(firstT.getAb(), p);
			allCollinear = false;
			break;

		case GeometryUtil.PointToLinePosition.PositivePlane:
			root = extendOutside(firstT, p);
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
		lastT = firstT = new Triangle(a, b);
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
