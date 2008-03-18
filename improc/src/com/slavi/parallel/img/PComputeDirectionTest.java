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

public class PComputeDirectionTest {
//	static final String finName = "C:/Users/S/ImageProcess/images/HPIM7379.JPG";
	static final String finName = "C:/Users/S/ImageProcess/images/output.png";
	static final String fouName = "C:/temp/test.jpg";
//	static final String finName = "D:/Users/s/Images/20071219 SAP Party in Exit club/DSC00325.JPG";
//	static final String finName = "D:/Users/s/kayak/me in the kayak.jpg";

	public static void main1(String[] args) {
		Rectangle r = new Rectangle(0, 0, 400, 500);
		System.out.println(r.contains(1, 1));
	}

	public void doIt() throws IOException, InterruptedException {
		DImageMap src = new DImageMap(new File(finName));
		DImageMap dest = new DImageMap(src.getSizeX(), src.getSizeY());

		Marker.mark("original");
		src.computeDirection(dest);
		Marker.release();

		ExecutorService exec = Executors.newFixedThreadPool(6);
		
		Rectangle srcExt = src.getExtent();
		int gridX = 3;
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
				Rectangle srcR = PComputeDirection.getNeededSourceExtent(destR);
				srcR = srcR.intersection(srcExt);
				destR = destR.intersection(srcExt);
				DImageWrapper srcW = new DImageWrapper(src, srcR);
				DImageWrapper destW = new DImageWrapper(dest, destR);
				Runnable task = new PComputeDirection(srcW, destW, false);
				tasks.add(task);
			}
		}
		Marker.mark("started");
		for (Runnable task : tasks) { 
			exec.submit(task);
		}
		exec.shutdown();
		while (!exec.isTerminated())
			exec.awaitTermination(1000, TimeUnit.SECONDS);
		Marker.release();
		
		dest.toImageFile(fouName);
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		new PComputeDirectionTest().doIt();
	}
}
