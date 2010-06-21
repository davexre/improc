package com.slavi.improc.myadjust.sphere;

import java.util.ArrayList;

import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.myadjust.CalculatePanoramaParams;
import com.slavi.improc.myadjust.PanoTransformer;
import com.slavi.math.MathUtil;
import com.slavi.math.RotationZYX;
import com.slavi.math.SphericalCoordsLongLat;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.TransformLearnerResult;

public class SpherePanoTransformLearner extends PanoTransformer {

	LeastSquaresAdjust lsa;
	double discrepancyThreshold;
	protected int iteration = 0; 

	public void initialize(ArrayList<KeyPointPairList> chain) {
		this.chain = chain;
		this.images = new ArrayList<KeyPointList>();
		this.ignoredPairLists = new ArrayList<KeyPointPairList>();
		this.discrepancyThreshold = 5.0 / 60.0; // 5 angular minutes
	}
	
	public double getDiscrepancyThreshold() {
		return discrepancyThreshold;
	}

	/**
	 * Transforms from source image coordinate system into world coord.system.
	 * @param sx, sy	Coordinates in pixels of the source image with origin pixel(0,0)
	 * @param dest		The transformed coordinates in radians. Longitude is 
	 * 					returned in dest[0] and is in the range (-pi; pi] and Latitude
	 * 					is returned in dest[1] in the range [-pi/2; pi/2].   
	 */
	public void transformForeward(double sx, double sy, KeyPointList srcImage, double dest[]) {
		sx -= srcImage.cameraOriginX;
		sy -= srcImage.cameraOriginY;
		// sx => longitude, sy => latitude
		sy = Math.asin(sy / Math.sqrt(sx * sx + sy * sy + srcImage.scaleZ * srcImage.scaleZ));
		sx = Math.atan2(sx, srcImage.scaleZ);
		rotateForeward(sx, sy, srcImage.rx, srcImage.ry, srcImage.rz, dest);
	}
	
	public static void rotateForeward(double sx, double sy, double IX, double IY, double IZ, double dest[]) {
		double sinDX = Math.sin(sx - IX);
		double cosDX = Math.cos(sx - IX);
		double sinIY = Math.sin(IY);
		double cosIY = Math.cos(IY);
		double sinSY = Math.sin(sy);
		double cosSY = Math.cos(sy);

		dest[0] = IZ - Math.atan2(sinDX * cosSY, cosIY * sinSY - cosDX * sinIY * cosSY);
//		dest[0] = IZ + Math.PI - Math.atan2(sinDX * cosSY, cosIY * sinSY - cosDX * sinIY * cosSY);
		dest[1] = Math.asin(sinIY * sinSY + cosIY * cosSY * cosDX);
	}

	public void transformBackward(double rx, double ry, KeyPointList srcImage, double dest[]) {
		rotateBackward(rx, ry, srcImage.rx, srcImage.ry, srcImage.rz, dest);
		// sx => longitude, sy => latitude
		dest[1] = srcImage.cameraOriginY + srcImage.scaleZ * Math.tan(dest[1]) / Math.cos(dest[0]);
		dest[0] = srcImage.cameraOriginX + srcImage.scaleZ * Math.tan(dest[0]);
	}

	public static void rotateBackward(double rx, double ry, double IX, double IY, double IZ, double dest[]) {
		rx = IZ - rx;
//		rx = IZ + Math.PI - rx;
		
		double sinIY = Math.sin(IY);
		double cosIY = Math.cos(IY);
		double sinRY = Math.sin(ry);
		double cosRY = Math.cos(ry);
		double sinRX = Math.sin(rx);
		double cosRX = Math.cos(rx);
		
		dest[0] = IX + Math.atan2(sinRX * cosRY, cosIY * sinRY - cosRX * sinIY * cosRY);
		dest[1] = Math.asin(sinIY * sinRY + cosIY * cosRY * cosRX);
	}
	
