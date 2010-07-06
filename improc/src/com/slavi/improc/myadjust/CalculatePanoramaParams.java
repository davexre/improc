package com.slavi.improc.myadjust;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
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
		
		public Void call() {
			copyBadStatus(chain);
			panoTransformer.initialize(chain);
			int iteration = 0;
//			for (int i = 0; i < maxIterations; i++) {
			while (iteration < maxIterations) {
				TransformLearnerResult result = panoTransformer.calculateOne();
				System.out.println(result);
				iteration = result.iteration;
				if (iteration < 3) {
					copyBadStatus(chain);
					continue;
				}
				if (result.isAdjustFailed())
					break;
				if (/*result.isAdjusted() || */ (result.discrepancyStatistics.getMaxX() < panoTransformer.getDiscrepancyThreshold())) {
					synchronized (panos) {
						panos.add(panoTransformer.chain);
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
