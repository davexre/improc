package com.slavi.improc.old.singletreaded;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.slavi.math.adjust.Statistics;

/**
 * This class represents a gray scale image with the pixels stored 
 * in an array of doubles. By default all pixel values are in the 
 * range [0..1].
 */
public class DImageMap {
	/**
	 * See #getSizeX()
	 */
	protected int sizeX;

	/**
	 * See #getSizeY() 
	 */
	protected int sizeY;

	/**
	 * The pixels of the image. The first index of the array is 
	 * x (width) and the second index is the y (height).
	 */
	protected double[][] pixel;

	/**
	 * Specifies the value for the one-pixel border of the computed magnitude or
	 * direction map. Used by {@link #computeMagnitude(DImageMap)} and 
	 * {@link #computeDirection(DImageMap)}.
	 */
	private static final double borderColorValue = 0;

	/**
	 * Returns a string containing the min, max and sum of all pixels in the image. 
	 */
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

	public Rectangle getExtent() {
		return new Rectangle(0, 0, sizeX, sizeY);
	}
	
	/**
	 * Width of the image in pixels.
	 */
	public int getSizeX() {
		return sizeX;
	}

	/**
	 * Height of the image in pixels.
	 */
	public int getSizeY() {
		return sizeY;
	}

	/**
	 * Returns a pixel from the image.
	 */
	public double getPixel(int atX, int atY) {
		return pixel[atX][atY];
	}

	/**
	 * Set the value of a pixel
	 */
	public void setPixel(int atX, int atY, double aValue) {
		pixel[atX][atY] = aValue;
	}

	/**
	 * Creates a new image with the specified size with all pixels set to zero.
	 */
	public DImageMap(int aSizeX, int aSizeY) {
		sizeX = 0;
		sizeY = 0;
		resize(aSizeX, aSizeY);
	}

	/**
	 * Reads the image and converts it to gray scale values in the
	 * range [0..1].
	 */
	public DImageMap(BufferedImage image) {
		resize(image.getWidth(), image.getHeight());
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				int c = image.getRGB(i, j);
				pixel[i][j] = (
						((c >> 16) & 0xff) + 
						((c >> 8) & 0xff) + 
						 (c & 0xff)) / (3.0 * 255.0);
			}
	}
	
	/**
	 * Reads the image and converts it to gray scale values in the
	 * range [0..1].
	 */
	public DImageMap(File image) throws IOException {
		this(ImageIO.read(image));
	}

	/**
	 * Resizes the pixels buffer to the new size if necessary. After
	 * this the content of the pixels is unspecified.
	 */
	public void resize(int newSizeX, int newSizeY) {
		if ((newSizeX == sizeX) && (newSizeY == sizeY))
			return;
		if ((newSizeX <= 0) || (newSizeY <= 0))
			throw new IllegalArgumentException("Invalid size parameter");
		sizeX = newSizeX;
		sizeY = newSizeY;
		pixel = new double[sizeX][sizeY];
	}

	/**
	 * Scales down this image by half into the dest image.
	 */
	public void scaleHalf(DImageMap dest) {
		dest.resize(sizeX >> 1, sizeY >> 1);
		for (int i = dest.sizeX - 1; i >= 0; i--) {
			int i2 = i << 1;
			for (int j = dest.sizeY - 1; j >= 0; j--)
				dest.pixel[i][j] = pixel[i2][j << 1];
		}
	}

	/**
	 * Scales up this image by twice into the dest image.
	 */
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

	/**
	 * Computes the magnitude of the image. The magnitude of a
	 * pixels is computed as
	 * <pre>
	 * dX = getPixel(i + 1, j) - getPixel(i - 1, j);
	 * dY = getPixel(i, j + 1) - getPixel(i, j - 1);
	 * m(i,j) = sqrt( dX * dX + dY * dY );
	 * </pre>
	 */
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
				double dX = getPixel(i + 1, j) - getPixel(i - 1, j);
				double dY = getPixel(i, j + 1) - getPixel(i, j - 1);
				dest.setPixel(i, j, Math.sqrt(dX * dX + dY * dY));
			}
	}

	/**
	 * Computes the directions of the image. The direction of a
	 * pixel is computed as
	 * dX = getPixel(i, j + 1) - getPixel(i, j - 1);
	 * dY = getPixel(i + 1, j) - getPixel(i - 1, j);
	 * d(i,j) = atan2(dX, dY);
	 */
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

	/**
	 * Returns the maximum pixel value.
	 */
	public double max() {
		double m = pixel[0][0];
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				if (m < getPixel(i, j))
					m = getPixel(i, j);
		return m;
	}

	/**
	 * Returns the minimum pixel value.
	 */
	public double min() {
		double m = getPixel(0, 0);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				if (m > getPixel(i, j))
					m = getPixel(i, j);
		return m;
	}

	/**
	 * Normalizes the pixel values of the image scaling the values
	 * so that all values are in the [0..1] interval.
	 *
	 */
	public void normalize() {
		double aMin = min();
		double aDelta = max() - aMin;
		if (aDelta == 0.0)
			return;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				setPixel(i, j, (getPixel(i, j) - aMin) / aDelta);
	}

	/**
	 * Makes a copy of this image into the dest image.
	 */
	public void copyTo(DImageMap dest) {
		dest.resize(sizeX, sizeY);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				dest.pixel[i][j] = pixel[i][j];
	}

	/**
	 * Initializes all pixel values to zero.
	 */
	public void make0() {
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				pixel[i][j] = 0;
	}

	/**
	 * Calculates the pixel statistics using the class {@link Statistics}. 
	 */
	public Statistics calcStatistics() {
		Statistics result = new Statistics();
		result.start();
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				result.addValue(getPixel(i, j));
			}
		result.stop();
		return result;
	}
	
	/**
	 * Converts this image to BufferedImage.TYPE_BYTE_GRAY image scaling
	 * the pixel values as to fall in the interval [0..255].
	 */
	public BufferedImage toImage() {
		double aMin = min();
		double aDelta = max() - aMin;
		if (aDelta == 0.0) 
			aDelta = 1.0;
		BufferedImage bi = new BufferedImage(sizeX, sizeY,
				BufferedImage.TYPE_BYTE_GRAY);
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				int c = (int) Math.floor(((getPixel(i, j) - aMin) * 255.0) / aDelta);
				bi.setRGB(i, j, (c << 16) | (c << 8) | c);
			}
		return bi;
	}
	
	/**
	 * Converts this image to BufferedImage using {@link #toImage()} and
	 * saves it to the specified file.
	 */
	public void toImageFile(String fouName) throws IOException {
		ImageIO.write(toImage(), "png", new File(fouName));
	}

	/**
	 * See {@link #applyStatisticsFilter(double)}
	 */
	public void applyStatisticsFilter() {
		applyStatisticsFilter(0.5);
	}
	
	/**
	 * Uses statistical approach to remove suspicious peak pixels, i.e.
	 * pixels with extremely low or extremely high values.
	 */
	public void applyStatisticsFilter(double timesStandardDeviation) {
		Statistics stat = calcStatistics();
		double stdDev = timesStandardDeviation * stat.getStdDeviation();
		double minVal = stat.getAvgValue() - stdDev;
		double maxVal = stat.getAvgValue() + stdDev;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--) {
				double tmp = getPixel(i, j);
				if (tmp < minVal) tmp = minVal;
				if (tmp > maxVal) tmp = maxVal;
				setPixel(i, j, tmp);
			}
	}
}
