package com.slavi.improc.myadjust;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import com.slavi.improc.KeyPointList;
import com.slavi.math.MathUtil;
import com.slavi.util.Const;
import com.slavi.util.file.AbsoluteToRelativePathMaker;

public class MyGeneratePanoramas implements Callable<Void> {

	MyPanoPairTransformer3 tr;
	AbsoluteToRelativePathMaker keyPointPairFileRoot;
	ArrayList<KeyPointList> images;
	
	public MyGeneratePanoramas(MyPanoPairTransformer3 tr, AbsoluteToRelativePathMaker keyPointPairFileRoot) {
		this.keyPointPairFileRoot = keyPointPairFileRoot;
		this.tr = tr;
		images = new ArrayList<KeyPointList>();
		images.add(tr.origin);
		MyPanoPairTransformLearner3.buildCamera2RealMatrix(tr.origin);
//		images.addAll(tr.images);
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
		String fouName = Const.tempDir + "/temp.png";
		calcExtents();
		int imgX = 1000;
		int imgY = (int)((max.y - min.y) * imgX / (max.x - min.x));
		
		System.out.println("MIN: " + MathUtil.d4(min.x) + "\t" + MathUtil.d4(min.y));
		System.out.println("MAX: " + MathUtil.d4(max.x) + "\t" + MathUtil.d4(max.y));
		System.out.println("WH : " + imgX + "\t" + imgY);
		
		BufferedImage bi = new BufferedImage(imgX, imgY, BufferedImage.TYPE_INT_RGB);
		
		Point2D.Double d = new Point2D.Double();
		for (KeyPointList image : images) {
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
					
					if (
						(ox < 0) || (ox >= imgX) ||
						(oy < 0) || (oy >= imgY))
						continue;
					bi.setRGB(ox, oy, im.getRGB(i, j));
				}
		}		
		ImageIO.write(bi, "png", new File(fouName));
		return null;
	}
}
