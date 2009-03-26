package com.slavi.improc.myadjust;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.math.MathUtil;
import com.slavi.math.RotationXYZ;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public class MyPanoPairTransformLearner3 {

	MyPanoPairTransformer3 tr;
	
	LeastSquaresAdjust lsa;

	ArrayList<KeyPointPairList> keyPointPairLists;
	
	public static final double defaultCameraFieldOfView = MathUtil.deg2rad * 40;
	public static final double defaultCameraFOV_to_ScaleZ = 1.0 / 
			(2.0 * Math.tan(defaultCameraFieldOfView / 2.0));
	
	public MyPanoPairTransformLearner3(ArrayList<KeyPointPairList> keyPointPairLists) {
		this.keyPointPairLists = keyPointPairLists;
		ArrayList<KeyPointList> images = new ArrayList<KeyPointList>();
		for (KeyPointPairList i : keyPointPairLists) {
			if (!images.contains(i.source))
				images.add(i.source);
			if (!images.contains(i.target))
				images.add(i.target);
		}
		for (KeyPointList image : images) {
			image.rx = 0.0;
			image.ry = 0.0;
			image.rz = 0.0;
			image.cameraOriginX = image.imageSizeX / 2.0;
			image.cameraOriginY = image.imageSizeY / 2.0;
			image.cameraScale = 1.0 / Math.max(image.imageSizeX, image.imageSizeY);
			image.scaleZ = defaultCameraFOV_to_ScaleZ;
			buildCamera2RealMatrix(image);
		}
		KeyPointList origin = images.remove(0);
		tr = new MyPanoPairTransformer3(origin, images);
		this.lsa = new LeastSquaresAdjust(tr.getNumberOfCoefsPerCoordinate(), 1);		
	}

	public void calculatePrims() throws Exception {
		tr.origin.rx = 0.0;
		tr.origin.ry = 0.0;
		tr.origin.rz = 0.0;
		tr.origin.cameraOriginX = tr.origin.imageSizeX / 2.0;
		tr.origin.cameraOriginY = tr.origin.imageSizeY / 2.0;
		tr.origin.cameraScale = 1.0 / Math.max(tr.origin.imageSizeX, tr.origin.imageSizeY);
		tr.origin.scaleZ = defaultCameraFOV_to_ScaleZ;
		
		ArrayList<KeyPointList> todo = new ArrayList<KeyPointList>(tr.images);
		int curImageIndex = todo.size() - 1;
		while (curImageIndex >= 0) {
			KeyPointList curImage = todo.get(curImageIndex);
			for (KeyPointPairList pairList : keyPointPairLists) {
				if (curImage == pairList.source) {
					if (todo.contains(pairList.target)) 
						continue;
					double angles[] = new double[3];
					Matrix sourceToTarget = RotationXYZ.makeAngles(pairList.rx, pairList.ry, pairList.rz);
					Matrix targetToWorld = RotationXYZ.makeAngles(pairList.target.rx, pairList.target.ry, pairList.target.rz);
					Matrix sourceToWorld = new Matrix(3, 3);
					targetToWorld.mMul(sourceToTarget, sourceToWorld);
					RotationXYZ.getRotationAngles(sourceToWorld, angles);
					curImage.rx = angles[0];
					curImage.ry = angles[1];
					curImage.rz = angles[2];
					todo.remove(curImageIndex);
					curImageIndex = todo.size();
				} else if (curImage == pairList.target) {
					if (todo.contains(pairList.source)) 
						continue;
					double angles[] = new double[3];
					RotationXYZ.getRotationAnglesBackword(pairList.rx, pairList.ry, pairList.rz, angles);
					Matrix targetToSource = RotationXYZ.makeAngles(angles[0], angles[1], angles[2]);
					Matrix sourceToWorld = RotationXYZ.makeAngles(pairList.source.rx, pairList.source.ry, pairList.source.rz);
					Matrix targetToWorld = new Matrix(3, 3);
					sourceToWorld.mMul(targetToSource, targetToWorld);
					RotationXYZ.getRotationAngles(targetToWorld, angles);
					curImage.rx = angles[0];
					curImage.ry = angles[1];
					curImage.rz = angles[2];
					todo.remove(curImageIndex);
					curImageIndex = todo.size();
				}
			}
			curImageIndex--;
		}
		
		if (todo.size() > 0) 
			throw new Exception("Failed calculating the prims");
		printCameraAngles(tr.origin);
		for (KeyPointList i : tr.images)
			printCameraAngles(i);
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

	private double oneOverSumWeights = 1.0;
	/**
	 * 
	 * @return Number of point pairs NOT marked as bad.
	 */
	protected int computeWeights() {
		int goodCount = 0;
		double sumWeight = 0;
		for (KeyPointPairList pairList : keyPointPairLists) {
			for (KeyPointPair item : pairList.items) {
				if (isBad(item))
					continue;
				double weight = getWeight(item); 
				if (weight < 0)
					throw new IllegalArgumentException("Negative weight received.");
				goodCount++;
				sumWeight += weight;
			}
		}
		if (sumWeight == 0.0) {
			oneOverSumWeights = 1.0 / goodCount;
		} else {
			oneOverSumWeights = 1.0 / sumWeight;
		}
		return goodCount;
	}
	
	public double getComputedWeight(KeyPointPair item) {
		return isBad(item) ? 0.0 : getWeight(item) * oneOverSumWeights; 
	}


	final double scaleScaleZ = 3;
	public static void buildCamera2RealMatrix(KeyPointList image) {
		image.camera2real = RotationXYZ.makeAngles(image.rx, image.ry, image.rz);
		image.dMdX = RotationXYZ.make_dF_dX(image.rx, image.ry, image.rz);
		image.dMdY = RotationXYZ.make_dF_dY(image.rx, image.ry, image.rz);
		image.dMdZ = RotationXYZ.make_dF_dZ(image.rx, image.ry, image.rz);
	}
	
	public boolean calculateDiscrepancy() {
		int goodCount = computeWeights();
		if (goodCount < lsa.getRequiredMeasurements())
			return false;
		Matrix coefs = new Matrix(tr.getNumberOfCoefsPerCoordinate(), 1);			

		tr.origin.rx = 0;
		tr.origin.ry = 0;
		tr.origin.rz = 0;
		buildCamera2RealMatrix(tr.origin);
		for (KeyPointList image : tr.images) {
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
		for (KeyPointPairList pairList : keyPointPairLists) {
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
				
				int srcIndex = tr.images.indexOf(pairList.source) * 4;
				int destIndex = tr.images.indexOf(pairList.target) * 4;
				
				coefs.make0();
	
				tr.transform3D(source, pairList.source, PW1);
				tr.transform3D(dest, pairList.target, PW2);
				
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
								source.keyPointList.camera2real.getItem(2, c2) * getTransformedCoord(PW2, c1)) * scaleScaleZ);
					}
					if (destIndex >= 0) {
						setCoef(coefs, dPW2dX2, dPW2dY2, dPW2dZ2, destIndex, c1, -getTransformedCoord(PW1, c2));
						setCoef(coefs, dPW2dX2, dPW2dY2, dPW2dZ2, destIndex, c2,  getTransformedCoord(PW1, c1));
						coefs.setItem(destIndex + 3, 0, (
								getTransformedCoord(PW1, c1) * dest.keyPointList.camera2real.getItem(2, c2) - 
								getTransformedCoord(PW1, c2) * dest.keyPointList.camera2real.getItem(2, c1)) * scaleScaleZ);
					}
					lsa.addMeasurement(coefs, computedWeight, L, 0);
				}
			}
		}
		return true;
	}

	public boolean calculateParameters() {
		if (!lsa.calculate()) 
			return false;

		// Build transformer
		Matrix u = lsa.getUnknown();
		System.out.println("U=");
		System.out.println(u.toString());
		u.rMul(-1.0);

		for (int curImage = 0; curImage < tr.images.size(); curImage++) {
			KeyPointList image = tr.images.get(curImage);
			int index = curImage * 4;

			image.scaleZ = (image.scaleZ + u.getItem(0, index + 3) / scaleScaleZ);
			image.rx = MathUtil.fixAngleMPI_PI(image.rx + u.getItem(0, index + 0));
			image.ry = MathUtil.fixAngleMPI_PI(image.ry + u.getItem(0, index + 1));
			image.rz = MathUtil.fixAngleMPI_PI(image.rz + u.getItem(0, index + 2));
			
/*				
			camera.scaleZ = Math.abs(camera.scaleZ + u.getItem(0, index + 3) / scaleScaleZ);
			camera.rx = MathUtil.fixAngle2PI(camera.rx + u.getItem(0, index + 0));
			camera.ry = MathUtil.fixAngle2PI(camera.ry + u.getItem(0, index + 1));
			camera.rz = MathUtil.fixAngle2PI(camera.rz + u.getItem(0, index + 2));
*/				
/*				
			camera.scaleZ += u.getItem(0, index + 3) / scaleScaleZ;
			if (camera.scaleZ < 0) {
				camera.rx = MathUtil.fixAngle2PI(Math.PI + camera.rx + u.getItem(0, index + 0));
				camera.ry = MathUtil.fixAngle2PI(Math.PI + camera.ry + u.getItem(0, index + 1));
				camera.scaleZ = -camera.scaleZ;
			} else {
				camera.rx = MathUtil.fixAngle2PI(camera.rx + u.getItem(0, index + 0));
				camera.ry = MathUtil.fixAngle2PI(camera.ry + u.getItem(0, index + 1));
			}
			camera.rz = MathUtil.fixAngle2PI(camera.rz + u.getItem(0, index + 2));
*/
			buildCamera2RealMatrix(image);
			printCameraAngles(image);
		}
		
		return true;
	}

	public double getDiscrepancy(Map.Entry<KeyPoint, KeyPoint> item) {
		return ((KeyPointPair) item).discrepancy;
	}

	public static double getWeight(KeyPointPair item) {
		return item.weight;
	}
	
	public static double getWeight2(Map.Entry<KeyPoint, KeyPoint> item) {
		KeyPointPair kp = (KeyPointPair) item;
		int nonzero = Math.min(kp.sourceSP.getNumberOfNonZero(), kp.targetSP.getNumberOfNonZero());
		if (nonzero == 0)
			nonzero = 1;
		int unmatched = kp.getUnmatchingCount();
		if (unmatched == 0)
			unmatched = 1;
		double w = (double) unmatched * kp.distanceToNearest / (double)nonzero;
		if (w < 1.0)
			w = 1.0;
		return 1.0 / w;
	}

	public boolean isBad(Map.Entry<KeyPoint, KeyPoint> item) {
		return ((KeyPointPair) item).bad;
	}

	public void setBad(Map.Entry<KeyPoint, KeyPoint> item, boolean bad) {
		((KeyPointPair) item).bad = bad;
	}

	public void setDiscrepancy(Map.Entry<KeyPoint, KeyPoint> item, double discrepancy) {
		((KeyPointPair) item).discrepancy = discrepancy;
	}

	public double getMedianSquareError() {
		return lsa.getMedianSquareError();
	}

	public static void printCameraAngles(KeyPointList image) {
		System.out.println(image.imageFileStamp.getFile().getName() + 
				"\tscaleZ=" + MathUtil.d4(image.scaleZ) + 
				"\trx=" + MathUtil.d4(image.rx * MathUtil.rad2deg) + 
				"\try=" + MathUtil.d4(image.ry * MathUtil.rad2deg) + 
				"\trz=" + MathUtil.d4(image.rz * MathUtil.rad2deg) 
				);
	}

	public static final int maxIterations = 10;
	
	private double computeDiscrepancies() {
		double result = 0.0;
		Point2D.Double PW1 = new Point2D.Double();
		Point2D.Double PW2 = new Point2D.Double();

		Statistics stat = new Statistics();
		stat.start();

		int pointCount = 0;
		int goodPointCount = 0;
		for (KeyPointPairList pairList : keyPointPairLists) {
			for (KeyPointPair pair : pairList.items) {
				pointCount++;
				// Compute for all points, so no item.isBad check
				MyPanoPairTransformer3.transform(pair.sourceSP.doubleX, pair.sourceSP.doubleY, pairList.source, PW1);
				MyPanoPairTransformer3.transformBackward(PW1.x, PW1.y, pairList.target, PW2);

				double dx = pair.targetSP.doubleX - PW2.x;
				double dy = pair.targetSP.doubleY - PW2.y;
				pair.discrepancy = Math.sqrt(dx*dx + dy*dy);
				pair.weight = pair.discrepancy < 1 ? 1.0 : 1.0 / pair.discrepancy;
//				if (pointCount < 10)
//					System.out.println(pointCount + "\t" + MathUtil.d4(dx) + "\t" + MathUtil.d4(dy) + "\t" + MathUtil.d4(item.discrepancy));
				if (!isBad(pair)) {
					goodPointCount++;
					result += pair.discrepancy;
					stat.addValue(pair.discrepancy);
				}
			}
		}
		stat.stop();
		System.out.println("MyDiscrepancy statistics:");
		System.out.println(stat.toString(Statistics.CStatMinMax));
		return goodPointCount == 0 ? Double.POSITIVE_INFINITY : result / goodPointCount;
	}
	
	protected boolean isAdjusted() {
		Statistics stat = new Statistics();
		stat.start();
		for (KeyPointPairList pairList : keyPointPairLists) {
			for (KeyPointPair item : pairList.items) {
				if (!isBad(item)) {
					stat.addValue(getDiscrepancy(item), getWeight(item));
				}
			}
		}
		stat.stop();

//		System.out.println("ADJUSTED statistics is: ");
//		System.out.println(stat.toString(Statistics.CStatAll));
		boolean adjusted = true;
		double maxDiscrepancy = stat.getAvgValue();
		if (maxDiscrepancy < 5)
			maxDiscrepancy = 5; //stat.getJ_End();
		for (KeyPointPairList pairList : keyPointPairLists) {
			for (KeyPointPair item : pairList.items) {
				boolean oldIsBad = isBad(item);
				double discrepancy = getDiscrepancy(item);
				boolean curIsBad = discrepancy > maxDiscrepancy; //stat.isBad(discrepancy);
				if (oldIsBad != curIsBad) {
					setBad(item, curIsBad);
					adjusted = false;
				}
			}
		}
		return adjusted;
	}
	
	public boolean calculate() {
		boolean failed = true;
		boolean adjusted = false;
		for (int iter = 0; iter < maxIterations; iter++) {
			adjusted = false;
			if (!calculateDiscrepancy())
				break;
			if (!calculateParameters())
				break;
			double mse1 = getMedianSquareError();
			double mse2 = computeDiscrepancies(); 
			System.out.println("Iter=" + iter + " MSE1=" + mse1 + " MSE2=" + mse2);
			adjusted = isAdjusted();
			if (adjusted && mse2 < 2) {
				failed = false;
				break;
			}
		}
		System.out.println(failed ? "\n\n*** FAILED\n\n" : "\n\n*** SUCESS\n\n"); 

		printCameraAngles(tr.origin);
		for (KeyPointList image : tr.images) {
			printCameraAngles(image);
		}
		
		return !failed;
	}
}
