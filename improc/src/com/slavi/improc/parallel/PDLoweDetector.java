package com.slavi.improc.parallel;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.slavi.image.DImageWrapper;
import com.slavi.image.DWindowedImage;
import com.slavi.image.DWindowedImageUtils;
import com.slavi.image.PDImageMapBuffer;
import com.slavi.improc.KeyPoint;
import com.slavi.improc.singletreaded.DLoweDetector.Hook;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.matrix.SymmetricMatrix;
import com.slavi.util.Const;

public class PDLoweDetector implements Callable<Void> {

	public Hook hook = null;

	public static final int defaultScaleSpaceLevels = 3;

	DWindowedImage src;
	
	DWindowedImage nextLevelBlurredImage;

	int scale;

	int scaleSpaceLevels;

	Rectangle dloweExtent;
	
	public PDLoweDetector(DWindowedImage src, Rectangle dloweExtent, DWindowedImage nextLevelBlurredImage, int scale, int scaleSpaceLevels) {
		this.src = src;
		this.scale = scale;
		this.scaleSpaceLevels = scaleSpaceLevels;
		this.dloweExtent = dloweExtent;
		Rectangle r = src.getExtent();
//		r.x = (src.minX() + 1) >> 1;
//		r.y = (src.minY() + 1) >> 1;
//		r.width = (src.maxX() >> 1) - r.x + 1;
//		r.height = (src.maxY() >> 1) - r.y + 1;
		r.x = (dloweExtent.x + 1) >> 1;
		r.y = (dloweExtent.y + 1) >> 1;
		r.width = ((dloweExtent.x + dloweExtent.width) >> 1) - r.x;
		r.height = ((dloweExtent.y + dloweExtent.height) >> 1) - r.y;
		this.nextLevelBlurredImage = new DImageWrapper(nextLevelBlurredImage, r);
	}
	
	public static double getNextSigma(double sigma, int scaleSpaceLevels) {
		return sigma * Math.pow(2.0, 1.0 / scaleSpaceLevels); // -> This is the original formula!!!
	}
	
