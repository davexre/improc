package com.slavi.improc.myadjust.xyz;

import java.util.ArrayList;

import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.myadjust.CalculatePanoramaParams;
import com.slavi.improc.myadjust.PanoTransformer;
import com.slavi.math.MathUtil;
import com.slavi.math.SphericalCoordsLongLat;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.TransformLearnerResult;

public class MyPanoPairTransformLearner extends PanoTransformer {

	static boolean adjustForScale = false;
	static boolean adjustOriginForScale = false;

	LeastSquaresAdjust lsa;

	public void initialize(ArrayList<KeyPointPairList> chain) {
		this.chain = chain;
		this.images = new ArrayList<KeyPointList>();
		this.ignoredPairLists = new ArrayList<KeyPointPairList>();
		this.iteration = 0;
		for (KeyPointPairList pairList : chain) {
			double f = pairList.scale * KeyPointList.defaultCameraFOV_to_ScaleZ;
			double c = pairList.translateX * pairList.source.cameraScale;
			double d = pairList.translateY * pairList.source.cameraScale;
			double f1f1 = f * f + pairList.translateY * pairList.translateY;
			double f1 = Math.sqrt(f1f1);
			double f2 = Math.sqrt(f1f1 + c * c);

			pairList.rx = Math.atan2(d, f);
			pairList.ry = Math.atan2(c, f1);
			pairList.rz = Math.atan2(Math.tan(pairList.angle) * f1f1, f * f2);
		}
	}
	
	public double getDiscrepancyThreshold() {
		return MathUtil.rad2deg * Math.atan2(maxDiscrepancyInPixelsOfOriginImage * origin.cameraScale, origin.scaleZ);
	}

	/**
	 * Transforms from source image coordinate system into world coord.system.
	 * @param sx, sy	Coordinates in pixels of the source image with origin pixel(0,0)
	 * @param dest		The transformed coordinates in radians. Longitude is 
	 * 					returned in dest.x and is in the range (-pi; pi] and Latitude
	 * 					is returned in dest.y in the range [-pi/2; pi/2].    
	 */
	public void transformForeward(double sx, double sy, KeyPointList srcImage, double dest[]) {
		MyPanoPairTransformNorm.transformForeward(sx, sy, srcImage, dest);
		double d = Math.sqrt(dest[0]*dest[0] + dest[2]*dest[2]);
		dest[0] = Math.atan2(dest[0], dest[2]);
		dest[1] = Math.atan2(dest[1], d);
//		SphericalCoordsLongLat.cartesianToPolar(dest[0], dest[1], dest[2], dest);
	}

