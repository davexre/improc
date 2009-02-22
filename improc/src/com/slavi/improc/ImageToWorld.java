package com.slavi.improc;

import java.util.ArrayList;

import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.BaseTransformer;
public class ImageToWorld {
	
	public static class Point3D {
		double x, y, z;
	}
	
	public static class ImageToWorldTransformer extends BaseTransformer<KeyPoint, Point3D> {
	
		KeyPointList images[];
		
		KeyPointList originImage;
		
		public ImageToWorldTransformer(KeyPointList originImage, ArrayList<KeyPointList> images) {
			this.originImage = originImage;
			ArrayList<KeyPointList> tmp = new ArrayList<KeyPointList>(images);
			tmp.remove(originImage);
			this.images = tmp.toArray(new KeyPointList[0]);
			
			this.originImage.toWorld = new Matrix(3, 3);
			for (KeyPointList image : images) {
				image.toWorld = new Matrix(3, 3);
			}
		}
		
		public int indexOf(KeyPointList image) {
			for (int i = 0; i < images.length; i++)
				if (images[i] == image)
					return i;
			return -1;
		}
		
		public int getInputSize() {
			return 2;
		}
	
		public int getNumberOfCoefsPerCoordinate() {
			return images.length * 9;
		}
	
		public int getOutputSize() {
			return 2;
		}
	
		public double getSourceCoord(KeyPoint item, int coordIndex) {
			switch (coordIndex) {
			case 0: return item.doubleX;
			case 1: return item.doubleY;
			default: throw new IllegalArgumentException("Index out of range [0..1]");
			}
		}
	
		public double getTargetCoord(Point3D item, int coordIndex) {
			switch (coordIndex) {
			case 0: return item.x;
			case 1: return item.y;
			default: throw new IllegalArgumentException("Index out of range [0..1]");
			}
		}
	
		public void setSourceCoord(KeyPoint item, int coordIndex, double value) {
			switch (coordIndex) {
			case 0: 
				item.doubleX = value;
				break;
			case 1: 
				item.doubleY = value;
				break;
			default: throw new IllegalArgumentException("Index out of range [0..1]");
			}
		}
	
		public void setTargetCoord(Point3D item, int coordIndex, double value) {
			switch (coordIndex) {
			case 0: 
				item.x = value;
				break;
			case 1: 
				item.y = value;
				break;
			default: throw new IllegalArgumentException("Index out of range [0..1]");
			}
		}
	
		public void transform(KeyPoint source, Point3D dest) {
			dest.x =
				source.doubleX * source.keyPointList.toWorld.getItem(0, 0) +
				source.doubleX * source.keyPointList.toWorld.getItem(1, 0) +
				source.keyPointList.getFocalDistance() * source.keyPointList.toWorld.getItem(2, 0);
			dest.y =
				source.doubleX * source.keyPointList.toWorld.getItem(0, 1) +
				source.doubleY * source.keyPointList.toWorld.getItem(1, 1) +
				source.keyPointList.getFocalDistance() * source.keyPointList.toWorld.getItem(2, 1);
			dest.z =
				source.doubleX * source.keyPointList.toWorld.getItem(0, 2) +
				source.doubleY * source.keyPointList.toWorld.getItem(1, 2) +
				source.keyPointList.getFocalDistance() * source.keyPointList.toWorld.getItem(2, 2);
		}
	
		public void fromWorld() {
			
		}
	}
	
	public static class ImageToWorldTransformLearner { //extends BaseTransformLearner<KeyPoint, Point3D> {

		ImageToWorldTransformer tr;
		
		LeastSquaresAdjust lsa;
		
		ArrayList<KeyPointPairList> pointsPairList;
		
		public ImageToWorldTransformLearner(KeyPointList originImage, ArrayList<KeyPointList> images,
				ArrayList<KeyPointPairList> pointsPairList) {
			tr = new ImageToWorldTransformer(originImage, images);
			this.pointsPairList = pointsPairList;
			this.lsa = new LeastSquaresAdjust(tr.getNumberOfCoefsPerCoordinate(), 1);		
		}

