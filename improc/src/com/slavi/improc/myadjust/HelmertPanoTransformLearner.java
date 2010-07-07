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

	public void initialize(ArrayList<KeyPointPairList> chain) {
		this.chain = chain;
		this.images = new ArrayList<KeyPointList>();
		this.ignoredPairLists = new ArrayList<KeyPointPairList>();
		this.iteration = 0;
	}
	
	public double getDiscrepancyThreshold() {
		return maxDiscrepancyInPixelsOfOriginImage;
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
		dest[0] = srcImage.hfa * sx - srcImage.hfb * sy + srcImage.hfc;
		dest[1] = srcImage.hfb * sx + srcImage.hfa * sy + srcImage.hfd;
		dest[2] = 1.0;
	}

	public void transformBackward(double rx, double ry, KeyPointList srcImage, double dest[]) {
		dest[0] = srcImage.hba * rx - srcImage.hbb * ry + srcImage.hbc;
		dest[1] = srcImage.hbb * rx + srcImage.hba * ry + srcImage.hbd;
		dest[2] = 1.0;
		if ((dest[0] < 0.0) || (dest[0] >= srcImage.imageSizeX) ||
			(dest[1] < 0.0) || (dest[1] >= srcImage.imageSizeY)) {
			dest[0] = 0.0;
			dest[1] = 0.0;
			dest[2] = -1.0;
		}
	}
	
	void calculatePrims() {
		origin.hfa = 1.0;
		origin.hfb = 0.0;
		origin.hfc = 0.0;
		origin.hfd = 0.0;

		origin.hba = 1.0;
		origin.hbb = 0.0;
		origin.hbc = 0.0;
		origin.hbd = 0.0;

		origin.calculatePrimsAtHop = 0;
		
		ArrayList<KeyPointList> todo = new ArrayList<KeyPointList>(images);
		for (KeyPointList image : todo) {
			image.calculatePrimsAtHop = -1;
		}
		for (KeyPointPairList pairList : chain) {
			pairList.a = pairList.scale * Math.cos(pairList.angle);
			pairList.b = pairList.scale * Math.sin(pairList.angle);
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
					Matrix sourceToTarget = new Matrix(3, 3);
					sourceToTarget.setItem(0, 0, minHopPairList.a);
					sourceToTarget.setItem(1, 0, -minHopPairList.b);
					sourceToTarget.setItem(2, 0, minHopPairList.translateX);
					sourceToTarget.setItem(0, 1, minHopPairList.b);
					sourceToTarget.setItem(1, 1, minHopPairList.a);
					sourceToTarget.setItem(2, 1, minHopPairList.translateY);
					sourceToTarget.setItem(0, 2, 0.0);
					sourceToTarget.setItem(1, 2, 0.0);
					sourceToTarget.setItem(2, 2, 1.0);
					
					Matrix targetToWorld = new Matrix(3, 3);
					targetToWorld.setItem(0, 0, minHopPairList.target.hfa);
					targetToWorld.setItem(1, 0, -minHopPairList.target.hfb);
					targetToWorld.setItem(2, 0, minHopPairList.target.hfc);
					targetToWorld.setItem(0, 1, minHopPairList.target.hfb);
					targetToWorld.setItem(1, 1, minHopPairList.target.hfa);
					targetToWorld.setItem(2, 1, minHopPairList.target.hfd);
					targetToWorld.setItem(0, 2, 0.0);
					targetToWorld.setItem(1, 2, 0.0);
					targetToWorld.setItem(2, 2, 1.0);
					
					Matrix sourceToWorld = new Matrix(3, 3);
					sourceToTarget.mMul(targetToWorld, sourceToWorld);
					
					curImage.hfa = sourceToWorld.getItem(0, 0);
					curImage.hfb = sourceToWorld.getItem(0, 1);
					curImage.hfc = sourceToWorld.getItem(2, 0);
					curImage.hfd = sourceToWorld.getItem(2, 1);
					if (!sourceToWorld.inverse())
						throw new RuntimeException("Invalid inverse matrix");
					curImage.hba = sourceToWorld.getItem(0, 0);
					curImage.hbb = sourceToWorld.getItem(0, 1);
					curImage.hbc = sourceToWorld.getItem(2, 0);
					curImage.hbd = sourceToWorld.getItem(2, 1);
					
/*					curImage.hfa = minHopPairList.target.hfa * minHopPairList.a - minHopPairList.target.hfb * minHopPairList.b;
					curImage.hfb = minHopPairList.target.hfb * minHopPairList.a + minHopPairList.target.hfa * minHopPairList.b;
					curImage.hTranslateX = minHopPairList.target.hfa * minHopPairList.hTranslateX - 
							minHopPairList.target.hfb * minHopPairList.hTranslateY + minHopPairList.target.hTranslateX;
					curImage.hTranslateY = minHopPairList.target.hfb * minHopPairList.hTranslateX + 
							minHopPairList.target.hfa * minHopPairList.hTranslateY + minHopPairList.target.hTranslateY;
*/							
//					System.out.println(curImage.imageFileStamp.getFile().getName() + "\t" + minHopPairList.target.imageFileStamp.getFile().getName());
				} else { // if (curImage == minHopPairList.target) {
					Matrix targetToSource = new Matrix(3, 3);
					targetToSource.setItem(0, 0, minHopPairList.a);
					targetToSource.setItem(1, 0, -minHopPairList.b);
					targetToSource.setItem(2, 0, minHopPairList.translateX);
					targetToSource.setItem(0, 1, minHopPairList.b);
					targetToSource.setItem(1, 1, minHopPairList.a);
					targetToSource.setItem(2, 1, minHopPairList.translateY);
					targetToSource.setItem(0, 2, 0.0);
					targetToSource.setItem(1, 2, 0.0);
					targetToSource.setItem(2, 2, 1.0);
					if (!targetToSource.inverse())
						throw new RuntimeException("Invalid inverse matrix");
					
					Matrix sourceToWorld = new Matrix(3, 3);
					sourceToWorld.setItem(0, 0, minHopPairList.source.hfa);
					sourceToWorld.setItem(1, 0, -minHopPairList.source.hfb);
					sourceToWorld.setItem(2, 0, minHopPairList.source.hfc);
					sourceToWorld.setItem(0, 1, minHopPairList.source.hfb);
					sourceToWorld.setItem(1, 1, minHopPairList.source.hfa);
					sourceToWorld.setItem(2, 1, minHopPairList.source.hfd);
					sourceToWorld.setItem(0, 2, 0.0);
					sourceToWorld.setItem(1, 2, 0.0);
					sourceToWorld.setItem(2, 2, 1.0);
					
					Matrix targetToWorld = new Matrix(3, 3);
					targetToSource.mMul(sourceToWorld, targetToWorld);
					
					curImage.hfa = targetToWorld.getItem(0, 0);
					curImage.hfb = targetToWorld.getItem(0, 1);
					curImage.hfc = targetToWorld.getItem(2, 0);
					curImage.hfd = targetToWorld.getItem(2, 1);
					if (!targetToWorld.inverse())
						throw new RuntimeException("Invalid inverse matrix");
					curImage.hba = targetToWorld.getItem(0, 0);
					curImage.hbb = targetToWorld.getItem(0, 1);
					curImage.hbc = targetToWorld.getItem(2, 0);
					curImage.hbd = targetToWorld.getItem(2, 1);
					
/*					curImage.hfa = minHopPairList.source.hfa * minHopPairList.a + minHopPairList.source.hfb * minHopPairList.b;
					curImage.hfb = minHopPairList.source.hfb * minHopPairList.a - minHopPairList.source.hfa * minHopPairList.b;
					curImage.hTranslateX = - minHopPairList.source.hfa * minHopPairList.hTranslateX + 
							minHopPairList.source.hfb * minHopPairList.hTranslateY + minHopPairList.source.hTranslateX;
					curImage.hTranslateY = - minHopPairList.source.hfb * minHopPairList.hTranslateX - 
							minHopPairList.source.hfa * minHopPairList.hTranslateY + minHopPairList.source.hTranslateY;
*/
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
		double scaleOLD = MathUtil.hypot(origin.hfa, origin.hfb);
		double angleOLD = Math.atan2(origin.hfb, origin.hfa);
		System.out.println(origin.imageFileStamp.getFile().getName() + 
				"\tangle=" + MathUtil.rad2degStr(angleOLD) + 
				"\tscale=" + MathUtil.d4(scaleOLD) + 
				"\ttranslateX=" + MathUtil.d4(origin.hfc) + 
				"\ttranslateY=" + MathUtil.d4(origin.hfd)
				);

		Matrix work = new Matrix(3, 3);
		for (int curImage = 0; curImage < images.size(); curImage++) {
			KeyPointList image = images.get(curImage);
			int index = curImage * 4;
			scaleOLD = MathUtil.hypot(image.hfa, image.hfb);
			angleOLD = Math.atan2(image.hfb, image.hfa);
			image.hfa -= u.getItem(0, index + 0);
			image.hfb -= u.getItem(0, index + 1);
			double scaleNEW = MathUtil.hypot(image.hfa, image.hfb);
			double angleNEW = Math.atan2(image.hfb, image.hfa);
			System.out.println(image.imageFileStamp.getFile().getName() + 
					"\tangle=" + MathUtil.rad2degStr(angleOLD) + 
					"\tscale=" + MathUtil.d4(scaleOLD) + 
					"\ttranslateX=" + MathUtil.d4(image.hfc) + 
					"\ttranslateY=" + MathUtil.d4(image.hfd) +
					"\tdangle=" + MathUtil.rad2degStr(angleOLD - angleNEW) + 
					"\tdscale=" + MathUtil.d4(scaleOLD - scaleNEW) + 
					"\tdTrX=" + MathUtil.d4(u.getItem(0, index + 2)) + 
					"\tdTrY=" + MathUtil.d4(u.getItem(0, index + 3)) 
					);
			image.hfc -= u.getItem(0, index + 2);
			image.hfd -= u.getItem(0, index + 3);
			
			work.setItem(0, 0, image.hfa);
			work.setItem(1, 0, -image.hfb);
			work.setItem(2, 0, image.hfc);
			work.setItem(0, 1, image.hfb);
			work.setItem(1, 1, image.hfa);
			work.setItem(2, 1, image.hfd);
			work.setItem(0, 2, 0.0);
			work.setItem(1, 2, 0.0);
			work.setItem(2, 2, 1.0);
			if (!work.inverse())
				throw new RuntimeException("Invalid inverse matrix");
			image.hba = work.getItem(0, 0);
			image.hbb = work.getItem(0, 1);
			image.hbc = work.getItem(2, 0);
			image.hbd = work.getItem(2, 1);
		}
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result;
	}
}
