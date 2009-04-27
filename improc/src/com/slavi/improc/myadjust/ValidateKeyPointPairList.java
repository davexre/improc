package com.slavi.improc.myadjust;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.math.MathUtil;

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
		boolean res = false;
		for (int i = 0; i < 20; i++) {
			res = learner.calculateOne();
			goodCount = pairList.getGoodCount();
/*			System.out.println("ITERATION " + i + 
					" " + goodCount + "/" + pairList.items.size() + 
					" discr=" + MathUtil.d4(learner.getMaxAllowedDiscrepancy()) + 
					" maxDiscr=" + MathUtil.d4(learner.discrepancyStatistics.getMaxX()));*/
			if (res || (goodCount < minRequredGoodPointPairs)) {
				break;
			}
		}
		if ((!res) || (goodCount < minRequredGoodPointPairs)) {
			System.out.println("FAILED " + goodCount + "/" + pairList.items.size() + "\t" +
					pairList.source.imageFileStamp.getFile().getName() + "\t" + 
					pairList.target.imageFileStamp.getFile().getName());
			return false;
		}

		for (KeyPointPair pair : pairList.items) {
			pair.weight = pair.discrepancy < 1 ? 1.0 : 1 / pair.discrepancy;
		}		
		
		KeyPointHelmertTransformer tr = (KeyPointHelmertTransformer) learner.transformer;
		calcRotationsUsingHelmert(tr, pairList);

		System.out.println(goodCount + "/" + pairList.items.size() + "\t" +
				pairList.source.imageFileStamp.getFile().getName() + "\t" + 
				pairList.target.imageFileStamp.getFile().getName() + "\t" +
				MathUtil.d4(pairList.scale) + "\t" +
				MathUtil.d4(pairList.rx * MathUtil.rad2deg) + "\t" +
				MathUtil.d4(pairList.ry * MathUtil.rad2deg) + "\t" +
				MathUtil.d4(pairList.rz * MathUtil.rad2deg) + "\t"
				);
		return true;
	}
	
	private class ProcessOne implements Callable<Boolean> {
		KeyPointPairList pairList;
		
		public ProcessOne(KeyPointPairList pairList) {
			this.pairList = pairList;
		}
		
		public Boolean call() throws Exception {
			return validateKeyPointPairList(pairList);
		}
	}
	
	public ArrayList<KeyPointPairList> call() throws Exception {
		ArrayList<KeyPointPairList> result = new ArrayList<KeyPointPairList>();

		HashMap<KeyPointPairList, Future<Boolean>> tasks = new HashMap<KeyPointPairList, Future<Boolean>>(kppl.size());
		for (KeyPointPairList pairList : kppl) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			Future<Boolean> f = exec.submit(new ProcessOne(pairList));
			tasks.put(pairList, f);
		}
	
		for (Map.Entry<KeyPointPairList, Future<Boolean>> item : tasks.entrySet()) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			boolean res = item.getValue().get();
			if (res) {
				result.add(item.getKey());
			}
		}
		return result;
	}
}
