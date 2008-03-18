package com.slavi.parallel.img;

import java.awt.Rectangle;
import java.util.concurrent.ExecutorService;

import com.slavi.img.DGaussianFilter;
import com.slavi.img.DImageMap;
import com.slavi.img.KeyPoint;

public class PDLoweDetector implements Runnable {

	public static final int defaultScaleSpaceLevels = 3;
	
	/**
	 * Detection parameters, suggested by Lowe's research paper.
	 */
	public static final double initialSigma = 1.6;
	
	DWindowedImage src; 
	
	DWindowedImage dest;

	double scale;
	
	int scaleSpaceLevels;
	
	ExecutorService exec;
	
	public static Rectangle getNeededSourceExtent(Rectangle dest) {
		return getNeededSourceExtent(dest, defaultScaleSpaceLevels);
	}
	
	public static Rectangle getNeededSourceExtent(Rectangle dest, int scaleSpaceLevels) {
		double sigma = initialSigma;
		double maxSigma = sigma;
		for (int aLevel = -2; aLevel < scaleSpaceLevels; aLevel++) {
			sigma *= Math.pow(2.0, 1.0 / scaleSpaceLevels); // -> This is the original formula !!!
			if (sigma > maxSigma)
				maxSigma = sigma;
		}

		Rectangle magnitude = PComputeMagnitude.getNeededSourceExtent(dest);
		Rectangle direction = PComputeDirection.getNeededSourceExtent(dest);
		Rectangle maxBlur = PGaussianFilter.getNeededSourceExtent(dest, maxSigma);
		Rectangle result = magnitude.union(direction).union(maxBlur);
		return result;
	}

	public void run() {
		double sigma = initialSigma;
		Rectangle destExtent = dest.getExtent();
		PDImageMapBuffer blured0 = new PDImageMapBuffer(destExtent);
		PDImageMapBuffer blured1 = new PDImageMapBuffer(destExtent);
		PDImageMapBuffer blured2 = new PDImageMapBuffer(destExtent);
		PDImageMapBuffer magnitude = new PDImageMapBuffer(destExtent);
		PDImageMapBuffer direction = new PDImageMapBuffer(destExtent);
	
		PDImageMapBuffer[] DOGs = new PDImageMapBuffer[3];
		for (int i = DOGs.length - 1; i >= 0; i--)
			DOGs[i] = new PDImageMapBuffer(destExtent);

//		src.copyTo(blured2);
		
		int isLocalExtremaCount = 0;
		int isTooEdgeLikeCount = 0;
		int localizeIsWeakCount = 0;
	
//		debugPrintImage(theImage, scale, "A", 0);
		
		for (int aLevel = -2; aLevel < scaleSpaceLevels; aLevel++) {
			PDImageMapBuffer tmpImageMap;
			DGaussianFilter gf = new DGaussianFilter(sigma);
			tmpImageMap = blured0;
			blured0 = blured1;
			blured1 = blured2;
//			lastBlured1Img = blured2; 
/*			
			blured2 = tmpImageMap;
			new PGaussianFilter(blured1, blured2
			gf.applyGaussianFilterOriginal(blured1, blured2);

			debugPrintImage(tmpImageMap, scale, "B", aLevel);
			
			sigma *= Math.pow(2.0, 1.0 / scaleSpaceLevels); // -> This is the original formula !!!

			tmpImageMap = DOGs[0];
			DOGs[0] = DOGs[1];
			DOGs[1] = DOGs[2];
			DOGs[2] = tmpImageMap;
			for (int i = tmpImageMap.getSizeX() - 1; i >= 0; i--)
				for (int j = tmpImageMap.getSizeY() - 1; j >= 0; j--)
					tmpImageMap.setPixel(i, j, blured2.getPixel(i, j)
							- blured1.getPixel(i, j));
			if (aLevel < 0)
				continue;
			
			debugPrintImage(tmpImageMap, scale, "G", aLevel);
			
			// Compute gradient magnitude and direction plane
			blured0.computeMagnitude(magnitude);
			blured0.computeDirection(direction);

			debugPrintImage(magnitude, scale, "M", aLevel);
			debugPrintImage(direction, scale, "D", aLevel);

			// Now we have three valid Difference Of Gaus images
			// Border pixels are skipped

			for (int i = blured0.getSizeX() - 2; i >= 1; i--) {
				for (int j = blured0.getSizeY() - 2; j >= 1; j--) {
					// Detect if DOGs[1].pixel[i][j] is a local extrema. Compare
					// this pixel to all its neighbour pixels.
					if (!isLocalExtrema(DOGs, i, j))
						continue; // current pixel in DOGs[1] is not a local
									// extrema
					isLocalExtremaCount++;
					
					// We have a peak.
					if (isTooEdgeLike(DOGs[1], i, j))
						continue;
					isTooEdgeLikeCount++;

					// When the localization hits some problem, i.e. while
					// moving the
					// point a border is reached, then skip this point.
					KeyPoint tempKeyPoint = new KeyPoint();
					if (localizeIsWeak(DOGs, i, j, tempKeyPoint))
						continue;
					localizeIsWeakCount++;

					// Ok. We have located a keypoint.
					tempKeyPoint.level = aLevel+1;
					tempKeyPoint.imgScale = scale;

					GenerateKeypointSingle(sigma, magnitude, direction, tempKeyPoint, scaleSpaceLevels);
				}
			}*/
		} // end of for aLevel
	}
}
