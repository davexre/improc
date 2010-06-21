package com.slavi.improc.myadjust.zyx;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.math.transform.TransformLearnerResult;
import com.slavi.util.concurrent.TaskSetExecutor;
import com.slavi.util.file.AbsoluteToRelativePathMaker;

public class CalculatePanoramaParamsZYX implements Callable<ArrayList<ArrayList<KeyPointPairList>>> {

	ExecutorService exec;
	AbsoluteToRelativePathMaker keyPointPairFileRoot;
	ArrayList<KeyPointPairList> kppl;
	final String outputDir;
	final boolean pinPoints;
	final boolean useColorMasks;
	final boolean useImageMaxWeight;
	
	ArrayList<ArrayList<KeyPointPairList>> panos = new ArrayList<ArrayList<KeyPointPairList>>();
	
	public CalculatePanoramaParamsZYX(ExecutorService exec,
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

	public static void copyBadStatus(ArrayList<KeyPointPairList> chain) {
		for (int i = chain.size() - 1; i >= 0; i--) {
			KeyPointPairList pairList = chain.get(i);
			for (KeyPointPair pair : pairList.items) {
				pair.panoBad = pair.bad;
			}
		}
	}

	/**
	 * Returns a list of immages that form an immage chain.
	 * Returns an empty list if no immages remain in {@link #kppl}.
	 * Returns a list with one item if the image can not be linked to any other image in the {@link #kppl}.
	 * The items returned in the result are removed from {@link #kppl}
	 */
	public static ArrayList<KeyPointPairList> getImageChain(ArrayList<KeyPointPairList> kppl) {
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
		
		static final int maxIterations = 30;
		
		void removeProcessedFromChain(ArrayList<KeyPointPairList>ignoredPairs, ArrayList<KeyPointList>processedImages) {
			for (int i = processedImages.size() - 1; i >= 0; i--) {
				KeyPointList image = processedImages.get(i);
				for (int p = ignoredPairs.size() - 1; p >= 0; p--) {
					KeyPointPairList pairList = ignoredPairs.get(p);
					if ((pairList.source == image) || (pairList.target == image)) {
						ignoredPairs.remove(p);
					}
				}
			}
		}
		
		public Void call() {
			copyBadStatus(chain);
			MyPanoPairTransformZYXLearner learner = new MyPanoPairTransformZYXLearner(chain);
			for (int i = 0; i < maxIterations; i++) {
				TransformLearnerResult result = learner.calculateOne();
				System.out.println(result);
				if (result.isAdjustFailed())
					break;
				if (/*result.isAdjusted() ||*/ (result.discrepancyStatistics.getMaxX() < learner.discrepancyThreshold)) {
					synchronized (panos) {
						panos.add(learner.chain);
					}
					break;
				}
			}
			learner.images.add(learner.origin);
			removeProcessedFromChain(learner.ignoredPairLists, learner.images);
			synchronized(kppl) {
				kppl.addAll(learner.ignoredPairLists);
			}
			return null;
		}
		
/*		public Void call2() throws Exception {
			discrepancyThreshold = 5;
			discrepancyThreshold = (5.0 / 60.0) * MathUtil.deg2rad; // 1 angular minute
			copyBadStatus();
			while (true) {
				ArrayList<KeyPointPairList> tmp_chain = getImageChain(chain);
				ignoredPairLists.addAll(chain);
				chain = tmp_chain;
				CalculatePanoramaParams.buildImagesList(chain, images);
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
					System.out.println("Iteration " + iter + 
							" maxDiscrepancy=" + MathUtil.d4(maxDiscrepancy * MathUtil.rad2deg) + 
							" avgMax=" + MathUtil.d4(avgMaxDiscrepancy * MathUtil.rad2deg) + 
							" tmp=" + MathUtil.d4(tmpdiscr * MathUtil.rad2deg));
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
*/	
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
		int maxAttempts = 1;
		int curAttempt = 0;
		while ((kppl.size() > 0) && (curAttempt < maxAttempts)) {
			curAttempt++;
			if (Thread.interrupted())
				throw new InterruptedException();
			TaskSetExecutor taskSet = new TaskSetExecutor(exec);
			while (true) {
				ArrayList<KeyPointPairList> chain = getImageChain(kppl);
				if (chain.size() == 0)
					break;
				taskSet.add(new ProcessOne(chain));
			}
			taskSet.addFinished();
			taskSet.get();
			System.out.println("*********** NEW ATTEMPT " + curAttempt + " ****************");
		}
		return panos;
	}	
}
