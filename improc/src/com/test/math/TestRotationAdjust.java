package com.test.math;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.slavi.math.MathUtil;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.BaseTransformLearner;
import com.slavi.math.transform.BaseTransformer;

public class TestRotationAdjust {

	public static class MyPoint3D {
		public final Matrix p = new Matrix(1, 3);
	}
	
	public static class MyCamera {
		public int imageId;
		public MyPoint3D realOrigin;
		public Matrix realRot;
		public double angles[];
		public double realFocalDistance;
		
		public Matrix coefs;
		public double calcF;
	}
	
	public static class MyImagePoint {
		public MyCamera camera;
		public double x, y, z;
	}
	
	public static class MyPointPair implements Map.Entry<MyImagePoint, MyImagePoint> {
		public MyImagePoint srcPoint, destPoint;
		public MyPoint3D realPoint;
		
		public double discrepancy = 0.0;
		public double weight = 1.0;
		public boolean isBad = false;
		
		public MyImagePoint getKey() {
			return srcPoint;
		}
		public MyImagePoint getValue() {
			return destPoint;
		}
		public MyImagePoint setValue(MyImagePoint value) {
			throw new UnsupportedOperationException();
		}
		public double myDiscrepancy;
	}
	
	public static List<MyPoint3D> generateRealPoints() {
		ArrayList<MyPoint3D> result = new ArrayList<MyPoint3D>();
		for (int x = 0; x < 10; x++)
			for (int y = 0; y < 10; y++)
				for (int z = 0; z < 10; z++) {
					MyPoint3D p = new MyPoint3D();
					p.p.setItem(0, 0, x);
					p.p.setItem(0, 1, y);
					p.p.setItem(0, 2, z);
					result.add(p);
				}					
		return result;
	}
	
	public static List<MyCamera> generateCameras(MyPoint3D cameraOrigin, double cameraAngles[][]) {
		ArrayList<MyCamera> result = new ArrayList<MyCamera>();
		int imageId = 1;
		for (double[] i : cameraAngles) {
			MyCamera c = new MyCamera();
			c.imageId = imageId++;
			c.realOrigin = cameraOrigin;
			c.realRot = MathUtil.makeAngles(i[0], i[1], i[2], false);
			c.angles = i;
			c.realFocalDistance = 1000; //i[3];
			c.coefs = c.realRot.makeCopy();
			result.add(c);
		}		
		return result;
	}
	
	public static List<MyPointPair> generatePointPairs(List<MyCamera> cameras, List<MyPoint3D> realPoints) {
		ArrayList<MyPointPair> result = new ArrayList<MyPointPair>();
		MyCamera first = null;
		MyCamera src = null;
		MyCamera dest = null;
		Matrix tmp1 = new Matrix(1, 3);
		Matrix tmp2 = new Matrix(1, 3);
		
		Iterator<MyCamera> iter = cameras.iterator();
		while (true) {
			if (iter.hasNext()) {
				src = dest;
				dest = iter.next();
				if (first == null) {
					first = dest;
					continue;
				}
			} else {
				if (first == null)
					break;
				dest = first;
				first = null;
			}
			
			for (MyPoint3D p : realPoints) {
				MyPointPair pp = new MyPointPair();
				pp.realPoint = p;
		
				p.p.mSub(src.realOrigin.p, tmp1);
				src.realRot.mMul(tmp1, tmp2);
				double z = tmp1.getItem(0, 2);
				if (z <= 0.0)
					continue;
				double scale = src.realFocalDistance / z;
				pp.srcPoint = new MyImagePoint();
				pp.srcPoint.camera = src;
				pp.srcPoint.x = tmp1.getItem(0, 0) * scale;
				pp.srcPoint.y = tmp1.getItem(0, 1) * scale;

				p.p.mSub(dest.realOrigin.p, tmp1);
				src.realRot.mMul(tmp1, tmp2);
				z = tmp1.getItem(0, 2);
				if (z <= 0.0)
					continue;
				scale = dest.realFocalDistance / z;
				pp.destPoint = new MyImagePoint();
				pp.destPoint.camera = dest;
				pp.destPoint.x = tmp1.getItem(0, 0) * scale;
				pp.destPoint.y = tmp1.getItem(0, 1) * scale;

				result.add(pp);
			}
		}
		return result;
	}
	
	public static class ImageToWorldTransformer extends BaseTransformer<MyImagePoint, MyImagePoint> {
		public int getInputSize() {
			return 2;
		}

