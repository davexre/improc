package com.slavi.improc.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointBigTree;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointListSaver;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.KeyPointTree;
import com.slavi.util.concurrent.TaskSetExecutor;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.tree.NearestNeighbours;
import com.slavi.util.ui.SwtUtil;

public class GenerateKeyPointPairs implements Callable<ArrayList<KeyPointPairList>> {

	ExecutorService exec;
	
	List<String> images;
	
	AbsoluteToRelativePathMaker imagesRoot;
	
	AbsoluteToRelativePathMaker keyPointFileRoot;
	
	ArrayList<KeyPointList> keyPointLists = new ArrayList<KeyPointList>();
	
	ArrayList<KeyPointPairList> result = new ArrayList<KeyPointPairList>();
	
	public GenerateKeyPointPairs(
			ExecutorService exec,
			List<String> images,
			AbsoluteToRelativePathMaker imagesRoot,
			AbsoluteToRelativePathMaker keyPointFileRoot) {
		this.exec = exec;
		this.images = images;
		this.imagesRoot = imagesRoot;
		this.keyPointFileRoot = keyPointFileRoot;
	}

	class ReadKeyPointLists implements Callable<Void> {

		String image;
		
		int imageId;
		
		public ReadKeyPointLists(String image, int imageId) {
			this.image = image;
			this.imageId = imageId;
		}
		
		public Void call() throws Exception {
			KeyPointList l = KeyPointListSaver.readKeyPointFile(exec, imagesRoot, keyPointFileRoot, new File(image));
			l.imageId = imageId;
			l.tree = new KeyPointTree();
			for (KeyPoint kp : l.items) {
				if (Thread.currentThread().isInterrupted())
					throw new InterruptedException();
				l.tree.add(kp);
			}
			
			synchronized (keyPointLists) {
				keyPointLists.add(l);
				String statusMessage = (keyPointLists.size()) + "/" + images.size() + " " + image;
				System.out.println(statusMessage);
				SwtUtil.activeWaitDialogSetStatus(statusMessage, keyPointLists.size() - 1);
			}
			return null;
		}
	}

	class MakePairs implements Callable<Void> {

		KeyPointList source;
		
		KeyPointList target;
		
		public MakePairs(KeyPointList l1, KeyPointList l2) {
			if (l1.items.size() > l2.items.size()) {
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
//			int count = processed.incrementAndGet();
//			SwtUtil.activeWaitDialogSetStatus("Processing " + 
//					Integer.toString(count) + "/" + Integer.toString(tree.keyPointLists.size()), count);
			return null;
		}
	}
		
	public ArrayList<KeyPointPairList> call() throws Exception {
		TaskSetExecutor taskSet = new TaskSetExecutor(exec);
		for (int imageId = 0; imageId < images.size(); imageId++) {
			String image = images.get(imageId);
			taskSet.add(new ReadKeyPointLists(image, imageId));
		}
		taskSet.addFinished();
		taskSet.get();

		taskSet = new TaskSetExecutor(exec);
		for (int i = 0; i < keyPointLists.size(); i++) {
			KeyPointList imageI = keyPointLists.get(i);
			for (int j = i + 1; j < keyPointLists.size(); j++) {
				KeyPointList imageJ = keyPointLists.get(j);
				taskSet.add(new MakePairs(imageI, imageJ));
			}
		}
		taskSet.addFinished();
		taskSet.get();
		
		for (KeyPointList i : keyPointLists) {
			i.tree = null;
		}
		return result;
	}
}
