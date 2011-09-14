package com.slavi.reprap;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.Shape3D;
import javax.vecmath.Point3d;

import org.j3d.renderer.java3d.loaders.STLLoader;

import com.slavi.math.GeometryUtil;
import com.slavi.math.MathUtil;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;

public class RepRap {
/*

AllSTLsToBuild.addEdge
AllSTLsToBuild.slice
RrPolygon.simplify


 */

	
	/**
	 * @return the vertex%points.size() at which the polygon deviates from a (nearly) straight line from v1
	 */
	private static int findAngleStart(ArrayList<Point2D> points, int startAt, double d2) {
		int len = points.size();
		Point2D curPoint = points.get(startAt % len);
		int nextPointIndex = startAt;
		for (int i = len; i >= 0; i--) {
			nextPointIndex++;
			Point2D nextPoint = points.get(nextPointIndex % len);
			for (int j = startAt + 1; j < nextPointIndex; j++) {
				Point2D tmpPoint = points.get(j % len);
				double tmpD2 = GeometryUtil.distanceFromPointToLineSquared(
						curPoint.getX(), curPoint.getY(), 
						nextPoint.getX(), nextPoint.getY(), 
						tmpPoint.getX(), tmpPoint.getY());
				if (tmpD2 >= d2) {
					return nextPointIndex - 1;
				}
			}
		}
		return -1;
	}

	public static ArrayList<Point2D> simplifyPolygon(ArrayList<Point2D> points, double d) {
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		int startAt = -1;
		for (int i = points.size() - 1; i >= 0; i--) {
			Point2D p = points.get(i);
			if (p.getX() > minX)
				continue;
			if ((p.getX() == minX) && (p.getY() > minY))
				continue;
			startAt = i;
			minX = p.getX();
			minY = p.getY();
		}
		
		ArrayList<Point2D> res = new ArrayList<Point2D>();
		if (startAt < 0) {
			return res;
		}
		double d2 = d*d;
		startAt = findAngleStart(points, startAt, d2);
		if (startAt < 0) {
			System.out.println("OPS");
			res.addAll(points);
			return res;
		}
		int curPoint = startAt; 
		while ((curPoint >= 0) && (curPoint - startAt < points.size())) {
			res.add(points.get(curPoint % points.size()));
			curPoint = findAngleStart(points, curPoint, d2);
		}
		return res;
	}
	
	void testSimplify() {
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		points.add(new Point2D.Double(0, 0));
		points.add(new Point2D.Double(1, 0));
		points.add(new Point2D.Double(2, 0));
		points.add(new Point2D.Double(2, 2));
		points.add(new Point2D.Double(-2, 2));
		points.add(new Point2D.Double(-2, 0));
		points.add(new Point2D.Double(-1, 0));
		double d = 0.1;
		for (int i = 0; i < points.size(); i++)
			System.out.println(i + " " + points.get(i));
		System.out.println();
		ArrayList<Point2D> simplified = simplifyPolygon(points, d);
		System.out.println(simplified);
	}
	
	public static void main(String[] args) {
		RepRap r = new RepRap();
		r.testSimplify();
	}
	
	void calcBoundingBox(ArrayList<Point2D> points, Rectangle2D bbox) {
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;
		
		for (Point2D p : points) {
			if (minX > p.getX())
				minX = p.getX();
			if (maxX < p.getX())
				maxX = p.getX();

			if (minY > p.getY())
				minY = p.getY();
			if (maxY < p.getY())
				maxY = p.getY();
		}
		bbox.setRect(minX, minY, maxX - minX, maxY - minY);
	}
	
