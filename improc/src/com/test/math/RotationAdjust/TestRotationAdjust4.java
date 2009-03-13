package com.test.math.RotationAdjust;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.slavi.math.MathUtil;
import com.slavi.math.RotationXYZ;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.BaseTransformer;

public class TestRotationAdjust4 {
	
	public static class ImageToWorldTransformer extends BaseTransformer<MyImagePoint, MyImagePoint> {
		public int getInputSize() {
			return 2;
		}
		

		public int getNumberOfCoefsPerCoordinate() {
			return cameras.length * 4 + 1;
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
				source.camera.scaleZ * source.camera.camera2real.getItem(2, 0);
			dest.y = 
				source.x * source.camera.camera2real.getItem(0, 1) +
				source.y * source.camera.camera2real.getItem(1, 1) +
				source.camera.scaleZ * source.camera.camera2real.getItem(2, 1);
			dest.z = 
				source.x * source.camera.camera2real.getItem(0, 2) +
				source.y * source.camera.camera2real.getItem(1, 2) +
				source.camera.scaleZ * source.camera.camera2real.getItem(2, 2);
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
	
	public static class ImageToWorldTransformLearner { 

		ImageToWorldTransformer tr;
		
		LeastSquaresAdjust lsa;
		
		ArrayList<MyPointPair> items;
		
		public ImageToWorldTransformLearner(MyCamera originCamera, MyCamera[] cameras,
				ArrayList<MyPointPair> pointsPairList) {
			items = pointsPairList;
			tr = new ImageToWorldTransformer(originCamera, cameras);
			this.lsa = new LeastSquaresAdjust(tr.getNumberOfCoefsPerCoordinate(), 1);		
		}

		private void setCoef(Matrix coef, Matrix dPWdX, Matrix dPWdY, Matrix dPWdZ,
				int atIndex, int c1, double transformedCoord) {
			coef.setItem(atIndex + 0, 0, dPWdX.getItem(0, c1) * transformedCoord + coef.getItem(atIndex + 0, 0));
			coef.setItem(atIndex + 1, 0, dPWdY.getItem(0, c1) * transformedCoord + coef.getItem(atIndex + 1, 0));
			coef.setItem(atIndex + 2, 0, dPWdZ.getItem(0, c1) * transformedCoord + coef.getItem(atIndex + 2, 0));
		}
		
		private double getTransformedCoord(MyImagePoint point, int coord) {
			switch (coord) {
			case 0: return point.x;
			case 1: return point.y;
			case 2: return point.z;
			}
			throw new RuntimeException();
		}
		
		private double oneOverSumWeights = 1.0;
		/**
		 * 
		 * @return Number of point pairs NOT marked as bad.
		 */
		protected int computeWeights() {
			int goodCount = 0;
			double sumWeight = 0;
			for (MyPointPair item : items) {
				if (isBad(item))
					continue;
				double weight = getWeight(item); 
				if (weight < 0)
					throw new IllegalArgumentException("Negative weight received.");
				goodCount++;
				sumWeight += weight;
			}
			if (sumWeight == 0.0) {
				oneOverSumWeights = 1.0 / goodCount;
			} else {
				oneOverSumWeights = 1.0 / sumWeight;
			}
			return goodCount;
		}
		
		public double getComputedWeight(MyPointPair item) {
			return isBad(item) ? 0.0 : getWeight(item) * oneOverSumWeights; 
		}

		final double scaleScaleZ = 1.0;
		public boolean calculateDiscrepancy() {
			int goodCount = computeWeights();
			if (goodCount < lsa.getRequiredPoints())
				return false;
			Matrix coefs = new Matrix(tr.getNumberOfCoefsPerCoordinate(), 1);			

			tr.originCamera.rx = 0;
			tr.originCamera.ry = 0;
			tr.originCamera.rz = 0;
			buildCamera2RealMatrix(tr.originCamera);

			for (MyCamera camera : tr.cameras) {
				buildCamera2RealMatrix(camera);
			}
			
			Matrix P1 = new Matrix(1, 3);
			Matrix P2 = new Matrix(1, 3);
			
			Matrix dPW1dX1 = new Matrix(1, 3);
			Matrix dPW1dY1 = new Matrix(1, 3);
			Matrix dPW1dZ1 = new Matrix(1, 3);

			Matrix dPW2dX2 = new Matrix(1, 3);
			Matrix dPW2dY2 = new Matrix(1, 3);
			Matrix dPW2dZ2 = new Matrix(1, 3);

			MyImagePoint source1 = new MyImagePoint();
			MyImagePoint dest1 = new MyImagePoint();
			
			MyImagePoint PW1 = new MyImagePoint();
			MyImagePoint PW2 = new MyImagePoint();
			lsa.clear();
			for (MyPointPair item : items) {
				if (isBad(item))
					continue;
				
				double computedWeight = getComputedWeight(item);
				MyImagePoint source = item.getKey();
				MyImagePoint dest = item.getValue();
				
				source1.camera = source.camera;
				source1.x = (source.x - source.camera.originX) * source.camera.scale;
				source1.y = (source.y - source.camera.originY) * source.camera.scale;
				
				dest1.camera = dest.camera;
				dest1.x = (dest.x - dest.camera.originX) * dest.camera.scale;
				dest1.y = (dest.y - dest.camera.originY) * dest.camera.scale;
				
				int srcIndex = tr.indexOf(source.camera) * 4 + 1;
				int destIndex = tr.indexOf(dest.camera) * 4 + 1;
				
				coefs.make0();

				tr.transform(source1, PW1);
				tr.transform(dest1, PW2);
				
				P1.setItem(0, 0, source1.x);
				P1.setItem(0, 1, source1.y);
				P1.setItem(0, 2, source1.camera.scaleZ);
				
				P2.setItem(0, 0, dest1.x);
				P2.setItem(0, 1, dest1.y);
				P2.setItem(0, 2, dest1.camera.scaleZ);
				
				source.camera.dMdX.mMul(P1, dPW1dX1);
				source.camera.dMdY.mMul(P1, dPW1dY1);
				source.camera.dMdZ.mMul(P1, dPW1dZ1);
				
				dest.camera.dMdX.mMul(P2, dPW2dX2);
				dest.camera.dMdY.mMul(P2, dPW2dY2);
				dest.camera.dMdZ.mMul(P2, dPW2dZ2);

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
								source.camera.camera2real.getItem(2, c1) * getTransformedCoord(PW2, c2) - 
								source.camera.camera2real.getItem(2, c2) * getTransformedCoord(PW2, c1)) * scaleScaleZ);
					} else {
						coefs.setItem(0, 0, (
								source.camera.camera2real.getItem(2, c1) * getTransformedCoord(PW2, c2) - 
								source.camera.camera2real.getItem(2, c2) * getTransformedCoord(PW2, c1)) * scaleScaleZ);
					}
					if (destIndex >= 0) {
						setCoef(coefs, dPW2dX2, dPW2dY2, dPW2dZ2, destIndex, c1, -getTransformedCoord(PW1, c2));
						setCoef(coefs, dPW2dX2, dPW2dY2, dPW2dZ2, destIndex, c2,  getTransformedCoord(PW1, c1));
						coefs.setItem(destIndex + 3, 0, (
								getTransformedCoord(PW1, c1) * dest.camera.camera2real.getItem(2, c2) - 
								getTransformedCoord(PW1, c2) * dest.camera.camera2real.getItem(2, c1)) * scaleScaleZ);
					} else {
						coefs.setItem(0, 0, (
								getTransformedCoord(PW1, c1) * dest.camera.camera2real.getItem(2, c2) - 
								getTransformedCoord(PW1, c2) * dest.camera.camera2real.getItem(2, c1)) * scaleScaleZ);
					}
					String name = "";
					switch (c1) {
					case 0: name = "xy"; break;
					case 1: name = "yz"; break;
					case 2: name = "xz"; break;
					}
//					System.out.println(name + "\t" + MathUtil.d4(L) + "\t" + coefs.toOneLineString());
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
			u.rMul(-1.0);

			System.out.println("OLD/NEW Camera");
			printCameraAngles(tr.originCamera);
			tr.originCamera.scaleZ = (tr.originCamera.scaleZ + u.getItem(0, 0)) / scaleScaleZ;
			buildCamera2RealMatrix(tr.originCamera);
			printCameraAngles(tr.originCamera);
			for (int curCamera = 0; curCamera < tr.cameras.length; curCamera++) {
				MyCamera camera = tr.cameras[curCamera];
				int index = curCamera * 4 + 1;
				System.out.println("OLD/NEW Camera");
				printCameraAngles(camera);

				camera.scaleZ = (camera.scaleZ + u.getItem(0, index + 3) / scaleScaleZ);
				camera.rx = MathUtil.fixAngleMPI_PI(camera.rx + u.getItem(0, index + 0));
				camera.ry = MathUtil.fixAngleMPI_PI(camera.ry + u.getItem(0, index + 1));
				camera.rz = MathUtil.fixAngleMPI_PI(camera.rz + u.getItem(0, index + 2));
				
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
				buildCamera2RealMatrix(camera);
				printCameraAngles(camera);
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

		public double getMedianSquareError() {
			return lsa.getMedianSquareError();
		}
	}
	
	public static void buildCamera2RealMatrix(MyCamera camera) {
		camera.camera2real = RotationXYZ.makeAngles(camera.rx, camera.ry, camera.rz);
		camera.dMdX = RotationXYZ.make_dF_dX(camera.rx, camera.ry, camera.rz);
		camera.dMdY = RotationXYZ.make_dF_dY(camera.rx, camera.ry, camera.rz);
		camera.dMdZ = RotationXYZ.make_dF_dZ(camera.rx, camera.ry, camera.rz);
	}
	
	public static void printCameraAngles(MyCamera camera) {
		System.out.println("Camera ID " + camera.cameraId + 
				"\tscaleZ=" + (camera.scaleZ) + 
				"\trx=" + (camera.rx * MathUtil.rad2deg) + 
				"\try=" + (camera.ry * MathUtil.rad2deg) + 
				"\trz=" + (camera.rz * MathUtil.rad2deg) 
				);
	}
	
	static double cameraAngles[][] = new double[][] {
		// rx, ry, rz, f
//		{ 20 * MathUtil.deg2rad, 20 * MathUtil.deg2rad,  0 * MathUtil.deg2rad, 10},
		{  0 * MathUtil.deg2rad,  0 * MathUtil.deg2rad,  0 * MathUtil.deg2rad, 4000},
		{-20 * MathUtil.deg2rad,-20 * MathUtil.deg2rad, 20 * MathUtil.deg2rad, 4500},
		{-20 * MathUtil.deg2rad, 10 * MathUtil.deg2rad,-20 * MathUtil.deg2rad, 4200},
		{  0 * MathUtil.deg2rad,  0 * MathUtil.deg2rad,-20 * MathUtil.deg2rad, 3800},
	};

	static Data readTestData() throws Exception {
		MyPoint3D origin = new MyPoint3D();
		
		double cameraAngles[][] = new double[][] {
			// rx, ry, rz, f
			{  0 * MathUtil.deg2rad,  0 * MathUtil.deg2rad,  0 * MathUtil.deg2rad, 2000},
			{ 20 * MathUtil.deg2rad,  0 * MathUtil.deg2rad,  0 * MathUtil.deg2rad, 2500},
		};
		
		Data data = new Data();
		data.cameras = Utils.generateCameras(origin, cameraAngles);
		for (MyCamera camera : data.cameras) {
			camera.imageSizeX = 2272;
			camera.imageSizeY = 1712;
			camera.originX = camera.imageSizeX / 2.0;
			camera.originY = camera.imageSizeY / 2.0;
			camera.scale = 1.0 / Math.max(camera.imageSizeX, camera.imageSizeY);
			camera.scaleZ = 1.0 / (2.0 * Math.tan(MathUtil.deg2rad * (20)));  
		}
		data.pointPairs = new ArrayList<MyPointPair>();
		
		InputStream is = TestRotationAdjust4.class.getResourceAsStream("TestData.txt");
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		in.readLine();
		while (in.ready()) {
			String line = in.readLine();
			if (line.equals(""))
				break;
			StringTokenizer st = new StringTokenizer(line);
			MyPointPair pair = new MyPointPair();
			pair.srcPoint = new MyImagePoint();
			pair.destPoint = new MyImagePoint();
			pair.srcPoint.camera = data.cameras[0];
			pair.destPoint.camera = data.cameras[1];
			
			pair.srcPoint.x = Double.parseDouble(st.nextToken());
			pair.srcPoint.y = Double.parseDouble(st.nextToken());
			pair.destPoint.x = Double.parseDouble(st.nextToken());
			pair.destPoint.y = Double.parseDouble(st.nextToken());

			pair.discrepancy = Double.parseDouble(st.nextToken());
			pair.weight = pair.discrepancy < 1 ? 1 : 1 / pair.discrepancy;
			
			data.pointPairs.add(pair);
		}
		is.close();
		return data;
	}
	
	public static class Data {
		MyCamera cameras[];
		ArrayList<MyPointPair> pointPairs;
	}
	
	public static Data generateData() {
		Data data = new Data();
		List<MyPoint3D> realPoints = Utils.generateRealPoints(3000.0);
		
		MyPoint3D cameraOrigin = new MyPoint3D();
		cameraOrigin.p.setItem(0, 0, 2000);
		cameraOrigin.p.setItem(0, 1, 2000);
		cameraOrigin.p.setItem(0, 2, -5000);
		data.cameras = Utils.generateCameras(cameraOrigin, cameraAngles);
		
		data.pointPairs = Utils.generatePointPairs(data.cameras, realPoints);
		return data;
	}

	public static void transform2(MyImagePoint source, MyImagePoint dest) {
/*		double x = source.x - source.camera.originX;
		double y = source.y - source.camera.originY;
		double z = source.camera.scaleZ / source.camera.scale;
*/
		
		double x = (source.x - source.camera.originX) * source.camera.scale;
		double y = (source.y - source.camera.originY) * source.camera.scale;
		double z = source.camera.scaleZ;

		dest.x = 1000 * (
			x * source.camera.camera2real.getItem(0, 0) +
			y * source.camera.camera2real.getItem(1, 0) +
			z * source.camera.camera2real.getItem(2, 0));
		dest.y = 1000 * (
			x * source.camera.camera2real.getItem(0, 1) +
			y * source.camera.camera2real.getItem(1, 1) +
			z * source.camera.camera2real.getItem(2, 1));
		dest.z = 1000 * (
			x * source.camera.camera2real.getItem(0, 2) +
			y * source.camera.camera2real.getItem(1, 2) +
			z * source.camera.camera2real.getItem(2, 2));
	}
	
	public static void calculateDiscrepancy(
			List<MyPointPair> pointPairs, BaseTransformer<MyImagePoint, MyImagePoint> tr) {
		MyImagePoint p1 = new MyImagePoint();
		MyImagePoint p2 = new MyImagePoint();
		
		Statistics stat = new Statistics();
		stat.start();
//		System.out.println("*** Source - Target");
		for (MyPointPair pair : pointPairs) {
			transform2(pair.srcPoint, p1);
			transform2(pair.destPoint, p2);

//			System.out.println(
//					p1.x + "\t" + p1.y + "\t" + p1.z + "\t" + 
//					p2.x + "\t" + p2.y + "\t" + p2.z);
/*
			pair.myDiscrepancy = Math.sqrt(
				Math.pow(p1.y*p2.z - p1.z*p2.y, 2) +	
				Math.pow(p1.x*p2.z - p1.z*p2.x, 2) +	
				Math.pow(p1.x*p2.y - p1.y*p2.x, 2)	
				);*/
//			pair.myDiscrepancy = Math.max(Math.max(
//					Math.abs(p1.y*p2.z - p1.z*p2.y), 
//					Math.abs(p1.x*p2.z - p1.z*p2.x)), 
//					Math.abs(p1.x*p2.y - p1.y*p2.x));
			pair.myDiscrepancy = Math.abs(p1.y/p1.z - p2.y/p2.z);
//			pair.myDiscrepancy = Math.abs(p1.y*p2.z - p1.z*p2.y);
			stat.addValue(pair.myDiscrepancy);
		}
		stat.stop();
		System.out.println("MyDiscrepancy statistics:");
		System.out.println(stat.toString(Statistics.CStatMinMax));
	}
	
	public static void main(String[] args) throws Exception {
//		Data data = generateData();
		Data data = readTestData();
		
		System.out.println("Point pairs " + data.pointPairs.size());
		Utils.dumpPoints(data.pointPairs);
		
		for (int i = 0; i < data.cameras.length; i++) {
			MyCamera camera = data.cameras[i];
			camera.scaleZ = camera.realFocalDistance;
			printCameraAngles(camera);
			camera.rx = 0;
			camera.ry = 0;
			camera.rz = 0;
			camera.scaleZ = 3;
//			camera.scaleZ = camera.realFocalDistance;
		}
	
		ImageToWorldTransformLearner learner = new ImageToWorldTransformLearner(data.cameras[0], data.cameras, data.pointPairs);
		boolean failed = true;
		if (learner.calculateDiscrepancy()) {
			for (int iter = 0; iter < 50; iter++) {
				if (!learner.calculateParameters())
					break;
				if (!learner.calculateDiscrepancy())
					break;
				double mse = learner.getMedianSquareError();
				System.out.println("ITER=" + iter + " MSE=" + mse);
//				learner.tr.cameras[0].camera2real.printM("A");
				if (mse < 0.01) {
					failed = false;
					break;
				}
			}
		}
		
		System.out.println(failed ? "\n\n*** FAILED\n\n" : "\n\n*** SUCESS\n\n"); 
/*
		for (MyCamera camera : data.cameras) {
			camera.scaleZ /= camera.scale;
			camera.scale = 1.0;
		}		
		
		failed = true;
		
		if (learner.calculateDiscrepancy()) {
			for (int iter = 0; iter < 50; iter++) {
				if (!learner.calculateParameters())
					break;
				if (!learner.calculateDiscrepancy())
					break;
				double mse = learner.getMedianSquareError();
				System.out.println("ITER=" + iter + " MSE=" + mse);
//				learner.tr.cameras[0].camera2real.printM("A");
				if (mse < 0.01) {
					failed = false;
					break;
				}
			}
		}

		System.out.println(failed ? "\n\n*** FAILED\n\n" : "\n\n*** SUCESS\n\n"); 
		*/
		System.out.println("FINAL");
		for (MyCamera camera : data.cameras) {
			printCameraAngles(camera);
			System.out.println(1/camera.scale);
		}
		calculateDiscrepancy(data.pointPairs, learner.tr);
		System.out.println(data.pointPairs.size());
		System.out.println("Done.");
	}
}
