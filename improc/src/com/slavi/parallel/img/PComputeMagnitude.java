package com.slavi.parallel.img;

import java.awt.Rectangle;

/**
 * Parallel compute magnitude of an ImageMap (using DWindowedImage)
 */
public class PComputeMagnitude implements Runnable {
	DWindowedImage src; 
	
	DWindowedImage dest;
	
	boolean initializeDest;
	
	public PComputeMagnitude(DWindowedImage src, DWindowedImage dest, boolean initializeDest) {
		this.src = src;
		this.dest = dest;
		this.initializeDest = initializeDest;
	}

	public static Rectangle getNeededSourceExtent(Rectangle dest) {
		return new Rectangle(
				dest.x - 1,
				dest.y - 1,
				dest.width + 2,
				dest.height + 2);
	}
	
	public void run() {
		int minX = src.minX();
		int maxX = src.maxX();
		int minY = src.minY();
		int maxY = src.maxY();
		
		if (initializeDest) {
			// Draw a one-pixel border. At this border direction CAN NOT be computed
			// adequately.
			for (int i = minX; i <= maxX; i++) {
				dest.setPixel(i, 0, PComputeDirection.borderColorValue);
				dest.setPixel(i, maxY, PComputeDirection.borderColorValue);
			}
			for (int j = minY; j <= maxY; j++) {
				dest.setPixel(0, j, PComputeDirection.borderColorValue);
				dest.setPixel(maxX, j, PComputeDirection.borderColorValue);
			}
		}

		for (int i = minX + 1; i < maxX; i++)
			for (int j = minY + 1; j < maxY; j++) {
				// Magnitude is computed m = sqrt( dX ^ 2 + dY ^ 2 )
				// Since dX and dY are actually COLORS, i.e. BYTES
				// the maxumum values for dX and dY is 255, so
				// m = sqrt ( 255 ^ 2 + 255 ^ 2 ) = 360.6244...
				// ... so ...
				// Scale the magnitude to fit 0..255 interval
				// The maximum value for magnitude is 360.6244...
				double dX = src.getPixel(i + 1, j) - src.getPixel(i - 1, j);
				double dY = src.getPixel(i, j + 1) - src.getPixel(i, j - 1);
				dest.setPixel(i, j, Math.sqrt(dX * dX + dY * dY));
			}
	}
}
