package com.test.math;

import com.slavi.math.MathUtil;

public class TestDerivative {

	public static void main(String[] args) {
		double val0 = 20 * MathUtil.deg2rad;
		double delta = MathUtil.epsAngle * 1000000;
		double val1 = val0 + delta;
		
		double f0 = Math.sin(val0);
		double f2 = Math.sin(val1);
		double f1 = Math.sin(val1) + Math.cos(val1) * (val0 - val1);
		
		System.out.println(MathUtil.rad2degStr(val0));
		System.out.println(MathUtil.rad2degStr(delta));
		System.out.println(MathUtil.d4(f0));
		System.out.println(MathUtil.d4(f1));
		System.out.println(MathUtil.d4(f2));
		System.out.println(MathUtil.d20(f1-f0));
		System.out.println(MathUtil.d20(delta));
	}
	
}
