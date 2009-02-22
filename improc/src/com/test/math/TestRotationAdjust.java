package com.test.math;

import java.util.ArrayList;
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
		public Matrix real2camera;
		public double angles[];
		public double realFocalDistance;
		
		public Matrix camera2real;
		public Statistics stat;
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
		for (int x = 0; x < 3; x++)
			for (int y = 0; y < 2; y++)
				for (int z = 0; z < 2; z++) {
					MyPoint3D p = new MyPoint3D();
					p.p.setItem(0, 0, x);
					p.p.setItem(0, 1, y);
					p.p.setItem(0, 2, z);
					result.add(p);
				}					
		return result;
	}

	/**
	 * Return a rotation matrix R=mx*my*mz 
	 */
	public static Matrix makeAnglesXYZ(double rx, double ry, double rz) {
		/*
		 *  mx            my            mz
		 *  1   0   0     cb  0  sb     cc -sc  0
		 *  0  ca -sa      0  1  0      sc  cc  0
		 *  0  sa  ca    -sb  0  cb      0   0  1
		 */
		double sa = Math.sin(rx);
		double ca = Math.cos(rx);
		
		double sb = Math.sin(ry);
		double cb = Math.cos(ry);

		double sc = Math.sin(rz);
		double cc = Math.cos(rz);

		/*
		 * mx*my
		 * cb				0				-sb
		 * -sa*sb			ca				-sa*cb
		 * ca*sb			sa				ca*cb
		 * 
		 * (mx*my)*mz
		 * cc*cb			-cb*sc			-sb
		 * ca*sc-sa*sb*cc	ca*cc+sa*sb*sc	-sa*cb
		 * sa*sc+ca*sb*cc	sa*cc-ca*sb*sc	ca*cb
		 */
		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, cc*cb);
		r.setItem(1, 0, -cb*sc);
		r.setItem(2, 0, -sb);
		r.setItem(0, 1, ca*sc-sa*sb*cc);
		r.setItem(1, 1, ca*cc+sa*sb*sc);
		r.setItem(2, 1, -sa*cb);
		r.setItem(0, 2, sa*sc+ca*sb*cc);
		r.setItem(1, 2, sa*cc-ca*sb*sc);
		r.setItem(2, 2, ca*cb);
		return r;
	}

	public static void main(String[] args) {
		makeAnglesXYZ(2, 3, 4);
	}
	
	public static MyCamera[] generateCameras(MyPoint3D cameraOrigin, double cameraAngles[][]) {
		MyCamera[] result = new MyCamera[cameraAngles.length];
		int imageId = 1;
		for (int i = 0; i < cameraAngles.length; i++) {
			double[] data = cameraAngles[i];
			MyCamera c = new MyCamera();
			c.imageId = imageId++;
			c.realOrigin = cameraOrigin;
			c.real2camera = makeAnglesXYZ(data[0], data[1], data[2]);
			c.angles = data;
			c.realFocalDistance = data[3];
			c.camera2real = c.real2camera.makeCopy();
			c.camera2real.inverse();
			result[i] = c;
		}		
		return result;
	}
	
	public static List<MyPointPair> generatePointPairs(MyCamera cameras[], List<MyPoint3D> realPoints) {
		ArrayList<MyPointPair> result = new ArrayList<MyPointPair>();
		Matrix tmp1 = new Matrix(1, 3);
		Matrix tmp2 = new Matrix(1, 3);
		
		for (int cameraIndex = 0; cameraIndex < cameras.length; cameraIndex++) {
			MyCamera src = cameras[cameraIndex];
			MyCamera dest = cameras[(cameraIndex + 1) % cameras.length];

			for (MyPoint3D p : realPoints) {
				MyPointPair pp = new MyPointPair();
				pp.realPoint = p;
		
				p.p.mSub(src.realOrigin.p, tmp1);
				src.real2camera.mMul(tmp1, tmp2);
				double z = tmp2.getItem(0, 2);
				if (z <= 0.0)
					continue;
				double scale = src.realFocalDistance / z;
				pp.srcPoint = new MyImagePoint();
				pp.srcPoint.camera = src;
				pp.srcPoint.x = tmp2.getItem(0, 0) * scale;
				pp.srcPoint.y = tmp2.getItem(0, 1) * scale;

				p.p.mSub(dest.realOrigin.p, tmp1);
				dest.real2camera.mMul(tmp1, tmp2);
				z = tmp2.getItem(0, 2);
				if (z <= 0.0)
					continue;
				scale = dest.realFocalDistance / z;
				pp.destPoint = new MyImagePoint();
				pp.destPoint.camera = dest;
				pp.destPoint.x = tmp2.getItem(0, 0) * scale;
				pp.destPoint.y = tmp2.getItem(0, 1) * scale;

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
			return images.length * 9;
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
		
		MyCamera[] images;
		
		MyCamera originImage;
		
		double averageFocalDistance;
		
		public int indexOf(MyCamera image) {
			for (int i = 0; i < images.length; i++)
				if (images[i] == image)
					return i;
			return -1;
		}
		
		public ImageToWorldTransformer(MyCamera originImage, MyCamera[] images) {
			this.originImage = originImage;
			int count = images.length;
			this.images = new MyCamera[count - 1];
			int index = 0;
			for (int i = 0; i < count; i++) {
				if (images[i] != originImage) {
					this.images[index++] = images[i];
				}
			}
		}
	}
	
	public static class ImageToWorldTransformLearner extends BaseTransformLearner<MyImagePoint, MyImagePoint> {

		ImageToWorldTransformer tr;
		
		LeastSquaresAdjust lsa;
		
		public ImageToWorldTransformLearner(MyCamera originImage, MyCamera[] images,
				Iterable<MyPointPair> pointsPairList) {
			super(new ImageToWorldTransformer(originImage, images), pointsPairList);
			tr = (ImageToWorldTransformer) transformer;
			this.lsa = new LeastSquaresAdjust(transformer.getNumberOfCoefsPerCoordinate(), 1);		
		}

		private void setCoef(Matrix coefs, int atIndex,	MyImagePoint p, double transformedCoord) {
			coefs.setItem(atIndex + 0, 0, p.x * transformedCoord);
			coefs.setItem(atIndex + 1, 0, p.y * transformedCoord);
			coefs.setItem(atIndex + 2, 0, p.camera.realFocalDistance * transformedCoord);
		}

		public boolean calculateOne() {
			int goodCount = computeWeights();
			if (goodCount < lsa.getRequiredPoints())
				return false;
			Matrix coefs = new Matrix(tr.getNumberOfCoefsPerCoordinate(), 1);			

			tr.originImage.camera2real.makeE();
			tr.averageFocalDistance = tr.originImage.realFocalDistance;
			
			Matrix t1 = new Matrix(1, 3);
			Matrix t2 = new Matrix(1, 3);
			MyImagePoint tmp1 = new MyImagePoint();
			MyImagePoint tmp2 = new MyImagePoint();
			lsa.clear();
			System.out.println("COEFS=");
			for (Map.Entry<MyImagePoint, MyImagePoint> item : items) {
				if (isBad(item))
					continue;
				
				double computedWeight = getComputedWeight(item);
				MyImagePoint source = item.getKey();
				MyImagePoint dest = item.getValue();
				
				int srcIndex = tr.indexOf(source.camera) * 9;
				int destIndex = tr.indexOf(dest.camera) * 9;
				
				coefs.make0();
				
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
					System.out.println(coefs.toOneLineString());
				}
			}

			System.out.println("NM=");
			System.out.println(lsa.getNm());
			if (!lsa.calculate()) 
				return false;

			// Build transformer
			Matrix u = lsa.getUnknown();
			System.out.println("U=");
			System.out.println(u.toString());

			for (int curImage = 0; curImage < tr.images.length; curImage++) {
				MyCamera image = tr.images[curImage];
				int index = curImage * 9;
				image.camera2real.setItem(0, 0, u.getItem(0, index + 0) + image.camera2real.getItem(0, 0));
				image.camera2real.setItem(1, 0, u.getItem(0, index + 1) + image.camera2real.getItem(1, 0));
				image.camera2real.setItem(2, 0, u.getItem(0, index + 2) + image.camera2real.getItem(2, 0));

				image.camera2real.setItem(0, 1, u.getItem(0, index + 3) + image.camera2real.getItem(0, 1));
				image.camera2real.setItem(1, 1, u.getItem(0, index + 4) + image.camera2real.getItem(1, 1));
				image.camera2real.setItem(2, 1, u.getItem(0, index + 5) + image.camera2real.getItem(2, 1));

				image.camera2real.setItem(0, 2, u.getItem(0, index + 6) + image.camera2real.getItem(0, 2));
				image.camera2real.setItem(1, 2, u.getItem(0, index + 7) + image.camera2real.getItem(1, 2));
				image.camera2real.setItem(2, 2, u.getItem(0, index + 8) + image.camera2real.getItem(2, 2));
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
	
	public static void calculateDiscrepancy(MyCamera[] images, 
			List<MyPointPair> pointPairs, ImageToWorldTransformer tr) {
		MyImagePoint p1 = new MyImagePoint();
		MyImagePoint p2 = new MyImagePoint();
		
		for (MyCamera image : images) {
			image.stat = new Statistics();
			image.stat.start();
		}
		
		Statistics stat = new Statistics();
		stat.start();
		for (MyPointPair pair : pointPairs) {
/*			p1.x = pair.srcPoint.x;
			p1.y = pair.srcPoint.y;
			p1.z = pair.srcPoint.camera.realFocalDistance;
			p2.x = pair.destPoint.x;
			p2.y = pair.destPoint.y;
			p2.z = pair.destPoint.camera.realFocalDistance;
*/			
			tr.transform(pair.srcPoint, p1);
			tr.transform(pair.destPoint, p2);

			///////////////
			pair.myDiscrepancy = Math.sqrt(
				Math.pow(p1.y*p2.z - p2.y*p1.z, 2) +	
				Math.pow(p1.x*p2.z - p2.x*p1.z, 2) +	
				Math.pow(p1.x*p2.y - p2.x*p1.y, 2)	
				);
			stat.addValue(pair.myDiscrepancy);
			pair.srcPoint.camera.stat.addValue(pair.myDiscrepancy);
			pair.destPoint.camera.stat.addValue(pair.myDiscrepancy);
		}
		stat.stop();
		System.out.println("MyDiscrepancy statistics:");
		System.out.println(stat.toString(Statistics.CStatMinMax));

		for (MyCamera image : images) {
			image.stat.stop();
			System.out.println();
			System.out.println("Image " + image.imageId);
			System.out.println(image.stat.toString(Statistics.CStatMinMax));
		}
	}
	
	static double cameraAngles[][] = new double[][] {
		// rx, ry, rz, f
//		{ 20 * MathUtil.deg2rad, 20 * MathUtil.deg2rad,  0 * MathUtil.deg2rad, 10},
		{  0 * MathUtil.deg2rad,  0 * MathUtil.deg2rad,  0 * MathUtil.deg2rad, 10},
		{-20 * MathUtil.deg2rad,  10 * MathUtil.deg2rad,-20 * MathUtil.deg2rad, 11},
		{-20 * MathUtil.deg2rad,-20 * MathUtil.deg2rad, 20 * MathUtil.deg2rad, 12},
//		{  0 * MathUtil.deg2rad,  0 * MathUtil.deg2rad,-20 * MathUtil.deg2rad, 11},
	};

	public static void main2(String[] args) {
		List<MyPoint3D> realPoints = generateRealPoints();
		
		MyPoint3D cameraOrigin = new MyPoint3D();
		cameraOrigin.p.setItem(0, 0, -5);
		cameraOrigin.p.setItem(0, 1, -5);
		cameraOrigin.p.setItem(0, 2, -5);
		MyCamera cameras[] = generateCameras(cameraOrigin, cameraAngles);
		
		List<MyPointPair> pointPairs = generatePointPairs(cameras, realPoints);
		
		ImageToWorldTransformLearner learner = new ImageToWorldTransformLearner(cameras[0], cameras, pointPairs);
		if (!learner.calculateOne()) {
			System.out.println("FAILED");
			return;
		}
		
		for (MyCamera image : cameras) {
			System.out.println("Image " + image.imageId);
			System.out.println(image.camera2real.toString());
			System.out.println();
		}
		calculateDiscrepancy(cameras, pointPairs, learner.tr);
		System.out.println(pointPairs.size());
		System.out.println("Done.");
	}
}
