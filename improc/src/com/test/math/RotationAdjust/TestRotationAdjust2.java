package com.test.math.RotationAdjust;

import java.util.List;
import java.util.Map;

import com.slavi.math.MathUtil;
import com.slavi.math.RotationXYZ;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.BaseTransformLearner;
import com.slavi.math.transform.BaseTransformer;

public class TestRotationAdjust2 {
	
	public static class ImageToWorldTransformer extends BaseTransformer<MyImagePoint, MyImagePoint> {
		public int getInputSize() {
			return 2;
		}

		public int getNumberOfCoefsPerCoordinate() {
			return cameras.length * 3;
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
				source.camera.realFocalDistance * source.camera.camera2real.getItem(2, 0); // / averageFocalDistance;
			dest.y = 
				source.x * source.camera.camera2real.getItem(0, 1) +
				source.y * source.camera.camera2real.getItem(1, 1) +
				source.camera.realFocalDistance * source.camera.camera2real.getItem(2, 1); // / averageFocalDistance;
			dest.z =  //averageFocalDistance * (
				source.x * source.camera.camera2real.getItem(0, 2) +
				source.y * source.camera.camera2real.getItem(1, 2) +
				source.camera.realFocalDistance * source.camera.camera2real.getItem(2, 2); // / averageFocalDistance);
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

		private void buildCamera2RealMatrix(MyCamera camera) {
			/*Matrix m = RotationXYZ.makeAngles(camera.rx, camera.ry, camera.rz);
			Matrix s = new Matrix(3, 3);
			s.make0();
			s.setItem(0, 0, camera.scaleXY);
			s.setItem(1, 1, camera.scaleXY);
			s.setItem(2, 2, camera.scaleZ);
			m.mMul(s, camera.camera2real);
			*/
			camera.camera2real = RotationXYZ.makeAngles(camera.rx, camera.ry, camera.rz);
			camera.dMdX = RotationXYZ.make_dF_dX(camera.rx, camera.ry, camera.rz);
			camera.dMdY = RotationXYZ.make_dF_dY(camera.rx, camera.ry, camera.rz);
			camera.dMdZ = RotationXYZ.make_dF_dZ(camera.rx, camera.ry, camera.rz);
		}
		
		public boolean calculateOne() {
			int goodCount = computeWeights();
			if (goodCount < lsa.getRequiredPoints())
				return false;
			Matrix coefs = new Matrix(tr.getNumberOfCoefsPerCoordinate(), 1);			

			tr.originCamera.rx = 0;
			tr.originCamera.ry = 0;
			tr.originCamera.rz = 0;
			tr.originCamera.scaleXY = 1;
			tr.originCamera.scaleZ = 1;
			buildCamera2RealMatrix(tr.originCamera);

			int counter = 1;
			for (MyCamera camera : tr.cameras) {
//				camera.rx = counter;
//				camera.ry = counter + 1;
//				camera.rz = counter + 2;
				camera.scaleXY = 1;
				camera.scaleZ = tr.originCamera.realFocalDistance / camera.realFocalDistance;
				buildCamera2RealMatrix(camera);
				counter++;
			}			
			
			
			Matrix P1 = new Matrix(1, 3);
			Matrix P2 = new Matrix(1, 3);
			
			Matrix t1 = new Matrix(1, 3);
			Matrix t2 = new Matrix(1, 3);
			
			Matrix dPW1dX1 = new Matrix(1, 3);
			Matrix dPW1dY1 = new Matrix(1, 3);
			Matrix dPW1dZ1 = new Matrix(1, 3);

			Matrix dPW2dX2 = new Matrix(1, 3);
			Matrix dPW2dY2 = new Matrix(1, 3);
			Matrix dPW2dZ2 = new Matrix(1, 3);

			MyImagePoint PW1 = new MyImagePoint();
			MyImagePoint PW2 = new MyImagePoint();
			lsa.clear();
			for (Map.Entry<MyImagePoint, MyImagePoint> item : items) {
				if (isBad(item))
					continue;
				
				double computedWeight = getComputedWeight(item);
				MyImagePoint source = item.getKey();
				MyImagePoint dest = item.getValue();
				
				int srcIndex = tr.indexOf(source.camera) * 3;
				int destIndex = tr.indexOf(dest.camera) * 3;
				
				coefs.make0();
				
				P1.setItem(0, 0, source.x);
				P1.setItem(0, 1, source.y);
				P1.setItem(0, 2, source.camera.realFocalDistance);

				P2.setItem(0, 0, dest.x);
				P2.setItem(0, 1, dest.y);
				P2.setItem(0, 2, dest.camera.realFocalDistance);
				
				double L;
				tr.transform(source, PW1);
				t1.setItem(0, 0, PW1.x);
				t1.setItem(0, 1, PW1.y);
				t1.setItem(0, 2, PW1.z);

				tr.transform(dest, PW2);
				t2.setItem(0, 0, PW2.x);
				t2.setItem(0, 1, PW2.y);
				t2.setItem(0, 2, PW2.z);

				/*
						P'1 = M1 * P1
						P'2 = M2 * P2
						P'1 <> P'2

						F() = P'1 x P'2 = 0
						F() = M1 * P1 x M2 * P2 = 0
						
						fx = P'1(y) * P'2(z) - P'1(z) * P'2(y) = 0
						fy = P'1(x) * P'2(z) - P'1(z) * P'2(x) = 0
						fz = P'1(x) * P'2(y) - P'1(y) * P'2(x) = 0
						
						dF/dX1 = (dP'1/dX1
				 */
				source.camera.dMdX.mMul(P1, dPW1dX1);
				source.camera.dMdY.mMul(P1, dPW1dY1);
				source.camera.dMdZ.mMul(P1, dPW1dZ1);
				
				dest.camera.dMdX.mMul(P2, dPW2dX2);
				dest.camera.dMdY.mMul(P2, dPW2dY2);
				dest.camera.dMdZ.mMul(P2, dPW2dZ2);

				// L(x) = (d1*x1 + e1*y1 + f1*z1) * (g2*x2 + h2*y2 + i2*z2) - (d2*x2 + e2*y2 + f2*z2) * (g1*x1 + h1*y1 + i1*z1)
				// d(L(x))/d(d1) = x1 * (g2*x2 + h2*y2 + i2*z2)
				// dfx
				coefs.make0();
				L = PW1.y * PW2.z - PW1.z * PW2.y;
				if (srcIndex >= 0) {
					coefs.setItem(srcIndex + 0, 0, dPW1dX1.getItem(0, 1) * PW2.z - dPW1dX1.getItem(0, 2) * PW2.y);
					coefs.setItem(srcIndex + 1, 0, dPW1dY1.getItem(0, 1) * PW2.z - dPW1dY1.getItem(0, 2) * PW2.y);
					coefs.setItem(srcIndex + 2, 0, dPW1dZ1.getItem(0, 1) * PW2.z - dPW1dZ1.getItem(0, 2) * PW2.y);
				}
				if (destIndex >= 0) {
					coefs.setItem(destIndex + 0, 0, PW1.y * dPW2dX2.getItem(0, 2) - PW1.z * dPW2dX2.getItem(0, 2));
					coefs.setItem(destIndex + 1, 0, PW1.y * dPW2dY2.getItem(0, 2) - PW1.z * dPW2dY2.getItem(0, 2));
					coefs.setItem(destIndex + 2, 0, PW1.y * dPW2dZ2.getItem(0, 2) - PW1.z * dPW2dZ2.getItem(0, 2));
				}
				lsa.addMeasurement(coefs, computedWeight, L, 0);
				// dfy
				coefs.make0();
				L = PW1.x * PW2.z - PW1.z * PW2.x;
				if (srcIndex >= 0) {
					coefs.setItem(srcIndex + 0, 0, dPW1dX1.getItem(0, 0) * PW2.z - dPW1dX1.getItem(0, 2) * PW2.x);
					coefs.setItem(srcIndex + 1, 0, dPW1dY1.getItem(0, 0) * PW2.z - dPW1dY1.getItem(0, 2) * PW2.x);
					coefs.setItem(srcIndex + 2, 0, dPW1dZ1.getItem(0, 0) * PW2.z - dPW1dZ1.getItem(0, 2) * PW2.x);
				}
				if (destIndex >= 0) {
					coefs.setItem(destIndex + 0, 0, PW1.x * dPW2dX2.getItem(0, 2) - PW1.z * dPW2dX2.getItem(0, 0));
					coefs.setItem(destIndex + 1, 0, PW1.x * dPW2dY2.getItem(0, 2) - PW1.z * dPW2dY2.getItem(0, 0));
					coefs.setItem(destIndex + 2, 0, PW1.x * dPW2dZ2.getItem(0, 2) - PW1.z * dPW2dZ2.getItem(0, 0));
				}
				lsa.addMeasurement(coefs, computedWeight, L, 0);
				coefs.make0();
				// dfz
				L = PW1.x * PW2.y - PW1.y * PW2.x;
				if (srcIndex >= 0) {
					coefs.setItem(srcIndex + 0, 0, dPW1dX1.getItem(0, 0) * PW2.y - dPW1dX1.getItem(0, 1) * PW2.x);
					coefs.setItem(srcIndex + 1, 0, dPW1dY1.getItem(0, 0) * PW2.y - dPW1dY1.getItem(0, 1) * PW2.x);
					coefs.setItem(srcIndex + 2, 0, dPW1dZ1.getItem(0, 0) * PW2.y - dPW1dZ1.getItem(0, 1) * PW2.x);
				}
				if (destIndex >= 0) {
					coefs.setItem(destIndex + 0, 0, PW1.x * dPW2dX2.getItem(0, 1) - PW1.y * dPW2dX2.getItem(0, 0));
					coefs.setItem(destIndex + 1, 0, PW1.x * dPW2dY2.getItem(0, 1) - PW1.y * dPW2dY2.getItem(0, 0));
					coefs.setItem(destIndex + 2, 0, PW1.x * dPW2dZ2.getItem(0, 1) - PW1.y * dPW2dZ2.getItem(0, 0));
				}
				lsa.addMeasurement(coefs, computedWeight, L, 0);
			}

			if (!lsa.calculate()) 
				return false;

			// Build transformer
			Matrix u = lsa.getUnknown();
			System.out.println("U=");
			System.out.println(u.toString());

			for (int curCamera = 0; curCamera < tr.cameras.length; curCamera++) {
				MyCamera camera = tr.cameras[curCamera];
				System.out.println("Camera " + camera.cameraId);
				int index = curCamera * 3;
				System.out.println("OLD Camera " + camera.cameraId + " rx=" + camera.rx + " ry=" + camera.ry + " rz=" + camera.rz);
				camera.rx += u.getItem(0, index + 0);
				camera.ry += u.getItem(0, index + 1);
				camera.rz += u.getItem(0, index + 2);
				buildCamera2RealMatrix(camera);
				System.out.println("NEW Camera " + camera.cameraId + " rx=" + camera.rx + " ry=" + camera.ry + " rz=" + camera.rz);
			}
			
			return true;
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
	}
	
	static double cameraAngles[][] = new double[][] {
		// rx, ry, rz, f
//		{ 20 * MathUtil.deg2rad, 20 * MathUtil.deg2rad,  0 * MathUtil.deg2rad, 10},
		{  0 * MathUtil.deg2rad,  0 * MathUtil.deg2rad,  0 * MathUtil.deg2rad, 10},
		{-20 * MathUtil.deg2rad,  10 * MathUtil.deg2rad,-20 * MathUtil.deg2rad, 10},
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
	
//		int count = 1;
//		for (MyCamera camera : cameras) {
//			camera.rx = count / 10;
//			camera.ry = (count + 1) /10;
//			camera.rz = (count + 2) / 10;
//			count++;
//		}
		
		ImageToWorldTransformLearner learner = new ImageToWorldTransformLearner(cameras[0], cameras, pointPairs);
		if (!learner.calculateOne()) {
			System.out.println("FAILED");
			return;
		}
		
		for (MyCamera camera : cameras) {
			System.out.println("Camera " + camera.cameraId);
			System.out.println(camera.camera2real.toString());
			System.out.println();
		}
		Utils.calculateDiscrepancy(cameras, pointPairs, learner.tr);
		System.out.println(pointPairs.size());
		System.out.println("Done.");
	}
}
