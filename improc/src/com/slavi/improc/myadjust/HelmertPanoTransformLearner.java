package com.slavi.improc.myadjust;

import java.util.ArrayList;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.math.MathUtil;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.TransformLearnerResult;


public class HelmertPanoTransformLearner extends PanoTransformer {

	LeastSquaresAdjust lsa;
	double discrepancyThreshold;
	protected int iteration = 0; 

	public void initialize(ArrayList<KeyPointPairList> chain) {
		this.chain = chain;
		this.images = new ArrayList<KeyPointList>();
		this.ignoredPairLists = new ArrayList<KeyPointPairList>();
		this.discrepancyThreshold = 5.0; // 5 pixels
	}
	
	public double getDiscrepancyThreshold() {
		return discrepancyThreshold;
	}

	/**
	 * Transforms from source image coordinate system into world coord.system.
	 * @param sx, sy	Coordinates in pixels of the source image with origin pixel(0,0)
	 * @param dest		The transformed coordinates in radians. Longitude is 
	 * 					returned in dest[0] and is in the range (-pi; pi] and Latitude
	 * 					is returned in dest[1] in the range [-pi/2; pi/2]. dest[2] should be 1.0    
	 */
	public void transformForeward(double sx, double sy, KeyPointList srcImage, double dest[]) {
		if ((sx < 0.0) || (sx >= srcImage.imageSizeX) ||
			(sy < 0.0) || (sy >= srcImage.imageSizeY)) {
			dest[0] = 0.0;
			dest[1] = 0.0;
			dest[2] = -1.0;
			return;
		}
		dest[0] = srcImage.a * sx - srcImage.b * sy + srcImage.hTranslateX;
		dest[1] = srcImage.b * sx + srcImage.a * sy + srcImage.hTranslateY;
		dest[2] = 1.0;
	}

	public void transformBackward(double rx, double ry, KeyPointList srcImage, double dest[]) {
		rx -= srcImage.hTranslateX;
		ry -= srcImage.hTranslateY;
		dest[0] =  srcImage.a * rx + srcImage.b * ry;
		dest[1] = -srcImage.b * rx + srcImage.a * ry;
		dest[2] = 1.0;
		if ((dest[0] < 0.0) || (dest[0] >= srcImage.imageSizeX) ||
			(dest[1] < 0.0) || (dest[1] >= srcImage.imageSizeY)) {
			dest[0] = 0.0;
			dest[1] = 0.0;
			dest[2] = -1.0;
		}
	}
	
