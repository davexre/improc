package com.slavi.improc.parallel;

import java.awt.Rectangle;

import com.slavi.image.DWindowedImage;

/**
 * Parallel Compute direction of an ImageMap (using DWindowedImage)
 *
 */
public class PComputeDirection {

	/**
	 * Specifies the value for the one-pixel border of the computed magnitude or
	 * direction map. Used by {@link #computeDirection(DWindowedImage, DWindowedImage)} and 
	 * {@link PComputeMagnitude#computeMagnitude(DWindowedImage, DWindowedImage)}.
	 */
	static final double borderColorValue = 0.0;
	
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
		
	public static void computeDirection(DWindowedImage source, DWindowedImage dest) {
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
				dest.setPixel(i, j, borderColorValue);
		// Draw bottom
		for (int j = maxY + 1; j <= dMaxY; j++)
			for (int i = dMinX; i <= dMaxX; i++)
				dest.setPixel(i, j, borderColorValue);
		// Draw left
		for (int i = dMinX; i < minX; i++)
			for (int j = minY; j <= maxY; j++)
				dest.setPixel(i, j, borderColorValue);
		// Draw right
		for (int i = maxX + 1; i <= dMaxX; i++)
			for (int j = minY; j <= maxY; j++)
				dest.setPixel(i, j, borderColorValue);

		// Compute direction
		for (int i = minX; i <= maxX; i++) {
			for (int j = minY; j <= maxY; j++) {
				// Direction is computed as d = atan2( dX, dY )
				// The returned value of atan2 is from -pi to +pi.
				double value = Math.atan2(
					source.getPixel(i, j + 1) - source.getPixel(i, j - 1), 
					source.getPixel(i + 1, j) - source.getPixel(i - 1, j));
				dest.setPixel(i, j, value);
			}
		}
	}
}
