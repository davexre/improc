package com.slavi.parallel.img;

import java.awt.Rectangle;

/**
 * Parallel apply gaussian filter (fast) to an ImageMap.
 */
public class PFastGaussianFilter {
	public static int getMaskRadius(double sigma) {
		return 1 + (int) Math.floor(3.0 * sigma);
	}
	
	public static Rectangle getNeededSourceExtent(Rectangle dest) {
		return getNeededSourceExtent(dest, getMaskRadius(PGaussianFilter.defaultSigma));
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
		return getEffectiveTargetExtent(source, getMaskRadius(PGaussianFilter.defaultSigma));
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
		applyFilter(source, dest, PGaussianFilter.defaultSigma);
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

		int sizeX = dMaxX - dMinX + 1;
		int sizeY = dMaxY - dMinY + 1;
		
		double mask[] = new double[(maskRadius << 1) - 1];
		PGaussianFilter.fillArray(mask, sigma);
		int maxBufSize = Math.max(sizeX, sizeY) + mask.length - 1;
		double buf[] = new double[maxBufSize];
		
		for (int destI = dMinX; destI <= dMaxX; destI++) {
			int srcI = (destI <= sMinX) ? sMinX :
				(destI >= sMaxX) ? sMaxX : destI;
			// fill in the buffer
			double lowValue = source.getPixel(srcI, sMinY);
			double highValue = source.getPixel(srcI, sMaxY);
			int lowIndex = dMinY - maskRadius + 1;
			int highIndex = dMaxY + maskRadius - 1;
			for (int j = lowIndex, bufIndex = 0; j <= highIndex;  j++, bufIndex++) {
				buf[bufIndex] = (j <= sMinY) ? lowValue : 
					(j >= sMaxY) ? highValue : source.getPixel(srcI, j);
			}
			// apply mask
			for (int j = dMinY, bufIndex = 0; j <= dMaxY; j++, bufIndex++) {
				double sum = 0;
				for (int k = mask.length - 1; k >= 0; k--)
					sum += mask[k] * buf[bufIndex + k];
				dest.setPixel(destI, j, sum);
			}
		}

		for (int destJ = dMinY; destJ <= dMaxY; destJ++) {
			int srcJ = (destJ <= sMinY) ? sMinY :
				(destJ >= sMaxY) ? sMaxY : destJ;
			// fill in the buffer
			double lowValue = dest.getPixel(sMinX, srcJ);
			double highValue = dest.getPixel(sMaxX, srcJ);
			int lowIndex = dMinX - maskRadius + 1;
			int highIndex = dMaxX + maskRadius - 1;
			for (int i = lowIndex, bufIndex = 0; i <= highIndex;  i++, bufIndex++) {
				buf[bufIndex] = (i <= sMinX) ? lowValue : 
					(i >= sMaxX) ? highValue : dest.getPixel(i, srcJ);
			}
			// apply mask
			for (int i = dMinX, bufIndex = 0; i <= dMaxX; i++, bufIndex++) {
				double sum = 0;
				for (int k = mask.length - 1; k >= 0; k--)
					sum += mask[k] * buf[bufIndex + k];
				dest.setPixel(i, destJ, sum);
			}
		}
	}
}
