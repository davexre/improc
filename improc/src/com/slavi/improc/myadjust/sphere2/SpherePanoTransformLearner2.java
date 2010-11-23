package com.slavi.improc.myadjust.sphere2;

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
import com.slavi.math.matrix.Matrix;
import com.slavi.math.matrix.SymmetricMatrix;
import com.slavi.math.transform.TransformLearnerResult;

public class SpherePanoTransformLearner2 extends PanoTransformer {

	final static boolean adjustForScale = true;
	
	LeastSquaresAdjust lsa;

	public void initialize(ArrayList<KeyPointPairList> chain) {
		this.chain = chain;
		this.images = new ArrayList<KeyPointList>();
		this.ignoredPairLists = new ArrayList<KeyPointPairList>();
		this.iteration = 0;
		for (KeyPointPairList pairList : chain) {
			double f = 1.0 / (2.0 * Math.tan(pairList.source.fov / 2.0) * pairList.source.cameraScale);
			double r = Math.sqrt(pairList.translateX * pairList.translateX + pairList.translateY * pairList.translateY);
			pairList.sphereRZ1 = Math.atan2(pairList.translateY, pairList.translateX);
			pairList.sphereRY = -Math.atan2(r, f);
			pairList.sphereRZ2 = pairList.angle - pairList.sphereRZ1;
		}
	}
	
	public double getDiscrepancyThreshold() {
		return MathUtil.rad2deg * Math.atan2(maxDiscrepancyInPixelsOfOriginImage * origin.cameraScale, origin.scaleZ);
	}
	
	double wRot[] = new double[] { -90 * MathUtil.deg2rad, 90 * MathUtil.deg2rad, 0 * MathUtil.deg2rad }; 
	
	public void transformForeward(double sx, double sy, KeyPointList image, double dest[]) {
		SphereNorm2.transformForeward(sx, sy, image, dest);
		SphericalCoordsLongZen.rotateForeward(dest[0], dest[1], wRot[0], wRot[1], wRot[2], dest);
		dest[0] = -dest[0];
	}
	
	public void transformBackward(double rx, double ry, KeyPointList image, double dest[]) {
		SphericalCoordsLongZen.rotateBackward(-rx, ry, wRot[0], wRot[1], wRot[2], dest);
		SphereNorm2.transformBackward(dest[0], dest[1], image, dest);
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
		origin.sphereRZ1 = 0;
		origin.sphereRY = 0;
		origin.sphereRZ2 = 0;
		origin.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;
		
		lsa.clear();
		Matrix coefs = new Matrix(images.size() * (adjustForScale ? 4 : 3), 1);			
		SphereNorm2 sn = new SphereNorm2();
		for (KeyPointPairList pairList : chain) {
			for (KeyPointPair item : pairList.items) {
				if (isBad(item))
					continue;
				
				sn.setKeyPointPair(item);
				double computedWeight = getWeight(item);
				int srcIndex = images.indexOf(pairList.source) * (adjustForScale ? 4 : 3);
				int destIndex = images.indexOf(pairList.target) * (adjustForScale ? 4 : 3);
				
				coefs.make0();
				if (srcIndex >= 0) {
					coefs.setItem(srcIndex + 0, 0, sn.dDist_dSR1);
					coefs.setItem(srcIndex + 1, 0, sn.dDist_dSR2);
					coefs.setItem(srcIndex + 2, 0, sn.dDist_dSR3);
					if (adjustForScale) {
						coefs.setItem(srcIndex + 3, 0, sn.dDist_dSF);
					}
				}
				if (destIndex >= 0) {
					coefs.setItem(destIndex + 0, 0, sn.dDist_dTR1);
					coefs.setItem(destIndex + 1, 0, sn.dDist_dTR2);
					coefs.setItem(destIndex + 2, 0, sn.dDist_dTR3);
					if (adjustForScale) {
						coefs.setItem(destIndex + 3, 0, sn.dDist_dTF);
					}
				}
				lsa.addMeasurement(coefs, computedWeight, sn.Dist, 0);
			}
		}
	}

	protected double computeOneDiscrepancy(KeyPointPair item, double PW1[], double PW2[]) {
		SphereNorm2.transformForeward(item.sourceSP.doubleX, item.sourceSP.doubleY, item.sourceSP.keyPointList, PW1);
		SphereNorm2.transformForeward(item.targetSP.doubleX, item.targetSP.doubleY, item.targetSP.keyPointList, PW2);
		return SphericalCoordsLongZen.getSphericalDistance(PW1[0], PW1[1], PW2[0], PW2[1]) * MathUtil.rad2deg;
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
		startNewIteration(result);
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
		SymmetricMatrix nm = lsa.getNm().makeCopy();
		SymmetricMatrix tmp = nm.makeCopy();
		if (!nm.inverse())
			throw new RuntimeException();
		nm.mMul(lsa.getNm(), tmp);
		System.out.println("**** DEVIATION FROM E is: " + Math.sqrt(tmp.getSquaredDeviationFromE()));
		
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
		System.out.println(
				origin.imageId +
				"\t" + origin.imageFileStamp.getFile().getName() + 
				"\trz1=" + MathUtil.rad2degStr(origin.sphereRZ1) + 
				"\try=" + MathUtil.rad2degStr(origin.sphereRY) + 
				"\trz2=" + MathUtil.rad2degStr(origin.sphereRZ2) + 
				"\ts=" + MathUtil.d4(origin.scaleZ)
				);
		for (int curImage = 0; curImage < images.size(); curImage++) {
			KeyPointList image = images.get(curImage);
			int index = curImage * (adjustForScale ? 4 : 3);
			System.out.println(
					image.imageId +
					"\t" + image.imageFileStamp.getFile().getName() + 
					"\trz1=" + MathUtil.rad2degStr(image.sphereRZ1) + 
					"\try=" + MathUtil.rad2degStr(image.sphereRY) + 
					"\trz2=" + MathUtil.rad2degStr(image.sphereRZ2) + 
					"\ts=" + MathUtil.d4(image.scaleZ) +
					"\tdz1=" + MathUtil.rad2degStr(u.getItem(0, index + 0)) + 
					"\tdy=" + MathUtil.rad2degStr(u.getItem(0, index + 1)) + 
					"\tdz2=" + MathUtil.rad2degStr(u.getItem(0, index + 2)) + 
					(adjustForScale ? "\tds=" + MathUtil.d4(u.getItem(0, index + 3)) : "") 
					);
			image.sphereRZ1 = MathUtil.fixAngle2PI(image.sphereRZ1 - u.getItem(0, index + 0));
			image.sphereRY = MathUtil.fixAngle2PI(image.sphereRY - u.getItem(0, index + 1));
			image.sphereRZ2 = MathUtil.fixAngle2PI(image.sphereRZ2 - u.getItem(0, index + 2));
			if (adjustForScale) {
				image.scaleZ = (image.scaleZ - u.getItem(0, index + 3));
			}
		}
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result;
	}
}
