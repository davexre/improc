package com.slavi.reprap;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
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
	
	/**
	 *  reprap - slice
	 */
	Area calcPolygonsForLayer(ArrayList objects, double z) {
		Area result = new Area();
		ArrayList<Line2D> edges = new ArrayList<Line2D>();
		for (Object object : objects) {
			edges.clear();
			RepRapRoutines.calcShape3DtoPlaneZIntersectionEdges(edges, z, object);
			while (edges.size() > 1) {
				Path2D path = GeometryUtil.extractPolygon(edges);
				Area area = new Area(path);
				result.add(area);
			}
		}
		return result;
	}
	
	void produceAdditiveTopDown(ArrayList objects) {
		double densityWidthInsideFill = 5;
		double densityWidthSurfaceFill = 2;
		double layersWidth = 0.56;
		double hatchAngleIncrease = 73 * MathUtil.deg2rad;
		
		Bounds3d bounds = new Bounds3d(); 
		for (Object object : objects)
			RepRapRoutines.calcShapeBounds(object, bounds);

		double curHatchAngle = 0;
		Stroke stroke = new BasicStroke((float) layersWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
		ArrayList<Area> layers = new ArrayList<Area>(5);
		layers.add(new Area());
		layers.add(new Area());
		double calcZ = bounds.maxZ - (bounds.maxZ - bounds.minZ + layersWidth * 0.5) % layersWidth;
		double minZ = bounds.minZ - layersWidth - layersWidth;
		while (calcZ >= minZ) {
			Area calcLayer = calcPolygonsForLayer(objects, calcZ);
			calcZ -= layersWidth;
			if (layers.size() < 5) {
				layers.add(0, calcLayer);
				continue;
			}
			layers.remove(5);
			layers.add(0, calcLayer);
			
			// Compute infills
			Area curLayer = layers.get(3);
			Area adjacentSlices = new Area();
			adjacentSlices.add(layers.get(0));			// curLayer -2
			adjacentSlices.intersect(layers.get(1));	// curLayer -1
			adjacentSlices.intersect(layers.get(3));	// curLayer +1
			adjacentSlices.intersect(layers.get(4));	// curLayer +2
			
			Area infills = new Area();
			infills.add(curLayer);
			infills.intersect(adjacentSlices);
			infills = RepRapRoutines.areaShrinkWithBrushWidth(stroke, infills); // reprap offset( outline=false )
			infills = new Area(RepRapRoutines.hatchArea(0, 0, densityWidthInsideFill, curHatchAngle, infills));

			Area outfills = new Area(); // reprap computeInfill - @see java.util.BitSet.andNot()
			outfills.add(curLayer);
			outfills.add(adjacentSlices);
			outfills.intersect(adjacentSlices);
			outfills = new Area(RepRapRoutines.hatchArea(0, 0, densityWidthSurfaceFill, curHatchAngle, outfills));

			Area outline = RepRapRoutines.areaShrinkWithBrushWidth(stroke, curLayer); // reprap computeOutlines - offset(outline=true, shell=1)
			// computeOutlines - middleStart - change the starting point of polygons... this seem to me to be obsolete

			// nearEnds - change the starting point of polygons so that the next 
			// polygon to print is the closest one to the last polygon that finished printing 
			
			// reprap - computeSupport
			Area support = RepRapRoutines.areaShrinkWithBrushWidth(stroke, curLayer); // reprap uses the constant -3
			support.add(layers.get(3)); // layer+1
			
			
			curHatchAngle = MathUtil.fixAngle2PI(curHatchAngle + hatchAngleIncrease);
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
