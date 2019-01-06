package com.slavi.ann.test.v2.test;

import java.util.List;

import com.slavi.ann.test.dataset.MatrixDataPointPair;
import com.slavi.ann.test.dataset.MatrixTestData;

public class TestLsa {

	void doIt() throws Exception {
		List<MatrixDataPointPair> data = MatrixTestData.generatePoints(true);

	}

	public static void main(String[] args) throws Exception {
		new TestLsa().doIt();
		System.out.println("Done.");
	}
}
