package com.slavi.parallel.img;

import java.awt.Rectangle;

/**
 * Parallel compute magnitude of an ImageMap (using DWindowedImage)
 */
public class PComputeMagnitude {
	public static Rectangle getNeededSourceExtent(Rectangle dest) {
		return new Rectangle(
				dest.x - 1,
				dest.y - 1,
				dest.width + 2,
				dest.height + 2);
	}

	public static Rectangle getEffectiveTargetExtent(Rectangle source) {
		return new Rectangle(
				source.x + 1,
				source.y + 1,
				source.width - 2,
				source.height - 2);
	}
	
	public static void computeMagnitude(DWindowedImage source, DWindowedImage dest) {
		int dMinX = dest.minX();
		int dMaxX = dest.maxX();
		int dMinY = dest.minY();
		int dMaxY = dest.maxY();
		
		int sMinX = source.minX();
		int sMaxX = source.maxX();
		int sMinY = source.minY();
		int sMaxY = source.maxY();

		int minX = Math.max(dMinX, sMinX + 1);
		int maxX = Math.min(dMaxX, sMaxX - 1);
		int minY = Math.max(dMinY, sMinY + 1);
		int maxY = Math.min(dMaxY, sMaxY - 1);

		// Draw a border where values can not be adequately computed.
		// Draw top 
		for (int j = dMinY; j < minY; j++)
			for (int i = dMinX; i <= dMaxX; i++)
				dest.setPixel(i, j, PComputeDirection.borderColorValue);
		// Draw bottom
		for (int j = maxY + 1; j <= dMaxY; j++)
			for (int i = dMinX; i <= dMaxX; i++)
				dest.setPixel(i, j, PComputeDirection.borderColorValue);
		// Draw left
		for (int i = dMinX; i < minX; i++)
			for (int j = minY; j <= maxY; j++)
				dest.setPixel(i, j, PComputeDirection.borderColorValue);
		// Draw right
		for (int i = maxX + 1; i <= dMaxX; i++)
			for (int j = minY; j <= maxY; j++)
				dest.setPixel(i, j, PComputeDirection.borderColorValue);

		// Compute magnitude
		for (int i = minX; i <= maxX; i++) {
			for (int j = minY; j <= maxY; j++) {
				// Magnitude is computed m = sqrt( dX ^ 2 + dY ^ 2 )
				// Since dX and dY are actually COLORS, i.e. BYTES
				// the maxumum values for dX and dY is 255, so
				// m = sqrt ( 255 ^ 2 + 255 ^ 2 ) = 360.6244...
				// ... so ...
				// Scale the magnitude to fit 0..255 interval
				// The maximum value for magnitude is 360.6244...
				double dX = source.getPixel(i + 1, j) - source.getPixel(i - 1, j);
				double dY = source.getPixel(i, j + 1) - source.getPixel(i, j - 1);
				double value = Math.sqrt(dX * dX + dY * dY);
				dest.setPixel(i, j, value);
			}
		}
	}
}
