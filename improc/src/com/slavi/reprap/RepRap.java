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
		Enumeration<?> children = stl.getAllChildren();
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
		Enumeration<?> children = stl.getAllChildren();
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
			Enumeration<?> children = group.getAllChildren();
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
