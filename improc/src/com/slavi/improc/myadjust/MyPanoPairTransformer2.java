package com.slavi.improc.myadjust;

import java.util.ArrayList;

import com.slavi.improc.KeyPointList;
import com.slavi.improc.PanoPair;
import com.slavi.math.transform.BaseTransformer;

public class MyPanoPairTransformer2 extends BaseTransformer<PanoPair, MyPoint3D> {

	public ArrayList<KeyPointList> images;
	
	public KeyPointList origin;
	
	public MyPanoPairTransformer2(KeyPointList origin, ArrayList<KeyPointList> images) {
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

	public double getSourceCoord(PanoPair item, int coordIndex) {
		switch (coordIndex) {
		case 0: return item.sx;
		case 1: return item.sy;
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

	public void setSourceCoord(PanoPair item, int coordIndex, double value) {
		switch (coordIndex) {
		case 0: item.sx = value; break;
		case 1: item.sy = value; break;
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

	public void transform(PanoPair source, MyPoint3D dest) {
		throw new RuntimeException("No implemented");
	}
	
	public void transformToOrigin(MyPoint3D source, KeyPointList srcImage, MyPoint3D dest) {
		dest.x = 
			source.x * srcImage.camera2real.getItem(0, 0) +
			source.y * srcImage.camera2real.getItem(1, 0) +
			srcImage.scaleZ * srcImage.camera2real.getItem(2, 0);
		dest.y = 
			source.x * srcImage.camera2real.getItem(0, 1) +
			source.y * srcImage.camera2real.getItem(1, 1) +
			srcImage.scaleZ * srcImage.camera2real.getItem(2, 1);
		dest.z = 
			source.x * srcImage.camera2real.getItem(0, 2) +
			source.y * srcImage.camera2real.getItem(1, 2) +
			srcImage.scaleZ * srcImage.camera2real.getItem(2, 2);
	}
}
