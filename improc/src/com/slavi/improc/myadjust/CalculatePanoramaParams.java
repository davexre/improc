package com.slavi.improc.myadjust;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPairList;
import com.slavi.util.file.AbsoluteToRelativePathMaker;

public class CalculatePanoramaParams implements Callable<Void> {

	ExecutorService exec;
	AbsoluteToRelativePathMaker keyPointPairFileRoot;
	ArrayList<KeyPointPairList> kppl;
	final String outputDir;
	final boolean pinPoints;
	final boolean useColorMasks;
	final boolean useImageMaxWeight;
	
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
	
	private ArrayList<KeyPointPairList> getImageChain() {
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
		return null;
	}
	
	public Void call() throws Exception {
		removeBadKeyPointPairLists();
		while (true) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			ArrayList<KeyPointPairList> chain = getImageChain();
			if (chain == null)
				break;
			
			ArrayList<KeyPointList> images = new ArrayList<KeyPointList>();
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
			
			System.out.println("Found chain: ");
			for (KeyPointList i : images) {
				System.out.println("  " + i.imageFileStamp.getFile().getName());
			}
			
			MyPanoPairTransformLearner3 learner = new MyPanoPairTransformLearner3(chain);
			learner.calculatePrims();
			boolean success = learner.calculate();
//			if (success) {
				MyGeneratePanoramas gen = new MyGeneratePanoramas(exec, images, chain, keyPointPairFileRoot,
						outputDir, pinPoints, useColorMasks, useImageMaxWeight);
				gen.call();
//			}
			System.out.println("Adjust panorama " + (success ? "SUCCESS" : "FAILED"));
		}
		return null;
	}	
}
