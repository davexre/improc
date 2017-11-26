package com.slavi.ann.test.v2.test;

import com.slavi.math.matrix.Matrix;

import com.slavi.ann.test.MnistData.MnistPattern;

import java.util.List;

import com.slavi.ann.test.MnistData;
import com.slavi.ann.test.MnistData.MnistPattern;

public class MyMnistCheckData {

	void doIt() throws Exception {
		List<MnistPattern> pats = MnistData.readMnistSet(false);
		Matrix m = new Matrix(784, 1);
		Matrix max = new Matrix(784, 1);
		max.makeR(Double.MIN_VALUE);
		
		for (MnistPattern pat : pats) {
			for (int i = 0; i < pat.image.length; i++)
				m.setVectorItem(i, ((int) pat.image[i]) & 255);
			max.mMax(m, max);
		}
		System.out.println(m.toMatlabString("m"));
	}

	public static void main(String[] args) throws Exception {
		new MyMnistCheckData().doIt();
		System.out.println("Done.");
	}
}
