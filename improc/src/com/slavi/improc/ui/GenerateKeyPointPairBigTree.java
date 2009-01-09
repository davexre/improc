package com.slavi.improc.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointListSaver;
import com.slavi.improc.KeyPointPairBigTree;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.ui.SwtUtil;

public class GenerateKeyPointPairBigTree implements Callable<KeyPointPairBigTree> {

	List<String> images;
	AbsoluteToRelativePathMaker imagesRoot;
	AbsoluteToRelativePathMaker keyPointFileRoot;

	public GenerateKeyPointPairBigTree(List<String> images,
			AbsoluteToRelativePathMaker imagesRoot,
			AbsoluteToRelativePathMaker keyPointFileRoot) {
		this.images = images;
		this.imagesRoot = imagesRoot;
		this.keyPointFileRoot = keyPointFileRoot;
	}

	class ProcessOne implements Callable<Void> {

		String image;
		
		KeyPointPairBigTree bigTree;
		
		public ProcessOne(KeyPointPairBigTree bigTree, String image) {
			this.bigTree = bigTree;
			this.image = image;
		}
		
		public Void call() throws Exception {
			KeyPointList l = KeyPointListSaver.readKeyPointFile(imagesRoot, keyPointFileRoot, new File(image));
			synchronized(bigTree.keyPointLists) {
				bigTree.keyPointLists.add(l);
				String statusMessage = (bigTree.keyPointLists.size()) + "/" + images.size() + " " + image;
				System.out.println(statusMessage);
				SwtUtil.activeWaitDialogSetStatus(statusMessage, bigTree.keyPointLists.size() - 1);
			}
			for (KeyPoint kp : l) {
				bigTree.add(kp);
			}
			return null;
		}
	}
	
	public KeyPointPairBigTree call() throws Exception {
		Runtime runtime = Runtime.getRuntime();
		int numberOfProcessors = runtime.availableProcessors();
		final KeyPointPairBigTree result = new KeyPointPairBigTree();

		ExecutorService exec = Executors.newFixedThreadPool(numberOfProcessors + 1);
		try {
			ArrayList<Future<?>> tasks = new ArrayList<Future<?>>(images.size());
			for (int i = 0; i < images.size(); i++) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				String image = images.get(i);
				Future<?> f = exec.submit(new ProcessOne(result, image));
				tasks.add(f);
			}
		
			for (Future<?> task : tasks) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				task.get();
			}
		} finally {
			exec.shutdownNow();
		}

//		SwtUtl.activeWaitDialogSetStatus("Balancing the tree", 0);
//		System.out.println("Tree size        : " + result.getSize());
//		System.out.println("Tree depth before: " + result.getTreeDepth());
//		result.balanceIfNeeded();
//		System.out.println("Tree depth after : " + result.getTreeDepth());
		
		return result;
	}
}
