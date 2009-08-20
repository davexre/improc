package com.slavi.improc.myadjust;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.math.MathUtil;
import com.slavi.math.RotationXYZ;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.concurrent.TaskSetExecutor;
import com.slavi.util.file.AbsoluteToRelativePathMaker;

public class CalculatePanoramaParams implements Callable<ArrayList<ArrayList<KeyPointPairList>>> {

	ExecutorService exec;
	AbsoluteToRelativePathMaker keyPointPairFileRoot;
	ArrayList<KeyPointPairList> kppl;
	final String outputDir;
	final boolean pinPoints;
	final boolean useColorMasks;
	final boolean useImageMaxWeight;
	
	ArrayList<ArrayList<KeyPointPairList>> panos = new ArrayList<ArrayList<KeyPointPairList>>();
	
	public CalculatePanoramaParams(ExecutorService exec,
			ArrayList<KeyPointPairList> kppl,
			AbsoluteToRelativePathMaker keyPointPairFileRoot,
			String outputDir,
			boolean pinPoints,
			boolean useColorMasks,
			boolean useImageMaxWeight) {
		this.exec = exec;
		this.kppl = kppl;
		this.keyPointPairFileRoot = keyPointPairFileRoot;
		this.outputDir = outputDir;
		this.pinPoints = pinPoints;
		this.useColorMasks = useColorMasks;
		this.useImageMaxWeight = useImageMaxWeight;
	}

	/**
	 * Returns a list of immages that form an immage chain.
	 * Returns an empty list if no immages remain in {@link #kppl}.
	 * Returns a list with one item if the image can not be linked to any other image in the {@link #kppl}.
	 * The items returned in the result are removed from {@link #kppl}
	 */
	private static ArrayList<KeyPointPairList> getImageChain(ArrayList<KeyPointPairList> kppl) {
		ArrayList<KeyPointPairList> result = new ArrayList<KeyPointPairList>();
		while (kppl.size() > 0) {
			KeyPointPairList start = kppl.remove(0);
			result.add(start);
			
			int curItemIndex = kppl.size() - 1;
			while (curItemIndex >= 0) {
				KeyPointPairList curItem = kppl.get(curItemIndex);
				
				for (int iIndex = result.size() - 1; iIndex >= 0; iIndex--) {
					KeyPointPairList i = result.get(iIndex);
					if (
							(i.source.imageId == curItem.source.imageId) ||
							(i.source.imageId == curItem.target.imageId) ||
							(i.target.imageId == curItem.source.imageId) ||
							(i.target.imageId == curItem.target.imageId)) {
						result.add(curItem);
						kppl.remove(curItemIndex);
						curItemIndex = kppl.size();
						break;
					}
				}
				curItemIndex--;
			}
			// Found a chain.
			return result;
		}
		return result;
	}
	
	public static void buildImagesList(ArrayList<KeyPointPairList> chain, ArrayList<KeyPointList> images) {
		images.clear();
		for (KeyPointPairList pairList : chain) {
			if (!images.contains(pairList.source))
				images.add(pairList.source);
			if (!images.contains(pairList.target))
				images.add(pairList.target);
		}
		Collections.sort(images, new Comparator<KeyPointList>() {
			public int compare(KeyPointList o1, KeyPointList o2) {
				return o1.imageFileStamp.getFile().getName().compareTo(o2.imageFileStamp.getFile().getName());
			}
		});
	}
	
	private class ProcessOne implements Callable<Void> {
		ArrayList<KeyPointPairList> chain;
		ArrayList<KeyPointList> images;
		ArrayList<KeyPointPairList> ignoredPairLists;
		KeyPointList origin;
		
		public ProcessOne(ArrayList<KeyPointPairList> chain) {
			this.chain = chain;
			images = new ArrayList<KeyPointList>();
			ignoredPairLists = new ArrayList<KeyPointPairList>();
		}
		
		void copyBadStatus() {
			for (int i = chain.size() - 1; i >= 0; i--) {
				KeyPointPairList pairList = chain.get(i);
				for (KeyPointPair pair : pairList.items) {
					pair.panoBad = pair.bad;
				}
			}
		}

		LeastSquaresAdjust lsa;
		double discrepancyThreshold;
		static final int maxIterations = 10;
		
		void removeProcessedFromChain() {
			chain = ignoredPairLists;
			ignoredPairLists = new ArrayList<KeyPointPairList>();
			for (int i = images.size() - 1; i >= 0; i--) {
				KeyPointList image = images.get(i);
				for (int p = chain.size() - 1; p >= 0; p--) {
					KeyPointPairList pairList = chain.get(p);
					if ((pairList.source == image) || (pairList.target == image)) {
						chain.remove(p);
					}
				}
			}
			copyBadStatus();
		}
		
