package com.slavi.parallel.img;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.slavi.statistics.StatisticsLT;

public class DWindowedImageUtils {
	/**
	 * Returns a string containing the min, max and sum of all pixels in the image. 
	 */
	public static String toString(DWindowedImage image) {
		int minX = image.minX();
		int maxX = image.maxX();
		int minY = image.minY();
		int maxY = image.maxY();
		
		double sum = 0;
		for (int i = minX; i <= maxX; i++)
			for (int j = minY; j <= maxY; j++)
				sum += image.getPixel(i, j);

		return String.format(
				"Min: %.5f\n" +
				"Max: %.5f\n" +
				"Sum: %.5f\n", 
					min(image), 
					max(image), 
					sum);
	}

	/**
	 * Returns the maximum pixel value.
	 */
	public static double max(DWindowedImage image) {
		int minX = image.minX();
		int maxX = image.maxX();
		int minY = image.minY();
		int maxY = image.maxY();
		
		double m = image.getPixel(minX, minY);
		for (int i = minX; i <= maxX; i++)
			for (int j = minY; j <= maxY; j++) 
				if (m < image.getPixel(i, j))
					m = image.getPixel(i, j);
		return m;
	}

	/**
	 * Returns the minimum pixel value.
	 */
	public static double min(DWindowedImage image) {
		int minX = image.minX();
		int maxX = image.maxX();
		int minY = image.minY();
		int maxY = image.maxY();
		
		double m = image.getPixel(minX, minY);
		for (int i = minX; i <= maxX; i++)
			for (int j = minY; j <= maxY; j++) 
				if (m > image.getPixel(i, j))
					m = image.getPixel(i, j);
		return m;
	}

	/**
	 * Normalizes the pixel values of the image scaling the values
	 * so that all values are in the [0..1] interval.
	 *
	 */
	public static void normalize(DWindowedImage image) {
		int minX = image.minX();
		int maxX = image.maxX();
		int minY = image.minY();
		int maxY = image.maxY();
		
		double aMin = min(image);
		double aDelta = max(image) - aMin;
		if (aDelta == 0.0)
			return;
		for (int i = minX; i <= maxX; i++)
			for (int j = minY; j <= maxY; j++) 
				image.setPixel(i, j, (image.getPixel(i, j) - aMin) / aDelta);
	}

	public static void drawBorder(Rectangle innerRect, DWindowedImage dest) {
		int dMinX = dest.minX();
		int dMaxX = dest.maxX();
		int dMinY = dest.minY();
		int dMaxY = dest.maxY();
		
		int sMinX = innerRect.x;
		int sMaxX = innerRect.x + innerRect.width;
		int sMinY = innerRect.y;
		int sMaxY = innerRect.y + innerRect.height;

		int minX = Math.max(dMinX, sMinX);
		int maxX = Math.min(dMaxX, sMaxX);
		int minY = Math.max(dMinY, sMinY);
		int maxY = Math.min(dMaxY, sMaxY);

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
	}
	
	/**
	 * Makes a copy of this image into the dest image.
	 */
	public static void copyTo(DWindowedImage source, DWindowedImage dest) {
		int minX = Math.max(dest.minX(), source.minX());
		int maxX = Math.min(dest.maxX(), source.maxX());
		int minY = Math.max(dest.minY(), source.minY());
		int maxY = Math.min(dest.maxY(), source.maxY());
		
		drawBorder(source.getExtent(), dest);
		for (int i = minX; i <= maxX; i++)
			for (int j = minY; j <= maxY; j++) 
				dest.setPixel(i, j, source.getPixel(i, j));
	}

	/**
	 * Initializes all pixel values to zero.
	 */
	public static void make0(DWindowedImage image) {
		int minX = image.minX();
		int maxX = image.maxX();
		int minY = image.minY();
		int maxY = image.maxY();

		for (int i = minX; i <= maxX; i++)
			for (int j = minY; j <= maxY; j++) 
				image.setPixel(i, j, 0.0);
	}
	
	/**
	 * Calculates the pixel statistics using the class {@link StatisticsLT}. 
	 */
	public static StatisticsLT calcStatistics(DWindowedImage image) {
		int minX = image.minX();
		int maxX = image.maxX();
		int minY = image.minY();
		int maxY = image.maxY();

		StatisticsLT result = new StatisticsLT();
		result.start();
		for (int i = minX; i <= maxX; i++)
			for (int j = minY; j <= maxY; j++) 
				result.addValue(image.getPixel(i, j));
		result.stop();
		return result;
	}
	
	/**
	 * Subtract image2 from image1 and return the result.
	 * The extent of the resulting image is the intersection of both images. 
	 */
	public static DWindowedImage sub(DWindowedImage i1, DWindowedImage i2) {
		Rectangle ext = i1.getExtent().intersection(i2.getExtent());
		PDImageMapBuffer result = new PDImageMapBuffer(ext);
		for (int i = result.minX(); i <= result.maxX(); i++)
			for (int j = result.minY(); j <= result.maxY(); j++)
				result.setPixel(i, j, i1.getPixel(i, j) - i2.getPixel(i, j));
		return result;
	}
	
	/**
	 * Converts this image to BufferedImage.TYPE_BYTE_GRAY image scaling
	 * the pixel values as to fall in the interval [0..255].
	 */
	public static BufferedImage toImage(DWindowedImage image) {
		int minX = image.minX();
		int maxX = image.maxX();
		int minY = image.minY();
		int maxY = image.maxY();

		double aMin = min(image);
		double aDelta = max(image) - aMin;
		if (aDelta == 0.0) 
			aDelta = 1.0;
		BufferedImage bi = new BufferedImage(maxX - minX + 1, maxY - minY + 1,
				BufferedImage.TYPE_BYTE_GRAY);
		for (int i = minX; i <= maxX; i++)
			for (int j = minY; j <= maxY; j++) { 
				int c = (int) Math.floor(((image.getPixel(i, j) - aMin) * 255.0) / aDelta);
				bi.setRGB(i - minX, j - minY, (c << 16) | (c << 8) | c);
			}
		return bi;
	}
	
	/**
	 * Converts this image to BufferedImage using {@link #toImage()} and
	 * saves it to the specified file.
	 */
	public static void toImageFile(DWindowedImage image, String fouName) throws IOException {
		ImageIO.write(toImage(image), "png", new File(fouName));
	}

	/**
	 * See {@link #applyStatisticsFilter(double)}
	 */
	public static void applyStatisticsFilter(DWindowedImage image) {
		applyStatisticsFilter(image, 0.5);
	}
	
	/**
	 * Uses statistical approach to remove suspicious peak pixels, i.e.
	 * pixels with extremely low or extremely high values.
	 */
	public static void applyStatisticsFilter(DWindowedImage image, double timesStandardDeviation) {
		int minX = image.minX();
		int maxX = image.maxX();
		int minY = image.minY();
		int maxY = image.maxY();

		StatisticsLT stat = calcStatistics(image);
		double stdDev = timesStandardDeviation * stat.getStdDeviation();
		double minVal = stat.getAvgValue() - stdDev;
		double maxVal = stat.getAvgValue() + stdDev;
		for (int i = minX; i <= maxX; i++)
			for (int j = minY; j <= maxY; j++) { 
				double tmp = image.getPixel(i, j);
				if (tmp < minVal) tmp = minVal;
				if (tmp > maxVal) tmp = maxVal;
				image.setPixel(i, j, tmp);
			}
	}
}
