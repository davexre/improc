package com.slavi.improc.ui;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.PanoList;
import com.slavi.improc.PanoPair;
import com.slavi.improc.PanoPairList;
import com.slavi.math.transform.AffineTransformLearner;
import com.slavi.math.transform.AffineTransformer;
import com.slavi.util.ui.SwtUtl;

public class GeneratePanoPairFromBigTree implements Callable<PanoList>{

	ArrayList<KeyPointPairList> keyPointPairLists = new ArrayList<KeyPointPairList>();
	
	PanoList panoList = new PanoList();
	
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
		
		public Void call() throws Exception {
			PanoPairList result = new PanoPairList();
			result.items = new ArrayList<PanoPair>();

			AffineTransformLearner atl = new AffineTransformLearner(2, 2, kppl.items);

			checkInterrupted(); atl.calculateOne();
			checkInterrupted(); atl.calculateOne();
			checkInterrupted(); atl.calculateOne();
			checkInterrupted(); atl.calculateOne();
			
			kppl.leaveGoodElements(9.0); // Math.min(image.sizex, image.sizeY) * 0.005; // 0.5% of the size
			checkInterrupted(); atl.calculateOne();
			checkInterrupted(); atl.calculateOne();

			for (KeyPointPair pp : kppl.items) {
				if (pp.getDiscrepancy() < 2.0) {
					result.items.add(new PanoPair(pp));
				}				
			}
			result.transform = (AffineTransformer) atl.transformer;
			result.sourceImage = kppl.source.imageFileStamp.getFile().getAbsolutePath();
			result.targetImage = kppl.target.imageFileStamp.getFile().getAbsolutePath();
			result.sourceImageSizeX = kppl.source.imageSizeX;
			result.sourceImageSizeY = kppl.source.imageSizeY;
			result.targetImageSizeX = kppl.target.imageSizeX;
			result.targetImageSizeY = kppl.target.imageSizeY;

			panoList.addItem(result);
			int count = processed.incrementAndGet();
			SwtUtl.activeWaitDialogSetStatus(null, count);
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

		ArrayList<Future<?>> tasks = new ArrayList<Future<?>>(keyPointPairLists.size());
		for (KeyPointPairList k : keyPointPairLists) {
			if (k.items.size() < 5) // TODO: Move it somewhere else?!? 
				continue;
			Future<?> task = exec.submit(new ProcessOneKeyPointPairList(k));
			tasks.add(task);
		}
		keyPointPairLists = null;
		try {
			for (Future<?> task : tasks) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				task.get();
			}
			tasks.clear();
		} catch (Exception e) {
			exec.shutdownNow();
			throw e;
		}

		exec.shutdown();
		return panoList;
	}
}
