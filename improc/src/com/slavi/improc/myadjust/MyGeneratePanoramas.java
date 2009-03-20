package com.slavi.improc.myadjust;

import java.awt.geom.Point2D;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.slavi.image.DWindowedImageUtils;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.SafeImage;
import com.slavi.math.MathUtil;
import com.slavi.util.file.AbsoluteToRelativePathMaker;

public class MyGeneratePanoramas implements Callable<Void> {

	AbsoluteToRelativePathMaker keyPointPairFileRoot;
	ArrayList<KeyPointList> images;
	ArrayList<KeyPointPairList> pairLists;
	
	public MyGeneratePanoramas(ArrayList<KeyPointList> images,
			ArrayList<KeyPointPairList> pairLists,
			AbsoluteToRelativePathMaker keyPointPairFileRoot) {
		this.keyPointPairFileRoot = keyPointPairFileRoot;
		this.pairLists = pairLists;
		this.images = images;
	}
	
	static void calcExt(Point2D.Double p, Point2D.Double min, Point2D.Double max) {
		if (p.x < min.x) 
			min.x = p.x;
		if (p.y < min.y) 
			min.y = p.y;
		if (p.x > max.x) 
			max.x = p.x;
		if (p.y > max.y) 
			max.y = p.y;
	}
	
	Point2D.Double minAngle = new Point2D.Double();
	Point2D.Double sizeAngle = new Point2D.Double();

	void calcExtents() {
		minAngle.x = Double.POSITIVE_INFINITY;
		minAngle.y = Double.POSITIVE_INFINITY;
		sizeAngle.x = Double.NEGATIVE_INFINITY;
		sizeAngle.y = Double.NEGATIVE_INFINITY;
		
		Point2D.Double tmp = new Point2D.Double();
		for (KeyPointList i : images) {
			MyPanoPairTransformer3.transform(0, 0, i, tmp);
			calcExt(tmp, minAngle, sizeAngle);
			MyPanoPairTransformer3.transform(0, i.imageSizeY - 1, i, tmp);
			calcExt(tmp, minAngle, sizeAngle);
			MyPanoPairTransformer3.transform(i.imageSizeX - 1, 0, i, tmp);
			calcExt(tmp, minAngle, sizeAngle);
			MyPanoPairTransformer3.transform(i.imageSizeX - 1, i.imageSizeY - 1, i, tmp);
			calcExt(tmp, minAngle, sizeAngle);
		}
		sizeAngle.x -= minAngle.x;
		sizeAngle.y -= minAngle.y;
		outputImageSizeX = 1000;
		outputImageSizeY = (int)(outputImageSizeX * (sizeAngle.y / sizeAngle.x));
		
		for (KeyPointList i : images) {
			i.min = new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
			i.max = new Point2D.Double(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

			transformCameraToWorld(0, 0, i, tmp);
			calcExt(tmp, i.min, i.max);
			transformCameraToWorld(0, i.imageSizeY - 1, i, tmp);
			calcExt(tmp, i.min, i.max);
			transformCameraToWorld(i.imageSizeX - 1, 0, i, tmp);
			calcExt(tmp, i.min, i.max);
			transformCameraToWorld(i.imageSizeX - 1, i.imageSizeY - 1, i, tmp);
			calcExt(tmp, i.min, i.max);
		}
	}
	
	int outputImageSizeX;
	int outputImageSizeY;
	
	private void transformWorldToCamera(double x, double y, KeyPointList image, Point2D.Double dest) {
		x = sizeAngle.x * (x / outputImageSizeX) + minAngle.x;
		y = sizeAngle.y * (y / outputImageSizeY) + minAngle.y;
		MyPanoPairTransformer3.transformBackward(x, y, image, dest);
	}
	
	private void transformCameraToWorld(double x, double y, KeyPointList image, Point2D.Double dest) {
		MyPanoPairTransformer3.transform(x, y, image, dest);
		dest.x = outputImageSizeX * ((dest.x - minAngle.x) / sizeAngle.x);
		dest.y = outputImageSizeY * ((dest.y - minAngle.y) / sizeAngle.y);
	}
	
	public Void call() throws Exception {
		calcExtents();
		
		System.out.println("MIN Angle X,Y:  " + MathUtil.d4(minAngle.x) + "\t" + MathUtil.d4(minAngle.y));
		System.out.println("SIZE angle X,Y: " + MathUtil.d4(sizeAngle.x) + "\t" + MathUtil.d4(sizeAngle.y));
		System.out.println("Size in pixels: " + outputImageSizeX + "\t" + outputImageSizeY);
		
		SafeImage oi = new SafeImage(outputImageSizeX, outputImageSizeY);
		
		Point2D.Double d = new Point2D.Double();
		for (int index = 0; index < images.size(); index++) {
			KeyPointList image = images.get(index);
			SafeImage im = new SafeImage(new FileInputStream(image.imageFileStamp.getFile()));
			int imageColorMask = 0xff << ((index % 3) * 8);
			
			for (int i = (int)image.min.x; i < image.max.x; i++) {
				for (int j = (int)image.min.y; j < image.max.y; j++) {
					transformWorldToCamera(i, j, image, d);
					int ox = (int)d.x;
					int oy = (int)d.y;
					int color = im.getRGB(ox, oy);
					if (color < 0)
						continue;
					int col2 = oi.getRGB(i, j);
					if (col2 < 0)
						continue;
					color = DWindowedImageUtils.getGrayColor(color);
					color = (color & imageColorMask) | (col2 & (~imageColorMask));
					oi.setRGB(i, j, color);
				}
			}
		}

		// Pin pairs
		for (KeyPointPairList pairList : pairLists) {
			for (KeyPointPair pair : pairList.items.values()) {
				if (!pair.bad) {
					transformCameraToWorld(pair.sourceSP.doubleX, pair.sourceSP.doubleY, pairList.source, d);
					int x1 = (int)d.x;
					int y1 = (int)d.y;
					transformCameraToWorld(pair.targetSP.doubleX, pair.targetSP.doubleY, pairList.target, d);
					int x2 = (int)d.x;
					int y2 = (int)d.y;
					oi.pinPair(x1, y1, x2, y2);
				}
			}
		}
		oi.save();
		return null;
	}
}
