package com.slavi.improc.myadjust;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.slavi.improc.KeyPointPairList;

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
