package com.slavi.improc.myadjust;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.KeyPointTreeImageSpace;
import com.slavi.math.MathUtil;
import com.slavi.math.transform.TransformLearnerResult;
import com.slavi.util.concurrent.TaskSetExecutor;
import com.slavi.util.tree.KDTree.NearestNeighbours;
import com.slavi.util.ui.SwtUtil;

public class ValidateKeyPointPairList implements Callable<ArrayList<KeyPointPairList>> {

	public static int minRequredGoodPointPairs = 10;
	
	ExecutorService exec;
	
	ArrayList<KeyPointPairList> kppl;
	
	public ValidateKeyPointPairList(ExecutorService exec, ArrayList<KeyPointPairList> kppl) {
		this.exec = exec;
		this.kppl = kppl;
	}
	
	public static boolean validateKeyPointPairList(KeyPointPairList pairList) throws Exception {
		for (KeyPointPair pair : pairList.items) {
			pair.weight = pair.distanceToNearest < 1 ? 1.0 : 1 / pair.distanceToNearest;
		}		

		KeyPointHelmertTransformLearner learner = new KeyPointHelmertTransformLearner(pairList.items);
		int goodCount = 0;
		TransformLearnerResult res = null;
		for (int i = 0; i < 100; i++) {
			if (Thread.currentThread().isInterrupted())
				throw new InterruptedException();
			res = learner.calculateOne();
//			System.out.println("------Validate KeyPointPairList ------------");
//			System.out.println(res);
			goodCount = pairList.getGoodCount();
			if (res.isAdjusted() || (goodCount < minRequredGoodPointPairs)) {
				break;
			}
		}
		if ((!res.isAdjusted()) || (goodCount < minRequredGoodPointPairs)) {
			return false;
		}
		for (KeyPointPair pair : pairList.items) {
			pair.weight = pair.discrepancy < 1 ? 1.0 : 1 / pair.discrepancy;
/*			double dsx = pair.sourceSP.doubleX - pair.sourceSP.keyPointList.cameraOriginX;  
			double dsy = pair.sourceSP.doubleY - pair.sourceSP.keyPointList.cameraOriginY;
			double dtx = pair.sourceSP.doubleX - pair.sourceSP.keyPointList.cameraOriginX;  
			double dty = pair.sourceSP.doubleY - pair.sourceSP.keyPointList.cameraOriginY;
			pair.weight = Math.sqrt(dsx*dsx + dsy*dsy) / Math.max(pair.sourceSP.keyPointList.cameraOriginX, pair.sourceSP.keyPointList.cameraOriginY)
			+ Math.sqrt(dtx*dtx + dty*dty) / Math.max(pair.targetSP.keyPointList.cameraOriginX, pair.targetSP.keyPointList.cameraOriginY);
*/			
		}		
		KeyPointHelmertTransformer tr = (KeyPointHelmertTransformer) learner.transformer;
		double params[] = new double[4];
		tr.getParams(params);
		pairList.scale = params[0];
		pairList.angle = params[1];
		pairList.translateX= params[2];
		pairList.translateY= params[3];
		if ((int)(pairList.scale * 1000) == 0) {
//			throw new Error("ERROR");
			return false;
		}

		System.out.printf("%11s\t%s\t%s\n", (goodCount + "/" + pairList.items.size()),
				pairList.source.imageFileStamp.getFile().getName(),
				pairList.target.imageFileStamp.getFile().getName() +
				"\tangle=" +MathUtil.rad2degStr(pairList.angle) + 
				"\tscale=" +MathUtil.d4(pairList.scale) + 
				"\tdX=" +MathUtil.d4(pairList.translateX) + 
				"\tdY=" +MathUtil.d4(pairList.translateY) 
				);

		return true;
	}
	
	AtomicInteger processedPairsList = new AtomicInteger(0);
	
	private class ValidateOne implements Callable<Void> {
		KeyPointPairList pairList;
		
		public ValidateOne(KeyPointPairList pairList) {
			this.pairList = pairList;
		}
		
