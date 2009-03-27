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
import com.slavi.math.adjust.Statistics;

public class ValidateKeyPointPairList implements Callable<ArrayList<KeyPointPairList>> {

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

//		pairList.scale = Math.sqrt(tr.a * tr.a + tr.b * tr.b);
//		double angle = Math.acos(tr.a / pairList.scale);

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

	public static void calcRotationsUsingHelmertWRONG(
			KeyPointHelmertTransformer tr,
			KeyPointPairList pairList) {
		pairList.scale = Math.sqrt(tr.a * tr.a + tr.b * tr.b);
		double angle = Math.acos(tr.a / pairList.scale);

		double f = pairList.scale * pairList.source.scaleZ;
		double c = tr.c * pairList.source.cameraScale;
		double d = tr.d * pairList.source.cameraScale;
		double f1f1 = f * f + c * c;
		double f1 = Math.sqrt(f1f1);
		double f2 = Math.sqrt(f1f1 + d * d);

		pairList.rx = Math.atan2(c, f);
		pairList.ry = Math.atan2(d, f1);
		pairList.rz = Math.atan2(Math.tan(angle) * f1f1, f * f2);
	}

/*	public void calcRotationsUsingHelmertParams(
			double a, double b, double c, double d,
			double sourceCameraFocalDistance) {
		double scale = Math.sqrt(a * a + b * b);
		double angle = Math.acos(a / scale);
		double f = sourceCameraFocalDistance;
		
		double f1f1 = f * f + c * c;
		double f1 = Math.sqrt(f1f1);
		double f2 = Math.sqrt(f1f1 + d * d);
		
		double rx = Math.atan2(c, f);
		double ry = Math.atan2(d, f1);
		double rz = Math.atan2(Math.tan(angle) * f1f1, f * f2);
	}
*/

	public static boolean validateKeyPointPairList(KeyPointPairList pairList) {
		for (KeyPointPair pair : pairList.items) {
			pair.weight = pair.distanceToNearest < 1 ? 1.0 : 1 / pair.distanceToNearest;
//			System.out.println(
//					MathUtil.d4(pair.distanceToNearest) + "\t" + 
//					MathUtil.d4(pair.distanceToNearest2) + "\t" + 
//					MathUtil.d4(MyPanoPairTransformLearner3.getWeight(pair)));
//			int unmatching = pair.getUnmatchingCount();
//			pair.weight = 1.0 / (unmatching + 1);
//			pair.bad = unmatching > 10;
//
//			pair.weight = pair.distanceToNearest < 1 ? 1.0 : 10 / pair.distanceToNearest;
//			pair.weight = pair.weight < 1 ? 1 : 1/pair.weight;
//			pair.bad = pair.distanceToNearest > 1000;
//			pair.bad = pair.distanceToNearest > maxDist;
		}		

		KeyPointHelmertTransformLearner learner = new KeyPointHelmertTransformLearner(pairList.items);
		boolean res = false;
		for (int i = 0; i < 20; i++) {
			res = learner.calculateOne();
//			System.out.println("*** ITERATION " + i + " " + pairList.getGoodCount() + "/" + pairList.items.size() + " discr=" + learner.getMaxAllowedDiscrepancy());
//			System.out.println(learner.discrepancyStatistics.toString(Statistics.CStatMinMax));
//			System.out.println(res);
			if (res) {
				break;
			}
		}
		if (!res)
			return false;
//		double discrepancy = learner.getMaxAllowedDiscrepancy();
//		System.out.println("Max allowed discrepancy = " + discrepancy);
		
		KeyPointHelmertTransformer tr = (KeyPointHelmertTransformer) learner.transformer;
		calcRotationsUsingHelmert(tr, pairList);
		
		int goodCount = pairList.getGoodCount();
		System.out.println(goodCount + "/" + pairList.items.size() + "\t" +
				pairList.source.imageFileStamp.getFile().getName() + "\t" + 
				pairList.target.imageFileStamp.getFile().getName() + "\t" +
				MathUtil.d4(pairList.scale) + "\t" +
				MathUtil.d4(pairList.rx * MathUtil.rad2deg) + "\t" +
				MathUtil.d4(pairList.ry * MathUtil.rad2deg) + "\t" +
				MathUtil.d4(pairList.rz * MathUtil.rad2deg) + "\t"
				);
		if (goodCount < 10) {
			System.out.println("NOT ENOUGH GOOD POINT PAIRS");
		}
		return goodCount < 10;
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
			if (!res) {
				result.add(item.getKey());
			}
		}
		return result;
	}
}
