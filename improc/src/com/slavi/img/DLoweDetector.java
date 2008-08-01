package com.slavi.img;

import java.io.File;
import java.io.IOException;

import com.slavi.matrix.DiagonalMatrix;
import com.slavi.matrix.Matrix;
import com.slavi.utils.Marker;

/**
 * @author Slavian Petrov
 */
public class DLoweDetector {

	public static interface Hook {
		public void keyPointCreated(KeyPoint scalePoint);
	}
	
	public Hook hook = null;
	
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

	private boolean isLocalExtrema(DImageMap[] DOGs, int atX, int atY) {
		double curPixel = DOGs[1].getPixel(atX, atY);
		if (Math.abs(curPixel) < dogThreshold)
			return false;

		boolean isMaximum = true;
		boolean isMinimum = true;
		for (int k = 2; k >= 0; k--) {
			DImageMap im = DOGs[k];
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

	private boolean isTooEdgeLike(DImageMap aDOG, int atX, int atY) {
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

	protected boolean localizeIsWeak(DImageMap[] DOGs, int x, int y,
			KeyPoint sp) {
		DImageMap below = DOGs[0];
		DImageMap current = DOGs[1];
		DImageMap above = DOGs[2];

		DiagonalMatrix h = new DiagonalMatrix(3);
		Matrix adj = new Matrix(1, 3);
		Matrix b = new Matrix(1, 3);
		int adjustments = relocationMaximum;

		while (adjustments-- > 0) {
			// When the localization hits some problem, i.e. while moving the
			// point a border is reached, then skip this point.
			if ((x < 1) || (x > current.getSizeX() - 2) || 
				(y < 1) || (y > current.getSizeY() - 2))
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

	/* wrong, wrong, wrong
	public void createDescriptor2(KeyPoint sp, DImageMap magnitude,
			DImageMap direction) {

		for (int i = 0 ; i < KeyPoint.descriptorSize; i++)
			for (int j = 0 ; j < KeyPoint.descriptorSize; j++)
				for (int k = 0 ; k < KeyPoint.numDirections; k++)
					featureVector[i][j][k] = 0;
		
		double considerScaleFactor = 2.0 * sp.kpScale;
		double sigma2Sq = 2.0 * Math.pow(KeyPoint.descriptorSize / 2.0, 2.0);
		double sizeInPixels05 = (KeyPoint.descriptorSize - 1) * considerScaleFactor / 2.0;
		int sizeInPixels = (int)(sizeInPixels05 * 2.0);
		double cosD = Math.cos(sp.degree);
		double sinD = Math.sin(sp.degree);		
		
		for (int i = 0; i < sizeInPixels; i++) {
			double translatedX = i - sizeInPixels05;
//			double translatedXSq = Math.pow(translatedX, 2.0);
			for (int j = 0; j < sizeInPixels; j++) {
				double translatedY = j - sizeInPixels05;
				double xR = (translatedX * cosD - translatedY * sinD) / considerScaleFactor;
				double yR = (translatedX * sinD + translatedY * cosD) / considerScaleFactor;
				
				int imgX = (int)(sp.doubleX + translatedX * cosD - translatedY * sinD);
				int imgY = (int)(sp.doubleY + translatedX * sinD + translatedY * cosD);
				if ((imgX < 0) || (imgY < 0) || (imgX >= magnitude.sizeX) || (imgY >= magnitude.sizeY))
					continue;
				
				int indxX = (int)(i / considerScaleFactor);
				int indxY = (int)(j / considerScaleFactor);
				int indxOrientation = (int) ((direction.getPixel(imgX, imgY) - sp.degree + 
						2.0 * Math.PI) * KeyPoint.numDirections / (2.0 * Math.PI)) % KeyPoint.numDirections;
				double weightX = indxX - i / considerScaleFactor;
				double weightY = indxY - j / considerScaleFactor;
				//double weightGauss = Math.exp (-(translatedXSq + Math.pow(translatedY, 2.0)) / sigma2Sq);
				double weightGauss = Math.exp (-(Math.pow(xR, 2.0) + Math.pow(yR, 2.0)) / sigma2Sq);
				
				double value = magnitude.getPixel(imgX, imgY); 
				sp.setItem(indxX, indxY, indxOrientation, 
						sp.getItem(indxX, indxY, indxOrientation) +
						weightX * weightY * weightGauss * value);
			}
		}
		// Normalize and hicap the feature vector, as recommended on page
		// 16 in Lowe03.
		// Straight normalization
		
		// TODO: Optimize me!
		sp.normalize();
		sp.hiCap(0.2);
		sp.normalize();

		for (int i = 0 ; i < KeyPoint.descriptorSize; i++)
			for (int j = 0 ; j < KeyPoint.descriptorSize; j++)
				for (int k = 0 ; k < KeyPoint.numDirections; k++)
					sp.setItem(i, j, k, (int)(255.0 * sp.getItem(i, j, k)));

		sp.doubleX *= sp.imgScale;
		sp.doubleY *= sp.imgScale;
		sp.kpScale *= sp.imgScale;
	}
	*/
	
	private double[][][] featureVector = new double[KeyPoint.descriptorSize][KeyPoint.descriptorSize][KeyPoint.numDirections];
	
	public void createDescriptor(KeyPoint sp, DImageMap magnitude,
			DImageMap direction) {

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
				if (currentX < 1 || currentX >= (magnitude.getSizeX() - 1) ||
					currentY < 1 || currentY >= (magnitude.getSizeY() - 1))
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
	
	private void GenerateKeypointSingle(double sigma, DImageMap magnitude,
			DImageMap direction, KeyPoint sp, int scaleSpaceLevels) {
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
		int xMin = Math.max(sp.imgX - radius, 1);
		int xMax = Math.min(sp.imgX + radius, magnitude.getSizeX() - 1);
		int yMin = Math.max(sp.imgY - radius, 1);
		int yMax = Math.min(sp.imgY + radius, magnitude.getSizeY() - 1);

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

	private DImageMap lastBlured1Img = null;
	
	String workDir = "D:/Temp/t1/";
	private void debugPrintImage(DImageMap img, double scale, String type, int level) {
//		try {
//			img.toImageFile(workDir + Integer.toString((int)scale) + "-" + type + "-" + level + ".jpg");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println("**** " + Integer.toString((int) scale) + "\t" + type + "\t" + level + "\t");
//		System.out.println(img.calcStatistics());
	}

	private void DetectFeaturesInSingleDOG(DImageMap[] DOGs, DImageMap magnitude, DImageMap direction, int dogLevel, double scale, int scaleSpaceLevels, double sigma) {
		// Now we have three valid Difference Of Gaus images
		// Border pixels are skipped

		int sizeX = DOGs[0].getSizeX();
		int sizeY = DOGs[0].getSizeY();
		
		for (int i = sizeX - 2; i >= 1; i--) {
			for (int j = sizeY - 2; j >= 1; j--) {
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
				tempKeyPoint.dogLevel = dogLevel+1;
				tempKeyPoint.imgScale = scale;

				GenerateKeypointSingle(sigma, magnitude, direction, tempKeyPoint, scaleSpaceLevels);
			}
		}
	}

	int isLocalExtremaCount;
	int isTooEdgeLikeCount;
	int localizeIsWeakCount;
	
	public void DetectFeaturesInSingleLevel(DImageMap theImage, double scale,
			int scaleSpaceLevels) {

		isLocalExtremaCount = 0;
		isTooEdgeLikeCount = 0;
		localizeIsWeakCount = 0;
		
		double sigma = initialSigma;
		DImageMap blured0 = new DImageMap(theImage.getSizeX(), theImage.getSizeY());
		DImageMap blured1 = new DImageMap(theImage.getSizeX(), theImage.getSizeY());
		DImageMap blured2 = new DImageMap(theImage.getSizeX(), theImage.getSizeY());
		DImageMap magnitude = new DImageMap(theImage.getSizeX(), theImage.getSizeY());
		DImageMap direction = new DImageMap(theImage.getSizeX(), theImage.getSizeY());

		DImageMap[] DOGs = new DImageMap[3];
		for (int i = DOGs.length - 1; i >= 0; i--)
			DOGs[i] = new DImageMap(theImage.getSizeX(), theImage.getSizeY());

		theImage.copyTo(blured2);

		debugPrintImage(theImage, scale, "A", 0);

		for (int dogLevel = -2; dogLevel < scaleSpaceLevels; dogLevel++) {
			DImageMap tmpImageMap;
			tmpImageMap = blured0;
			blured0 = blured1;
			lastBlured1Img = blured1 = blured2;
			blured2 = tmpImageMap;

			DGaussianFilter gf = new DGaussianFilter(sigma);
			gf.applyGaussianFilter(blured1, blured2);
//			gf.applyGaussianFilterOriginal(blured1, blured2);
			
			debugPrintImage(tmpImageMap, scale, "B", dogLevel);
			
			sigma *= Math.pow(2.0, 1.0 / scaleSpaceLevels); // -> This is the original formula !!!

			tmpImageMap = DOGs[0];
			DOGs[0] = DOGs[1];
			DOGs[1] = DOGs[2];
			DOGs[2] = tmpImageMap;
			for (int i = tmpImageMap.getSizeX() - 1; i >= 0; i--)
				for (int j = tmpImageMap.getSizeY() - 1; j >= 0; j--)
					tmpImageMap.setPixel(i, j, blured2.getPixel(i, j)
							- blured1.getPixel(i, j));
			if (dogLevel < 0)
				continue;
			
			debugPrintImage(tmpImageMap, scale, "G", dogLevel);
			
			// Compute gradient magnitude and direction plane
			blured0.computeMagnitude(magnitude);
			blured0.computeDirection(direction);

			DImageMap temp = new DImageMap(1, 1);
			magnitude.copyTo(temp);
			
			debugPrintImage(magnitude, scale, "M", dogLevel);
			debugPrintImage(direction, scale, "D", dogLevel);
			
			DetectFeaturesInSingleDOG(DOGs, magnitude, direction, dogLevel, scale, scaleSpaceLevels, sigma);
		} // end of for aLevel
	}
	
	public static DImageMap firstLastBlurredImage = null;
	
	public void DetectFeatures(DImageMap theImage, int scaleSpaceLevels,
			int minimumRequiredPixelsize) {
		// ??? more initializers
		double scale = 1;
		DImageMap curImage = new DImageMap(theImage.getSizeX(), theImage.getSizeY());
//		DGaussianFilter gf = new DGaussianFilter(1.5);
//		gf.applyGaussianFilter(theImage, curImage);
		theImage.copyTo(curImage);

		int count = 0;
		while (curImage != null) {
			try {
				curImage.toImageFile("d:/temp/a" + (count++) + ".png");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DetectFeaturesInSingleLevel(curImage, scale, scaleSpaceLevels);
			// Next scale down level
		
			if (curImage.getSizeX() / 2 > minimumRequiredPixelsize) { 
				DImageMap tmpImage = new DImageMap(curImage.getSizeX() / 2, curImage.getSizeY() / 2); 
				curImage = lastBlured1Img;
				if (firstLastBlurredImage == null) {
					firstLastBlurredImage = new DImageMap(1, 1); 
					lastBlured1Img.copyTo(firstLastBlurredImage);
				}
				
				curImage.scaleHalf(tmpImage); 
				curImage = tmpImage; 
				scale *= 2; 
			} else { 
				curImage = null; 
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		File image = new File(Const.sourceImage);
		System.out.println("Processing image " + image);
		DImageMap img = new DImageMap(image);
		DLoweDetector d = new DLoweDetector();
		Marker.mark("started");
		d.DetectFeatures(img, 3, 32);
		Marker.release();
		System.out.println("DONE.");
	}
}
