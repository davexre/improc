package com.slavi.improc.myadjust.zyz7params;

import java.util.ArrayList;

import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.myadjust.CalculatePanoramaParams;
import com.slavi.improc.myadjust.PanoTransformer;
import com.slavi.math.MathUtil;
import com.slavi.math.SphericalCoordsLongZen;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.TransformLearnerResult;


public class Stereo_7ParamsLearner extends PanoTransformer {

	static boolean adjustOriginForScale = false;
	static boolean adjustForScale = true;
	
	LeastSquaresAdjust lsa;
	Stereo_7ParamsNorm norm = new Stereo_7ParamsNorm(); 

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
		Stereo_7ParamsNorm.transformForeward(sx, sy, srcImage, dest);
		// TODO: more
		SphericalCoordsLongZen.cartesianToPolar(dest[0], dest[1], dest[2], dest);
		SphericalCoordsLongZen.rotateForeward(dest[0], dest[1], wRot[0], wRot[1], wRot[2], dest);
		dest[0] = -dest[0];
	}

	public void transformBackward(double rx, double ry, KeyPointList srcImage, double dest[]) {
		SphericalCoordsLongZen.rotateBackward(-rx, ry, wRot[0], wRot[1], wRot[2], dest);
		SphericalCoordsLongZen.polarToCartesian(dest[0], dest[1], 1.0, dest);
		Stereo_7ParamsNorm.transformBackward(dest[0], dest[1], dest[2], srcImage, dest);
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
				curImage.tx = 1.0;
				curImage.ty = 0.0;
				curImage.tz = 0.0;
				if (curImage == minHopPairList.source) {
					double angles[] = new double[3];
					Matrix sourceToTarget = Stereo_7ParamsNorm.rot.makeAngles(minHopPairList.sphereRZ1, minHopPairList.sphereRY, minHopPairList.sphereRZ2);
					Matrix targetToWorld = Stereo_7ParamsNorm.rot.makeAngles(minHopPairList.target.sphereRZ1, minHopPairList.target.sphereRY, minHopPairList.target.sphereRZ2);
					Matrix sourceToWorld = new Matrix(3, 3);
					sourceToTarget.mMul(targetToWorld, sourceToWorld);
					Stereo_7ParamsNorm.rot.getRotationAngles(sourceToWorld, angles);
					curImage.sphereRZ1 = angles[0];
					curImage.sphereRY = angles[1];
					curImage.sphereRZ2 = angles[2];
					if (adjustForScale) {
						curImage.scaleZ = minHopPairList.target.scaleZ * minHopPairList.scale;
					}
				} else { // if (curImage == minHopPairList.target) {
					double angles[] = new double[3];
					Stereo_7ParamsNorm.rot.getRotationAnglesBackword(minHopPairList.sphereRZ1, minHopPairList.sphereRY, minHopPairList.sphereRZ2, angles);
					Matrix targetToSource = Stereo_7ParamsNorm.rot.makeAngles(angles[0], angles[1], angles[2]);
					Matrix sourceToWorld = Stereo_7ParamsNorm.rot.makeAngles(minHopPairList.source.sphereRZ1, minHopPairList.source.sphereRY, minHopPairList.source.sphereRZ2);
					Matrix targetToWorld = new Matrix(3, 3);
					targetToSource.mMul(sourceToWorld, targetToWorld);
					Stereo_7ParamsNorm.rot.getRotationAngles(targetToWorld, angles);
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

	int getParamsPerImage() {
		int result = 3;				// 3 rotations
		result += 3;			// 3 translations
		if (adjustForScale)
			result++;				// 1 focal distance
		return result;
	}
	
	int getNumberOfParams() {
		int result = getParamsPerImage();
		result *= images.size();	// every images has these
		if (adjustOriginForScale)
			result++;				// 1 focal distance of the origin image
		return result;
	}
	
	void calculateNormalEquations() {
//		Matrix coefs = new Matrix((adjustOriginForScale ? 1 : 0) + images.size() * (adjustForScale ? 7 : 6), 1);			
		Matrix coefs = new Matrix(getNumberOfParams(), 1);			

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
		
		int paramsPerImage = getParamsPerImage();
		lsa.clear();

//		System.out.println("NORMAL EQUASIONS");
		for (KeyPointPairList pairList : chain) {
			int srcIndex = (adjustOriginForScale ? 1 : 0) + images.indexOf(pairList.source) * paramsPerImage;
			int destIndex = (adjustOriginForScale ? 1 : 0) + images.indexOf(pairList.target) * paramsPerImage;
			for (KeyPointPair item : pairList.items) {
				if (isBad(item))
					continue;
				
				double computedWeight = getWeight(item);
				norm.setKeyPointPair(item);
				coefs.make0();
				
				double L = norm.F;
				if (srcIndex >= 0) {
					int curParam = srcIndex;
					coefs.setItem(curParam++, 0, norm.dF_dSZ1);
					coefs.setItem(curParam++, 0, norm.dF_dSY);
					coefs.setItem(curParam++, 0, norm.dF_dSZ2);
					coefs.setItem(curParam++, 0, norm.dF_dSTX);
					coefs.setItem(curParam++, 0, norm.dF_dSTY);
					coefs.setItem(curParam++, 0, norm.dF_dSTZ);
					if (adjustForScale) {
						coefs.setItem(curParam++, 0, norm.dF_dSF);
					}
				} else {
					if (adjustOriginForScale) {
						coefs.setItem(0, 0, norm.dF_dSF);
					}
				}
				if (destIndex >= 0) {
					int curParam = destIndex;
					coefs.setItem(curParam++, 0, norm.dF_dTZ1);
					coefs.setItem(curParam++, 0, norm.dF_dTY);
					coefs.setItem(curParam++, 0, norm.dF_dTZ2);
					coefs.setItem(curParam++, 0, norm.dF_dTTX);
					coefs.setItem(curParam++, 0, norm.dF_dTTY);
					coefs.setItem(curParam++, 0, norm.dF_dTTZ);
					if (adjustForScale) {
						coefs.setItem(curParam++, 0, norm.dF_dTF);
					}
				} else {
					if (adjustOriginForScale) {
						coefs.setItem(0, 0, norm.dF_dTF);
					}
				}
				lsa.addMeasurement(coefs, computedWeight, L, 0);
//				System.out.print(MathUtil.d4(L) + "\t" + coefs.toString());
			}
		}
	}
	
	public static void buildCamera2RealMatrix(KeyPointList image) {
		image.camera2real = Stereo_7ParamsNorm.rot.makeAngles(image.sphereRZ1, image.sphereRY, image.sphereRZ2);
		image.dMdX = Stereo_7ParamsNorm.rot.make_dF_dR1(image.sphereRZ1, image.sphereRY, image.sphereRZ2);
		image.dMdY = Stereo_7ParamsNorm.rot.make_dF_dR2(image.sphereRZ1, image.sphereRY, image.sphereRZ2);
		image.dMdZ = Stereo_7ParamsNorm.rot.make_dF_dR3(image.sphereRZ1, image.sphereRY, image.sphereRZ2);
		Stereo_7ParamsNorm.rot.transformBackward(image.camera2real, -image.tx, -image.ty, -image.tz, image.worldOrigin);		
	}

	protected double computeOneDiscrepancy(KeyPointPair item, double PW1[], double PW2[]) {
		return norm.calcF_Only(item);
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
		System.out.println("Number of images in chain: " + (images.size() + 1));
		lsa = new LeastSquaresAdjust(getNumberOfParams(), 1);
		calculateNormalEquations();
		// Calculate Unknowns
		if (!lsa.calculate()) 
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
		int paramsPerImage = getParamsPerImage();
		for (int curImage = 0; curImage < images.size(); curImage++) {
			KeyPointList image = images.get(curImage);
			int index = (adjustOriginForScale ? 1 : 0) + curImage * paramsPerImage;
			int tmpIndex = index;
			StringBuilder sb = new StringBuilder();
			sb.append(image.imageFileStamp.getFile().getName());
			sb.append("\trz1=");		sb.append(MathUtil.rad2degStr(image.sphereRZ1));
			sb.append("\try=" );		sb.append(MathUtil.rad2degStr(image.sphereRY)); 
			sb.append("\trz2=");		sb.append(MathUtil.rad2degStr(image.sphereRZ2));
			sb.append("\ttx=" );		sb.append(MathUtil.d4(image.tx));
			sb.append("\tty=" );		sb.append(MathUtil.d4(image.ty));
			sb.append("\ttz=" );		sb.append(MathUtil.d4(image.tz));
			sb.append("\ts="  );		sb.append(MathUtil.d4(image.scaleZ));
			sb.append("\tdz1=");		sb.append(MathUtil.rad2degStr(u.getItem(0, index++))); 
			sb.append("\tdy=" );		sb.append(MathUtil.rad2degStr(u.getItem(0, index++)));
			sb.append("\tdz2=");		sb.append(MathUtil.rad2degStr(u.getItem(0, index++)));

			sb.append("\tdtx=");	sb.append(MathUtil.d4(u.getItem(0, index++)));
			sb.append("\tdty=");	sb.append(MathUtil.d4(u.getItem(0, index++)));
			sb.append("\tdtz=");	sb.append(MathUtil.d4(u.getItem(0, index++)));

			if (adjustForScale) {
				sb.append("\tds=");		sb.append(MathUtil.d4(u.getItem(0, index++)));
			}
			System.out.println(sb.toString());
			index = tmpIndex;
			image.sphereRZ1 = MathUtil.fixAngleMPI_PI(image.sphereRZ1 - u.getItem(0, index++));
			image.sphereRY  = MathUtil.fixAngleMPI_PI(image.sphereRY  - u.getItem(0, index++));
			image.sphereRZ2 = MathUtil.fixAngleMPI_PI(image.sphereRZ2 - u.getItem(0, index++));

			image.tx = image.tx - u.getItem(0, index++);
			image.ty = image.ty - u.getItem(0, index++);
			image.tz = image.tz - u.getItem(0, index++);

			if (adjustForScale) {
				image.scaleZ = (image.scaleZ - u.getItem(0, index++));
			}
			buildCamera2RealMatrix(image);
		}
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result;
	}
}
