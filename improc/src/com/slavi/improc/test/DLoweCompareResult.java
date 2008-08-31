package com.slavi.improc.test;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.slavi.image.DWindowedImage;
import com.slavi.image.DWindowedImageUtils;
import com.slavi.image.PDImageMapBuffer;
import com.slavi.improc.DImageMap;
import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.parallel.ExecutePDLowe;
import com.slavi.improc.parallel.ExecutionProfile;
import com.slavi.improc.singletreaded.DLoweDetector;
import com.slavi.improc.singletreaded.DLoweDetector.Hook;
import com.slavi.util.Const;
import com.slavi.util.Marker;
import com.slavi.util.concurrent.SteppedParallelTaskExecutor;
import com.slavi.util.file.FindFileIterator;

public class DLoweCompareResult {

	static KeyPointList makeWithOldWorkingDetector(String fileName) throws IOException {
		File fin = new File(fileName);
		
		com.slavi.improc.working.DLoweDetector ld = new com.slavi.improc.working.DLoweDetector();
		com.slavi.improc.working.DImageMap img = new com.slavi.improc.working.DImageMap(fin);
		ld.DetectFeatures(img, 3, 32);
		
		KeyPointList result = new KeyPointList();
		result.imageSizeX = img.getSizeX();
		result.imageSizeY = img.getSizeY();
		for (int i = ld.scalePointList.points.size() - 1; i >= 0; i--) {
			com.slavi.improc.working.ScalePoint sp = ld.scalePointList.points.get(i);
			KeyPoint kp = new KeyPoint();

			kp.imgX = sp.imgX;
			kp.imgY = sp.imgY;
			kp.imgScale = sp.imgScale;
			kp.doubleX = sp.doubleX;
			kp.doubleY = sp.doubleY;
			kp.dogLevel = (int)sp.level;
			kp.adjS = sp.adjS;
			kp.kpScale = sp.kpScale;
			kp.degree = sp.degree;
			for (int i2 = 0; i2 < KeyPoint.descriptorSize; i2++) 
				for (int j2 = 0; j2 < KeyPoint.descriptorSize; j2++) 
					for (int k2 = 0; k2 < KeyPoint.numDirections; k2++) 
						kp.setItem(i2, j2, k2, (byte)sp.getItem(i2, j2, k2));
			kp.keyPointList = result;
			result.kdtree.add(kp);
		}
		return result;
	}

	static KeyPointList makeWithSingleThreadedDetector(String fileName) throws IOException {
		File fin = new File(fileName);
		
		DImageMap img = new DImageMap(fin);
		final KeyPointList result = new KeyPointList();
		result.imageSizeX = img.getSizeX();
		result.imageSizeY = img.getSizeY();

		DLoweDetector d = new DLoweDetector();
		Hook hook = new Hook() {
			public synchronized void keyPointCreated(KeyPoint scalePoint) {
				scalePoint.keyPointList = result;
				result.kdtree.add(scalePoint);
			}		
		};
		d.hook = hook;
		d.DetectFeatures(img, 3, 32);
//		d.DetectFeaturesInSingleLevel(img, 1, 3);
		return result;
	}
	
