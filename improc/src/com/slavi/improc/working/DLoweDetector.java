package com.slavi.improc.working;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.math.matrix.SymmetricMatrix;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Marker;
import com.slavi.util.XMLHelper;

/**
 * @author Slavian Petrov
 */
public class DLoweDetector {

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

	public ScalePointList scalePointList;	
	
	private boolean isLocalExtrema(DImageMap[] DOGs, int atX, int atY) {
		double curPixel = DOGs[1].getPixel(atX, atY);
		if (Math.abs(curPixel) < dogThreshold)
			return false;

		boolean isMaximum = true;
		boolean isMinimum = true;
		for (int k = 2; k >= 0; k--) {
			DImageMap im = DOGs[k];
			if (isMinimum) {
				if ((im.getPixel(atX + 1, atY + 1) <= curPixel) ||
					(im.getPixel(atX - 1, atY + 1) <= curPixel) ||
					(im.getPixel(atX    , atY + 1) <= curPixel) ||
					(im.getPixel(atX + 1, atY - 1) <= curPixel) ||
					(im.getPixel(atX - 1, atY - 1) <= curPixel) ||
					(im.getPixel(atX    , atY - 1) <= curPixel) ||
					(im.getPixel(atX + 1, atY    ) <= curPixel) ||
					(im.getPixel(atX - 1, atY    ) <= curPixel) ||
					// note here its just < instead of <=
					(k == 1 ? false : (im.getPixel(atX, atY) < curPixel)))
					isMinimum = false;
			}
			if (isMaximum) {
				if ((im.getPixel(atX + 1, atY + 1) >= curPixel) ||
					(im.getPixel(atX - 1, atY + 1) >= curPixel) ||
					(im.getPixel(atX    , atY + 1) >= curPixel) ||
					(im.getPixel(atX + 1, atY - 1) >= curPixel) ||
					(im.getPixel(atX - 1, atY - 1) >= curPixel) ||
					(im.getPixel(atX    , atY - 1) >= curPixel) ||
					(im.getPixel(atX + 1, atY    ) >= curPixel) ||
					(im.getPixel(atX - 1, atY    ) >= curPixel) ||
					// note here its just > instead of >=
					(k == 1 ? false : (im.getPixel(atX, atY) > curPixel)))
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
			ScalePoint sp) {
		DImageMap below = DOGs[0];
		DImageMap current = DOGs[1];
		DImageMap above = DOGs[2];

		SymmetricMatrix h = new SymmetricMatrix(3);
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
				if (Math.abs(adjS) > scaleAdjustThresh) {
//					if ((sp.imgX==35) && (sp.imgY==91)) {
//						System.out.println(sp.toString());
//					}
					return true;
				}
				// Additional local pixel information is now available,
				// threshhold the D(^x)
				if (Math.abs(current.getPixel(x, y)) > dValueLowThresh) {
					sp.imgX = x;
					sp.imgY = y;
					sp.doubleX = x + adjX;
					sp.doubleY = y + adjY;
					sp.adjS = adjS; // ??? Who uses this (adjust scale) ???!!!???
//					if ((sp.imgX==35) && (sp.imgY==91)) {
//						System.out.println(sp.toString());
//					}
//					fou.println(x + "\t" + y + "\t" + level + "\t" + adjS);
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

	public void createDescriptor2(ScalePoint sp, DImageMap magnitude,
			DImageMap direction) {

		sp.initFeatureVector();
		
		double considerScaleFactor = 2.0 * sp.kpScale;
		double sigma2Sq = 2.0 * Math.pow(ScalePoint.descriptorSize / 2.0, 2.0);
		double sizeInPixels05 = (ScalePoint.descriptorSize - 1) * considerScaleFactor / 2.0;
		int sizeInPixels = (int)(sizeInPixels05 * 2.0);
		double cosD = Math.cos(sp.degree);
		double sinD = Math.sin(sp.degree);		
		
		for (int i = 0; i < sizeInPixels; i++) {
			double translatedX = i - sizeInPixels05;
			double translatedXSq = Math.pow(translatedX, 2.0);
			for (int j = 0; j < sizeInPixels; j++) {
				double translatedY = j - sizeInPixels05;
				
				int imgX = (int)(sp.doubleX + translatedX * cosD - translatedY * sinD);
				int imgY = (int)(sp.doubleY + translatedX * sinD + translatedY * cosD);
				if ((imgX < 0) || (imgY < 0) || (imgX >= magnitude.sizeX) || (imgY >= magnitude.sizeY))
					continue;
				
				int indxX = (int)(i / considerScaleFactor);
				int indxY = (int)(j / considerScaleFactor);
				int indxOrientation = (int) ((direction.getPixel(imgX, imgY) - sp.degree + 
						2.0 * Math.PI) * ScalePoint.numDirections / (2.0 * Math.PI)) % ScalePoint.numDirections;
				double weightX = indxX - i / considerScaleFactor;
				double weightY = indxY - j / considerScaleFactor;
				double weightGauss = Math.exp (-(translatedXSq + Math.pow(translatedY, 2.0)) / sigma2Sq);
				
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
	}

	public void createDescriptor(ScalePoint sp, DImageMap magnitude,
			DImageMap direction) {

		for (int i = 0 ; i < ScalePoint.descriptorSize; i++)
			for (int j = 0 ; j < ScalePoint.descriptorSize; j++)
				for (int k = 0 ; k < ScalePoint.numDirections; k++)
					sp.setItem(i, j, k, 0);
		
		double considerScaleFactor = 2 * sp.kpScale;
		double dDim05 = ScalePoint.descriptorSize / 2.0;
		int radius = (int) (((ScalePoint.descriptorSize + 1.0) / 2) *
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

				if (xR < (ScalePoint.descriptorSize - 1)) {
					xIdx[1] = (int) (xR + 1.0);
					xWeight[1] = xR - xIdx[1] + 1.0;
				}
				if (yR < (ScalePoint.descriptorSize - 1)) {
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

				double idxDir = (dir * ScalePoint.numDirections) /
					(2.0 * Math.PI);
				if (idxDir < 0.0)
					idxDir += ScalePoint.numDirections;

				dirIdx[0] = (int) idxDir;
				dirIdx[1] = (dirIdx[0] + 1) % ScalePoint.numDirections;
				dirWeight[0] = 1.0 - (idxDir - dirIdx[0]);
				dirWeight[1] = idxDir - dirIdx[0];

				for (int iy = 0 ; iy < 2 ; iy++) {
					for (int ix = 0 ; ix < 2 ; ix++) {
						for (int id = 0 ; id < 2 ; id++) {
							double value = sp.getItem(xIdx[ix], yIdx[iy], dirIdx[id]) +
								xWeight[ix] * yWeight[iy] * dirWeight[id] * magW; 
							sp.setItem(xIdx[ix], yIdx[iy], dirIdx[id], value);
						}
					}
				}
			}
		}
		
		// Normalize and hicap the feature vector, as recommended on page
		// 16 in Lowe03.
		// Straight normalization
		double norm = 0.0;
		
		for (int i = 0 ; i < ScalePoint.descriptorSize; i++)
			for (int j = 0 ; j < ScalePoint.descriptorSize; j++)
				for (int k = 0 ; k < ScalePoint.numDirections; k++)
					norm += Math.pow(sp.getItem(i, j, k), 2.0);

		norm = Math.sqrt(norm);
		if (norm == 0.0)
			throw (new IllegalArgumentException("CapAndNormalizeFV cannot normalize with norm = 0.0"));
		
		for (int i = 0 ; i < ScalePoint.descriptorSize; i++)
			for (int j = 0 ; j < ScalePoint.descriptorSize; j++)
				for (int k = 0 ; k < ScalePoint.numDirections; k++)
					sp.setItem(i, j, k, sp.getItem(i, j, k) / norm);

		double fvGradHicap = 0.2;
		// Hicap after normalization
		for (int i = 0 ; i < ScalePoint.descriptorSize; i++)
			for (int j = 0 ; j < ScalePoint.descriptorSize; j++)
				for (int k = 0 ; k < ScalePoint.numDirections; k++)
					if (sp.getItem(i, j, k) > fvGradHicap)
						sp.setItem(i, j, k, fvGradHicap);

		// Renormalize again
		norm = 0.0;
		for (int i = 0 ; i < ScalePoint.descriptorSize; i++)
			for (int j = 0 ; j < ScalePoint.descriptorSize; j++)
				for (int k = 0 ; k < ScalePoint.numDirections; k++)
					norm += Math.pow(sp.getItem(i, j, k), 2.0);

		norm = Math.sqrt (norm);
		for (int i = 0 ; i < ScalePoint.descriptorSize; i++)
			for (int j = 0 ; j < ScalePoint.descriptorSize; j++)
				for (int k = 0 ; k < ScalePoint.numDirections; k++)
					sp.setItem(i, j, k, sp.getItem(i, j, k) / norm);
//					sp.setItem(i, j, k, (int)(255.0 * sp.getItem(i, j, k) / norm));

		sp.doubleX *= sp.imgScale;
		sp.doubleY *= sp.imgScale;
		sp.kpScale *= sp.imgScale;

//		String str =  "";
//		for (int i = 0; i < ScalePoint.descriptorSize; i++)
//			for (int j = 0; j < ScalePoint.descriptorSize; j++)
//				for (int k = 0; k < ScalePoint.numDirections; k++)
//					str = str + "\t" + sp.getItem(i, j, k);
//		fou.println(sp.doubleX + "\t" + sp.doubleY + "\t" + (int)sp.level + "\t" + sp.imgScale + "\t" + sp.degree + str);
		
//		if ((sp.imgX==276) && (sp.imgY==75)) {
//			System.out.println("Degree=" + sp.degree);
//			for (int ix = 0 ; ix < 2 ; ix++) {
//				for (int iy = 0 ; iy < 2 ; iy++) {
//					for (int id = 0 ; id < 2 ; id++) {
//						System.out.println("FV[" + ix + "," + iy + "," + id + "]=" + 
//							sp.getItem(ix, iy, id));
//					}
//				}
//			}
//		}
	}
	
	private void GenerateKeypointSingle(double sigma, DImageMap magnitude,
			DImageMap direction, ScalePoint sp, int scaleSpaceLevels) {
		// The relative estimated keypoint scale. The actual absolute keypoint
		// scale to the original image is yielded by the product of imgScale.
		// But as we operate in the current octave, the size relative to the
		// anchoring images is missing the imgScale factor.
		double kpScale = initialSigma
				* Math.pow(2.0, (sp.level + sp.adjS) / scaleSpaceLevels);

//		if (sp.level > 1)
//			System.out.println("SP.X=" + sp.imgX + " y=" + sp.imgY);
//		if ((sp.imgX==35) && (sp.imgY==91)) {
//			System.out.println("scaleSpaceLevels" + scaleSpaceLevels);
//			System.out.println("kpScale=" + kpScale);
//			System.out.println(sp.toString());
//		}

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
		// TO DO: is this really the best method? (we also loose a bit of
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
//		String str = "";
//		for (int i = 0; i < bins.length; i++) {
//			str = str + "\t" + bins[i];
//		}
//		fou.println(sp.imgX + "\t" + sp.imgY + "\t" + (int)sp.level + "\t" + kpScale + str);

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
		if (aval == 0)
			return; // Not a parabol
		double cval = (((leftval - middleval) / aval) - 1.0) / 2.0;
		double binThreshold = (middleval - cval * cval * aval);
		binThreshold = binThreshold * 0.8;

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

				// [-1.0 ; 1.0] -> [0 ; binrange], and add the fixed absolute
				// bin
				// position.
				// We subtract PI because bin 0 refers to 0, binCount-1 bin
				// refers
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

//				if ((sp.imgX==35) && (sp.imgY==91)) {
//					System.out.println("degree=" + degree + " corr=" + degreeCorrection + 
//							" oneBinRad=" + oneBinRad + " b=" + i);
//					for (int tt = 0; tt < binCount; tt++)
//						System.out.println("bin[" + tt + "]=" + bins[tt]);
//					System.out.println("Level=" + sp.level + 
//						" X=" + sp.imgX + " Y=" + sp.imgY + 
//						" FineX=" + sp.doubleX + " FineY=" + sp.doubleY + 
//						" kpScale=" + kpScale + " degree=" + degree);
//				}
				sp.kpScale = kpScale;
				sp.degree = degree;
				
				ScalePoint sp2 = new ScalePoint();
				sp2.adjS = sp.adjS;
				sp2.degree = sp.degree;
				sp2.doubleX = sp.doubleX;
				sp2.doubleY = sp.doubleY;
				sp2.imgX = sp.imgX;
				sp2.imgY = sp.imgY;
				sp2.kpScale = sp.kpScale;
				sp2.level = sp.level;
				sp2.imgScale = sp.imgScale;
				
//				fou.println(sp.imgX + "\t" + sp.imgY + "\t" + (int)sp.level + "\t" + degree);
				
				createDescriptor(sp2, magnitude, direction);
				scalePointCreated(sp2);
			}
		}
	}

	private void scalePointCreated(ScalePoint sp) {
		for (int i = 0; i < ScalePoint.descriptorSize; i++)
			for (int j = 0; j < ScalePoint.descriptorSize; j++)
				for (int k = 0; k < ScalePoint.numDirections; k++)
					sp.setItem(i, j, k, (int) (255.0 * sp.getItem(i, j, k)));
		scalePointList.points.add(sp);
	}
	
	private DImageMap lastBlured1Img = null;
	
	private void DetectFeaturesInSingleLevel(DImageMap theImage, double scale,
			int scaleSpaceLevels) {

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

		int isLocalExtremaCount = 0;
		int isTooEdgeLikeCount = 0;
		int localizeIsWeakCount = 0;

		for (int aLevel = -2; aLevel < scaleSpaceLevels; aLevel++) {
			DImageMap tmpImageMap;
			DGaussianFilter gf = new DGaussianFilter(sigma);
			tmpImageMap = blured0;
			blured0 = blured1;
			lastBlured1Img = blured1 = blured2;
			
			blured2 = tmpImageMap;
			// gf.applyGaussianFilter(curImage, blurredImage);
			gf.applyGaussianFilterOriginal(blured1, blured2);

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
			
			// Compute gradient magnitude and direction plane
			blured0.computeMagnitude(magnitude);
			blured0.computeDirection(direction);

			// Now we have three valid Difference Of Gaus images
			// Border pixels are skipped

			for (int i = blured0.getSizeX() - 2; i >= 1; i--) {
				for (int j = blured0.getSizeY() - 2; j >= 1; j--) {
					
//					if ((i == 449) && (j == 41) && (aLevel+1 == 2)) {
//						fou.println("below");
//						for (int iii = i - 1; iii <= i + 1; iii++)
//								for (int iij = j - 1; iij <= j + 1; iij++)
//									fou.println("" + iii + "\t" + iij + "\t" + DOGs[0].getPixel(iii, iij));
//						fou.println("current");
//						for (int iii = i - 1; iii <= i + 1; iii++)
//								for (int iij = j - 1; iij <= j + 1; iij++)
//									fou.println("" + iii + "\t" + iij + "\t" + DOGs[1].getPixel(iii, iij));
//						fou.println("above");
//						for (int iii = i - 1; iii <= i + 1; iii++)
//								for (int iij = j - 1; iij <= j + 1; iij++)
//									fou.println("" + iii + "\t" + iij + "\t" + DOGs[2].getPixel(iii, iij));
//					}
					
					
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
					ScalePoint sp = new ScalePoint();
					if (localizeIsWeak(DOGs, i, j, sp))
						continue;
					localizeIsWeakCount++;

					// Ok. We have located a keypoint.
					sp.level = aLevel+1;
					sp.imgScale = scale;

					GenerateKeypointSingle(sigma, magnitude, direction, sp, scaleSpaceLevels);
				}
			}
		} // end of for aLevel
	}
	
	public void DetectFeatures(DImageMap theImage, int scaleSpaceLevels,
			int minimumRequiredPixelsize) {
		// ??? more initializers
		double scale = 1;
		DImageMap doubledImage = new DImageMap(theImage.getSizeX() * 2, theImage.getSizeY() * 2);
		DImageMap curImage = new DImageMap(theImage.getSizeX() * 2, theImage.getSizeY() * 2);
		theImage.scaleDouble(doubledImage);
		DGaussianFilter gf = new DGaussianFilter(1.5);
		gf.applyGaussianFilter(theImage, curImage);
		theImage.copyTo(curImage);

		while (curImage != null) {
			DetectFeaturesInSingleLevel(curImage, scale, scaleSpaceLevels);
			// Next scale down level
		
			if (curImage.getSizeX() / 2 > minimumRequiredPixelsize) { 
				DImageMap tmpImage = new DImageMap(curImage.getSizeX() / 2, curImage.getSizeY() / 2); 
				curImage = lastBlured1Img;
				
				curImage.scaleHalf(tmpImage); 
				curImage = tmpImage; 
				scale *= 2; 
			} else { 
				curImage = null; 
			}
		}
	}

	public DLoweDetector() {
		scalePointList = new ScalePointList();
	}
	
	public void processImage(String imageFileName) throws IOException {
		File imgFile = new File(imageFileName);
		DImageMap img = new DImageMap(imgFile);
		scalePointList.points.clear();
		scalePointList.imageFileName = imgFile.getName();
		scalePointList.imageSizeX = img.sizeX;
		scalePointList.imageSizeY = img.sizeY;
		Marker.mark();
		DetectFeatures(img, 3, 32);
		Marker.release();
		String fouName = imageFileName.substring(0, imageFileName.lastIndexOf(".")) + ".xml";
		Element e = new Element("ScalePointList");
		scalePointList.toXML(e);
		OutputStream fou = new FileOutputStream(fouName);
		XMLHelper.writeXML(fou, e, "");
		fou.close();
	}

//	public static PrintWriter fou;
	
	public ScalePointList autoPanoPoints;
	public void loadAutoPanoFile(String finName) throws JDOMException, IOException {
		Element root = XMLHelper.readXML(new File(finName));
		autoPanoPoints = new ScalePointList();
		autoPanoPoints.imageFileName = root.getChildText("ImageFile");
		autoPanoPoints.imageSizeX = Integer.parseInt(root.getChildText("XDim"));
		autoPanoPoints.imageSizeY = Integer.parseInt(root.getChildText("YDim"));
		
		List<?> kpl = root.getChild("Arr").getChildren("KeypointN");
		
		for (int counter = 0; counter < kpl.size(); counter++) {
			Element key = (Element)kpl.get(counter);
			ScalePoint sp = new ScalePoint();
			sp.doubleX = Double.parseDouble(key.getChildText("X"));
			sp.doubleY = Double.parseDouble(key.getChildText("Y"));
			sp.imgX = (int)sp.doubleX;
			sp.imgY = (int)sp.doubleY;
			sp.adjS = 0;
			sp.degree = Double.parseDouble(key.getChildText("Orientation"));
			sp.level = Integer.parseInt(key.getChildText("Level"));
			sp.kpScale = Double.parseDouble(key.getChildText("Scale"));
			
			List<?> descr = key.getChild("Descriptor").getChildren("int");
			for (int i = 0; i < ScalePoint.descriptorSize; i++) {
				for (int j = 0; j < ScalePoint.descriptorSize; j++) {
					for (int k = 0; k < ScalePoint.numDirections; k++) {
						int index = 
							i * ScalePoint.descriptorSize * ScalePoint.numDirections +
							j * ScalePoint.numDirections + k;
						sp.setItem(i, j, k, Double.parseDouble(
							((Element)descr.get(index)).getText()));
					}
				}
			}
			autoPanoPoints.points.add(sp);
		}
	}

	public void compareKeyLists(ScalePointList list1, ScalePointList list2) throws Exception {
		int matchedCount1 = 0;
		for (int i = list1.points.size() - 1; i >= 0; i--) {
			ScalePoint sp1 = list1.points.get(i);
//			boolean matchingFound = false;
			for (int j = list2.points.size() - 1; j >= 0; j--) {
				ScalePoint sp2 = list2.points.get(j);
				if (sp1.equals(sp2)) {
//					matchingFound = true;
					matchedCount1++;
					break;
				}
			}
//			if (!matchingFound)
//				System.out.println("Point No " + i + " from 1-st list has no match in 2-nd list");
		}

		int matchedCount2 = 0;
		for (int j = list2.points.size() - 1; j >= 0; j--) {
			ScalePoint sp2 = list2.points.get(j);
//			boolean matchingFound = false;
			for (int i = list1.points.size() - 1; i >= 0; i--) {
				ScalePoint sp1 = list1.points.get(i);
				if (sp1.equals(sp2)) {
//					matchingFound = true;
					matchedCount2++;
					break;
				}
			}
//			if (!matchingFound)
//				System.out.println("Point No " + j + " from 2-nd list has no match in 1-st list /X=" + sp2.doubleX + ",Y=" + sp2.doubleY);
		}
		
		System.out.println("Matched 1-st list against 2-nd list: " + matchedCount1 + "/" + list1.points.size());
		System.out.println("Matched 2-nd list against 1-st list: " + matchedCount2 + "/" + list2.points.size());
//		System.out.println("Finished!");
	}	
	
	public static void main(String[] args) throws Exception {
//		fou = new PrintWriter("./../../images/debug.my");
		String imgDir = "./../../images/";
//		String fn = imgDir + "HPIM0336.JPG";
//		String fn = imgDir + "testimg.bmp";

		DLoweDetector ld = new DLoweDetector();

//		String fn = args[0];
//		ld.processImage(fn);
//		ld.loadAutoPanoFile(XMLHelper.chageFileExtension(fn, "KEY"));
//		System.out.println("Processing file " + fn);
//		ld.compareKeyLists(ld.scalePointList, ld.autoPanoPoints);
		
//		fou.close();
		
		ld.processImage(imgDir + "HPIM0336.JPG");
		ld.processImage(imgDir + "HPIM0337.JPG");
		
//		DImageMap img = new DImageMap(new File("./../../images/testimg.bmp"));
//		Marker.mark();
//		ld.DetectFeatures(img, 3, 32);
//		Marker.release();
//		ld.scalePointList.toXmlFile(new File("./../../images/my_keys.xml"));
//		Test.compareKeyFiles();
	}
}