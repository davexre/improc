package com.slavi.improc.myadjust;

import java.util.ArrayList;

import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.math.MathUtil;
import com.slavi.math.transform.TransformLearnerResult;

public abstract class PanoTransformer {

	public ArrayList<KeyPointPairList> chain;
	public ArrayList<KeyPointList> images;
	public ArrayList<KeyPointPairList> ignoredPairLists;
	public KeyPointList origin;
	protected int iteration; 
	
	public abstract void initialize(ArrayList<KeyPointPairList> chain);
	
	public abstract TransformLearnerResult calculateOne();
	
	public static final double maxDiscrepancyInPixelsOfOriginImage = 25;
	
	public abstract double getDiscrepancyThreshold();
	
	/**
	 * Transforms from source image coordinate system into world coord.system.
	 * @param sx, sy	Coordinates in pixels of the source image with origin pixel(0,0)
	 * @param dest[3]	OUTPUT: The transformed coordinates in radians.  
	 * 					dest[0] = Longitude
	 * 					dest[1] = Zenith (pi/2-Latitude)
	 * 					dest[2] = r - radius, i.e. distance from the image pixel
	 * 						to the focal point or origin of the 3D image coordinate system. 
	 */
	public abstract void transformForeward(double sx, double sy, KeyPointList srcImage, double dest[]);
	
	/**
	 * Transforms from world coordinate system into source image coord.system. 
	 * @param rx		Longitude
	 * @param ry		Zenith (pi/2-Latitude)
	 * @param dest[3]	OUTPUT: The transformed coordinates. 
	 * 					dest[0] = x in image coordinates
	 * 					dest[1] = y in image coordinates
	 * 					dest[2] = r - radius, i.e. distance from the image pixel
	 * 						to the focal point or origin of the 3D image coordinate system. 
	 * 						If r > 0 coordinates are ok.
	 * 						If r <=0 the specified rx,ry are outside of the source image (on 
	 * 						the opposite side of the sphere) 
	 */
	public abstract void transformBackward(double rx, double ry, KeyPointList srcImage, double dest[]);
	
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
			pairList.transformResult.newBadCount = 0;
			pairList.transformResult.newGoodCount = 0;
			pairList.transformResult.oldGoodNowBad = 0;
			pairList.transformResult.oldBadNowGood = 0;
			
