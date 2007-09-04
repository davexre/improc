package com.slavi.testpackage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageHistogram {
	
	public int avgR, avgG, avgB, avgGRAY;
	
	public ImageHistogram(File fImage) throws IOException {
		this(ImageIO.read(fImage));
	}
	
	public ImageHistogram(BufferedImage bImage) {
		int sizeX = bImage.getWidth();
		int sizeY = bImage.getHeight();
		
		avgR = 0;
		avgG = 0;
		avgB = 0;
		avgGRAY = 0;
		
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				int c = bImage.getRGB(i, j);
				int r = (c >> 16) & 0xff;
				int g = (c >> 8) & 0xff;
				int b = c & 0xff;
								
				avgR += r;
				avgG += g;
				avgB += b;
			}
		int numPixels = sizeX * sizeY;
		avgGRAY = (avgR + avgG + avgB) / (numPixels * 3); 
		avgR /= numPixels;
		avgG /= numPixels;
		avgB /= numPixels;
	}
	
	public String toString() {
		return "Avg(R,G,B,GRAY)\t" + avgR + "\t" + avgG + "\t" + avgB + "\t" + avgGRAY;
	}

	public void fixBrightness(BufferedImage bi, BufferedImage bo, int newAvgGRAY) {
		if (newAvgGRAY <= 0)
			newAvgGRAY = avgGRAY;
		
		int sizeX = bi.getWidth();
		int sizeY = bi.getHeight();
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				int c = bi.getRGB(i, j);
				int r = (c >> 16) & 0xff;
				int g = (c >> 8) & 0xff;
				int b = c & 0xff;

				r = r * newAvgGRAY / avgGRAY;
				g = g * newAvgGRAY / avgGRAY;
				b = b * newAvgGRAY / avgGRAY;
				
				r = Math.min(r, 255); 
				g = Math.min(g, 255); 
				b = Math.min(b, 255); 
				
				c = (r << 16) | (g << 8) | b;
				bo.setRGB(i, j, c);
			}		
	}
	
	public void fixAutoColor(BufferedImage bi, BufferedImage bo) {
		int newAvgR = (avgR + avgR + avgGRAY) / 3;
		int newAvgG = (avgG + avgG + avgGRAY) / 3;
		int newAvgB = (avgB + avgB + avgGRAY) / 3;

//		int newAvgR = (avgR + avgGRAY) / 2;
//		int newAvgG = (avgG + avgGRAY) / 2;
//		int newAvgB = (avgB + avgGRAY) / 2;
		
		int sizeX = bi.getWidth();
		int sizeY = bi.getHeight();
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				int c = bi.getRGB(i, j);
				int r = (c >> 16) & 0xff;
				int g = (c >> 8) & 0xff;
				int b = c & 0xff;

				r = (r * newAvgR) / avgR;
				g = (g * newAvgG) / avgG;
				b = (b * newAvgB) / avgB;
				
				r = Math.min(r, 255); 
				g = Math.min(g, 255); 
				b = Math.min(b, 255); 
				
				c = (r << 16) | (g << 8) | b;
				bo.setRGB(i, j, c);
			}		
	}
	
	public void fixRGB(BufferedImage bi, BufferedImage bo, int newAvgR, int newAvgG, int newAvgB) throws IOException {
		int sizeX = bi.getWidth();
		int sizeY = bi.getHeight();
		
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				int c = bi.getRGB(i, j);
				int r = (c >> 16) & 0xff;
				int g = (c >> 8) & 0xff;
				int b = c & 0xff;

				r = (r * newAvgR) / avgR;
				g = (g * newAvgG) / avgG;
				b = (b * newAvgB) / avgB;
				
				r = Math.min(r, 255); 
				g = Math.min(g, 255); 
				b = Math.min(b, 255); 
				
				c = (r << 16) | (g << 8) | b;
				bo.setRGB(i, j, c);
			}
	}
	
	public static void main(String[] args) throws IOException {
		String thedir = "c:/users/s/java/images/";
		
		File fi1 = new File(thedir + "i1.jpg");
		File fi2 = new File(thedir + "i2.jpg");

		File fo1 = new File(thedir + "i1o.jpg");
		File fo2 = new File(thedir + "i2o.jpg");
		
		BufferedImage bi1 = ImageIO.read(fi1);
		BufferedImage bi2 = ImageIO.read(fi2);
		
		BufferedImage bo1 = new BufferedImage(bi1.getWidth(), bi1.getHeight(), BufferedImage.TYPE_INT_RGB);
		BufferedImage bo2 = new BufferedImage(bi2.getWidth(), bi2.getHeight(), BufferedImage.TYPE_INT_RGB);
		
		ImageHistogram hi1 = new ImageHistogram(bi1);
		ImageHistogram hi2 = new ImageHistogram(bi2);

		System.out.println(hi1);
		System.out.println(hi2);
		
		int newAvgGRAY = (hi1.avgGRAY + hi2.avgGRAY + 100) / 3;
		//int newAvgGRAY = (hi1.avgGRAY + hi2.avgGRAY) / 2;
		
		hi1.fixAutoColor(bi1, bo1);
		hi1.fixBrightness(bo1, bo2, newAvgGRAY);
//		hi2.fixBrightness(bi2, bo2, newAvgGRAY);
		
//		hi1.fixRGB(bi1, bo1, 
//				(hi1.avgR + hi2.avgR) / 2,
//				(hi1.avgG + hi2.avgG) / 2,
//				(hi1.avgB + hi2.avgB) / 2
//				);
//		hi2.fixRGB(bi2, bo2, 
//				(hi1.avgR + hi2.avgR) / 2,
//				(hi1.avgG + hi2.avgG) / 2,
//				(hi1.avgB + hi2.avgB) / 2
//				);
		
		ImageHistogram ho1 = new ImageHistogram(bo1);
		ImageHistogram ho2 = new ImageHistogram(bo2);
		
		System.out.println(ho1);
		System.out.println(ho2);

		ImageIO.write(bo1, "jpg", fo1);
		ImageIO.write(bo2, "jpg", fo2);

		ImageIO.write(bo1, "jpg", new File(thedir + "a/i1o.jpg"));
		ImageIO.write(bo2, "jpg", new File(thedir + "a/i2o.jpg"));
	}
}
