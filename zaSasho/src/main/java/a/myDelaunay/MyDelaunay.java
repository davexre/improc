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
	
	public ArrayList<Point2D> points = new ArrayList<Point2D>();
	public ArrayList<Triangle> triangles = new ArrayList<Triangle>();
	public HashSet<Triangle> trianglesToCheck = new HashSet<Triangle>();
	public Triangle root;

	boolean allCollinear = true;
	
	public ArrayList<Triangle> getTriangles() {
		return triangles;
	}
	
	public void insertPoint(Point2D p) {
		points.add(p);
		if (points.size() == 1) {
			return;
		}
		if (points.size() == 2) {
			Point2D a = points.get(0);
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
		for (Triangle t : triangles)
			t.flipCount = 0;
		checkAndFlip();
		for (Triangle t : triangles)
			if (t.flipCount > individualMaxFlipCount)
				individualMaxFlipCount = t.flipCount;
		
/*		
		boolean isok = isTopologyOk();
		if (!isok) {
			dumpTriangles("bad topology");
			throw new Error();
		}*/
	}
	
	public int individualMaxFlipCount = 0;
	
	private void doInsert(Triangle t, Point2D p) {
		while (true) {
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
				if (t.c == null) {
					Triangle next = t.getCa();
					int nextABP = GeometryUtil.pointToLine(next.a, next.b, p);
					if ((nextABP == GeometryUtil.PointToLinePosition.AfterTheEndPoint) ||
						(nextABP == GeometryUtil.PointToLinePosition.PositivePlane)) {
						break;
					}
					t = next;
					continue;
				}
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
	
	private void splitOnEdgeAB(Triangle left, Point2D p) {
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

	/**
	 * Extends Outside, i.e. increase the convex hull of all the points.
	 */
	void extendOutside(Triangle t, Point2D p) {
		// Triangle t is a "border" tirangle, i.e. t.c == null and t is also 
		//   the "left most" triangle applicapble for extending to point p
		Triangle leftT = new Triangle();
		Triangle rightT = new Triangle();
		triangles.add(leftT);
		triangles.add(rightT);
		trianglesToCheck.add(leftT);
		trianglesToCheck.add(rightT);

		rightT.a = p;
		leftT.b = p;
		rightT.setCa(leftT);
		leftT.setBc(rightT);

		Triangle curT = t;
		int curTabp;
		int abp = curTabp = GeometryUtil.pointToLine(curT.a, curT.b, p);
		while (true) {
			// Go Right (clockwise)
			if (curTabp == GeometryUtil.PointToLinePosition.NegativePlane) {
				curT.c = p;
				trianglesToCheck.add(curT);
			}
			Triangle nextT = curT.getBc();
			curTabp = GeometryUtil.pointToLine(nextT.a, nextT.b, p);
			if (curTabp == GeometryUtil.PointToLinePosition.PositivePlane || 
				curTabp == GeometryUtil.PointToLinePosition.BeforeTheStartPoint) {
				rightT.b = curT.b;
				rightT.setAb(curT);
				rightT.setBc(nextT);

				nextT.setCa(rightT);
				curT.setBc(rightT);
				trianglesToCheck.add(nextT);
				trianglesToCheck.add(curT);
				break;
			}
			curT = nextT;
		}
		
		if (abp == GeometryUtil.PointToLinePosition.AfterTheEndPoint) {
			// Triangle t is the "left most" triangle
			leftT.a = t.b;
			leftT.setCa(t);
			rightT.setAb(leftT);
			leftT.setAb(rightT);
			t.setBc(leftT);
		} else if (abp == GeometryUtil.PointToLinePosition.NegativePlane) {
			leftT.a = t.a;
			Triangle nextT = t.getCa();
			leftT.setCa(nextT);
			leftT.setAb(t);

			nextT.setBc(leftT);
			t.setCa(leftT);
			
			trianglesToCheck.add(nextT);
			trianglesToCheck.add(t);
		} else {
			// already handled
			throw new RuntimeException("WTF?");
		}
	}
	
	public void checkAndFlip() {
		while (!trianglesToCheck.isEmpty()) {
			if (trianglesToCheck.size() > maxToFlip)
				maxToFlip = trianglesToCheck.size();
			
			Iterator<Triangle> it = trianglesToCheck.iterator();
			Triangle t = it.next();
			it.remove();
			// extract the adjacent triangles because flip() will rotate the T triangle.
			Triangle ab = t.getAb();
			Triangle bc = t.getBc();
			Triangle ca = t.getCa();
			
			if (flipAdjacentTriangles(t, ab) || 
				flipAdjacentTriangles(t, bc) || 
				flipAdjacentTriangles(t, ca))
				; // do nothing
		}
	}
	
	public int maxToFlip = 0;
	public int flipCount = 0;
	
	void dumpFlipInfo(Triangle t1, Triangle t2) {
		int it1 = triangles.indexOf(t1);
		int it2 = triangles.indexOf(t2);
/*		if (it1 > it2) {
			int tmp = it1;
			it1 = it2;
			it2 = tmp;
			Triangle tmpT = t1;
			t1 = t2;
			t2 = tmpT;
		}
*/		System.out.println("Flip\t" + it1 + "\t" + it2 + "\t" + t1.flipCount + "/" + t2.flipCount);
	}
	
	boolean flipAdjacentTriangles(Triangle t, Triangle t1) {
		if ((t.c == null) || (t1.c == null)) {
			return false;
		}
		Point2D p = t.getNotAdjacentPoint(t1);
		if (p == null)
			return false;
		if (!t.getCircumCircle().isPointInsideEps(p)) {
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
		flipCount++;
		t.flipCount++;
		t1.flipCount++;
//		dumpFlipInfo(t, t1);
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
	
	public boolean isTopologyOk() {
		boolean result = true;
		for (int i = 0; i < triangles.size(); i++) {
			Triangle t = triangles.get(i);
			result &= t.isTriangleOk();
			if (!result)
				break;
			if (t.c == null)
				continue;
			Circle c = t.getCircumCircle();
			Point2D p = t.getNotAdjacentPoint(t.getAb());
			if (p != null)
				result &= !c.isPointInsideEps(p);
			p = t.getNotAdjacentPoint(t.getBc());
			if (p != null)
				result &= !c.isPointInsideEps(p);
			p = t.getNotAdjacentPoint(t.getCa());
			if (p != null)
				result &= !c.isPointInsideEps(p);
		}
		return result;
	}
	
	public void dumpTriangles(String text) {
		System.out.println(text);
		for (Triangle t : triangles) {
			System.out.println(triangle2String(t));
		}
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
	
	public void dumpStatistics() {
		System.out.println("Individual max Flip count: " + individualMaxFlipCount);
		System.out.println("MaxToFlip:  " + maxToFlip);
		System.out.println("Flip count: " + flipCount);
		System.out.println("Triangles:  " + triangles.size());
		System.out.println("Points:     " + points.size());
	}
}
