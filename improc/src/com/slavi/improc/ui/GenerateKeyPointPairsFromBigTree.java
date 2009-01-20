package com.slavi.improc.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairBigTree;
import com.slavi.improc.KeyPointPairList;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.tree.KDTree;
import com.slavi.util.ui.SwtUtil;

public class GenerateKeyPointPairsFromBigTree implements Callable<ArrayList<KeyPointPairList>> {

	KeyPointPairBigTree tree;
	
	AbsoluteToRelativePathMaker rootKeyPointFileDir;

	Map<String, KeyPointPairList> keyPointPairLists = new HashMap<String, KeyPointPairList>();

	AtomicInteger processed = new AtomicInteger(0);
	
	public KeyPointPairList getKeyPointPairList(String id, KeyPointList source, KeyPointList target) {
		synchronized (keyPointPairLists) {
			KeyPointPairList result = keyPointPairLists.get(id);
			if (result == null) {
				result = new KeyPointPairList();
				result.source = source;
				result.target = target;
				keyPointPairLists.put(id, result);
			}
			return result;
		}
	}

	class ProcessOneImage implements Callable<Void> {

		KeyPointList image;
		
		public ProcessOneImage(KeyPointList image) {
			this.image = image;
		}
		
		public Void call() throws Exception {
//			int searchSteps = (int) (Math.max(130.0, (Math.log(tree.getSize()) / Math.log (1000.0)) * 130.0));
			int searchSteps = tree.getTreeDepth() / 2;
			int imageId = image.hashCode();
			String strImageId = Integer.toString(imageId);
			
			int totalPairCount = 0;
			for (KeyPoint kp : image) {
				if (Thread.interrupted())
					throw new InterruptedException();
				
				KDTree.NearestNeighbours<KeyPoint> nnlst = tree.getNearestNeighboursBBF(kp, 2, searchSteps);
				if (nnlst.size() < 2)
					continue;
//				if (nnlst.getDistanceToTarget(0) > nnlst.getDistanceToTarget(1) * 0.6) {
//					continue;
//				}
				KeyPoint kp2 = nnlst.getItem(0);
				int destImageId = kp2.keyPointList.hashCode();
				String pairId;
				KeyPoint kpS, kpT;
				
				if (imageId > destImageId) {
					pairId = strImageId + "-" + Integer.toString(destImageId);
					kpS = kp;
					kpT = kp2;
				} else {
					pairId = Integer.toString(destImageId) + "-" + strImageId;
					kpS = kp2;
					kpT = kp;
				}
				
				KeyPointPairList kppl = getKeyPointPairList(pairId, kpS.keyPointList, kpT.keyPointList);
				synchronized (kppl) {
					KeyPointPair pair = kppl.items.get(kpS);
					if (pair == null) {
						pair = new KeyPointPair(kpS, kpT, nnlst.getDistanceToTarget(0), nnlst.getDistanceToTarget(1));						
						totalPairCount++;
						kppl.items.put(pair.sourceSP, pair);
					} else if (pair.distanceToNearest > nnlst.getDistanceToTarget(0)) {
						pair.targetSP = kpT;
						pair.distanceToNearest = nnlst.getDistanceToTarget(0);
						pair.distanceToNearest2 = nnlst.getDistanceToTarget(1);
					}
				}
/*
				synchronized (kppl) {
 					boolean duplicated = false;
					for (KeyPointPair i : kppl.items.values()) {
						if (i.sourceSP == kpS) {
							if (i.distanceToNearest > nnlst.getDistanceToTarget(0)) {
								i.targetSP = kpT;
								i.distanceToNearest = nnlst.getDistanceToTarget(0);
								i.distanceToNearest2 = nnlst.getDistanceToTarget(1);
								duplicated = true;
							}
						}
						if (i.targetSP == kpT) {
							if (i.distanceToNearest > nnlst.getDistanceToTarget(0)) {
								i.sourceSP = kpS;
								i.distanceToNearest = nnlst.getDistanceToTarget(0);
								i.distanceToNearest2 = nnlst.getDistanceToTarget(1);
								duplicated = true;
							}
						}
					}
					if (!duplicated) {
						KeyPointPair kpp = new KeyPointPair(kpS, kpT, nnlst.getDistanceToTarget(0), nnlst.getDistanceToTarget(1));
						totalPairCount++;
						kppl.items.put(kpp.sourceSP, kpp);
					}
				}
*/
			}
			int count = processed.incrementAndGet();
			SwtUtil.activeWaitDialogSetStatus("Processing " + 
					Integer.toString(count) + "/" + Integer.toString(tree.keyPointLists.size()), count);
			System.out.println(image.imageFileStamp.getFile().getAbsolutePath() + " (" + totalPairCount + ")");
			return null;
		}
	}
	
	public GenerateKeyPointPairsFromBigTree(KeyPointPairBigTree tree) {
		this.tree = tree;
	}

	public ArrayList<KeyPointPairList> call() throws Exception {
		Runtime runtime = Runtime.getRuntime();
		int numberOfProcessors = runtime.availableProcessors();

		ExecutorService exec = Executors.newFixedThreadPool(numberOfProcessors + 1);
		try {
			ArrayList<Future<?>> tasks = new ArrayList<Future<?>>(tree.keyPointLists.size());
			for (KeyPointList k : tree.keyPointLists) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				Future<?> f = exec.submit(new ProcessOneImage(k));
				tasks.add(f);
			}

			for (Future<?> task : tasks) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				task.get();
			}
		} finally {
			exec.shutdownNow();
		}

		ArrayList<KeyPointPairList> result = new ArrayList<KeyPointPairList>();
		for (KeyPointPairList k : keyPointPairLists.values()) {
			result.add(k);
		}
		return result;
	}
}
