package com.slavi.improc.working;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class DImageMap {
	protected int sizeX;

	protected int sizeY;

	protected double[][] pixel;

	/**
	 * Specifies the value for the one-pixel border of the computed magnitude or
	 * direction map. Used by computeMagnitude and computeDirection.
	 */
	private static final double borderColorValue = 0;

	public String toString() {
		double sum = 0;
		for (int i = this.sizeX - 1; i >= 0; i--)
			for (int j = this.sizeY - 1; j >= 0; j--)
				sum += this.getPixel(i, j);

		return String.format(
				"Min: %.5f\n" +
				"Max: %.5f\n" +
				"Sum: %.5f\n", new Object[] { 
						new Double(this.min()), 
						new Double(this.max()), 
						new Double(sum) } );
	}

	public int getSizeX() {
		return sizeX;
	}

	public int getSizeY() {
		return sizeY;
	}

	public double getPixel(int atX, int atY) {
		return pixel[atX][atY];
	}

	public void setPixel(int atX, int atY, double aValue) {
		pixel[atX][atY] = aValue;
	}

	public DImageMap(int aSizeX, int aSizeY) {
		super();
		sizeX = 0;
		sizeY = 0;
		resize(aSizeX, aSizeY);
	}

	public DImageMap(File fImage) throws IOException {
		BufferedImage bi = ImageIO.read(fImage);
		resize(bi.getWidth(), bi.getHeight());
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				int c = bi.getRGB(i, j);
				pixel[i][j] = (
						((c >> 16) & 0xff) + 
						((c >> 8) & 0xff) + 
						 (c & 0xff)) / (3.0 * 255.0);
			}
	}

	public void resize(int newSizeX, int newSizeY) {
		if ((newSizeX == sizeX) && (newSizeY == sizeY))
			return;
		if ((newSizeX <= 0) || (newSizeY <= 0))
			throw new IllegalArgumentException("Invalid size parameter");
		sizeX = newSizeX;
		sizeY = newSizeY;
		pixel = new double[sizeX][sizeY];
	}

	public void scaleHalf(DImageMap dest) {
		dest.resize(sizeX >> 1, sizeY >> 1);
		for (int i = dest.sizeX - 1; i >= 0; i--) {
			int i2 = i << 1;
			for (int j = dest.sizeY - 1; j >= 0; j--)
				dest.pixel[i][j] = pixel[i2][j << 1];
		}
	}

	public void scaleDouble(DImageMap dest) {
		dest.resize(sizeX << 1, sizeY << 1);
		for (int i = sizeX - 1; i >= 0; i--) {
			int i2 = i << 1;
			for (int j = sizeY - 1; j >= 0; j--) {
				int j2 = j << 1;
				dest.pixel[i2    ][j2    ] = 
				dest.pixel[i2 + 1][j2    ] = 
				dest.pixel[i2    ][j2 + 1] = 
				dest.pixel[i2 + 1][j2 + 1] = pixel[i][j];
			}
		}
	}

	public void computeMagnitude(DImageMap dest) {
		dest.resize(sizeX, sizeY);
		// Draw a one-pixel border. At this border magnutde CAN NOT be computed
		// adequately.
		for (int i = sizeX - 1; i >= 0; i--) {
			dest.setPixel(i, 0, borderColorValue);
			dest.setPixel(i, sizeY - 1, borderColorValue);
		}
		for (int j = sizeY - 1; j >= 0; j--) {
			dest.setPixel(0, j, borderColorValue);
			dest.setPixel(sizeX - 1, j, borderColorValue);
		}
		for (int i = sizeX - 2; i > 0; i--)
			for (int j = sizeY - 2; j > 0; j--) {
				// Magnitude is computed m = sqrt( dX ^ 2 + dY ^ 2 )
				// Since dX and dY are actually COLORS, i.e. BYTES
				// the maxumum values for dX and dY is 255, so
				// m = sqrt ( 255 ^ 2 + 255 ^ 2 ) = 360.6244...
				// ... so ...
				// Scale the magnitude to fit 0..255 interval
				// The maximum value for magnitude is 360.6244...
				dest.setPixel(i, j, Math.sqrt(
					Math.pow(getPixel(i + 1, j) - getPixel(i - 1, j), 2) +
					Math.pow(getPixel(i, j + 1) - getPixel(i, j - 1), 2)));
			}
	}

	public void computeDirection(DImageMap dest) {
		dest.resize(sizeX, sizeY);
		// Draw a one-pixel border. At this border direction CAN NOT be computed
		// adequately.
		for (int i = sizeX - 1; i >= 0; i--) {
			dest.setPixel(i, 0, borderColorValue);
			dest.setPixel(i, sizeY - 1, borderColorValue);
		}
		for (int j = sizeY - 1; j >= 0; j--) {
			dest.setPixel(0, j, borderColorValue);
			dest.setPixel(sizeX - 1, j, borderColorValue);
		}

		for (int i = sizeX - 2; i > 0; i--)
			for (int j = sizeY - 2; j > 0; j--) {
				// Direction is computed as d = atan2( dX, dY )
				// The returned value of atan2 is from -pi to +pi.
				dest.setPixel(i, j, Math.atan2(
					getPixel(i, j + 1) - getPixel(i, j - 1), 
					getPixel(i + 1, j) - getPixel(i - 1, j)));
			}
	}

	public double max() {
		double m = pixel[0][0];
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				if (m < getPixel(i, j))
					m = getPixel(i, j);
		return m;
	}

	public double min() {
		double m = getPixel(0, 0);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				if (m > getPixel(i, j))
					m = getPixel(i, j);
		return m;
	}

	public void normalize() {
		double aMin = min();
		double aDelta = max() - aMin;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				setPixel(i, j, (getPixel(i, j) - aMin) / aDelta);
	}

	public void copyTo(DImageMap dest) {
		dest.resize(sizeX, sizeY);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				dest.pixel[i][j] = pixel[i][j];
	}

	public void make0() {
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				pixel[i][j] = 0;
	}

	public BufferedImage toImage() {
		double aMin, aDelta;
		int c;
		aMin = min();
		aDelta = max() - aMin;
		if (aDelta == 0) 
			aDelta = 1;
		BufferedImage bi = new BufferedImage(sizeX, sizeY,
				BufferedImage.TYPE_INT_RGB);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				c = (int) Math.floor(((getPixel(i, j) - aMin) * 255) / aDelta);
				bi.setRGB(i, j, (c << 16) | (c << 8) | c);
			}
		return bi;
	}
}