		private double oneOverSumWeights = 1.0;
		/**
		 * 
		 * @return Number of point pairs NOT marked as bad.
		 */
		protected int computeWeights() {
			int goodCount = 0;
			double sumWeight = 0;
			for (KeyPointPairList pairList : pointsPairList) {
				for (KeyPointPair item : pairList.items.values()) {
					if (item.bad)
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
		
		private double getCoord(Point3D item, int coordIndex) {
			switch (coordIndex) {
			case 0: return item.x;
			case 1: return item.y;
			case 2: return item.z;
			default: throw new IllegalArgumentException("Index out of range [0..2]");
			}
		}
		
		private void setCoef(Matrix coefs, int atIndex,	Matrix source, double transformedCoord) {
			coefs.setItem(atIndex + 0, 0, source.getItem(0, 0) * transformedCoord);
			coefs.setItem(atIndex + 1, 0, source.getItem(0, 1) * transformedCoord);
			coefs.setItem(atIndex + 2, 0, source.getItem(0, 2) * transformedCoord);
		}

		public boolean calculateOne() {
			int goodCount = computeWeights();
			if (goodCount < lsa.getRequiredPoints())
				return false;
			Matrix coefs = new Matrix(tr.getNumberOfCoefsPerCoordinate(), 1);			

			tr.originImage.toWorld.makeE();
			
			Matrix p1 = new Matrix(1, 3);
			Matrix p2 = new Matrix(1, 3);
			Point3D t1 = new Point3D();
			Point3D t2 = new Point3D();
			lsa.clear();
			for (KeyPointPairList pairList : pointsPairList) {
				for (KeyPointPair item : pairList.items.values()) {
					if (isBad(item))
						continue;
					
					double computedWeight = getComputedWeight(item);
					int srcIndex = tr.indexOf(item.sourceSP.keyPointList) * 9;
					int destIndex = tr.indexOf(item.targetSP.keyPointList) * 9;
					
					coefs.make0();
					tr.transform(item.sourceSP, t1);
					tr.transform(item.targetSP, t2);
					
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
							getCoord(t1, c1) * getCoord(t2, c2) -
							getCoord(t1, c2) * getCoord(t2, c1);
						/*
						 * fx: P'1(y) * P'2(z) - P'1(z) * P'2(y) = 0
						 * fy: P'1(x) * P'2(z) - P'1(z) * P'2(x) = 0
						 * fz: P'1(x) * P'2(y) - P'1(y) * P'2(x) = 0
						 * 
						 * f(curCoord): P'1(c1) * P'2(c2) - P'1(c2) * P'2(c1) = 0
						 */
						if (srcIndex >= 0) {
							setCoef(coefs, srcIndex + c1 * 3, p1,  getCoord(t2, c2));
							setCoef(coefs, srcIndex + c2 * 3, p1, -getCoord(t2, c1));
						}
						if (destIndex >= 0) {
							setCoef(coefs, destIndex + c1 * 3, p2, -getCoord(t1, c1));
							setCoef(coefs, destIndex + c2 * 3, p2,  getCoord(t1, c2));
						}
						lsa.addMeasurement(coefs, computedWeight, L, 0);
					}
				}
			}
			
			if (!lsa.calculate()) 
				return false;

			// Build transformer
			Matrix u = lsa.getUnknown();
			for (int curImage = 0; curImage < tr.images.length; curImage++) {
				KeyPointList image = tr.images[curImage];
				int index = curImage * 9;
				image.toWorld.setItem(0, 0, u.getItem(0, index + 0) + image.toWorld.getItem(0, 0));
				image.toWorld.setItem(1, 0, u.getItem(0, index + 1) + image.toWorld.getItem(1, 0));
				image.toWorld.setItem(2, 0, u.getItem(0, index + 2) + image.toWorld.getItem(2, 0));

				image.toWorld.setItem(0, 1, u.getItem(0, index + 3) + image.toWorld.getItem(0, 1));
				image.toWorld.setItem(1, 1, u.getItem(0, index + 4) + image.toWorld.getItem(1, 1));
				image.toWorld.setItem(2, 1, u.getItem(0, index + 5) + image.toWorld.getItem(2, 1));

				image.toWorld.setItem(0, 2, u.getItem(0, index + 6) + image.toWorld.getItem(0, 2));
				image.toWorld.setItem(1, 2, u.getItem(0, index + 7) + image.toWorld.getItem(1, 2));
				image.toWorld.setItem(2, 2, u.getItem(0, index + 8) + image.toWorld.getItem(2, 2));
			}
			return true;
		}

		public double getDiscrepancy(KeyPointPair item) {
			return item.discrepancy;
		}

		public double getWeight(KeyPointPair item) {
			return item.weight;
		}

		public boolean isBad(KeyPointPair item) {
			return item.bad;
		}

		public void setBad(KeyPointPair item, boolean bad) {
			item.bad = bad;
		}

		public void setDiscrepancy(KeyPointPair item, double discrepancy) {
			item.discrepancy = discrepancy;
		}
	}
	
}