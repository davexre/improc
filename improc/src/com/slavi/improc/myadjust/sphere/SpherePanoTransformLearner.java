package com.slavi.improc.myadjust.sphere;

import java.util.ArrayList;

import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.myadjust.CalculatePanoramaParams;
import com.slavi.improc.myadjust.PanoTransformer;
import com.slavi.math.MathUtil;
import com.slavi.math.RotationZYZ;
import com.slavi.math.SphericalCoordsLongZen;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.TransformLearnerResult;

public class SpherePanoTransformLearner extends PanoTransformer {

	static boolean adjustForScale = true;
	static boolean adjustOriginForScale = true;
	
	LeastSquaresAdjust lsa;
	double discrepancyThreshold;
	protected int iteration; 
	private double oneOverSumWeights;

	private static double getFocalDistance(KeyPointList image) {
		return Math.max(image.imageSizeX, image.imageSizeY) / 
			(2.0 * Math.tan(image.fov / 2.0));
	}
	
	private static double getFOV(KeyPointList image) {
		return 2.0 * Math.atan2(
				Math.max(image.imageSizeX, image.imageSizeY),
				2.0 * image.scaleZ);
	}
	
	public void initialize(ArrayList<KeyPointPairList> chain) {
		this.chain = chain;
		this.images = new ArrayList<KeyPointList>();
		this.ignoredPairLists = new ArrayList<KeyPointPairList>();
		this.discrepancyThreshold = 30.0 / 60.0; // 5 angular minutes
		iteration = 0;
		oneOverSumWeights = 1.0;
		for (KeyPointPairList pairList : chain) {
			pairList.source.fov = KeyPointList.defaultCameraFieldOfView;
			pairList.target.fov = KeyPointList.defaultCameraFieldOfView;
			double f = getFocalDistance(pairList.source);
			double r = Math.sqrt(pairList.translateX * pairList.translateX + pairList.translateY * pairList.translateY);
			pairList.sphereRZ1 = Math.atan2(pairList.translateY, pairList.translateX);
			pairList.sphereRY = -Math.atan2(r, f);
			pairList.sphereRZ2 = pairList.angle - pairList.sphereRZ1;
		}
	}
	
	public double getDiscrepancyThreshold() {
		return discrepancyThreshold;
	}
	
	double wRot[] = new double[] { -90 * MathUtil.deg2rad, 90 * MathUtil.deg2rad, 0 * MathUtil.deg2rad }; 
	
	public void transformForeward(double sx, double sy, KeyPointList image, double dest[]) {
		SphereNorm.transformForeward(sx, sy, image, dest);
		SphericalCoordsLongZen.rotateForeward(dest[0], dest[1], wRot[0], wRot[1], wRot[2], dest);
		dest[0] = -dest[0];
	}
	
	public void transformBackward(double rx, double ry, KeyPointList image, double dest[]) {
		SphericalCoordsLongZen.rotateBackward(-rx, ry, wRot[0], wRot[1], wRot[2], dest);
		SphereNorm.transformBackward(dest[0], dest[1], image, dest);
	}

