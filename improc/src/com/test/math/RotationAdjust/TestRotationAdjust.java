package com.test.math.RotationAdjust;

import java.util.List;
import java.util.Map;

import com.slavi.math.MathUtil;
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

		private void setCoef(Matrix coefs, int atIndex,	MyImagePoint p, double transformedCoord) {
			coefs.setItem(atIndex + 0, 0, p.x * transformedCoord);
			coefs.setItem(atIndex + 1, 0, p.y * transformedCoord);
			coefs.setItem(atIndex + 2, 0, p.camera.realFocalDistance * transformedCoord);
		}

		public boolean calculateDiscrepancy() {
			int goodCount = computeWeights();
			if (goodCount < lsa.getRequiredPoints())
				return false;
			Matrix coefs = new Matrix(tr.getNumberOfCoefsPerCoordinate(), 1);			

			tr.originCamera.camera2real.makeE();
			
			Matrix t1 = new Matrix(1, 3);
			Matrix t2 = new Matrix(1, 3);
			MyImagePoint tmp1 = new MyImagePoint();
			MyImagePoint tmp2 = new MyImagePoint();
			lsa.clear();
			for (Map.Entry<MyImagePoint, MyImagePoint> item : items) {
				if (isBad(item))
					continue;
				
				double computedWeight = getComputedWeight(item);
				MyImagePoint source = item.getKey();
				MyImagePoint dest = item.getValue();
				
				int srcIndex = tr.indexOf(source.camera) * 9;
				int destIndex = tr.indexOf(dest.camera) * 9;
				
				
				tr.transform(source, tmp1);
				t1.setItem(0, 0, tmp1.x);
				t1.setItem(0, 1, tmp1.y);
				t1.setItem(0, 2, tmp1.z);

				tr.transform(dest, tmp2);
				t2.setItem(0, 0, tmp2.x);
				t2.setItem(0, 1, tmp2.y);
				t2.setItem(0, 2, tmp2.z);
				
				for (int curCoord = 0; curCoord < 3; curCoord++) {
					int c1;
					int c2;
					switch (curCoord) {
					case 0: c1 = 1; c2 = 2; break;
					case 1: c1 = 0; c2 = 2; break;
					case 2: 
					default:
							c1 = 0; c2 = 1; break;
					}

					coefs.make0();
					// L(x) = (d1*x1 + e1*y1 + f1*z1) * (g2*x2 + h2*y2 + i2*z2) - (d2*x2 + e2*y2 + f2*z2) * (g1*x1 + h1*y1 + i1*z1)
					// d(L(x))/d(d1) = x1 * (g2*x2 + h2*y2 + i2*z2)
					double L = 
						t1.getItem(0, c1) * t2.getItem(0, c2) -
						t1.getItem(0, c2) * t2.getItem(0, c1);
					/*
					 * fx: P'1(y) * P'2(z) - P'1(z) * P'2(y) = 0
					 * fy: P'1(x) * P'2(z) - P'1(z) * P'2(x) = 0
					 * fz: P'1(x) * P'2(y) - P'1(y) * P'2(x) = 0
					 * 
					 * f(curCoord): P'1(c1) * P'2(c2) - P'1(c2) * P'2(c1) = 0
					 */
					if (srcIndex >= 0) {
						setCoef(coefs, srcIndex + c1 * 3, source,  t2.getItem(0, c2));
						setCoef(coefs, srcIndex + c2 * 3, source, -t2.getItem(0, c1));
					}
					if (destIndex >= 0) {
						setCoef(coefs, destIndex + c1 * 3, dest, -t1.getItem(0, c1));
						setCoef(coefs, destIndex + c2 * 3, dest,  t1.getItem(0, c2));
					}
					lsa.addMeasurement(coefs, computedWeight, L, 0);
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

			for (int curCamera = 0; curCamera < tr.cameras.length; curCamera++) {
				MyCamera camera = tr.cameras[curCamera];
				int index = curCamera * 9;
				camera.camera2real.setItem(0, 0, camera.camera2real.getItem(0, 0) - u.getItem(0, index + 0));
				camera.camera2real.setItem(1, 0, camera.camera2real.getItem(1, 0) - u.getItem(0, index + 1));
				camera.camera2real.setItem(2, 0, camera.camera2real.getItem(2, 0) - u.getItem(0, index + 2));
				camera.camera2real.setItem(0, 1, camera.camera2real.getItem(0, 1) - u.getItem(0, index + 3));
				camera.camera2real.setItem(1, 1, camera.camera2real.getItem(1, 1) - u.getItem(0, index + 4));
				camera.camera2real.setItem(2, 1, camera.camera2real.getItem(2, 1) - u.getItem(0, index + 5));
				camera.camera2real.setItem(0, 2, camera.camera2real.getItem(0, 2) - u.getItem(0, index + 6));
				camera.camera2real.setItem(1, 2, camera.camera2real.getItem(1, 2) - u.getItem(0, index + 7));
				camera.camera2real.setItem(2, 2, camera.camera2real.getItem(2, 2) - u.getItem(0, index + 8));
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
		{-20 * MathUtil.deg2rad,  10 * MathUtil.deg2rad,-20 * MathUtil.deg2rad, 11},
		{-20 * MathUtil.deg2rad,  10 * MathUtil.deg2rad,-20 * MathUtil.deg2rad, 12},
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
		boolean failed = true;
		if (learner.calculateDiscrepancy()) {
			for (int iter = 0; iter < 20; iter++) {
				if (!learner.calculateParameters())
					break;
				if (!learner.calculateDiscrepancy())
					break;
				double mse = learner.getMedianSquareError();
				System.out.println("MSE=" + mse);
				if (mse < 0.03) {
					failed = false;
					break;
				}
			}
		}
		if (failed) {
			System.out.println("FAILED");
			return;
		}
	
//		ImageToWorldTransformer tr = calcPrims(cameras[0], cameras, pointPairs);
		for (MyCamera camera : cameras) {
			System.out.println("Camera " + camera.cameraId);
			System.out.println(camera.camera2real.toString());
			System.out.println();
		}
//		Utils.calculateDiscrepancy(cameras, pointPairs, tr);
		Utils.calculateDiscrepancy(cameras, pointPairs, learner.tr);
		Matrix m = learner.tr.cameras[0].camera2real;
		Matrix mInv = m.makeCopy();
		mInv.inverse();
		Matrix mTr = new Matrix();
		m.transpose(mTr);
		m.printM("m");
		mInv.printM("mInv");
		mTr.printM("mTr");
		
		System.out.println(pointPairs.size());
		System.out.println("Done.");
	}
}
