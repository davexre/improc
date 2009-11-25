package com.slavi.improc.myadjust.sphere3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.myadjust.xyz.CalculatePanoramaParams;
import com.slavi.math.MathUtil;
import com.slavi.math.RotationZXZ;
import com.slavi.math.SphericalCoordsLongZen;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.TransformLearnerResult;

public class SpherePanoTransformLearner3 {

	static boolean adjustForScale = true;
	
	ArrayList<KeyPointPairList> chain;
	ArrayList<KeyPointList> images;
	ArrayList<KeyPointPairList> ignoredPairLists;
	KeyPointList origin;
	LeastSquaresAdjust lsa;
	double discrepancyThreshold;
	protected int iteration = 0; 

	public SpherePanoTransformLearner3(ArrayList<KeyPointPairList> chain) {
		this.chain = chain;
		this.images = new ArrayList<KeyPointList>();
		this.ignoredPairLists = new ArrayList<KeyPointPairList>();
	}
	
	public static void buildImagesList(ArrayList<KeyPointPairList> chain, ArrayList<KeyPointList> images) {
		images.clear();
		for (KeyPointPairList pairList : chain) {
			if (!images.contains(pairList.source))
				images.add(pairList.source);
			if (!images.contains(pairList.target))
				images.add(pairList.target);
		}
		Collections.sort(images, new Comparator<KeyPointList>() {
			public int compare(KeyPointList o1, KeyPointList o2) {
				return o1.imageFileStamp.getFile().getName().compareTo(o2.imageFileStamp.getFile().getName());
			}
		});
	}

