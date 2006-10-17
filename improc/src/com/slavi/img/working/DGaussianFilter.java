package com.slavi.img.working;

public class DGaussianFilter {

	public double[] mask;

	public int maskRadius;

	public double maskSum;

	/**
	 * bufX and bufY arrays are used by applyGaussianFilter()
	 */
	public double[] bufX = null;

	public double[] bufY = null;

	public String toString() {
		String result = "Mask length: " + mask.length + "\n";
		String prefix = "";
		for (int i = 0; i < mask.length; i++) {
			result += prefix + mask[i];
			prefix = " ";
		}
		return result + "\n";
	};

	public DGaussianFilter() {
		this(1.5);
	}

	public DGaussianFilter(double sigma) {
		this(sigma, 1 + (int) Math.floor(3 * sigma));
	}

	public DGaussianFilter(double sigma, int MaskRadius) {
		maskRadius = MaskRadius;
		mask = new double[(maskRadius << 1) - 1];
		maskSum = fillArray(mask, sigma);
	}

	public double getMaskSum() {
		return maskSum;
	}

	public double getMaskRadius() {
		return maskRadius;
	}

	public int getSize() {
		return mask.length;
	}

	public static double fillArray(double[] dest, double sigma) {
		int maxR = ((dest.length + 1) >> 1) - 1;

		double sigma2sq = -1 / (2 * Math.pow(sigma, 2));
		double normalizeFactor = 1 / (Math.sqrt(2 * Math.PI) * sigma);
		for (int i = maxR; i >= 0; i--) {
			double G = Math.exp((i * i) * sigma2sq) * normalizeFactor;
			dest[maxR + i] = dest[maxR - i] = G;
		}
		double maskSum = 0;
		for (int i = dest.length - 1; i >= 0; i--)
			maskSum += dest[i];
		return maskSum;
	}

	public void applyGaussianFilter(DImageMap src, DImageMap dest) {
		dest.resize(src.sizeX, src.sizeY);

		if ((bufY == null) || (bufY.length != src.sizeY + mask.length - 1))
			bufY = new double[src.sizeY + mask.length - 1];
		for (int i = src.sizeX - 1; i >= 0; i--) {
			// fill in the buffer
			int bufYIndx = 0;
			double tmp = src.getPixel(i, 0);
			for (int j = maskRadius - 1; j > 0; j--)
				bufY[bufYIndx++] = tmp;
			for (int j = 0; j < src.sizeY; j++)
				bufY[bufYIndx++] = src.getPixel(i, j);
			tmp = src.getPixel(i, src.sizeY - 1);
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
	}

	/////////// OBSOLETE ?!?
	public void applyGaussianFilterOriginal(DImageMap src, DImageMap dest) {
		DImageMap tmp = new DImageMap(src.sizeX, src.sizeY);
		dest.resize(src.sizeX, src.sizeY);

		for (int i = 0; i < src.sizeX; i++)
			for (int j = 0; j < src.sizeY; j++) {
				double sum = 0;
				double msum = 0;
				for (int k = 0; k < mask.length; k++) {
					int indx = j + k - (mask.length >> 1);
					if ((indx >= 0) && (indx < src.sizeY))
						sum += src.getPixel(i, indx) * mask[k];
					else
						msum += mask[k];
				}
				tmp.setPixel(i, j, sum / (1 - msum));
			}

		for (int i = 0; i < src.sizeX; i++)
			for (int j = 0; j < src.sizeY; j++) {
				double sum = 0;
				double msum = 0;
				for (int k = 0; k < mask.length; k++) {
					int indx = i + k - (mask.length >> 1);
					if ((indx >= 0) && (indx < src.sizeX))
						sum += tmp.getPixel(indx, j) * mask[k];
					else
						msum += mask[k];
				}
				dest.setPixel(i, j, sum / (1 - msum));
			}
	}
}
