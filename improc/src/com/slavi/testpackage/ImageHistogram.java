package com.slavi.testpackage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageHistogram {
	
	public int avgR, avgG, avgB;
	public int minR, minG, minB;
	public int maxR, maxG, maxB;
	
	public ImageHistogram(File fImage) throws IOException {
		this(ImageIO.read(fImage));
	}
	
	public ImageHistogram(BufferedImage bImage) {
		int sizeX = bImage.getWidth();
		int sizeY = bImage.getHeight();
		
		avgR = 0;
		avgG = 0;
		avgB = 0;
		
		minR = minG = minB = 255;
		maxR = maxG = maxB = 0;
		
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				int c = bImage.getRGB(i, j);
				int r = (c >> 16) & 0xff;
				int g = (c >> 8) & 0xff;
				int b = c & 0xff;
								
				avgR += r;
				avgG += g;
				avgB += b;
				
				if (minR > r) minR = r;
				if (minG > g) minG = g;
				if (minB > b) minB = b;

				if (maxR < r) maxR = r;
				if (maxG < g) maxG = g;
				if (maxB < b) maxB = b;
			}
		int numPixels = sizeX * sizeY;
		avgR /= numPixels;
		avgG /= numPixels;
		avgB /= numPixels;
	}
	
	public String toString() {
		return 
			"Avg(R,G,B)\t" + avgR + "\t" + avgG + "\t" + avgB + 
			"\tMin(R,G,B)\t" + minR + "\t" + minG + "\t" + avgB +
			"\tMax(R,G,B)\t" + maxR + "\t" + maxG + "\t" + maxB;
	}

	public static void fiximg(File fin, File fou, int avgR, int avgG, int avgB) throws IOException {
		BufferedImage bi = ImageIO.read(fin);
		int sizeX = bi.getWidth();
		int sizeY = bi.getHeight();
		ImageHistogram h = new ImageHistogram(bi);
		
		BufferedImage bo = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_RGB);
		
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				int c = bi.getRGB(i, j);
				int r = (c >> 16) & 0xff;
				int g = (c >> 8) & 0xff;
				int b = c & 0xff;

				r = r * avgR / h.avgR;
				g = g * avgG / h.avgG;
				b = b * avgB / h.avgB;
				
				c = (r << 16) | (g << 8) | b;
				bo.setRGB(i, j, c);
			}
		ImageIO.write(bo, "jpg", fou);
	}
	
	public static void main(String[] args) throws IOException {
		File fi1 = new File("c:/users/s/java/images/i1.jpg");
		File fi2 = new File("c:/users/s/java/images/i2.jpg");
		File fo1 = new File("c:/users/s/java/images/o1.jpg");
		File fo2 = new File("c:/users/s/java/images/o2.jpg");
		ImageHistogram hi1 = new ImageHistogram(fi1);
		ImageHistogram hi2 = new ImageHistogram(fi2);
		System.out.println(hi1);
		System.out.println(hi2);
		fiximg(fi1, fo1,
			(hi1.avgR + hi2.avgR) / 2,
			(hi1.avgG + hi2.avgG) / 2,
			(hi1.avgB + hi2.avgB) / 2
			);
		fiximg(fi2, fo2,
				(hi1.avgR + hi2.avgR) / 2,
				(hi1.avgG + hi2.avgG) / 2,
				(hi1.avgB + hi2.avgB) / 2
				);
		ImageHistogram ho1 = new ImageHistogram(fo1);
		System.out.println(ho1);
		ImageHistogram ho2 = new ImageHistogram(fo2);
		System.out.println(ho2);
	}
}
