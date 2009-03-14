package com.slavi.improc.myadjust;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.math.transform.BaseTransformer;

public class MyPanoPairTransformer3 extends BaseTransformer<KeyPoint, Point2D.Double> {

	ArrayList<KeyPointList> images;
	
	KeyPointList origin;
	
	public MyPanoPairTransformer3(KeyPointList origin, ArrayList<KeyPointList> images) {
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

	public double getTargetCoord(Point2D.Double item, int coordIndex) {
		switch (coordIndex) {
		case 0: return item.x;
		case 1: return item.y;
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

	public void setTargetCoord(Point2D.Double item, int coordIndex, double value) {
		switch (coordIndex) {
		case 0: item.x = value; break;
		case 1: item.y = value; break;
		default:
			throw new IllegalArgumentException();
		}
	}

	public void transform(KeyPoint source, Point2D.Double dest) {
		transform(source, source.keyPointList, dest);
	}
	
	public void transform(KeyPoint source, KeyPointList srcImage, Point2D.Double dest) {
		double sx = (source.doubleX - srcImage.cameraOriginX) * srcImage.cameraScale;
		double sy = (source.doubleY - srcImage.cameraOriginY) * srcImage.cameraScale;
		double sz = srcImage.scaleZ;
		
		double x = 
			sx * srcImage.camera2real.getItem(0, 0) +
			sy * srcImage.camera2real.getItem(1, 0) +
			sz * srcImage.camera2real.getItem(2, 0);
		double y = 
			sx * srcImage.camera2real.getItem(0, 1) +
			sy * srcImage.camera2real.getItem(1, 1) +
			sz * srcImage.camera2real.getItem(2, 1);
		double z = 
			sx * srcImage.camera2real.getItem(0, 2) +
			sy * srcImage.camera2real.getItem(1, 2) +
			sz * srcImage.camera2real.getItem(2, 2);
		
		dest.x = Math.atan2(x, z);
		dest.y = Math.atan2(y, z);
	}
	
	public void transform3D(KeyPoint source, KeyPointList srcImage, MyPoint3D dest) {
		double sx = (source.doubleX - srcImage.cameraOriginX) * srcImage.cameraScale;
		double sy = (source.doubleY - srcImage.cameraOriginY) * srcImage.cameraScale;
		double sz = srcImage.scaleZ;
		
		dest.x = 
			sx * srcImage.camera2real.getItem(0, 0) +
			sy * srcImage.camera2real.getItem(1, 0) +
			sz * srcImage.camera2real.getItem(2, 0);
		dest.y = 
			sx * srcImage.camera2real.getItem(0, 1) +
			sy * srcImage.camera2real.getItem(1, 1) +
			sz * srcImage.camera2real.getItem(2, 1);
		dest.z = 
			sx * srcImage.camera2real.getItem(0, 2) +
			sy * srcImage.camera2real.getItem(1, 2) +
			sz * srcImage.camera2real.getItem(2, 2);
	}
}
