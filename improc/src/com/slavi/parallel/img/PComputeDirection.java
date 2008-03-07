package com.slavi.parallel.img;

import java.awt.Rectangle;

import com.slavi.img.DImageMap;

/**
 * Parallel Compute direction of an ImageMap (using DWindowedImage)
 *
 */
public class PComputeDirection implements Runnable {

	/**
	 * Specifies the value for the one-pixel border of the computed magnitude or
	 * direction map. Used by {@link #computeMagnitude(DImageMap)} and 
	 * {@link #computeDirection(DImageMap)}.
	 */
	static final double borderColorValue = 0;
	
	DWindowedImage src; 
	
	DWindowedImage dest;
	
	boolean initializeDest;
	
	public PComputeDirection(DWindowedImage src, DWindowedImage dest, boolean initializeDest) {
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
				dest.setPixel(i, 0, borderColorValue);
				dest.setPixel(i, maxY, borderColorValue);
			}
			for (int j = minY; j <= maxY; j++) {
				dest.setPixel(0, j, borderColorValue);
				dest.setPixel(maxX, j, borderColorValue);
			}
		}

		for (int i = minX + 1; i < maxX; i++)
			for (int j = minY + 1; j < maxY; j++)
				// Direction is computed as d = atan2( dX, dY )
				// The returned value of atan2 is from -pi to +pi.
				dest.setPixel(i, j, Math.atan2(
					src.getPixel(i, j + 1) - src.getPixel(i, j - 1), 
					src.getPixel(i + 1, j) - src.getPixel(i - 1, j)));
	}
}