	public static void calculatePrims(KeyPointList origin, ArrayList<KeyPointList> images, ArrayList<KeyPointPairList> chain) {
		origin.rx = 0.0;
		origin.ry = 0 * MathUtil.deg2rad;
		origin.rz = 0 * MathUtil.deg2rad;
		origin.scaleZ = 0.5 * Math.max(origin.imageSizeX, origin.imageSizeY) * 
				Math.tan(0.5 * KeyPointList.defaultCameraFieldOfView);
		origin.calculatePrimsAtHop = 0;
		
		ArrayList<KeyPointList> todo = new ArrayList<KeyPointList>(images);
		for (KeyPointList image : todo)
			image.calculatePrimsAtHop = -1;
		
		int curImageIndex = todo.size() - 1;
		boolean listModified = false;
		while (curImageIndex >= 0) {
			KeyPointList curImage = todo.get(curImageIndex);
			KeyPointPairList minHopPairList = null;
			int minHop = Integer.MAX_VALUE;
			
			for (KeyPointPairList pairList : chain) {
				if (curImage == pairList.source) {
					if ((pairList.target.calculatePrimsAtHop < 0)) 
						continue;
					if ((minHopPairList == null) ||
						(minHop > pairList.target.calculatePrimsAtHop)) {
						minHopPairList = pairList;
						minHop = pairList.target.calculatePrimsAtHop;
					}
				} else if (curImage == pairList.target) {
					if ((pairList.source.calculatePrimsAtHop < 0)) 
						continue;
					if ((minHopPairList == null) ||
						(minHop > pairList.source.calculatePrimsAtHop)) {
						minHopPairList = pairList;
						minHop = pairList.source.calculatePrimsAtHop;
					}
				}
			}
			
			if (minHopPairList != null) {
				if (curImage == minHopPairList.source) {
					double angles[] = new double[3];
					Matrix sourceToTarget = RotationZYX.instance.makeAngles(-minHopPairList.rx, -minHopPairList.ry + 90 * MathUtil.deg2rad, -minHopPairList.rz - 0 * MathUtil.deg2rad);
					Matrix targetToWorld = RotationZYX.instance.makeAngles(minHopPairList.target.rx, minHopPairList.target.ry, minHopPairList.target.rz);
					Matrix sourceToWorld = new Matrix(3, 3);
					sourceToTarget.mMul(targetToWorld, sourceToWorld);
					RotationZYX.instance.getRotationAngles(sourceToWorld, angles);
					curImage.rx = angles[0];
					curImage.ry = angles[1];
					curImage.rz = angles[2];
					curImage.scaleZ = minHopPairList.target.scaleZ * minHopPairList.scale; 
				} else { // if (curImage == minHopPairList.target) {
					double angles[] = new double[3];
					RotationZYX.instance.getRotationAnglesBackword(-minHopPairList.rx, -minHopPairList.ry + 90 * MathUtil.deg2rad, -minHopPairList.rz - 0 * MathUtil.deg2rad, angles);
					Matrix targetToSource = RotationZYX.instance.makeAngles(angles[0], angles[1], angles[2]);
					Matrix sourceToWorld = RotationZYX.instance.makeAngles(minHopPairList.source.rx, minHopPairList.source.ry, minHopPairList.source.rz);
					Matrix targetToWorld = new Matrix(3, 3);
					targetToSource.mMul(sourceToWorld, targetToWorld);
					RotationZYX.instance.getRotationAngles(targetToWorld, angles);
					curImage.rx = angles[0];
					curImage.ry = angles[1];
					curImage.rz = angles[2];
					curImage.scaleZ = minHopPairList.source.scaleZ / minHopPairList.scale; 
				}
			
/*			if (minHopPairList != null) {
				if (curImage == minHopPairList.source) {
					double dest[] = new double[2];
					SpherePanoTransformer.rotateBackward(
							minHopPairList.target.rx, minHopPairList.target.ry, 
							minHopPairList.rx, minHopPairList.ry, minHopPairList.rz, dest);
					curImage.rx = MathUtil.fixAngleMPI_PI(dest[0]);
					curImage.ry = MathUtil.fixAngleMPI_PI(dest[1]);
					curImage.rz = MathUtil.fixAngleMPI_PI(minHopPairList.rz + minHopPairList.source.rz);
					curImage.scaleZ = minHopPairList.source.scaleZ * minHopPairList.scale; 
//					System.out.println(curImage.imageFileStamp.getFile().getName() + "\t" + minHopPairList.target.imageFileStamp.getFile().getName());
				} else { // if (curImage == minHopPairList.target) {
					double dest[] = new double[2];
					SpherePanoTransformer.rotateForeward(
							minHopPairList.source.rx, minHopPairList.source.ry, 
							minHopPairList.rx, minHopPairList.ry, 0, dest);
					curImage.rx = MathUtil.fixAngleMPI_PI(dest[0]);
					curImage.ry = MathUtil.fixAngleMPI_PI(dest[1]);
					curImage.rz = MathUtil.fixAngleMPI_PI(Math.PI - (minHopPairList.rz - minHopPairList.source.rz));
					curImage.scaleZ = minHopPairList.source.scaleZ / minHopPairList.scale; 
//					System.out.println(curImage.imageFileStamp.getFile().getName() + "\t" + minHopPairList.source.imageFileStamp.getFile().getName());
				}*/
				curImage.calculatePrimsAtHop = minHop + 1;
				todo.remove(curImageIndex);
				curImageIndex = todo.size();
				listModified = true;
			}
			curImageIndex--;
			if (curImageIndex < 0) {
				if (!listModified)
					break;
				curImageIndex = todo.size() - 1;
				listModified = false;
			}
		}
		
		if (todo.size() > 0) 
			throw new RuntimeException("Failed calculating the prims");
	}

