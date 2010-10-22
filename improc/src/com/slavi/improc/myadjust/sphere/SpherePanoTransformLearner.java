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
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.TransformLearnerResult;

public class SpherePanoTransformLearner extends PanoTransformer {

	final static boolean adjustForScale = true;
	final static boolean adjustOriginForScale = true;
	
	LeastSquaresAdjust lsa;

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
		this.iteration = 0;
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
		return MathUtil.rad2deg * origin.fov * maxDiscrepancyInPixelsOfOriginImage / Math.max(origin.imageSizeX, origin.imageSizeY);
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
//					System.out.println("curImage=source " + curImage.imageFileStamp.getFile().getName());
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
//					System.out.println("curImage=target " + curImage.imageFileStamp.getFile().getName());
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

	private void normalizeAnglesZYZ(double angles[]) {
		angles[1] = MathUtil.fixAngleMPI_PI(angles[1]);
		if (angles[1] < 0) {
			angles[0] = MathUtil.fixAngleMPI_PI(angles[0] - Math.PI);
			angles[1] = -angles[1];
			angles[2] = angles[2] - Math.PI;
		}
		angles[1] = MathUtil.fixAngleMPI_PI(angles[1]);
		angles[2] = MathUtil.fixAngleMPI_PI(angles[2]);
	}
	
	public void calcKeyPointPairListCorrections() {
		double angles1[] = new double[3];
		double angles2[] = new double[3];
		System.out.println("*** PRIM CORRECTIONS");
		for (KeyPointPairList pairList : chain) {
			Matrix sourceToWorld = RotationZYZ.instance.makeAngles(pairList.source.sphereRZ1, pairList.source.sphereRY, pairList.source.sphereRZ2);
			RotationZYZ.instance.getRotationAnglesBackword(pairList.target.sphereRZ1, pairList.target.sphereRY, pairList.target.sphereRZ2, angles2);
			Matrix worldToTarget = RotationZYZ.instance.makeAngles(angles2[0], angles2[1], angles2[2]);
			Matrix sourceToTarget = new Matrix(3, 3);
			sourceToWorld.mMul(worldToTarget, sourceToTarget);
			RotationZYZ.instance.getRotationAngles(sourceToTarget, angles2);

			angles1[0] = pairList.sphereRZ1;
			angles1[1] = pairList.sphereRY;
			angles1[2] = pairList.sphereRZ2;
			normalizeAnglesZYZ(angles1);
			normalizeAnglesZYZ(angles2);
			
			double ds = pairList.scale - pairList.source.scaleZ / pairList.target.scaleZ;
			System.out.println(
				pairList.source.imageFileStamp.getFile().getName() +  
				"\t" + pairList.target.imageFileStamp.getFile().getName() + 
//				"\tRZ1" + MathUtil.rad2degStr(MathUtil.fixAngleMPI_PI(angles1[0])) +
//				"\tRZ1'" + MathUtil.rad2degStr(MathUtil.fixAngleMPI_PI(angles2[0])) +
//				"\tRY" + MathUtil.rad2degStr(MathUtil.fixAngleMPI_PI(angles1[1])) +
//				"\tRY'" + MathUtil.rad2degStr(MathUtil.fixAngleMPI_PI(angles2[1])) +
//				"\tRZ2" + MathUtil.rad2degStr(MathUtil.fixAngleMPI_PI(angles1[2])) +
//				"\tRZ2'" + MathUtil.rad2degStr(MathUtil.fixAngleMPI_PI(angles2[2])) +
				
				"\tdRZ1" + MathUtil.rad2degStr(MathUtil.fixAngleMPI_PI(MathUtil.fixAngle2PI(angles1[0]) - MathUtil.fixAngle2PI(angles2[0]))) +
				"\tdRY" + MathUtil.rad2degStr(MathUtil.fixAngleMPI_PI(MathUtil.fixAngle2PI(angles1[1]) - MathUtil.fixAngle2PI(angles2[1]))) +
				"\tdRZ2" + MathUtil.rad2degStr(MathUtil.fixAngleMPI_PI(MathUtil.fixAngle2PI(angles1[2]) - MathUtil.fixAngle2PI(angles2[2]))) +
				"\tdS" + MathUtil.d4(ds)
			);
		}
		System.out.println("------------------");
	}
	
