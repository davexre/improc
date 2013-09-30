package com.test;

import java.awt.geom.Point2D;

import com.slavi.math.GeometryUtil;

public class CircumCircleTest {
	public static void main(String[] args) {
		Point2D.Double p1, p2, p3, c;
		p1 = new Point2D.Double(10, 10);
		p2 = new Point2D.Double(20, 10);
		p3 = new Point2D.Double(40, 10);
		c = new Point2D.Double();

		double r = GeometryUtil.circleTreePoints(p1, p2, p3, c);
		
		System.out.println("R=" + r);
		System.out.println("C=" + c);
	}
}