		public Void call() throws Exception {
			copyBadStatus();
			while (true) {
				discrepancyThreshold = 5;
				ArrayList<KeyPointPairList> tmp_chain = getImageChain(chain);
				ignoredPairLists.addAll(chain);
				chain = tmp_chain;
				buildImagesList(chain, images);
//				copyBadStatus();
				int iter = 0;
				if (images.size() > 0) {
					origin = images.remove(0);
					calculatePrims();
				}
				while (true) {
					if (origin != null)
						images.add(0, origin);
					origin = null;
					removeBadKeyPointPairLists();					
					computeWeights();
					boolean chainModified = false;
					
					for (int i = chain.size() - 1; i >= 0; i--) {
						KeyPointPairList pairList = chain.get(i);
						int goodCount = 0;
						for (KeyPointPair pair : pairList.items) {
							if (!isBad(pair))
								goodCount++;
						}
						if (goodCount < 5) {
							System.out.println("BAD PAIR: " + goodCount + "/" + pairList.items.size() +
									"\t" + pairList.source.imageFileStamp.getFile().getName() +
									"\t" + pairList.target.imageFileStamp.getFile().getName());
							ignoredPairLists.add(chain.remove(i));
							chainModified = true;
						}
					}
					
					for (int i = images.size() - 1; i >= 0; i--) {
						KeyPointList image = images.get(i);
						if (image.goodCount > 10)
							continue;
						// Image does not have enough good points. Remove image and image pairs
						chainModified = true;
						System.out.println(image.imageFileStamp.getFile().getName() + " removed from current chain");
						images.remove(i);
						for (int p = chain.size() - 1; p >= 0; p--) {
							KeyPointPairList pairList = chain.get(p);
							if ((pairList.source == image) || (pairList.target == image)) {
								ignoredPairLists.add(chain.remove(p));
							}
						}
					}
					if (images.size() == 1) {
						removeProcessedFromChain();
						break;
					}
					if (images.size() == 0)
						return null;
					if (chainModified)
						break;
	
					// Adjust
					origin = images.remove(0);
//					calculatePrims();
					lsa = new LeastSquaresAdjust(images.size() * 4, 1);
					calculateNormalEquations();
					// Calculate Unknowns
					if (!lsa.calculate()) 
						return null;
					// Build transformer
					Matrix u = lsa.getUnknown();
					System.out.println(origin.imageFileStamp.getFile().getName() + 
							"\trx=" + MathUtil.d4(origin.rx * MathUtil.rad2deg) + 
							"\try=" + MathUtil.d4(origin.ry * MathUtil.rad2deg) + 
							"\trz=" + MathUtil.d4(origin.rz * MathUtil.rad2deg) + 
							"\ts=" + MathUtil.d4(origin.scaleZ)
							);
					for (int curImage = 0; curImage < images.size(); curImage++) {
						KeyPointList image = images.get(curImage);
						int index = curImage * 4;
						System.out.println(image.imageFileStamp.getFile().getName() + 
								"\trx=" + MathUtil.d4(image.rx * MathUtil.rad2deg) + 
								"\try=" + MathUtil.d4(image.ry * MathUtil.rad2deg) + 
								"\trz=" + MathUtil.d4(image.rz * MathUtil.rad2deg) + 
								"\ts=" + MathUtil.d4(image.scaleZ) +
								"\tdx=" + MathUtil.d4(u.getItem(0, index + 0) * MathUtil.rad2deg) + 
								"\tdy=" + MathUtil.d4(u.getItem(0, index + 1) * MathUtil.rad2deg) + 
								"\tdz=" + MathUtil.d4(u.getItem(0, index + 2) * MathUtil.rad2deg) + 
								"\tds=" + MathUtil.d4(u.getItem(0, index + 3)) 
								);
						image.scaleZ = (image.scaleZ - u.getItem(0, index + 3));
						image.rx = MathUtil.fixAngleMPI_PI(image.rx - u.getItem(0, index + 0));
						image.ry = MathUtil.fixAngleMPI_PI(image.ry - u.getItem(0, index + 1));
						image.rz = MathUtil.fixAngleMPI_PI(image.rz - u.getItem(0, index + 2));
						buildCamera2RealMatrix(image);
					}
					computeDiscrepancies();
					double maxDiscrepancy = maxDiscrepancyStat.getMaxX();
					double avgMaxDiscrepancy = maxDiscrepancyStat.getAvgValue();
					double tmpdiscr = (maxDiscrepancyStat.getAvgValue() + maxDiscrepancy) / 2.0;
					System.out.println("Iteration " + iter + " maxDiscrepancy=" + maxDiscrepancy + " avgMax=" + avgMaxDiscrepancy + " tmp=" + tmpdiscr);
					boolean isDone = false;
					if (maxDiscrepancy > discrepancyThreshold) {
						if (recomputeBad(maxDiscrepancy)) {
							isDone = true;
						}
					} else {
						isDone = true;
					}
					if (isDone) {
						synchronized (panos) {
							panos.add(chain);
						}
						removeProcessedFromChain();
						break;
					}
					iter++;
					if (iter >= maxIterations) {
						removeProcessedFromChain();
						break;
					}
				}
			}
		}

