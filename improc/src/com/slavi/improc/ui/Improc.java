package com.slavi.improc.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.slavi.improc.KeyPointBigTree;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.myadjust.ValidateKeyPointPairList;
import com.slavi.improc.myadjust.sphere2.CalculatePanoramaParamsSpherical2;
import com.slavi.improc.myadjust.sphere2.MyGeneratePanoramasSphere2;
import com.slavi.improc.myadjust.zyz.CalculatePanoramaParamsZYZ;
import com.slavi.improc.myadjust.zyz.MyGeneratePanoramasZYZ;
import com.slavi.util.Marker;
import com.slavi.util.Util;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.file.FindFileIterator;
import com.slavi.util.ui.SwtUtil;

public class Improc {
	
	Shell parent;
	
	public void doTheJob(ExecutorService exec) throws Exception {
		parent = new Shell(SWT.NONE);
		parent.setBounds(-10, -10, 0, 0); // fixes a bug in SWT
		parent.open();
		
		Settings settings = Settings.getSettings(parent);
		if (settings == null)
			return;
		SwtUtil.openTaskManager(parent, true);
		Marker.mark("Image Processing");

		AbsoluteToRelativePathMaker imagesRoot = new AbsoluteToRelativePathMaker(settings.imagesRootStr);
		AbsoluteToRelativePathMaker keyPointFileRoot = new AbsoluteToRelativePathMaker(settings.keyPointFileRootStr);
		
		FindFileIterator imagesIterator = FindFileIterator.makeWithWildcard(imagesRoot.getFullPath("*.jpg"), true, true);
		ArrayList<String> images = SwtUtil.openWaitDialog(parent, "Searching for images", new EnumerateImageFiles(imagesIterator), -1);
		Collections.sort(images);
		SwtUtil.openWaitDialog(parent, "Generating key point files", 
				new GenerateKeyPointFiles(exec, images, imagesRoot, keyPointFileRoot), images.size() - 1);
		
		System.out.println("---------- Generating key point BIG tree");
		KeyPointBigTree bigTree = SwtUtil.openWaitDialog(parent, "Generating key point BIG tree", 
				new GenerateKeyPointPairBigTree(exec, images, imagesRoot, keyPointFileRoot), 
				images.size() - 1);
		System.out.println("Tree size  : " + bigTree.getSize());
		System.out.println("Tree depth : " + bigTree.getTreeDepth());
		System.out.println("Tree size          : " + bigTree.getSize());
		System.out.println("Tree depth         : " + bigTree.getTreeDepth());
		System.out.println("Perfect tree depth : " + bigTree.getPerfectTreeDepth());
		
		System.out.println("---------- Generating key point pairs from BIG tree");
		ArrayList<KeyPointPairList> kppl = SwtUtil.openWaitDialog(parent, "Generating key point pairs from BIG tree", 
				new GenerateKeyPointPairsFromBigTree(exec, bigTree),
				images.size() - 1);
		images = null;
		bigTree = null;

		System.out.println("Key point pairs lists to be validated: " + kppl.size());
		
		System.out.println("---------- Validating key point pairs");
		ArrayList<KeyPointPairList> validkppl = SwtUtil.openWaitDialog(parent, "Validating key point pairs", 
				new ValidateKeyPointPairList(exec, kppl),
				kppl.size() - 1);
		kppl = null;

		if (true) {
			System.out.println("---------- Calculating panorama parameters");
			ArrayList<ArrayList<KeyPointPairList>> panos = SwtUtil.openWaitDialog(parent, "Calculating panorama parameters", 
					new CalculatePanoramaParamsSpherical2(exec, validkppl, keyPointFileRoot, settings.outputDirStr,
							settings.pinPoints, settings.useColorMasks, settings.useImageMaxWeight), -1);
			
			System.out.println("---------- Generating panorama images");
			SwtUtil.openWaitDialog(parent, "Generating panorama images", 
					new MyGeneratePanoramasSphere2(exec, panos, settings.outputDirStr,
							settings.pinPoints, settings.useColorMasks, settings.useImageMaxWeight), -1);
		} else {	
			System.out.println("---------- Calculating panorama parameters");
			ArrayList<ArrayList<KeyPointPairList>> panos = SwtUtil.openWaitDialog(parent, "Calculating panorama parameters", 
					new CalculatePanoramaParamsZYZ(exec, validkppl, keyPointFileRoot, settings.outputDirStr,
							settings.pinPoints, settings.useColorMasks, settings.useImageMaxWeight), -1);
			
			System.out.println("---------- Generating panorama images");
			SwtUtil.openWaitDialog(parent, "Generating panorama images", 
					new MyGeneratePanoramasZYZ(exec, panos, settings.outputDirStr,
							settings.pinPoints, settings.useColorMasks, settings.useImageMaxWeight), -1);
		}
		Marker.release();
		System.out.println("Done.");
	}
	
	public static class MyThreadFactory implements ThreadFactory {
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setPriority(Thread.MIN_PRIORITY);
			return thread;
		}
	}
	
	public static void main(String[] args) throws Exception {
		Runtime runtime = Runtime.getRuntime();
		int numberOfProcessors = runtime.availableProcessors();
//		ExecutorService exec = Executors.newFixedThreadPool(numberOfProcessors + 1, new MyThreadFactory());
//		ExecutorService exec = Util.newBlockingThreadPoolExecutor(numberOfProcessors + 1, new MyThreadFactory());
		ExecutorService exec = Util.newBlockingThreadPoolExecutor(1, new MyThreadFactory());

		Improc application = new Improc();
		try {
			application.doTheJob(exec);
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			exec.shutdown();
			application.parent.close();
		}
		SwtUtil.closeTaskManager();
	}
}
