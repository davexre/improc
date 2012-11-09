package a.myDelaunay;

import java.awt.geom.Point2D;
import java.util.ArrayList;

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

		Triangle t = doInsert(root, p);
		System.out.println("Selected triangle id=" + triangles.indexOf(t));
		dumpIfBadTrianglesExist();
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
	
	private Triangle splitOnEdgeAB(Triangle t, Point2D.Double p) {
		if ((t.c != null) && 
			(GeometryUtil.pointToLine(t.a, p, t.c) >= GeometryUtil.PointToLinePosition.PositivePlane))
			throw new RuntimeException("WTF?");

		dumpTriangle(t, "Split");
		dumpIfBadTrianglesExist();
		System.out.println();
		
		Triangle right = new Triangle(p, t.b);
		right.c = t.c;
		Triangle mirrorRight = new Triangle(t.b, p);
		triangles.add(right);
		triangles.add(mirrorRight);
		Triangle mirrorT = t.getAb();

		if (t.a == mirrorT.b) {
			mirrorRight.c = mirrorT.c;
			mirrorRight.setCa(mirrorT.getCa());
			mirrorT.a = p;
			mirrorT.setCa(mirrorRight);
		} else if (t.a == mirrorT.c) {
			mirrorRight.c = mirrorT.a;
			mirrorRight.setCa(mirrorT.getAb());
			mirrorT.b = p;
			mirrorT.setAb(mirrorRight);
		} else if (t.a == mirrorT.a) {
			mirrorRight.c = mirrorT.b;
			mirrorRight.setCa(mirrorT.getBc());
			mirrorT.c = p;
			mirrorT.setBc(mirrorRight);
		} else {
			throw new RuntimeException("WTF?");
		}

		right.setAb(mirrorRight);
		right.setBc(t.getBc());
		right.setCa(t);

		mirrorRight.setAb(right);
		mirrorRight.setBc(mirrorT);

		t.b = p;
		t.setBc(right);
		
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
}
