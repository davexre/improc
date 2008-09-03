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
import com.slavi.util.ui.SwtUtl;

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
		
		int skipped = 0;
		
		public ProcessOneImage(KeyPointList image) {
			this.image = image;
		}
		
		public Void call() throws Exception {
//			int searchSteps = (int) (Math.max(130.0, (Math.log(tree.getSize()) / Math.log (1000.0)) * 130.0));
			int searchSteps = tree.getTreeDepth() * 2;
			int imageId = image.hashCode();
			String strId = Integer.toString(imageId);
			
			for (KeyPoint kp : image) {
				if (Thread.interrupted())
					throw new InterruptedException();
				
				KDTree.NearestNeighbours<KeyPoint> nnlst = tree.getNearestNeighboursBBF(kp, 2, searchSteps);
				if (nnlst.size() < 2)
					continue;
//				if (nnlst.getDistanceToTarget(0) > nnlst.getDistanceToTarget(1) * 0.6) {
				if (nnlst.getDistanceToTarget(0) * 2.0 > nnlst.getDistanceToTarget(1)) {
					skipped++;
					continue;
				}
				KeyPoint kp2 = nnlst.getItem(0);
				int destImageId = kp2.keyPointList.hashCode();
				String pairId;
				KeyPoint kpS, kpT;
				
				if (imageId > destImageId) {
					pairId = strId + "-" + Integer.toString(destImageId);
					kpS = kp;
					kpT = kp2;
				} else {
					pairId = Integer.toString(destImageId) + "-" + strId;
					kpS = kp2;
					kpT = kp;
				}
				
				KeyPointPairList kppl = getKeyPointPairList(pairId, kpS.keyPointList, kpT.keyPointList);
				synchronized (kppl) {
					boolean duplicated = false;
					for (KeyPointPair i : kppl.items) {
						if (i.sourceSP == kpS && i.targetSP == kpT) {
							duplicated = true;
							break;
						}
					}
					if (!duplicated) {
						KeyPointPair kpp = new KeyPointPair(kpS, kpT, nnlst.getDistanceToTarget(0), nnlst.getDistanceToTarget(1));
						kppl.items.add(kpp);
					}
				}
			}
			int count = processed.incrementAndGet();
			SwtUtl.activeWaitDialogSetStatus("Processing " + 
					Integer.toString(count) + "/" + Integer.toString(tree.keyPointLists.size()), count);
			System.out.println(image.imageFileStamp.getFile().getAbsolutePath() + " has skipped " + skipped + " points.");
			return null;
		}
	}
	
	public GenerateKeyPointPairsFromBigTree(KeyPointPairBigTree tree) {
		this.tree = tree;
	}

	public ArrayList<KeyPointPairList> call() throws Exception {
		Runtime runtime = Runtime.getRuntime();
		int numberOfProcessors = runtime.availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(numberOfProcessors);

		ArrayList<Future<?>> tasks = new ArrayList<Future<?>>(tree.keyPointLists.size());
		for (KeyPointList k : tree.keyPointLists) {
			if (Thread.interrupted()) {
				exec.shutdownNow();
				throw new InterruptedException();
			}
			Future<?> f = exec.submit(new ProcessOneImage(k));
			tasks.add(f);
		}

		try {
			for (Future<?> task : tasks) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				task.get();
			}
		} catch (Exception e) {
			exec.shutdownNow();
			throw e;
		}

		ArrayList<KeyPointPairList> result = new ArrayList<KeyPointPairList>();
		for (KeyPointPairList k : keyPointPairLists.values()) {
			result.add(k);
		}

		exec.shutdown();
		return result;
	}
}
