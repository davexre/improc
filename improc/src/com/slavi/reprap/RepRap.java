package com.slavi.reprap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Shape3D;
import javax.vecmath.Point3d;

import org.j3d.renderer.java3d.loaders.STLLoader;

import com.slavi.math.GeometryUtil;
import com.slavi.math.MathUtil;
import com.slavi.util.Const;
import com.slavi.util.file.FileUtil;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;

public class RepRap {
/*

AllSTLsToBuild.addEdge
AllSTLsToBuild.slice
RrPolygon.simplify


 */

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
	
	void doIt() throws Exception {
		String fname = "pulley-4.5-6-8-40.stl";
//		String fname = "qube10.stl";
//		String fname = "small-qube10.stl";
		URL fin = getClass().getResource(fname);
/*
		String fname = "C:/Users/i047367/S/img3d/img3d.stl";
		URL fin = new File(fname).toURL();
*/
		STLLoader loader = new STLLoader();
		Scene scene = loader.load(fin);
		BranchGroup stl = scene.getSceneGroup();
		ArrayList objects = new ArrayList();
		objects.add(stl);

		PrintJob job = new PrintJob();
		job.initialize(objects);
		job.print(new RepRapPrinter());
	}

	public static void main(String[] args) throws Exception {
		new RepRap().doIt();
//		new RepRap().testSlice();
		System.out.println("Done.");
	}
}
