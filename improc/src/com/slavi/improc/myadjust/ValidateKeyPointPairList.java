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
	
	public static void calcRotationsUsingHelmert(
			KeyPointHelmertTransformer tr,
			KeyPointPairList pairList) {
		double params[] = new double[2];
		tr.getParams(params);
		pairList.scale = params[0];
		double angle = params[1];

		double f = pairList.scale * pairList.source.scaleZ;
		double c = tr.c * pairList.source.cameraScale;
		double d = tr.d * pairList.source.cameraScale;
		double f1f1 = f * f + d * d;
		double f1 = Math.sqrt(f1f1);
		double f2 = Math.sqrt(f1f1 + c * c);

		pairList.rx = Math.atan2(d, f);
		pairList.ry = Math.atan2(c, f1);
		pairList.rz = Math.atan2(Math.tan(angle) * f1f1, f * f2);
	}

	public static boolean validateKeyPointPairList(KeyPointPairList pairList) throws Exception {
		for (KeyPointPair pair : pairList.items) {
			pair.weight = pair.distanceToNearest < 1 ? 1.0 : 1 / pair.distanceToNearest;
		}		

		KeyPointHelmertTransformLearner learner = new KeyPointHelmertTransformLearner(pairList.items);
		int goodCount = 0;
		TransformLearnerResult res = null;
		for (int i = 0; i < 20; i++) {
			res = learner.calculateOne();
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
		calcRotationsUsingHelmert(tr, pairList);

		System.out.printf("%11s\t%s\t%s\n", (goodCount + "/" + pairList.items.size()),
				pairList.source.imageFileStamp.getFile().getName(),
				pairList.target.imageFileStamp.getFile().getName() +
				"\trx=" +MathUtil.d4(pairList.rx * MathUtil.rad2deg) + 
				"\try=" +MathUtil.d4(pairList.ry * MathUtil.rad2deg) + 
				"\trz=" + MathUtil.d4(pairList.rz * MathUtil.rad2deg) +
				"\ts=" + MathUtil.d4(pairList.scale)
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