		boolean recomputeBad(double maxMaxDiscrepancy) {
			boolean adjusted = true;
			for (KeyPointPairList pairList : chain) {
//				int goodCount = 0;
				double discr = Math.min(maxMaxDiscrepancy, pairList.maxDiscrepancy);
				for (KeyPointPair item : pairList.items) {
					boolean oldIsBad = isBad(item);
					double discrepancy = getDiscrepancy(item);
					boolean curIsBad = discrepancy > discr;
					if (oldIsBad != curIsBad) {
						setBad(item, curIsBad);
						if (curIsBad)
							adjusted = false;
					}
//					if (!curIsBad)
//						goodCount++;
				}
//				System.out.println(goodCount + "/" + pairList.items.size() + "\t" +
//						pairList.source.imageFileStamp.getFile().getName() + "\t" + 
//						pairList.target.imageFileStamp.getFile().getName() + "\t" +
//						MathUtil.d4(pairList.maxDiscrepancy)
//				);
			}
			return adjusted;
		}
		
		Statistics maxDiscrepancyStat = new Statistics();
		protected void computeDiscrepancies() {
			Point2D.Double PW1 = new Point2D.Double();
			Point2D.Double PW2 = new Point2D.Double();

			maxDiscrepancyStat.start();
			Statistics stat = new Statistics();
			for (KeyPointPairList pairList : chain) {
				stat.start();
				int goodCount = 0;
				for (KeyPointPair item : pairList.items) {
					// Compute for all points, so no item.isBad check
					MyPanoPairTransformer.transform(item.sourceSP.doubleX, item.sourceSP.doubleY, pairList.source, PW1);
					MyPanoPairTransformer.transformBackward(PW1.x, PW1.y, pairList.target, PW2);

					double dx = item.targetSP.doubleX - PW2.x;
					double dy = item.targetSP.doubleY - PW2.y;
					setDiscrepancy(item, Math.sqrt(dx*dx + dy*dy));
					if (!isBad(item)) {
						stat.addValue(item.discrepancy, getWeight(item));
						goodCount++;
					}
				}
				stat.stop();
				pairList.maxDiscrepancy = stat.getAvgValue();
				if (pairList.maxDiscrepancy < discrepancyThreshold)
					pairList.maxDiscrepancy = discrepancyThreshold;
				System.out.println(
						pairList.source.imageFileStamp.getFile().getName() + "\t" +
						pairList.target.imageFileStamp.getFile().getName() + "\t" +
						MathUtil.d4(pairList.maxDiscrepancy) + "\t" +
						goodCount
						);
				maxDiscrepancyStat.addValue(pairList.maxDiscrepancy);
			}
			maxDiscrepancyStat.stop();
			return;
		}
		