	public static void calculatePrims(KeyPointList origin, ArrayList<KeyPointList> images, ArrayList<KeyPointPairList> chain) {
		origin.sphereRZ1 = 0.0;
		origin.sphereRY = 0.0;
		origin.sphereRZ2 = 0.0;
		origin.fov = KeyPointList.defaultCameraFieldOfView;
		origin.scaleZ = getFocalDistance(origin);
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
					angles[0] = minHopPairList.sphereRZ1;
					angles[1] = minHopPairList.sphereRY;
					angles[2] = minHopPairList.sphereRZ2;
//					RotationZYZ.instance.getRotationAnglesBackword(angles[0], angles[1], angles[2], angles);
					Matrix sourceToTarget = RotationZYZ.instance.makeAngles(angles[0], angles[1], angles[2]);
					Matrix targetToWorld = RotationZYZ.instance.makeAngles(minHopPairList.target.sphereRZ1, minHopPairList.target.sphereRY, minHopPairList.target.sphereRZ2);
					Matrix sourceToWorld = new Matrix(3, 3);
					sourceToTarget.mMul(targetToWorld, sourceToWorld);
					RotationZYZ.instance.getRotationAngles(sourceToWorld, angles);
					curImage.sphereRZ1 = angles[0];
					curImage.sphereRY = angles[1];
					curImage.sphereRZ2 = angles[2];
					if (adjustForScale) {
						curImage.scaleZ = minHopPairList.target.scaleZ * minHopPairList.scale;
						curImage.fov = getFOV(curImage);
					}
				} else { // if (curImage == minHopPairList.target) {
					double angles[] = new double[3];
					angles[0] = minHopPairList.sphereRZ1;
					angles[1] = minHopPairList.sphereRY;
					angles[2] = minHopPairList.sphereRZ2;
					RotationZYZ.instance.getRotationAnglesBackword(angles[0], angles[1], angles[2], angles);
					Matrix targetToSource = RotationZYZ.instance.makeAngles(angles[0], angles[1], angles[2]);
					Matrix sourceToWorld = RotationZYZ.instance.makeAngles(minHopPairList.source.sphereRZ1, minHopPairList.source.sphereRY, minHopPairList.source.sphereRZ2);
					Matrix targetToWorld = new Matrix(3, 3);
					targetToSource.mMul(sourceToWorld, targetToWorld);
					RotationZYZ.instance.getRotationAngles(targetToWorld, angles);
					curImage.sphereRZ1 = angles[0];
					curImage.sphereRY = angles[1];
					curImage.sphereRZ2 = angles[2];
					if (adjustForScale) {
						curImage.scaleZ = minHopPairList.source.scaleZ / minHopPairList.scale; 
						curImage.fov = getFOV(curImage);
					}
				}
			
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
		lsa.clear();
		Matrix coefs = new Matrix((adjustOriginForScale ? 1 : 0) + images.size() * (adjustForScale ? 4 : 3), 1);			
		SphereNorm sn = new SphereNorm();
		for (KeyPointPairList pairList : chain) {
			for (KeyPointPair item : pairList.items) {
				if (isBad(item))
					continue;
				
				sn.setKeyPointPair(item);
				double computedWeight = getComputedWeight(item);
				int srcIndexOf = images.indexOf(pairList.source);
				int destIndexOf = images.indexOf(pairList.target);
				int srcIndex = (adjustOriginForScale ? 1 : 0) + srcIndexOf * (adjustForScale ? 4 : 3);
				int destIndex = (adjustOriginForScale ? 1 : 0) + destIndexOf * (adjustForScale ? 4 : 3);
				
				coefs.make0();
				if (srcIndexOf >= 0) {
					coefs.setItem(srcIndex + 0, 0, sn.dDist_dSR1);
					coefs.setItem(srcIndex + 1, 0, sn.dDist_dSR2);
					coefs.setItem(srcIndex + 2, 0, sn.dDist_dSR3);
					if (adjustForScale) {
						coefs.setItem(srcIndex + 3, 0, sn.dDist_dSF);
					}
				} else {
					if (adjustOriginForScale)
						coefs.setItem(0, 0, sn.dDist_dSF);
				}
				if (destIndexOf >= 0) {
					coefs.setItem(destIndex + 0, 0, sn.dDist_dTR1);
					coefs.setItem(destIndex + 1, 0, sn.dDist_dTR2);
					coefs.setItem(destIndex + 2, 0, sn.dDist_dTR3);
					if (adjustForScale) {
						coefs.setItem(destIndex + 3, 0, sn.dDist_dTF);
					}
				} else {
					if (adjustOriginForScale)
						coefs.setItem(0, 0, sn.dDist_dSF);
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
					if (curIsBad) {
						setBad(item, curIsBad);
						result.oldGoodNowBad++;
						pairList.transformResult.oldGoodNowBad++;
					} else {
						if (discrepancy < pairList.transformResult.discrepancyStatistics.getAvgValue()) {
							setBad(item, curIsBad);
							result.oldBadNowGood++;
							pairList.transformResult.oldBadNowGood++;
						}
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
	
	private double calcMaxDiscrepancy(Statistics stat) {
		double res = Math.min(stat.getMaxX(), stat.getJ_End());
		if (res < discrepancyThreshold)
			res = discrepancyThreshold;
			
/*		res = stat.getJ_End();
		if (res >= stat.getMaxX()) { 
			res = (stat.getAvgValue() + stat.getMaxX()) / 2.0;
		}
		if (res < discrepancyThreshold)
			res = discrepancyThreshold;*/
		return res;
	}
	
	protected void computeDiscrepancies(TransformLearnerResult result) {
		double PW1[] = new double[3];
		double PW2[] = new double[3];

		result.discrepancyStatistics.start();
		for (KeyPointPairList pairList : chain) {
			pairList.transformResult.discrepancyStatistics.start();
			int goodCount = 0;
			for (KeyPointPair item : pairList.items) {
				// Compute for all points, so no item.isBad check
				SphereNorm.transformForeward(item.sourceSP.doubleX, item.sourceSP.doubleY, pairList.source, PW1);
				SphereNorm.transformForeward(item.targetSP.doubleX, item.targetSP.doubleY, pairList.target, PW2);
				double discrepancy = SphericalCoordsLongZen.getSphericalDistance(PW1[0], PW1[1], PW2[0], PW2[1]) * MathUtil.rad2deg;
				setDiscrepancy(item, discrepancy);
				if (!isBad(item)) {
					double weight = getWeight(item);
					pairList.transformResult.discrepancyStatistics.addValue(discrepancy, weight);
					result.discrepancyStatistics.addValue(discrepancy, weight);
					goodCount++;
				}
			}
			pairList.transformResult.discrepancyStatistics.stop();
			pairList.maxDiscrepancy = calcMaxDiscrepancy(pairList.transformResult.discrepancyStatistics);
		}
		result.discrepancyStatistics.stop();
		result.maxAllowedDiscrepancy = calcMaxDiscrepancy(result.discrepancyStatistics);
		return;
	}
	
	public TransformLearnerResult calculateOne() {
		TransformLearnerResult result = new TransformLearnerResult();

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

		lsa = new LeastSquaresAdjust((adjustOriginForScale ? 1 : 0) + images.size() * (adjustForScale ? 4 : 3), 1);
		calculateNormalEquations();
		// Calculate Unknowns
/*		Matrix m1 = lsa.getNm().makeSquareMatrix();
		Matrix m2 = lsa.getNm().makeSquareMatrix();
		Matrix m3 = new Matrix();
		if (!m2.inverse())
			System.out.println("FAILED!!!!!");
//			throw new RuntimeException("failed");
		m1.printM("M1");
		System.out.println("DET=" + m1.det());
		m2.printM("M2");
		m1.mMul(m2, m3);		
		m3.printM("M3");
*/
		if (!lsa.calculate()) 
			return result;
		// Build transformer
		Matrix u = lsa.getUnknown();
//		u.printM("U");
		System.out.println(origin.imageFileStamp.getFile().getName() + 
				"\trz1=" + MathUtil.rad2degStr(origin.sphereRZ1) + 
				"\try=" + MathUtil.rad2degStr(origin.sphereRY) + 
				"\trz2=" + MathUtil.rad2degStr(origin.sphereRZ2) + 
				"\tFOV=" + MathUtil.rad2degStr(origin.fov) + 
				"\tdFOV=" + MathUtil.rad2degStr(u.getItem(0, 0))
				);
		if (adjustOriginForScale) {
			origin.fov = (origin.fov - u.getItem(0, 0));
			origin.scaleZ = getFocalDistance(origin);
		}
		
		for (int curImage = 0; curImage < images.size(); curImage++) {
			KeyPointList image = images.get(curImage);
			int index = (adjustOriginForScale ? 1 : 0) + curImage * (adjustForScale ? 4 : 3);
			System.out.println(image.imageFileStamp.getFile().getName() + 
					"\trz1=" + MathUtil.rad2degStr(image.sphereRZ1) + 
					"\try=" + MathUtil.rad2degStr(image.sphereRY) + 
					"\trz2=" + MathUtil.rad2degStr(image.sphereRZ2) + 
					"\tFOV=" + MathUtil.rad2degStr(image.fov) + 
					"\tdz1=" + MathUtil.rad2degStr(u.getItem(0, index + 0)) + 
					"\tdy=" + MathUtil.rad2degStr(u.getItem(0, index + 1)) + 
					"\tdz2=" + MathUtil.rad2degStr(u.getItem(0, index + 2)) + 
					(adjustForScale ? "\tdFOV=" + MathUtil.rad2degStr(u.getItem(0, index + 3)) : "") 
					);
			image.sphereRZ1 = MathUtil.fixAngle2PI(image.sphereRZ1 - u.getItem(0, index + 0));
			image.sphereRY = MathUtil.fixAngle2PI(image.sphereRY - u.getItem(0, index + 1));
			image.sphereRZ2 = MathUtil.fixAngle2PI(image.sphereRZ2 - u.getItem(0, index + 2));
			if (adjustForScale) {
				image.fov = (image.fov - u.getItem(0, index + 3));
				image.scaleZ = getFocalDistance(image);
			}
		}
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result;
	}
}
