package com.slavi.improc.myadjust;

import java.awt.geom.Point2D;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

	private void pinPoints(SafeImage oi) {
		Point2D.Double d = new Point2D.Double();
		// Pin pairs
		for (KeyPointPairList pairList : pairLists) {
			for (KeyPointPair pair : pairList.items) {
				if (!pair.bad) {
					transformCameraToWorld(pair.sourceSP.doubleX, pair.sourceSP.doubleY, pairList.source, d);
					int x1 = (int)d.x;
					int y1 = (int)d.y;
					transformCameraToWorld(pair.targetSP.doubleX, pair.targetSP.doubleY, pairList.target, d);
					int x2 = (int)d.x;
					int y2 = (int)d.y;
					oi.pinPair(x1, y1, x2, y2);
					
				}
				// dump statistic info for pairs
//				System.out.println(
//						MathUtil.d4(pair.discrepancy) + "\t" +	
//						MathUtil.d4(pair.distanceToNearest) + "\t" +	
//						MathUtil.d4(pair.distanceToNearest2) + "\t" +	
//						MathUtil.d4(pair.getMaxDifference()) + "\t"	
//				);
			}
		}
	}
	
	void renderImage(SafeImage oi) throws Exception {
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
					if (useImageColorMasks) {
						color = DWindowedImageUtils.getGrayColor(color);
						color = (color & imageColorMask) | (col2 & (~imageColorMask));
					}
					oi.setRGB(i, j, color);
				}
			}
		}
	}
	
	private int fixColorValue(long color, long count) {
		if (count == 0)
			return 0;
		color /= count;
		if (color <= 0)
			return 0;
		if (color >= 255)
			return 255;
		return (int) color;
	}
	
	void renderImage2(SafeImage oi) throws Exception {
		Map<KeyPointList, SafeImage> imageData = new HashMap<KeyPointList, SafeImage>();
		for (int index = 0; index < images.size(); index++) {
			KeyPointList image = images.get(index);
			SafeImage im = new SafeImage(new FileInputStream(image.imageFileStamp.getFile()));
			imageData.put(image, im);
		}
		
		Point2D.Double d = new Point2D.Double();
		for (int oimgY = 0; oimgY < oi.sizeY; oimgY++) {
			for (int oimgX = 0; oimgX < oi.sizeX; oimgX++) {
				long colorR = 0;
				long colorG = 0;
				long colorB = 0;
				int countR = 0;
				int countG = 0;
				int countB = 0;
				for (int index = 0; index < images.size(); index++) {
					KeyPointList image = images.get(index);
					SafeImage im = imageData.get(image);
					transformWorldToCamera(oimgX, oimgY, image, d);
					int ox = (int)d.x;
					int oy = (int)d.y;
					int color = im.getRGB(ox, oy);
					if (color < 0)
						continue;

					double precision = 1000.0;
					double dx = Math.abs(ox - image.cameraOriginX) / image.cameraOriginX;
					double dy = Math.abs(oy - image.cameraOriginY) / image.cameraOriginY;
//					int weight = 1 + (int) (precision * (1 - MathUtil.hypot(dx, dy)));  
					int weight = 1 + (int) (precision * (1 - Math.max(dx, dy)));  
					
					if (useImageColorMasks) {
						color = weight * DWindowedImageUtils.getGrayColor(color) & 0xff;
						switch (index % 3) {
						case 0:
							colorR += color;
							countR += weight;
							break;
						case 1:
							colorG += color;
							countG += weight;
							break;
						default:
							colorB += color;
							countB += weight;
							break;
						}
					} else {
						countR += weight;
						countG += weight;
						countB += weight;
						colorR += weight * ((color >> 16) & 0xff);
						colorG += weight * ((color >> 8) & 0xff);
						colorB += weight * (color & 0xff);
					}
				}

				int color = 
					(fixColorValue(colorR, countR) << 16) |
					(fixColorValue(colorG, countG) << 8) |
					fixColorValue(colorB, countB);
				oi.setRGB(oimgX, oimgY, color);
			}			
		}
	}

	boolean useImageColorMasks = false;
	int outputImageSizeX = 1000;
	int outputImageSizeY;
	
	public Void call() throws Exception {
		calcExtents();
		
		System.out.println("MIN Angle X,Y:  " + MathUtil.d4(minAngle.x) + "\t" + MathUtil.d4(minAngle.y));
		System.out.println("SIZE angle X,Y: " + MathUtil.d4(sizeAngle.x) + "\t" + MathUtil.d4(sizeAngle.y));
		System.out.println("Size in pixels: " + outputImageSizeX + "\t" + outputImageSizeY);
		
		SafeImage oi = new SafeImage(outputImageSizeX, outputImageSizeY);
		
		renderImage2(oi);
//		pinPoints(oi);
		oi.save();
		return null;
	}
}
