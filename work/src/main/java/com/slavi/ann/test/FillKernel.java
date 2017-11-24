package com.slavi.ann.test;

import java.util.Arrays;

import com.slavi.math.matrix.Matrix;

public class FillKernel {
	void doIt() throws Exception {
		Matrix m = new Matrix(4, 3);
		double[] tmpX = new double[m.getSizeX()];
		double[] tmpY = new double[m.getSizeY()];
		BellCurveDistribution.fillArray(tmpX, 0.3, (tmpX.length - 1) / 2);
		BellCurveDistribution.fillArray(tmpY, 0.3, (tmpY.length - 1) / 2);
		System.out.println(Arrays.toString(tmpX));
		System.out.println(Arrays.toString(tmpY));
		for (int i = m.getSizeX() - 1; i >= 0; i--)
			for (int j = m.getSizeY() - 1; j >= 0; j--)
				m.setItem(i, j, (tmpX[i] + tmpY[j]) * 0.5);
		m.printM("");
	}

	public static void main(String[] args) throws Exception {
		new FillKernel().doIt();
		System.out.println("Done.");
	}
}
