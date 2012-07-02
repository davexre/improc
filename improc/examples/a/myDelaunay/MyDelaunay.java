package a.myDelaunay;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;

import com.slavi.math.GeometryUtil;

public class MyDelaunay {

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
		recursiveAddTriangle(root, r);
		return r;
	}
	
	public void insertPoint(Point2D.Double p) {
		Triangle t = insertPointSimple(p);
		if (t == null)
			return;
		Triangle t1 = t;
		do {
			flip(t1);
			t1 = t1.getCa();
		} while (t1 != t && t1.c != null);
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
	
	Triangle extendOutside(Triangle t, Point2D.Double p) {
		if (GeometryUtil.pointToLine(t.a, t.b, p) == GeometryUtil.PointToLinePosition.Inside) {
			Triangle t1 = new Triangle(t.a, t.b, p);
			Triangle t3 = new Triangle(p, t.b);
			triangles.add(t1);
			triangles.add(t3);
			t.b = p;
			t1.setAb(t.getAb());
			t1.getAb().switchneighbors(t, t1);
			t1.setBc(t3);
			t3.setAb(t1);
			t1.setCa(t);
			t.setAb(t1);
			t3.setBc(t.getBc());
			t3.getBc().setCa(t3);
			t3.setCa(t);
			t.setBc(t3);
			return t1;
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

	Triangle extendcounterclock(Triangle t, Point2D.Double p) {
		t.c = p;
		Triangle t1 = t.getCa();
		if (GeometryUtil.pointToLine(t1.a, t1.b, p) >= GeometryUtil.PointToLinePosition.PositivePlane) {
			Triangle t2 = new Triangle(t.a, p);
			triangles.add(t2);
			t2.setAb(t);
			t.setCa(t2);
			t2.setCa(t1);
			t1.setBc(t2);
			return t2;
		} else {
			return extendcounterclock(t1, p);
		}
	}

	Triangle extendclock(Triangle t, Point2D.Double p) {
		t.c = p;
		Triangle t1 = t.getBc();
		if (GeometryUtil.pointToLine(t1.a, t1.b, p) >= GeometryUtil.PointToLinePosition.PositivePlane) {
			Triangle t2 = new Triangle(p, t.b);
			triangles.add(t2);
			t2.setAb(t);
			t.setBc(t2);
			t2.setBc(t1);
			t1.setCa(t2);
			return t2;
		} else {
			return extendclock(t1, p);
		}
	}
	
	void flip(Triangle t) {
		Triangle t1 = t.getAb();
		if ((t1.c == null) || !t1.getCircumCircle().contains(t.c))
			return;
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
		flip(t);
		flip(t2);
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
			Triangle t;
			for (t = firstT; isGreater(p, t.a); t = t.getCa())
				;
			Triangle t2 = new Triangle(p, t.b);
			Triangle t3 = new Triangle(t.b, p);
			triangles.add(t2);
			triangles.add(t3);
			t.b = p;
			t.getAb().a = p;
			t2.setAb(t3);
			t3.setAb(t2);
			t2.setBc(t.getBc());
			t.getBc().setCa(t2);
			t2.setCa(t);
			t.setBc(t2);
			t3.setCa(t.getAb().getCa());
			t.getAb().getCa().setBc(t3);
			t3.setBc(t.getAb());
			t.getAb().setCa(t3);
			if (firstT == t) {
				firstT = t2;
			}
			break;
		}
		case GeometryUtil.PointToLinePosition.BeforeTheStartPoint: {
			Triangle t = new Triangle(firstP, p);
			Triangle t3 = new Triangle(p, firstP);
			triangles.add(t);
			triangles.add(t3);
			t.setAb(t3);
			t3.setAb(t);
			t.setBc(t3);
			t3.setCa(t);
			t.setCa(firstT);
			firstT.setBc(t);
			t3.setBc(firstT.getAb());
			firstT.getAb().setCa(t3);
			firstT = t;
			firstP = p;
			break;
		}
		case GeometryUtil.PointToLinePosition.AfterTheEndPoint: {
			Triangle t1 = new Triangle(p, lastP);
			Triangle t4 = new Triangle(lastP, p);
			triangles.add(t1);
			triangles.add(t4);
			t1.setAb(t4);
			t4.setAb(t1);
			t1.setBc(lastT);
			lastT.setCa(t1);
			t1.setCa(t4);
			t4.setBc(t1);
			t4.setCa(lastT.getAb());
			lastT.getAb().setBc(t4);
			lastT = t1;
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
			a = p;
			b = firstP;
		} else {
			a = firstP;
			b = p;
		}
		firstT = new Triangle(a, b);
		triangles.add(firstT);
		lastT = firstT;
		Triangle t = new Triangle(b, a);
		triangles.add(t);
		
		firstT.setAb(t);
		t.setAb(firstT);
		
		firstT.setBc(t);
		t.setBc(firstT);

		t.setCa(firstT);
		firstT.setCa(t);
		
		firstP = firstT.b;
		lastP = lastT.a;
	}
}