		public int getNumberOfCoefsPerCoordinate() {
			return images.size() * 9;
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
				source.x * source.camera.coefs.getItem(0, 0) +
				source.y * source.camera.coefs.getItem(1, 0) +
				source.camera.realFocalDistance * source.camera.coefs.getItem(2, 0) / averageFocalDistance;
			dest.y = 
				source.x * source.camera.coefs.getItem(0, 1) +
				source.y * source.camera.coefs.getItem(1, 1) +
				source.camera.realFocalDistance * source.camera.coefs.getItem(2, 1) / averageFocalDistance;
			dest.z =  averageFocalDistance * (
				source.x * source.camera.coefs.getItem(0, 2) +
				source.y * source.camera.coefs.getItem(1, 2) +
				source.camera.realFocalDistance * source.camera.coefs.getItem(2, 2) / averageFocalDistance);
		}
		
		ArrayList<MyCamera> images = new ArrayList<MyCamera>();
		
		MyCamera originImage;
		
		double averageFocalDistance;
		
		public ImageToWorldTransformer(MyCamera originImage, List<MyCamera> images) {
			this.originImage = originImage;
			this.images.addAll(images);
			this.images.remove(originImage);
		}
	}
	
	public static class ImageToWorldTransformLearner extends BaseTransformLearner<MyImagePoint, MyImagePoint> {

		ImageToWorldTransformer tr;
		
		LeastSquaresAdjust lsa;
		
		public ImageToWorldTransformLearner(MyCamera originImage, List<MyCamera> images,
				Iterable<MyPointPair> pointsPairList) {
			super(new ImageToWorldTransformer(originImage, images), pointsPairList);
			tr = (ImageToWorldTransformer) transformer;
			this.lsa = new LeastSquaresAdjust(transformer.getNumberOfCoefsPerCoordinate(), 1);		
		}

		private void setCoef(Matrix coefs, int atIndex,
				int atRow, MyImagePoint source, MyImagePoint dest, double sign) {
			double d1 = sign * source.x * (  
				source.camera.realRot.getItem(0, atRow) * dest.x +  
				source.camera.realRot.getItem(1, atRow) * dest.y +  
				source.camera.realRot.getItem(2, atRow) * dest.camera.realFocalDistance / tr.averageFocalDistance);  
			double e1 = sign * source.y * (  
				source.camera.realRot.getItem(0, atRow) * dest.x +  
				source.camera.realRot.getItem(1, atRow) * dest.y +  
				source.camera.realRot.getItem(2, atRow) * dest.camera.realFocalDistance / tr.averageFocalDistance);  
			double f1 = sign * source.camera.realFocalDistance * (  
				source.camera.realRot.getItem(0, atRow) * dest.x +  
				source.camera.realRot.getItem(1, atRow) * dest.y +  
				source.camera.realRot.getItem(2, atRow) * dest.camera.realFocalDistance / tr.averageFocalDistance);  
			
			coefs.setItem(atIndex + 0, 0, d1);
			coefs.setItem(atIndex + 1, 0, e1);
			coefs.setItem(atIndex + 2, 0, f1);
		}
		
