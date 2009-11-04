package com.slavi.improc.myadjust;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.math.MathUtil;
import com.slavi.math.RotationXYZ;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.TransformLearnerResult;


public class MyPanoPairTransformLearner {

	ArrayList<KeyPointPairList> chain;
	ArrayList<KeyPointList> images;
	ArrayList<KeyPointPairList> ignoredPairLists;
	KeyPointList origin;
	LeastSquaresAdjust lsa;
	double discrepancyThreshold;
	protected int iteration = 0; 

	public MyPanoPairTransformLearner(ArrayList<KeyPointPairList> chain) {
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

	void calculatePrims() {
		origin.rx = 0.0;
		origin.ry = 0.0;
		origin.rz = 0.0;
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
					Matrix sourceToTarget = RotationXYZ.instance.makeAngles(-minHopPairList.rx, -minHopPairList.ry, -minHopPairList.rz);
					Matrix targetToWorld = RotationXYZ.instance.makeAngles(minHopPairList.target.rx, minHopPairList.target.ry, minHopPairList.target.rz);
					Matrix sourceToWorld = new Matrix(3, 3);
					sourceToTarget.mMul(targetToWorld, sourceToWorld);
					RotationXYZ.instance.getRotationAngles(sourceToWorld, angles);
					curImage.rx = angles[0];
					curImage.ry = angles[1];
					curImage.rz = angles[2];
					curImage.scaleZ = minHopPairList.target.scaleZ * minHopPairList.scale; 
//					System.out.println(curImage.imageFileStamp.getFile().getName() + "\t" + minHopPairList.target.imageFileStamp.getFile().getName());
				} else { // if (curImage == minHopPairList.target) {
					double angles[] = new double[3];
					RotationXYZ.instance.getRotationAnglesBackword(-minHopPairList.rx, -minHopPairList.ry, -minHopPairList.rz, angles);
					Matrix targetToSource = RotationXYZ.instance.makeAngles(angles[0], angles[1], angles[2]);
					Matrix sourceToWorld = RotationXYZ.instance.makeAngles(minHopPairList.source.rx, minHopPairList.source.ry, minHopPairList.source.rz);
					Matrix targetToWorld = new Matrix(3, 3);
					targetToSource.mMul(sourceToWorld, targetToWorld);
					RotationXYZ.instance.getRotationAngles(targetToWorld, angles);
					curImage.rx = angles[0];
					curImage.ry = angles[1];
					curImage.rz = angles[2];
					curImage.scaleZ = minHopPairList.source.scaleZ / minHopPairList.scale; 
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

		origin.rx = 0;
		origin.ry = 0;
		origin.rz = 0;
		origin.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;
		buildCamera2RealMatrix(origin);
		for (KeyPointList image : images) {
			buildCamera2RealMatrix(image);
		}
		
		Matrix P1 = new Matrix(1, 3);
		Matrix P2 = new Matrix(1, 3);
		
		Matrix dPW1dX1 = new Matrix(1, 3);
		Matrix dPW1dY1 = new Matrix(1, 3);
		Matrix dPW1dZ1 = new Matrix(1, 3);

		Matrix dPW2dX2 = new Matrix(1, 3);
		Matrix dPW2dY2 = new Matrix(1, 3);
		Matrix dPW2dZ2 = new Matrix(1, 3);

		MyPoint3D PW1 = new MyPoint3D();
		MyPoint3D PW2 = new MyPoint3D();
		
		KeyPoint source1 = new KeyPoint();
		KeyPoint dest1= new KeyPoint();
		lsa.clear();
		int pointCounter = 0;
		for (KeyPointPairList pairList : chain) {
			for (KeyPointPair item : pairList.items) {
				if (isBad(item))
					continue;
				pointCounter++;
				
				double computedWeight = getComputedWeight(item);
				KeyPoint source = item.getKey();
				KeyPoint dest = item.getValue();
				
				source1.doubleX = (source.doubleX - pairList.source.cameraOriginX) * pairList.source.cameraScale;
				source1.doubleY = (source.doubleY - pairList.source.cameraOriginY) * pairList.source.cameraScale;

				dest1.doubleX = (dest.doubleX - pairList.target.cameraOriginX) * pairList.target.cameraScale;
				dest1.doubleY = (dest.doubleY - pairList.target.cameraOriginY) * pairList.target.cameraScale;
				
				int srcIndex = images.indexOf(pairList.source) * 4;
				int destIndex = images.indexOf(pairList.target) * 4;
				
				coefs.make0();
	
				MyPanoPairTransformer.transform3D(source, pairList.source, PW1);
				MyPanoPairTransformer.transform3D(dest, pairList.target, PW2);
				
				P1.setItem(0, 0, source1.doubleX);
				P1.setItem(0, 1, source1.doubleY);
				P1.setItem(0, 2, pairList.source.scaleZ);
				
				P2.setItem(0, 0, dest1.doubleX);
				P2.setItem(0, 1, dest1.doubleY);
				P2.setItem(0, 2, pairList.target.scaleZ);
				
				source.keyPointList.dMdX.mMul(P1, dPW1dX1);
				source.keyPointList.dMdY.mMul(P1, dPW1dY1);
				source.keyPointList.dMdZ.mMul(P1, dPW1dZ1);
				
				dest.keyPointList.dMdX.mMul(P2, dPW2dX2);
				dest.keyPointList.dMdY.mMul(P2, dPW2dY2);
				dest.keyPointList.dMdZ.mMul(P2, dPW2dZ2);
	
				for (int c1 = 0; c1 < 3; c1++) {
					int c2 = (c1 + 1) % 3;
					coefs.make0();
					double L = 
						getTransformedCoord(PW1, c1) * getTransformedCoord(PW2, c2) -
						getTransformedCoord(PW1, c2) * getTransformedCoord(PW2, c1);
					/*
					 * fx: P'1(y) * P'2(z) - P'1(z) * P'2(y) = 0
					 * fy: P'1(z) * P'2(x) - P'1(x) * P'2(z) = 0
					 * fz: P'1(x) * P'2(y) - P'1(y) * P'2(x) = 0
					 * 
					 * f(curCoord): P'1(c1) * P'2(c2) - P'1(c2) * P'2(c1) = 0
					 */
					if (srcIndex >= 0) {
						setCoef(coefs, dPW1dX1, dPW1dY1, dPW1dZ1, srcIndex, c1,  getTransformedCoord(PW2, c2));
						setCoef(coefs, dPW1dX1, dPW1dY1, dPW1dZ1, srcIndex, c2, -getTransformedCoord(PW2, c1));
						coefs.setItem(srcIndex + 3, 0, (
								source.keyPointList.camera2real.getItem(2, c1) * getTransformedCoord(PW2, c2) - 
								source.keyPointList.camera2real.getItem(2, c2) * getTransformedCoord(PW2, c1)));
					}
					if (destIndex >= 0) {
						setCoef(coefs, dPW2dX2, dPW2dY2, dPW2dZ2, destIndex, c1, -getTransformedCoord(PW1, c2));
						setCoef(coefs, dPW2dX2, dPW2dY2, dPW2dZ2, destIndex, c2,  getTransformedCoord(PW1, c1));
						coefs.setItem(destIndex + 3, 0, (
								getTransformedCoord(PW1, c1) * dest.keyPointList.camera2real.getItem(2, c2) - 
								getTransformedCoord(PW1, c2) * dest.keyPointList.camera2real.getItem(2, c1)));
					}
					lsa.addMeasurement(coefs, computedWeight, L, 0);
				}
			}
		}
	}
	
	void buildCamera2RealMatrix(KeyPointList image) {
		image.camera2real = RotationXYZ.instance.makeAngles(image.rx, image.ry, image.rz);
		image.dMdX = RotationXYZ.instance.make_dF_dR1(image.rx, image.ry, image.rz);
		image.dMdY = RotationXYZ.instance.make_dF_dR2(image.rx, image.ry, image.rz);
		image.dMdZ = RotationXYZ.instance.make_dF_dR3(image.rx, image.ry, image.rz);
	}

	private void setCoef(Matrix coef, Matrix dPWdX, Matrix dPWdY, Matrix dPWdZ,
			int atIndex, int c1, double transformedCoord) {
		coef.setItem(atIndex + 0, 0, dPWdX.getItem(0, c1) * transformedCoord + coef.getItem(atIndex + 0, 0));
		coef.setItem(atIndex + 1, 0, dPWdY.getItem(0, c1) * transformedCoord + coef.getItem(atIndex + 1, 0));
		coef.setItem(atIndex + 2, 0, dPWdZ.getItem(0, c1) * transformedCoord + coef.getItem(atIndex + 2, 0));
	}
	
	private double getTransformedCoord(MyPoint3D point, int coord) {
		switch (coord) {
		case 0: return point.x;
		case 1: return point.y;
		case 2: return point.z;
		}
		throw new IllegalArgumentException();
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
		Point2D.Double PW1 = new Point2D.Double();
		Point2D.Double PW2 = new Point2D.Double();

		result.discrepancyStatistics.start();
		for (KeyPointPairList pairList : chain) {
			pairList.transformResult.discrepancyStatistics.start();
			int goodCount = 0;
			for (KeyPointPair item : pairList.items) {
				// Compute for all points, so no item.isBad check
				MyPanoPairTransformer.transform(item.sourceSP.doubleX, item.sourceSP.doubleY, pairList.source, PW1);
				MyPanoPairTransformer.transform(item.targetSP.doubleX, item.targetSP.doubleY, pairList.target, PW2);

				double discrepancy = SpherePanoTransformer.getSphericalDistance(PW1.x, PW1.y, PW2.x, PW2.y) * MathUtil.rad2deg;
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
			buildImagesList(chain, tmp_images);
			if (tmp_images.size() != images.size() + 1) {
				images.clear();
				images.addAll(tmp_images);
			} else {
				chainModified = false;
			}
		}
		computeWeights(result);
		if (images.size() <= 1)
			return result;
		if (chainModified) {
			System.out.println("************* COMPUTE PRIMS");
			origin = images.remove(0);
			calculatePrims();
		}

		lsa = new LeastSquaresAdjust(images.size() * 4, 1);
		calculateNormalEquations();
		// Calculate Unknowns
		if (!lsa.calculate()) 
			return null;
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
			image.scaleZ = (image.scaleZ - u.getItem(0, index + 3));
			image.rx = MathUtil.fixAngleMPI_PI(image.rx - u.getItem(0, index + 0));
			image.ry = MathUtil.fixAngleMPI_PI(image.ry - u.getItem(0, index + 1));
			image.rz = MathUtil.fixAngleMPI_PI(image.rz - u.getItem(0, index + 2));
			buildCamera2RealMatrix(image);
		}
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result;
	}
}
