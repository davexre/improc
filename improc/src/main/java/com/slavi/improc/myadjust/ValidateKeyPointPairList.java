package com.slavi.improc.myadjust;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.math.AbstractConvexHullArea;
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
//			double dist = Math.sqrt(pair.distanceToNearest);
			double dist = pair.distanceToNearest;
			pair.weight = dist < 1 ? 1.0 : 1.0 / dist;
		}

		KeyPointHelmertTransformLearner learner = new KeyPointHelmertTransformLearner(pairList);
		int goodCount = 0;
		TransformLearnerResult res = null;
		for (int i = 0; i < 100; i++) {
			if (Thread.currentThread().isInterrupted())
				throw new InterruptedException();
			res = learner.calculateOne();
//			System.out.println("------Validate KeyPointPairList ------------");
//			System.out.println(res);
			goodCount = res.newGoodCount;
			if (res.isAdjusted() || (goodCount < minRequredGoodPointPairs)) {
				break;
			}
		}
		for (KeyPointPair pair : pairList.items) {
			pair.panoDiscrepancy = pair.discrepancy;
			pair.weight = pair.discrepancy < 1 ? 1.0 : 1 / pair.discrepancy;
		}

		KeyPointHelmertTransformer tr = (KeyPointHelmertTransformer) learner.transformer;
		double params[] = new double[4];
		tr.getParams(params);
		pairList.scale = params[0];
		pairList.angle = params[1];
		pairList.translateX= params[2];
		pairList.translateY= params[3];
		System.out.printf("%11s\t%s\t%s\n", (goodCount + "/" + pairList.items.size()),
				pairList.source.imageFileStamp.getFile().getName(),
				pairList.target.imageFileStamp.getFile().getName() +
				"\tangle=" +MathUtil.rad2degStr(pairList.angle) +
				"\tscale=" +MathUtil.d4(pairList.scale) +
				"\tdX=" +MathUtil.d4(pairList.translateX) +
				"\tdY=" +MathUtil.d4(pairList.translateY)
				);
		if ((!res.isAdjusted()) || (goodCount < minRequredGoodPointPairs)) {
//			System.out.println("NOT adjusted");
//			System.out.println(res);
			return false;
		}