	public static void calculatePrims(KeyPointList origin, ArrayList<KeyPointList> images, ArrayList<KeyPointPairList> chain) {
		origin.sphereRZ1 = 0.0;
		origin.sphereRY = 0.0;
		origin.sphereRZ2 = 0.0;
		origin.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;
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
//					RotationZXZ.instance.getRotationAnglesBackword(angles[0], angles[1], angles[2], angles);
					Matrix sourceToTarget = RotationZXZ.instance.makeAngles(angles[0], angles[1], angles[2]);
					Matrix targetToWorld = RotationZXZ.instance.makeAngles(minHopPairList.target.sphereRZ1, minHopPairList.target.sphereRY, minHopPairList.target.sphereRZ2);
					Matrix sourceToWorld = new Matrix(3, 3);
					targetToWorld.mMul(sourceToTarget, sourceToWorld);
					RotationZXZ.instance.getRotationAngles(sourceToWorld, angles);
					curImage.sphereRZ1 = angles[0];
					curImage.sphereRY = angles[1];
					curImage.sphereRZ2 = angles[2];
					if (adjustForScale) {
						curImage.scaleZ = minHopPairList.target.scaleZ * minHopPairList.scale; 
					}
				} else { // if (curImage == minHopPairList.target) {
					double angles[] = new double[3];
					angles[0] = minHopPairList.sphereRZ1;
					angles[1] = minHopPairList.sphereRY;
					angles[2] = minHopPairList.sphereRZ2;
					RotationZXZ.instance.getRotationAnglesBackword(angles[0], angles[1], angles[2], angles);
					Matrix targetToSource = RotationZXZ.instance.makeAngles(angles[0], angles[1], angles[2]);
					Matrix sourceToWorld = RotationZXZ.instance.makeAngles(minHopPairList.source.sphereRZ1, minHopPairList.source.sphereRY, minHopPairList.source.sphereRZ2);
					Matrix targetToWorld = new Matrix(3, 3);
					sourceToWorld.mMul(targetToSource, targetToWorld);
					RotationZXZ.instance.getRotationAngles(targetToWorld, angles);
					curImage.sphereRZ1 = angles[0];
					curImage.sphereRY = angles[1];
					curImage.sphereRZ2 = angles[2];
					if (adjustForScale) {
						curImage.scaleZ = minHopPairList.source.scaleZ / minHopPairList.scale; 
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
/*		
		double source[] = new double[2];
		double target[] = new double[2];
		for (KeyPointPairList pairList : chain) {
			for (KeyPointPair item : pairList.items) {
				if (item.panoBad)
					continue;
				
				SpherePanoTransformer2.transformForeward(item.sourceSP.doubleX, item.sourceSP.doubleY, item.sourceSP.keyPointList, source);
				SpherePanoTransformer2.transformForeward(item.targetSP.doubleX, item.targetSP.doubleY, item.targetSP.keyPointList, target);
				double dist = SpherePanoTransformer2.getSphericalDistance(source[0], source[1], target[0], target[1]);
				System.out.println("Dist=" + MathUtil.rad2degStr(dist));
			}
		}*/
	}

	double scaleTheZ = 1;
	void calculateNormalEquations() {
		origin.sphereRZ1 = 0;
		origin.sphereRY = 0;
		origin.sphereRZ2 = 0;
		origin.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;
		
		lsa.clear();
		Matrix coefs = new Matrix(images.size() * (adjustForScale ? 4 : 3), 1);			
		SphereNorm3 sn = new SphereNorm3();
		for (KeyPointPairList pairList : chain) {
			for (KeyPointPair item : pairList.items) {
				if (isBad(item))
					continue;
				
				sn.setKeyPointPair(item);
				double computedWeight = getComputedWeight(item);
				int srcIndex = images.indexOf(pairList.source) * (adjustForScale ? 4 : 3);
				int destIndex = images.indexOf(pairList.target) * (adjustForScale ? 4 : 3);
				
				coefs.make0();
				if (srcIndex >= 0) {
					coefs.setItem(srcIndex + 0, 0, sn.dDist_dSR1);
					coefs.setItem(srcIndex + 1, 0, sn.dDist_dSR2);
					coefs.setItem(srcIndex + 2, 0, sn.dDist_dSR3);
					if (adjustForScale) {
						coefs.setItem(srcIndex + 3, 0, sn.dDist_dSF * scaleTheZ);
					}
				}
				if (destIndex >= 0) {
					coefs.setItem(destIndex + 0, 0, sn.dDist_dTR1);
					coefs.setItem(destIndex + 1, 0, sn.dDist_dTR2);
					coefs.setItem(destIndex + 2, 0, sn.dDist_dTR3);
					if (adjustForScale) {
						coefs.setItem(destIndex + 3, 0, sn.dDist_dTF * scaleTheZ);
					}
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
		double maxMaxDiscrepancy = result.discrepancyStatistics.getJ_End();
		if (maxMaxDiscrepancy >= result.discrepancyStatistics.getMaxX()) { 
			maxMaxDiscrepancy = (result.discrepancyStatistics.getAvgValue() + result.discrepancyStatistics.getMaxX()) / 2.0;
		}
		if (maxMaxDiscrepancy < discrepancyThreshold)
			maxMaxDiscrepancy = discrepancyThreshold;
		
		for (KeyPointPairList pairList : chain) {
			if (pairList.maxDiscrepancy > maxMaxDiscrepancy)
				pairList.maxDiscrepancy = maxMaxDiscrepancy;
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
						if (discrepancy < result.discrepancyStatistics.getAvgValue()) {
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
	
	protected void computeDiscrepancies(TransformLearnerResult result) {
		double PW1[] = new double[2];
		double PW2[] = new double[2];

		result.discrepancyStatistics.start();
		for (KeyPointPairList pairList : chain) {
			pairList.transformResult.discrepancyStatistics.start();
			int goodCount = 0;
			for (KeyPointPair item : pairList.items) {
				// Compute for all points, so no item.isBad check
				SpherePanoTransformer3.transformForeward(item.sourceSP.doubleX, item.sourceSP.doubleY, pairList.source, PW1);
				SpherePanoTransformer3.transformForeward(item.targetSP.doubleX, item.targetSP.doubleY, pairList.target, PW2);
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
			pairList.maxDiscrepancy = 0.8 * Math.min(
				pairList.transformResult.discrepancyStatistics.getMaxX(),
				pairList.transformResult.discrepancyStatistics.getJ_End());
/*			pairList.maxDiscrepancy = pairList.transformResult.discrepancyStatistics.getJ_End();
			if (pairList.maxDiscrepancy >= pairList.transformResult.discrepancyStatistics.getMaxX()) { 
				pairList.maxDiscrepancy = (pairList.transformResult.discrepancyStatistics.getAvgValue() + 
						pairList.transformResult.discrepancyStatistics.getMaxX()) / 2.0;
			}*/
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
			buildImagesList(chain, tmp_images);
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

		lsa = new LeastSquaresAdjust(images.size() * (adjustForScale ? 4 : 3), 1);
		calculateNormalEquations();
		// Calculate Unknowns
/*		Matrix m1 = lsa.getNm().makeSquareMatrix();
		Matrix m2 = lsa.getNm().makeSquareMatrix();
		Matrix m3 = new Matrix();
		if (!m2.inverse())
			throw new RuntimeException("failed");
		m1.printM("M1");
		System.out.println("DET=" + m1.det());
		m2.printM("M2");
		m1.mMul(m2, m3);		
		m3.printM("M3");*/
		
		if (!lsa.calculate()) 
			return result;
		// Build transformer
		Matrix u = lsa.getUnknown();
//		u.printM("U");
		System.out.println(origin.imageFileStamp.getFile().getName() + 
				"\trz1=" + MathUtil.rad2degStr(origin.sphereRZ1) + 
				"\try=" + MathUtil.rad2degStr(origin.sphereRY) + 
				"\trz2=" + MathUtil.rad2degStr(origin.sphereRZ2) + 
				"\ts=" + MathUtil.d4(origin.scaleZ)
				);
		for (int curImage = 0; curImage < images.size(); curImage++) {
			KeyPointList image = images.get(curImage);
			int index = curImage * (adjustForScale ? 4 : 3);
			System.out.println(image.imageFileStamp.getFile().getName() + 
					"\trz1=" + MathUtil.rad2degStr(image.sphereRZ1) + 
					"\try=" + MathUtil.rad2degStr(image.sphereRY) + 
					"\trz2=" + MathUtil.rad2degStr(image.sphereRZ2) + 
					"\ts=" + MathUtil.d4(image.scaleZ) +
					"\tdz1=" + MathUtil.rad2degStr(u.getItem(0, index + 0)) + 
					"\tdy=" + MathUtil.rad2degStr(u.getItem(0, index + 1)) + 
					"\tdz2=" + MathUtil.rad2degStr(u.getItem(0, index + 2)) + 
					(adjustForScale ? "\tds=" + MathUtil.d4(u.getItem(0, index + 3) / scaleTheZ) : "") 
					);
			image.sphereRZ1 = MathUtil.fixAngle2PI(image.sphereRZ1 - u.getItem(0, index + 0));
			image.sphereRY = MathUtil.fixAngle2PI(image.sphereRY - u.getItem(0, index + 1));
			image.sphereRZ2 = MathUtil.fixAngle2PI(image.sphereRZ2 - u.getItem(0, index + 2));
			if (adjustForScale) {
				image.scaleZ = (image.scaleZ - u.getItem(0, index + 3) / scaleTheZ);
			}
		}
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result;
	}
}