	static KeyPointList makeWithParallelDetector(String fileName) throws IOException, InterruptedException, ExecutionException  {
		File image = new File(fileName);
		
		DWindowedImage img = new PDImageMapBuffer(image);
		final KeyPointList result = new KeyPointList();
		result.imageSizeX = img.maxX() + 1;
		result.imageSizeY = img.maxY() + 1;

		Hook hook = new Hook() {
			public synchronized void keyPointCreated(KeyPoint scalePoint) {
				scalePoint.keyPointList = result;
				result.kdtree.add(scalePoint);
			}		
		};
		ExecutionProfile profile = ExecutionProfile.suggestExecutionProfile(img.getExtent());
		profile.destWindowSizeX /= 2;
		profile.destWindowSizeY /= 2;
		ExecutePDLowe execPDLowe = new ExecutePDLowe(img, hook, profile);

		ExecutorService exec;
//		exec = Executors.newSingleThreadExecutor();
		exec = Executors.newFixedThreadPool(profile.parallelTasks);
		Future<Void> ft = new SteppedParallelTaskExecutor<Void>(exec, 2, execPDLowe).start();
		try {
			ft.get();
		} finally {
			exec.shutdown();
		}		
		
//		int scale = 1;
//		int count = 0;
//		while (true) {
//			ExecutionProfile profile = PDLoweDetector.makeOneTaskProfile(img.getExtent());
//			PDLoweDetector.makeTasks(img, scale, hook, profile);
//			DWindowedImageUtils.toImageFile(img, "d:/temp/b" + (count++) + ".png");
//			ExecutionProfile profile = ExecutionProfile.suggestExecutionProfile(img.getExtent());
//			profile.destWindowSizeX = profile.destWindowSizeX / 2 + 1;
//			profile.destWindowSizeY = profile.destWindowSizeY / 2 + 1;
//			ExecutePDLowe.makeTasks(img, scale, hook, profile);
//			ExecutorService exec = Executors.newSingleThreadExecutor();
//			ExecutorService exec = Executors.newFixedThreadPool(profile.parallelTasks);
//			System.out.println(profile);
//			System.out.println("---------------------");
//			for (Runnable task : profile.tasks) {
//				System.out.println(task);
//				System.out.println();
//			}
//			for (Runnable task : profile.tasks) {
//				PDLoweDetector d = (PDLoweDetector)task;
//				System.out.println("running new task...");
//				task.run();
//			}
				
//			for (Runnable task : profile.tasks)
//				exec.execute(task);
//			exec.shutdown();
//			while (!exec.isTerminated()) {
//				try {
//					exec.awaitTermination(1000, TimeUnit.SECONDS);
//				} catch (InterruptedException e) {
//				}
//			}
//			DWindowedImageUtils. profile.nextLevelBlurredImage
//			DImageMap tmp = new DImageMap(img.getSizeX() >> 1, img.getSizeY() >> 1);
//			img.scaleHalf(tmp);
//			scale *= 2.0;
//			img = profile.nextLevelBlurredImage;
//			if (img.maxX() <= 32) 
//				break;
//		};
		return result;
	}
	
	public static boolean compareKeyPoints(KeyPoint kp1, KeyPoint kp2) {
		int multiply = 10000;
		if (
//			(kp2.imgX != kp1.imgX) || 
//			(kp2.imgY != kp1.imgY) ||  
//			((int)(kp2.dogLevel * multiply) != (int)(kp1.dogLevel * multiply)) || 
//			((int)(kp2.degree * multiply) != (int)(kp1.degree * multiply)) ||
//			((int)(kp2.kpScale * multiply) != (int)(kp1.kpScale * multiply)) || 
			((int)(kp2.doubleX * multiply) != (int)(kp1.doubleX * multiply)) ||
			((int)(kp2.doubleY * multiply) != (int)(kp1.doubleY * multiply)) ||
			false
			)
			return false;
		for (int k = 0; k < KeyPoint.numDirections; k++) {
			for (int j = 0; j < KeyPoint.descriptorSize; j++) {
				for (int i = 0; i < KeyPoint.descriptorSize; i++) {
					if (kp2.getItem(i, j, k) != kp1.getItem(i, j, k))
						return false;
				}
			}
		}
		return true;
	}
	
	static void pinKeyPoint(DWindowedImage buf, KeyPoint kp) {
		int x = (int)kp.doubleX;
		int y = (int)kp.doubleY;
		if (x > buf.maxX())
			x = buf.maxX();
		if (y > buf.maxY())
			y = buf.maxY();
		if (x < buf.minX())
			x = buf.minX();
		if (y < buf.minY())
			y = buf.minY();
		buf.setPixel(x, y, 1.0);
	}
	
