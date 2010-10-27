package com.slavi.improc.myadjust;

import java.io.FileNotFoundException;
import java.io.PrintStream;
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
import com.slavi.math.adjust.Statistics;
import com.slavi.math.transform.TransformLearnerResult;
import com.slavi.util.file.AbsoluteToRelativePathMaker;

public class CalculatePanoramaParams implements Callable<ArrayList<ArrayList<KeyPointPairList>>> {

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
	
	ExecutorService exec;
	PanoTransformer panoTransformer;
	AbsoluteToRelativePathMaker keyPointPairFileRoot;
	ArrayList<KeyPointPairList> kppl;
	final String outputDir;
	final boolean pinPoints;
	final boolean useColorMasks;
	final boolean useImageMaxWeight;
	
	ArrayList<ArrayList<KeyPointPairList>> panos = new ArrayList<ArrayList<KeyPointPairList>>();
	
	public CalculatePanoramaParams(ExecutorService exec,
			String panoTransformerClassName,
			ArrayList<KeyPointPairList> kppl,
			AbsoluteToRelativePathMaker keyPointPairFileRoot,
			String outputDir,
			boolean pinPoints,
			boolean useColorMasks,
			boolean useImageMaxWeight) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		this.exec = exec;
		this.panoTransformer = (PanoTransformer) Class.forName(panoTransformerClassName).newInstance();
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
				pair.panoBad = pair.validatePairBad;
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
		if (kppl.size() > 0) {
			result.add(kppl.remove(0));

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
		}
/*		// print
		System.out.println();
		System.out.println("Result image chain:");
		for (KeyPointPairList pairList : result) {
			System.out.println(
					pairList.source.imageFileStamp.getFile().getName() +
					"\t" + pairList.target.imageFileStamp.getFile().getName() + 
					"\t" + pairList.items.size()
					);

		}
		System.out.println();
		System.out.println("Remaining image chain:");
		for (KeyPointPairList pairList : kppl) {
			System.out.println(
					pairList.source.imageFileStamp.getFile().getName() +
					"\t" + pairList.target.imageFileStamp.getFile().getName() + 
					"\t" + pairList.items.size()
					);
		}
*/
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
		
		private boolean isBad(KeyPointPair pair, boolean usePanoBad) {
			return usePanoBad ? pair.panoBad : pair.validatePairBad;
		}
		
		public void dumpPanoData(ArrayList<KeyPointPairList> pano, boolean usePanoBad) {
			System.out.println("\n=============== Pano data ================== usePanoBad=" + usePanoBad);
			Statistics good = new Statistics();
			Statistics goodDist = new Statistics();
			Statistics bad = new Statistics();
			Statistics badDist = new Statistics();
			for (KeyPointPairList pairList : pano) {
				good.start();
				goodDist.start();
				bad.start();
				badDist.start();
				
				for (KeyPointPair pair : pairList.items) {
					double dist = 0.0;
					for (int i = 0; i < KeyPoint.featureVectorLinearSize; i++) {
						double sv = pair.sourceSP.getValue(i); 
						double tv = pair.targetSP.getValue(i);
						double d = Math.abs(sv - tv);
						dist += d*d;
						if (d > 0) {
							if (isBad(pair, usePanoBad))
								bad.addValue(d);
							else
								good.addValue(d);
						}
					}
					if (isBad(pair, usePanoBad))
						badDist.addValue(Math.sqrt(dist));
					else
						goodDist.addValue(Math.sqrt(dist));
				}
				good.stop();
				goodDist.stop();
				bad.stop();
				badDist.stop();
				
				System.out.println(MathUtil.l10(goodDist.getItemsCount()) + "/" + MathUtil.l10(pairList.items.size()) +
						"\t" + pairList.source.imageFileStamp.getFile().getName() +
						"\t" + pairList.target.imageFileStamp.getFile().getName() + 
						" goodDist.Avg=" + MathUtil.d4(goodDist.getAvgValue()) +
						" goodDist.Max=" + MathUtil.d4(goodDist.getMaxX()) +
						" badDist.Avg=" + MathUtil.d4(badDist.getAvgValue()) +
						" badDist.Max=" + MathUtil.d4(badDist.getMaxX()) +
						" good.Avg=" + MathUtil.d4(good.getAvgValue()) +
						" good.Max=" + MathUtil.d4(good.getMaxX()) +
						" good.Min=" + MathUtil.d4(good.getMinX()) +
						" bad.Avg=" + MathUtil.d4(bad.getAvgValue()) +
						" bad.Max=" + MathUtil.d4(bad.getMaxX()) +
						" bad.Min=" + MathUtil.d4(bad.getMinX())
				);
			}
		}
		
		public Void call() {
			copyBadStatus(chain);
			panoTransformer.initialize(chain);
			int iteration = 0;
//			for (int i = 0; i < maxIterations; i++) {
			while (iteration < maxIterations) {
				TransformLearnerResult result = panoTransformer.calculateOne();
				System.out.println(result);
				iteration = result.iteration;
				
				System.out.println("=== Discrepancy threshold " + MathUtil.d4(panoTransformer.getDiscrepancyThreshold()));
				panoTransformer.showValidateKeyPointPairStatistic();				
//				if (iteration < 3) {
//					copyBadStatus(chain);
//					continue;
//				}
				if (result.isAdjustFailed())
					break;
				if (/*result.isAdjusted() || */ (result.discrepancyStatistics.getMaxX() < panoTransformer.getDiscrepancyThreshold())) {
					synchronized (panos) {
						panos.add(panoTransformer.chain);
						dumpPanoData(panoTransformer.chain, true);
						dumpPanoData(panoTransformer.chain, false);
					}
					break;
				}
			}
			panoTransformer.images.add(panoTransformer.origin);
			removeProcessedFromChain(panoTransformer.ignoredPairLists, panoTransformer.images);
			synchronized(kppl) {
				kppl.addAll(panoTransformer.ignoredPairLists);
			}
			return null; 
		}
	}
	
	private void removeBadKeyPointPairLists() {
		for (int i = kppl.size() - 1; i >= 0; i--) {
			KeyPointPairList pairList = kppl.get(i);
			int goodCount = 0;
			for (KeyPointPair pair : pairList.items)
				if (!pair.panoBad)
					goodCount++;
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
			System.out.println("*********** ATTEMPT " + curAttempt + " ****************");
			if (Thread.interrupted())
				throw new InterruptedException();
//			TaskSetExecutor taskSet = new TaskSetExecutor(exec);
			while (true) {
				ArrayList<KeyPointPairList> chain = getImageChain(kppl);
				if (chain.size() == 0)
					break;
//				taskSet.add(new ProcessOne(chain));
				new ProcessOne(chain).call();
			}
//			taskSet.addFinished();
//			taskSet.get();
		}
		return panos;
	}	
}