		public Void call() throws Exception {
			if (validateKeyPointPairList(pairList)) {
				synchronized (result) {
					result.add(pairList);
				}
			}
			int curCount = processedPairsList.incrementAndGet();
			String status = "Processing " + Integer.toString(curCount) + "/" + Integer.toString(kppl.size());
			SwtUtil.activeWaitDialogSetStatus(status, curCount);
			return null;
		}
	}

	private class GenerateImageSpaceTree implements Callable<Void> {
		KeyPointList image;
		
		public GenerateImageSpaceTree(KeyPointList image) {
			this.image = image;
		}
		
		public Void call() throws Exception {
			image.imageSpaceTree = new KeyPointTreeImageSpace();
			for (KeyPoint item : image.items) {
				if (Thread.currentThread().isInterrupted())
					throw new InterruptedException();
				image.imageSpaceTree.add(item);
			}
			return null;
		}
	}

	private class GenerateNewKeyPointPairs implements Callable<Void> {
		KeyPointPairList pairList;
		
		public GenerateNewKeyPointPairs(KeyPointPairList pairList) {
			this.pairList = pairList;
		}

		public Void call() throws Exception {
			pairList.items.clear();
			KeyPoint tmpKP = new KeyPoint();
			KeyPointHelmertTransformer tr = new KeyPointHelmertTransformer();
			tr.setParams(pairList.scale, pairList.angle, pairList.translateX, pairList.translateY);
			
			for (KeyPoint kp : pairList.source.items) {
				if (Thread.currentThread().isInterrupted())
					throw new InterruptedException();
				tr.transform(kp, tmpKP);
				NearestNeighbours<KeyPoint> nearest = pairList.target.imageSpaceTree.getNearestNeighboursMy(tmpKP, 20, 1000);
				double minDistance = Double.MAX_VALUE;
				KeyPoint target = null;
				for (int i = nearest.size() - 1; i >= 0; i--) {
					KeyPoint point = nearest.getItem(i);
					double distance = 0;
					for (int j = KeyPoint.featureVectorLinearSize - 1; j >= 0; j--) {
						double d = kp.getValue(j) - point.getValue(j);
						distance += d * d;
					}
					if (minDistance > distance) {
						minDistance = distance;
						target = point;
					}
				}
				if (target != null) {
					KeyPointPair pair = new KeyPointPair(kp, target, minDistance, minDistance);
					pairList.items.add(pair);
				}
			}
			return null;
		}
	}
	
	ArrayList<KeyPointPairList> result = new ArrayList<KeyPointPairList>();
	
	public ArrayList<KeyPointPairList> call() throws Exception {
		//////////////////////////////////
		TaskSetExecutor taskSet = new TaskSetExecutor(exec);
		for (KeyPointPairList pairList : kppl) {
			taskSet.add(new ValidateOne(pairList));
		}
		taskSet.addFinished();
		taskSet.get();

		System.out.println("---------------");
		
		//////////////////////////////////
		HashSet<KeyPointList> targets = new HashSet<KeyPointList>();
		for (KeyPointPairList pairList : result) {
			targets.add(pairList.target);
		}
		taskSet = new TaskSetExecutor(exec);
		for (KeyPointList image : targets) {
			taskSet.add(new GenerateImageSpaceTree(image));
		}
		taskSet.addFinished();
		taskSet.get();
		
		//////////////////////////////////
		taskSet = new TaskSetExecutor(exec);
		for (KeyPointPairList pairList : result) {
			taskSet.add(new GenerateNewKeyPointPairs(pairList));
		}
		taskSet.addFinished();
		taskSet.get();
		
		//////////////////////////////////
		kppl.clear();
		kppl.addAll(result);
		result.clear();
		taskSet = new TaskSetExecutor(exec);
		for (KeyPointPairList pairList : kppl) {
			taskSet.add(new ValidateOne(pairList));
		}
		taskSet.addFinished();
		taskSet.get();

		return result;
	}
}