			for (KeyPointPair item : pairList.items) {
				boolean oldIsBad = isBad(item);
				double discrepancy = getDiscrepancy(item);
				boolean curIsBad = discrepancy > pairList.maxDiscrepancy;
				if (oldIsBad != curIsBad) {
					if (curIsBad) {
						setBad(item, curIsBad);
						result.oldGoodNowBad++;
						pairList.transformResult.oldGoodNowBad++;
					} else {
						if (discrepancy < pairList.recoverDiscrepancy) {
							setBad(item, curIsBad);
							result.oldBadNowGood++;
							pairList.transformResult.oldBadNowGood++;
						} else {
							curIsBad = true; // rejected to be recovered
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
					"\tmax=" + MathUtil.d4(pairList.maxDiscrepancy) + 
					"\trecover=" + MathUtil.d4(pairList.recoverDiscrepancy) + 
					"\tjend=" + MathUtil.d4(pairList.transformResult.discrepancyStatistics.getJ_End()) + 
					"\tmaxX=" + MathUtil.d4(pairList.transformResult.discrepancyStatistics.getMaxX()) + 
					"\tminX=" + MathUtil.d4(pairList.transformResult.discrepancyStatistics.getMinX()) + 
					"\tavg=" + MathUtil.d4(pairList.transformResult.discrepancyStatistics.getAvgValue()) + 
					"\toldBadNowGood=" + String.format("%4d", pairList.transformResult.oldBadNowGood) +
					"\toldGoodNowBad=" + String.format("%4d", pairList.transformResult.oldGoodNowBad) +
					"\tgoodRatio=" + MathUtil.d2(pairList.transformResult.getGoodDataRatio()) + "%" +
					"\t" + pairList.transformResult.newGoodCount + "/" + pairList.transformResult.dataCount
					);
		}
	}
	
	protected double computeOneDiscrepancy(KeyPointPair item, double PW1[], double PW2[]) {
		transformForeward(item.sourceSP.doubleX, item.sourceSP.doubleY, item.sourceSP.keyPointList, PW1);
		transformForeward(item.targetSP.doubleX, item.targetSP.doubleY, item.targetSP.keyPointList, PW2);
		return MathUtil.hypot(PW1[0] - PW2[0], PW1[1] - PW2[1]);
	}
	
	protected void computeDiscrepancies(TransformLearnerResult result) {
		double PW1[] = new double[3];
		double PW2[] = new double[3];

		result.discrepancyStatistics.start();
		for (KeyPointPairList pairList : chain) {
			pairList.transformResult.discrepancyStatistics.start();
			for (KeyPointPair item : pairList.items) {
				// Compute for all points, so no item.isBad check
				double discrepancy = computeOneDiscrepancy(item, PW1, PW2);
				setDiscrepancy(item, discrepancy);
				if (!isBad(item)) {
					double weight = getComputedWeight(item);
					pairList.transformResult.discrepancyStatistics.addValue(discrepancy, weight);
					result.discrepancyStatistics.addValue(discrepancy, weight);
				}
			}
			pairList.transformResult.discrepancyStatistics.stop();
		}
		result.discrepancyStatistics.stop();
			
		double discrepancyThreshold = getDiscrepancyThreshold();
		result.maxAllowedDiscrepancy = Math.min(result.discrepancyStatistics.getJ_End(),
			(result.discrepancyStatistics.getAvgValue() + 
					result.discrepancyStatistics.getMaxX()) / 2.0);
		if (result.maxAllowedDiscrepancy < discrepancyThreshold)
			result.maxAllowedDiscrepancy = discrepancyThreshold;
/*
		for (KeyPointPairList pairList : chain) {
			pairList.maxDiscrepancy = pairList.transformResult.discrepancyStatistics.getJ_End(); 
			if (pairList.maxDiscrepancy >= pairList.transformResult.discrepancyStatistics.getMaxX()) {
				pairList.maxDiscrepancy = (pairList.transformResult.discrepancyStatistics.getAvgValue() +
						pairList.transformResult.discrepancyStatistics.getMaxX()) / 2.0;
			}
			if (pairList.maxDiscrepancy > result.maxAllowedDiscrepancy) {
				pairList.maxDiscrepancy = (pairList.transformResult.discrepancyStatistics.getAvgValue() +
						result.maxAllowedDiscrepancy) / 2.0;
			}
			if (pairList.maxDiscrepancy <= pairList.transformResult.discrepancyStatistics.getAvgValue()) {
				pairList.maxDiscrepancy = pairList.transformResult.discrepancyStatistics.getAvgValue();
			}
			if (pairList.maxDiscrepancy < discrepancyThreshold)
				pairList.maxDiscrepancy = discrepancyThreshold;

			pairList.recoverDiscrepancy = (pairList.transformResult.discrepancyStatistics.getAvgValue() +
					pairList.maxDiscrepancy) / 2.0;
			if (pairList.recoverDiscrepancy > pairList.maxDiscrepancy)
				pairList.recoverDiscrepancy = (pairList.transformResult.discrepancyStatistics.getAvgValue() +
						pairList.transformResult.discrepancyStatistics.getMinX()) / 2.0;
			if (pairList.recoverDiscrepancy > discrepancyThreshold)
				pairList.recoverDiscrepancy = discrepancyThreshold;
		}
*/		
		for (KeyPointPairList pairList : chain) {
			pairList.maxDiscrepancy =  
				(pairList.transformResult.discrepancyStatistics.getAvgValue() + 
				pairList.transformResult.discrepancyStatistics.getAvgValue() +
				pairList.transformResult.discrepancyStatistics.getMaxX()) / 3.0;
			if (pairList.maxDiscrepancy < result.discrepancyStatistics.getAvgValue())
				pairList.maxDiscrepancy = pairList.transformResult.discrepancyStatistics.getMaxX();
			if (pairList.transformResult.discrepancyStatistics.getMaxX() < discrepancyThreshold)
				pairList.maxDiscrepancy = pairList.transformResult.discrepancyStatistics.getMaxX();
			pairList.recoverDiscrepancy = pairList.transformResult.discrepancyStatistics.getAvgValue();
		}

	}
	
	protected boolean removeBadKeyPointPairLists() {
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
			CalculatePanoramaParams.copyBadStatus(chain);
			ArrayList<KeyPointPairList> tmpChain = CalculatePanoramaParams.getImageChain(chain);
			ignoredPairLists.addAll(chain);
			chain = tmpChain;
		}
		return chainModified;
	}
	
