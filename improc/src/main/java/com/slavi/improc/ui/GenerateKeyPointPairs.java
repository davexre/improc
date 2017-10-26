package com.slavi.improc.ui;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.util.concurrent.TaskSetExecutor;
import com.slavi.util.swt.SwtUtil;
import com.slavi.util.tree.NearestNeighbours;

public class GenerateKeyPointPairs implements Callable<ArrayList<KeyPointPairList>> {

	ExecutorService exec;
	
	ArrayList<KeyPointList> kpl;
	
	ArrayList<KeyPointPairList> result = new ArrayList<KeyPointPairList>();
	
	public GenerateKeyPointPairs(
			ExecutorService exec,
			ArrayList<KeyPointList> kpl) {
		this.exec = exec;
		this.kpl = kpl;
	}

	class MakePairs implements Callable<Void> {

		KeyPointList source;
		
		KeyPointList target;
		
		public MakePairs(KeyPointList l1, KeyPointList l2) {
			if (l1.imageId < l2.imageId) {
				source = l2;
				target = l1;
			} else {
				source = l1;
				target = l2;
			}				
		}
		
		public static final double maxDistanceToTarget = 110;
		public Void call() throws Exception {
			KeyPointPairList kppl = new KeyPointPairList();
			kppl.source = source;
			kppl.target = target;
			
			int searchSteps = target.tree.getTreeDepth() / 2;
			for (KeyPoint kp : source.items) {
				if (Thread.interrupted())
					throw new InterruptedException();
				NearestNeighbours<KeyPoint> nnlst = target.tree.getNearestNeighboursBBF(kp, 2, searchSteps);
//				NearestNeighbours<KeyPoint> nnlst = target.tree.getNearestNeighboursMyBBF(kp, 2, 
//						KeyPointBigTree.maxAbsoluteDiscrepancyPerCoordinate, searchSteps,
//						maxDistanceToTarget * maxDistanceToTarget);
				if (nnlst.getSize() < 2)
					continue;
				KeyPointPair pair = new KeyPointPair(kp, nnlst.getItem(0), nnlst.getDistanceToTarget(0), nnlst.getDistanceToTarget(1));						
				kppl.items.add(pair);
			}
			synchronized(result) {
				result.add(kppl);
			}
			SwtUtil.activeWaitDialogProgress(1); 
			return null;
		}
	}
		
	public ArrayList<KeyPointPairList> call() throws Exception {
		TaskSetExecutor taskSet = new TaskSetExecutor(exec);
		for (int i = 0; i < kpl.size(); i++) {
			KeyPointList imageI = kpl.get(i);
			for (int j = i + 1; j < kpl.size(); j++) {
				KeyPointList imageJ = kpl.get(j);
				taskSet.add(new MakePairs(imageI, imageJ));
			}
		}
		taskSet.addFinished();
		taskSet.get();
		
		for (KeyPointList i : kpl) {
			i.tree = null;
		}
		return result;
	}
}