	void loadSTL(URL fin) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
		STLLoader loader = new STLLoader();
		Scene scene = loader.load(fin);
		BranchGroup stl = scene.getSceneGroup();
		Enumeration children = stl.getAllChildren();
		while (children.hasMoreElements()) {
			Object child = children.nextElement();
			System.out.println(child.getClass());
			if (child instanceof Shape3D) {
				Shape3D shape = (Shape3D) child;
				GeometryArray g = (GeometryArray)shape.getGeometry();
				Point3d p = new Point3d();
				int count = g.getVertexCount();
				for (int i = 0; i < count; i++) {
					g.getCoordinate(i, p);
					System.out.println("  " + p);
				}
			}
		}
	}
	
	void calcBounds(BranchGroup stl) {
		Enumeration children = stl.getAllChildren();
		while (children.hasMoreElements()) {
			Object child = children.nextElement();
			if (child instanceof Group) {
				Group group = (Group)child;

			} else if (child instanceof Shape3D) {
				Shape3D shape = (Shape3D)child;
				GeometryArray g = (GeometryArray)shape.getGeometry();
				Point3d p = new Point3d();
				int count = g.getVertexCount();
				for (int i = 0; i < count; i++) {
					g.getCoordinate(i, p);
					System.out.println("  " + p);
				}
			} else if (child instanceof BranchGroup) {
				BranchGroup branchGroup = (BranchGroup) child;
			}
		}		
	}
	
	double dist2(double x1, double y1, double x2, double y2) {
		x1 -= x2;
		y1 -= y2;
		return x1*x1 + y1*y2;
	}
	
	public ArrayList<Point2D> extractPolygon(ArrayList<Line2D> edges) {
		if (edges.size() < 1)
			return null;
		ArrayList<Point2D> res = new ArrayList<Point2D>();
		Line2D edge = edges.remove(0);
		Point2D start = edge.getP1();
		Point2D end = edge.getP2();
		res.add(start);
		res.add(end);
		
		boolean first = true;
		while(edges.size() > 0) {
			double d2 = dist2(start.getX(), start.getY(), end.getX(), end.getY());
			if(first)
				d2 = Math.max(d2, 1);

			// Find nearest segment to start OR end point
			boolean aEnd = false;
			int index = -1;
			for(int i = 0; i < edges.size(); i++) {
				edge = edges.get(i);
				double dd = dist2(end.getX(), end.getY(), edge.getX1(), edge.getY1());
				if(dd < d2) {
					d2 = dd;
					aEnd = true;
					index = i;
				}
				dd = dist2(end.getX(), end.getY(), edge.getX2(), edge.getY2());
				if(dd < d2) {
					d2 = dd;
					aEnd = false;
					index = i;
				}
			}

			if(index >= 0) {
				edge = edges.get(index);
				edges.remove(index);
				Point2D p1;
				Point2D p2;
				if(aEnd) {
					p1 = edge.getP1();
					p2 = edge.getP2();
				} else {
					p2 = edge.getP1();
					p1 = edge.getP2();
				}
				end.setLocation(
						0.5 * (end.getX() + p1.getX()), 
						0.5 * (end.getY() + p1.getY()));
				end = p2;
				res.add(end);
			} else {
				break;
			}
		}
		return res;
	}
	
	static final double gridRes = 1.0/100;  // ...10 micron grid
	
	void addEdges(ArrayList<Line2D.Double> edges, double z, Object object) throws Exception {
		if (object instanceof Shape3D) {
			Shape3D shape = (Shape3D) object;
			GeometryArray g = (GeometryArray)shape.getGeometry();
			int count = g.getVertexCount();
			if (count % 3 != 0)
				throw new Exception("Vertices not a multiple of 3");
			Point3d p1 = new Point3d();
			Point3d p2 = new Point3d();
			Point3d p3 = new Point3d();
			int i = 0;
			while (i < count) {
	            g.getCoordinate(i++, p1);
	            g.getCoordinate(i++, p2);
	            g.getCoordinate(i++, p3);
	            addEdges(edges, z, p1, p2, p3);
			}
		} else if (object instanceof Group) {
			Group group = (Group) object;
			Enumeration children = group.getAllChildren();
			while (children.hasMoreElements())
				addEdges(edges, z, children.nextElement());
		}
	}
	
	void addEdges(ArrayList<Line2D.Double> edges, double z, Point3d p, Point3d q, Point3d r) {
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
		double dist = MathUtil.hypot(x1 - x2, y1 - y2);
		if (dist > gridRes)
			edges.add(new Line2D.Double(x1, y1, x2, y2));
	}
	
	void doIt() throws Exception {
//		String fname = "pulley-4.5-6-8-40.stl";
		String fname = "qube10.stl";
//		String fname = "small-qube10.stl";
		URL fin = getClass().getResource(fname);
		loadSTL(fin);
	}

	public static void main2(String[] args) throws Exception {
		new RepRap().doIt();
		System.out.println("Done.");
	}
}
