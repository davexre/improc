package com.slavi.improc.myadjust.zyz7params;

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
import com.slavi.math.transform.TransformLearnerResult;


public class ZYZ_7ParamsLearner extends PanoTransformer {

	public static final RotationZYZ rot = RotationZYZ.instance;
	static boolean adjustForScale = false;
	static boolean adjustOriginForScale = false;
	
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
	
	/*
	 * x -> fi (longitude)
	 * y -> psi (latitude) 
	 */
	double wRot[] = new double[] { -90 * MathUtil.deg2rad, 90 * MathUtil.deg2rad, 0 * MathUtil.deg2rad }; 
	
	/**
	 * Transforms from source image coordinate system into world coord.system.
	 * @param sx, sy	Coordinates in pixels of the source image with origin pixel(0,0)
	 * @param dest		The transformed coordinates in radians. Longitude is 
	 * 					returned in dest[0] and is in the range (-pi; pi] and Latitude
	 * 					is returned in dest[1] in the range [-pi/2; pi/2]. dest[2] should be 1.0    
	 */
	public void transformForeward(double sx, double sy, KeyPointList srcImage, double dest[]) {
		ZYZ_7ParamsNorm.transformForeward(sx, sy, srcImage, dest);
		SphericalCoordsLongZen.cartesianToPolar(dest[0], dest[1], dest[2], dest);
		SphericalCoordsLongZen.rotateForeward(dest[0], dest[1], wRot[0], wRot[1], wRot[2], dest);
		dest[0] = -dest[0];
	}

	public void transformBackward(double rx, double ry, KeyPointList srcImage, double dest[]) {
		SphericalCoordsLongZen.rotateBackward(-rx, ry, wRot[0], wRot[1], wRot[2], dest);
		SphericalCoordsLongZen.polarToCartesian(dest[0], dest[1], 1.0, dest);
		ZYZ_7ParamsNorm.transformBackward(dest[0], dest[1], dest[2], srcImage, dest);
	}
	
