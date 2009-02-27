package com.test.math.RotationAdjust;

import java.util.List;
import java.util.Map;

import com.slavi.math.MathUtil;
import com.slavi.math.RotationXYZ;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.BaseTransformLearner;
import com.slavi.math.transform.BaseTransformer;

public class TestRotationAdjust {
	
	public static class ImageToWorldTransformer extends BaseTransformer<MyImagePoint, MyImagePoint> {
		public int getInputSize() {
			return 2;
		}

		public int getNumberOfCoefsPerCoordinate() {
			return cameras.length * 9;
		}

		public int getOutputSize() {
			return 2;
		}

		public double getSourceCoord(MyImagePoint item, int coordIndex) {
			switch (coordIndex) {
			case 0: return item.x;
			case 1: return item.y;
			default:
				throw new IllegalArgumentException();
			}
		}

		public double getTargetCoord(MyImagePoint item, int coordIndex) {
			switch (coordIndex) {
			case 0: return item.x;
			case 1: return item.y;
			default:
				throw new IllegalArgumentException();
			}
		}

		public void setSourceCoord(MyImagePoint item, int coordIndex, double value) {
			switch (coordIndex) {
			case 0: item.x = value; break;
			case 1: item.y = value; break;
			default:
				throw new IllegalArgumentException();
			}
		}

		public void setTargetCoord(MyImagePoint item, int coordIndex, double value) {
			switch (coordIndex) {
			case 0: item.x = value; break;
			case 1: item.y = value; break;
			default:
				throw new IllegalArgumentException();
			}
		}

		public void transform(MyImagePoint source, MyImagePoint dest) {
			dest.x = 
				source.x * source.camera.camera2real.getItem(0, 0) +
				source.y * source.camera.camera2real.getItem(1, 0) +
				source.camera.realFocalDistance * source.camera.camera2real.getItem(2, 0);
			dest.y = 
				source.x * source.camera.camera2real.getItem(0, 1) +
				source.y * source.camera.camera2real.getItem(1, 1) +
				source.camera.realFocalDistance * source.camera.camera2real.getItem(2, 1);
			dest.z =
				source.x * source.camera.camera2real.getItem(0, 2) +
				source.y * source.camera.camera2real.getItem(1, 2) +
				source.camera.realFocalDistance * source.camera.camera2real.getItem(2, 2);
		}
		
		MyCamera[] cameras;
		
		MyCamera originCamera;
		
		public int indexOf(MyCamera camera) {
			for (int i = 0; i < cameras.length; i++)
				if (cameras[i] == camera)
					return i;
			return -1;
		}
		
		public ImageToWorldTransformer(MyCamera originCamera, MyCamera[] cameras) {
			this.originCamera = originCamera;
			int count = cameras.length;
			this.cameras = new MyCamera[count - 1];
			int index = 0;
			for (int i = 0; i < count; i++) {
				if (cameras[i] != originCamera) {
					this.cameras[index++] = cameras[i];
				}
			}
		}
	}
	
	public static class ImageToWorldTransformLearner extends BaseTransformLearner<MyImagePoint, MyImagePoint> {

		ImageToWorldTransformer tr;
		
		LeastSquaresAdjust lsa;
		
		public ImageToWorldTransformLearner(MyCamera originCamera, MyCamera[] cameras,
				Iterable<MyPointPair> pointsPairList) {
			super(new ImageToWorldTransformer(originCamera, cameras), pointsPairList);
			tr = (ImageToWorldTransformer) transformer;
			this.lsa = new LeastSquaresAdjust(transformer.getNumberOfCoefsPerCoordinate(), 1);		
		}

		private void setCoef(Matrix coefs, int atIndex,	MyImagePoint transformedCoord, double imageCoord) {
			coefs.setItem(atIndex + 0, 0, transformedCoord.x * imageCoord);
			coefs.setItem(atIndex + 1, 0, transformedCoord.y * imageCoord);
			coefs.setItem(atIndex + 2, 0, transformedCoord.z * imageCoord);
		}

		private double getImageCoord(MyImagePoint point, int coord) {
			switch (coord) {
			case 0: return point.x;
			case 1: return point.y;
			case 2: return point.camera.realFocalDistance;
			}
			throw new RuntimeException();
		}
		
		private double getTransformedCoord(MyImagePoint point, int coord) {
			switch (coord) {
			case 0: return point.x;
			case 1: return point.y;
			case 2: return point.z;
			}
			throw new RuntimeException();
		}
		
