package com.slavi.parallel;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.slavi.img.DImageMap;
import com.slavi.utils.Marker;

public class ComputeDirection {

	static final String finName = "D:/Users/s/Images/20071219 SAP Party in Exit club/DSC00325.JPG";
	static final String fouName = "D:/temp/test.jpg";

	public void single() throws IOException {
		DImageMap src = new DImageMap(new File(finName));
		DImageMap dest = new DImageMap(src.getSizeX(), src.getSizeY());

		Marker.mark("original");
		src.computeDirection(dest);
		Marker.release();
//		for (int dummyLoop = 0; dummyLoop < 10; dummyLoop++) {
//			int sizeX = src.getSizeX();
//			int sizeY = src.getSizeY();
//			for (int i = sizeX - 2; i > 0; i--)
//				for (int j = sizeY - 2; j > 0; j--) {
//					// Direction is computed as d = atan2( dX, dY )
//					// The returned value of atan2 is from -pi to +pi.
//					dest.setPixel(i, j, Math.atan2(
//						src.getPixel(i, j + 1) - src.getPixel(i, j - 1), 
//						src.getPixel(i + 1, j) - src.getPixel(i - 1, j)));
//				}
//		}
//		long end = System.nanoTime();
//		System.out.println("Elapsed " + (end - start));
	}

	static class DirectionTask implements Runnable{
		
		int x1, x2, y1, y2;
		DImageMap src, dest;
		
		public static int getBorderZone() {
			return 1;
		}
		
		public void run() {
			try {
				for (int i = x2 - 2; i > x1; i--)
					for (int j = y2 - 2; j > y1; j--) {
						// Direction is computed as d = atan2( dX, dY )
						// The returned value of atan2 is from -pi to +pi.
						dest.setPixel(i, j, Math.atan2(
							src.getPixel(i, j + 1) - src.getPixel(i, j - 1), 
							src.getPixel(i + 1, j) - src.getPixel(i - 1, j)));
					}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}		
	}
		
	public void multiple() throws IOException, InterruptedException {
		DImageMap src = new DImageMap(new File(finName));
		DImageMap dest = new DImageMap(src.getSizeX(), src.getSizeY());
		int gridX = 3;
		int gridY = 3;
		ExecutorService exec = Executors.newFixedThreadPool(6);
		int dx = src.getSizeX() / gridX; 
		int dy = src.getSizeY() / gridY;

//		long start = System.nanoTime();
		Marker.mark("parallel");
		for (int dummyLoop = 0; dummyLoop < 10; dummyLoop++) {
			for (int i = 0; i < gridX; i++) {
				int x1 = i == 0 ? 0 : i * dx - DirectionTask.getBorderZone();
				int x2 = i == gridX - 1 ? src.getSizeX() : (i + 1) * dx + DirectionTask.getBorderZone();
				for (int j = 0; j < gridY; j++) {
					int y1 = j == 0 ? 0 : j * dy - DirectionTask.getBorderZone();
					int y2 = j == gridY - 1 ? src.getSizeY() : (j + 1) * dy + DirectionTask.getBorderZone();
					
					DirectionTask task = new DirectionTask();
					task.x1 = x1;
					task.x2 = x2;
					task.y1 = y1;
					task.y2 = y2;
					task.src = src;
					task.dest = dest;
					exec.execute(task);
				}
			}
		}
		exec.shutdown();
		while (!exec.isTerminated())
			exec.awaitTermination(1000, TimeUnit.SECONDS);
		Marker.release();
//		long end = System.nanoTime();
//		System.out.println("Elapsed " + (end - start));
		
//		dest.toImageFile(fouName);
	}
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		ComputeDirection test = new ComputeDirection();
		test.single();
		test.multiple();
	}
}
