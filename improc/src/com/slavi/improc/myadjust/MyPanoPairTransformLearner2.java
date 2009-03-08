package com.slavi.improc.myadjust;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.PanoPair;
import com.slavi.improc.PanoPairList;
import com.slavi.improc.pano.ImageData;
import com.slavi.math.MathUtil;
import com.slavi.math.RotationXYZ;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;

public class MyPanoPairTransformLearner2 {

	MyPanoPairTransformer2 tr;
	
	LeastSquaresAdjust lsa;

	ArrayList<PanoPairList> pairsLists;
	
	public MyPanoPairTransformLearner2(ArrayList<PanoPairList> pairsLists) {
		this.pairsLists = pairsLists;
		HashMap<String, ImageData> imagesMap = new HashMap<String, ImageData>();
		for (PanoPairList i : pairsLists) {
			ImageData image = imagesMap.get(i.sourceImage);
			if (image == null) {
				image = new ImageData();
				image.name = i.sourceImage;
				image.width = i.sourceImageSizeX;
				image.height = i.sourceImageSizeY;
				imagesMap.put(image.name, image);
			}
			i.source = image;

			image = imagesMap.get(i.targetImage);
			if (image == null) {
				image = new ImageData();
				image.name = i.targetImage;
				image.width = i.targetImageSizeX;
				image.height = i.targetImageSizeY;
				imagesMap.put(image.name, image);
			}
			i.target = image;
		}
		ArrayList<ImageData> images = new ArrayList<ImageData>(imagesMap.values());
		
		final double defaultCameraFieldOfView = 42; // degrees
		final double defaultCameraFOV_to_ScaleZ = 1.0 / 
				(2.0 * Math.tan(MathUtil.deg2rad * (defaultCameraFieldOfView / 2.0)));  
		for (ImageData image : images) {
			image.rx = 0.0;
			image.ry = 0.0;
			image.rz = 0.0;
			image.scaleZ = Math.max(image.width, image.height) * defaultCameraFOV_to_ScaleZ;
			buildCamera2RealMatrix(image);
		}
		ImageData origin = images.remove(0);
		tr = new MyPanoPairTransformer2(origin, images);
		this.lsa = new LeastSquaresAdjust(tr.getNumberOfCoefsPerCoordinate(), 1);		
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

	final double scaleScaleZ = 1;

	public static void buildCamera2RealMatrix(ImageData image) {
		image.camera2real = RotationXYZ.makeAngles(image.rx, image.ry, image.rz);
		image.dMdX = RotationXYZ.make_dF_dX(image.rx, image.ry, image.rz);
		image.dMdY = RotationXYZ.make_dF_dY(image.rx, image.ry, image.rz);
		image.dMdZ = RotationXYZ.make_dF_dZ(image.rx, image.ry, image.rz);
	}
	
	public boolean calculateDiscrepancy() {
//		int goodCount = computeWeights();
//		if (goodCount < lsa.getRequiredPoints())
//			return false;
		Matrix coefs = new Matrix(tr.getNumberOfCoefsPerCoordinate(), 1);			

		tr.origin.rx = 0;
		tr.origin.ry = 0;
		tr.origin.rz = 0;
		buildCamera2RealMatrix(tr.origin);

		for (ImageData image : tr.images) {
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

		MyPoint3D source = new MyPoint3D();
		MyPoint3D dest = new MyPoint3D();
		lsa.clear();
		int pointCounter = 0;
		for (PanoPairList pairList : pairsLists) {
			for (PanoPair item : pairList.items) {
				if (isBad(item))
					continue;
				pointCounter++;
				
				double computedWeight = 1.0; //getComputedWeight(item);
				source.x = item.sx;
				source.y = item.sy;
				source.z = 0;
				dest.x = item.tx;
				dest.y = item.ty;
				dest.z = 0;
				
				int srcIndex = tr.images.indexOf(pairList.source) * 4 + 1;
				int destIndex = tr.images.indexOf(pairList.target) * 4 + 1;
				
				coefs.make0();
	
				tr.transformToOrigin(source, pairList.source, PW1);
				tr.transformToOrigin(dest, pairList.target, PW2);
				
				P1.setItem(0, 0, item.sx);
				P1.setItem(0, 1, item.sy);
				P1.setItem(0, 2, pairList.source.scaleZ);
	//			P1.setItem(0, 2, 1.0);
				
				P2.setItem(0, 0, item.tx);
				P2.setItem(0, 1, item.ty);
				P2.setItem(0, 2, pairList.target.scaleZ);
	//			P2.setItem(0, 2, 1.0);
				
				pairList.source.dMdX.mMul(P1, dPW1dX1);
				pairList.source.dMdY.mMul(P1, dPW1dY1);
				pairList.source.dMdZ.mMul(P1, dPW1dZ1);
				
				pairList.target.dMdX.mMul(P2, dPW2dX2);
				pairList.target.dMdY.mMul(P2, dPW2dY2);
				pairList.target.dMdZ.mMul(P2, dPW2dZ2);
	
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
								pairList.source.camera2real.getItem(2, c1) * getTransformedCoord(PW2, c2) - 
								pairList.source.camera2real.getItem(2, c2) * getTransformedCoord(PW2, c1)) * scaleScaleZ);
					} else {
						coefs.setItem(0, 0, (
								pairList.source.camera2real.getItem(2, c1) * getTransformedCoord(PW2, c2) - 
								pairList.source.camera2real.getItem(2, c2) * getTransformedCoord(PW2, c1)) * scaleScaleZ);
					}
					if (destIndex >= 0) {
						setCoef(coefs, dPW2dX2, dPW2dY2, dPW2dZ2, destIndex, c1, -getTransformedCoord(PW1, c2));
						setCoef(coefs, dPW2dX2, dPW2dY2, dPW2dZ2, destIndex, c2,  getTransformedCoord(PW1, c1));
						coefs.setItem(destIndex + 3, 0, (
								getTransformedCoord(PW1, c1) * pairList.target.camera2real.getItem(2, c2) - 
								getTransformedCoord(PW1, c2) * pairList.target.camera2real.getItem(2, c1)) * scaleScaleZ);
					} else {
						coefs.setItem(0, 0, (
								getTransformedCoord(PW1, c1) * pairList.target.camera2real.getItem(2, c2) - 
								getTransformedCoord(PW1, c2) * pairList.target.camera2real.getItem(2, c1)) * scaleScaleZ);
					}
					String name = "";
					switch (c1) {
					case 0: name = "xy"; break;
					case 1: name = "yz"; break;
					case 2: name = "xz"; break;
					}
					if (pointCounter < 5)
						System.out.println(name + "\t" + MathUtil.d4(L) + "\t" + coefs.toOneLineString());
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

		tr.origin.scaleZ = (tr.origin.scaleZ + u.getItem(0, 0)) / scaleScaleZ;
		for (int curImage = 0; curImage < tr.images.size(); curImage++) {
			ImageData image = tr.images.get(curImage);
			int index = curImage * 4 + 1;
			System.out.println("OLD/NEW Camera");

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

	public double getWeight(Map.Entry<KeyPoint, KeyPoint> item) {
		return ((KeyPointPair) item).weight;
	}

	public boolean isBad(PanoPair item) {
		return item.bad;
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

	public static void printCameraAngles(ImageData image) {
		System.out.println("Image ID " + image.name + 
				"\tscaleZ=" + (image.scaleZ) + 
				"\trx=" + (image.rx * MathUtil.rad2deg) + 
				"\try=" + (image.ry * MathUtil.rad2deg) + 
				"\trz=" + (image.rz * MathUtil.rad2deg) 
				);
	}

	public static final int maxIterations = 7;
	public boolean calculate() {
		boolean failed = true;
		if (calculateDiscrepancy()) {
			for (int iter = 0; iter < maxIterations; iter++) {
				if (!calculateParameters())
					break;
				if (!calculateDiscrepancy())
					break;
				double mse = getMedianSquareError();
				System.out.println("MSE=" + mse);
				if (mse < 0.01) {
					failed = false;
					break;
				}
			}
		}
		if (failed) {
			System.out.println("FAILED");
		}
		return !failed;
	}
}