//		for (KeyPointPair pair : pairList.items) {
//			pair.weight = pair.discrepancy < 1.0 ? 1.0 : 1.0 / pair.discrepancy;
//		}
		if ((pairList.scale < 0.2) || (pairList.scale > 20)) {
			// The scale parameter can get very close to 0. It happens when no real match
			// is possible between two images, but when scale is close to 0 a false match
			// is reported.
//			System.out.println("BAD scale");
//			System.out.println(res);
			return false;
		}

		return true;
	}

	AtomicInteger processedPairsList = new AtomicInteger(0);

	private class ValidateOne extends AbstractConvexHullArea implements Callable<Void> {
		KeyPointPairList pairList;
		int curPoint;
		boolean calcSourceArea;

		public ValidateOne(KeyPointPairList pairList) {
			this.pairList = pairList;
		}

		private boolean checkAreaRatio(boolean calcSourceArea) {
			this.calcSourceArea = calcSourceArea;
			double convexHullArea = Math.abs(getConvexHullArea());
			double imageArea = pairList.source.imageSizeX * pairList.source.imageSizeY;
			double ratio = convexHullArea / imageArea;
			if (ratio <= 0.05)
				System.out.println("RATIO " + calcSourceArea + "\t" +
					pairList.source.imageFileStamp.getFile().getName() + "\t" +
					pairList.target.imageFileStamp.getFile().getName() + "\t" +
					MathUtil.d4(convexHullArea) + "\t" +
					MathUtil.d4(imageArea) + "\t" +
					MathUtil.d4(ratio) + "\t"
					);
//			for (KeyPointPair pair : pairList.items) {
//				pair.weight = ratio;
//			}
			return ratio > 0.05;
		}

		public Void call() throws Exception {
			if (validateKeyPointPairList(pairList)) {
//				&& checkAreaRatio(true) && checkAreaRatio(false))
				checkAreaRatio(true);
				checkAreaRatio(false);
				calcSourceArea = true;
//				double sourceConvexHullArea = Math.abs(getConvexHullArea());
//				double sourceImageArea = pairList.source.imageSizeX * pairList.source.imageSizeY;
//				double sourceRatio = sourceConvexHullArea / sourceImageArea;

				calcSourceArea = false;
//				double targetConvexHullArea = Math.abs(getConvexHullArea());
//				double targetImageArea = pairList.target.imageSizeX * pairList.target.imageSizeY;
//				double targetRatio = targetConvexHullArea / targetImageArea;

//				if (sourceRatio <= 0.05 || targetRatio <= 0.05) {
/*					System.out.println("RATIO " +
						pairList.source.imageFileStamp.getFile().getName() + "\t" +
						pairList.target.imageFileStamp.getFile().getName() + "\t" +
						MathUtil.d2(sourceConvexHullArea) + "\t" +
						MathUtil.d2(sourceImageArea) + "\t" +
						MathUtil.d2(sourceRatio) + "\t|" +
						MathUtil.d2(targetConvexHullArea) + "\t" +
						MathUtil.d2(targetImageArea) + "\t" +
						MathUtil.d2(targetRatio) + "\t"
						);*/
//				} else {
					synchronized (result) {
						result.add(pairList);
					}
//				}
			}

			int curCount = processedPairsList.incrementAndGet();
			String status = "Processing " + Integer.toString(curCount) + "/" + Integer.toString(kppl.size());
			SwtUtil.activeWaitDialogSetStatus(status, curCount);
			return null;
		}

		public void resetPointIterator() {
			curPoint = -1;
		}

		public boolean nextPoint() {
			while (true) {
				curPoint++;
				if (curPoint >= pairList.items.size())
					return false;
				KeyPointPair pair = pairList.items.get(curPoint);
				if (!pair.validatePairBad)
					return true;
			}
		}

		public double getX() {
			KeyPointPair pair = pairList.items.get(curPoint);
			return calcSourceArea ? pair.sourceSP.getDoubleX() : pair.targetSP.getDoubleX();
		}

		public double getY() {
			KeyPointPair pair = pairList.items.get(curPoint);
			return calcSourceArea ? pair.sourceSP.getDoubleY() : pair.targetSP.getDoubleY();
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
/*
		System.out.println("---------------");


		//////////////////////////////////
		for (KeyPointList image : targets) {
			image.imageSpaceTree = null;
		}
		// Revalidate key point pairs
		kppl.clear();
		kppl.addAll(result);
		result.clear();
		taskSet = new TaskSetExecutor(exec);
		for (KeyPointPairList pairList : kppl) {
			taskSet.add(new ValidateOne(pairList));
		}
		taskSet.addFinished();
		taskSet.get();
*/
/*
		// Generate image discrepancies

		for (KeyPointPairList pairList : result) {
			String fou = "c:/temp/" + pairList.source.imageFileStamp.getFile().getName() + "-" +
				pairList.target.imageFileStamp.getFile().getName() + ".png";
			SafeImage im = new SafeImage(new FileInputStream(pairList.target.imageFileStamp.getFile()));
			BufferedImage bi = new BufferedImage(pairList.target.imageSizeX, pairList.target.imageSizeY, BufferedImage.TYPE_INT_RGB);
			for (int i = 0; i < bi.getWidth(); i++)
				for (int j = 0; j < bi.getHeight(); j++) {
					int color = im.getRGB(i, j);
					int grayColor = DWindowedImageUtils.getGrayColor(color) & 0xff;
					bi.setRGB(i, j, grayColor);
				}
			KeyPointHelmertTransformer tr = new KeyPointHelmertTransformer();
			tr.setParams(pairList.scale, pairList.angle, pairList.translateX, pairList.translateY);
			KeyPoint tmpKP = new KeyPoint();
			tmpKP.keyPointList = pairList.target;
			Graphics gr = bi.getGraphics();
			gr.setColor(Color.yellow);
			for (KeyPointPair kpp : pairList.items) {
				if (kpp.validatePairBad)
					continue;
				tr.transform(kpp.sourceSP, tmpKP);
				gr.drawLine((int)kpp.targetSP.doubleX, (int)kpp.targetSP.doubleY, (int)tmpKP.doubleX, (int)tmpKP.doubleY);
			}
			ImageIO.write(bi, "png", new File(fou));
		}
		throw new Exception("Done");*/
		return result;
	}
}