	void calculatePrims() {
		origin.a = 1.0;
		origin.b = 0.0;
		origin.hTranslateX = 0.0;
		origin.hTranslateY = 0.0;
		origin.calculatePrimsAtHop = 0;
		
		ArrayList<KeyPointList> todo = new ArrayList<KeyPointList>(images);
		for (KeyPointList image : todo) {
			image.calculatePrimsAtHop = -1;
		}
		for (KeyPointPairList pairList : chain) {
			pairList.a = pairList.scale * Math.cos(pairList.angle);
			pairList.b = pairList.scale * Math.sin(pairList.angle);
			pairList.hTranslateX = pairList.translateX;
			pairList.hTranslateY = pairList.translateY;
		}
		
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
					curImage.a = minHopPairList.target.a * minHopPairList.a - minHopPairList.target.b * minHopPairList.b;
					curImage.b = minHopPairList.target.b * minHopPairList.a + minHopPairList.target.a * minHopPairList.b;
					curImage.hTranslateX = minHopPairList.target.a * minHopPairList.hTranslateX - 
							minHopPairList.target.b * minHopPairList.hTranslateY + minHopPairList.target.hTranslateX;
					curImage.hTranslateY = minHopPairList.target.b * minHopPairList.hTranslateX + 
							minHopPairList.target.a * minHopPairList.hTranslateY + minHopPairList.target.hTranslateY;
//					System.out.println(curImage.imageFileStamp.getFile().getName() + "\t" + minHopPairList.target.imageFileStamp.getFile().getName());
				} else { // if (curImage == minHopPairList.target) {
					curImage.a = minHopPairList.source.a * minHopPairList.a + minHopPairList.source.b * minHopPairList.b;
					curImage.b = minHopPairList.source.b * minHopPairList.a - minHopPairList.source.a * minHopPairList.b;
					curImage.hTranslateX = - minHopPairList.source.a * minHopPairList.hTranslateX + 
							minHopPairList.source.b * minHopPairList.hTranslateY + minHopPairList.source.hTranslateX;
					curImage.hTranslateY = - minHopPairList.source.b * minHopPairList.hTranslateX - 
							minHopPairList.source.a * minHopPairList.hTranslateY + minHopPairList.source.hTranslateY;
//					System.out.println(curImage.imageFileStamp.getFile().getName() + "\t" + minHopPairList.source.imageFileStamp.getFile().getName());
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
		Matrix coefs = new Matrix(images.size() * 4, 1);			
		double PW1[] = new double[3];
		double PW2[] = new double[3];
		lsa.clear();
		for (KeyPointPairList pairList : chain) {
			for (KeyPointPair item : pairList.items) {
				if (isBad(item))
					continue;

				double computedWeight = getComputedWeight(item);
				KeyPoint source = item.getKey();
				KeyPoint target = item.getValue();
				
				transformForeward(source.doubleX, source.doubleY, source.keyPointList, PW1);
				transformForeward(target.doubleX, target.doubleY, target.keyPointList, PW2);
				
				int srcIndex = images.indexOf(pairList.source) * 4;
				int destIndex = images.indexOf(pairList.target) * 4;

				coefs.make0();
				double L = PW1[0] - PW2[0];
				if (srcIndex >= 0) {
					coefs.setItem(srcIndex + 0, 0, source.doubleX);
					coefs.setItem(srcIndex + 1, 0, -source.doubleY);
					coefs.setItem(srcIndex + 2, 0, 1.0);
					coefs.setItem(srcIndex + 3, 0, 0);
				}
				if (destIndex >= 0) {
					coefs.setItem(destIndex + 0, 0, -target.doubleX);
					coefs.setItem(destIndex + 1, 0, target.doubleY);
					coefs.setItem(destIndex + 2, 0, -1.0);
					coefs.setItem(destIndex + 3, 0, 0);
				}
//				System.out.print(L + "\t" + coefs.toString());
				lsa.addMeasurement(coefs, computedWeight, L, 0);

				L = PW1[1] - PW2[1];
				if (srcIndex >= 0) {
					coefs.setItem(srcIndex + 0, 0, source.doubleY);
					coefs.setItem(srcIndex + 1, 0, source.doubleX);
					coefs.setItem(srcIndex + 2, 0, 0);
					coefs.setItem(srcIndex + 3, 0, 1.0);
				}
				if (destIndex >= 0) {
					coefs.setItem(destIndex + 0, 0, -target.doubleY);
					coefs.setItem(destIndex + 1, 0, -target.doubleX);
					coefs.setItem(destIndex + 2, 0, 0);
					coefs.setItem(destIndex + 3, 0, -1.0);
				}
//				System.out.print(L + "\t" + coefs.toString());
				lsa.addMeasurement(coefs, computedWeight, L, 0);
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
	
	protected void computeDiscrepancies(TransformLearnerResult result) {
		double PW1[] = new double[3];
		double PW2[] = new double[3];

		result.discrepancyStatistics.start();
		for (KeyPointPairList pairList : chain) {
			pairList.transformResult.discrepancyStatistics.start();
			int goodCount = 0;
			for (KeyPointPair item : pairList.items) {
				// Compute for all points, so no item.isBad check
				transformForeward(item.sourceSP.doubleX, item.sourceSP.doubleY, pairList.source, PW1);
				transformForeward(item.targetSP.doubleX, item.targetSP.doubleY, pairList.target, PW2);
				
				double discrepancy = MathUtil.hypot(PW1[0] - PW2[0], PW1[1] - PW2[1]);
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
			calculatePrims();
		}

		lsa = new LeastSquaresAdjust(images.size() * 4, 1);
		calculateNormalEquations();
/*		// Calculate Unknowns
		Matrix m1 = lsa.getNm().makeSquareMatrix();
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
		double scaleOLD = MathUtil.hypot(origin.a, origin.b);
		double angleOLD = Math.atan2(origin.b, origin.a);
		System.out.println(origin.imageFileStamp.getFile().getName() + 
				"\tangle=" + MathUtil.rad2degStr(angleOLD) + 
				"\tscale=" + MathUtil.d4(scaleOLD) + 
				"\ttranslateX=" + MathUtil.d4(origin.hTranslateX) + 
				"\ttranslateY=" + MathUtil.d4(origin.hTranslateY)
				);
		for (int curImage = 0; curImage < images.size(); curImage++) {
			KeyPointList image = images.get(curImage);
			int index = curImage * 4;
			scaleOLD = MathUtil.hypot(image.a, image.b);
			angleOLD = Math.atan2(image.b, image.a);
			image.a -= u.getItem(0, index + 0);
			image.b -= u.getItem(0, index + 1);
			double scaleNEW = MathUtil.hypot(image.a, image.b);
			double angleNEW = Math.atan2(image.b, image.a);
			System.out.println(image.imageFileStamp.getFile().getName() + 
					"\tangle=" + MathUtil.rad2degStr(angleOLD) + 
					"\tscale=" + MathUtil.d4(scaleOLD) + 
					"\ttranslateX=" + MathUtil.d4(image.hTranslateX) + 
					"\ttranslateY=" + MathUtil.d4(image.hTranslateY) +
					"\tdangle=" + MathUtil.rad2degStr(angleOLD - angleNEW) + 
					"\tdscale=" + MathUtil.d4(scaleOLD - scaleNEW) + 
					"\tdTrX=" + MathUtil.d4(u.getItem(0, index + 2)) + 
					"\tdTrY=" + MathUtil.d4(u.getItem(0, index + 3)) 
					);
			image.hTranslateX -= u.getItem(0, index + 2);
			image.hTranslateY -= u.getItem(0, index + 3);
		}
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result;
	}
}
