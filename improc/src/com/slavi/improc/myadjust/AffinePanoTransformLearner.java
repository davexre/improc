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


public class AffinePanoTransformLearner extends PanoTransformer {

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
		dest[0] = srcImage.afa * sx + srcImage.afb * sy + srcImage.afc;
		dest[1] = srcImage.afd * sx + srcImage.afe * sy + srcImage.aff;
		dest[2] = 1.0;
	}

	public void transformBackward(double rx, double ry, KeyPointList srcImage, double dest[]) {
		dest[0] = Math.round(srcImage.aba * rx + srcImage.abb * ry + srcImage.abc);
		dest[1] = Math.round(srcImage.abd * rx + srcImage.abe * ry + srcImage.abf);
		if ((dest[0] < 0.0) || (dest[0] >= srcImage.imageSizeX) ||
			(dest[1] < 0.0) || (dest[1] >= srcImage.imageSizeY)) {
			dest[0] = 0.0;
			dest[1] = 0.0;
			dest[2] = -1.0;
		} else {
			dest[2] = 1.0;
		}
	}
	
	void calculatePrims() {
		origin.afa = 1.0;
		origin.afb = 0.0;
		origin.afc = 0.0;
		origin.afd = 0.0;
		origin.afe = 1.0;
		origin.aff = 0.0;

		origin.aba = 1.0;
		origin.abb = 0.0;
		origin.abc = 0.0;
		origin.abd = 0.0;
		origin.abe = 1.0;
		origin.abf = 0.0;

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
					targetToWorld.setItem(0, 0, minHopPairList.target.afa);
					targetToWorld.setItem(1, 0, minHopPairList.target.afb);
					targetToWorld.setItem(2, 0, minHopPairList.target.afc);
					targetToWorld.setItem(0, 1, minHopPairList.target.afd);
					targetToWorld.setItem(1, 1, minHopPairList.target.afe);
					targetToWorld.setItem(2, 1, minHopPairList.target.aff);
					targetToWorld.setItem(0, 2, 0.0);
					targetToWorld.setItem(1, 2, 0.0);
					targetToWorld.setItem(2, 2, 1.0);
					
					Matrix sourceToWorld = new Matrix(3, 3);
					sourceToTarget.mMul(targetToWorld, sourceToWorld);
					
					curImage.afa = sourceToWorld.getItem(0, 0);
					curImage.afb = sourceToWorld.getItem(1, 0);
					curImage.afc = sourceToWorld.getItem(2, 0);
					curImage.afd = sourceToWorld.getItem(0, 1);
					curImage.afe = sourceToWorld.getItem(1, 1);
					curImage.aff = sourceToWorld.getItem(2, 1);
					if (!sourceToWorld.inverse())
						throw new RuntimeException("Invalid inverse matrix");
					curImage.aba = sourceToWorld.getItem(0, 0);
					curImage.abb = sourceToWorld.getItem(1, 0);
					curImage.abc = sourceToWorld.getItem(2, 0);
					curImage.abd = sourceToWorld.getItem(0, 1);
					curImage.abe = sourceToWorld.getItem(1, 1);
					curImage.abf = sourceToWorld.getItem(2, 1);
					
/*					curImage.afa = minHopPairList.target.afa * minHopPairList.a - minHopPairList.target.afb * minHopPairList.b;
					curImage.afb = minHopPairList.target.afb * minHopPairList.a + minHopPairList.target.afa * minHopPairList.b;
					curImage.afc = minHopPairList.target.afa * minHopPairList.hTranslateX - 
							minHopPairList.target.afb * minHopPairList.hTranslateY + minHopPairList.target.hTranslateX;
					curImage.aff = minHopPairList.target.afb * minHopPairList.hTranslateX + 
							minHopPairList.target.afa * minHopPairList.hTranslateY + minHopPairList.target.hTranslateY;
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
					sourceToWorld.setItem(0, 0, minHopPairList.source.afa);
					sourceToWorld.setItem(1, 0, minHopPairList.source.afb);
					sourceToWorld.setItem(2, 0, minHopPairList.source.afc);
					sourceToWorld.setItem(0, 1, minHopPairList.source.afd);
					sourceToWorld.setItem(1, 1, minHopPairList.source.afe);
					sourceToWorld.setItem(2, 1, minHopPairList.source.aff);
					sourceToWorld.setItem(0, 2, 0.0);
					sourceToWorld.setItem(1, 2, 0.0);
					sourceToWorld.setItem(2, 2, 1.0);
					
					Matrix targetToWorld = new Matrix(3, 3);
					targetToSource.mMul(sourceToWorld, targetToWorld);

					curImage.afa = targetToWorld.getItem(0, 0);
					curImage.afb = targetToWorld.getItem(1, 0);
					curImage.afc = targetToWorld.getItem(2, 0);
					curImage.afd = targetToWorld.getItem(0, 1);
					curImage.afe = targetToWorld.getItem(1, 1);
					curImage.aff = targetToWorld.getItem(2, 1);
					if (!targetToWorld.inverse())
						throw new RuntimeException("Invalid inverse matrix");
					curImage.aba = targetToWorld.getItem(0, 0);
					curImage.abb = targetToWorld.getItem(1, 0);
					curImage.abc = targetToWorld.getItem(2, 0);
					curImage.abd = targetToWorld.getItem(0, 1);
					curImage.abe = targetToWorld.getItem(1, 1);
					curImage.abf = targetToWorld.getItem(2, 1);
					
/*					curImage.afa = minHopPairList.source.afa * minHopPairList.a + minHopPairList.source.afb * minHopPairList.b;
					curImage.afb = minHopPairList.source.afb * minHopPairList.a - minHopPairList.source.afa * minHopPairList.b;
					curImage.afc = - minHopPairList.source.afa * minHopPairList.hTranslateX + 
							minHopPairList.source.afb * minHopPairList.hTranslateY + minHopPairList.source.hTranslateX;
					curImage.aff = - minHopPairList.source.afb * minHopPairList.hTranslateX - 
							minHopPairList.source.afa * minHopPairList.hTranslateY + minHopPairList.source.hTranslateY;
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
		Matrix coefs = new Matrix(images.size() * 6, 1);			
		double PW1[] = new double[3];
		double PW2[] = new double[3];
		lsa.clear();
		for (KeyPointPairList pairList : chain) {
			for (KeyPointPair item : pairList.items) {
				if (isBad(item))
					continue;

				double computedWeight = getWeight(item);
				KeyPoint source = item.getKey();
				KeyPoint target = item.getValue();
				
				transformForeward(source.getDoubleX(), source.getDoubleY(), source.getKeyPointList(), PW1);
				transformForeward(target.getDoubleX(), target.getDoubleY(), target.getKeyPointList(), PW2);
				
				int srcIndex = images.indexOf(pairList.source) * 6;
				int destIndex = images.indexOf(pairList.target) * 6;

				coefs.make0();
				double L = PW1[0] - PW2[0];
				if (srcIndex >= 0) {
					coefs.setItem(srcIndex + 0, 0, source.getDoubleX());
					coefs.setItem(srcIndex + 1, 0, source.getDoubleY());
					coefs.setItem(srcIndex + 2, 0, 1.0);
				}
				if (destIndex >= 0) {
					coefs.setItem(destIndex + 0, 0, -target.getDoubleX());
					coefs.setItem(destIndex + 1, 0, -target.getDoubleY());
					coefs.setItem(destIndex + 2, 0, -1.0);
				}
//				System.out.print(L + "\t" + coefs.toString());
				lsa.addMeasurement(coefs, computedWeight, L, 0);

				coefs.make0();
				L = PW1[1] - PW2[1];
				if (srcIndex >= 0) {
					coefs.setItem(srcIndex + 3, 0, source.getDoubleX());
					coefs.setItem(srcIndex + 4, 0, source.getDoubleY());
					coefs.setItem(srcIndex + 5, 0, 1.0);
				}
				if (destIndex >= 0) {
					coefs.setItem(destIndex + 3, 0, -target.getDoubleX());
					coefs.setItem(destIndex + 4, 0, -target.getDoubleY());
					coefs.setItem(destIndex + 5, 0, -1.0);
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
		startNewIteration(result);
		if (chainModified) {
			if (images.size() <= 1)
				return result;
			System.out.println("************* COMPUTE PRIMS");
			origin = images.remove(0);
			calculatePrims();
		}

		lsa = new LeastSquaresAdjust(images.size() * 6, 1);
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
		System.out.println(origin.imageFileStamp.getFile().getName() + 
				"\tafa=" + MathUtil.d4(origin.afa) + 
				"\tafb=" + MathUtil.d4(origin.afb) +
				"\tafc=" + MathUtil.d4(origin.afc) +
				"\tafd=" + MathUtil.d4(origin.afd) +
				"\tafe=" + MathUtil.d4(origin.afe) +
				"\taff=" + MathUtil.d4(origin.aff)
				);

		Matrix work = new Matrix(3, 3);
		for (int curImage = 0; curImage < images.size(); curImage++) {
			KeyPointList image = images.get(curImage);
			int index = curImage * 6;
			System.out.println(image.imageFileStamp.getFile().getName() + 
					"\tafa=" + MathUtil.d4(image.afa) + 
					"\tafb=" + MathUtil.d4(image.afb) +
					"\tafc=" + MathUtil.d4(image.afc) +
					"\tafd=" + MathUtil.d4(image.afd) +
					"\tafe=" + MathUtil.d4(image.afe) +
					"\taff=" + MathUtil.d4(image.aff) +
					"\tdafa=" + MathUtil.d4(u.getItem(0, index + 0)) + 
					"\tdafb=" + MathUtil.d4(u.getItem(0, index + 1)) + 
					"\tdafc=" + MathUtil.d4(u.getItem(0, index + 2)) + 
					"\tdafd=" + MathUtil.d4(u.getItem(0, index + 3)) + 
					"\tdafe=" + MathUtil.d4(u.getItem(0, index + 4)) + 
					"\tdaff=" + MathUtil.d4(u.getItem(0, index + 5)) 
					);
			image.afa -= u.getItem(0, index + 0);
			image.afb -= u.getItem(0, index + 1);
			image.afc -= u.getItem(0, index + 2);
			image.afd -= u.getItem(0, index + 3);
			image.afe -= u.getItem(0, index + 4);
			image.aff -= u.getItem(0, index + 5);

			work.setItem(0, 0, image.afa);
			work.setItem(1, 0, image.afb);
			work.setItem(2, 0, image.afc);
			work.setItem(0, 1, image.afd);
			work.setItem(1, 1, image.afe);
			work.setItem(2, 1, image.aff);
			work.setItem(0, 2, 0.0);
			work.setItem(1, 2, 0.0);
			work.setItem(2, 2, 1.0);
			if (!work.inverse())
				throw new RuntimeException("Invalid inverse matrix");
			image.aba = work.getItem(0, 0);
			image.abb = work.getItem(1, 0);
			image.abc = work.getItem(2, 0);
			image.abd = work.getItem(0, 1);
			image.abe = work.getItem(1, 1);
			image.abf = work.getItem(2, 1);
		}
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result;
	}
}
