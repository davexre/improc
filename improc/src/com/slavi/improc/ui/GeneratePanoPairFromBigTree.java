package com.slavi.improc.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.slavi.improc.PanoPairTransformLerner;
import com.slavi.improc.PanoPairTransformer;
import com.slavi.math.MathUtil;
import com.slavi.util.ui.SwtUtil;

public class GeneratePanoPairFromBigTree implements Callable<PanoList>{

	ArrayList<KeyPointPairList> keyPointPairLists = new ArrayList<KeyPointPairList>();
	
	PanoList panoList = new PanoList();
	
	AtomicInteger processed = new AtomicInteger(0);

	private static class CompareByAngle implements Comparator<KeyPointPair> {
		public static final CompareByAngle instance = new CompareByAngle();

		public int compare(KeyPointPair kpp1, KeyPointPair kpp2) {
			return Double.compare(kpp1.angle, kpp2.angle);
		} 
	}

	class ProcessOneNew implements Callable<Void> {
		KeyPointPairList kppl;
		
		public ProcessOneNew(KeyPointPairList kppl) {
			this.kppl = kppl;
		}

		void checkInterrupted() throws InterruptedException {
			if (Thread.interrupted())
				throw new InterruptedException();
		}
		
		public Void call() throws Exception {
			int size = kppl.items.size(); 
			if (size < 5)
				return null;
			PanoPairList result = new PanoPairList();
			result.items = new ArrayList<PanoPair>();

			ArrayList<KeyPointPair>work = new ArrayList<KeyPointPair>(kppl.items.values());
			for (KeyPointPair pp : work) {
				pp.angle = Math.atan2(
						pp.targetSP.doubleY - pp.sourceSP.doubleY, 
						pp.targetSP.doubleX - pp.sourceSP.doubleX);
			}
			Collections.sort(work, CompareByAngle.instance);			
			KeyPointPair prev = work.get(size - 1);
			for (int i = 0; i < size; i++) {
				KeyPointPair pp = work.get(i);
				pp.d1 = MathUtil.fixAngle2PI(pp.angle - prev.angle);
			}
					
			
			
			PanoPairTransformLerner atl = new PanoPairTransformLerner(kppl.items.values());

			checkInterrupted(); atl.calculateOne();
			checkInterrupted(); atl.calculateOne();
			checkInterrupted(); atl.calculateOne();
			checkInterrupted(); atl.calculateOne();
			
			kppl.leaveGoodElements(9.0); // Math.min(image.sizex, image.sizeY) * 0.005; // 0.5% of the size
			checkInterrupted(); atl.calculateOne();
			checkInterrupted(); atl.calculateOne();

			for (KeyPointPair pp : kppl.items.values()) {
				if (pp.getDiscrepancy() < 2.0) {
					result.items.add(new PanoPair(pp));
				}				
			}
			result.transform = (PanoPairTransformer) atl.transformer;
			result.sourceImage = kppl.source.imageFileStamp.getFile().getAbsolutePath();
			result.targetImage = kppl.target.imageFileStamp.getFile().getAbsolutePath();
			result.sourceImageSizeX = kppl.source.imageSizeX;
			result.sourceImageSizeY = kppl.source.imageSizeY;
			result.targetImageSizeX = kppl.target.imageSizeX;
			result.targetImageSizeY = kppl.target.imageSizeY;

			panoList.addItem(result);
			int count = processed.incrementAndGet();
			SwtUtil.activeWaitDialogSetStatus(null, count);
			return null;
		}
	}
	
	/////////////////////////////////////////
	
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

			for (KeyPointPair pp : kppl.items.values()) {
				if (pp.distanceToNearest > pp.distanceToNearest2 * 0.6) {
					pp.setBad(true);
				}	
			}
			PanoPairTransformLerner atl = new PanoPairTransformLerner(kppl.items.values());

			checkInterrupted(); atl.calculateOne();
			checkInterrupted(); atl.calculateOne();
			checkInterrupted(); atl.calculateOne();
			checkInterrupted(); atl.calculateOne();
			
			kppl.leaveGoodElements(9.0); // Math.min(image.sizex, image.sizeY) * 0.005; // 0.5% of the size
			checkInterrupted(); atl.calculateOne();
			checkInterrupted(); atl.calculateOne();

			for (KeyPointPair pp : kppl.items.values()) {
				if (pp.getDiscrepancy() < 2.0) {
					result.items.add(new PanoPair(pp));
				}				
			}
			result.transform = (PanoPairTransformer) atl.transformer;
			result.sourceImage = kppl.source.imageFileStamp.getFile().getAbsolutePath();
			result.targetImage = kppl.target.imageFileStamp.getFile().getAbsolutePath();
			result.sourceImageSizeX = kppl.source.imageSizeX;
			result.sourceImageSizeY = kppl.source.imageSizeY;
			result.targetImageSizeX = kppl.target.imageSizeX;
			result.targetImageSizeY = kppl.target.imageSizeY;

			panoList.addItem(result);
			int count = processed.incrementAndGet();
			SwtUtil.activeWaitDialogSetStatus(null, count);
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
		return panoList;
	}
}
