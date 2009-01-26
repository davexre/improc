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

	private void setCoord(KeyPoint item, int coordIndex, double value) {
		switch (coordIndex) {
		case 0: 
			item.doubleX = value;
			break;
		case 1: 
			item.doubleY = value;
			break;
		default: throw new IllegalArgumentException("Index out of range [0..1]");
		}
	}
	
	public void setSourceCoord(KeyPoint item, int coordIndex, double value) {
		setCoord(item, coordIndex, value);
	}

	public void setTargetCoord(KeyPoint item, int coordIndex, double value) {
		setCoord(item, coordIndex, value);
	}
}