	public void transformBackward(double rx, double ry, KeyPointList srcImage, double dest[]) {
		double d = Math.cos(ry);
		double sx = d * Math.sin(rx);
		double sy = Math.sin(ry);
		double sz = d * Math.cos(rx);
		MyPanoPairTransformNorm.transformBackward(sx, sy, sz, srcImage, dest);
//		SphericalCoordsLongLat.polarToCartesian(rx, ry, 1.0, dest);
//		MyPanoPairTransformNorm.transformBackward(dest[0], dest[1], dest[2], srcImage, dest);
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
					Matrix sourceToTarget = MyPanoPairTransformNorm.rot.makeAngles(-minHopPairList.rx, -minHopPairList.ry, -minHopPairList.rz);
					Matrix targetToWorld = MyPanoPairTransformNorm.rot.makeAngles(minHopPairList.target.rx, minHopPairList.target.ry, minHopPairList.target.rz);
					Matrix sourceToWorld = new Matrix(3, 3);
					sourceToTarget.mMul(targetToWorld, sourceToWorld);
					MyPanoPairTransformNorm.rot.getRotationAngles(sourceToWorld, angles);
					curImage.rx = angles[0];
					curImage.ry = angles[1];
					curImage.rz = angles[2];
					curImage.scaleZ = minHopPairList.target.scaleZ * minHopPairList.scale; 
//					System.out.println(curImage.imageFileStamp.getFile().getName() + "\t" + minHopPairList.target.imageFileStamp.getFile().getName());
				} else { // if (curImage == minHopPairList.target) {
					double angles[] = new double[3];
					MyPanoPairTransformNorm.rot.getRotationAnglesBackword(-minHopPairList.rx, -minHopPairList.ry, -minHopPairList.rz, angles);
					Matrix targetToSource = MyPanoPairTransformNorm.rot.makeAngles(angles[0], angles[1], angles[2]);
					Matrix sourceToWorld = MyPanoPairTransformNorm.rot.makeAngles(minHopPairList.source.rx, minHopPairList.source.ry, minHopPairList.source.rz);
					Matrix targetToWorld = new Matrix(3, 3);
					targetToSource.mMul(sourceToWorld, targetToWorld);
					MyPanoPairTransformNorm.rot.getRotationAngles(targetToWorld, angles);
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
		Matrix coefs = new Matrix((adjustOriginForScale ? 1 : 0) + images.size() * (adjustForScale ? 4 : 3), 1);			

		origin.rx = 0;
		origin.ry = 0;
		origin.rz = 0;
		origin.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;
		buildCamera2RealMatrix(origin);
		for (KeyPointList image : images) {
			buildCamera2RealMatrix(image);
		}
		
		lsa.clear();
		MyPanoPairTransformNorm norm = new MyPanoPairTransformNorm();
//		System.out.println("NORMAL EQUASIONS");
		int pointCounter = 0;
		for (KeyPointPairList pairList : chain) {
			int srcIndex = (adjustOriginForScale ? 1 : 0) + images.indexOf(pairList.source) * (adjustForScale ? 4 : 3);
			int destIndex = (adjustOriginForScale ? 1 : 0) + images.indexOf(pairList.target) * (adjustForScale ? 4 : 3);
			for (KeyPointPair item : pairList.items) {
				if (isBad(item))
					continue;
				pointCounter++;
				
				double computedWeight = getWeight(item);
				norm.setKeyPointPair(item);

				for (int c1 = 0; c1 < 3; c1++) {
					int c2 = (c1 + 1) % 3;
					coefs.make0();
					double L = norm.p1.P[c1] * norm.p2.P[c2] - norm.p1.P[c2] * norm.p2.P[c1];
					/*
					 * fx: P'1(y) * P'2(z) - P'1(z) * P'2(y) = 0
					 * fy: P'1(z) * P'2(x) - P'1(x) * P'2(z) = 0
					 * fz: P'1(x) * P'2(y) - P'1(y) * P'2(x) = 0
					 * 
					 * f(curCoord): P'1(c1) * P'2(c2) - P'1(c2) * P'2(c1) = 0
					 */
					if (srcIndex >= 0) {
						coefs.setItem(srcIndex + 0, 0, norm.p1.dPdZ1[c1] * norm.p2.P[c2] - norm.p1.dPdZ1[c2] * norm.p2.P[c1]);
						coefs.setItem(srcIndex + 1, 0, norm.p1.dPdY [c1] * norm.p2.P[c2] - norm.p1.dPdY [c2] * norm.p2.P[c1]);
						coefs.setItem(srcIndex + 2, 0, norm.p1.dPdZ2[c1] * norm.p2.P[c2] - norm.p1.dPdZ2[c2] * norm.p2.P[c1]);
						if (adjustForScale) {
							coefs.setItem(srcIndex + 3, 0, norm.p1.dPdS[c1] * norm.p2.P[c2] - norm.p1.dPdS[c2] * norm.p2.P[c1]);
						}
					} else {
						if (adjustOriginForScale) {
							coefs.setItem(0, 0, norm.p1.dPdS[c1] * norm.p2.P[c2] - norm.p1.dPdS[c2] * norm.p2.P[c1]);
						}
					}
					if (destIndex >= 0) {
						coefs.setItem(destIndex + 0, 0, norm.p1.P[c1] * norm.p2.dPdZ1[c2] - norm.p1.P[c2] * norm.p2.dPdZ1[c1]);
						coefs.setItem(destIndex + 1, 0, norm.p1.P[c1] * norm.p2.dPdY [c2] - norm.p1.P[c2] * norm.p2.dPdY [c1]);
						coefs.setItem(destIndex + 2, 0, norm.p1.P[c1] * norm.p2.dPdZ2[c2] - norm.p1.P[c2] * norm.p2.dPdZ2[c1]);
						if (adjustForScale) {
							coefs.setItem(destIndex + 3, 0, norm.p1.P[c1] * norm.p2.dPdS[c2] - norm.p1.P[c2] * norm.p2.dPdS[c1]);
						}
					} else {
						if (adjustOriginForScale) {
							coefs.setItem(0, 0, norm.p1.P[c1] * norm.p2.dPdS[c2] - norm.p1.P[c2] * norm.p2.dPdS[c1]);
						}
					}
					lsa.addMeasurement(coefs, computedWeight, L, 0);
				}
			}
		}
	}
	