	public static Rectangle getNeededSourceExtent(Rectangle dest) {
		return getNeededSourceExtent(dest, defaultScaleSpaceLevels);
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

	public static Rectangle getEffectiveTargetExtent(Rectangle source) {
		return getEffectiveTargetExtent(source, defaultScaleSpaceLevels);
	}
	
	public static Rectangle getEffectiveTargetExtent(Rectangle source, int scaleSpaceLevels) {
		double sigma = initialSigma;
		double sigmas[] = new double[scaleSpaceLevels + 2];
		for (int aLevel = -2, i = 0; aLevel < scaleSpaceLevels; aLevel++, i++) {
			sigmas[i] = sigma = getNextSigma(sigma, scaleSpaceLevels);
		}

		Rectangle tmp = source;
		for (int i = 0; i < sigmas.length; i++) {
			tmp = PFastGaussianFilter.getEffectiveTargetExtent(tmp, sigmas[i]);
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

		SymmetricMatrix h = new SymmetricMatrix(3);
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
			throw (new IllegalArgumentException("CapAndNormalizeFV cannot normalize with norm = 0.0"));
		
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
	
	private void GenerateKeypointSingle(double sigma, DWindowedImage magnitude,
			DWindowedImage direction, KeyPoint sp, int scaleSpaceLevels) {
		// The relative estimated keypoint scale. The actual absolute keypoint
		// scale to the original image is yielded by the product of imgScale.
		// But as we operate in the current octave, the size relative to the
		// anchoring images is missing the imgScale factor.
		double kpScale = initialSigma
				* Math.pow(2.0, (sp.dogLevel + sp.adjS) / scaleSpaceLevels);

		// Lowe03, "A gaussian-weighted circular window with a \sigma three
		// times that of the scale of the keypoint".
		//
		// With \sigma = 3.0 * kpScale, the square dimension we have to
		// consider is (3 * \sigma) (until the weight becomes very small).
		sigma = 3.0 * kpScale;
		int radius = (int) (3.0 * sigma / 2.0 + 0.5);
		int radiusSq = radius * radius;

		// As the point may lie near the border, build the rectangle
		// coordinates we can still reach, minus the border pixels, for which
		// we do not have gradient information available.
		int xMin = Math.max(sp.imgX - radius, direction.minX());
		int xMax = Math.min(sp.imgX + radius, direction.maxX());
		int yMin = Math.max(sp.imgY - radius, direction.minY());
		int yMax = Math.min(sp.imgY + radius, direction.maxY());

		// Precompute 1D gaussian divisor (2 \sigma^2) in:
		// G(r) = e^{-\frac{r^2}{2 \sigma^2}}
		double gaussianSigmaFactor = 2.0 * sigma * sigma;
		final int binCount = 36;
		double[] bins = new double[binCount];
		for (int i = binCount - 1; i >= 0; i--)
			bins[i] = 0;

		// Build the direction histogram
		for (int y = yMin; y < yMax; y++) {
			for (int x = xMin; x < xMax; x++) {
				// Only consider pixels in the circle, else we might skew the
				// orientation histogram by considering more pixels into the
				// corner directions
				int relX = x - sp.imgX;
				int relY = y - sp.imgY;
				int rSq = relX * relX + relY * relY;
				if (rSq <= radiusSq) {
					// The gaussian weight factor.
					double gaussianWeight = Math.exp(-rSq / gaussianSigmaFactor);
					// Find the closest bin and add the direction
					double angle = Math.PI + direction.getPixel(x, y);
					angle /= 2 * Math.PI;
					angle *= binCount;

					int indx = (int) angle;
					indx = (indx >= binCount ? 0 : indx);
					bins[indx] += magnitude.getPixel(x, y) * gaussianWeight;
				}
			}
		}

		// As there may be succeeding histogram entries like this:
		// ( ..., 0.4, 0.3, 0.4, ... ) where the real peak is located at the
		// middle of this three entries, we can improve the distinctiveness of
		// the bins by applying an averaging pass.
		//
		// TODO: is this really the best method? (we also loose a bit of
		// information. Maybe there is a one-step method that conserves more)
		// ???AverageWeakBins (bins, binCount);

		for (int pass = 0; pass < 4; pass++) {
			double firstE = bins[0];
			double last = bins[binCount - 1];
			for (int i = 0; i < binCount; i++) {
				double cur = bins[i];
				// double next = bins[(i + 1) % binCount];
				double next = (i == (binCount - 1)) ? firstE : bins[(i + 1)
						% binCount];
				bins[i] = (last + cur + next) / 3.0;
				last = cur;
			}
		}

		// find the maximum peak in gradient orientation
		double binMaxValue = bins[0];
		int maxBinIndx = 0;
		for (int i = binCount - 1; i > 0; i--)
			if (bins[i] > binMaxValue) {
				binMaxValue = bins[i];
				maxBinIndx = i;
			}

		// Any other local peak that is within 80% of the highest peak is
		// used to also create keypoints with the corresponding orientation.
		double leftval = bins[(maxBinIndx + binCount - 1) % binCount];
		double middleval = bins[maxBinIndx];
		double rightval = bins[(maxBinIndx + 1) % binCount];
		double aval = ((leftval + rightval) - 2.0 * middleval) / 2.0;
		if (aval == 0.0)
			return; // Not a parabol
		double cval = (((leftval - middleval) / aval) - 1.0) / 2.0;
		double binThreshold = (middleval - cval * cval * aval) * 0.8;

		final double oneBinRad = (2.0 * Math.PI) / binCount;

		for (int i = binCount - 1; i >= 0; i--) {
			double left = bins[(i + binCount - 1) % binCount];
			double middle = bins[i];
			double right = bins[(i + 1) % binCount];
			// Check if current bin is local peak
			// if ((middle >= binThreshold) &&
			// ((middle == binMaxValue) || (
			// (middle > left) && (middle > right)) )) {
			if ((i == maxBinIndx) ||
				((middle >= binThreshold) && (middle > left) && (middle > right))) {
				// Get an interpolated peak direction and value guess.
				double a = ((left + right) - 2.0 * middle) / 2.0;
				if (a == 0.0)
					continue; // Not a parabol
				double degreeCorrection = (((left - middle) / a) - 1.0) / 2.0;

				// double peakValue = middle - degreeCorrection *
				// degreeCorrection * a;

				// [-1.0 ; 1.0] -> [0 ; binrange], and add the fixed absolute bin position.
				// We subtract PI because bin 0 refers to 0, binCount-1 bin refers
				// to a bin just below 2PI, so -> [-PI ; PI]. Note that at this
				// point we determine the canonical descriptor anchor angle. It
				// does not matter where we set it relative to the peak degree,
				// but it has to be constant. Also, if the output of this
				// implementation is to be matched with other implementations it
				// must be the same constant angle (here: -PI).
				double degree = (i + degreeCorrection) * oneBinRad - Math.PI;
				if (degree < -Math.PI)
					degree += 2.0 * Math.PI;
				else if (degree > Math.PI)
					degree -= 2.0 * Math.PI;

				sp.kpScale = kpScale;
				sp.degree = degree;
				
				KeyPoint sp2 = new KeyPoint();
				sp2.adjS = sp.adjS;
				sp2.degree = sp.degree;
				sp2.doubleX = sp.doubleX;
				sp2.doubleY = sp.doubleY;
				sp2.imgX = sp.imgX;
				sp2.imgY = sp.imgY;
				sp2.kpScale = sp.kpScale;
				sp2.dogLevel = sp.dogLevel;
				sp2.imgScale = sp.imgScale;
				
				createDescriptor(sp2, magnitude, direction);
				if (hook != null)
					hook.keyPointCreated(sp2);
			}
		}
	}
	
	private void DetectFeaturesInSingleDOG(DWindowedImage[] DOGs, DWindowedImage magnitude, DWindowedImage direction, int aLevel, int scale, int scaleSpaceLevels, double sigma) {
		// Now we have three valid Difference Of Gaus images
		// Border pixels are skipped

		DWindowedImage tmpDOG = DOGs[0];
		int minX = tmpDOG.minX();
		int maxX = tmpDOG.maxX();
		int minY = tmpDOG.minY();
		int maxY = tmpDOG.maxY();

		for (int i = minX + 1; i < maxX; i++) {
			for (int j = minY + 1; j < maxY; j++) {
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
				tempKeyPoint.dogLevel = aLevel+1;
				tempKeyPoint.imgScale = scale;

				GenerateKeypointSingle(sigma, magnitude, direction, tempKeyPoint, scaleSpaceLevels);
			}
		}
	}

	int isLocalExtremaCount;
	int isTooEdgeLikeCount;
	int localizeIsWeakCount;
	
	void DetectFeaturesInSingleLevel() throws InterruptedException, ExecutionException, IOException {
		double sigma = initialSigma;
		Rectangle srcExtent = src.getExtent();

		PDImageMapBuffer blurred0 = new PDImageMapBuffer(srcExtent);
		PDImageMapBuffer blurred1 = new PDImageMapBuffer(srcExtent);
		PDImageMapBuffer blurred2 = new PDImageMapBuffer(srcExtent);
		PDImageMapBuffer magnitude = new PDImageMapBuffer(srcExtent);
		PDImageMapBuffer direction = new PDImageMapBuffer(srcExtent);

		Rectangle dogExtent = new Rectangle(dloweExtent.x - 1, dloweExtent.y - 1, dloweExtent.width + 2, dloweExtent.height + 2);
		dogExtent = dogExtent.intersection(srcExtent);
		
		PDImageMapBuffer[] DOGs = new PDImageMapBuffer[3];
		for (int i = DOGs.length - 1; i >= 0; i--)
			DOGs[i] = new PDImageMapBuffer(dogExtent);

		isLocalExtremaCount = 0;
		isTooEdgeLikeCount = 0;
		localizeIsWeakCount = 0;

		PFastGaussianFilter.applyFilter(src, blurred1, sigma);
		computeDOG(src, blurred1, DOGs[1]);
		sigma = getNextSigma(sigma, scaleSpaceLevels);

		PFastGaussianFilter.applyFilter(blurred1, blurred2, sigma);
		computeDOG(blurred1, blurred2, DOGs[2]);
		sigma = getNextSigma(sigma, scaleSpaceLevels);

		int dogcount = 0;
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

			PComputeMagnitude.computeMagnitude(blurred0, magnitude);
			PComputeDirection.computeDirection(blurred0, direction);
			
			// Compute next DOG
			PFastGaussianFilter.applyFilter(blurred1, blurred2, sigma);
			computeDOG(blurred1, blurred2, DOGs[2]);
			DWindowedImageUtils.toImageFile(DOGs[2], Const.workDir + "/dlowe_dog_" + (int)scale + "_" + dogcount + "p.png");
			DWindowedImageUtils.toImageFile(magnitude, Const.workDir + "/dlowe_magnitude_" + (int)scale + "_" + dogcount + "p.png");
			DWindowedImageUtils.toImageFile(direction, Const.workDir + "/dlowe_direction_" + (int)scale + "_" + dogcount + "p.png");
			dogcount++;
			sigma = getNextSigma(sigma, scaleSpaceLevels);

			// detect
			DetectFeaturesInSingleDOG(DOGs, magnitude, direction, aLevel, scale, scaleSpaceLevels, sigma);
		}

		for (int j = nextLevelBlurredImage.minY(); j <= nextLevelBlurredImage.maxY(); j++) {
			for (int i = nextLevelBlurredImage.minX(); i <= nextLevelBlurredImage.maxX(); i++) {
				nextLevelBlurredImage.setPixel(i, j, blurred1.getPixel(i << 1, j << 1));
			}
		}
		
/*		int minX = dloweExtent.x;
		int minY = dloweExtent.y;
		int maxX = dloweExtent.x + dloweExtent.width - 1;
		int maxY = dloweExtent.y + dloweExtent.height - 1;
		
		if ((minX & 0x01) != 0)
			minX++;
		if ((minY & 0x01) != 0)
			minY++;
		
		for (int j = minY; j <= maxY; j += 2) {
			for (int i = minX; i <= maxX; i += 2) {
				try {
					nextLevelBlurredImage.setPixel(i >> 1, j >> 1, blurred1.getPixel(i, j));
				} catch (Exception e) {
					e.printStackTrace(System.out);
					System.out.println("ERROR");
					System.out.println("DEST EXTENT = " + nextLevelBlurredImage.getExtent());
					System.out.println("SRC EXTENT  = " + blurred1.getExtent());
					System.out.println("minX = " + minX);
					System.out.println("maxX = " + maxX);
					System.out.println("minY = " + minY);
					System.out.println("maxY = " + maxY);
				}
			}
		}*/
	}
	
	public Void call() throws Exception {
		DetectFeaturesInSingleLevel();
		return null;
	}

	public String toString() {
		Rectangle srcExtent = src.getExtent();
		StringBuilder r = new StringBuilder();
		r.append("Source X,Y                :"); r.append(srcExtent.x); r.append(","); r.append(srcExtent.y);
		r.append("\nSource width, height      :"); r.append(srcExtent.width); r.append(","); r.append(srcExtent.height);
		r.append("\nDestination X,Y           :"); r.append(dloweExtent.x); r.append(","); r.append(dloweExtent.y);
		r.append("\nDestination width, height :"); r.append(dloweExtent.width); r.append(","); r.append(dloweExtent.height);
		r.append("\nScale                     :"); r.append(scale);
		if (srcExtent.isEmpty() || dloweExtent.isEmpty())
			throw new RuntimeException("zxc");
		return r.toString();
	}
}