		public boolean calculateDiscrepancy() {
			int goodCount = computeWeights();
			if (goodCount < lsa.getRequiredPoints())
				return false;
			Matrix coefs = new Matrix(tr.getNumberOfCoefsPerCoordinate(), 1);			

			tr.originCamera.camera2real.makeE();
			
			MyImagePoint pp1 = new MyImagePoint();
			MyImagePoint pp2 = new MyImagePoint();
			lsa.clear();
			for (Map.Entry<MyImagePoint, MyImagePoint> item : items) {
				if (isBad(item))
					continue;
				
				double computedWeight = 1.0; //getComputedWeight(item);
				MyImagePoint source = item.getKey();
				MyImagePoint dest = item.getValue();
				
				int srcIndex = tr.indexOf(source.camera) * 9;
				int destIndex = tr.indexOf(dest.camera) * 9;
				
				tr.transform(source, pp1);
				tr.transform(dest, pp2);
				for (int c1 = 0; c1 < 3; c1++) {
					int c2 = (c1 + 1) % 3;
					coefs.make0();
					double L = 
						getTransformedCoord(pp1, c1) * getTransformedCoord(pp2, c2) -
						getTransformedCoord(pp1, c2) * getTransformedCoord(pp2, c1);
					/*
					 * fx: P'1(y) * P'2(z) - P'1(z) * P'2(y) = 0
					 * fy: P'1(z) * P'2(x) - P'1(x) * P'2(z) = 0
					 * fz: P'1(x) * P'2(y) - P'1(y) * P'2(x) = 0
					 * 
					 * f(curCoord): P'1(c1) * P'2(c2) - P'1(c2) * P'2(c1) = 0
					 */
					if (srcIndex >= 0) {
						setCoef(coefs, srcIndex + c1 * 3, pp2,  getImageCoord(source, c1));
						setCoef(coefs, srcIndex + c2 * 3, pp2, -getImageCoord(source, c2));
					}
					if (destIndex >= 0) {
						setCoef(coefs, destIndex + c1 * 3, pp1, -getImageCoord(dest, c1));
						setCoef(coefs, destIndex + c2 * 3, pp1,  getImageCoord(dest, c2));
					}
					lsa.addMeasurement(coefs, computedWeight, L, 0);

				}
				if (srcIndex >= 0) {
					// sum(X*X)=1
					addSumXX(coefs, source.camera, srcIndex);
					addSumYY(coefs, source.camera, srcIndex);
				}
				if (destIndex >= 0) {
					addSumXX(coefs, dest.camera, destIndex);
					addSumYY(coefs, dest.camera, destIndex);
				}
			}
			return true;
		}
		
		private void addSumXX(Matrix coefs, MyCamera camera, int cameraIndex) {
			if (cameraIndex < 0)
				return;
			for (int i = 0; i < 3; i++) {
				coefs.make0();
				double L = 0;
				for (int j = 0; j < 3; j++) {
					double d = camera.camera2real.getItem(i, j);
					L += Math.pow(d, 2.0);
					coefs.setItem(cameraIndex + j * 3 + i, 0, 2.0 * d);
				}
				lsa.addMeasurement(coefs, 1.0, L - 1.0, 0);
			}
			for (int j = 0; j < 3; j++) {
				coefs.make0();
				double L = 0;
				for (int i = 0; i < 3; i++) {
					double d = camera.camera2real.getItem(i, j);
					L += Math.pow(d, 2.0);
					coefs.setItem(cameraIndex + j * 3 + i, 0, 2.0 * d);
				}
				lsa.addMeasurement(coefs, 1.0, L - 1.0, 0);
			}
		}
		
		private void addSumYY(Matrix coefs, MyCamera camera, int cameraIndex) {
			if (cameraIndex < 0)
				return;
			for (int i = 0; i < 3; i++) {
				coefs.make0();
				double L = 0;
				for (int j = 0; j < 3; j++) {
					int i1 = (i + 1) % 3;
					double d = camera.camera2real.getItem(i, j);
					double d1 = camera.camera2real.getItem(i1, j);
					L += d * d1;
					coefs.setItem(cameraIndex + j * 3 + i, 0, d1);
				}
				lsa.addMeasurement(coefs, 1.0, L, 0);
			}
		}
		
		public boolean calculateParameters() {
			if (!lsa.calculate()) 
				return false;

			// Build transformer
			Matrix u = lsa.getUnknown();
			System.out.println("U=");
			System.out.println(u.toString());
			u.rMul(-1.0);

			for (int curCamera = 0; curCamera < tr.cameras.length; curCamera++) {
				MyCamera camera = tr.cameras[curCamera];
				int index = curCamera * 9;
				System.out.println("BEFORE");
				RotationXYZ.dumpTestForRotationMatrix(camera.camera2real);
				camera.camera2real.setItem(0, 0, camera.camera2real.getItem(0, 0) + u.getItem(0, index + 0));
				camera.camera2real.setItem(1, 0, camera.camera2real.getItem(1, 0) + u.getItem(0, index + 1));
				camera.camera2real.setItem(2, 0, camera.camera2real.getItem(2, 0) + u.getItem(0, index + 2));
				camera.camera2real.setItem(0, 1, camera.camera2real.getItem(0, 1) + u.getItem(0, index + 3));
				camera.camera2real.setItem(1, 1, camera.camera2real.getItem(1, 1) + u.getItem(0, index + 4));
				camera.camera2real.setItem(2, 1, camera.camera2real.getItem(2, 1) + u.getItem(0, index + 5));
				camera.camera2real.setItem(0, 2, camera.camera2real.getItem(0, 2) + u.getItem(0, index + 6));
				camera.camera2real.setItem(1, 2, camera.camera2real.getItem(1, 2) + u.getItem(0, index + 7));
				camera.camera2real.setItem(2, 2, camera.camera2real.getItem(2, 2) + u.getItem(0, index + 8));
				System.out.println("AFTER");
				RotationXYZ.dumpTestForRotationMatrix(camera.camera2real);
			}
			return true;
		}
		
