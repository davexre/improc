package com.slavi.improc.myadjust;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.OutputImage;
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
	
	void calcExt(Point2D.Double p) {
		if (p.x < min.x) min.x = p.x;
		if (p.y < min.y) min.y = p.y;
		if (p.x > max.x) max.x = p.x;
		if (p.y > max.y) max.y = p.y;
	}
	
	Point2D.Double min = new Point2D.Double();
	Point2D.Double max = new Point2D.Double();

	void calcExtents() {
		min.x = Double.MAX_VALUE;
		min.y = Double.MAX_VALUE;
		max.x = Double.MIN_VALUE;
		max.y = Double.MIN_VALUE;
		
		for (KeyPointList i : images) {
			i.tl = new Point2D.Double();
			i.tr = new Point2D.Double();
			i.bl = new Point2D.Double();
			i.br = new Point2D.Double();
			
			MyPanoPairTransformer3.transform(0, 0, i, i.tl);
			MyPanoPairTransformer3.transform(0, i.imageSizeY - 1, i, i.tr);
			MyPanoPairTransformer3.transform(i.imageSizeX - 1, 0, i, i.bl);
			MyPanoPairTransformer3.transform(i.imageSizeX - 1, i.imageSizeY - 1, i, i.br);
			
			calcExt(i.tl);
			calcExt(i.tr);
			calcExt(i.bl);
			calcExt(i.br);
		}
	}
	
	public Void call() throws Exception {
		calcExtents();
		int imgX = 1000;
		int imgY = (int)((max.y - min.y) * imgX / (max.x - min.x));
		
		System.out.println("MIN: " + MathUtil.d4(min.x) + "\t" + MathUtil.d4(min.y));
		System.out.println("MAX: " + MathUtil.d4(max.x) + "\t" + MathUtil.d4(max.y));
		System.out.println("WH : " + imgX + "\t" + imgY);
		
		OutputImage oi = new OutputImage(imgX, imgY);
		
		Point2D.Double d = new Point2D.Double();
		for (int index = 0; index < images.size(); index++) {
//		for (int index = images.size() - 1; index >= 0; index--) {
			KeyPointList image = images.get(index);
			BufferedImage im = ImageIO.read(image.imageFileStamp.getFile());
			for (int i = 0; i < im.getWidth(); i++)
				for (int j = 0; j < im.getHeight(); j++) {
					MyPanoPairTransformer3.transform(i, j, image, d);
					d.x -= min.x;
					d.y -= min.y;
					
					d.x *= imgX / (max.x - min.x);
					d.y *= imgY / (max.y - min.y);
					
					int ox = (int)d.x;
					int oy = (int)d.y;
					
					oi.setRGB(ox, oy, im.getRGB(i, j));
				}
		}
		
		// Pin pairs
		for (KeyPointPairList pairList : pairLists) {
			for (KeyPointPair pair : pairList.items.values()) {
				if (!pair.bad) {
					MyPanoPairTransformer3.transform(
							pair.sourceSP.doubleX, pair.sourceSP.doubleY, 
							pair.sourceSP.keyPointList, d);
					d.x -= min.x;
					d.y -= min.y;
					
					d.x *= imgX / (max.x - min.x);
					d.y *= imgY / (max.y - min.y);
					
					int x1 = (int)d.x;
					int y1 = (int)d.y;
					
					MyPanoPairTransformer3.transform(
							pair.targetSP.doubleX, pair.targetSP.doubleY, 
							pair.targetSP.keyPointList, d);
					d.x -= min.x;
					d.y -= min.y;
					
					d.x *= imgX / (max.x - min.x);
					d.y *= imgY / (max.y - min.y);
					
					int x2 = (int)d.x;
					int y2 = (int)d.y;

					oi.pinPair(x1, y1, x2, y2);
				}
			}
		}
		oi.close();
		return null;
	}
}
