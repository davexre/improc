package com.slavi.improc.myadjust;

import java.util.ArrayList;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.math.transform.BaseTransformer;

public class MyPanoPairTransformer extends BaseTransformer<KeyPoint, MyPoint3D> {

	ArrayList<KeyPointList> images;
	
	KeyPointList origin;
	
	public MyPanoPairTransformer(KeyPointList origin, ArrayList<KeyPointList> images) {
		this.images = images;
		this.origin = origin;
	}
	
	public int getInputSize() {
		return 2;
	}
	
	public int getOutputSize() {
		return 3;
	}

	public int getNumberOfCoefsPerCoordinate() {
		return images.size() * 4 + 1;
	}

	public double getSourceCoord(KeyPoint item, int coordIndex) {
		switch (coordIndex) {
		case 0: return item.doubleX;
		case 1: return item.doubleY;
		default:
			throw new IllegalArgumentException();
		}
	}

	public double getTargetCoord(MyPoint3D item, int coordIndex) {
		switch (coordIndex) {
		case 0: return item.x;
		case 1: return item.y;
		case 2: return item.z;
		default:
			throw new IllegalArgumentException();
		}
	}

	public void setSourceCoord(KeyPoint item, int coordIndex, double value) {
		switch (coordIndex) {
		case 0: item.doubleX = value; break;
		case 1: item.doubleY = value; break;
		default:
			throw new IllegalArgumentException();
		}
	}

	public void setTargetCoord(MyPoint3D item, int coordIndex, double value) {
		switch (coordIndex) {
		case 0: item.x = value; break;
		case 1: item.y = value; break;
		case 2: item.z = value; break;
		default:
			throw new IllegalArgumentException();
		}
	}

	public void transform(KeyPoint source, MyPoint3D dest) {
		throw new RuntimeException("not implemented");
	}
	
	public void transform(KeyPoint source, KeyPointList srcImage, MyPoint3D dest) {
		dest.x = 
			source.doubleX * srcImage.camera2real.getItem(0, 0) +
			source.doubleY * srcImage.camera2real.getItem(1, 0) +
			srcImage.scaleZ * srcImage.camera2real.getItem(2, 0);
		dest.y = 
			source.doubleX * srcImage.camera2real.getItem(0, 1) +
			source.doubleY * srcImage.camera2real.getItem(1, 1) +
			srcImage.scaleZ * srcImage.camera2real.getItem(2, 1);
		dest.z = 
			source.doubleX * srcImage.camera2real.getItem(0, 2) +
			source.doubleY * srcImage.camera2real.getItem(1, 2) +
			srcImage.scaleZ * srcImage.camera2real.getItem(2, 2);
	}
}
