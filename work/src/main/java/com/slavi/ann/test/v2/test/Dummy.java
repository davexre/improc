package com.slavi.ann.test.v2.test;

import java.util.Arrays;

import com.slavi.ann.test.BellCurveDistribution;
import com.slavi.ann.test.v2.connection.ConvolutionLayer;
import com.slavi.improc.parallel.PGaussianFilter;

public class Dummy {

	public void doIt(String[] args) throws Exception {
		ConvolutionLayer cl = new ConvolutionLayer(3, 3, 1);
		System.out.println(cl.kernel);
		System.out.println(cl.kernel.sumAll());

		double d[] = new double[5];
		BellCurveDistribution.fillArray(d, 1.5, (d.length - 1) / 2);
		System.out.println(Arrays.toString(d));
		System.out.println("----");


		double sum = PGaussianFilter.fillArray(d, 1.5);

		System.out.println(Arrays.toString(d));
		System.out.println(sum);
		System.out.println("----");
	}

	public static void main(String[] args) throws Exception {
		new Dummy().doIt(args);
		System.out.println("Done.");
	}
}
