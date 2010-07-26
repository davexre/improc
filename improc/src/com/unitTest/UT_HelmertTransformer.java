package com.unitTest;

import com.test.math.TestHelmert2DTransformer.MyTestHelmert2DTransformer;

public class UT_HelmertTransformer {

	public void testGetSetParams() {
		MyTestHelmert2DTransformer tr = new MyTestHelmert2DTransformer();
		double p1[] = new double[4];
		p1[0] = 2.2;
		p1[1] = -3.4;
		p1[2] = 5;
		p1[3] = 6;
		
		tr.a = p1[0];
		tr.b = p1[1];
		tr.c = p1[2];
		tr.d = p1[3];
		
		double params[] = new double[4];
		tr.getParams(params);
		tr.setParams(params[0], params[1], params[2], params[3]);
		
		double p2[] = new double[4];
		p2[0] = tr.a;
		p2[1] = tr.b;
		p2[2] = tr.c;
		p2[3] = tr.d;

		TestUtils.assertEqual("", p1, p2);
	}
	
	public static void main(String[] args) {
		UT_HelmertTransformer test = new UT_HelmertTransformer();
		test.testGetSetParams();
		System.out.println("Done.");
	}
}
