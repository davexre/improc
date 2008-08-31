package com.slavi.improc.test;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.singletreaded.DLoweDetector.Hook;

public class TestPDLoweDetector {
	
	static final String finName = "C:/Users/S/ImageProcess/images/HPIM7379.JPG";
//	static final String fouName = "C:/temp/test.jpg";

//	static final String finName = "D:/Users/s/Images/20071219 SAP Party in Exit club/DSC00325.JPG";
//	static final String finName = "D:/Users/s/kayak/me in the kayak.jpg";
//	static final String fouName = "D:/temp/test.jpg";

	class DLoweHook implements Hook{
		KeyPointList spl;
		
		DLoweHook(KeyPointList spl) {
			this.spl = spl;
		}

		public synchronized void keyPointCreated(KeyPoint scalePoint) {
			scalePoint.keyPointList = spl;
			spl.kdtree.add(scalePoint);
		}
	}
	
	KeyPointList newDLowe;
	KeyPointList oldDLowe;

	public void doIt() {
//		DWindowedImage src = new PDImageMapBuffer(new File(finName));

		newDLowe = new KeyPointList();
//		Hook hook = new DLoweHook(newDLowe);
//		ExecutorService exec;
		
/*		///////////////////////////////////
		exec = Executors.newFixedThreadPool(2);
		Rectangle srcExt = src.getExtent();
		int gridX = 4;
		int gridY = 3;

		int dx = 1 + src.getSizeX() / gridX; 
		int dy = 1 + src.getSizeY() / gridY;

		ArrayList<Runnable>tasks = new ArrayList<Runnable>();
		for (int i = 0; i < gridX; i++) {
			for (int j = 0; j < gridY; j++) {
				int minx = i * dx;
				int miny = j * dy;
				int maxx = (i + 1) * dx;
				int maxy = (j + 1) * dy;
				
				Rectangle destR = new Rectangle(minx, miny, maxx - minx, maxy - miny);
				Rectangle srcR = PDLoweDetector.getNeededSourceExtent(destR);
				srcR = srcR.intersection(srcExt);
				destR = destR.intersection(srcExt);
				DImageWrapper srcW = new DImageWrapper(src, srcR);
				PDLoweDetector task = new PDLoweDetector(srcW, destR, 2, 3);
				task.hook = hook;
				tasks.add(task);
			}
		}
		
		System.gc();
		Marker.mark("started");
		for (Runnable task : tasks) 
			exec.execute(task);
		
		exec.shutdown();
		while (!exec.isTerminated())
			exec.awaitTermination(1000, TimeUnit.SECONDS);
		Marker.release();
		System.out.println(Utl.getFormatedMilliseconds(PDLoweDetector.timeElapsed.get()));
*/		
		///////////////////////////////////
/* TODO: FIXME:		ExecutionProfile profile = ExecutePDLowe.makeTasks(src, 1, hook);
		exec = Executors.newFixedThreadPool(profile.parallelTasks);
		System.out.println(profile);
		
		System.gc();
		Marker.mark("started");
//		for (Runnable task : profile.tasks)
//			exec.execute(task);
		exec.shutdown();
		while (!exec.isTerminated())
			exec.awaitTermination(1000, TimeUnit.SECONDS);
		Marker.release();
//		System.out.println(Utl.getFormatedMilliseconds(PDLoweDetector.timeElapsed.get()));
*/		
		/////////////////////////////////
/*		
		DLoweDetector d = new DLoweDetector();
		oldDLowe = new KeyPointList();
		hook = new DLoweHook(oldDLowe);
		d.hook = hook;
		System.gc();
		Marker.mark("started");
		d.DetectFeatures(src, 3, 32);
		Marker.release();
		
		/////////////////////////////////
		
//		System.out.println("COMPARING.");
//		
//		newDLowe.compareToList(oldDLowe);
	*/	
		System.out.println("DONE.");
		
	}

	public static void main(String[] args) {
		new TestPDLoweDetector().doIt();

//		Runtime runtime = Runtime.getRuntime();
//		MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
//		long maxMem = memoryUsage.getMax();
//		long usedMem = memoryUsage.getUsed();
//		long memoryAvailable = maxMem - usedMem;
//
//		long freeMem2 = runtime.freeMemory();
//		long maxMem2 = runtime.maxMemory();
//		long totalMem2 = runtime.totalMemory();
//		System.out.format("Max        %,15d\n", maxMem);
//		System.out.format("Used       %,15d\n", usedMem);
//		System.out.format("Available  %,15d\n", memoryAvailable);
//		System.out.println();
//		System.out.format("Free mem   %,15d\n", freeMem2);
//		System.out.format("Max mem    %,15d\n", maxMem2);
//		System.out.format("total mem  %,15d\n", totalMem2);
//		System.out.println();
//		System.out.format("deltaFree  %,15d\n", (memoryAvailable - freeMem2));
	}
}