	public static void calculatePrims(KeyPointList origin, ArrayList<KeyPointList> images, ArrayList<KeyPointPairList> chain) {
		origin.sphereRZ1 = 0.0;
		origin.sphereRY = 0.0;
		origin.sphereRZ2 = 0.0;
		origin.tx = 0.0;
		origin.ty = 0.0;
		origin.tz = 0.0;
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
				curImage.tx = 0.0;
				curImage.ty = 0.0;
				curImage.tz = 0.0;
				if (curImage == minHopPairList.source) {
					double angles[] = new double[3];
					Matrix sourceToTarget = rot.makeAngles(minHopPairList.sphereRZ1, minHopPairList.sphereRY, minHopPairList.sphereRZ2);
					Matrix targetToWorld = rot.makeAngles(minHopPairList.target.sphereRZ1, minHopPairList.target.sphereRY, minHopPairList.target.sphereRZ2);
					Matrix sourceToWorld = new Matrix(3, 3);
					sourceToTarget.mMul(targetToWorld, sourceToWorld);
					rot.getRotationAngles(sourceToWorld, angles);
					curImage.sphereRZ1 = angles[0];
					curImage.sphereRY = angles[1];
					curImage.sphereRZ2 = angles[2];
					if (adjustForScale) {
						curImage.scaleZ = minHopPairList.target.scaleZ * minHopPairList.scale;
					}
				} else { // if (curImage == minHopPairList.target) {
					double angles[] = new double[3];
					rot.getRotationAnglesBackword(minHopPairList.sphereRZ1, minHopPairList.sphereRY, minHopPairList.sphereRZ2, angles);
					Matrix targetToSource = rot.makeAngles(angles[0], angles[1], angles[2]);
					Matrix sourceToWorld = rot.makeAngles(minHopPairList.source.sphereRZ1, minHopPairList.source.sphereRY, minHopPairList.source.sphereRZ2);
					Matrix targetToWorld = new Matrix(3, 3);
					targetToSource.mMul(sourceToWorld, targetToWorld);
					rot.getRotationAngles(targetToWorld, angles);
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

/*		origin.sphereRZ1 = 0;
		origin.sphereRY = 0;
		origin.sphereRZ2 = 0;
		origin.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;
		buildCamera2RealMatrix(origin);
		for (KeyPointList image : images) {
			buildCamera2RealMatrix(image);
		}
		double PW1[] = new double[3];
		double PW2[] = new double[3];
		for (KeyPointPairList pairList : chain) {
			for (KeyPointPair item : pairList.items) {
				if (item.panoBad)
					continue;
				
				MyPanoPairTransformerZYZ.transformForeward(item.sourceSP.doubleX, item.sourceSP.doubleY, pairList.source, PW1);
				MyPanoPairTransformerZYZ.transformForeward(item.targetSP.doubleX, item.targetSP.doubleY, pairList.target, PW2);
				double discrepancy = SpherePanoTransformer.getSphericalDistance(PW1[0], PW1[1], PW2[0], PW2[1]);
				System.out.println("Dist=" + MathUtil.rad2degStr(discrepancy));
			}
		}*/
	}

	void calculateNormalEquations() {
		Matrix coefs = new Matrix((adjustOriginForScale ? 1 : 0) + images.size() * (adjustForScale ? 7 : 6), 1);			

		origin.sphereRZ1 = 0.0;
		origin.sphereRY = 0.0;
		origin.sphereRZ2 = 0.0;
		origin.tx = 0.0;
		origin.ty = 0.0;
		origin.tz = 0.0;
		buildCamera2RealMatrix(origin);
		for (KeyPointList image : images) {
			buildCamera2RealMatrix(image);
		}
		
		lsa.clear();
		ZYZ_7ParamsNorm norm = new ZYZ_7ParamsNorm();
//		System.out.println("NORMAL EQUASIONS");
		int pointCounter = 0;
		for (KeyPointPairList pairList : chain) {
			int srcIndex = (adjustOriginForScale ? 1 : 0) + images.indexOf(pairList.source) * (adjustForScale ? 7 : 6);
			int destIndex = (adjustOriginForScale ? 1 : 0) + images.indexOf(pairList.target) * (adjustForScale ? 7 : 6);
			for (KeyPointPair item : pairList.items) {
				if (isBad(item))
					continue;
				pointCounter++;
				
				double computedWeight = getComputedWeight(item);
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
						coefs.setItem(srcIndex + 0, 0, norm.p1.dPdZ1.getItem(0, c1) * norm.p2.P[c2] - norm.p1.dPdZ1.getItem(0, c2) * norm.p2.P[c1]);
						coefs.setItem(srcIndex + 1, 0, norm.p1.dPdY .getItem(0, c1) * norm.p2.P[c2] - norm.p1.dPdY .getItem(0, c2) * norm.p2.P[c1]);
						coefs.setItem(srcIndex + 2, 0, norm.p1.dPdZ2.getItem(0, c1) * norm.p2.P[c2] - norm.p1.dPdZ2.getItem(0, c2) * norm.p2.P[c1]);
						coefs.setItem(srcIndex + 3, 0, norm.p1.dPdTX.getItem(0, c1) * norm.p2.P[c2] - norm.p1.dPdTX.getItem(0, c2) * norm.p2.P[c1]);
						coefs.setItem(srcIndex + 4, 0, norm.p1.dPdTY.getItem(0, c1) * norm.p2.P[c2] - norm.p1.dPdTY.getItem(0, c2) * norm.p2.P[c1]);
						coefs.setItem(srcIndex + 5, 0, norm.p1.dPdTZ.getItem(0, c1) * norm.p2.P[c2] - norm.p1.dPdTZ.getItem(0, c2) * norm.p2.P[c1]);
						if (adjustForScale) {
							coefs.setItem(srcIndex + 6, 0, norm.p1.dPdS .getItem(0, c1) * norm.p2.P[c2] - norm.p1.dPdS .getItem(0, c2) * norm.p2.P[c1]);
						}
					} else {
						if (adjustOriginForScale) {
							coefs.setItem(0, 0, norm.p1.dPdS .getItem(0, c1) * norm.p2.P[c2] - norm.p1.dPdS .getItem(0, c2) * norm.p2.P[c1]);
						}
					}
					if (destIndex >= 0) {
						coefs.setItem(destIndex + 0, 0, norm.p1.P[c1] * norm.p2.dPdZ1.getItem(0, c2) - norm.p1.P[c2] * norm.p2.dPdZ1.getItem(0, c1));
						coefs.setItem(destIndex + 1, 0, norm.p1.P[c1] * norm.p2.dPdY .getItem(0, c2) - norm.p1.P[c2] * norm.p2.dPdY .getItem(0, c1));
						coefs.setItem(destIndex + 2, 0, norm.p1.P[c1] * norm.p2.dPdZ2.getItem(0, c2) - norm.p1.P[c2] * norm.p2.dPdZ2.getItem(0, c1));
						coefs.setItem(destIndex + 3, 0, norm.p1.P[c1] * norm.p2.dPdTX.getItem(0, c2) - norm.p1.P[c2] * norm.p2.dPdTX.getItem(0, c1));
						coefs.setItem(destIndex + 4, 0, norm.p1.P[c1] * norm.p2.dPdTY.getItem(0, c2) - norm.p1.P[c2] * norm.p2.dPdTY.getItem(0, c1));
						coefs.setItem(destIndex + 5, 0, norm.p1.P[c1] * norm.p2.dPdTZ.getItem(0, c2) - norm.p1.P[c2] * norm.p2.dPdTZ.getItem(0, c1));
						if (adjustForScale) {
							coefs.setItem(destIndex + 6, 0, norm.p1.P[c1] * norm.p2.dPdS .getItem(c2, 0) - norm.p1.P[c2] * norm.p2.dPdS .getItem(c1, 0));
						}
					} else {
						if (adjustOriginForScale) {
							coefs.setItem(0, 0, norm.p1.P[c1] * norm.p2.dPdS .getItem(0, c2) - norm.p1.P[c2] * norm.p2.dPdS .getItem(0, c1));
						}
					}
					lsa.addMeasurement(coefs, computedWeight, L, 0);
//					System.out.print(MathUtil.d4(L) + "\t" + coefs.toString());
				}
			}
		}
	}
	
	public static void buildCamera2RealMatrix(KeyPointList image) {
		image.camera2real = rot.makeAngles(image.sphereRZ1, image.sphereRY, image.sphereRZ2);
		image.dMdX = rot.make_dF_dR1(image.sphereRZ1, image.sphereRY, image.sphereRZ2);
		image.dMdY = rot.make_dF_dR2(image.sphereRZ1, image.sphereRY, image.sphereRZ2);
		image.dMdZ = rot.make_dF_dR3(image.sphereRZ1, image.sphereRY, image.sphereRZ2);
		rot.transformBackward(image.camera2real, -image.tx, -image.ty, -image.tz, image.worldOrigin);		
	}

	protected double computeOneDiscrepancy(KeyPointPair item, double PW1[], double PW2[]) {
		ZYZ_7ParamsNorm.transformForeward(item.sourceSP.doubleX, item.sourceSP.doubleY, item.sourceSP.keyPointList, PW1);
		ZYZ_7ParamsNorm.transformForeward(item.targetSP.doubleX, item.targetSP.doubleY, item.targetSP.keyPointList, PW2);
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
		computeWeights(result);
		if (chainModified) {
			if (images.size() <= 1)
				return result;
			System.out.println("************* COMPUTE PRIMS");
			origin = images.remove(0);
			calculatePrims(origin, images, chain);
		}

		lsa = new LeastSquaresAdjust((adjustOriginForScale ? 1 : 0) + images.size() * (adjustForScale ? 7 : 6), 1);
		calculateNormalEquations();
		// Calculate Unknowns
		if (!lsa.calculateWithDebug()) 
			return result;
		// Build transformer
		Matrix u = lsa.getUnknown();
//		u.printM("U");
		System.out.println(origin.imageFileStamp.getFile().getName() + 
				"\trz1=" + MathUtil.rad2degStr(origin.sphereRZ1) + 
				"\try=" + MathUtil.rad2degStr(origin.sphereRY) + 
				"\trz2=" + MathUtil.rad2degStr(origin.sphereRZ2) + 
				"\ts=" + MathUtil.d4(origin.scaleZ) +
				(adjustOriginForScale ? "\tds=" + MathUtil.d4(u.getItem(0, 0)) : "")
				);
		if (adjustOriginForScale) {
			origin.scaleZ = (origin.scaleZ - u.getItem(0, 0));
		}
		for (int curImage = 0; curImage < images.size(); curImage++) {
			KeyPointList image = images.get(curImage);
			int index = (adjustOriginForScale ? 1 : 0) + curImage * (adjustForScale ? 7 : 6);
			System.out.println(image.imageFileStamp.getFile().getName() + 
					"\trz1=" + MathUtil.rad2degStr(image.sphereRZ1) + 
					"\try=" + MathUtil.rad2degStr(image.sphereRY) + 
					"\trz2=" + MathUtil.rad2degStr(image.sphereRZ2) + 
					"\ttx=" + MathUtil.d4(image.tx) +
					"\tty=" + MathUtil.d4(image.ty) +
					"\ttz=" + MathUtil.d4(image.tz) +
					"\ts=" + MathUtil.d4(image.scaleZ) +
					"\tdz1=" + MathUtil.rad2degStr(u.getItem(0, index + 0)) + 
					"\tdy=" + MathUtil.rad2degStr(u.getItem(0, index + 1)) + 
					"\tdz2=" + MathUtil.rad2degStr(u.getItem(0, index + 2)) + 
					"\tdtx=" + MathUtil.d4(u.getItem(0, index + 3)) + 
					"\tdty=" + MathUtil.d4(u.getItem(0, index + 4)) + 
					"\tdtz=" + MathUtil.d4(u.getItem(0, index + 5)) + 
					(adjustForScale ? "\tds=" + MathUtil.d4(u.getItem(0, index + 6)) : "") 
					);
			image.sphereRZ1 = MathUtil.fixAngleMPI_PI(image.sphereRZ1 - u.getItem(0, index + 0));
			image.sphereRY = MathUtil.fixAngleMPI_PI(image.sphereRY - u.getItem(0, index + 1));
			image.sphereRZ2 = MathUtil.fixAngleMPI_PI(image.sphereRZ2 - u.getItem(0, index + 2));
			image.tx = (image.tx - u.getItem(0, index + 3));
			image.ty = (image.ty - u.getItem(0, index + 4));
			image.tz = (image.tz - u.getItem(0, index + 5));
			if (adjustForScale) {
				image.scaleZ = (image.scaleZ - u.getItem(0, index + 6));
			}
			buildCamera2RealMatrix(image);
		}
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result;
	}
}
