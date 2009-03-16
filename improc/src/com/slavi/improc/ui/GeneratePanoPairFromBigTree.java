package com.slavi.improc.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.PanoList;
import com.slavi.improc.PanoPair;
import com.slavi.improc.PanoPairList;
import com.slavi.improc.PanoPairTransformLerner;
import com.slavi.improc.PanoPairTransformer;
import com.slavi.math.MathUtil;
import com.slavi.util.ui.SwtUtil;

public class GeneratePanoPairFromBigTree implements Callable<PanoList>{

	ArrayList<KeyPointPairList> keyPointPairLists = new ArrayList<KeyPointPairList>();
	
	PanoList panoList = new PanoList();
	
	Map<String, PanoPairList> panoPairLists = new HashMap<String, PanoPairList>();
	
	AtomicInteger processed = new AtomicInteger(0);
	
	class ProcessOneKeyPointPairList implements Callable<Void> {
		KeyPointPairList kppl;
		
		public ProcessOneKeyPointPairList(KeyPointPairList kppl) {
			this.kppl = kppl;
		}

		void checkInterrupted() throws InterruptedException {
			if (Thread.interrupted())
				throw new InterruptedException();
		}
		
		void addPair(KeyPointPair kpp) {
			KeyPoint sourceSP = kpp.sourceSP;
			KeyPoint targetSP = kpp.targetSP;
			String sourceName = sourceSP.keyPointList.imageFileStamp.getFile().getAbsolutePath();
			String targetName = targetSP.keyPointList.imageFileStamp.getFile().getAbsolutePath();
			if (sourceName.compareTo(targetName) < 0) {
				KeyPoint tmp = sourceSP;
				sourceSP = targetSP;
				targetSP = tmp;
				String str = sourceName;
				sourceName = targetName;
				targetName = str;
			}
			String id = sourceName + ":" + targetName;
			PanoPairList ppl;
			synchronized(panoPairLists) {
				ppl = panoPairLists.get(id);
				if (ppl == null) {
					ppl = new PanoPairList();
					ppl.source = sourceSP.keyPointList;
					ppl.target = targetSP.keyPointList;
					ppl.sourceImage = kppl.source.imageFileStamp.getFile().getAbsolutePath();
					ppl.targetImage = kppl.target.imageFileStamp.getFile().getAbsolutePath();
					ppl.sourceImageSizeX = kppl.source.imageSizeX;
					ppl.sourceImageSizeY = kppl.source.imageSizeY;
					ppl.targetImageSizeX = kppl.target.imageSizeX;
					ppl.targetImageSizeY = kppl.target.imageSizeY;
					panoPairLists.put(id, ppl);
				}
			}
			PanoPair pair = new PanoPair();
			pair.sx = sourceSP.doubleX;
			pair.sy = sourceSP.doubleY;
			pair.tx = targetSP.doubleX;
			pair.ty = targetSP.doubleY;
			pair.discrepancy = kpp.discrepancy;
			pair.distance1 = kpp.distanceToNearest;
			pair.distance2 = kpp.distanceToNearest2;
			pair.list = ppl;
			
			synchronized(ppl) {
				ppl.items.add(pair);
			}
		}
		
		public Void call() throws Exception {
//			PanoPairList result = new PanoPairList();
//			result.items = new ArrayList<PanoPair>();

//			for (KeyPointPair pp : kppl.items.values()) {
//				if (pp.distanceToNearest > pp.distanceToNearest2 * 0.6) {
//					pp.setBad(true);
//				}	
//			}
//			PanoPairTransformLerner atl = new PanoPairTransformLerner(kppl.items.values());

			PanoPairTransformLerner atl = new PanoPairTransformLerner(kppl.items.values()) {
				public double getWeight(Entry<KeyPoint, KeyPoint> item) {
					KeyPointPair kp = (KeyPointPair) item;
					int nonzero = Math.min(kp.sourceSP.getNumberOfNonZero(), kp.targetSP.getNumberOfNonZero());
					if (nonzero == 0)
						nonzero = 1;
					int unmatched = kp.getUnmatchingCount();
					if (unmatched == 0)
						unmatched = 1;
					double w = (double) unmatched * kp.distanceToNearest / (double)nonzero;
					if (w < 1.0)
						w = 1.0;
					return 1.0 / w;
				}
			};

			checkInterrupted(); atl.calculateOne();
//			checkInterrupted(); atl.calculateOne();
			atl.useWeight = false;
//			checkInterrupted(); atl.calculateOne();
			checkInterrupted(); atl.calculateOne();
			
			double maxDiscrepancy = Math.min(kppl.target.imageSizeX, kppl.target.imageSizeY) * 0.005; // 0.5% of the size
			System.out.println("Max discrepancy for pano pair adjust = " + MathUtil.d4(maxDiscrepancy));
			if (maxDiscrepancy < 1.5)
				maxDiscrepancy = 1.5;
			kppl.leaveGoodElements(maxDiscrepancy);
//			checkInterrupted(); atl.calculateOne();
			checkInterrupted(); atl.calculateOne();

			for (KeyPointPair pp : kppl.items.values()) {
				if (pp.discrepancy <= maxDiscrepancy) {
//				if (!pp.isBad()) {
//					result.items.add(new PanoPair(pp));
					addPair(pp);
				}				
			}
//			result.transform = (PanoPairTransformer) atl.transformer;
//			result.sourceImage = kppl.source.imageFileStamp.getFile().getAbsolutePath();
//			result.targetImage = kppl.target.imageFileStamp.getFile().getAbsolutePath();
//			result.sourceImageSizeX = kppl.source.imageSizeX;
//			result.sourceImageSizeY = kppl.source.imageSizeY;
//			result.targetImageSizeX = kppl.target.imageSizeX;
//			result.targetImageSizeY = kppl.target.imageSizeY;
//
//			panoList.addItem(result);
			int count = processed.incrementAndGet();
			SwtUtil.activeWaitDialogSetStatus(null, count);
			
			System.out.println("Affine coefs=");
			System.out.println(((PanoPairTransformer)(atl.transformer)).affineCoefs);
			
			return null;
		}
	}
	
	public GeneratePanoPairFromBigTree(ArrayList<KeyPointPairList> keyPointPairLists) {
		this.keyPointPairLists = keyPointPairLists;
	}
	
	public PanoList call() throws Exception {
		Runtime runtime = Runtime.getRuntime();
		int numberOfProcessors = runtime.availableProcessors();

		ExecutorService exec = Executors.newFixedThreadPool(numberOfProcessors);
		try {
			ArrayList<Future<?>> tasks = new ArrayList<Future<?>>(keyPointPairLists.size());
			for (KeyPointPairList k : keyPointPairLists) {
				if (k.items.size() < 5) // TODO: Move it somewhere else?!? 
					continue;
				Future<?> task = exec.submit(new ProcessOneKeyPointPairList(k));
				tasks.add(task);
			}
			keyPointPairLists = null;
			for (Future<?> task : tasks) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				task.get();
			}
			tasks.clear();
		} finally {
			exec.shutdownNow();
		}
		panoList.items.addAll(panoPairLists.values());
		return panoList;
	}
}