	void buildCamera2RealMatrix(KeyPointList image) {
		image.camera2real = MyPanoPairTransformNorm.rot.makeAngles(image.rx, image.ry, image.rz);
		image.dMdX = MyPanoPairTransformNorm.rot.make_dF_dR1(image.rx, image.ry, image.rz);
		image.dMdY = MyPanoPairTransformNorm.rot.make_dF_dR2(image.rx, image.ry, image.rz);
		image.dMdZ = MyPanoPairTransformNorm.rot.make_dF_dR3(image.rx, image.ry, image.rz);
	}

	protected double computeOneDiscrepancy(KeyPointPair item, double PW1[], double PW2[]) {
		transformForeward(item.sourceSP.getDoubleX(), item.sourceSP.getDoubleY(), item.sourceSP.getKeyPointList(), PW1);
		transformForeward(item.targetSP.getDoubleX(), item.targetSP.getDoubleY(), item.targetSP.getKeyPointList(), PW2);
		return SphericalCoordsLongLat.getSphericalDistance(PW1[0], PW1[1], PW2[0], PW2[1]) * MathUtil.rad2deg;
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
			calculatePrims();
		}

		lsa = new LeastSquaresAdjust((adjustOriginForScale ? 1 : 0) + images.size() * (adjustForScale ? 4 : 3), 1);
		calculateNormalEquations();
		// Calculate Unknowns
		if (!lsa.calculateWithDebug(false)) 
			return result;
		// Build transformer
		Matrix u = lsa.getUnknown();
		System.out.println(origin.imageFileStamp.getFile().getName() + 
				"\trx=" + MathUtil.rad2degStr(origin.rx) + 
				"\try=" + MathUtil.rad2degStr(origin.ry) + 
				"\trz=" + MathUtil.rad2degStr(origin.rz) + 
				"\ts=" + MathUtil.d4(origin.scaleZ) +
				(adjustOriginForScale ? "\tds=" + MathUtil.d4(u.getItem(0, 0)) : "")
				);
		if (adjustOriginForScale) {
			origin.scaleZ = (origin.scaleZ - u.getItem(0, 0));
		}
		for (int curImage = 0; curImage < images.size(); curImage++) {
			KeyPointList image = images.get(curImage);
			int index = (adjustOriginForScale ? 1 : 0) + curImage * (adjustForScale ? 4 : 3);
			System.out.println(image.imageFileStamp.getFile().getName() + 
					"\trx=" + MathUtil.rad2degStr(image.rx) + 
					"\try=" + MathUtil.rad2degStr(image.ry) + 
					"\trz=" + MathUtil.rad2degStr(image.rz) + 
					"\ts=" + MathUtil.d4(image.scaleZ) +
					"\tdx=" + MathUtil.rad2degStr(u.getItem(0, index + 0)) + 
					"\tdy=" + MathUtil.rad2degStr(u.getItem(0, index + 1)) + 
					"\tdz=" + MathUtil.rad2degStr(u.getItem(0, index + 2)) + 
					(adjustForScale ? "\tds=" + MathUtil.d4(u.getItem(0, index + 3)) : "") 
					);
			image.rx = MathUtil.fixAngleMPI_PI(image.rx - u.getItem(0, index + 0));
			image.ry = MathUtil.fixAngleMPI_PI(image.ry - u.getItem(0, index + 1));
			image.rz = MathUtil.fixAngleMPI_PI(image.rz - u.getItem(0, index + 2));
			if (adjustForScale) {
				image.scaleZ = (image.scaleZ - u.getItem(0, index + 3));
			}
			buildCamera2RealMatrix(image);
		}
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result;
	}
}
