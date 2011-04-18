package com.slavi.math.transform;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Map;

public class TransformerDataTestImpl implements Map.Entry<Point2D.Double, Point2D.Double>{

	public Point2D.Double src = new Point2D.Double();
	
	public Point2D.Double dest = new Point2D.Double();
	
	public boolean isBad;
	
	public double discrepancy;
	
	public boolean originalBad = false;

	public TransformerDataTestImpl() {
	}
	
	public TransformerDataTestImpl(AffineTransform jTransform, Point2D.Double s) {
		src.setLocation(s);
		jTransform.transform(src, dest);
	}

	public Point2D.Double getKey() {
		return src;
	}

	public Point2D.Double getValue() {
		return dest;
	}

	public Point2D.Double setValue(Point2D.Double value) {
		throw new RuntimeException("Method not allowed");
	}
}