	public static boolean compare(KeyPointList kp1, KeyPointList kp2) throws IOException {
//		if (kp1.kdtree.getSize() != kp2.kdtree.getSize())
//			return false;
		
		ArrayList<KeyPoint> p1 = kp1.kdtree.toList();
		ArrayList<KeyPoint> p2 = kp2.kdtree.toList();

//		int count = 0;
//		for (DWindowedImage i : directionsPAR) {
//			DWindowedImageUtils.toImageFile(i, "d:/temp/a" + (count++) + ".png");
//		}
//		count = 0;
//		for (DImageMap i : directionsST) {
//			DImageWrapper wr = new DImageWrapper(i, i.getExtent());
//			DWindowedImageUtils.toImageFile(wr, "d:/temp/b" + (count++) + ".png");
//		}
//		System.out.println("------------------");
		
		PDImageMapBuffer buf = new PDImageMapBuffer(new Rectangle(kp1.imageSizeX, kp1.imageSizeY));
		PDImageMapBuffer buf2 = new PDImageMapBuffer(new Rectangle(kp1.imageSizeX, kp1.imageSizeY));
		
		boolean result = true;
		int totalMatch = 0;
		for (int i = p1.size() - 1; i >= 0; i--) {
			KeyPoint sp1 = p1.get(i);
			if (sp1.dogLevel == 1) 
				pinKeyPoint(buf2, sp1);
			
			boolean matchingFound = false;
			for (int j = p2.size() - 1; j >= 0; j--) {
				KeyPoint sp2 = p2.get(j);
				if (compareKeyPoints(sp1, sp2)) {
					if (matchingFound) {
						System.out.println("== DUPLICATED");
						System.out.println(sp1);
						System.out.println(sp2);
//						result = false; // Found a second one
					}
					matchingFound = true;
				}
			}
			if (matchingFound) {
				totalMatch++;
			} else
				result = false;
			if (!matchingFound) {
				System.out.println(sp1);
				if (sp1.dogLevel == 1) 
					pinKeyPoint(buf, sp1);
				result = false;
			}
		}

		System.out.print("Total matched " + totalMatch + " / " + p1.size() + "/" + p2.size());
		DWindowedImageUtils.toImageFile(buf, Const.workDir + "/points_0notMatched.png");
		DWindowedImageUtils.toImageFile(buf2, Const.workDir + "/points_1.png");

		buf = new PDImageMapBuffer(new Rectangle(kp1.imageSizeX, kp1.imageSizeY));
		for (int j = p2.size() - 1; j >= 0; j--) {
			KeyPoint sp2 = p2.get(j);
			if (sp2.dogLevel == 1) 
				pinKeyPoint(buf, sp2);
		}
		DWindowedImageUtils.toImageFile(buf, Const.workDir + "/points_2.png");
		
		return result;
	}
	
	public static boolean compare2(KeyPointList kp1, KeyPointList kp2) {
		ArrayList<KeyPoint> p1 = kp1.kdtree.toList();
		ArrayList<KeyPoint> p2 = kp2.kdtree.toList();

		for (int i = p1.size() - 1; i >= 0; i--) {
			KeyPoint sp1 = p1.get(i);
			boolean matchingFound = false;
			for (int j = p2.size() - 1; j >= 0; j--) {
				KeyPoint sp2 = p2.get(j);
				if (sp1.equals(sp2)) {
					if (matchingFound) {
//						System.out.println(sp1);
//						System.out.println(sp2);
//						return false; // Found a second one
					}
					matchingFound = true;
					p2.remove(j);
					break;
				}
			}
			if (matchingFound)
				return false;
		}

//		if (p2.size() != 0)
//			return false;
		return true;
	}

	public static void makeMap(KeyPointList kp, String fouName) throws IOException {
		PDImageMapBuffer buf = new PDImageMapBuffer(new Rectangle(kp.imageSizeX, kp.imageSizeY));
		for (KeyPoint sp : kp.kdtree) {
			if (sp.imgScale != 1)
				continue;
//			System.out.println("qqqq");
			buf.setPixel((int)sp.doubleX, (int)sp.doubleY, 1.0);
		}
		DWindowedImageUtils.toImageFile(buf, fouName);
	}
	
	static void doIt3(String fileName) throws Exception {
//		KeyPointList kp1 = makeWithOldDetector(fileName);
		KeyPointList kp1 = makeWithParallelDetector(fileName);
		KeyPointList.KeyPointTree kt = new KeyPointList.KeyPointTree();
		for (KeyPoint k : kp1.kdtree) {
			KeyPoint kp = kt.findMatching(k);
			if (kp == null) {
				kt.add(kp);
			} else {
				System.out.println("Duplicated " + kp.id);
			}
		}
	}
	
