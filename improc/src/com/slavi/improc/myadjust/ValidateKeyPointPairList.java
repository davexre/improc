package com.slavi.improc.myadjust;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.math.MathUtil;
import com.slavi.math.transform.TransformLearnerResult;
import com.slavi.util.concurrent.TaskSetExecutor;
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
	
	private class ProcessOne implements Callable<Void> {
		KeyPointPairList pairList;
		
		public ProcessOne(KeyPointPairList pairList) {
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

	ArrayList<KeyPointPairList> result = new ArrayList<KeyPointPairList>();
	
	public ArrayList<KeyPointPairList> call() throws Exception {
		TaskSetExecutor taskSet = new TaskSetExecutor(exec);
		for (KeyPointPairList pairList : kppl) {
			taskSet.add(new ProcessOne(pairList));
		}
		taskSet.addFinished();
		taskSet.get();
		return result;
	}
}
