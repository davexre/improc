package com.slavi.reprap;

import java.awt.geom.Area;
import java.awt.geom.Path2D;

import com.slavi.math.GeometryUtil;

public class OptimizePathTest {

	public static void main(String[] args) {
		Path2D path = new Path2D.Double();
		path.moveTo(0, 0);
		path.lineTo(10, 0);
		path.lineTo(10, 10);
		path.lineTo(10, 10);
		path.lineTo(0, 10);

		Area p = new Area(path);
		Area area = new Area();
		area.add(p);
		
		System.out.println(GeometryUtil.pathIteratorToString(path.getPathIterator(null)));
		System.out.println("-------");
		System.out.println(GeometryUtil.pathIteratorToString(p.getPathIterator(null)));
		System.out.println("-------");
		System.out.println(GeometryUtil.pathIteratorToString(area.getPathIterator(null)));
	}
}
