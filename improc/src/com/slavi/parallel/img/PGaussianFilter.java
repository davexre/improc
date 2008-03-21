package com.slavi.parallel.img;

import java.awt.Rectangle;

/**
 * Parallel apply gaussian filter (mathematically correct) to an ImageMap.
 */
public class PGaussianFilter {

	public static final double defaultSigma = 1.5;

	public static double fillArray(double[] dest, double sigma) {
		int maxR = ((dest.length + 1) >> 1) - 1;

		double sigma2sq = -0.5 / (sigma * sigma);
		double normalizeFactor = 1.0 / (Math.sqrt(2.0 * Math.PI) * sigma);
		for (int i = maxR; i >= 0; i--) {
			double G = Math.exp((i * i) * sigma2sq) * normalizeFactor;
			dest[maxR + i] = dest[maxR - i] = G;
		}
		double maskSum = 0;
		for (int i = dest.length - 1; i >= 0; i--)
			maskSum += dest[i];
		return maskSum;
	}
	
	public static int getMaskRadius(double sigma) {
		return 1 + (int) Math.floor(3.0 * sigma);
	}
	
	public static Rectangle getNeededSourceExtent(Rectangle dest) {
		return getNeededSourceExtent(dest, getMaskRadius(defaultSigma));
	}
	
	public static Rectangle getNeededSourceExtent(Rectangle dest, double sigma) {
		return getNeededSourceExtent(dest, getMaskRadius(sigma));
	}

	public static Rectangle getNeededSourceExtent(Rectangle dest, int maskRadius) {
		return new Rectangle(
				dest.x - maskRadius + 1,
				dest.y - maskRadius + 1,
				dest.width + (maskRadius << 1) - 1,
				dest.height + (maskRadius << 1) - 1);
	}

	public static Rectangle getEffectiveTargetExtent(Rectangle source) {
		return getEffectiveTargetExtent(source, getMaskRadius(defaultSigma));
	}
	
	public static Rectangle getEffectiveTargetExtent(Rectangle source, double sigma) {
		return getEffectiveTargetExtent(source, getMaskRadius(sigma));
	}

	public static Rectangle getEffectiveTargetExtent(Rectangle source, int maskRadius) {
		return new Rectangle(
				source.x + maskRadius - 1,
				source.y + maskRadius - 1,
				source.width - (maskRadius << 1) + 1,
				source.height - (maskRadius << 1) + 1);
	}
	
	public static void applyFilter(DWindowedImage source, DWindowedImage dest) {
		applyFilter(source, dest, defaultSigma);
	}

	public static void applyFilter(DWindowedImage source, DWindowedImage dest, double sigma) {
		applyFilter(source, dest, sigma, getMaskRadius(sigma));
	}
	
	public static void applyFilter(DWindowedImage source, DWindowedImage dest, double sigma, int maskRadius) {
		int dMinX = dest.minX();
		int dMaxX = dest.maxX();
		int dMinY = dest.minY();
		int dMaxY = dest.maxY();
		
		int sMinX = source.minX();
		int sMaxX = source.maxX();
		int sMinY = source.minY();
		int sMaxY = source.maxY();
		
		double mask[] = new double[(maskRadius << 1) - 1];
		fillArray(mask, sigma);
		
		for (int i = dMinX; i <= dMaxX; i++) {
			int indPartialX = i - (mask.length >> 1);
			for (int j = dMinY; j <= dMaxY; j++) {
				double sum = 0;
				double msum = 0;
				int indPartialY = j - (mask.length >> 1);
				
				for (int i2 = 0; i2 < mask.length; i2++) {
					int indx = indPartialX + i2;
					if ((indx < sMinX) || (indx > sMaxX)) {
						for (int j2 = 0; j2 < mask.length; j2++) 
							msum += mask[i2] * mask[j2];
					} else {
						for (int j2 = 0; j2 < mask.length; j2++) {
							int indy = indPartialY + j2;
							if ((indy < sMinY) || (indy > sMaxY)) {
								msum += mask[i2] * mask[j2];
							} else {
								sum += source.getPixel(indx, indy) * mask[i2] * mask[j2];
							}
						}
					}
				}
				dest.setPixel(i, j, sum / (1 - msum));
			}
		}
	}
}
