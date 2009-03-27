package com.slavi.improc.old;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.myadjust.MyPanoPairTransformLearner3;
import com.slavi.improc.myadjust.MyPanoPairTransformer3;

public class MyAdjustTask implements Callable<MyPanoPairTransformer3> {

	ArrayList<KeyPointPairList> keyPointPairLists;
	
	public MyAdjustTask(ArrayList<KeyPointPairList> keyPointPairLists) {
		this.keyPointPairLists = keyPointPairLists;
	}
	
	public MyPanoPairTransformer3 call() throws Exception {
		MyPanoPairTransformLearner3 learner = new MyPanoPairTransformLearner3(keyPointPairLists);
		learner.calculate();
		return learner.tr;
	}
}