	public void showValidateKeyPointPairStatistic() {
		int ALL_panoGood = 0;
		int ALL_panoBad = 0;
		int ALL_validateGood = 0;
		int ALL_validateBad = 0;
		int ALL_validateGoodPanoGood = 0;
		int ALL_validateGoodPanoBad = 0;
		int ALL_validateBadPanoGood = 0;
		int ALL_validateBadPanoBad = 0;

		System.out.println();
		for (int i = chain.size() - 1; i >= 0; i--) {
			KeyPointPairList pairList = chain.get(i);
			int panoGood = 0;
			int panoBad = 0;
			int validateGood = 0;
			int validateBad = 0;
			int validateGoodPanoGood = 0;
			int validateGoodPanoBad = 0;
			int validateBadPanoGood = 0;
			int validateBadPanoBad = 0;

			for (KeyPointPair pair : pairList.items) {
				if (pair.validatePairBad) {
					validateBad++;
					if (pair.panoBad) {
						panoBad++;
						validateBadPanoBad++;
					} else {
						panoGood++;
						validateBadPanoGood++;
					}
				} else {
					validateGood++;
					if (pair.panoBad) {
						panoBad++;
						validateGoodPanoBad++;
					} else {
						panoGood++;
						validateGoodPanoGood++;
					}
				}
			}
			
			ALL_panoGood += panoGood;
			ALL_panoBad += panoBad;
			ALL_validateGood += validateGood;
			ALL_validateBad += validateBad;
			ALL_validateGoodPanoGood += validateGoodPanoGood;
			ALL_validateGoodPanoBad += validateGoodPanoBad;
			ALL_validateBadPanoGood += validateBadPanoGood;
			ALL_validateBadPanoBad += validateBadPanoBad;
			
			System.out.println(
					pairList.source.imageFileStamp.getFile().getName() +
					"\t" + pairList.target.imageFileStamp.getFile().getName() +
					"\tvalidateGoodPanoGood " + MathUtil.l10(validateGoodPanoGood) +
					"\tvalidateGoodPanoBad  " + MathUtil.l10(validateGoodPanoBad) +
					"\tvalidateBadPanoGood  " + MathUtil.l10(validateBadPanoGood) +
					"\tvalidateBadPanoBad   " + MathUtil.l10(validateBadPanoBad) +
					"\tpanoGood " + MathUtil.l10(panoGood) +
					"\tpanoBad  " + MathUtil.l10(panoBad) +
					"\tvalidateGood  " + MathUtil.l10(validateGood) +
					"\tvalidateBad   " + MathUtil.l10(validateBad)
			);
		}
		System.out.println(
				"--- total ---" +
				"\t--- total ---" +
				"\tvalidateGoodPanoGood " + MathUtil.l10(ALL_validateGoodPanoGood) +  
				"\tvalidateGoodPanoBad  " + MathUtil.l10(ALL_validateGoodPanoBad) +  
				"\tvalidateBadPanoGood  " + MathUtil.l10(ALL_validateBadPanoGood) +  
				"\tvalidateBadPanoBad   " + MathUtil.l10(ALL_validateBadPanoBad) +
				"\tpanoGood " + MathUtil.l10(ALL_panoGood) +
				"\tpanoBad  " + MathUtil.l10(ALL_panoBad) +
				"\tvalidateGood   " + MathUtil.l10(ALL_validateGood) +
				"\tvalidateBad    " + MathUtil.l10(ALL_validateBad)
		);
		System.out.println();
	}
}
