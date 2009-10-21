package com.test.improc;

import com.slavi.improc.myadjust.SpherePanoTransformer;
import com.slavi.math.MathUtil;

public class Dummy {

	void dump(double dest[]) {
		double x = MathUtil.fixAngle2PI(dest[0]);
		double y = MathUtil.fixAngle2PI(dest[1]);
		System.out.println(MathUtil.rad2degStr(x) + "\t" + MathUtil.rad2degStr(y));
	}
	
	void asd() {
		double dest1[] = new double[2];
		double dest2[] = new double[2];

		dest1[0] = 0 * MathUtil.deg2rad;
		dest1[1] = 0 * MathUtil.deg2rad;

		SpherePanoTransformer.rotateForeward(dest1[0], dest1[1], 0, 80 * MathUtil.deg2rad, 0, dest2);
		dump(dest1);
		dump(dest2);
	}
	
	public static void main(String[] args) {
		new Dummy().asd();
	}
}