	void calculateNormalEquations() {
		origin.rx = 0;
		origin.ry = 0;
		origin.rz = 0;
		origin.scaleZ = 0.5 * Math.max(origin.imageSizeX, origin.imageSizeY) * 
				Math.tan(0.5 * KeyPointList.defaultCameraFieldOfView);
		
		lsa.clear();
		Matrix coefs = new Matrix(images.size() * 4, 1);			
		SphereNorm sn = new SphereNorm();
		for (KeyPointPairList pairList : chain) {
			for (KeyPointPair item : pairList.items) {
				if (isBad(item))
					continue;
				
				sn.setKeyPointPair(item);
				double computedWeight = getComputedWeight(item);
				int srcIndex = images.indexOf(pairList.source) * 4;
				int destIndex = images.indexOf(pairList.target) * 4;
				
				coefs.make0();
				if (srcIndex >= 0) {
					coefs.setItem(srcIndex + 0, 0, sn.dDist_dSR1);
					coefs.setItem(srcIndex + 1, 0, sn.dDist_dSR2);
					coefs.setItem(srcIndex + 2, 0, sn.dDist_dSR3);
					coefs.setItem(srcIndex + 3, 0, sn.dDist_dSF);
				}
				if (destIndex >= 0) {
					coefs.setItem(destIndex + 0, 0, sn.dDist_dTR1);
					coefs.setItem(destIndex + 1, 0, sn.dDist_dTR2);
					coefs.setItem(destIndex + 2, 0, sn.dDist_dTR3);
					coefs.setItem(destIndex + 3, 0, sn.dDist_dTF);
				}
				lsa.addMeasurement(coefs, computedWeight, sn.Dist, 0);
			}
		}
	}
	
	public void setDiscrepancy(KeyPointPair item, double discrepancy) {
		item.discrepancy = discrepancy;
	}

	public double getDiscrepancy(KeyPointPair item) {
		return item.discrepancy;
	}
	
	public void setBad(KeyPointPair item, boolean bad) {
		item.panoBad = bad;
	}

	public boolean isBad(KeyPointPair item) {
		return item.panoBad;
	}

	public double getWeight(KeyPointPair item) {
		return item.weight;
	}
	
	public double getComputedWeight(KeyPointPair item) {
		return isBad(item) ? 0.0 : getWeight(item) * oneOverSumWeights; 
	}

	private double oneOverSumWeights = 1.0;
	/**
	 * @return Number of point pairs NOT marked as bad.
	 */
	protected void computeWeights(TransformLearnerResult result) {
		result.iteration = ++iteration;
		result.dataCount = 0;
		result.oldBadCount = 0;
		result.oldGoodCount = 0;

		for (KeyPointList image : images) {
			image.goodCount = 0;
		}
		double sumWeight = 0;
		for (KeyPointPairList pairList : chain) {
			result.dataCount += pairList.items.size();
			pairList.transformResult = new TransformLearnerResult();
			pairList.transformResult.iteration = result.iteration;
			pairList.transformResult.dataCount = pairList.items.size();
			pairList.transformResult.oldBadCount = 0;
			pairList.transformResult.oldGoodCount = 0;
			
			for (KeyPointPair item : pairList.items) {
				if (isBad(item)) {
					result.oldBadCount++;
					pairList.transformResult.oldBadCount++;
					continue;
				}
				result.oldGoodCount++;
				pairList.transformResult.oldGoodCount++;
				item.sourceSP.keyPointList.goodCount++;
				item.targetSP.keyPointList.goodCount++;
				double weight = getWeight(item); 
				if (weight < 0)
					throw new IllegalArgumentException("Negative weight received.");
				sumWeight += weight;
			}
		}
		if (sumWeight == 0.0) {
			oneOverSumWeights = 1.0 / result.oldGoodCount;
		} else {
			oneOverSumWeights = 1.0 / sumWeight;
		}
	}
	
