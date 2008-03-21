package com.slavi.parallel.img;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class PDLoweDetector_OLD implements Runnable {

	public static final int defaultScaleSpaceLevels = 3;

	/**
	 * Detection parameters, suggested by Lowe's research paper.
	 */
	public static final double initialSigma = 1.6;

	DWindowedImage src;

	double scale;

	int scaleSpaceLevels;

	ExecutorService exec;

	public PDLoweDetector_OLD(DWindowedImage src, double scale, int scaleSpaceLevels, ExecutorService exec) {
		this.src = src;
		this.scale = scale;
		this.scaleSpaceLevels = scaleSpaceLevels;
		this.exec = exec;
	}
	
	public static Rectangle getNeededSourceExtent(Rectangle dest) {
		return getNeededSourceExtent(dest, defaultScaleSpaceLevels);
	}

	public static double getNextSigma(double sigma, int scaleSpaceLevels) {
		return sigma * Math.pow(2.7, 1.0 / scaleSpaceLevels); // -> This is the original formula!!!
//		return sigma * Math.pow(2.0, 1.0 / scaleSpaceLevels); // -> This is the original formula!!!
	}
	
	public static Rectangle getNeededSourceExtent(Rectangle dest, int scaleSpaceLevels) {
		double sigma = initialSigma;
		double maxSigma = sigma;
		for (int aLevel = -2; aLevel < scaleSpaceLevels; aLevel++) {
			sigma = getNextSigma(sigma, scaleSpaceLevels);
			if (sigma > maxSigma)
				maxSigma = sigma;
		}

		Rectangle magnitude = PComputeMagnitude.getNeededSourceExtent(dest);
		Rectangle direction = PComputeDirection.getNeededSourceExtent(dest);
		Rectangle maxBlur = PGaussianFilter.getNeededSourceExtent(dest, maxSigma);
		Rectangle result = magnitude.union(direction).union(maxBlur);
		return result;
	}

	public static Rectangle getEffectiveTargetExtent(Rectangle source, int scaleSpaceLevels) {
		double sigma = initialSigma;
		double maxSigma = sigma;
		for (int aLevel = -2; aLevel < scaleSpaceLevels; aLevel++) {
			sigma = getNextSigma(sigma, scaleSpaceLevels);
			if (sigma > maxSigma)
				maxSigma = sigma;
		}

		Rectangle magnitude = PComputeMagnitude.getEffectiveTargetExtent(source);
		Rectangle direction = PComputeDirection.getEffectiveTargetExtent(source);
		Rectangle maxBlur = PGaussianFilter.getEffectiveTargetExtent(source, maxSigma);
		Rectangle result = magnitude.union(direction).union(maxBlur);
		return result;
	}
	
	void computeDOG(DWindowedImage blured1, DWindowedImage blured2, DWindowedImage destDOG) {
		int minX = destDOG.minX();
		int maxX = destDOG.maxX();
		int minY = destDOG.minY();
		int maxY = destDOG.maxY();

		for (int i = minX; i <= maxX; i++)
			for (int j = minY; j <= maxY; j++)
				destDOG.setPixel(i, j, blured2.getPixel(i, j) - blured1.getPixel(i, j));
	}

	String workDir = "D:/Temp/ttt/";

	private void debugPrintImage(DWindowedImage src, double scale, String type, int level) {
		try {
			DWindowedImageUtils.toImageFile(src, workDir + Integer.toString((int) scale) + "-" + type + "-" + level
					+ ".jpg");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("**** " + Integer.toString((int) scale) + "\t" + type + "\t" + level + "\t");
		System.out.println(DWindowedImageUtils.calcStatistics(src));
	}

	void doit() throws InterruptedException, ExecutionException {
		double sigma = initialSigma;
		Rectangle srcExtent = src.getExtent();
		Rectangle destExtent = getEffectiveTargetExtent(srcExtent, scaleSpaceLevels);

		PDImageMapBuffer blurred0 = new PDImageMapBuffer(destExtent);
		PDImageMapBuffer blurred1 = new PDImageMapBuffer(destExtent);
		PDImageMapBuffer blurred2 = new PDImageMapBuffer(destExtent);
		PDImageMapBuffer magnitude = new PDImageMapBuffer(destExtent);
		PDImageMapBuffer direction = new PDImageMapBuffer(destExtent);

		PDImageMapBuffer[] DOGs = new PDImageMapBuffer[3];
		for (int i = DOGs.length - 1; i >= 0; i--)
			DOGs[i] = new PDImageMapBuffer(destExtent);

		int isLocalExtremaCount = 0;
		int isTooEdgeLikeCount = 0;
		int localizeIsWeakCount = 0;

		PFastGaussianFilter.applyFilter(src, blurred1, sigma);
		sigma = getNextSigma(sigma, scaleSpaceLevels);

		PFastGaussianFilter.applyFilter(src, blurred2, sigma);
		sigma = getNextSigma(sigma, scaleSpaceLevels);

		computeDOG(src, blurred1, DOGs[1]);
		computeDOG(blurred1, blurred2, DOGs[2]);

		debugPrintImage(blurred1, scale, "B", -2);
		debugPrintImage(blurred2, scale, "B", -1);
		debugPrintImage(src, scale, "A", 0);

		for (int aLevel = 0; aLevel < scaleSpaceLevels; aLevel++) {
			// Shift blurred
			PDImageMapBuffer tmpimap = blurred0;
			blurred0 = blurred1;
			blurred1 = blurred2;
			blurred2 = tmpimap;
			// Shift DOGs
			tmpimap = DOGs[0];
			DOGs[0] = DOGs[1];
			DOGs[1] = DOGs[2];
			DOGs[2] = tmpimap;
			// Compute next DOG
			PFastGaussianFilter.applyFilter(src, blurred2, sigma);
			sigma = getNextSigma(sigma, scaleSpaceLevels);
			PComputeMagnitude.computeMagnitude(blurred1, magnitude);
			PComputeDirection.computeDirection(blurred1, direction);
			computeDOG(blurred1, blurred2, DOGs[2]);

			debugPrintImage(blurred2, scale, "B", aLevel);
			debugPrintImage(DOGs[0], scale, "G", aLevel);
			debugPrintImage(magnitude, scale, "M", aLevel);
			debugPrintImage(direction, scale, "D", aLevel);
			// detect
		}
	}
	
	public void run()  {
		try {
			doit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
