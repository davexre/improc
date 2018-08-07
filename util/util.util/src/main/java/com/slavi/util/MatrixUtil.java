package com.slavi.util;

import java.awt.image.BufferedImage;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import com.slavi.math.MathUtil;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public class MatrixUtil {

	public static BlockRealMatrix toApacheMatrix(Matrix m) {
		return new BlockRealMatrix(m.toArray());
	}

	public static Matrix fromApacheMatrix(RealMatrix m, Matrix dest) {
		if (dest == null)
			dest = new Matrix(m.getColumnDimension(), m.getRowDimension());
		else
			dest.resize(m.getColumnDimension(), m.getRowDimension());
		for (int i = dest.getSizeX() - 1; i >= 0; i--)
			for (int j = dest.getSizeY() - 1; j >= 0; j--)
				dest.setItem(i, j, m.getEntry(j, i));
		return dest;
	}

	public static Matrix fromApacheVector(RealVector v, Matrix dest) {
		if (dest == null)
			dest = new Matrix(v.getDimension(), 1);
		else
			dest.resize(v.getDimension(), 1);
		for (int i = dest.getVectorSize() - 1; i >= 0; i--)
				dest.setItem(i, 0, v.getEntry(i));
		return dest;
	}

	/**
	 * Uses the Hue and Saturation (HSL) of the baseColor and varies the Light to make a visual representation of the Matrix m.
	 * The BufferedImage dest is null or does not match in size a new one is created.
	 */
	public static BufferedImage toImage(Matrix m, double minVal, double maxVal, int baseColor, BufferedImage dest) {
		double hsl[] = new double[3];
		double drgb[] = new double[3];
		ColorConversion.RGB.fromRGB(baseColor, drgb);
		ColorConversion.HSL.fromDRGB(drgb, hsl);
		double maxL = hsl[2];
		BufferedImage result = dest;
		if (result == null ||
			result.getWidth() != m.getSizeX() ||
			result.getHeight() != m.getSizeY() ||
			result.getType() != BufferedImage.TYPE_INT_RGB)
			result = new BufferedImage(m.getSizeX(), m.getSizeY(), BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < m.getSizeX(); i++)
			for (int j = 0; j < m.getSizeY(); j++) {
				hsl[2] = MathUtil.mapValue(m.getItem(i, j), minVal, maxVal, 1, maxL);
				ColorConversion.HSL.toDRGB(hsl, drgb);
				result.setRGB(i, j, ColorConversion.RGB.toRGB(drgb));
			}
		return result;
	}

	public static BufferedImage toImage(Matrix m, int baseColor, BufferedImage dest) {
		return toImage(m, m.min(), m.max(), baseColor, dest);
	}

	public static Statistics calcStatistics(Matrix m) {
		return calcStatistics(m, null);
	}

	/**
	 * Calculates Statistics of all items in dest. Returns dest.
	 * If dest is null a new Statistics object is created.
	 */
	public static Statistics calcStatistics(Matrix m, Statistics dest) {
		if (dest == null)
			dest = new Statistics();
		dest.start();
		for (int i = m.getVectorSize() - 1; i >= 0; i--)
			dest.addValue(m.getVectorItem(i));
		dest.stop();
		return dest;
	}

}
