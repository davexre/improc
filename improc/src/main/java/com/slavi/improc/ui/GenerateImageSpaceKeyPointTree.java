package com.slavi.improc.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.KeyPointTreeImageSpace;
import com.slavi.improc.myadjust.KeyPointHelmertTransformer;
import com.slavi.util.concurrent.TaskSet;
import com.slavi.util.tree.NearestNeighbours;

public class GenerateImageSpaceKeyPointTree implements Callable<ArrayList<KeyPointPairList>> {

	ExecutorService exec;

	ArrayList<KeyPointPairList> kppl;

	public GenerateImageSpaceKeyPointTree(ExecutorService exec, ArrayList<KeyPointPairList> kppl) {
		this.exec = exec;
		this.kppl = kppl;
	}

	private class GenerateImageSpaceTree implements Runnable {
		KeyPointList image;

		public GenerateImageSpaceTree(KeyPointList image) {
			this.image = image;
		}

		public void run() {
			image.imageSpaceTree = new KeyPointTreeImageSpace();
			for (KeyPoint item : image.items) {
				if (Thread.currentThread().isInterrupted())
					throw new CompletionException(new InterruptedException());
				image.imageSpaceTree.add(item);
			}
		}
	}

	private class GenerateNewKeyPointPairs implements Runnable {
		KeyPointPairList pairList;

		public GenerateNewKeyPointPairs(KeyPointPairList pairList) {
			this.pairList = pairList;
		}

		public void run() {
			pairList.items.clear();
			KeyPoint tmpKP = new KeyPoint(pairList.target, 0, 0);
			KeyPointHelmertTransformer tr = new KeyPointHelmertTransformer();
			tr.setParams(pairList.scale, pairList.angle, pairList.translateX, pairList.translateY);

			for (KeyPoint kp : pairList.source.items) {
				if (Thread.currentThread().isInterrupted())
					throw new CompletionException(new InterruptedException());
				tr.transform(kp, tmpKP);
/*				NearestNeighbours<KeyPoint> nearest = pairList.target.imageSpaceTree.getNearestNeighbours(tmpKP, 1);
				KeyPoint target = nearest.size() > 0 ? nearest.getItem(0) : null;
				double minDistance = nearest.size() > 0 ? nearest.getDistanceToTarget(0) : Double.MAX_VALUE;
*/
				NearestNeighbours<KeyPoint> nearest = pairList.target.imageSpaceTree.getNearestNeighbours(tmpKP, 5);
//				NearestNeighbours<KeyPoint> nearest = pairList.target.imageSpaceTree.getNearestNeighboursMy(tmpKP, 20,
//					40 * KeyPointHelmertTransformLearner.discrepancyThreshold);
				double minDistance = Double.MAX_VALUE;
				KeyPoint target = null;
				for (int i = nearest.getSize() - 1; i >= 0; i--) {
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
		}
	}

	ArrayList<KeyPointPairList> result = new ArrayList<KeyPointPairList>();

	public ArrayList<KeyPointPairList> call() throws Exception {
		// Build 2D key point tree per target image
		HashSet<KeyPointList> targets = new HashSet<KeyPointList>();
		for (KeyPointPairList pairList : kppl) {
			targets.add(pairList.target);
		}
		TaskSet taskSet = new TaskSet(exec);
		for (KeyPointList image : targets) {
			taskSet.add(new GenerateImageSpaceTree(image));
		}
		taskSet.run().get();

		// Rebuild key point pairs
		taskSet = new TaskSet(exec);
		for (KeyPointPairList pairList : result) {
			taskSet.add(new GenerateNewKeyPointPairs(pairList));
		}
		taskSet.run().get();

		return null;
	}
}
