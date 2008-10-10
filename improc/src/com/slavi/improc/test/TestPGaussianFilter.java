package com.slavi.improc.test;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.slavi.image.DImageWrapper;
import com.slavi.improc.parallel.PFastGaussianFilter;
import com.slavi.improc.parallel.PGaussianFilter;
import com.slavi.improc.singletreaded.DGaussianFilter;
import com.slavi.improc.singletreaded.DImageMap;
import com.slavi.util.Marker;

public class TestPGaussianFilter {
//	static final String finName = "C:/Users/S/ImageProcess/images/HPIM7379.JPG";
//	static final String finName = "C:/Users/S/ImageProcess/images/output.png";
//	static final String fouName = "C:/temp/test.png";
//	static final String fouName1 = "C:/temp/test1.png";
//	static final String fouName2 = "C:/temp/test2.png";

//	static final String finName = "D:/Users/s/Images/20071219 SAP Party in Exit club/DSC00325.JPG";
	static final String finName = "D:/Users/s/kayak/me in the kayak.jpg";
	static final String fouName = "D:/temp/test.jpg";
	static final String fouName1 = "D:/temp/test1.jpg";
	static final String fouName2 = "D:/temp/test2.jpg";

	public void doIt() throws IOException, InterruptedException {
		DImageMap src = new DImageMap(new File(finName));
		DImageMap dest = new DImageMap(src.getSizeX(), src.getSizeY());
		DImageMap dest1 = new DImageMap(src.getSizeX(), src.getSizeY());

		DGaussianFilter gf = new DGaussianFilter();
		System.gc();
		Marker.mark("original");
		gf.applyGaussianFilter(src, dest1);
		Marker.release();
		dest1.toImageFile(fouName1);

		dest = new DImageMap(src.getSizeX(), src.getSizeY());

		ExecutorService exec = Executors.newFixedThreadPool(6);
		
		Rectangle srcExt = src.getExtent();
		int gridX = 10;
		int gridY = 10;

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
				Rectangle srcR = PGaussianFilter.getNeededSourceExtent(destR);
				srcR = srcR.intersection(srcExt);
				destR = destR.intersection(srcExt);
				final DImageWrapper srcW = new DImageWrapper(src, srcR);
				final DImageWrapper destW = new DImageWrapper(dest, destR);
				Runnable task = new Runnable() {
					public void run() {
						PFastGaussianFilter.applyFilter(srcW, destW);
					}
				};
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
		
		dest.toImageFile(fouName);
		
		// compare results.
		double maxDiff = 0.0;
		int atX = 0;
		int atY = 0;
		for (int i = dest.getSizeX() - 1; i >= 0; i--)
			for (int j = dest.getSizeY() - 1; j >= 0; j--) {
				double v = dest.getPixel(i, j);
				double v1 = dest1.getPixel(i, j);
				double diff = Math.abs(v - v1);
				dest.setPixel(i, j, diff * 10000000 > 1.0 ? 2.0 : 0.0);
				if (maxDiff < diff) {
					maxDiff = diff;
					atX = i;
					atY = j;
				}
			}
		System.out.println(maxDiff);
		System.out.println(atX);
		System.out.println(atY);
		for (int i = dest.getSizeX() - 1; i >= 0; i--) {
			dest.setPixel(i, 0, 1.0);
			dest.setPixel(i, dest.getSizeY() - 1, 1.0);
		}
		for (int j = dest.getSizeY() - 1; j >= 0; j--) {
			dest.setPixel(0, j, 1.0);
			dest.setPixel(dest.getSizeX() - 1, j, 1.0);
		}

//		dest.normalize();
		dest.toImageFile(fouName2);
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		new TestPGaussianFilter().doIt();
	}
}
