package com.slavi.parallel.img;

import java.awt.Rectangle;

import com.slavi.img.DGaussianFilter;

/**
 * Parallel apply gaussian filter to an ImageMap.
 */
public class PApplyGaussianFilter1 implements Runnable {

	public static final double defaultSigma = 1.5;
	
	DWindowedImage src; 
	
	DWindowedImage dest;
	
	double sigma;
	
	int maskRadius;

	public double[] mask;

	public double maskSum;
	
	public PApplyGaussianFilter1(DWindowedImage src, DWindowedImage dest, double sigma) {
		this(src, dest, sigma, getMaskRadius(sigma));
	}
	
	public PApplyGaussianFilter1(DWindowedImage src, DWindowedImage dest, double sigma, int maskRadius) {
		this.src = src;
		this.dest = dest;
		this.sigma = sigma;
		this.maskRadius = maskRadius;
		this.maskSum = DGaussianFilter.fillArray(mask, sigma);
	}

	public static int getMaskRadius(double sigma) {
		return 1 + (int) Math.floor(3.0 * sigma);
	}
	
	public static Rectangle getNeededSourceExtent(Rectangle dest, double sigma) {
		return getNeededSourceExtent(dest, getMaskRadius(sigma));
	}

	public static Rectangle getNeededSourceExtent(Rectangle dest, int maskRadius) {
		return new Rectangle(
				dest.x - maskRadius,
				dest.y - maskRadius,
				dest.width + (maskRadius >> 1),
				dest.height + (maskRadius >> 1));
	}

	double[] bufX = null;

	double[] bufY = null;
	
	public void run() {
		int minX = dest.minX();
		int maxX = dest.maxX();
		int minY = dest.minY();
		int maxY = dest.maxY();
		int sizeX = maxX - minX + 1;
		int sizeY = maxY - minY + 1;
		
		for (int i = minX; i <= maxX; i++) {
			int i2 = i << 1;
			for (int j = minY; j <= maxY; j++)
				dest.setPixel(i, j, src.getPixel(i2, j << 1));
		}
/*		
		if ((bufY == null) || (bufY.length != sizeY + mask.length - 1))
			bufY = new double[sizeY + mask.length - 1];
		for (int i = maxX; i >= minX; i--) {
			// fill in the buffer
			int bufYIndx = 0;
			double tmp = src.getPixel(i, 0);
			for (int j = maskRadius - 1; j > 0; j--)
				bufY[bufYIndx++] = tmp;
			for (int j = minY; j <= maxY; j++)
				bufY[bufYIndx++] = src.getPixel(i, j);
			tmp = src.getPixel(i, maxY - 1);
			for (int j = maskRadius - 1; j > 0; j--)
				bufY[bufYIndx++] = tmp;

			// apply mask
			for (int j = src.sizeY - 1; j >= 0; j--) {
				double sum = 0;
				for (int k = mask.length - 1; k >= 0; k--)
					sum += mask[k] * bufY[j + k];
				dest.setPixel(i, j, sum);
			}
		}

		if ((bufX == null) || (bufX.length != src.sizeX + mask.length - 1))
			bufX = new double[src.sizeX + mask.length - 1];
		for (int j = src.sizeY - 1; j >= 0; j--) {
			// fill in the buffer
			int bufXIndx = 0;
			double tmp = dest.getPixel(0, j);
			for (int i = maskRadius - 1; i > 0; i--)
				bufX[bufXIndx++] = tmp;
			for (int i = 0; i < src.sizeX; i++)
				bufX[bufXIndx++] = dest.getPixel(i, j);
			tmp = dest.getPixel(src.sizeX - 1, j);
			for (int i = maskRadius - 1; i > 0; i--)
				bufX[bufXIndx++] = tmp;

			// apply mask
			for (int i = src.sizeX - 1; i >= 0; i--) {
				double sum = 0;
				for (int k = mask.length - 1; k >= 0; k--)
					sum += mask[k] * bufX[i + k];
				dest.setPixel(i, j, sum);
			}
		}
*/		
	}
}
