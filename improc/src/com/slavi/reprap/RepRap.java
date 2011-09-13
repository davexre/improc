package com.slavi.reprap;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Shape3D;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import org.j3d.renderer.java3d.loaders.STLLoader;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;

public class RepRap {
/*

AllSTLsToBuild.addEdge
AllSTLsToBuild.slice
RrPolygon.simplify


 */

	void simplify(ArrayList<Point2D> points, double d) {
		ArrayList<Point2D> res = new ArrayList<Point2D>();
		if (points.size() <= 3) {
			res.addAll(points);
			return;
		}
		double d2 = d*d;

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
	
	void doIt() throws Exception {
//		String fname = "pulley-4.5-6-8-40.stl";
		String fname = "qube10.stl";
//		String fname = "small-qube10.stl";
		URL fin = getClass().getResource(fname);
		loadSTL(fin);
	}

	public static void main(String[] args) throws Exception {
		new RepRap().doIt();
		System.out.println("Done.");
	}
}
