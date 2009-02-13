package com.test.math;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.BaseTransformLearner;
import com.slavi.math.transform.BaseTransformer;

public class TestRotationAdjust {

	public static class MyPoint3D {
		public final Matrix p = new Matrix(1, 3);
	}
	
	public static class MyCamera {
		public int imageId;
		public MyPoint3D origin;
		public Matrix rot;
		public double angles[];
		public double focalDistance;
	}
	
	public static class MyImagePoint {
		public MyCamera camera;
		public double x, y;
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
			c.origin = cameraOrigin;
			c.rot = MathUtil.makeAngles(i[0], i[1], i[2], false);
			c.angles = i;
			c.focalDistance = i[3];
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
		
				p.p.mSub(src.origin.p, tmp1);
				src.rot.mMul(tmp1, tmp2);
				double z = tmp1.getItem(0, 2);
				if (z <= 0.0)
					continue;
				double scale = src.focalDistance / z;
				pp.srcPoint = new MyImagePoint();
				pp.srcPoint.camera = src;
				pp.srcPoint.x = tmp1.getItem(0, 0) * scale;
				pp.srcPoint.y = tmp1.getItem(0, 1) * scale;

				p.p.mSub(dest.origin.p, tmp1);
				src.rot.mMul(tmp1, tmp2);
				z = tmp1.getItem(0, 2);
				if (z <= 0.0)
					continue;
				scale = dest.focalDistance / z;
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
			return images.size() * 4;
		}

		public int getOutputSize() {
			return 2;
		}

		public double getSourceCoord(MyImagePoint item, int coordIndex) {
			switch (coordIndex) {
			case 0: return item.x;
			case 1: return item.y;
			}
			throw new IllegalArgumentException();
		}

		public double getTargetCoord(MyImagePoint item, int coordIndex) {
			switch (coordIndex) {
			case 0: return item.x;
			case 1: return item.y;
			}
			throw new IllegalArgumentException();
		}

		public void setSourceCoord(MyImagePoint item, int coordIndex, double value) {
			switch (coordIndex) {
			case 0: item.x = value; break;
			case 1: item.y = value; break;
			}
			throw new IllegalArgumentException();
		}

		public void setTargetCoord(MyImagePoint item, int coordIndex, double value) {
			switch (coordIndex) {
			case 0: item.x = value; break;
			case 1: item.y = value; break;
			}
			throw new IllegalArgumentException();
		}

		public void transform(MyImagePoint source, MyImagePoint dest) {
			// ???
		}
		
		ArrayList<MyCamera> images = new ArrayList<MyCamera>();
		
		MyCamera originImage;
		
		public ImageToWorldTransformer(MyCamera originImage, List<MyCamera> images) {
			this.originImage = originImage;
			this.images.addAll(images);
			this.images.remove(originImage);
		}
	}
	
	public static class ImageToWorldTransformLearner extends BaseTransformLearner<MyImagePoint, MyImagePoint> {

		ImageToWorldTransformer tr;
		
		public ImageToWorldTransformLearner(MyCamera originImage, List<MyCamera> images,
				Iterable<Map.Entry<MyImagePoint, MyImagePoint>> pointsPairList) {
			super(new ImageToWorldTransformer(originImage, images), pointsPairList);
			tr = (ImageToWorldTransformer) transformer;
		}

		public boolean calculateOne() {
			int goodCount = computeWeights();
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
	}
	
	
	public static void calculateUsingOriginImage(MyCamera image, List<MyCamera> cameras, List<MyPointPair> pointPairs) {
		int camerasCount = cameras.size();
		int pointPairsCount = pointPairs.size();
		
		
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
		System.out.println(pointPairs.size());
	}
}
