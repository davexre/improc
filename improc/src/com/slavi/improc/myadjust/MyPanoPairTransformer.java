package com.slavi.improc.myadjust;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.math.transform.BaseTransformer;

public class MyPanoPairTransformer extends BaseTransformer<KeyPoint, Point2D.Double> {

	public ArrayList<KeyPointList> images;
	
	public KeyPointList origin;
	
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
		return images.size() * 4;
	}

	public double getSourceCoord(KeyPoint item, int coordIndex) {
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

	public void setTargetCoord(Point2D.Double item, int coordIndex, double value) {
		switch (coordIndex) {
		case 0: item.x = value; break;
		case 1: item.y = value; break;
		default:
			throw new IllegalArgumentException();
		}
	}

	public void transform(KeyPoint source, Point2D.Double dest) {
		transform(source.doubleX, source.doubleY, source.keyPointList, dest);
	}
	
	///////////////////////////////
	
	/**
	 * Transforms from source image coordinate system into world coord.system.
	 * @param sx, sy	Coordinates in pixels of the source image with origin pixel(0,0)
	 * @param dest		The transformed coordinates in radians. Longitude is 
	 * 					returned in dest.x and is in the range (-pi; pi] and Latitude
	 * 					is returned in dest.y in the range [-pi/2; pi/2].    
	 */
	public static void transform(double sx, double sy, KeyPointList srcImage, Point2D.Double dest) {
		sx = (sx - srcImage.cameraOriginX) * srcImage.cameraScale;
		sy = (sy - srcImage.cameraOriginY) * srcImage.cameraScale;
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
		
		double d = Math.sqrt(x*x + z*z);
		dest.x = Math.atan2(x, z);
		dest.y = Math.atan2(y, d);
	}

	public static void transformBackward(double rx, double ry, KeyPointList srcImage, Point2D.Double dest) {
		double d = Math.cos(ry);
		double sx = d * Math.sin(rx);
		double sy = Math.sin(ry);
		double sz = d * Math.cos(rx);
		
		double x = 
			sx * srcImage.camera2real.getItem(0, 0) +
			sy * srcImage.camera2real.getItem(0, 1) +
			sz * srcImage.camera2real.getItem(0, 2);
		double y = 
			sx * srcImage.camera2real.getItem(1, 0) +
			sy * srcImage.camera2real.getItem(1, 1) +
			sz * srcImage.camera2real.getItem(1, 2);
		double z = 
			sx * srcImage.camera2real.getItem(2, 0) +
			sy * srcImage.camera2real.getItem(2, 1) +
			sz * srcImage.camera2real.getItem(2, 2);
		
		if (z == 0) {
			dest.x = Double.NaN;
			dest.y = Double.NaN;
			return;
		}
		x = srcImage.scaleZ * (x / z);
		y = srcImage.scaleZ * (y / z);
		
		dest.x = (x / srcImage.cameraScale) + srcImage.cameraOriginX;
		dest.y = (y / srcImage.cameraScale) + srcImage.cameraOriginY;
	}
	
	public static void transform3D(KeyPoint source, KeyPointList srcImage, MyPoint3D dest) {
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
