package com.slavi.parallel.img.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.slavi.img.Const;
import com.slavi.img.DImageMap;
import com.slavi.img.DLoweDetector;
import com.slavi.img.KeyPoint;
import com.slavi.img.KeyPointList;
import com.slavi.img.DLoweDetector.Hook;
import com.slavi.img.KeyPointList.KeyPointListSaver;
import com.slavi.img.KeyPointList.KeyPointTree;
import com.slavi.parallel.img.PDLoweDetector;
import com.slavi.parallel.img.PDLoweDetector.ExecutionProfile;
import com.slavi.utils.FindFileIterator;
import com.slavi.utils.Marker;

public class DLoweCompareResult {

	static KeyPointList makeWithOldWorkingDetector(String fileName) throws IOException {
		File fin = new File(fileName);
		
		com.slavi.img.working.DLoweDetector ld = new com.slavi.img.working.DLoweDetector();
		com.slavi.img.working.DImageMap img = new com.slavi.img.working.DImageMap(fin);
		ld.DetectFeatures(img, 3, 32);
		
		KeyPointList result = new KeyPointList();
		result.imageSizeX = img.getSizeX();
		result.imageSizeY = img.getSizeY();
		for (int i = ld.scalePointList.points.size() - 1; i >= 0; i--) {
			com.slavi.img.working.ScalePoint sp = (com.slavi.img.working.ScalePoint) ld.scalePointList.points.get(i);
			KeyPoint kp = new KeyPoint();

			kp.imgX = sp.imgX;
			kp.imgY = sp.imgY;
			kp.imgScale = sp.imgScale;
			kp.doubleX = sp.doubleX;
			kp.doubleY = sp.doubleY;
			kp.level = sp.level;
			kp.adjS = sp.adjS;
			kp.kpScale = sp.kpScale;
			kp.degree = sp.degree;
			for (int i2 = 0; i2 < KeyPoint.descriptorSize; i2++) 
				for (int j2 = 0; j2 < KeyPoint.descriptorSize; j2++) 
					for (int k2 = 0; k2 < KeyPoint.numDirections; k2++) 
						kp.setItem(i2, j2, k2, (byte)sp.getItem(i2, j2, k2));
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
				result.kdtree.add(scalePoint);
			}		
		};
		d.hook = hook;
		d.DetectFeatures(img, 3, 32);
		return result;
	}
	
	static KeyPointList makeWithParallelDetector(String fileName) throws IOException  {
		File image = new File(fileName);
		
		DImageMap img = new DImageMap(image);
		final KeyPointList result = new KeyPointList();
		result.imageSizeX = img.getSizeX();
		result.imageSizeY = img.getSizeY();

		Hook hook = new Hook() {
			public synchronized void keyPointCreated(KeyPoint scalePoint) {
				result.kdtree.add(scalePoint);
			}		
		};
		
		double scale = 1.0;
		while (true) {
			ExecutionProfile profile = PDLoweDetector.makeOneTaskProfile(img.getExtent());
			PDLoweDetector.makeTasks(img, scale, hook, profile);
//			ExecutionProfile profile = PDLoweDetector.makeTasks(img, scale, hook);
//			ExecutorService exec = Executors.newSingleThreadExecutor();
//			ExecutorService exec = Executors.newFixedThreadPool(profile.parallelTasks);
//			System.out.println(profile);
//			System.out.println("---------------------");
//			for (Runnable task : profile.tasks) {
//				System.out.println(task);
//				System.out.println();
//			}
			for (Runnable task : profile.tasks)
				task.run();
				
//			for (Runnable task : profile.tasks)
//				exec.execute(task);
//			exec.shutdown();
//			while (!exec.isTerminated()) {
//				try {
//					exec.awaitTermination(1000, TimeUnit.SECONDS);
//				} catch (InterruptedException e) {
//				}
//			}
			DImageMap tmp = new DImageMap(img.getSizeX() >> 1, img.getSizeY() >> 1);
			img.scaleHalf(tmp);
			scale *= 2.0;
			img = tmp;
			if (img.getSizeX() / 2 <= 64) 
				break;
		};		
		return result;
	}
	
	public static boolean compare(KeyPointList kp1, KeyPointList kp2) {
//		if (kp1.kdtree.getSize() != kp2.kdtree.getSize())
//			return false;
		
		ArrayList p1 = kp1.kdtree.toList();
		ArrayList p2 = kp2.kdtree.toList();

		for (int i = p1.size() - 1; i >= 0; i--) {
			KeyPoint sp1 = (KeyPoint)p1.get(i);
			boolean matchingFound = false;
			for (int j = p2.size() - 1; j >= 0; j--) {
				KeyPoint sp2 = (KeyPoint)p2.get(j);
				if (sp1.equals(sp2)) {
					if (matchingFound) {
						System.out.println(sp1);
						System.out.println(sp2);
						return false; // Found a second one
					}
					matchingFound = true;
					p2.remove(j);
					break;
				}
			}
			if (!matchingFound) {
				System.out.println(sp1);
				System.out.println("--------------");
				new KeyPointList.KeyPointListSaver().toTextStream(kp2.kdtree, new PrintWriter(System.out));
				return false;
			}
		}

		if (p2.size() != 0)
			return false;
		return true;
	}
	
	public static boolean compare2(KeyPointList kp1, KeyPointList kp2) {
		ArrayList p1 = kp1.kdtree.toList();
		ArrayList p2 = kp2.kdtree.toList();

		for (int i = p1.size() - 1; i >= 0; i--) {
			KeyPoint sp1 = (KeyPoint)p1.get(i);
			boolean matchingFound = false;
			for (int j = p2.size() - 1; j >= 0; j--) {
				KeyPoint sp2 = (KeyPoint)p2.get(j);
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
	
	static void doIt3(String fileName) throws IOException {
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
	
	static void doIt(String fileName) throws IOException {
		System.out.println("========= RUNNING OLD DETECTOR ==========");
		Marker.mark();
//		KeyPointList kp1 = makeWithOldWorkingDetector(fileName);
		KeyPointList kp1 = makeWithSingleThreadedDetector(fileName);
		Marker.release();

		System.out.println("========= RUNNING NEW DETECTOR ==========");
		Marker.mark();
		KeyPointList kp2 = makeWithParallelDetector(fileName);
		Marker.release();

		System.out.println("========= COMPARING RESULTS ==========");
		Marker.mark();
		boolean b = compare(kp2, kp1);
		System.out.println(b ? "ok" : "FAILED");
//		kp1.compareToList(kp2);
		Marker.release();
	}
	
	static void doIt2(String findFilePattern) throws IOException {
		FindFileIterator ff = FindFileIterator.makeWithWildcard(findFilePattern, true, true);
		int count = 0;
		for (File f = ff.next(); f != null; f = ff.next()) {
			String fileName = f.getAbsolutePath();
			System.out.print("Processing image (" + (count++) + ") " + fileName);
			KeyPointList kp1 = makeWithOldWorkingDetector(fileName);
			KeyPointList kp2 = makeWithParallelDetector(fileName);
			boolean b = compare(kp1, kp2);
			System.out.println(b ? " ok" : " FAILED");
		}
	}
	
	public static void main(String[] args) throws IOException {
		doIt(Const.smallImage);
//		doIt("D:/Users/s/Images/DSC_0237.JPG");
//		doIt3("D:/Users/s/Images/DSC_0237.JPG");
//		doIt2("D:/Users/s/Images/*.jpg");
//		doIt2("D:/Users/s/Images/DSC_0237.JPG");
//		doIt("D:/Users/s/Images/Airports in Bulgaria BG_AIR.jpg");
		System.out.println("DONE.");
	}
}
