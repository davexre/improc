package com.slavi.reprap;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.Shape3D;
import javax.vecmath.Point3d;

import com.slavi.math.GeometryUtil;
import com.slavi.math.MathUtil;

public class PrintJob {
	public ArrayList<Layer> layers = new ArrayList<Layer>();
	Bounds3d objectsBounds = new Bounds3d();
	Rectangle2D layerBounds = new Rectangle2D.Double();

	double layersWidth = 0.56;

	public static void calcShapeBounds(Object object, Bounds3d bounds) {
		if (object instanceof Shape3D) {
			Shape3D shape = (Shape3D) object;
			GeometryArray g = (GeometryArray)shape.getGeometry();
			int count = g.getVertexCount();
			Point3d p = new Point3d();
			int i = 0;
			while (i < count) {
	            g.getCoordinate(i++, p);
	            bounds.minX = Math.min(bounds.minX, p.x);
	            bounds.minY = Math.min(bounds.minY, p.y);
	            bounds.minZ = Math.min(bounds.minZ, p.z);
	            
	            bounds.maxX = Math.max(bounds.maxX, p.x);
	            bounds.maxY = Math.max(bounds.maxY, p.y);
	            bounds.maxZ = Math.max(bounds.maxZ, p.z);
			}
		} else if (object instanceof Group) {
			Group group = (Group) object;
			Enumeration children = group.getAllChildren();
			while (children.hasMoreElements())
				calcShapeBounds(children.nextElement(), bounds);
		}
	}
	
	/**
	 *  reprap - slice
	 */
	public static Area calcPolygonsForLayer(ArrayList objects, double z, AffineTransform tranfsormObjects) {
		Area result = new Area();
		ArrayList<Line2D> edges = new ArrayList<Line2D>();
		for (Object object : objects) {
			edges.clear();
			RepRapRoutines.calcShape3DtoPlaneZIntersectionEdges(edges, z, object);
			while (edges.size() > 1) {
				Path2D path = GeometryUtil.extractPolygon(edges);
				Path2D transformedPath = new Path2D.Double();
				transformedPath.append(path.getPathIterator(tranfsormObjects), false);
				Area area = new Area(transformedPath);
				result.add(area);
			}
		}
		return result;
	}
	
	// produceAdditiveTopDown
	public void print(RepRapPrinter printer) throws Exception {
		double densityWidthInsideFill = 20;
		double densityWidthSurfaceFill = 10;
		double layersWidth = 0.56;
		double hatchAngleIncrease = 73 * MathUtil.deg2rad;
		double curHatchAngle = 0;
		Stroke stroke = new BasicStroke((float) layersWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);

		Area tmp = new Area();
		Area adjacentSlices = new Area();
		printer.startPrinting(layerBounds);
		for (int curLayerNumber = 0; curLayerNumber < layers.size(); curLayerNumber++) {
			Layer curLayer = layers.get(curLayerNumber);
			adjacentSlices.reset();

			if ((curLayerNumber - 2 >= 0) && (curLayerNumber + 2 < layers.size())) {
				adjacentSlices.add(layers.get(curLayerNumber + 2).slice);
				adjacentSlices.intersect(layers.get(curLayerNumber + 1).slice);
				adjacentSlices.intersect(layers.get(curLayerNumber - 1).slice);
				adjacentSlices.intersect(layers.get(curLayerNumber - 2).slice);
			}

			PrintLayer printLayer = new PrintLayer(curLayer);
			// Calc the "inside" filled polygons, i.e. the "not a surface" areas that are with less dense fill.
			tmp.reset();
			tmp.add(curLayer.slice);
			tmp.intersect(adjacentSlices);
			printLayer.infills = RepRapRoutines.areaShrinkWithBrushWidth(stroke, tmp); // reprap offset( outline=false )
			printLayer.infillsHatch = RepRapRoutines.hatchArea(0, 0, densityWidthInsideFill, curHatchAngle, printLayer.infills);

			// Calc the surface polygons - higher hatch density
			printLayer.outfills = new Area(); // reprap computeInfill - @see java.util.BitSet.andNot()
			printLayer.outfills.add(curLayer.slice);
			printLayer.outfills.add(adjacentSlices);
			printLayer.outfills.exclusiveOr(adjacentSlices);
			printLayer.outfillsHatch = RepRapRoutines.hatchArea(0, 0, densityWidthSurfaceFill, curHatchAngle, printLayer.outfills);

			// reprap computeOutlines - offset(outline=true, shell=1)
			printLayer.outline = RepRapRoutines.areaShrinkWithBrushWidth(stroke, curLayer.slice); 
			// computeOutlines - middleStart - change the starting point of polygons... this seem to me to be obsolete

			// nearEnds - change the starting point of polygons so that the next 
			// polygon to print is the closest one to the last polygon that finished printing 
			
			// reprap - computeSupport
//			Area support = RepRapRoutines.areaShrinkWithBrushWidth(stroke, curLayer); // reprap uses the constant -3
//			support.add(layers.get(3)); // layer+1

			printLayer.supportHatch = RepRapRoutines.hatchArea(0, 0, densityWidthSurfaceFill, curHatchAngle + Math.PI/2, printLayer.support);
			
			curHatchAngle = MathUtil.fixAngle2PI(curHatchAngle + hatchAngleIncrease);
			printer.printLayer(printLayer);
		}
		printer.stopPrinting();
	}
	
	public void initialize(ArrayList objects) {
		for (Object object : objects)
			calcShapeBounds(object, objectsBounds);

		AffineTransform transformObjects = new AffineTransform();
		transformObjects.scale(30, 30);
		transformObjects.translate(-objectsBounds.minX, -objectsBounds.minY);
		int curLayerNumber = 0;

		double startZ = objectsBounds.minZ + layersWidth *0.5;
		for (double curZ = startZ; curZ <= objectsBounds.maxZ; curZ += layersWidth) {
			Layer curLayer = new Layer();
			curLayer.objectZ = curZ;
			curLayer.reprapZ = curZ - startZ;
			curLayer.slice = PrintJob.calcPolygonsForLayer(objects, curZ, transformObjects);

			Rectangle2D curBounds = curLayer.slice.getBounds2D();
			if (layers.size() == 0)
				layerBounds.setFrame(curBounds);
			else
				Rectangle2D.union(layerBounds, curBounds, layerBounds);
			curLayer.layerNumber = curLayerNumber++;
			layers.add(curLayer);
		}
		
		Area unionOfLayersAbove = new Area();
		for (int i = layers.size() - 1; i >= 0; i--) {
			Layer curLayer = layers.get(i);
			unionOfLayersAbove.add(curLayer.slice);
			curLayer.support = new Area();
			curLayer.support.add(unionOfLayersAbove);
			curLayer.support.exclusiveOr(curLayer.slice);
		}
	}
}
