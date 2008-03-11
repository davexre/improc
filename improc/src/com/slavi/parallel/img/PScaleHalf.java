package com.slavi.parallel.img;

import java.awt.Rectangle;

/**
 * Parallel scales down this image by half into the dest image.
 */
public class PScaleHalf implements Runnable {
	DWindowedImage src; 
	
	DWindowedImage dest;
	
	public PScaleHalf(DWindowedImage src, DWindowedImage dest) {
		this.src = src;
		this.dest = dest;
	}

	public static Rectangle getNeededSourceExtent(Rectangle dest) {
		return new Rectangle(
				dest.x << 1,
				dest.y << 1,
				dest.width << 1,
				dest.height << 1);
	}
	
	public void run() {
		int minX = dest.minX();
		int maxX = dest.maxX();
		int minY = dest.minY();
		int maxY = dest.maxY();
		
		for (int i = minX; i <= maxX; i++) {
			int i2 = i << 1;
			for (int j = minY; j <= maxY; j++)
				dest.setPixel(i, j, src.getPixel(i2, j << 1));
		}
	}
}
