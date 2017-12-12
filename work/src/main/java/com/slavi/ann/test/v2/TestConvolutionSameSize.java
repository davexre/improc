package com.slavi.ann.test.v2;

import com.slavi.ann.test.v2.ConvolutionSameSizeLayer.LayerWorkspace;
import com.slavi.math.matrix.Matrix;

public class TestConvolutionSameSize {

	void doIt() throws Exception {
		ConvolutionSameSizeLayer l = new ConvolutionSameSizeLayer(2, 2, 1);
		l.scale = 1;
		l.kernel.makeR(0);
		l.kernel.setItem(1, 1, 1);
		LayerWorkspace w = l.createWorkspace();
		Matrix m = new Matrix(3, 3);
		m.setItem(0, 0, 1);
		m.setItem(1, 0, 2);
		m.setItem(2, 0, 3);
		m.setItem(0, 1, 4);
		m.setItem(1, 1, 5);
		m.setItem(2, 1, 6);
		m.setItem(0, 2, 7);
		m.setItem(1, 2, 8);
		m.setItem(2, 2, 9);
		Matrix o = w.feedForward(m);
		System.out.println(o.toString());
	}

	public static void main(String[] args) throws Exception {
		new TestConvolutionSameSize().doIt();
		System.out.println("Done.");
	}
}