	protected void computeBad(TransformLearnerResult result) {
		result.newBadCount = 0;
		result.newGoodCount = 0;
		result.oldGoodNowBad = 0;
		result.oldBadNowGood = 0;
		result.maxAllowedDiscrepancy = result.discrepancyStatistics.getJ_End();
		if (result.maxAllowedDiscrepancy >= result.discrepancyStatistics.getMaxX()) { 
			result.maxAllowedDiscrepancy = (result.discrepancyStatistics.getAvgValue() + result.discrepancyStatistics.getMaxX()) / 2.0;
		}
		if (result.maxAllowedDiscrepancy < discrepancyThreshold)
			result.maxAllowedDiscrepancy = discrepancyThreshold;
		
		for (KeyPointPairList pairList : chain) {
			if (pairList.maxDiscrepancy > result.maxAllowedDiscrepancy)
				pairList.maxDiscrepancy = result.maxAllowedDiscrepancy;
			double maxDiscrepancy = pairList.maxDiscrepancy;
			pairList.transformResult.newBadCount = 0;
			pairList.transformResult.newGoodCount = 0;
			pairList.transformResult.oldGoodNowBad = 0;
			pairList.transformResult.oldBadNowGood = 0;
			
			for (KeyPointPair item : pairList.items) {
				boolean oldIsBad = isBad(item);
				double discrepancy = getDiscrepancy(item);
				boolean curIsBad = discrepancy > maxDiscrepancy;
				if (oldIsBad != curIsBad) {
					setBad(item, curIsBad);
					if (curIsBad) {
						result.oldGoodNowBad++;
						pairList.transformResult.oldGoodNowBad++;
					} else {
						result.oldBadNowGood++;
						pairList.transformResult.oldBadNowGood++;
					}
				}
				if (curIsBad) {
					result.newBadCount++;
					pairList.transformResult.newBadCount++;
				} else {
					result.newGoodCount++;
					pairList.transformResult.newGoodCount++;
				}
			}

			System.out.println(
					pairList.source.imageFileStamp.getFile().getName() + "\t" +
					pairList.target.imageFileStamp.getFile().getName() +
					"\tmax=" + MathUtil.d4(pairList.maxDiscrepancy) + " deg" +
					"\tjend=" + MathUtil.d4(pairList.transformResult.discrepancyStatistics.getJ_End()) + " deg" +
					"\tmaxX=" + MathUtil.d4(pairList.transformResult.discrepancyStatistics.getMaxX()) + " deg" +
					"\tavg=" + MathUtil.d4(pairList.transformResult.discrepancyStatistics.getAvgValue()) + " deg" +
					"\toldBadNowGood=" + String.format("%4d", pairList.transformResult.oldBadNowGood) +
					"\toldGoodNowBad=" + String.format("%4d", pairList.transformResult.oldGoodNowBad) +
					"\tgoodRatio=" + MathUtil.d2(pairList.transformResult.getGoodDataRatio()) + "%" +
					"\t" + pairList.transformResult.newGoodCount + "/" + pairList.transformResult.dataCount
					);
		}
	}
	
	protected void computeDiscrepancies(TransformLearnerResult result) {
		double PW1[] = new double[2];
		double PW2[] = new double[2];

		result.discrepancyStatistics.start();
		for (KeyPointPairList pairList : chain) {
			pairList.transformResult.discrepancyStatistics.start();
			int goodCount = 0;
			for (KeyPointPair item : pairList.items) {
				// Compute for all points, so no item.isBad check
				transformForeward(item.sourceSP.doubleX, item.sourceSP.doubleY, pairList.source, PW1);
				transformForeward(item.targetSP.doubleX, item.targetSP.doubleY, pairList.target, PW2);
				double discrepancy = SphericalCoordsLongLat.getSphericalDistance(PW1[0], PW1[1], PW2[0], PW2[1]) * MathUtil.rad2deg;
				setDiscrepancy(item, discrepancy);
				if (!isBad(item)) {
					double weight = getWeight(item);
					pairList.transformResult.discrepancyStatistics.addValue(discrepancy, weight);
					result.discrepancyStatistics.addValue(discrepancy, weight);
					goodCount++;
				}
			}
			pairList.transformResult.discrepancyStatistics.stop();
			pairList.maxDiscrepancy = pairList.transformResult.discrepancyStatistics.getJ_End();
			if (pairList.maxDiscrepancy >= pairList.transformResult.discrepancyStatistics.getMaxX()) { 
				pairList.maxDiscrepancy = (pairList.transformResult.discrepancyStatistics.getAvgValue() + 
						pairList.transformResult.discrepancyStatistics.getMaxX()) / 2.0;
			}
			if (pairList.maxDiscrepancy < discrepancyThreshold)
				pairList.maxDiscrepancy = discrepancyThreshold;
		}
		result.discrepancyStatistics.stop();
		return;
	}
	
