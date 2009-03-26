package com.slavi.improc.myadjust;

import com.slavi.improc.KeyPoint;
import com.slavi.math.transform.Helmert2DTransformer;

public class KeyPointHelmertTransformer extends Helmert2DTransformer<KeyPoint, KeyPoint>{

	public double getCoord(KeyPoint item, int coordIndex) {
		switch (coordIndex) {
		case 0:
			if (item.keyPointList == null)
				return item.doubleX;
			else 
				return item.doubleX - item.keyPointList.cameraOriginX;
		case 1: 
			if (item.keyPointList == null)
				return item.doubleY;
			else
				return item.doubleY - item.keyPointList.cameraOriginY;
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
			item.doubleX = value;
			if (item.keyPointList != null)
				item.doubleX += item.keyPointList.cameraOriginX; 
			break;
		case 1: 
			item.doubleY = value;
			if (item.keyPointList != null)
				item.doubleY += item.keyPointList.cameraOriginY; 
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
