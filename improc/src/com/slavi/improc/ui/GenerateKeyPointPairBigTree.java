package com.slavi.improc.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointBigTree;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointListSaver;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.ui.SwtUtil;

public class GenerateKeyPointPairBigTree implements Callable<KeyPointBigTree> {

	ExecutorService exec;
	List<String> images;
	AbsoluteToRelativePathMaker imagesRoot;
	AbsoluteToRelativePathMaker keyPointFileRoot;
	AtomicInteger imageCounter = new AtomicInteger();
	
	public GenerateKeyPointPairBigTree(
			ExecutorService exec,
			List<String> images,
			AbsoluteToRelativePathMaker imagesRoot,
			AbsoluteToRelativePathMaker keyPointFileRoot) {
		this.exec = exec;
		this.images = images;
		this.imagesRoot = imagesRoot;
		this.keyPointFileRoot = keyPointFileRoot;
	}

	class ProcessOne implements Callable<Void> {

		String image;
		
		KeyPointBigTree bigTree;
		
		public ProcessOne(KeyPointBigTree bigTree, String image) {
			this.bigTree = bigTree;
			this.image = image;
		}
		
		public Void call() throws Exception {
			KeyPointList l = KeyPointListSaver.readKeyPointFile(exec, imagesRoot, keyPointFileRoot, new File(image));
			l.imageId = imageCounter.incrementAndGet();
			synchronized(bigTree.keyPointLists) {
				bigTree.keyPointLists.add(l);
				String statusMessage = (bigTree.keyPointLists.size()) + "/" + images.size() + " " + image;
				System.out.println(statusMessage);
				SwtUtil.activeWaitDialogSetStatus(statusMessage, bigTree.keyPointLists.size() - 1);
			}
			for (KeyPoint kp : l.items) {
				bigTree.add(kp);
			}
			return null;
		}
	}
	
	public KeyPointBigTree call() throws Exception {
		final KeyPointBigTree result = new KeyPointBigTree();

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

		return result;
	}
}