	private boolean removeBadKeyPointPairLists() {
		boolean chainModified = false;
		for (int i = chain.size() - 1; i >= 0; i--) {
			KeyPointPairList pairList = chain.get(i);
			int goodCount = 0;
			for (KeyPointPair pair : pairList.items) {
				if (!isBad(pair))
					goodCount++;
			}
			if (goodCount < 10) {
				System.out.println("BAD PAIR: " + goodCount + "/" + pairList.items.size() +
						"\t" + pairList.source.imageFileStamp.getFile().getName() +
						"\t" + pairList.target.imageFileStamp.getFile().getName());
				chain.remove(i);
				ignoredPairLists.add(pairList);
				chainModified = true;
			}
		}
		if (chainModified) {
			ArrayList<KeyPointPairList> tmpChain = CalculatePanoramaParams.getImageChain(chain);
			ignoredPairLists.addAll(chain);
			chain = tmpChain;
		}
		return chainModified;
	}
	
	public TransformLearnerResult calculateOne() {
		TransformLearnerResult result = new TransformLearnerResult();
		discrepancyThreshold = 5.0 / 60.0; // 5 angular minutes

		boolean chainModified = removeBadKeyPointPairLists();
		if (iteration == 0) 
			chainModified = true;
		
		if (chainModified) {
			ArrayList<KeyPointList> tmp_images = new ArrayList<KeyPointList>();
			CalculatePanoramaParams.buildImagesList(chain, tmp_images);
			if (tmp_images.size() != images.size() + 1) {
				images.clear();
				images.addAll(tmp_images);
			} else {
				chainModified = false;
			}
		}
		computeWeights(result);
		if (chainModified) {
			if (images.size() <= 1)
				return result;
			System.out.println("************* COMPUTE PRIMS");
			origin = images.remove(0);
			calculatePrims(origin, images, chain);
		}

		lsa = new LeastSquaresAdjust(images.size() * 4, 1);
		calculateNormalEquations();
		// Calculate Unknowns
		if (!lsa.calculate()) 
			return result;
		// Build transformer
		Matrix u = lsa.getUnknown();
		System.out.println(origin.imageFileStamp.getFile().getName() + 
				"\trx=" + MathUtil.rad2degStr(origin.rx) + 
				"\try=" + MathUtil.rad2degStr(origin.ry) + 
				"\trz=" + MathUtil.rad2degStr(origin.rz) + 
				"\ts=" + MathUtil.d4(origin.scaleZ)
				);
		for (int curImage = 0; curImage < images.size(); curImage++) {
			KeyPointList image = images.get(curImage);
			int index = curImage * 4;
			System.out.println(image.imageFileStamp.getFile().getName() + 
					"\trx=" + MathUtil.rad2degStr(image.rx) + 
					"\try=" + MathUtil.rad2degStr(image.ry) + 
					"\trz=" + MathUtil.rad2degStr(image.rz) + 
					"\ts=" + MathUtil.d4(image.scaleZ) +
					"\tdx=" + MathUtil.rad2degStr(u.getItem(0, index + 0)) + 
					"\tdy=" + MathUtil.rad2degStr(u.getItem(0, index + 1)) + 
					"\tdz=" + MathUtil.rad2degStr(u.getItem(0, index + 2)) + 
					"\tds=" + MathUtil.d4(u.getItem(0, index + 3)) 
					);
			image.rx = MathUtil.fixAngleMPI_PI(image.rx - u.getItem(0, index + 0));
			image.ry = MathUtil.fixAngleMPI_PI(image.ry - u.getItem(0, index + 1));
			image.rz = MathUtil.fixAngleMPI_PI(image.rz - u.getItem(0, index + 2));
			image.scaleZ = (image.scaleZ - u.getItem(0, index + 3));
		}
		computeDiscrepancies(result);
//		computeBad(result);
		result.adjustFailed = false;
		return result;
	}
}
