package com.slavi.improc.myadjust;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.slavi.improc.KeyPointPairList;

public class MyAdjustTask implements Callable<Void> {

	ArrayList<KeyPointPairList> keyPointPairLists;
	
	public MyAdjustTask(ArrayList<KeyPointPairList> keyPointPairLists) {
		this.keyPointPairLists = keyPointPairLists;
	}
	
	public Void call() throws Exception {
		ArrayList<KeyPointPairList> list1 = new ArrayList<KeyPointPairList>(1);
		list1.add(keyPointPairLists.get(0));
		MyPanoPairTransformLearner learner = new MyPanoPairTransformLearner(list1);
		learner.calculate();
		throw new Exception("Done");
	}

}
