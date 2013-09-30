package com.test.image;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Image {

	private static BufferedImage stretchImage(BufferedImage source, int destWidth, int destHeight) {
		int srcWidth = source.getWidth();
		int srcHeight = source.getHeight();
		
		double scale = Math.min(
				(double) destWidth / srcWidth, 
				(double) destHeight / srcHeight);
		BufferedImage result = new BufferedImage((int)(source.getWidth() * scale), 
				(int)(source.getHeight() * scale),
				source.getType());

		new AffineTransformOp(
				AffineTransform.getScaleInstance(scale, scale),
				AffineTransformOp.TYPE_BICUBIC).filter(source, result);
		return result;
	}

	public static void resizeImage(String sourceFileName, String outputFileName, int newMaxWidth, int newMaxHeight) throws IOException {
		BufferedImage source = ImageIO.read(new File(sourceFileName));
		BufferedImage resized = stretchImage(source, newMaxWidth, newMaxHeight);
		ImageIO.write(resized, "jpg", new File(outputFileName));
		source.flush();
		resized.flush();
	}
	
	public static void main(String[] args) throws IOException {
		resizeImage("C:/Users/monitor-test/HPIM7429.jpg", "C:/Users/monitor-test/HPIM7429_small.JPG", 200, 200);
	}

}
