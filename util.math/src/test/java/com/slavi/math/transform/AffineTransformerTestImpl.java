package com.slavi.math.transform;

import java.awt.geom.Point2D;

public class AffineTransformerTestImpl extends AffineTransformer<Point2D.Double, Point2D.Double> {
	public int getInputSize() {
		return 2;
	}

	public int getOutputSize() {
		return 2;
	}

	private double getCoord(Point2D.Double item, int coordIndex) {
		switch (coordIndex) {
			case 0: return item.x;
			case 1: return item.y;
			default: throw new RuntimeException("Invalid coordinate");
		}
	}

	private void setCoord(Point2D.Double item, int coordIndex, double value) {
		switch (coordIndex) {
			case 0: 
				item.x = value;
				break;				
			case 1: 
				item.y = value;
				break;
			default: throw new RuntimeException("Invalid coordinate");
		}
	}
	
	public double getSourceCoord(Point2D.Double item, int coordIndex) {
		return getCoord(item, coordIndex);
	}

	public double getTargetCoord(Point2D.Double item, int coordIndex) {
		return getCoord(item, coordIndex);
	}

	public void setSourceCoord(Point2D.Double item, int coordIndex, double value) {
		setCoord(item, coordIndex, value);
	}

	public void setTargetCoord(Point2D.Double item, int coordIndex, double value) {
		setCoord(item, coordIndex, value);
	}
}
