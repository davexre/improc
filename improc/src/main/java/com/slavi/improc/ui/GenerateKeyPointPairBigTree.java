package com.slavi.improc.ui;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointBigTree;
import com.slavi.improc.KeyPointList;
import com.slavi.util.concurrent.TaskSet;
import com.slavi.util.swt.SwtUtil;

public class GenerateKeyPointPairBigTree implements Callable<KeyPointBigTree> {

	ExecutorService exec;
	ArrayList<KeyPointList> kpl;
	
	public GenerateKeyPointPairBigTree(
			ExecutorService exec,
			ArrayList<KeyPointList> kpl) {
		this.exec = exec;
		this.kpl = kpl;
	}

	class ProcessOne implements Runnable {
		KeyPointBigTree bigTree;
		
		KeyPointList l;
		
		public ProcessOne(KeyPointBigTree bigTree, KeyPointList l) {
			this.bigTree = bigTree;
			this.l = l;
		}
		
		public void run() {
			synchronized(bigTree.keyPointLists) {
				bigTree.keyPointLists.add(l);
			}
			for (KeyPoint kp : l.items) {
				if (Thread.currentThread().isInterrupted())
					throw new CompletionException(new InterruptedException());
				bigTree.add(kp);
			}
			SwtUtil.activeWaitDialogProgress(1);
		}
	}
	
	public KeyPointBigTree call() throws Exception {
		final KeyPointBigTree result = new KeyPointBigTree();

		TaskSet taskSet = new TaskSet(exec);
		for (KeyPointList l : kpl) {
			taskSet.add(new ProcessOne(result, l));
		}
		taskSet.run().get();
		return result;
	}
}
