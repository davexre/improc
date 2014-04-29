package com.test;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import com.slavi.math.GeometryUtil;

public class LineIntersectsLineTest {

	public static void main(String[] args) {
		Point2D.Double a = new Point2D.Double(1, 1);
		Point2D.Double b = new Point2D.Double(2, 1);
		Point2D.Double c = new Point2D.Double(3, 2);
		Point2D.Double d = new Point2D.Double(3, 3);
		
		Point2D x = GeometryUtil.lineIntersectsLine(new Line2D.Double(a, b), new Line2D.Double(c, d));
		System.out.println(x);
	}
}
