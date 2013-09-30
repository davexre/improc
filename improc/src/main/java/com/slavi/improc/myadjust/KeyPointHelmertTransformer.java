package com.slavi.improc.myadjust;

import com.slavi.improc.KeyPoint;
import com.slavi.math.transform.Helmert2DTransformer;

public class KeyPointHelmertTransformer extends Helmert2DTransformer<KeyPoint, KeyPoint>{

	public double getCoord(KeyPoint item, int coordIndex) {
		switch (coordIndex) {
		case 0:
			if (item.getKeyPointList() == null)
				return item.getDoubleX();
			else 
				return item.getDoubleX() - item.getKeyPointList().cameraOriginX;
		case 1: 
			if (item.getKeyPointList() == null)
				return item.getDoubleY();
			else
				return item.getDoubleY() - item.getKeyPointList().cameraOriginY;
		default:
			throw new IllegalArgumentException();
		}
	}
	
	public double getSourceCoord(KeyPoint item, int coordIndex) {
		return getCoord(item, coordIndex);
	}

	public double getTargetCoord(KeyPoint item, int coordIndex) {
		return getCoord(item, coordIndex);
	}

	public void setCoord(KeyPoint item, int coordIndex, double value) {
		switch (coordIndex) {
		case 0: 
			if (item.getKeyPointList() == null)
				item.setDoubleX(value);
			else
				item.setDoubleX(value + item.getKeyPointList().cameraOriginX); 
			break;
		case 1: 
			if (item.getKeyPointList() == null)
				item.setDoubleY(value);
			else
				item.setDoubleY(value + item.getKeyPointList().cameraOriginY); 
			break;
		default:
			throw new IllegalArgumentException();
		}
	}
	
	public void setSourceCoord(KeyPoint item, int coordIndex, double value) {
		setCoord(item, coordIndex, value);
	}

	public void setTargetCoord(KeyPoint item, int coordIndex, double value) {
		setCoord(item, coordIndex, value);
	}
}
