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
//			dest.x = 
//				source.x * source.camera.coefs.getItem(0, 0) +
//				source.y * source.camera.coefs.getItem(1, 0) +
//				source.camera.realFocalDistance * source.camera.coefs.getItem(2, 0) / averageFocalDistance;
//			dest.y = 
//				source.x * source.camera.coefs.getItem(0, 1) +
//				source.y * source.camera.coefs.getItem(1, 1) +
//				source.camera.realFocalDistance * source.camera.coefs.getItem(2, 1) / averageFocalDistance;
//			dest.z =  averageFocalDistance * (
//				source.x * source.camera.coefs.getItem(0, 2) +
//				source.y * source.camera.coefs.getItem(1, 2) +
//				source.camera.realFocalDistance * source.camera.coefs.getItem(2, 2) / averageFocalDistance);
			Matrix s = new Matrix(1, 3);
			Matrix d = new Matrix(1, 3);
			s.setItem(0, 0, source.x);
			s.setItem(0, 1, source.y);
			s.setItem(0, 2, source.camera.realFocalDistance);
			source.camera.coefs.mMul(s, d);
			dest.x = d.getItem(0, 0);
			dest.y = d.getItem(0, 1);
			dest.z = d.getItem(0, 2);
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

/*		private void setCoef(Matrix coefs, int atIndex,
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
*/		
		
		/**
		 * Maths:
		 * КС - Координатна Система
		 * 
		 * Ротация на КС на снимка 1 към глобална КС
		 * M1 = [a1 b1 c1; d1 e1 f1; g1 h1 i1]
		 * 
		 * Координати на проекцията на точка P в КС на снимка 1, 
		 * f1 - фокусно разстояние на камера 1
		 * P1 = [X1; Y1; F1]
		 * 
		 * Координати на проекциите на точка P в снимки 1 и 2 
		 * съответно P1/P2 и трансформиране на точки P1/P2 в глобална КС
		 * P'1 = M1 * P1
		 * P'2 = M2 * P2
		 * P'1 <> P'2
		 * 
		 * Колинеарност на P'1 и P'2 (векторното произведение = 0)
		 * F(...) = P'1 x P'2 = 0
		 * F(...): F[a1, b1,..., i1, a2,..., i?, Pk(Xm), Pk(Ym), Fm, Pk(Xn), Pk(Yn), Fn]
		 * F(...):
		 * fx: P'1(y) * P'2(z) - P'1(z) * P'2(y) = 0
		 * fy: P'1(x) * P'2(z) - P'1(z) * P'2(x) = 0
		 * fz: P'1(x) * P'2(y) - P'1(y) * P'2(x) = 0
		 * 
		 * fx: (d1*X1 + e1*Y1 + f1*F1) * (g2*X2 + h2*Y2 + i2*F2) - (g1*X1 + g1*Y1 + i1*F1) * (d2*X2 + e2*Y2 + f2*F2)
		 * fy: (a1*X1 + b1*Y1 + c1*F1) * (g2*X2 + h2*Y2 + i2*F2) - (g1*X1 + g1*Y1 + i1*F1) * (a2*X2 + b2*Y2 + c2*F2)
		 * fz: (a1*X1 + b1*Y1 + c1*F1) * (d2*X2 + e2*Y2 + f2*F2) - (d1*X1 + e1*Y1 + f1*F1) * (a2*X2 + b2*Y2 + c2*F2)
		 * 
		 * Измервания Rk - съответсващи проекции на една и съща точка 
		 * в две (съседни) снимки m и n
		 * Rk: Pm <-> Pn
		 * така за всяко Lk съответстват F(...) функции на M1, M2,...
		 * F(Lk) = F(M1, M2, ...) 
		 * F(Lk) = F(a1, b1,..., i1, a2,..., i?)
		 * 
		 * Изравнени стойности (на параметрите на ротация)
		 * M'1 = [a'1 b'1 c'1; d'1 e'1 f'1; g'1 h'1 i'1]
		 * a'1 = a1 + δ(a1)
		 * b'1 = b1 + δ(b1)
		 * ...
		 * R'k -> изравнена стойност на измерена величина
		 * Fk(a'1, b'1,..., i'1, a'2,..., i'?) = 0
		 * Fk(a1+δ(a1),..., i1+δ(i1),..., i?+δ(i?)) = 0
		 * F(a1,..., i1,..., i?) + δ(a1)*d(F)/d(a1) + δ(b1)*d(F)/d(b1) + ... + δ(i?)*d(F)/d(i?) = 0
		 *  
		 * Неизвестни
		 * U[0] = δ(a1)
		 * U[1] = δ(b1)
		 * ...
		 * U[8] = δ(i1)
		 * U[9] = δ(a2)
		 * ...
		 * U[17]= δ(i2)
		 * ...
		 * U[?] = δ(?)
		 * 
		 * Частни производни на F(...) или на fx, fy, fz
		 * A[0][0] = d(fx)/d(a1) = 0
		 * A[0][1] = d(fx)/d(b1) = 0
		 * A[0][2] = d(fx)/d(c1) = 0
		 * 
		 * A[0][3] = d(fx)/d(d1) = X1 * (g2*X2 + h2*Y2 + i2*F2)
		 * A[0][4] = d(fx)/d(e1) = Y1 * (g2*X2 + h2*Y2 + i2*F2)
		 * A[0][5] = d(fx)/d(f1) = F1 * (g2*X2 + h2*Y2 + i2*F2)
		 * 
		 * A[0][6] = d(fx)/d(g1) = -X1 * (d2*X2 + e2*Y2 + f2*F2)
		 * A[0][7] = d(fx)/d(h1) = -Y1 * (d2*X2 + e2*Y2 + f2*F2)
		 * A[0][8] = d(fx)/d(i1) = -F1 * (d2*X2 + e2*Y2 + f2*F2)
		 *
		 * A[0][9] = d(fx)/d(a2) = 0
		 * A[0][10]= d(fx)/d(b2) = 0
		 * A[0][11]= d(fx)/d(c2) = 0
		 * 
		 * A[0][12]= d(fx)/d(d2) = -X2 * (g1*X1 + h1*Y1 + i1*F1)
		 * A[0][13]= d(fx)/d(e2) = -Y2 * (g1*X1 + h1*Y1 + i1*F1)
		 * A[0][14]= d(fx)/d(f2) = -F2 * (g1*X1 + h1*Y1 + i1*F1)
		 * 
		 * A[0][15]= d(fx)/d(g2) = X2 * (d1*X1 + e1*Y1 + f1*F1)
		 * A[0][16]= d(fx)/d(h2) = Y2 * (d1*X1 + e1*Y1 + f1*F1)
		 * A[0][17]= d(fx)/d(i2) = F2 * (d1*X1 + e1*Y1 + f1*F1)
		 * ...
		 * A[0][?] = d(fx)/d(?) = 0
		 * A[1][0] = d(fy)/d(a1) = X1 * (g2*X2 + h2*Y2 + i2*F2)
		 * A[1][1] = d(fy)/d(b1) = Y1 * (g2*X2 + h2*Y2 + i2*F2)
		 * A[1][2] = d(fy)/d(c1) = F1 * (g2*X2 + h2*Y2 + i2*F2)
		 * ...
		 * A[2][?] = d(fz)/d(?) = ...
		 * 
		 * Уравнения на поправките v(Xi) v(Yi) v(Fi)
		 * v(Xi) = fx(a1,..., i1,..., i?) + δ(a1)*d(F)/d(a1) + δ(b1)*d(F)/d(b1) + ... + δ(i?)*d(F)/d(i?)
		 *  
		 * v(Xi) = fx(a1,..., i1,..., i?) + U[0]*A[0][0] + U[1]A[0][1] + ... + U[?]*A[0][?] 
		 * v(Yi) = fy(a1,..., i1,..., i?) + U[0]*A[1][0] + U[1]A[1][1] + ... + U[?]*A[1][?] 
		 * v(Fi) = fz(a1,..., i1,..., i?) + U[0]*A[2][0] + U[1]A[2][1] + ... + U[?]*A[2][?] 
		 * 
		 * V = F(a1,..., i?) + U * A
		 * V = [v(X1); v(Y1); v(F1); ...; v(Xi); v(Yi); v(Fi)]
		 * 
		 * Свободни членове Lk(X)(Rk), Lk(Y)(Rk), Lk(Z)(Rk)
		 * Lk(Xi) = fx(a1,..., i1,..., i?, Xm, Ym, Fm, Xn, Yn, Zn)
		 * 
		 * 
		 * 
		 * M(?) = [a? b? c?; d? e? f?; g? h? i?] -> приблизителни са a?, b?,...i?
		 * M'(?) = [a'? b'? c'?; d'? e'? f'?; g'? h'? i'?] -> изравнени са a'?, b'?,...i'? 
		 * 
		 * 
		 */
		
		private void setCoef(Matrix coefs, int atIndex,	Matrix source, Matrix dest, double sign) {
			double sum = dest.getItem(0, 0) + dest.getItem(0, 1) + dest.getItem(0, 2); 
			double d1 = sign * source.getItem(0, 0) * sum;
			double e1 = sign * source.getItem(0, 1) * sum;  
			double f1 = sign * source.getItem(0, 2) * sum;  
			
			coefs.setItem(atIndex + 0, 0, d1 + coefs.getItem(atIndex + 0, 0));
			coefs.setItem(atIndex + 1, 0, e1 + coefs.getItem(atIndex + 1, 0));
			coefs.setItem(atIndex + 2, 0, f1 + coefs.getItem(atIndex + 2, 0));
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
			Matrix inv = new Matrix(3, 3);
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
				p1.setItem(0, 2, source.camera.realFocalDistance); // / tr.averageFocalDistance);
				source.camera.coefs.mMul(p1, t1);
				
				p2.setItem(0, 0, dest.x);
				p2.setItem(0, 1, dest.y);
				p2.setItem(0, 2, dest.camera.realFocalDistance); // / tr.averageFocalDistance);
				dest.camera.coefs.mMul(p2, t2);
				
				for (int curCoord = 0; curCoord < 3; curCoord++) {
					int c1 = (curCoord + 1) % 3;
					int c2 = (curCoord + 2) % 3;
					
					// L(x) = (d1*x1 + e1*y1 + f1*z1) * (g2*x2 + h2*y2 + i2*z2) - (d2*x2 + e2*y2 + f2*z2) * (g1*x1 + h1*y1 + i1*z1)
					// d(L(x))/d(d1) = x1 * (g2*x2 + h2*y2 + i2*z2)
					double L = 
						t1.getItem(0, c1) * t2.getItem(0, c2) -
						t2.getItem(0, c1) * t1.getItem(0, c2);
					if (srcIndex >= 0) {
						setCoef(coefs, srcIndex + c1 * 3, p1, t2,  1.0);
						setCoef(coefs, srcIndex + c2 * 3, p1, t2, -1.0);
						lsa.addMeasurement(coefs, computedWeight, L, 0);
					}
					if (destIndex >= 0) {
						setCoef(coefs, destIndex + c1 * 3, p2, t1, -1.0);
						setCoef(coefs, destIndex + c2 * 3, p2, t1,  1.0);
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
				image.coefs.setItem(0, 0, u.getItem(0, index + 0) + image.coefs.getItem(0, 0));
				image.coefs.setItem(1, 0, u.getItem(0, index + 1) + image.coefs.getItem(1, 0));
				image.coefs.setItem(2, 0, u.getItem(0, index + 2) + image.coefs.getItem(2, 0));

				image.coefs.setItem(0, 1, u.getItem(0, index + 3) + image.coefs.getItem(0, 1));
				image.coefs.setItem(1, 1, u.getItem(0, index + 4) + image.coefs.getItem(1, 1));
				image.coefs.setItem(2, 1, u.getItem(0, index + 5) + image.coefs.getItem(2, 1));

				image.coefs.setItem(0, 2, u.getItem(0, index + 6) + image.coefs.getItem(0, 2));
				image.coefs.setItem(1, 2, u.getItem(0, index + 7) + image.coefs.getItem(1, 2));
				image.coefs.setItem(2, 2, u.getItem(0, index + 8) + image.coefs.getItem(2, 2));
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
	
	public static void calculateDiscrepancy(List<MyCamera> images, 
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
			tr.transform(pair.srcPoint, p1);
			tr.transform(pair.destPoint, p2);
			pair.myDiscrepancy = Math.sqrt(
				Math.pow(p1.y*p2.z - p2.y*p1.z, 2) +	
				Math.pow(p1.x*p2.z - p2.x*p1.z, 2) +	
				Math.pow(p1.x*p2.y - p2.x*p1.y, 2)	
				);
//			pair.myDiscrepancy = Math.sqrt(
//				Math.pow(p1.x - p2.x, 2) +
//				Math.pow(p1.y - p2.y, 2)  
//				Math.pow(p1.z - p2.z, 2)
//				);
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
		calculateDiscrepancy(cameras, pointPairs, learner.tr);
		System.out.println("Done.");
	}
}
