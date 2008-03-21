package com.slavi.parallel.img;

import java.awt.Rectangle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import com.slavi.img.DImageMap;
import com.slavi.img.KeyPoint;
import com.slavi.matrix.DiagonalMatrix;
import com.slavi.matrix.Matrix;

public class PDLoweDetector implements Runnable {

	public static final int defaultScaleSpaceLevels = 3;

	DWindowedImage src;

	double scale;

	int scaleSpaceLevels;

	ExecutorService exec;

	Rectangle destExtent;
	
	public PDLoweDetector(DWindowedImage src, Rectangle dest, double scale, int scaleSpaceLevels, ExecutorService exec) {
		this.src = src;
		this.scale = scale;
		this.scaleSpaceLevels = scaleSpaceLevels;
		this.exec = exec;
		this.destExtent = dest;
	}
	
	public static Rectangle getNeededSourceExtent(Rectangle dest) {
		return getNeededSourceExtent(dest, defaultScaleSpaceLevels);
	}

	public static double getNextSigma(double sigma, int scaleSpaceLevels) {
//		return sigma * Math.pow(2.7, 1.0 / scaleSpaceLevels); // -> This is the original formula!!!
		return sigma * Math.pow(2.0, 1.0 / scaleSpaceLevels); // -> This is the original formula!!!
	}
	
	public static Rectangle getNeededSourceExtent(Rectangle dest, int scaleSpaceLevels) {
		double sigma = initialSigma;
		double sigmas[] = new double[scaleSpaceLevels + 2];
		for (int aLevel = -2, i = 0; aLevel < scaleSpaceLevels; aLevel++, i++) {
			sigmas[i] = sigma = getNextSigma(sigma, scaleSpaceLevels);
		}

		Rectangle tmp = dest;
		for (int i = sigmas.length - 1; i >= 0; i--) {
			tmp = PFastGaussianFilter.getNeededSourceExtent(tmp, sigmas[i]);
		}
		return tmp;
	}

