package com.slavi.improc.ui;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointBigTree;
import com.slavi.improc.KeyPointList;
import com.slavi.util.concurrent.TaskSetExecutor;
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

	class ProcessOne implements Callable<Void> {
		KeyPointBigTree bigTree;
		
		KeyPointList l;
		
		public ProcessOne(KeyPointBigTree bigTree, KeyPointList l) {
			this.bigTree = bigTree;
			this.l = l;
		}
		
		public Void call() throws Exception {
			synchronized(bigTree.keyPointLists) {
				bigTree.keyPointLists.add(l);
			}
			for (KeyPoint kp : l.items) {
				if (Thread.currentThread().isInterrupted())
					throw new InterruptedException();
				bigTree.add(kp);
			}
			SwtUtil.activeWaitDialogProgress(1);
			return null;
		}
	}
	
	public KeyPointBigTree call() throws Exception {
		final KeyPointBigTree result = new KeyPointBigTree();

		TaskSetExecutor taskSet = new TaskSetExecutor(exec);
		for (KeyPointList l : kpl) {
			taskSet.add(new ProcessOne(result, l));
		}
		taskSet.addFinished();
		taskSet.get();
		return result;
	}
}
