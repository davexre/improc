package com.slavi.reprap;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
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
	Bounds3d bounds = new Bounds3d();

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
				transformedPath.append(path.getPathIterator(tranfsormObjects), true);
				Area area = new Area(transformedPath);
				result.add(area);
			}
		}
		return result;
	}
		
	boolean isFirstAdjecentSlice;
	private void addAdjecentSlice(Area adjacentSlices, int layerToAdd) {
		if ((layerToAdd >= 0) && (layerToAdd < layers.size())) {
			if (isFirstAdjecentSlice)
				adjacentSlices.add(layers.get(layerToAdd).slice);
			else
				adjacentSlices.intersect(layers.get(layerToAdd).slice);
			isFirstAdjecentSlice = false;
		}
	}
	
	// produceAdditiveTopDown
	public void print(RepRapPrinter printer) throws Exception {
		double densityWidthInsideFill = 10;
		double densityWidthSurfaceFill = 5;
		double layersWidth = 0.56;
		double hatchAngleIncrease = 73 * MathUtil.deg2rad;
		double curHatchAngle = 0;
		Stroke stroke = new BasicStroke((float) layersWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
		Area unionOfLayersAbove = new Area();

		Area tmp = new Area();
		Area adjacentSlices = new Area();
		printer.startPrinting(bounds);
		for (int curLayerNumber = layers.size() - 1; curLayerNumber >= 0; curLayerNumber--) {
			Layer curLayer = layers.get(curLayerNumber);
			adjacentSlices.reset();
			isFirstAdjecentSlice = true;

			unionOfLayersAbove.add(curLayer.slice);
			
			addAdjecentSlice(adjacentSlices, curLayerNumber + 2);
			addAdjecentSlice(adjacentSlices, curLayerNumber + 1);
			addAdjecentSlice(adjacentSlices, curLayerNumber - 1);
			addAdjecentSlice(adjacentSlices, curLayerNumber - 2);
			
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

			printLayer.support = new Area();
			printLayer.support.add(unionOfLayersAbove);
			printLayer.support.exclusiveOr(curLayer.slice);
			printLayer.supportHatch = RepRapRoutines.hatchArea(0, 0, densityWidthSurfaceFill, curHatchAngle, printLayer.support);
			
			curHatchAngle = MathUtil.fixAngle2PI(curHatchAngle + hatchAngleIncrease);
			printer.printLayer(printLayer);
		}
		printer.stopPrinting();
	}
	
	public void initialize(ArrayList objects) {
		for (Object object : objects)
			calcShapeBounds(object, bounds);

		AffineTransform transformObjects = new AffineTransform();
		transformObjects.scale(30, 30);
		transformObjects.translate(-bounds.minX, -bounds.minY);
		int curLayerNumber = 0;
		for (double curZ = bounds.minZ + layersWidth *0.5; curZ <= bounds.maxZ; curZ += layersWidth) {
			Layer layer = new Layer();
			layer.z = curZ;
			layer.slice = PrintJob.calcPolygonsForLayer(objects, curZ, transformObjects);
			layer.layerNumber = curLayerNumber++;
			layers.add(layer);
		}
	}
}