		void calculateNormalEquations() {
			Matrix coefs = new Matrix(images.size() * 4, 1);			

			origin.rx = 0;
			origin.ry = 0;
			origin.rz = 0;
			buildCamera2RealMatrix(origin);
			for (KeyPointList image : images) {
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
			
			KeyPoint source1 = new KeyPoint();
			KeyPoint dest1= new KeyPoint();
			lsa.clear();
			int pointCounter = 0;
			for (KeyPointPairList pairList : chain) {
				for (KeyPointPair item : pairList.items) {
					if (isBad(item))
						continue;
					pointCounter++;
					
					double computedWeight = getComputedWeight(item);
					KeyPoint source = item.getKey();
					KeyPoint dest = item.getValue();
					
					source1.doubleX = (source.doubleX - pairList.source.cameraOriginX) * pairList.source.cameraScale;
					source1.doubleY = (source.doubleY - pairList.source.cameraOriginY) * pairList.source.cameraScale;

					dest1.doubleX = (dest.doubleX - pairList.target.cameraOriginX) * pairList.target.cameraScale;
					dest1.doubleY = (dest.doubleY - pairList.target.cameraOriginY) * pairList.target.cameraScale;
					
					int srcIndex = images.indexOf(pairList.source) * 4;
					int destIndex = images.indexOf(pairList.target) * 4;
					
					coefs.make0();
		
					MyPanoPairTransformer.transform3D(source, pairList.source, PW1);
					MyPanoPairTransformer.transform3D(dest, pairList.target, PW2);
					
					P1.setItem(0, 0, source1.doubleX);
					P1.setItem(0, 1, source1.doubleY);
					P1.setItem(0, 2, pairList.source.scaleZ);
					
					P2.setItem(0, 0, dest1.doubleX);
					P2.setItem(0, 1, dest1.doubleY);
					P2.setItem(0, 2, pairList.target.scaleZ);
					
					source.keyPointList.dMdX.mMul(P1, dPW1dX1);
					source.keyPointList.dMdY.mMul(P1, dPW1dY1);
					source.keyPointList.dMdZ.mMul(P1, dPW1dZ1);
					
					dest.keyPointList.dMdX.mMul(P2, dPW2dX2);
					dest.keyPointList.dMdY.mMul(P2, dPW2dY2);
					dest.keyPointList.dMdZ.mMul(P2, dPW2dZ2);
		
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
									source.keyPointList.camera2real.getItem(2, c1) * getTransformedCoord(PW2, c2) - 
									source.keyPointList.camera2real.getItem(2, c2) * getTransformedCoord(PW2, c1)));
						}
						if (destIndex >= 0) {
							setCoef(coefs, dPW2dX2, dPW2dY2, dPW2dZ2, destIndex, c1, -getTransformedCoord(PW1, c2));
							setCoef(coefs, dPW2dX2, dPW2dY2, dPW2dZ2, destIndex, c2,  getTransformedCoord(PW1, c1));
							coefs.setItem(destIndex + 3, 0, (
									getTransformedCoord(PW1, c1) * dest.keyPointList.camera2real.getItem(2, c2) - 
									getTransformedCoord(PW1, c2) * dest.keyPointList.camera2real.getItem(2, c1)));
						}
						lsa.addMeasurement(coefs, computedWeight, L, 0);
					}
				}
			}
		}
		
		void buildCamera2RealMatrix(KeyPointList image) {
			image.camera2real = RotationXYZ.makeAngles(image.rx, image.ry, image.rz);
			image.dMdX = RotationXYZ.make_dF_dX(image.rx, image.ry, image.rz);
			image.dMdY = RotationXYZ.make_dF_dY(image.rx, image.ry, image.rz);
			image.dMdZ = RotationXYZ.make_dF_dZ(image.rx, image.ry, image.rz);
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
		
		public void setDiscrepancy(KeyPointPair item, double discrepancy) {
			item.discrepancy = discrepancy;
		}

		public double getDiscrepancy(KeyPointPair item) {
			return item.discrepancy;
		}
		
		public void setBad(KeyPointPair item, boolean bad) {
			item.panoBad = bad;
		}

		public boolean isBad(KeyPointPair item) {
			return item.panoBad;
		}

		public double getWeight(KeyPointPair item) {
			return item.weight;
		}
		
		public double getComputedWeight(KeyPointPair item) {
			return isBad(item) ? 0.0 : getWeight(item) * oneOverSumWeights; 
		}

		private double oneOverSumWeights = 1.0;
		/**
		 * @return Number of point pairs NOT marked as bad.
		 */
		protected int computeWeights() {
			for (KeyPointList image : images) {
				image.goodCount = 0;
			}
			int totalGoodCount = 0;
			double sumWeight = 0;
			for (KeyPointPairList pairList : chain) {
				for (KeyPointPair item : pairList.items) {
					if (isBad(item))
						continue;
					item.sourceSP.keyPointList.goodCount++;
					item.targetSP.keyPointList.goodCount++;
					double weight = getWeight(item); 
					if (weight < 0)
						throw new IllegalArgumentException("Negative weight received.");
					totalGoodCount++;
					sumWeight += weight;
				}
			}
			if (sumWeight == 0.0) {
				oneOverSumWeights = 1.0 / totalGoodCount;
			} else {
				oneOverSumWeights = 1.0 / sumWeight;
			}
			return totalGoodCount;
		}
		
		void calculatePrims() throws Exception {
			origin.rx = 0.0;
			origin.ry = 0.0;
			origin.rz = 0.0;
			origin.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;
			origin.calculatePrimsAtHop = 0;
			
			System.out.println("Caclulating prims:");
			System.out.println(origin.imageFileStamp.getFile().getName());
			ArrayList<KeyPointList> todo = new ArrayList<KeyPointList>(images);
			for (KeyPointList image : todo)
				image.calculatePrimsAtHop = -1;
			
			int curImageIndex = todo.size() - 1;
			boolean listModified = false;
			while (curImageIndex >= 0) {
				KeyPointList curImage = todo.get(curImageIndex);
				KeyPointPairList minHopPairList = null;
				int minHop = Integer.MAX_VALUE;
				
				for (KeyPointPairList pairList : chain) {
					if (curImage == pairList.source) {
						if ((pairList.target.calculatePrimsAtHop < 0)) 
							continue;
						if ((minHopPairList == null) ||
							(minHop > pairList.target.calculatePrimsAtHop)) {
							minHopPairList = pairList;
							minHop = pairList.target.calculatePrimsAtHop;
						}
					} else if (curImage == pairList.target) {
						if ((pairList.source.calculatePrimsAtHop < 0)) 
							continue;
						if ((minHopPairList == null) ||
							(minHop > pairList.source.calculatePrimsAtHop)) {
							minHopPairList = pairList;
							minHop = pairList.source.calculatePrimsAtHop;
						}
					}
				}
				
				if (minHopPairList != null) {
					if (curImage == minHopPairList.source) {
						double angles[] = new double[3];
						Matrix sourceToTarget = RotationXYZ.makeAngles(minHopPairList.rx, minHopPairList.ry, minHopPairList.rz);
						Matrix targetToWorld = RotationXYZ.makeAngles(-minHopPairList.target.rx, -minHopPairList.target.ry, minHopPairList.target.rz);
						Matrix sourceToWorld = new Matrix(3, 3);
						sourceToTarget.mMul(targetToWorld, sourceToWorld);
						RotationXYZ.getRotationAngles(sourceToWorld, angles);
						curImage.rx = -angles[0];
						curImage.ry = -angles[1];
						curImage.rz = angles[2];
						curImage.scaleZ = minHopPairList.target.scaleZ * minHopPairList.scale; 
						System.out.println(curImage.imageFileStamp.getFile().getName() + "\t" + minHopPairList.target.imageFileStamp.getFile().getName());
					} else { // if (curImage == minHopPairList.target) {
						double angles[] = new double[3];
						RotationXYZ.getRotationAnglesBackword(minHopPairList.rx, minHopPairList.ry, minHopPairList.rz, angles);
						Matrix targetToSource = RotationXYZ.makeAngles(-angles[0], -angles[1], angles[2]);
						Matrix sourceToWorld = RotationXYZ.makeAngles(minHopPairList.source.rx, minHopPairList.source.ry, minHopPairList.source.rz);
						Matrix targetToWorld = new Matrix(3, 3);
						targetToSource.mMul(sourceToWorld, targetToWorld);
						RotationXYZ.getRotationAngles(targetToWorld, angles);
						curImage.rx = angles[0];
						curImage.ry = angles[1];
						curImage.rz = angles[2];
						curImage.scaleZ = minHopPairList.source.scaleZ / minHopPairList.scale; 
						System.out.println(curImage.imageFileStamp.getFile().getName() + "\t" + minHopPairList.source.imageFileStamp.getFile().getName());
					}
					curImage.calculatePrimsAtHop = minHop + 1;
					todo.remove(curImageIndex);
					curImageIndex = todo.size();
					listModified = true;
				}
				curImageIndex--;
				if (curImageIndex < 0) {
					if (!listModified)
						break;
					curImageIndex = todo.size() - 1;
					listModified = false;
				}
			}
			
			if (todo.size() > 0) 
				throw new Exception("Failed calculating the prims");
		}
	}
	
	private void removeBadKeyPointPairLists() {
		for (int i = kppl.size() - 1; i >= 0; i--) {
			KeyPointPairList pairList = kppl.get(i);
			int goodCount = pairList.getGoodCount();
			if (goodCount < 10) {
				System.out.println("BAD PAIR: " + goodCount + "/" + pairList.items.size() +
						"\t" + pairList.source.imageFileStamp.getFile().getName() +
						"\t" + pairList.target.imageFileStamp.getFile().getName());
				kppl.remove(i);
			}
		}
	}
	
	public ArrayList<ArrayList<KeyPointPairList>> call() throws Exception {
		removeBadKeyPointPairLists();
		TaskSetExecutor taskSet = new TaskSetExecutor(exec);
		while (true) {
			ArrayList<KeyPointPairList> chain = getImageChain(kppl);
			if (chain.size() == 0)
				break;
			taskSet.add(new ProcessOne(chain));
		}
		taskSet.addFinished();
		taskSet.get();
		return panos;
	}	
}