	public static Rectangle getEffectiveTargetExtent(Rectangle source, int scaleSpaceLevels) {
		double sigma = initialSigma;
		Rectangle tmp = source;
		for (int aLevel = -2; aLevel < scaleSpaceLevels; aLevel++) {
			sigma = getNextSigma(sigma, scaleSpaceLevels);
			tmp = PFastGaussianFilter.getEffectiveTargetExtent(tmp, sigma);
		}
		return tmp;
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

	/**
	 * Minimum absolute DoG value of a pixel to be allowed as minimum/maximum
	 * peak. This control how much general non-differing areas, such as the sky
	 * is filtered. Higher value = less peaks, lower value = more peaks. Good
	 * values from 0.005 to 0.01. Note this is related to 'dValueLowThresh',
	 * which should be a bit larger, factor 1.0 to 1.5.
	 */
	public static final double dogThreshold = 0.0075;

	/**
	 * D-value filter highcap value, higher = less keypoints, lower = more.
	 * Lower: only keep keypoints with good localization properties, i.e. those
	 * that are precisely and easily to localize (high contrast, see Lowe, page
	 * 11. He recommends 0.03, but this seems way too high to me.)
	 */
	double dValueLowThresh = 0.008;

	/**
	 * Detection parameters, suggested by Lowe's research paper.
	 */
	public static final double initialSigma = 1.6;

	/**
	 * Required cornerness ratio level, higher = more keypoints, lower = less.
	 */
	public static final double maximumEdgeRatio = 20.0;

	/**
	 * The exact sub-pixel localization is done on just one DoG plane. Even when
	 * the scale adjustment exceeds +/- 0.5, the plane is not changed. With this
	 * value you can discard peaks that are localized to be too far from the
	 * plane. A high value will allow for peaks to be used that are more far
	 * away from the plane used for localization, while a low value will sort
	 * out more peaks, that drifted too far away.
	 * 
	 * Be very careful with this value, as a too large value will lead to a high
	 * number of keypoints in hard to localize areas such as in photos of the
	 * sky.
	 * 
	 * Good values seem to lie between 0.30 and 0.6.
	 */
	public static final double scaleAdjustThresh = 0.50;

	/**
	 * Number of maximum steps a single keypoint can make in its space.
	 */
	public static final int relocationMaximum = 4;

	private boolean isLocalExtrema(DWindowedImage[] DOGs, int atX, int atY) {
		double curPixel = DOGs[1].getPixel(atX, atY);
		if (Math.abs(curPixel) < dogThreshold)
			return false;

		boolean isMaximum = true;
		boolean isMinimum = true;
		for (int k = 2; k >= 0; k--) {
			DWindowedImage im = DOGs[k];
			if (isMinimum) {
				if (
					(im.getPixel(atX + 1, atY + 1) <= curPixel) ||
					(im.getPixel(atX - 1, atY + 1) <= curPixel) ||
					(im.getPixel(atX    , atY + 1) <= curPixel) ||
					(im.getPixel(atX + 1, atY - 1) <= curPixel) ||
					(im.getPixel(atX - 1, atY - 1) <= curPixel) ||
					(im.getPixel(atX    , atY - 1) <= curPixel) ||
					(im.getPixel(atX + 1, atY    ) <= curPixel) ||
					(im.getPixel(atX - 1, atY    ) <= curPixel) ||
					// note here its just < instead of <=
					(k == 1 ? false : (im.getPixel(atX, atY) < curPixel)) )
					isMinimum = false;
			}
			if (isMaximum) {
				if (
					(im.getPixel(atX + 1, atY + 1) >= curPixel) ||
					(im.getPixel(atX - 1, atY + 1) >= curPixel) ||
					(im.getPixel(atX    , atY + 1) >= curPixel) ||
					(im.getPixel(atX + 1, atY - 1) >= curPixel) ||
					(im.getPixel(atX - 1, atY - 1) >= curPixel) ||
					(im.getPixel(atX    , atY - 1) >= curPixel) ||
					(im.getPixel(atX + 1, atY    ) >= curPixel) ||
					(im.getPixel(atX - 1, atY    ) >= curPixel) ||
					// note here its just > instead of >=
					(k == 1 ? false : (im.getPixel(atX, atY) > curPixel)) )
					isMaximum = false;
			}
		}
		return isMinimum || isMaximum;
	}
	
	private boolean isTooEdgeLike(DWindowedImage aDOG, int atX, int atY) {
		double D_xx, D_yy, D_xy;
		// Calculate the Hessian H elements [ D_xx, D_xy ; D_xy , D_yy ]
		D_xx = aDOG.getPixel(atX + 1, atY) + aDOG.getPixel(atX - 1, atY) - 
			2.0 * aDOG.getPixel(atX, atY);
		D_yy = aDOG.getPixel(atX, atY + 1) + aDOG.getPixel(atX, atY - 1) - 
			2.0 * aDOG.getPixel(atX, atY);
		D_xy = 0.25 * (
			(aDOG.getPixel(atX + 1, atY + 1) - aDOG.getPixel(atX + 1, atY - 1)) - 
			(aDOG.getPixel(atX - 1, atY + 1) - aDOG.getPixel(atX - 1, atY - 1)));

		// page 13 in Lowe's paper
		double TrHsq = D_xx + D_yy;
		TrHsq *= TrHsq;
		double DetH = D_xx * D_yy - (D_xy * D_xy);

		double r1sq = (maximumEdgeRatio + 1.0);
		r1sq *= r1sq;

		// BUG: this can invert < to >, uhh: if ((TrHsq * maximumEdgeRatio) >=
		// (DetH * r1sq))
		return (TrHsq / DetH) >= (r1sq / maximumEdgeRatio);
	}
	
	protected boolean localizeIsWeak(DWindowedImage[] DOGs, int x, int y,
			KeyPoint sp) {
		DWindowedImage below = DOGs[0];
		DWindowedImage current = DOGs[1];
		DWindowedImage above = DOGs[2];
		int minX = current.minX(); 
		int maxX = current.maxX(); 
		int minY = current.minY(); 
		int maxY = current.maxY(); 

		DiagonalMatrix h = new DiagonalMatrix(3);
		Matrix adj = new Matrix(1, 3);
		Matrix b = new Matrix(1, 3);
		int adjustments = relocationMaximum;

		while (adjustments-- > 0) {
			// When the localization hits some problem, i.e. while moving the
			// point a border is reached, then skip this point.
			if ((x <= minX) || (x >= maxX) || 
				(y <= minY) || (y >= maxY))
				return true;

			h.setItem(0, 0, below.getPixel(x, y) - 
				2 * current.getPixel(x, y) + above.getPixel(x, y));
			h.setItem(1, 1, current.getPixel(x, y - 1) - 
				2 * current.getPixel(x, y) + current.getPixel(x, y + 1));
			h.setItem(2, 2, current.getPixel(x - 1, y) - 
				2 * current.getPixel(x, y) + current.getPixel(x + 1, y));

			h.setItem(0, 1, 0.25 * (
				(above.getPixel(x, y + 1) - above.getPixel(x, y - 1)) - 
				(below.getPixel(x, y + 1) - below.getPixel(x, y - 1))));
			h.setItem(0, 2, 0.25 * (
				(above.getPixel(x + 1, y) - above.getPixel(x - 1, y)) - 
				(below.getPixel(x + 1, y) - below.getPixel(x - 1, y))));
			h.setItem(1, 2, 0.25 * (
				(current.getPixel(x + 1, y + 1) - current.getPixel(x - 1, y + 1)) - 
				(current.getPixel(x + 1, y - 1) - current.getPixel(x - 1, y - 1))));

			// Solve linear
			if (!h.inverse())
				return true;

			b.setItem(0, 0, -0.5 * 
				(above.getPixel(x, y) - below.getPixel(x, y)));
			b.setItem(0, 1, -0.5 * 
				(current.getPixel(x, y + 1) - current.getPixel(x, y - 1)));
			b.setItem(0, 2, -0.5 *
				(current.getPixel(x + 1, y) - current.getPixel(x - 1, y)));
			h.mMul(b, adj);

			double adjS = adj.getItem(0, 0);
			double adjY = adj.getItem(0, 1);
			double adjX = adj.getItem(0, 2);
			if ((Math.abs(adjX) <= 0.5) && (Math.abs(adjY) <= 0.5)) {
				// Now we approximated the exact sub-pixel peak position.
				if (Math.abs(adjS) > scaleAdjustThresh) 
					return true;
				// Additional local pixel information is now available,
				// threshhold the D(^x)
				if (Math.abs(current.getPixel(x, y)) > dValueLowThresh) {
					sp.imgX = x;
					sp.imgY = y;
					sp.doubleX = x + adjX;
					sp.doubleY = y + adjY;
					sp.adjS = adjS; // ??? Who uses this (adjust scale) ???!!!???
					return false;
				}
			}
			// Check that just one pixel step is needed, otherwise discard
			// the point
			double distSq = adjX * adjX + adjY * adjY;
			if (distSq > 2.0)
				return (true);
			if (Math.abs(adjX) >= 0.5)
				x += adjX < 0 ? -1 : (adjX > 0 ? 1 : 0);
			if (Math.abs(adjY) >= 0.5)
				y += adjY < 0 ? -1 : (adjY > 0 ? 1 : 0);
		}
		return true;
	}
	
	public void createDescriptor(KeyPoint sp, DWindowedImage magnitude, DWindowedImage direction) {
		double[][][] featureVector = new double[KeyPoint.descriptorSize][KeyPoint.descriptorSize][KeyPoint.numDirections];
		int minX = magnitude.minX(); 
		int maxX = magnitude.maxX(); 
		int minY = magnitude.minY(); 
		int maxY = magnitude.maxY(); 

		for (int i = 0 ; i < KeyPoint.descriptorSize; i++)
			for (int j = 0 ; j < KeyPoint.descriptorSize; j++)
				for (int k = 0 ; k < KeyPoint.numDirections; k++)
					featureVector[i][j][k] = 0;
		
		double considerScaleFactor = 2.0 * sp.kpScale;
		double dDim05 = KeyPoint.descriptorSize / 2.0;
		int radius = (int) (((KeyPoint.descriptorSize + 1.0) / 2) *
			Math.sqrt(2.0) * considerScaleFactor + 0.5);
		double sigma2Sq = 2.0 * dDim05 * dDim05;
		double angle = -sp.degree;
		
		for (int y = -radius ; y < radius ; ++y) {
			for (int x = -radius ; x < radius ; ++x) {
				// Rotate and scale
				double yR = Math.sin (angle) * x +
					Math.cos (angle) * y;
				double xR = Math.cos (angle) * x -
					Math.sin (angle) * y;

				yR /= considerScaleFactor;
				xR /= considerScaleFactor;
		
				// Now consider all (xR, yR) that are anchored within
				// (- descDim/2 - 0.5 ; -descDim/2 - 0.5) to
				//    (descDim/2 + 0.5 ; descDim/2 + 0.5),
				// as only those can influence the FV.
				if (yR >= (dDim05 + 0.5) || xR >= (dDim05 + 0.5) ||
					xR <= -(dDim05 + 0.5) || yR <= -(dDim05 + 0.5))
					continue;

				int currentX = (int) (x + sp.doubleX + 0.5);
				int currentY = (int) (y + sp.doubleY + 0.5);
				if (currentX < minX || currentX > maxX ||
					currentY < minY || currentY > maxY)
					continue;
				
				// Weight the magnitude relative to the center of the
				// whole FV. We do not need a normalizing factor now, as
				// we normalize the whole FV later anyway (see below).
				// xR, yR are each in -(dDim05 + 0.5) to (dDim05 + 0.5)
				// range
				double magW = Math.exp (-(xR * xR + yR * yR) / sigma2Sq) *
					magnitude.getPixel(currentX, currentY);

				// Anchor to (-1.0, -1.0)-(dDim + 1.0, dDim + 1.0), where
				// the FV points are located at (x, y)
				yR += dDim05 - 0.5;
				xR += dDim05 - 0.5;

				// Build linear interpolation weights:
				// A B
				// C D
				//
				// The keypoint is located between A, B, C and D.
				int[] xIdx = new int[2];
				int[] yIdx = new int[2];
				int[] dirIdx = new int[2];
				double[] xWeight = new double[2];
				double[] yWeight = new double[2];
				double[] dirWeight = new double[2];
				
				for (int i = 0; i<2; i++) 
					dirWeight[i] = yWeight[i] = xWeight[i] = dirIdx[i] = yIdx[i] = xIdx[i] = 0;
				
				if (xR >= 0) {
					xIdx[0] = (int) xR;
					xWeight[0] = (1.0 - (xR - xIdx[0]));
				}
				if (yR >= 0) {
					yIdx[0] = (int) yR;
					yWeight[0] = (1.0 - (yR - yIdx[0]));
				}

				if (xR < (KeyPoint.descriptorSize - 1)) {
					xIdx[1] = (int) (xR + 1.0);
					xWeight[1] = xR - xIdx[1] + 1.0;
				}
				if (yR < (KeyPoint.descriptorSize - 1)) {
					yIdx[1] = (int) (yR + 1.0);
					yWeight[1] = yR - yIdx[1] + 1.0;
				}
				
				// Rotate the gradient direction by the keypoint
				// orientation, then normalize to [-pi ; pi] range.
				double dir = direction.getPixel(currentX, currentY) - sp.degree;
				if (dir <= -Math.PI)
					dir += Math.PI;
				if (dir > Math.PI)
					dir -= Math.PI;

				double idxDir = (dir * KeyPoint.numDirections) /
					(2.0 * Math.PI);
				if (idxDir < 0.0)
					idxDir += KeyPoint.numDirections;

				dirIdx[0] = (int) idxDir;
				dirIdx[1] = (dirIdx[0] + 1) % KeyPoint.numDirections;
				dirWeight[0] = 1.0 - (idxDir - dirIdx[0]);
				dirWeight[1] = idxDir - dirIdx[0];

				for (int iy = 0 ; iy < 2 ; iy++) {
					for (int ix = 0 ; ix < 2 ; ix++) {
						for (int id = 0 ; id < 2 ; id++) {
							double value = featureVector[xIdx[ix]][yIdx[iy]][dirIdx[id]] +
								xWeight[ix] * yWeight[iy] * dirWeight[id] * magW; 
							featureVector[xIdx[ix]][yIdx[iy]][dirIdx[id]] = value;
						}
					}
				}
			}
		}
		
		// Normalize and hicap the feature vector, as recommended on page
		// 16 in Lowe03.
		// Straight normalization
		double norm = 0.0;
		
		for (int i = 0 ; i < KeyPoint.descriptorSize; i++)
			for (int j = 0 ; j < KeyPoint.descriptorSize; j++)
				for (int k = 0 ; k < KeyPoint.numDirections; k++) {
					double d = featureVector[i][j][k];
					norm += d * d;
				}

		norm = Math.sqrt(norm);
		if (norm == 0.0)
			throw (new Error("CapAndNormalizeFV cannot normalize with norm = 0.0"));
		
		for (int i = 0 ; i < KeyPoint.descriptorSize; i++)
			for (int j = 0 ; j < KeyPoint.descriptorSize; j++)
				for (int k = 0 ; k < KeyPoint.numDirections; k++)
					featureVector[i][j][k] = featureVector[i][j][k] / norm;

		double fvGradHicap = 0.2;
		// Hicap after normalization
		for (int i = 0 ; i < KeyPoint.descriptorSize; i++)
			for (int j = 0 ; j < KeyPoint.descriptorSize; j++)
				for (int k = 0 ; k < KeyPoint.numDirections; k++)
					if (featureVector[i][j][k] > fvGradHicap)
						featureVector[i][j][k] = fvGradHicap;

		// Renormalize again
		norm = 0.0;
		for (int i = 0 ; i < KeyPoint.descriptorSize; i++)
			for (int j = 0 ; j < KeyPoint.descriptorSize; j++)
				for (int k = 0 ; k < KeyPoint.numDirections; k++) {
					double d = featureVector[i][j][k];
					norm += d * d;
				}

		norm = Math.sqrt (norm);
		for (int i = 0 ; i < KeyPoint.descriptorSize; i++)
			for (int j = 0 ; j < KeyPoint.descriptorSize; j++)
				for (int k = 0 ; k < KeyPoint.numDirections; k++)
					sp.setItem(i, j, k, (byte)(255.0 * featureVector[i][j][k] / norm));

		sp.doubleX *= sp.imgScale;
		sp.doubleY *= sp.imgScale;
		sp.kpScale *= sp.imgScale;
	}
	
	
	void doit() throws InterruptedException, ExecutionException {
		double sigma = initialSigma;
		Rectangle srcExtent = src.getExtent();

		PDImageMapBuffer blurred0 = new PDImageMapBuffer(srcExtent);
		PDImageMapBuffer blurred1 = new PDImageMapBuffer(srcExtent);
		PDImageMapBuffer blurred2 = new PDImageMapBuffer(srcExtent);
		PDImageMapBuffer magnitude = new PDImageMapBuffer(srcExtent);
		PDImageMapBuffer direction = new PDImageMapBuffer(srcExtent);

		PDImageMapBuffer[] DOGs = new PDImageMapBuffer[3];
		for (int i = DOGs.length - 1; i >= 0; i--)
			DOGs[i] = new PDImageMapBuffer(destExtent);

		int isLocalExtremaCount = 0;
		int isTooEdgeLikeCount = 0;
		int localizeIsWeakCount = 0;

		Rectangle curExtent;
		
		curExtent = destExtent.union(PFastGaussianFilter.getEffectiveTargetExtent(srcExtent));
		blurred1.setExtent(curExtent);
		PFastGaussianFilter.applyFilter(src, blurred1, sigma);
		computeDOG(src, blurred1, DOGs[1]);
		sigma = getNextSigma(sigma, scaleSpaceLevels);

		curExtent = destExtent.union(PFastGaussianFilter.getEffectiveTargetExtent(curExtent));
		blurred2.setExtent(curExtent);
		PFastGaussianFilter.applyFilter(blurred1, blurred2, sigma);
		computeDOG(blurred1, blurred2, DOGs[2]);
		sigma = getNextSigma(sigma, scaleSpaceLevels);

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

			magnitude.setExtent(destExtent.union(PComputeMagnitude.getEffectiveTargetExtent(curExtent)));
			PComputeMagnitude.computeMagnitude(blurred1, magnitude);
			direction.setExtent(destExtent.union(PComputeDirection.getEffectiveTargetExtent(curExtent)));
			PComputeDirection.computeDirection(blurred1, direction);
			// Compute next DOG
			curExtent = destExtent.union(PFastGaussianFilter.getEffectiveTargetExtent(curExtent));
			blurred2.setExtent(curExtent);
			PFastGaussianFilter.applyFilter(blurred1, blurred2, sigma);
			computeDOG(blurred1, blurred2, DOGs[2]);
			sigma = getNextSigma(sigma, scaleSpaceLevels);

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
