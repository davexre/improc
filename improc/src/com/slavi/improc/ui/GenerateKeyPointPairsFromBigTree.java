package com.slavi.improc.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointBigTree;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.util.Marker;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.tree.KDTree;
import com.slavi.util.ui.SwtUtil;

public class GenerateKeyPointPairsFromBigTree implements Callable<ArrayList<KeyPointPairList>> {

	ExecutorService exec;
	
	KeyPointBigTree tree;
	
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
			String strImageId = Integer.toString(image.imageId);
			
			int totalPairCount = 0;
			for (KeyPoint kp : image.items) {
				if (Thread.interrupted())
					throw new InterruptedException();
				
//				KDTree.NearestNeighbours<KeyPoint> nnlst = tree.getNearestNeighboursMyBBF(kp, 2, KeyPointBigTree.maxAbsoluteDiscrepancyPerCoordinate, searchSteps);
				KDTree.NearestNeighbours<KeyPoint> nnlst = tree.getNearestNeighboursBBF(kp, 2, searchSteps);
//				KDTree.NearestNeighbours<KeyPoint> nnlst = tree.getNearestNeighbours(kp, 2);
				if (nnlst.size() < 2)
					continue;
//				if (nnlst.getDistanceToTarget(0) > nnlst.getDistanceToTarget(1) * 0.6) {
//					continue;
//				}
				KeyPoint kp2 = nnlst.getItem(0);
				String pairId;
				KeyPoint kpS, kpT;
				if (image.imageId > kp2.keyPointList.imageId) {
					pairId = strImageId + "-" + Integer.toString(kp2.keyPointList.imageId);
					kpS = kp;
					kpT = kp2;
				} else {
					pairId = Integer.toString(kp2.keyPointList.imageId) + "-" + strImageId;
					kpS = kp2;
					kpT = kp;
				}
				
				KeyPointPairList kppl = getKeyPointPairList(pairId, kpS.keyPointList, kpT.keyPointList);
				KeyPointPair pair = new KeyPointPair(kpS, kpT, nnlst.getDistanceToTarget(0), nnlst.getDistanceToTarget(1));						
				totalPairCount++;
				synchronized (kppl) {
					kppl.items.add(pair);
				}
			}
			int count = processed.incrementAndGet();
			SwtUtil.activeWaitDialogSetStatus("Processing " + 
					Integer.toString(count) + "/" + Integer.toString(tree.keyPointLists.size()), count);
			System.out.println(image.imageFileStamp.getFile().getAbsolutePath() + " (" + totalPairCount + ")");
			return null;
		}
	}
	
	public GenerateKeyPointPairsFromBigTree(
			ExecutorService exec,
			KeyPointBigTree tree) {
		this.exec = exec;
		this.tree = tree;
	}

	public ArrayList<KeyPointPairList> call() throws Exception {
		ArrayList<Future<?>> tasks = new ArrayList<Future<?>>(tree.keyPointLists.size());
		Marker.mark("\n\n***************");
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
		Marker.release();
		ArrayList<KeyPointPairList> result = new ArrayList<KeyPointPairList>();
		for (KeyPointPairList k : keyPointPairLists.values()) {
			result.add(k);
		}
		return result;
	}
}
