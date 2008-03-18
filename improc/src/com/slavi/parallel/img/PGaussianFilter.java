package com.slavi.parallel.img;

import java.awt.Rectangle;

import com.slavi.img.DGaussianFilter;

/**
 * Parallel apply gaussian filter (fast) to an ImageMap.
 */
public class PGaussianFilter implements Runnable {

	public static final double defaultSigma = 1.5;
	
	DWindowedImage src; 
	
	DWindowedImage dest;
	
	double sigma;
	
	int maskRadius;

	double[] mask;

	double maskSum;
	
	boolean fastFilter;
	
	public PGaussianFilter(DWindowedImage src, DWindowedImage dest) {
		this(src, dest, defaultSigma);
	}

	public PGaussianFilter(DWindowedImage src, DWindowedImage dest, double sigma) {
		this(src, dest, sigma, getMaskRadius(sigma), true);
	}
	
	/**
	 * @param fastFilter
	 *            Default True - Apply fast gaussian filter. False - apply
	 *            mathematically correct gaussian filter
	 */
	public PGaussianFilter(DWindowedImage src, DWindowedImage dest, double sigma, int maskRadius, boolean fastFilter) {
		this.src = src;
		this.dest = dest;
		this.sigma = sigma;
		this.fastFilter = fastFilter;
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

	double[] buf = null;
	
	private void doMathematicallyCorrectGaussianFilter() {
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
	
	int dMinX;
	int dMaxX;
	int dMinY;
	int dMaxY;
	
	int sMinX;
	int sMaxX;
	int sMinY;
	int sMaxY;
	
	private double getBlurredY(int atX, int atY) {
		if (atX <= sMinX)
			atX = sMinX;
		else if (atX >= sMaxX)
			atX = sMaxX;
		
		double result = 0.0;
		for (int k = 0, j = atY - maskRadius + 1; k < mask.length; k++, j++) {
			double tmp;
			if (j <= sMinY)
				tmp = src.getPixel(atX, sMinY);
			else if (j >= sMaxY)
				tmp = src.getPixel(atX, sMaxY);
			else
				tmp = src.getPixel(atX, j);
			result += mask[k] * tmp;
		}
		return result;
	}

	private void doFastGaussianFilter() {
		dMinX = dest.minX();
		dMaxX = dest.maxX();
		dMinY = dest.minY();
		dMaxY = dest.maxY();
		
		sMinX = src.minX();
		sMaxX = src.maxX();
		sMinY = src.minY();
		sMaxY = src.maxY();

		int sizeX = dMaxX - dMinX + 1;
		int sizeY = dMaxY - dMinY + 1;
		
		int maxBufSize = Math.max(sizeX, sizeY) + mask.length - 1;
		if ((buf == null) || (buf.length < maxBufSize))
			buf = new double[maxBufSize];
		
		for (int destI = dMinX; destI <= dMaxX; destI++) {
			int srcI = (destI <= sMinX) ? sMinX :
				(destI >= sMaxX) ? sMaxX : destI;
			// fill in the buffer
			double lowValue = src.getPixel(srcI, sMinY);
			double highValue = src.getPixel(srcI, sMaxY);
			int lowIndex = dMinY - maskRadius + 1;
			int highIndex = dMaxY + maskRadius - 1;
			for (int j = lowIndex, bufIndex = 0; j <= highIndex;  j++, bufIndex++) {
				buf[bufIndex] = (j <= sMinY) ? lowValue : 
					(j >= sMaxY) ? highValue : src.getPixel(srcI, j);
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
			// fill in the buffer
			int lowIndex = dMinX - maskRadius + 1;
			int highIndex = dMaxX + maskRadius - 1;
			for (int i = lowIndex, bufIndex = 0; i <= highIndex;  i++, bufIndex++) {
				buf[bufIndex] = (i <= dMinX) || (i >= dMaxX) ? 
						getBlurredY(i, destJ) : dest.getPixel(i, destJ);
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
	
	public void run() {
		if (fastFilter)
			doFastGaussianFilter();
		else
			doMathematicallyCorrectGaussianFilter();
	}
}
