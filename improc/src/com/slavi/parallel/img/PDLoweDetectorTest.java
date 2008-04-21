package com.slavi.parallel.img;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.slavi.img.DImageMap;
import com.slavi.utils.Marker;

public class PDLoweDetectorTest {
//	static final String finName = "C:/Users/S/ImageProcess/images/HPIM7379.JPG";
//	static final String fouName = "C:/temp/test.jpg";

//	static final String finName = "D:/Users/s/Images/20071219 SAP Party in Exit club/DSC00325.JPG";
	static final String finName = "D:/Users/s/kayak/me in the kayak.jpg";
	static final String fouName = "D:/temp/test.jpg";

	public void doIt() throws IOException, InterruptedException {
		DImageMap src = new DImageMap(new File(finName));
		DImageMap dest = new DImageMap(src.getSizeX(), src.getSizeY());

		ExecutorService exec = Executors.newFixedThreadPool(6);
		
		Rectangle srcExt = src.getExtent();
		int gridX = 3;
		int gridY = 3;

		int dx = 1 + src.getSizeX() / gridX; 
		int dy = 1 + src.getSizeY() / gridY;

		DImageMap processMask = new DImageMap(src.getSizeX(), src.getSizeY());
		processMask.make0();
		PDLoweDetector.mask = processMask;
		
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
				DImageWrapper destW = new DImageWrapper(dest, destR);
				PDLoweDetector task = new PDLoweDetector(srcW, destR, 2, 3, exec);
//				task.hook = new Hook() {
//					public void keyPointCreated(KeyPoint scalePoint) {
//						
//					}
//				};
				tasks.add(task);
			}
		}
		Marker.mark("started");
		for (Runnable task : tasks) 
			exec.execute(task);
		exec.shutdown();
		while (!exec.isTerminated())
			exec.awaitTermination(1000, TimeUnit.SECONDS);
		Marker.release();
		
		PDLoweDetector.mask.setPixel(0, 0, 0.0);
		PDLoweDetector.mask.toImageFile(fouName);
//		dest.toImageFile(fouName);
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		new PDLoweDetectorTest().doIt();
	}
}
