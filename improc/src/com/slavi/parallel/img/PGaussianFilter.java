package com.slavi.parallel.img;

import java.awt.Rectangle;

import com.slavi.img.DGaussianFilter;

public class PGaussianFilter implements Runnable {

	double[] mask;

	int maskRadius;

	double maskSum;

	static final double defaultSigma = 1.5; 
	
	DWindowedImage src; 
	
	DWindowedImage dest;
	
	public PGaussianFilter(DWindowedImage src, DWindowedImage dest) {
		this(src, dest, defaultSigma);
	}

	public PGaussianFilter(DWindowedImage src, DWindowedImage dest, double sigma) {
		this(src, dest, sigma, getMaskRadius(sigma));
	}

	public PGaussianFilter(DWindowedImage src, DWindowedImage dest, double sigma, int maskRadius) {
		this.src = src;
		this.dest = dest;
		this.maskRadius = maskRadius;
		this.mask = new double[(maskRadius << 1) - 1];
		this.maskSum = DGaussianFilter.fillArray(mask, sigma);
	}
	
	public static int getMaskRadius(double sigma) {
		return 1 + (int) Math.floor(3.0 * sigma);
	}

	public static Rectangle getNeededSourceExtent(Rectangle dest) {
		return getNeededSourceExtent(dest, getMaskRadius(defaultSigma));
	}
	
	public static Rectangle getNeededSourceExtent(Rectangle dest, int maskRadius) {
		return new Rectangle(
				dest.x - maskRadius,
				dest.y - maskRadius,
				dest.width + (maskRadius << 1),
				dest.height + (maskRadius << 1));
	}

	public void run() {
		int dMinX = dest.minX();
		int dMaxX = dest.maxX();
		int dMinY = dest.minY();
		int dMaxY = dest.maxY();
		
		int sMinX = src.minX();
		int sMaxX = src.maxX();
		int sMinY = src.minY();
		int sMaxY = src.maxY();
		
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
								sum += src.getPixel(indx, indy) * mask[i2] * mask[j2];
							}
						}
					}
				}
				
				dest.setPixel(i, j, sum / (1 - msum));
			}
		}
	}
}