	static void doIt(String fileName) throws IOException, InterruptedException, ExecutionException {
		System.out.println("========= RUNNING NEW DETECTOR ==========");
		Marker.mark();
		KeyPointList kp1 = makeWithParallelDetector(fileName);
		Marker.release();

		System.out.println("========= RUNNING OLD DETECTOR ==========");
		Marker.mark();
//		KeyPointList kp2 = makeWithOldWorkingDetector(fileName);
		KeyPointList kp2 = makeWithSingleThreadedDetector(fileName);
		Marker.release();

		System.out.println("========= COMPARING RESULTS ==========");
		Marker.mark();
		makeMap(kp1, Const.workDir + "/asd1.png");
		makeMap(kp2, Const.workDir + "/asd2.png");
//		for (KeyPoint kp : kp1.kdtree) {
//			System.out.println(kp.imgScale + "\t" + kp.imgX + "\t" + kp.imgY + "\t" + kp.doubleX + "\t" + kp.doubleY);
//		}
//		System.out.println("---------------------");
//		for (KeyPoint kp : kp2.kdtree) {
//			System.out.println(kp.imgScale + "\t" + kp.imgX + "\t" + kp.imgY + "\t" + kp.doubleX + "\t" + kp.doubleY);
//		}
		
		ArrayList<KeyPoint> l1 = kp1.kdtree.toList();
		ArrayList<KeyPoint> l2 = kp2.kdtree.toList();
		Comparator<KeyPoint> c = new Comparator<KeyPoint>() {
			public int compare(KeyPoint o1, KeyPoint o2) {
				int result;
				result = Double.compare(o1.imgScale, o2.imgScale);
				if (result != 0) 
					return result;
				result = Double.compare(o1.doubleX, o2.doubleX);
				if (result != 0) 
					return result;
				result = Double.compare(o1.doubleY, o2.doubleY);
				if (result != 0) 
					return result;
				return 0;
			}
		};
		Collections.sort(l1, c);
		Collections.sort(l2, c);
		
		PrintStream out = new PrintStream(Const.workDir + "/comare1.txt");
		out.println("=================== LIST 1");
		for (KeyPoint kp : l1) out.println(kp);
		out.close();

		out = new PrintStream(Const.workDir + "/comare2.txt");
		out.println("=================== LIST 2");
		for (KeyPoint kp : l2) out.println(kp);
		out.close();
		
		boolean b = true;
		b = compare(kp1, kp2);
		System.out.println(b ? "ok" : "FAILED");
//		kp1.compareToList(kp2);
		Marker.release();
	}
	
	static void doIt2(String findFilePattern) throws Exception {
		FindFileIterator ff = FindFileIterator.makeWithWildcard(findFilePattern, true, true);
		int count = 0;
		for (File f = ff.next(); f != null; f = ff.next()) {
			String fileName = f.getAbsolutePath();
			System.out.print("Processing image (" + (count++) + ") " + fileName + " ");
//			KeyPointList kp1 = makeWithOldWorkingDetector(fileName);
			KeyPointList kp1 = makeWithParallelDetector(fileName);
			KeyPointList kp2 = makeWithSingleThreadedDetector(fileName);
			boolean b = compare(kp1, kp2);
			System.out.println(b ? " ok" : " FAILED");
		}
	}
	
	public static void main(String[] args) throws Exception {
//		doIt("D:/Users/s/Images/20080627 SAP Teambuilding v selo Chiflika (Troian)/2008-06-27/P1010199.JPG");
		doIt2(Const.smallImage);
//		doIt(Const.sourceImage);
//		doIt("D:/Users/s/Images/DSC_0237.JPG");
//		doIt3("D:/Users/s/Images/DSC_0237.JPG");
//		doIt2("D:/Users/s/Images/*.jpg");
//		doIt2("D:/Users/s/Images/DSC_0237.JPG");
//		doIt("D:/Users/s/Images/Airports in Bulgaria BG_AIR.jpg");
		System.out.println("DONE.");
	}
}