		public boolean calculateOne() {
			int goodCount = computeWeights();
			if (goodCount < lsa.getRequiredPoints())
				return false;
			Matrix coefs = new Matrix(transformer.getNumberOfCoefsPerCoordinate(), 1);			

			tr.originImage.coefs.makeE();
			tr.averageFocalDistance = tr.originImage.realFocalDistance;
			
			Matrix p1 = new Matrix(1, 3);
			Matrix p2 = new Matrix(1, 3);
			Matrix t1 = new Matrix(1, 3);
			Matrix t2 = new Matrix(1, 3);
			lsa.clear();
			for (Map.Entry<MyImagePoint, MyImagePoint> item : items) {
				if (isBad(item))
					continue;
				
				double computedWeight = getComputedWeight(item);
				MyImagePoint source = item.getKey();
				MyImagePoint dest = item.getValue();
				
				int srcIndex = tr.images.indexOf(source.camera) * 9;
				int destIndex = tr.images.indexOf(dest.camera) * 9;
				
				coefs.make0();
				
				p1.setItem(0, 0, source.x);
				p1.setItem(0, 1, source.y);
				p1.setItem(0, 2, source.camera.realFocalDistance);
				source.camera.realRot.mMul(p1, t1);
				
				p2.setItem(0, 0, dest.x);
				p2.setItem(0, 1, dest.y);
				p2.setItem(0, 2, dest.camera.realFocalDistance);
				dest.camera.realRot.mMul(p2, t2);
				
				for (int curCoord = 0; curCoord < 3; curCoord++) {
					int c1 = (curCoord + 1) % 3;
					int c2 = (curCoord + 2) % 3;
					
					double L = 
						t1.getItem(0, c1) * t2.getItem(0, c2) -
						t2.getItem(0, c1) * t1.getItem(0, c2);
					if (srcIndex >= 0) {
						setCoef(coefs, srcIndex + c1 * 3, c2, source, dest,  1.0);
						setCoef(coefs, srcIndex + c2 * 3, c1, source, dest, -1.0);
						lsa.addMeasurement(coefs, computedWeight, L, 0);
					}
					if (destIndex >= 0) {
						setCoef(coefs, destIndex + c1 * 3, c2, dest, source, -1.0);
						setCoef(coefs, destIndex + c2 * 3, c1, dest, source,  1.0);
						lsa.addMeasurement(coefs, computedWeight, L, 0);
					}
				}
			}
			
			if (!lsa.calculate()) 
				return false;

			// Build transformer
			Matrix u = lsa.getUnknown(); 
			for (int curImage = 0; curImage < tr.images.size(); curImage++) {
				MyCamera image = tr.images.get(curImage);
				int index = curImage * 9;
				image.coefs.setItem(0, 0, u.getItem(0, index + 0));
				image.coefs.setItem(1, 0, u.getItem(0, index + 1));
				image.coefs.setItem(2, 0, u.getItem(0, index + 2));

				image.coefs.setItem(0, 1, u.getItem(0, index + 3));
				image.coefs.setItem(1, 1, u.getItem(0, index + 4));
				image.coefs.setItem(2, 1, u.getItem(0, index + 5));

				image.coefs.setItem(0, 2, u.getItem(0, index + 6));
				image.coefs.setItem(1, 2, u.getItem(0, index + 7));
				image.coefs.setItem(2, 2, u.getItem(0, index + 8));
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
	
	public static void calculateDiscrepancy(List<MyPointPair> pointPairs, ImageToWorldTransformer tr) {
		MyImagePoint p1 = new MyImagePoint();
		MyImagePoint p2 = new MyImagePoint();
		
		Statistics stat = new Statistics();
		stat.start();
		for (MyPointPair pair : pointPairs) {
			tr.transform(pair.srcPoint, p1);
			tr.transform(pair.destPoint, p2);
			pair.myDiscrepancy = Math.sqrt(
				Math.pow(p1.x - p2.x, 2) +
				Math.pow(p1.y - p2.y, 2)  
//				Math.pow(p1.z - p2.z, 2)
				);
			stat.addValue(pair.myDiscrepancy);
		}
		stat.stop();
		System.out.println("MyDiscrepancy statistics:");
		System.out.println(stat.toString(Statistics.CStatMinMax));
	}
	
	static double cameraAngles[][] = new double[][] {
		// rx, ry, rz, f
		{ 20 * MathUtil.deg2rad, 20 * MathUtil.deg2rad,  0 * MathUtil.deg2rad, 10},
		{-20 * MathUtil.deg2rad,  0 * MathUtil.deg2rad,-20 * MathUtil.deg2rad, 11},
		{-20 * MathUtil.deg2rad,-20 * MathUtil.deg2rad, 20 * MathUtil.deg2rad, 12},
		{  0 * MathUtil.deg2rad,  0 * MathUtil.deg2rad,-20 * MathUtil.deg2rad, 11},
	};

	public static void main(String[] args) {
		List<MyPoint3D> realPoints = generateRealPoints();
		
		MyPoint3D cameraOrigin = new MyPoint3D();
		cameraOrigin.p.setItem(0, 0, 5);
		cameraOrigin.p.setItem(0, 1, 5);
		cameraOrigin.p.setItem(0, 2, -5);
		List<MyCamera> cameras = generateCameras(cameraOrigin, cameraAngles);
		
		List<MyPointPair> pointPairs = generatePointPairs(cameras, realPoints);
		
		ImageToWorldTransformLearner learner = new ImageToWorldTransformLearner(cameras.get(0), cameras, pointPairs);
		learner.calculateOne();
		
		for (MyCamera image : cameras) {
			System.out.println("Image " + image.imageId);
			System.out.println(image.coefs.toString());
			System.out.println();
		}
		calculateDiscrepancy(pointPairs, learner.tr);
		System.out.println("Done.");
	}
}
