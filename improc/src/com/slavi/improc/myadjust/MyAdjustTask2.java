package com.slavi.improc.myadjust;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.slavi.improc.PanoList;
import com.slavi.improc.PanoPairList;

public class MyAdjustTask2 implements Callable<Void> {

	PanoList panoList;
	
	public MyAdjustTask2(PanoList panoList) {
		this.panoList = panoList;
	}
	
	public Void call() throws Exception {
		ArrayList<PanoPairList> list1 = new ArrayList<PanoPairList>(1);
		list1.add(panoList.items.get(0));
		MyPanoPairTransformLearner2 learner = new MyPanoPairTransformLearner2(list1);
		learner.calculate();
		throw new Exception("Done");
	}

}