		public boolean calculateOne() {
			if (calculateDiscrepancy())
				if (calculateParameters()) 
					return true;
			return false;
		}

		public MyImagePoint createTemporaryTargetObject() {
			throw new UnsupportedOperationException();
		}

		public double getDiscrepancy(Map.Entry<MyImagePoint, MyImagePoint> item) {
			return ((MyPointPair) item).discrepancy;
		}

		public int getRequiredTrainingPoints() {
			return 0;
		}

		public double getWeight(Map.Entry<MyImagePoint, MyImagePoint> item) {
			return ((MyPointPair) item).weight;
		}

		public boolean isBad(Map.Entry<MyImagePoint, MyImagePoint> item) {
			return ((MyPointPair) item).isBad;
		}

		public void setBad(Map.Entry<MyImagePoint, MyImagePoint> item, boolean bad) {
			((MyPointPair) item).isBad = bad;
		}

		public void setDiscrepancy(Map.Entry<MyImagePoint, MyImagePoint> item, double discrepancy) {
			((MyPointPair) item).discrepancy = discrepancy;
		}

		public double getMedianSquareError() {
			return lsa.getMedianSquareError();
		}
	}
	
	static double cameraAngles[][] = new double[][] {
		// rx, ry, rz, f
//		{ 20 * MathUtil.deg2rad, 20 * MathUtil.deg2rad,  0 * MathUtil.deg2rad, 10},
		{  0 * MathUtil.deg2rad,  0 * MathUtil.deg2rad,  0 * MathUtil.deg2rad, 10},
		{-20 * MathUtil.deg2rad,  10 * MathUtil.deg2rad,-20 * MathUtil.deg2rad, 10},
//		{-20 * MathUtil.deg2rad,  10 * MathUtil.deg2rad,-20 * MathUtil.deg2rad, 12},
//		{-20 * MathUtil.deg2rad,-20 * MathUtil.deg2rad, 20 * MathUtil.deg2rad, 12},
//		{  0 * MathUtil.deg2rad,  0 * MathUtil.deg2rad,-20 * MathUtil.deg2rad, 11},
	};

	public static void main(String[] args) {
		List<MyPoint3D> realPoints = Utils.generateRealPoints();
		
		MyPoint3D cameraOrigin = new MyPoint3D();
		cameraOrigin.p.setItem(0, 0, -5);
		cameraOrigin.p.setItem(0, 1, -5);
		cameraOrigin.p.setItem(0, 2, -5);
		MyCamera cameras[] = Utils.generateCameras(cameraOrigin, cameraAngles);
		
		List<MyPointPair> pointPairs = Utils.generatePointPairs(cameras, realPoints);

		for (MyCamera camera : cameras) {
			camera.camera2real.makeE();
		}

		ImageToWorldTransformLearner learner = new ImageToWorldTransformLearner(cameras[0], cameras, pointPairs);
//		learner.tr.cameras[0].realFocalDistance = 12;
		boolean failed = true;
		if (learner.calculateDiscrepancy()) {
			for (int iter = 0; iter < 2; iter++) {
				if (!learner.calculateParameters())
					break;
				if (!learner.calculateDiscrepancy())
					break;
				double mse = learner.getMedianSquareError();
				System.out.println("MSE=" + mse);
				learner.tr.cameras[0].camera2real.printM("A");
				if (mse < 0.1) {
					failed = false;
					break;
				}
			}
		}
		if (failed) {
			System.out.println("FAILED");
			return;
		}
	
		for (MyCamera camera : cameras) {
			System.out.println("Camera " + camera.cameraId);
			System.out.println(camera.camera2real.toString());
			System.out.println();
		}
		Utils.calculateDiscrepancy(cameras, pointPairs, learner.tr);
//		Matrix m = learner.tr.cameras[0].camera2real;
//		Matrix mInv = m.makeCopy();
//		mInv.inverse();
//		Matrix mTr = new Matrix();
//		m.transpose(mTr);
//		m.printM("m");
//		mInv.printM("mInv");
//		mTr.printM("mTr");
		
		System.out.println(pointPairs.size());
		System.out.println("Done.");
	}
}
