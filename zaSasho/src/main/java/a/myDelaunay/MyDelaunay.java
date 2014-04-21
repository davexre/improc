package a.myDelaunay;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

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
	public HashSet<Triangle> trianglesToCheck = new HashSet<Triangle>();
	public Triangle root;

	boolean allCollinear = true;
	
	public ArrayList<Triangle> getTriangles() {
		return triangles;
	}
	
	public void insertPoint(Point2D.Double p) {
		points.add(p);
		if (points.size() == 1) {
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

		doInsert(root, p);
		checkAndFlip();
		
		boolean isok = isTopologyOk();
		if (!isok) {
			dumpTriangles("bad topology");
			throw new Error();
		}
	}
	
	private void doInsert(Triangle t, Point2D.Double p) {
		while (true) {
//			System.out.println("doInsert-> " + triangle2String(t));
			int abp = GeometryUtil.pointToLine(t.a, t.b, p);
			switch (abp) {
			case GeometryUtil.PointToLinePosition.EqualsTheStartPoint:
			case GeometryUtil.PointToLinePosition.EqualsTheEndPoint:
			case GeometryUtil.PointToLinePosition.InvalidLine:
				throw new RuntimeException("Duplicated point");
			case GeometryUtil.PointToLinePosition.AfterTheEndPoint:
				if (t.c == null) {
					Triangle next = t.getBc();
					int nextABP = GeometryUtil.pointToLine(next.a, next.b, p);
					if ((nextABP == GeometryUtil.PointToLinePosition.BeforeTheStartPoint) ||
						(nextABP == GeometryUtil.PointToLinePosition.PositivePlane)) {
						break;
					}
				}
				t = t.getBc();
				continue;
			case GeometryUtil.PointToLinePosition.PositivePlane:
				t = t.getAb();
				continue;
			case GeometryUtil.PointToLinePosition.BeforeTheStartPoint:
				t = t.getCa();
				continue;

			case GeometryUtil.PointToLinePosition.Inside:
				splitOnEdgeAB(t, p);
				return;
			case GeometryUtil.PointToLinePosition.NegativePlane:
			default:
				break;
			}
			
			if (t.c == null) {
				// Half plane
				extendOutside(t, p);
				return;
			}

			int bcp = GeometryUtil.pointToLine(t.b, t.c, p);
			switch (bcp) {
			case GeometryUtil.PointToLinePosition.EqualsTheStartPoint:
			case GeometryUtil.PointToLinePosition.EqualsTheEndPoint:
			case GeometryUtil.PointToLinePosition.InvalidLine:
				throw new RuntimeException("Duplicated point");
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
				return;
			case GeometryUtil.PointToLinePosition.NegativePlane:
			default:
				break;
			}
			
			int cap = GeometryUtil.pointToLine(t.c, t.a, p);
			switch (cap) {
			case GeometryUtil.PointToLinePosition.EqualsTheStartPoint:
			case GeometryUtil.PointToLinePosition.EqualsTheEndPoint:
			case GeometryUtil.PointToLinePosition.InvalidLine:
				throw new RuntimeException("Duplicated point");
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
				return;
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
			t.setBc(right);
			t.setCa(left);
			
			Triangle tmp = left.getAb();
			TriangleRotation trot = tmp.rotateAndMatchA(left.b);
			tmp.setAb(left);
			tmp.unrotate(trot);
			tmp = right.getAb();
			trot = tmp.rotateAndMatchA(right.b);
			tmp.setAb(right);
			tmp.unrotate(trot);
			
			trianglesToCheck.add(t);
			trianglesToCheck.add(left);
			trianglesToCheck.add(right);
			return;
		}
	}
	
	private void splitOnEdgeAB(Triangle left, Point2D.Double p) {
		if ((left.c != null) && 
			(GeometryUtil.pointToLine(left.a, p, left.c) >= GeometryUtil.PointToLinePosition.PositivePlane))
			throw new RuntimeException("WTF?");

		Triangle tmp;
		Triangle right = new Triangle(p, left.b);
		Triangle mirrorLeft = new Triangle(p, left.a);
		triangles.add(right);
		triangles.add(mirrorLeft);
		Triangle mirrorRight = left.getAb();

		right.c = left.c;
		right.setAb(mirrorRight);
		tmp = left.getBc();
		TriangleRotation trot = tmp.rotateAndMatchA(left.b);
		tmp.setCa(right);
		tmp.unrotate(trot);
		right.setBc(tmp);
		right.setCa(left);

		TriangleRotation mirrorRightRot = mirrorRight.rotateAndMatchA(left.b);
		
		mirrorLeft.c = mirrorRight.c;
		mirrorLeft.setAb(left);
		tmp = mirrorRight.getBc();
		trot = tmp.rotateAndMatchA(mirrorRight.b);
		tmp.setCa(mirrorLeft);
		tmp.unrotate(trot);
		mirrorLeft.setBc(tmp);
		mirrorLeft.setCa(mirrorRight);

		mirrorRight.b = p;
		mirrorRight.setAb(right);
		mirrorRight.setBc(mirrorLeft);
		mirrorRight.unrotate(mirrorRightRot);
		
		left.b = p;
		left.setAb(mirrorLeft);
		left.setBc(right);

		trianglesToCheck.add(left);
		trianglesToCheck.add(right);
		trianglesToCheck.add(mirrorLeft);
		trianglesToCheck.add(mirrorRight);
	}

	Triangle extendClockWise(Triangle t, Point2D.Double p) {
		Triangle rightT = new Triangle();
		triangles.add(rightT);
		Triangle curT = t;
		int abp = GeometryUtil.pointToLine(curT.a, curT.b, p);
		while (true) {
			// Go Right (clock wise)
			if (abp == GeometryUtil.PointToLinePosition.NegativePlane) {
				curT.c = p;
				trianglesToCheck.add(curT);
			}
			Triangle nextT = curT.getBc();
			abp = GeometryUtil.pointToLine(nextT.a, nextT.b, p);
			switch (abp) {
			case GeometryUtil.PointToLinePosition.PositivePlane:
			case GeometryUtil.PointToLinePosition.BeforeTheStartPoint:
				rightT.a = p;
				rightT.b = curT.b;
				rightT.setAb(curT);
				rightT.setBc(nextT);
				rightT.setCa(t.getCa()); // leftT

				nextT.setCa(rightT);
				curT.setBc(rightT);
				trianglesToCheck.add(rightT);
				trianglesToCheck.add(nextT);
				trianglesToCheck.add(curT);
				return rightT;
			}
			curT = nextT;
		}
	}
	
	Triangle extendCounterClockWise(Triangle t, Point2D.Double p) {
		Triangle leftT = new Triangle();
		triangles.add(leftT);
		Triangle curT = t;
		int abp = GeometryUtil.pointToLine(curT.a, curT.b, p);
		while (true) {
			// Go Left (counter clock wise)
			if (abp == GeometryUtil.PointToLinePosition.NegativePlane) {
				curT.c = p;
				trianglesToCheck.add(curT);
			}
			Triangle nextT = curT.getCa();
			switch (GeometryUtil.pointToLine(nextT.a, nextT.b, p)) {
			case GeometryUtil.PointToLinePosition.PositivePlane:
			case GeometryUtil.PointToLinePosition.AfterTheEndPoint:
				leftT.a = curT.a;
				leftT.b = p;
				leftT.setAb(curT);
				leftT.setBc(t.getBc()); // rightT
				leftT.setCa(nextT);

				nextT.setBc(leftT);
				curT.setCa(leftT);
				trianglesToCheck.add(leftT);
				trianglesToCheck.add(nextT);
				trianglesToCheck.add(curT);
				return leftT;
			}
			curT = nextT;
		}
	}
	
	/**
	 * Extends Outside, i.e. increase the convex hull of all the points.
	 */
	void extendOutside(Triangle t, Point2D.Double p) {
		// Triangle t is a "border" tirangle, i.e. t.c == null
		int abp = GeometryUtil.pointToLine(t.a, t.b, p);
		switch (abp) {
		case GeometryUtil.PointToLinePosition.Inside:
			// already handled
			throw new RuntimeException("WTF?");
		case GeometryUtil.PointToLinePosition.AfterTheEndPoint: {
			Triangle leftT = new Triangle(t.b, p);
			triangles.add(leftT);
			Triangle rightT = extendClockWise(t, p);
			leftT.setBc(rightT);
			leftT.setCa(t);
			if (rightT.getAb() == t) {
				rightT.setAb(leftT);
				leftT.setAb(rightT);
			} else {
				throw new RuntimeException("WTF?!?");
			}
			rightT.setCa(leftT);
			t.c = null;
			t.setBc(leftT);

			trianglesToCheck.add(leftT);
			break;
		}
		case GeometryUtil.PointToLinePosition.BeforeTheStartPoint:
			throw new RuntimeException("WTF?");
		default: {
			Triangle leftT = extendCounterClockWise(t, p);
			Triangle rightT = extendClockWise(t, p);
			leftT.setBc(rightT);
			rightT.setCa(leftT);
			break;
		}
		}
	}

	void checkAndFlip() {
		while (!trianglesToCheck.isEmpty()) {
			Iterator<Triangle> it = trianglesToCheck.iterator();
			Triangle t = it.next();
			it.remove();
			// extract the adjacent triangles because flip() will rotate the T triangle.
			Triangle ab = t.getAb();
			Triangle bc = t.getBc();
			Triangle ca = t.getCa();
			
			if (flip2Triangles(t, ab) || 
				flip2Triangles(t, bc) || 
				flip2Triangles(t, ca))
				; // do nothing
		}
	}
	
	boolean flip2Triangles(Triangle t, Triangle t1) {
		if ((t.c == null) || (t1.c == null)) {
			return false;
		}
		Point2D.Double p = t.getNotAdjacentPoint(t1);
		if (p == null)
			return false;
		if (!t.getCircumCircle().isPointInside(p)) {
			return false;
		}
		if (t.getAb() == t1) {
			// do nothing
		} else if (t.getBc() == t1) {
			t.rotateCounterClockWise();
		} else if (t.getCa() == t1) {
			t.rotateClockWise();
		} else {
			return false; // triangles are no longer andjacent...
		}
		t1.rotateAndMatchA(t.b);

		Triangle tmp = t.getBc();
		TriangleRotation trot = tmp.rotateAndMatchA(t.b);
		tmp.setCa(t1);
		tmp.unrotate(trot);
		t.setBc(t1);
		t1.setAb(tmp);
		
		tmp = t1.getBc();
		trot = tmp.rotateAndMatchA(t1.b);
		tmp.setCa(t);
		tmp.unrotate(trot);
		t1.setBc(t);
		t.setAb(tmp);
		
		t.b = t1.c;
		t1.b = t.c;

		trianglesToCheck.add(t);
		trianglesToCheck.add(t1);
		return true;
	}
	
	public void dumpIfBadTrianglesExist() {
		boolean badExist = false;
		for (Triangle t : triangles) {
			if (!t.isTriangleOk()) {
				badExist = true;
				break;
			}
		}
		String text = badExist ? "\nBAD triangles exist" : "\nNo bad triangles";
		dumpTriangles(text);
	}

	public void dumpTriangles(String text) {
		System.out.println(text);
		for (Triangle t : triangles) {
			System.out.println(triangle2String(t));
		}
	}

	public void dumpTriangle(Triangle t, String name) {
		System.out.println(name);
		System.out.println(triangle2String(t));
	}
	
	public String triangle2String(Triangle t) {
		boolean abOk = (t.getAb() != null) && t.getAb().containsPoint(t.a) && t.getAb().containsPoint(t.b);
		boolean bcOk = (t.getBc() != null) && t.getBc().containsPoint(t.b) && (t.c == null ? true : t.getBc().containsPoint(t.c));
		boolean caOk = (t.getCa() != null) && t.getCa().containsPoint(t.a) && (t.c == null ? true : t.getCa().containsPoint(t.c));
		
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
	
	private void check(boolean b, Triangle t, String text) {
		if (topologyResult == false)
			return;
		if (b)
			return;
		topologyResult = b;
		dumpTriangles("Topo error: " + triangles.indexOf(t) + ":" + text);
	}
	
	boolean topologyResult;
	
	public boolean isTopologyOk() {
		topologyResult = true;
		for (int i = 0; i < triangles.size(); i++) {
			Triangle t = triangles.get(i);
			check(t.isTriangleOk(), t, "bad triangle");
			if (!topologyResult)
				break;
			if (t.c == null)
				continue;
			Circle c = t.getCircumCircle();
			Point2D.Double p = t.getNotAdjacentPoint(t.getAb());
			if (p != null)
				check(!c.isPointInside(p), t, "AB");
			p = t.getNotAdjacentPoint(t.getBc());
			if (p != null)
				check(!c.isPointInside(p), t, "BC");
			p = t.getNotAdjacentPoint(t.getCa());
			if (p != null)
				check(!c.isPointInside(p), t, "CA");
		}
		return topologyResult;
	}
	
}
