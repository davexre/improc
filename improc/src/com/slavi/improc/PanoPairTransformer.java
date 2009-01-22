package com.slavi.improc;

import com.slavi.math.transform.AffineTransformer;

public class PanoPairTransformer extends AffineTransformer<KeyPoint, KeyPoint> {

	public int getInputSize() {
		return 2;
	}

	public int getOutputSize() {
		return 2;
	}

	private double getCoord(KeyPoint item, int coordIndex) {
		switch (coordIndex) {
		case 0: return item.doubleX;
		case 1: return item.doubleY;
		default: throw new IllegalArgumentException("Index out of range [0..1]");
		}
	}
	
	public double getSourceCoord(KeyPoint item, int coordIndex) {
		return getCoord(item, coordIndex);
	}

	public double getTargetCoord(KeyPoint item, int coordIndex) {
		return getCoord(item, coordIndex);
	}

	public void setSourceCoord(KeyPoint item, int coordIndex, double value) {
		throw new RuntimeException("Method not allowed");
	}

	public void setTargetCoord(KeyPoint item, int coordIndex, double value) {
		throw new RuntimeException("Method not allowed");
	}
}