	static final double scaleF = 1; // TODO: Is this necessary?
	
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
						coefs.setItem(srcIndex + 3, 0, sn.dDist_dSF * scaleF);
					}
				} else {
					if (adjustOriginForScale)
						coefs.setItem(0, 0, sn.dDist_dSF * scaleF);
				}
				if (destIndexOf >= 0) {
					coefs.setItem(destIndex + 0, 0, sn.dDist_dTR1);
					coefs.setItem(destIndex + 1, 0, sn.dDist_dTR2);
					coefs.setItem(destIndex + 2, 0, sn.dDist_dTR3);
					if (adjustForScale) {
						coefs.setItem(destIndex + 3, 0, sn.dDist_dTF * scaleF);
					}
				} else {
					if (adjustOriginForScale)
						coefs.setItem(0, 0, sn.dDist_dSF * scaleF);
				}
				lsa.addMeasurement(coefs, computedWeight, sn.Dist, 0);
			}
		}
	}
	
	protected double computeOneDiscrepancy(KeyPointPair item, double PW1[], double PW2[]) {
		SphereNorm.transformForeward(item.sourceSP.doubleX, item.sourceSP.doubleY, item.sourceSP.keyPointList, PW1);
		SphereNorm.transformForeward(item.targetSP.doubleX, item.targetSP.doubleY, item.targetSP.keyPointList, PW2);
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
				iteration = 0;
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
		System.out.println(
				origin.imageId + 
				"\t" + origin.imageFileStamp.getFile().getName() + 
				"\trz1=" + MathUtil.rad2degStr(origin.sphereRZ1) + 
				"\try=" + MathUtil.rad2degStr(origin.sphereRY) + 
				"\trz2=" + MathUtil.rad2degStr(origin.sphereRZ2) + 
				"\tFOV=" + MathUtil.rad2degStr(origin.fov) + 
				(adjustOriginForScale ? "\tdFOV=" + MathUtil.rad2degStr(u.getItem(0, 0) / scaleF) : "")
				);
		if (adjustOriginForScale) {
			origin.fov = (origin.fov - u.getItem(0, 0) / scaleF);
			origin.scaleZ = getFocalDistance(origin);
		}
		
		for (int curImage = 0; curImage < images.size(); curImage++) {
			KeyPointList image = images.get(curImage);
			int index = (adjustOriginForScale ? 1 : 0) + curImage * (adjustForScale ? 4 : 3);
			System.out.println(
					image.imageId +
					"\t" + image.imageFileStamp.getFile().getName() + 
					"\trz1=" + MathUtil.rad2degStr(image.sphereRZ1) + 
					"\try=" + MathUtil.rad2degStr(image.sphereRY) + 
					"\trz2=" + MathUtil.rad2degStr(image.sphereRZ2) + 
					"\tFOV=" + MathUtil.rad2degStr(image.fov) + 
					"\tdz1=" + MathUtil.rad2degStr(u.getItem(0, index + 0)) + 
					"\tdy=" + MathUtil.rad2degStr(u.getItem(0, index + 1)) + 
					"\tdz2=" + MathUtil.rad2degStr(u.getItem(0, index + 2)) + 
					(adjustForScale ? "\tdFOV=" + MathUtil.rad2degStr(u.getItem(0, index + 3) / scaleF) : "") 
					);
			image.sphereRZ1 = MathUtil.fixAngle2PI(image.sphereRZ1 - u.getItem(0, index + 0));
			image.sphereRY = MathUtil.fixAngle2PI(image.sphereRY - u.getItem(0, index + 1));
			image.sphereRZ2 = MathUtil.fixAngle2PI(image.sphereRZ2 - u.getItem(0, index + 2));
			if (adjustForScale) {
				image.fov = (image.fov - u.getItem(0, index + 3) / scaleF);
				image.scaleZ = getFocalDistance(image);
			}
		}
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		calcKeyPointPairListCorrections();
		return result;
	}
}
