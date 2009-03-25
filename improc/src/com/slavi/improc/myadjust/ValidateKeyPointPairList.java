package com.slavi.improc.myadjust;

import java.util.concurrent.Callable;

import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.math.MathUtil;

public class ValidateKeyPointPairList implements Callable<Void> {

	KeyPointPairList kppl;
	
	public ValidateKeyPointPairList(KeyPointPairList kppl) {
		this.kppl = kppl;
	}
	
	public static void calcRotationsUsingHelmert(
			KeyPointHelmertTransformer tr,
			KeyPointPairList kppl) {
		kppl.scale = Math.sqrt(tr.a * tr.a + tr.b * tr.b);
		double angle = Math.acos(tr.a / kppl.scale);

		double f = kppl.scale * kppl.source.scaleZ;
		double c = tr.c * kppl.source.cameraScale;
		double d = tr.d * kppl.source.cameraScale;
		double f1f1 = f * f + c * c;
		double f1 = Math.sqrt(f1f1);
		double f2 = Math.sqrt(f1f1 + d * d);

		kppl.rx = Math.atan2(c, f);
		kppl.ry = Math.atan2(d, f1);
		kppl.rz = Math.atan2(Math.tan(angle) * f1f1, f * f2);
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
	public Void call() throws Exception {
		for (KeyPointPair pair : kppl.items.values()) {
			pair.weight = pair.distanceToNearest < 1 ? 1.0 : 10 / pair.distanceToNearest;
		}		
		KeyPointHelmertTransformLearner learner = new KeyPointHelmertTransformLearner(kppl.items.values());
		boolean res = false;
		learner.calculateOne();
		learner.calculateOne();
		kppl.leaveGoodElements(200);
		for (int i = 0; i < 2; i++) {
			res = learner.calculateOne();
			if (res) {
				break;
			}
		}
		
		KeyPointHelmertTransformer tr = (KeyPointHelmertTransformer) learner.transformer;
		calcRotationsUsingHelmert(tr, kppl);
		
		System.out.println(kppl.getGoodCount() + "/" + kppl.items.size() + "\t" +
				kppl.source.imageFileStamp.getFile().getName() + "\t" + 
				kppl.target.imageFileStamp.getFile().getName() + "\t" +
				MathUtil.d4(kppl.scale) + "\t" +
				MathUtil.d4(kppl.rx * MathUtil.rad2deg) + "\t" +
				MathUtil.d4(kppl.ry * MathUtil.rad2deg) + "\t" +
				MathUtil.d4(kppl.rz * MathUtil.rad2deg) + "\t"
				);

		return null;
	}
}
