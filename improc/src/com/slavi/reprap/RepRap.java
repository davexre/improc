package com.slavi.reprap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
				Area area = GeometryUtil.extractPolygon(edges);
				result.add(area);
			}
			/*
			for (Line2D l : edges)
				result.add(new Area(l));
			 */
		}
		return result;
	}
	
	void produceAdditiveTopDown(ArrayList objects) throws IOException {
		File outputDir = new File(Const.tempDir, "layers");
		FileUtil.removeDirectory(outputDir);
		outputDir.mkdirs();
		
		double densityWidthInsideFill = 5;
		double densityWidthSurfaceFill = 2;
		double layersWidth = 0.56;
		double hatchAngleIncrease = 73 * MathUtil.deg2rad;
		
		Bounds3d bounds = new Bounds3d(); 
		for (Object object : objects)
			RepRapRoutines.calcShapeBounds(object, bounds);
		
		double scale = 30;
		BufferedImage bi = new BufferedImage(
				(int) (scale * ((bounds.maxX - bounds.minX) + 1)), 
				(int) (scale * ((bounds.maxY - bounds.minY) + 1)),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) bi.getGraphics();
		AffineTransform tr = new AffineTransform();
		tr.scale(scale, scale);
		g.transform(tr);
		g.translate(-bounds.minX, -bounds.minY);
		
		double curHatchAngle = 0;
		Stroke stroke = new BasicStroke((float) layersWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
		ArrayList<Area> layers = new ArrayList<Area>(5);
		Area unionOfAllAbove = new Area();
		layers.add(new Area());
		layers.add(new Area());
		double calcZ = bounds.maxZ - (bounds.maxZ - bounds.minZ + layersWidth * 0.5) % layersWidth;
		double minZ = bounds.minZ - layersWidth - layersWidth;
		int curLayerNumber = 0;
		while (calcZ >= minZ) {
			Area calcLayer = calcPolygonsForLayer(objects, calcZ);
			calcZ -= layersWidth;
			if (layers.size() < 5) {
				layers.add(0, calcLayer);
				continue;
			}
			layers.remove(4);
			layers.add(0, calcLayer);
			
			// Compute infills
			Area curLayer = layers.get(3);
			
			Area adjacentSlices = new Area();
			adjacentSlices.add(layers.get(0));			// curLayer -2
			adjacentSlices.intersect(layers.get(1));	// curLayer -1
			adjacentSlices.intersect(layers.get(3));	// curLayer +1
			adjacentSlices.intersect(layers.get(4));	// curLayer +2
			
			// Calc the "inside" filled polygons, i.e. not a surface, less dense fill. 
			Area infills = new Area(); 
			infills.add(curLayer);
			infills.intersect(adjacentSlices);
			infills = RepRapRoutines.areaShrinkWithBrushWidth(stroke, infills); // reprap offset( outline=false )
//			infills = new Area(RepRapRoutines.hatchArea(0, 0, densityWidthInsideFill, curHatchAngle, infills));

			// Calc the surface polygons - higher hatch density
			Area outfills = new Area(); // reprap computeInfill - @see java.util.BitSet.andNot()
			outfills.add(curLayer);
			outfills.add(adjacentSlices);
			outfills.intersect(adjacentSlices);
//			outfills = new Area(RepRapRoutines.hatchArea(0, 0, densityWidthSurfaceFill, curHatchAngle, outfills));

			Area outline = RepRapRoutines.areaShrinkWithBrushWidth(stroke, curLayer); // reprap computeOutlines - offset(outline=true, shell=1)
			// computeOutlines - middleStart - change the starting point of polygons... this seem to me to be obsolete

			// nearEnds - change the starting point of polygons so that the next 
			// polygon to print is the closest one to the last polygon that finished printing 
			
			// reprap - computeSupport
//			Area support = RepRapRoutines.areaShrinkWithBrushWidth(stroke, curLayer); // reprap uses the constant -3
//			support.add(layers.get(3)); // layer+1
			unionOfAllAbove.add(curLayer);
			Area support = new Area();
			support.add(unionOfAllAbove);
			support.intersect(curLayer);
			support = new Area(RepRapRoutines.hatchArea(0, 0, densityWidthSurfaceFill, curHatchAngle, support));
			
			curHatchAngle = MathUtil.fixAngle2PI(curHatchAngle + hatchAngleIncrease);
			
			curLayerNumber++;
			g.setColor(Color.black);
			g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
/*			
			g.setColor(Color.blue);
			g.fill(curLayer);
*/
			
			g.setColor(Color.blue);
			g.fill(infills);
			g.setColor(Color.green);
			g.fill(outfills);
			g.setColor(Color.red);
			g.draw(support);
			g.setColor(Color.white);
			g.draw(outline);
						
			File fou = new File(outputDir, String.format(Locale.US, "layer_%04d.png", curLayerNumber));
			System.out.println(fou);
			ImageIO.write(bi, "png", fou);
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
		produceAdditiveTopDown(objects);
	}

	void testSlice() throws Exception {
		/*		
		String fname = "C:/Users/i047367/S/img3d/img3d.stl";
		URL fin = new File(fname).toURL();
		 */

		String fname = "qube10.stl";
		URL fin = getClass().getResource(fname);
		STLLoader loader = new STLLoader();
		Scene scene = loader.load(fin);
		BranchGroup object = scene.getSceneGroup();
		
		Bounds3d bounds = new Bounds3d(); 
		RepRapRoutines.calcShapeBounds(object, bounds);
		System.out.println(bounds);

		BufferedImage bi = new BufferedImage(
				1200, 1200,
//				(int) (bounds.maxX - bounds.minX) + 1, 
//				(int) (bounds.maxY - bounds.minY) + 1,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) bi.getGraphics();
		
//		g.translate(-bounds.minX, -bounds.minY);
		g.translate(100, 100);
		
		g.setColor(Color.blue);

		ArrayList<Line2D> edges = new ArrayList<Line2D>();
		RepRapRoutines.calcShape3DtoPlaneZIntersectionEdges(edges, 5, object);
		for (Line2D l : edges) {
			System.out.println(GeometryUtil.shapeToString(l));
			g.draw(l);
		}
//		g.translate(200, 0);
		g.setColor(Color.yellow);

		Area curLayer = new Area();
		while (edges.size() > 1) {
			System.out.println(edges.size());
			Area area = GeometryUtil.extractPolygon(edges);
			System.out.println(GeometryUtil.pathIteratorToString(area.getPathIterator(null)));
			g.draw(area);
//			g.fill(area);
			g.translate(120, 0);
			curLayer.add(area);
		}
		
//		g.translate(0, 100);
		g.setColor(Color.red);
		g.fill(curLayer);

		g.translate(bounds.minX, bounds.minY);
		File fou = new File(Const.tempDir, "output3.png");
		ImageIO.write(bi, "png", fou);
	}

	public static void main(String[] args) throws Exception {
		new RepRap().doIt();
//		new RepRap().testSlice();
		System.out.println("Done.");
	}
}
